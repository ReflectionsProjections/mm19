package mm19.game;

import java.util.Timer;
import java.util.TimerTask;

public class Timeout extends TimerTask{
	private Engine e;
	public Timeout(Engine engine){
		e=engine;
	}
	public void run(){
		e.timeout();
		Timer t = new Timer();
		t.schedule(new Timeout(e), Engine.TIMELIMIT*1000);
	}
}
