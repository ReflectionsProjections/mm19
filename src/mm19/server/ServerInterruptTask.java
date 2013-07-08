package mm19.server;

import java.util.TimerTask;

public class ServerInterruptTask extends TimerTask{
	public static int PLAYER_TO_INTERRUPT = 0;
	@Override
	public void run() {
		Server.interruptPlayer(PLAYER_TO_INTERRUPT);
	}

}
