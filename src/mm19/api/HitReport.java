package mm19.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mm19
 *
 * Immutable object for reporting attacks.
 */
public class HitReport {
	final public int x;
    final public int y;
    final public boolean shotSuccessful;

    public HitReport(int x, int y, boolean hitSuccessful){
        this.x = x;
        this.y = y;
        this.shotSuccessful = hitSuccessful;
    }
    
    public JSONObject toJSON() throws JSONException{
    	JSONObject json = new JSONObject();
    	json.put("xCoord", x);
    	json.put("yCoord", y);
    	json.put("hit", shotSuccessful);
    	return json;
    }
}
