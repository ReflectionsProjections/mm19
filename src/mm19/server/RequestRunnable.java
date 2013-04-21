package mm19.server;

/**
 * Thread runner for individual client requests.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestRunnable implements Runnable {

	protected Socket clientSocket = null;
	protected String playerToken;
	protected int playerID;
	
	protected BufferedReader in = null;
	
	
	public RequestRunnable(Socket clientSocket, String token, int pID) {
		this.clientSocket = clientSocket;
		playerToken = token;
		playerID = pID;
	}
	
	@Override
	public void run() {
		
		while(true) {
			
			try {
				in = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
				
				String msg = in.readLine();
				System.out.println("Recieved turn");
				JSONObject obj = new JSONObject(msg);
				Server.sendAPI(obj);
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
			
			
		// Close up the client socket
			
		Server.serverLog.log(Level.INFO, "Dropping Player");
		
		if(in != null) {
			try {
				in.close();
				Server.disconnectPlayer(playerToken);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}
/*	
	private boolean send(String data) {
		boolean successful = true;
		try {
			out.write(data.getBytes());
			out.flush();
		} catch (IOException e) {
			Server.serverLog.log(Level.WARNING, "Error sending message:\n" + data);
			successful = false;
			e.printStackTrace();
		}
		return successful;
		
	}
*/
	private JSONObject receive() {
		return null;
	}
	
}

