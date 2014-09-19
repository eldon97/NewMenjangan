package travel.kiri.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;


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
			final Worker worker = new Worker();
			final AdminListener admin = new AdminListener();
			final ServiceListener service = new ServiceListener(worker);
			admin.setWorker(worker);
			Server server = new Server(portNumber);
			server.setHandler(new AbstractHandler() {

				@Override
				public void handle(String target, Request baseRequest,
						HttpServletRequest request, HttpServletResponse response)
						throws IOException, ServletException {
					if (target.equals("/")) {
						service.handle(target, baseRequest, request, response);
					} else if (target.equals("/admin")) {
						admin.handle(target, baseRequest, request, response);						
					}
				}
			});
			server.start();
		} catch (Exception e) {
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