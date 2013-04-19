package mm19.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//
// makes a new logger to write to a file if the url is not valid then
// it will become a dummy logger
//
public class GameLogger {
	FileWriter logFile;
	BufferedWriter bw;

	public GameLogger(String LogUrl) {
		try {
			this.logFile = new FileWriter(LogUrl);
			bw = new BufferedWriter(logFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("invalid log url given no informaitn will be saved");
			logFile = null;
		}

	}

	// print string to file
	public void log(String outPut) {
		if (logFile == null) {
			return;
		}
		try {
			bw.write(outPut);
			bw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"WTF the file is open but it failed to write");
		}

	}
	public void close()
	{
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
