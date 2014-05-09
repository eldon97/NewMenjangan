package travel.kiri.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class Main {

	public static final int DEFAULT_PORT_NUMBER = 8080;

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		int portNumber = DEFAULT_PORT_NUMBER;
		if (args.length > 0) {
			try {
				portNumber = Integer.decode(args[0]);
			} catch (Exception ex) {
				// void: revert to default.
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

}
