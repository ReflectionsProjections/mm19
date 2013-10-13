package mm19.server;

/**
 * @author mm19
 * 
 * Thread runner for individual client requests.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestRunnable implements Runnable {

	protected Socket clientSocket = null;
	protected String playerToken;
	protected int playerID;

	protected BufferedReader in = null;
	protected static Lock mutex = null;

	public RequestRunnable(Socket clientSocket, String token, int pID) {
		this.clientSocket = clientSocket;
		playerToken = token;
		playerID = pID;
	}

	@Override
	public void run() {
		if(mutex == null) {
			mutex = new ReentrantLock();
		}
		
		Boolean running = true;
		try {
			while (running) {
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));

				String msg = in.readLine();
				if (msg == null) {
					running = false;
					in.close();
					break;
				}
				
				try {
					mutex.lock();
					JSONObject obj = new JSONObject(msg);
					Server.submitTurn(obj, playerToken, false);
				} catch (JSONException e) {
					Server.serverLog.log(Level.WARNING, "Player " + playerID + " has JSON error.");
				} finally {
					mutex.unlock();
				}

			}
			Server.submitTurn(null, playerToken, true);
			Server.serverLog.log(Level.INFO, "Player " + playerID + " dropped.");
		} catch (IOException e) {
			Server.serverLog.log(Level.INFO, "Player " + playerID + " dropped. (SocketException)");
			Server.submitTurn(null, playerToken, true);
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
