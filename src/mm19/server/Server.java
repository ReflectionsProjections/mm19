package mm19.server;

/**
 * Standalone MechMania XIX server.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import mm19.game.Constants;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.util.text.BasicTextEncryptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {
	// Static constant logger declaration
	public static final Logger serverLog = Logger.getLogger(Server.class
			.getName());

	// Server constants for now, configurable later
	public static final int PORT = 6969;

	public static final int MAX_THREADS = 8;
	public static final String LOG_PATH = "server_log.txt";
	public static final Level LOG_LEVEL = Level.INFO;

	// Bookkeeping
	private static boolean[] connected;
	private static int playersConnected;

	private static GameLogger visualizerLog = null;
	private static String visualizerLogURL = "log.out";

	// Sockets
	private static ServerSocket socket = null;
	private static Socket[] clientSockets;

	// Concurrent stuff
	private static RequestRunnable[] player;
	private static ExecutorService threadPool = null;
	private static boolean starting = false;
	private static boolean running = false;

	// For communication with the connecting client
	private static BufferedReader in;
	private static PrintWriter out;

	// Security
	private static String[] playerToken;
	private static BasicTextEncryptor bte;

	public static void main(String[] args) {

		// Set up the server, including logging and socket to listen on
		boolean success = initServer();
        //TODO I think something should happen with 'success' before it is writen again... -Eric
		success = API.initAPI();
		
		visualizerLog = new GameLogger(Server.visualizerLogURL);

		if (!success) {
			serverLog.log(Level.SEVERE, "Fatal error: unable to start server. Bailing out.");
			System.exit(1);
		}

		// Run the server until the game ends
		run();

		// End the server and clean up 
		 
		 
	}

	@SuppressWarnings("unused")
	private static boolean initServer() {

		clientSockets = new Socket[Constants.PLAYER_COUNT];

		playerToken = new String[Constants.PLAYER_COUNT];
		for(int i = 0; i < playerToken.length; i++) {
			playerToken[i] = "";
		}
		
		connected = new boolean[Constants.PLAYER_COUNT];
		for(int i = 0; i < connected.length; i++) {
			connected[i] = false;
		}

		bte = new BasicTextEncryptor();

        //TODO We are calling to string on an array here
		bte.setPassword((new RandomSaltGenerator().generateSalt(10)).toString());

		// TODO: Set up logging to a file
		serverLog.setLevel(LOG_LEVEL);

		// Set up the socket
		try {
			socket = new ServerSocket(PORT);
			serverLog.log(Level.INFO, "Server listening on port: " + PORT);
		} catch (IOException e) {
			serverLog.log(Level.SEVERE, "Failed to open socket on port: "
					+ PORT, e);
			return false;
		}

		// Set up the thread pool
		threadPool = Executors.newFixedThreadPool(MAX_THREADS);

		return true;
	}

	private static void run() {
		int currPlayerID = -1;
		serverLog.log(Level.INFO, "Starting server run loop");
		starting = true;
		Socket clientSocket = null;
		// Long polling and connecting/authenticating new players as they connect.
		while (starting) {
			try {
				// Listen for a new connection
				clientSocket = socket.accept();

				// players are already connected
				currPlayerID = getValidPlayerID();
				if (currPlayerID == -1) {
					serverLog.log(Level.WARNING, "The server needs to be restarted");
					continue;
				}

				serverLog.log(Level.INFO, "Connection received.");
				serverLog.log(Level.INFO, "Waiting on player for new player data.");

				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);

				// Blocks until it gets a response from the client, hopefully a
				// JSONObject with a playerName key.
				String s = in.readLine();

				serverLog.log(Level.INFO, "Received player info");

				try {
					// Check to see if we are sent valid data.
					JSONObject obj = new JSONObject(s);
					if (obj.has("playerName")) {
						// Create the player token
						String name = obj.getString("playerName");

                        //TODO We are calling toString on an array here
						name = name + (new RandomSaltGenerator().generateSalt(10).toString());

						clientSockets[currPlayerID] = clientSocket;
						playerToken[currPlayerID] = name;
						connected[currPlayerID] = true;
						boolean successfullyAdded = API.newData(obj,
								encrypt(name));
						if (successfullyAdded) {

							serverLog.log(Level.INFO,
									"Successfully initialized player!");
						} else {
							serverLog
									.log(Level.INFO,
											"Couldn't initialize ships for player, dropping connection");
							disconnectPlayer(name);
							continue;
						}
					}
				} catch (JSONException e) {
					serverLog
							.log(Level.WARNING,
									"Player didn't have playerName, couldn't authenticate");
					serverLog.log(Level.INFO,
							"Notifying and Disconnecting player");

					JSONObject ret = new JSONObject();

					try {
						ret.put("responseCode", 400);
						ret.put("playerToken", playerToken[currPlayerID]);
						ret.put("playerName", "");
						ret.put("resources", 0);
						ret.put("ships", new JSONArray());
						ret.put("shipActionResults", new JSONArray());
						ret.put("hitReport", new JSONArray());
						ret.put("pingReport", new JSONArray());
						ret.append(
								"error",
								"You need to include \"playerName\" with your team name so we can authenticate you.");

						out.println(ret.toString());
					} catch (JSONException e1) {
						e1.printStackTrace();
					}

					out.flush();
					disconnectPlayer(playerToken[currPlayerID]);
					// Drop the client because he had bad data.
					continue;
				}

				// Making sure we don't connect more than the max number of
				// players.
				serverLog.log(Level.INFO, "Players connected: "
						+ ++playersConnected);

			} catch (IOException e) {
				serverLog.log(Level.SEVERE, "Unexpected error accepting "
						+ "client connection.", e);
			}

			// Create a new task for the incoming connection and put it in the
			// thread pool
			if (currPlayerID != -1) {
				RequestRunnable task = new RequestRunnable(clientSocket,
						playerToken[currPlayerID], currPlayerID);
				threadPool.execute(task);
			}
			
			if(playersConnected == 2) {
				API.notifyTurn(0);
			}
		}

	}

	private static int getValidPlayerID() {
		if (!connected[0])
			return 0;
		else if (!connected[1])
			return 1;

		return -1;
	}

	public static int disconnectPlayer(String token) {
		if (encrypt(playerToken[0]).compareTo(token) == 0) {
			connected[0] = false;

			try {
				clientSockets[0].close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			clientSockets[0] = null;
			playerToken[0] = "";
			--playersConnected;
			return 0;
		} else if (encrypt(playerToken[1]).compareTo(token) == 0) {
			connected[1] = false;

			try {
				clientSockets[1].close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			clientSockets[1] = null;
			playerToken[1] = "";
			--playersConnected;
			return 1;
		}

		return -1;
	}

	// Returns 1 if player 1 is authenticated, returns 2 if player 2 is
	// authenticated, returns -1 if neither.
	private static int authenticate(String token) {

		if (connected[0]) {
			if (bte.decrypt(token).equals(playerToken[0])) {
				return 0;
			}
		} 
		if (connected[1]) {
			if (bte.decrypt(token).equals(playerToken[1])) {
				return 1;
			}
		}
		return -1;
	}

	private static String encrypt(String s) {
		return bte.encrypt(s);
	}

	public static synchronized void sendPlayer(JSONObject player1, String authP1) {
		// Authenticate the player.
		int playerID = authenticate(authP1);

		if (playerID == -1) {
			serverLog
					.log(Level.WARNING,
							"Couldn't authenticate player when trying to send a message");
			return;
		}

		try {
			out = new PrintWriter(clientSockets[playerID].getOutputStream(),
					true);
			out.println(player1);
			out.flush();

		} catch (IOException e) {
			serverLog.log(Level.WARNING, "Error communicating with player "
					+ playerID);
			e.printStackTrace();
		}
	}

	public static void winCondition(int player) {
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(player);
	}

	public static synchronized void sendAPI(JSONObject obj) {
		API.decodeTurn(obj);
	}

	public static void printToVisualizerLog(String string) {
		visualizerLog.log(string);
	}
}
