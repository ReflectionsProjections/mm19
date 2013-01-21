package mm19.server;

/**
 * Standalone MechMania XIX server.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
	// Static constant logger declaration
	public static final Logger serverLog =
		Logger.getLogger(Server.class.getName());

	// Server constants for now, configurability later
	public static final int PORT = 6969;
	public static final int MAX_THREADS = 8;
	public static final String LOG_PATH = "server_log.txt";
	public static final Level LOG_LEVEL = Level.INFO;

	// Server static variables
	private static ServerSocket socket = null;
	private static ExecutorService threadPool = null;
	private static boolean running = false;

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
		serverLog.log(Level.INFO, "Starting server run loop");
		running = true;

		while (running) {
			Socket clientSocket = null;
			try {
				// Listen for a new connection
				clientSocket = socket.accept();
				serverLog.log(Level.INFO, "Connection received");
			} catch (IOException e) {
				serverLog.log(Level.SEVERE, "Unexpected error accepting " +
						"client connection.", e);
			}

			// Create a new task for the incoming connection and put it in the
			// thread pool
			RequestRunnable task = new RequestRunnable(clientSocket);
			threadPool.execute(task);
		}

	}
}
