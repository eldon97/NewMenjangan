package travel.kiri.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import travel.kiri.backend.puller.DataPuller;


public class Main {

	public static String TRACKS_CONF = "etc/tracks.conf";
	public static String MYSQL_PROPERTIES = "etc/mysql.properties";
	public static String MJNSERVE_PROPERTIES = "etc/mjnserve.properties";
	public static String NEWMJNSERVE_LOG = "log/newmjnserve.log";
	
	static NewMenjanganServer server;
	static DataPuller puller;
	static Timer timer;
	static int portNumber;
	static String homeDirectory;

	public static final Logger globalLogger = Logger.getGlobal();
	
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
		FileHandler logFileHandler = new FileHandler(homeDirectory + "/" + NEWMJNSERVE_LOG);
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
	
	/**
	 * Pulls data from SQL and external sources
	 * @return true if success and data has changed, false otherwise
	 */
	private static boolean pullData() {
		if (puller == null) {
			puller = new DataPuller();
		}
		try {
			final String tracksConf = homeDirectory + "/" + TRACKS_CONF;
			final String tracksConfTemp = homeDirectory + "/" + TRACKS_CONF + ".tmp";

			PrintStream outStream = new PrintStream(tracksConfTemp);
			puller.pull(new File(homeDirectory + "/" + MYSQL_PROPERTIES), outStream);
			outStream.close();

			
			if (!fileEquals(new File(tracksConf), new File(tracksConfTemp))) {
				// Use nio library for consistent behavior across OS.
				Path source = FileSystems.getDefault().getPath(tracksConfTemp);
				Path dest = FileSystems.getDefault().getPath(tracksConf);
				Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
			} else {
				globalLogger.info("Data pulled successfuly, but no changes.");
				return false;
			}
			return true;
		} catch (Exception e) {
			globalLogger.severe("Failed to refresh data: " + e.toString());
			return false;
		}
	}
	
	/**
	 * Checks if two files are identical, binary-wise
	 * @param file1 the first file to check
	 * @param file2 the second file to check
	 * @return true if identical, false otherwise
	 */
	private static boolean fileEquals(File file1, File file2) {
		if (file1.length() != file2.length()) {
			return false;
		}
		BufferedReader reader1 = null;
		BufferedReader reader2 = null;
		try {
			reader1 = new BufferedReader(new FileReader(file1));
			reader2 = new BufferedReader(new FileReader(file2));
			int c1, c2, position = 0;
			while ((c1 = reader1.read()) != -1) {
				c2 = reader2.read();
				if (c1 != c2) {
					globalLogger.info("Tracks configuration differs in offset " + position);
					return false;
				}
				position++;
			}
			reader1.close();
			reader2.close();
		} catch (Exception e) {
			globalLogger.severe(e.getMessage());
			return false;
		} finally {
			try {
				reader1.close();
			} catch (Exception e1) {
				// void
			}
			try {
				reader2.close();
			} catch (Exception e1) {
				// void
			}			
		}
		return true;
	}
	
	static class DataRefresher extends TimerTask {

		@Override
		public void run() {
			globalLogger.info("Data refresh triggered, server reload executed!");
			if (server != null) {
				try {
					if (pullData()) {
						server.stop();
						server = server.clone();
						server.start();
					} else {
						globalLogger.info("No change in data, server not restarted.");
					}
				} catch (Exception e) {
					globalLogger.severe(e.getMessage());
					e.printStackTrace();
				}
			} else {
				globalLogger.severe("Can't restart server, as server is detected inactive!");
			}
		}
		
	}
}