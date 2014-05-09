package travel.kiri.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpServer;

public class Main {

	public static final int DEFAULT_PORT_NUMBER = 8000;

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

		long startTime = System.currentTimeMillis();
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(portNumber), 0);
			AdminListener admin = new AdminListener();
			server.createContext("/admin", admin);
			Worker worker = new Worker();
			server.createContext("/", new ServiceListener(worker));
			admin.setWorker(worker);
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Server loaded in " + elapsedTime + " ms");

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