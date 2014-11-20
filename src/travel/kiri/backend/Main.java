package travel.kiri.backend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Main {

	static NewMenjanganServer server;
	
	public static void main(String[] args) throws Exception {
		
		
		int portNumber = NewMenjanganServer.DEFAULT_PORT_NUMBER;
		for (String arg: args) {
			try {
				portNumber = Integer.decode(arg);
			} catch (Exception ex) {
				// Could be another option
				if (arg.equals("-c")) {
					checkStatus(portNumber);
				} else if (arg.equals("-s")) {
					shutdown(portNumber);
				}
			}
		}
		String homeDirectory = System.getenv("NEWMJNSERVE_HOME");
		if (homeDirectory == null) {
			System.err.println("You need to set NEWMJNSERVE_HOME first!");
			System.exit(1);
		}
		server = new NewMenjanganServer(portNumber, homeDirectory);
		
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
		
		server.start(portNumber, homeDirectory);
	}

	public static void checkStatus(int portNumber) {
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
	
	public static void shutdown(int portNumber) {
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
}