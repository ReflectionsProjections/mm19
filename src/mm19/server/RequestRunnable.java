package mm19.server;

/**
 * Thread runner for individual client requests.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

public class RequestRunnable implements Runnable {

	protected Socket clientSocket = null;

	public RequestRunnable(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		try {
			InputStream input = clientSocket.getInputStream();
			OutputStream output = clientSocket.getOutputStream();

			// TODO: Do something more useful than this
			Server.serverLog.log(Level.INFO, "Processing client request");
			String outStr =
				"HTTP/1.1 200 OK";
			output.write(outStr.getBytes());

			// Close up the client socket
			output.close();
			input.close();
		} catch (IOException e) {
			Server.serverLog.log(Level.WARNING, "Unknown error processing " +
					"client request", e);
		}
	}

}
