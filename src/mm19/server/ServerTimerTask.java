package mm19.server;

import java.util.TimerTask;

public class ServerTimerTask extends TimerTask{
	public static int PLAYER_TO_NOTIFY = 0;
	@Override
	public void run() {
		API.notifyTurn(PLAYER_TO_NOTIFY);
	}

}
