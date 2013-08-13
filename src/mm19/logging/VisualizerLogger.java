package mm19.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import mm19.game.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Makes a new logger to write to a file
//  If the URL is invalid, it will become a dummy logger
//
public class VisualizerLogger {
	FileWriter logFile;
	JSONArray turns;
	JSONObject boardConfiguration;
	BufferedWriter bw;
	public VisualizerLogger(String LogUrl) {
		try {
			this.logFile = new FileWriter(LogUrl);
			bw = new BufferedWriter(logFile);
			turns = new JSONArray();
			
			// Logging the initial board configurations for the visualizer
			boardConfiguration = new JSONObject();
			boardConfiguration.put("width", Constants.BOARD_SIZE);
			boardConfiguration.put("height", Constants.BOARD_SIZE);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Invalid log URL given, or bad JSON, no information will be saved!");
			logFile = null;
		}
		catch (JSONException e) {
			e.printStackTrace();
			System.out
					.println("Invalid log URL given, or bad JSON, no information will be saved!");
			logFile = null;
		}

	}
	
	public void addTurn(JSONObject turn) {
		turns.put(turn);
	}
	
	// print string to file
	public void writeToFile() {
		if (logFile == null) {
			return;
		}
		try {
			JSONObject json = new JSONObject();
			json.put("boardConfiguration", boardConfiguration);
			json.put("turns", turns);
			bw.write(json.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 protected void finalize() {
	        close();
	        // TODO What?
	        System.out.println("error you forgot to close the logfile handler");
	 }
}
