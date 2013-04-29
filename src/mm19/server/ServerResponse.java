package mm19.server;

import java.util.List;

import mm19.game.HitReport;
/*
 * This ServerResponse class is a java representation  of the JSON the server will 
 * send Upon a successful, or partially successful submission.
 */
public class ServerResponse {
	
	public List<ShipActionResult> shipActionResults;
	public int responseCode;
	public String playerToken;
	public List<HitReport> hitReport;
	public List<String> error;
	public String playerName;
	public int playerID;
	public int resources;
	public List<ShipData> ships;


	public ServerResponse(List error,String PlayerName,int PlayerID,int resources,List<ShipData> ships)
	{
		this.error=error;
		this.playerID=playerID;
		this.playerName=playerName;
		this.resources=resources;
		this.ships=ships;
	}
	
	
	
}