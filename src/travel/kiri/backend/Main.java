package travel.kiri.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpServer;

public class Main {

	public static final int DEFAULT_PORT_NUMBER = 8000;
	
	public static String homeDirectory = null;

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		int portNumber = DEFAULT_PORT_NUMBER;
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
		homeDirectory = System.getenv("NEWMJNSERVE_HOME");
		if (homeDirectory == null) {
			System.err.println("You need to set NEWMJNSERVE_HOME first!");
			System.exit(1);
		}
		try {
			long startTime = System.currentTimeMillis();
			HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);
			AdminListener admin = new AdminListener();
			server.createContext("/admin", admin);
			Worker worker = new Worker();
			server.createContext("/", new ServiceListener(worker));
			admin.setWorker(worker);
			server.setExecutor(null);
			server.start();
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println("Server loaded in " + elapsedTime + " ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
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