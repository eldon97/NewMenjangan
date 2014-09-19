package travel.kiri.backend;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * <p>
 * Kelas listener mendengarkan perintah-perintah dari luar saat service ini
 * menjadi daemon. Referensi dari servicelistener.cc dan adminlistener.cc.
 * Sebelumnya adminlistener menggunakan pipe, tetapi sekarang harus dari http,
 * hanya saja dicek apakah host nya dari localhost: hanya dari localhost yang
 * diterima.
 * </p>
 * 
 * <p>
 * Note: kita akan menggunakan library dari Sun untuk mengurus HTTP request nya
 * ({@linkplain http
 * ://stackoverflow.com/questions/3732109/simple-http-server-in-
 * java-using-only-java-se-api}. Jika tidak berhasil, baru coba gunakan Jetty (
 * {@linkplain http
 * ://stackoverflow.com/questions/2717294/create-a-simple-http-server-with-java}
 * ).
 * </p>
 * 
 * @author PascalAlfadian
 * 
 */
public class AdminListener extends AbstractHandler {	
	Worker worker = null;
	
	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String query = request.getQueryString();
		int responseStatus = HttpStatus.FORBIDDEN_403;
		String responseText = "";
		
		try {
			if (!InetAddress.getByName(baseRequest.getRemoteAddr()).isLoopbackAddress()) {
				responseText = "Forbidden : " + baseRequest.getRemoteAddr();
			} else if (query != null) {
				if (query.equals("forceshutdown")) {
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// nevermind, we'll exit immediately!
							}
							System.exit(0);						
						}
					}.start();
					responseStatus = HttpStatus.OK_200;
					responseText = "Server will shutdown in 1 second";
				} else if (query.equals("tracksinfo")) {
					if (worker != null) {
						responseStatus = HttpStatus.OK_200;
						responseText = worker.printTracksInfo();
					} else {
						responseStatus = HttpStatus.SERVICE_UNAVAILABLE_503;
						responseText = "Worker is not ready.";
					}
				} else if (query.equals("toggleverbose")) {
					if (worker != null) {
						responseStatus = HttpStatus.OK_200;
						responseText = worker.toggleVerbose();
					} else {
						responseStatus = HttpStatus.SERVICE_UNAVAILABLE_503;
						responseText = "Worker is not ready.";
					}
				} else if (query.equals("ping")) {
					responseStatus = HttpStatus.OK_200;
					responseText = "pong\n";					
				} else {
					responseText = "Invalid command: " + query;
				}
			} else {
				responseStatus = HttpStatus.BAD_REQUEST_400;
				responseText = "Command must be provided";
			}
		} catch (Exception e) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR_500;
			responseText = e.toString();
		}
		response.setStatus(responseStatus);
		baseRequest.setHandled(true);
		response.getWriter().println(responseText);
	}
}
