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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.RandomSaltGenerator;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {
	// Static constant logger declaration
	public static final Logger serverLog =
		Logger.getLogger(Server.class.getName());

	// Server constants for now, configurable later
	public static final int PORT = 6969;
	public static final int MAX_PLAYERS = 2;
	public static final int MAX_THREADS = 8;
	public static final String LOG_PATH = "server_log.txt";
	public static final Level LOG_LEVEL = Level.INFO;

	// Bookkeeping
	private static boolean[] connected;
	private static int playersConnected;
	
	// Sockets
	private static ServerSocket socket = null;
	private static Socket[] clientSockets;
	
	// Concurrent stuff
	private static RequestRunnable[] player;
	private static ExecutorService threadPool = null;
	private static boolean running = false;
	
	// For communication with the connecting client
	private static BufferedReader in;
	private static PrintWriter out;
	
	// The API
	private static API mAPI;
	
	// Security
	private static String[] playerToken;
	private static StandardPBEStringEncryptor pbe;

	public static void main(String[] args) {
		// Set up the server, including logging and socket to listen on
		boolean success = initServer();
		if (!success) {
			serverLog.log(Level.SEVERE,
					"Fatal error: unable to start server. Bailing out.");
			System.exit(1);
		}

		// Run the server until the game ends
		run();

		// End the server and clean up
	}

	private static boolean initServer() {
		
		mAPI = new API();
		clientSockets = new Socket[2];
		
		playerToken = new String[2];
		playerToken[0] = "";
		playerToken[1] = "";
		
		pbe = new StandardPBEStringEncryptor();
		pbe.setPassword(new RandomSaltGenerator().generateSalt(50).toString());
		pbe.initialize();
		
		connected = new boolean[2];
		connected[0] = false;
		connected[1] = false;
		
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
		running = true;
		
		while (running) {
			Socket clientSocket = null;
			try {
				// Listen for a new connection
				clientSocket = socket.accept();
				
				// TODO: Do something more than just drop the player if the max players are already connected
				currPlayerID = getValidPlayerID();
				if(currPlayerID == -1) {
					serverLog.log(Level.WARNING, "Already at maximum number of players");
					continue;
				}
				
				serverLog.log(Level.INFO, "Connection received.");
				serverLog.log(Level.INFO, "Waiting on player for playerName.");
				
				in = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				
				//TODO: Make this more formal
				out.println("Please send playerName");
				out.flush();
				
				// Blocks until it gets a response from the client, hopefully a JSONObject with a playerName key.
				String b = in.readLine();
				
				serverLog.log(Level.INFO, "Recieved player info");

				try {
					//Check to see if we are sent valid data.
					JSONObject obj = new JSONObject(b);
					if(obj.has("playerName")) {
						// Create the player token
						String name = obj.getString("playerName");
						name = name + (new RandomSaltGenerator().generateSalt(10).toString());
						playerToken[currPlayerID] = name;
					}
				}
				catch (JSONException e) {
					serverLog.log(Level.WARNING, "Player didn't have playerName, couldn't authenticate");
					serverLog.log(Level.INFO, "Notifying and Disconnecting player");
					
					//TODO: Make this more formal
					out.println("You need to include \"playerName\" with your team name so we can authenticate you.");
					out.flush();
					//Drop the client because he had bad data.
					continue;
				}
				
				// Making sure we don't connect more than the max number of players.
				clientSockets[currPlayerID] = clientSocket;
				connected[currPlayerID] = true;
			 	serverLog.log(Level.INFO, "Players connected: " + ++playersConnected);	
			 	
				
			} catch (IOException e) {
				serverLog.log(Level.SEVERE, "Unexpected error accepting " +
						"client connection.", e);
			}
			// Create a new task for the incoming connection and put it in the
			// thread pool
			if(currPlayerID != -1) {
				RequestRunnable task = new RequestRunnable(clientSocket, mAPI, playerToken[currPlayerID]);
				threadPool.execute(task);
			}
		}

	}
	
	private static int getValidPlayerID() {
		if(!connected[0])
			return 0;
		else if(!connected[1]) 
			return 1;
		
		return -1;
	}
	
	public static int disconnectPlayer(String token) {
		if(encrypt(playerToken[0]).compareTo(token) == 0) {
			connected[0] = false;
			clientSockets[0] = null;
			playerToken[0] = "";
			--playersConnected;
			return 0;
		}
		else if(encrypt(playerToken[1]).compareTo(token) == 0) {
			connected[1] = false;
			clientSockets[1] = null;
			playerToken[1] = "";
			--playersConnected;
			return 1;
		}
		
		return -1;
	}
	
	// Returns 1 if player 1 is authenticated, returns 2 if player 2 is authenticated, returns -1 if neither.
	private static int authenticate(String token) {
		if(connected[0]) {
			if(encrypt(playerToken[0]).compareTo(token) == 0) {
				return 0;
			}
		}
		else if(connected[1]) {
			if(encrypt(playerToken[1]).compareTo(token) == 0) {
				return 1;
			}
		}
		return -1;
	}
	
	private  static String encrypt(String s) {
		return pbe.encrypt(s);
	}
	
	public static void sendPlayer(JSONObject player1, String authP1) {
		// Authenticate the player.
		int playerID = authenticate(authP1);
		
		if(playerID == -1) {
			serverLog.log(Level.WARNING, "Couldn't authenticate player when trying to send a message");
			return;
			
		}
		
		try {
			out = new PrintWriter(clientSockets[playerID].getOutputStream(), true);
			out.println(player1);
			out.flush();
			
		} catch (IOException e) {
			serverLog.log(Level.WARNING, "Error communicating with player " + playerID);
			e.printStackTrace();
		}
	}

	public static void winCondition(String authP1) {
		// TODO Auto-generated method stub
		
		
	}
	
	public static synchronized void sendAPI() {
		
	}
}
