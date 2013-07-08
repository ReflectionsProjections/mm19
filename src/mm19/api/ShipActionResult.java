package mm19.api;

import org.json.JSONException;
import org.json.JSONObject;

public class ShipActionResult {

	public enum ActionResult{SUCCESS, INSUFFICIENT_RESOURCES, INCORRECT_PARAMETERS};
	public int shipID;
	public ActionResult result;
	
	public ShipActionResult(int id, ActionResult r){
		shipID=id;
		result=r;
	}
	
	public JSONObject toJSON() throws JSONException {
		String actionResult;
		switch(result) {
		case SUCCESS:
			actionResult = "S";
			break;
		case INSUFFICIENT_RESOURCES:
			actionResult = "R";
			break;
		case INCORRECT_PARAMETERS:
			actionResult = "I";
			break;
		default:
			actionResult = "S";
		}
		
		JSONObject json = new JSONObject();
		json.put("ID", shipID);
		json.put("result", actionResult);
		return json;
	}

}
