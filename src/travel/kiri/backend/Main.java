package travel.kiri.backend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Main {

	static NewMenjanganServer server;
	static Timer timer;
	static int portNumber;
	static String homeDirectory;

	static final Logger globalLogger = Logger.getGlobal();
	
	public static void main(String[] args) throws Exception {
		portNumber = NewMenjanganServer.DEFAULT_PORT_NUMBER;
		for (String arg: args) {
			try {
				portNumber = Integer.decode(arg);
			} catch (Exception ex) {
				// Could be another option
				if (arg.equals("-c")) {
					sendCheckStatus(portNumber);
				} else if (arg.equals("-s")) {
					sendShutdown(portNumber);
				}
			}
		}
		homeDirectory = System.getenv("NEWMJNSERVE_HOME");
		if (homeDirectory == null) {
			System.err.println("You need to set NEWMJNSERVE_HOME first!");
			System.exit(1);
		}
		FileHandler logFileHandler = new FileHandler(homeDirectory + "/log/newmjnserve.log");
		logFileHandler.setFormatter(new SimpleFormatter());
		globalLogger.addHandler(logFileHandler);
		server = new NewMenjanganServer(portNumber, homeDirectory);

		// Setup timer
		Calendar nextMidnight = Calendar.getInstance();
		nextMidnight.setTimeInMillis(nextMidnight.getTimeInMillis() + 24 * 60 * 60 * 1000);
		nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
		nextMidnight.set(Calendar.MINUTE, 15);
		Timer timer = new Timer(false);
		timer.schedule(new DataRefresher(), new Date(nextMidnight.getTimeInMillis()), 24 * 60 * 60 * 1000);
		DateFormat dateFormat = DateFormat.getInstance();
		globalLogger.info("Data refresh timer scheduled, first time at " + dateFormat.format(new Date(nextMidnight.getTimeInMillis())));
		
		// Test catching TERM signal TODO remove after confirmed
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Halted by signal!");
				try {
					server.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		server.start();
	}

	public static void sendCheckStatus(int portNumber) {
		try {
			HttpURLConnection connection = (HttpURLConnection)(new URL("http://localhost:" + portNumber + "/admin?ping").openConnection());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("NewMenjangan server is active");
				System.exit(0);
			} else {
				System.out.println("NewMenjangan server is inactive");
				System.exit(1);
			}
		} catch (IOException e) {
			System.out.println("NewMenjangan server is inactive");
			System.exit(1);
		}
	}
	
	public static void sendShutdown(int portNumber) {
		try {
			HttpURLConnection connection = (HttpURLConnection)(new URL("http://localhost:" + portNumber + "/admin?forceshutdown").openConnection());
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("NewMenjangan shutdown success");
				System.exit(0);
			} else {
				System.out.println("NewMenjangan shutdown fail");
				System.exit(1);
			}
		} catch (IOException e) {
			System.out.println("NewMenjangan shutdown fail");
			System.exit(1);
		}
	}	
	
	static class DataRefresher extends TimerTask {

		@Override
		public void run() {
			globalLogger.info("Data refresh triggered, server reload executed!");
			if (server != null) {
				try {
					server.stop();
					server = server.clone();
					server.start();
				} catch (Exception e) {
					globalLogger.severe(e.getMessage());
					e.printStackTrace();
				}
			} else {
				globalLogger.severe("Can't restart server, is server is detected inactive!");
			}
		}
		
	}
}