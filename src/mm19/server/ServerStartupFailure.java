package mm19.server;

import java.util.TimerTask;

public class ServerStartupFailure extends TimerTask{
	public static int PLAYER_TO_INTERRUPT = 0;
	@Override
	public void run() {
		Server.startupFailed();
	}

}
