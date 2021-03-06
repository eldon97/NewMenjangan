package travel.kiri.backend;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class NewMenjanganServer {

	public static final int DEFAULT_PORT_NUMBER = 8000;
	private final Worker worker;
	private final AdminListener admin;
	private final ServiceListener service;
	private final Server httpServer;
	
	private final int portNumber;
	private final String homeDirectory;
	
	public NewMenjanganServer(int portNumber, String homeDirectory) throws FileNotFoundException, IOException {
		this.portNumber = portNumber;
		this.homeDirectory = homeDirectory;
		worker = new Worker(homeDirectory);
		admin = new AdminListener();
		service = new ServiceListener(worker);
		admin.setWorker(worker);
		httpServer = new Server(portNumber);
		httpServer.setHandler(new AbstractHandler() {

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
	}
	
	public NewMenjanganServer clone() {
		try {
			return new NewMenjanganServer(this.portNumber, this.homeDirectory);
		} catch (Exception e) {
			Main.globalLogger.severe(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public void start() throws Exception {
		httpServer.start();		
	}
	public void stop() throws Exception {
		httpServer.stop();
	}
}
