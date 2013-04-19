package mm19.tests;

import org.json.JSONException;
import org.json.JSONObject;

import mm19.server.GameLogger;

public class loggerTest {
 public static void main(String[] arrgs){
	 GameLogger logger = new GameLogger("fakeLogFile");
	 JSONObject json = new JSONObject();
	 try {
		json.put("succsess", true);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	 logger.log(json.toString());
	 logger.log(json.toString());
	 logger.close();
 }
}
