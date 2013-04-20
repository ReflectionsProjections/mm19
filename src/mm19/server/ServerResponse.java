package mm19.server;

import java.util.List;
/*
 * This ServerResponse class is a java representation  of the JSON the server will 
 * send Upon a successful, or partially successful submission.
 */
public class ServerResponse {
	
	public List error;
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
