package mm19.server;

/**
 * @author mm19
 * 
 * MechMania XIX server.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mm19.api.API;
import mm19.api.PlayerTurn;
import mm19.exceptions.APIException;
import mm19.game.Constants;
import mm19.logging.VisualizerLogger;

import org.jasypt.util.text.BasicTextEncryptor;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {
	// Static constant logger declaration
	public static final Logger serverLog = Logger.getLogger(Server.class
			.getName());

	// API
	private static API api;
	// Server constants for now, configurable later
	public static final int PORT = 6969;

	public static final int MAX_THREADS = 2;
	public static final String LOG_PATH = "server_log.txt";
	public static final Level LOG_LEVEL = Level.INFO;

	// Bookkeeping
	private static boolean[] connected;
	private static int playersConnected;
	private static VisualizerLogger visualizerLog = null;
	private static String visualizerLogURL = "log.out";

	// Sockets
	private static ServerSocket socket = null;
	private static Socket[] clientSockets;

	// Concurrent stuff
	public static ExecutorService threadPool = null;
	private static boolean starting = false;

	// For communication with the connecting client
	private static BufferedReader in;
	private static PrintWriter out;

	// Security
	private static String[] playerToken;
	private static BasicTextEncryptor bte;

	// Interrupts
	public static final int TURN_TIME_LIMIT = 1000000;
	private static Timer interruptTimer;
	
	// winnerString
	private static JSONObject winJSON;

	/**
	 * Starting point for the game.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	    
		// Set up the server, including logging and socket to listen on
		boolean success = initServer();
		winJSON = null;

		if (!success) {
			serverLog.log(Level.SEVERE,
					"Fatal error: unable to start server. Bailing out.");
			System.exit(1);
		}


        if(args.length < 1) {
            visualizerLog = new VisualizerLogger(Server.visualizerLogURL);
        }
        else {
            visualizerLog = new VisualizerLogger(args[0]);
        }

		// Run the server until the game ends
		run();

		// End the server and clean up

	}

	/**
	 * Initialize the server
	 * 
	 * @return if the server was properly initialized
	 */
	private static boolean initServer() {

		interruptTimer = new Timer();
		clientSockets = new Socket[Constants.PLAYER_COUNT];
		api = API.getAPI();
		playerToken = new String[Constants.PLAYER_COUNT];
		for (int i = 0; i < playerToken.length; i++) {
			playerToken[i] = "";
		}

		connected = new boolean[Constants.PLAYER_COUNT];
		for (int i = 0; i < connected.length; i++) {
			connected[i] = false;
		}

		bte = new BasicTextEncryptor();
		bte.setPassword("mm19");

		// Set up to-file logging
        FileHandler fileLogHandler;
        try
        {
            // TRUE -> console is stopped and log is pointed to file; FALSE -> nothing changes
            boolean logToFile = false;
            if(logToFile)
            {
                fileLogHandler = new FileHandler(LOG_PATH);
                fileLogHandler.setFormatter(new SimpleFormatter());
                
                System.out.println("Console logging going down; File logging going up at location " + LOG_PATH);
                // Stop to-console logging
                serverLog.setUseParentHandlers(false);
                // Start to-file logging
                serverLog.addHandler(fileLogHandler);
            }
        }
        catch(IOException e)
        {
            serverLog.log(Level.SEVERE, "Fatal error: Unable to start server-to-file logging. Bailing out.");
            System.exit(1); // Does this exit code need to be changed?
        }

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

	/**
	 * Run the server, accept incoming clients and spawn worker threads to
	 * accept further requests from them.
	 * 
	 * Starts the game once enough clients have joined.
	 */
	private static void run() {
		int currPlayerID = -1;
		serverLog.log(Level.INFO, "Starting server run loop");
		starting = true;
		Socket clientSocket = null;
		// Long polling and connecting/authenticating new players as they
		// connect.
		while (starting) {
			try {
				// Listen for a new connection
				clientSocket = socket.accept();

				// players are already connected
				currPlayerID = getValidPlayerID();
				if (currPlayerID == -1) {
					serverLog.log(Level.WARNING,
							"The server needs to be restarted");
					continue;
				}

				serverLog.log(Level.INFO, "Connection received.");
				serverLog.log(Level.INFO,
						"Waiting on player for new player data.");

				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);

				// Blocks until it gets a response from the client, hopefully a
				// JSONObject with a playerName key.
				String s = in.readLine();

				serverLog.log(Level.INFO, "Received player info");

				// We can think of something more interesting here, but security
				// isn't the biggest issue at the moment.
				String token = "player" + playersConnected;

				try {
					api.addPlayer(s, encrypt(token));

					serverLog.log(Level.INFO,
							"Successfully initialized player!");
					clientSockets[currPlayerID] = clientSocket;
					playerToken[currPlayerID] = token;
					connected[currPlayerID] = true;

					serverLog.log(Level.INFO, "Players connected: "
							+ ++playersConnected);

					RequestRunnable task = new RequestRunnable(clientSocket,
							encrypt(playerToken[currPlayerID]), currPlayerID);
					threadPool.execute(task);

					if (api.getStarted()) {
						startGame();
						starting = false;
					}

				} catch (APIException e) {
					serverLog
							.log(Level.INFO,
									"Couldn't initialize ships for player, dropping connection");
					PlayerTurn turn = new PlayerTurn();
					turn.addError(e.getMessage());
					out.println(turn.toJSON());
					out.flush();
					continue;
				}
			} catch (IOException e) {
				serverLog.log(Level.SEVERE, "Unexpected error accepting "
						+ "client connection.", e);
				starting = false;
			}
		}

	}

	/**
	 * Get valid playerID for the connecting client
	 * 
	 * @return A valid player ID, -1 if no more players can join.
	 */
	private static int getValidPlayerID() {
		if (!connected[0])
			return 0;
		else if (!connected[1])
			return 1;

		return -1;
	}

	/**
	 * Authenticates a player by returning the player ID associated with it
	 * 
	 * @param token
	 *            The token to be authenticated
	 * @return The player ID, -1 if authentication failed
	 */
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

	/**
	 * Starts the game once enough players have connected
	 */
	private static void startGame() {
		PlayerTurn turn = api.getPlayerTurn(0);
		turn.setNotify();
		sendToPlayer(turn.toJSON(), encrypt(playerToken[0]));
		turn.resetTurn();

		ServerInterruptTask.PLAYER_TO_INTERRUPT = 0;
		
		// This line schedules the interrupts
		//interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	/**
	 * Encrypts the string, used for encrypting tokens
	 * 
	 * @param s
	 *            The string to be encrypted
	 * @return The encrypted string
	 */
	private static String encrypt(String s) {
		return bte.encrypt(s);
	}

	/**
	 * Send a JSON object to a client, which is determined by authenticating the
	 * token
	 * 
	 * @param json
	 *            The JSON object to be sent
	 * @param token
	 *            The token to be authenticated
	 */
	public static void sendToPlayer(JSONObject json, String token) {
		// Authenticate the player.
		int playerID = authenticate(token);

		if (playerID == -1) {
			serverLog
					.log(Level.WARNING,
							"Couldn't authenticate player when trying to send a message");
			return;
		}

		try {
			out = new PrintWriter(clientSockets[playerID].getOutputStream(),
					true);
			out.println(json);
			out.flush();

		} catch (IOException e) {
			serverLog.log(Level.WARNING, "Error communicating with player "
					+ playerID);
			e.printStackTrace();
		}
	}

	/**
	 * Called from RequestRunnable when a client submits a turn, sends to the
	 * API for further handling and reacts accordingly.
	 * 
	 * @param obj
	 *            The turn to submit
	 * @param token
	 *            The token to authenticate
	 */
	public static void submitTurn(JSONObject obj, String token) {
		int playerID = authenticate(token);
		if (playerID == -1) {
			serverLog.log(Level.WARNING,
					"Player not authenticated when trying to send to API");
			return;
		}

		// Rejecting the player's request because it's not his turn.
		if (playerID != api.getCurrPlayerID()) {
			PlayerTurn turn = api.getPlayerTurn(playerID);
			turn.addError("It is not your turn!");
			turn.setError();
			sendToPlayer(turn.toJSON(), token);
			turn.resetTurn();
			return;
		}

		interruptTimer.cancel();
		interruptTimer.purge();
		interruptTimer = new Timer();

		int opponentID = api.getCurrOpponentID();

		// Once you call sendToAPI, "playerID" and "opponentID" are switched,
		// since the turn switches, which is why playerID and opponentID are
		// preserved beforehand.
		sendToAPI(obj, playerID);

		PlayerTurn playerTurn = api.getPlayerTurn(playerID);
		PlayerTurn opponentTurn = api.getPlayerTurn(opponentID);

		// Adding turn to visualizer log
		try {
			visualizerLog.addTurn(playerTurn.getLoggingJSON());//toLoggingJSON());//
		} catch (JSONException e) {
			
		}
		if(playerTurn.hasWon() || playerTurn.hasLost()){
            if(playerTurn.hasWon()) winJSON = playerTurn.winnerJSON();
            else winJSON = opponentTurn.winnerJSON();
        }

		sendToPlayer(playerTurn.toJSON(), encrypt(playerToken[playerID]));
		sendToPlayer(opponentTurn.toJSON(), encrypt(playerToken[opponentID]));

		playerTurn.resetTurn();
		opponentTurn.resetTurn();

		if (api.getWinner()) {
			shutdown();
			return;
		}
		// Set the new player to interrupt.
		ServerInterruptTask.PLAYER_TO_INTERRUPT = api.getCurrPlayerID();
		interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	/**
	 * Sends information (a turn) to the API for processing
	 * 
	 * @param obj
	 *            The turn to be processed
	 * @param playerID
	 *            The player ID of the turn
	 */
	private static void sendToAPI(JSONObject obj, int playerID) {
		try {
			api.processTurn(obj, playerID);
		} catch (APIException e) {
			api.getPlayerTurn(playerID).addError(e.getMessage());
		}
	}

	/**
	 * Called from a ServerInterruptTask when the timer goes off, interrupts a
	 * current player and sends him a message for taking too long.
	 * 
	 * @param playerID
	 *            The player to interrupt
	 */
	public static void interruptPlayer(int playerID) {
		int opponentID = api.getCurrOpponentID();

		// This will switch the player/opponent IDs
		api.notifyInterrupt();

		PlayerTurn turn = api.getPlayerTurn(playerID);
		turn.setInterrupt();
		sendToPlayer(turn.toJSON(), encrypt(playerToken[playerID]));
		turn.resetTurn();

		turn = api.getPlayerTurn(opponentID);
		turn.setNotify();
		sendToPlayer(turn.toJSON(), encrypt(playerToken[opponentID]));
		turn.resetTurn();

		ServerInterruptTask.PLAYER_TO_INTERRUPT = api.getCurrPlayerID();
		interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	/**
	 * Shuts down the server and writes the game to the log file
	 */
	private static void shutdown() {
	    visualizerLog.addTurn(winJSON);	    
	    visualizerLog.writeToFile();
		visualizerLog.close();

		starting = false;

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		threadPool.shutdown();
		interruptTimer.cancel();
	}

	/**
	 * Adds a turn to the visualizer log
	 * 
	 * @param turn
	 *            The turn to add
	 */
	public static void addTurnToVisualizerLog(JSONObject turn) {
		visualizerLog.addTurn(turn);
	}
}
