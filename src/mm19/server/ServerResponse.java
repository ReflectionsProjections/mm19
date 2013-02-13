package mm19.server;

import java.util.List;

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
