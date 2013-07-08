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
				Server.submitTurn(obj, playerToken);
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
			
		Server.serverLog.log(Level.INFO, "Player dropped, restart server.");
		
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}
	
}

