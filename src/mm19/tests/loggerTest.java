package mm19.tests;

import org.json.JSONException;
import org.json.JSONObject;

import mm19.logging.VisualizerLogger;

public class loggerTest {
	
	// I'm not sure what this is testing. 
	public static void main(String[] args){
		VisualizerLogger logger = new VisualizerLogger("fakeLogFile");
		JSONObject json = new JSONObject();
		try {
			json.put("succsess", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.addTurn(json);
		logger.addTurn(json);
		logger.close();
	}
}
