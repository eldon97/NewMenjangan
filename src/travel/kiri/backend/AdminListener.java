package travel.kiri.backend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
public class AdminListener implements HttpHandler {	
	Worker worker = null;
	
	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	@Override
	public void handle(HttpExchange he) throws IOException {
		String query = he.getRequestURI().getRawQuery();
		int responseStatus = HttpURLConnection.HTTP_FORBIDDEN;
		String responseText = "";
		
		try {
			// TODO test this!
			if (!he.getRemoteAddress().getAddress().equals(InetAddress.getLocalHost())) {
				responseText = "Forbidden from your PC";
			}
			
			if (query != null) {
				if (query.equals("forceshutdown")) {
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// nevermind, we'll exit immediately!
							}
							System.exit(0);						
						}
					}.start();
					responseStatus = HttpURLConnection.HTTP_OK;
					responseText = "Server will shutdown in 3 seconds";
				} else if (query.equals("tracksinfo")) {
					if (worker != null) {
						responseStatus = HttpURLConnection.HTTP_OK;
						responseText = worker.printTracksInfo();
					} else {
						responseStatus = HttpURLConnection.HTTP_UNAVAILABLE;
						responseText = "Worker is not ready.";
					}
				} else if (query.equals("toggleverbose")) {
					if (worker != null) {
						responseStatus = HttpURLConnection.HTTP_OK;
						responseText = worker.toggleVerbose();
					} else {
						responseStatus = HttpURLConnection.HTTP_UNAVAILABLE;
						responseText = "Worker is not ready.";
					}					
				} else {
					responseText = "Invalid command: " + query;
				}
			} else {
				responseText = "Command must be provided";
			}
		} catch (Exception e) {
			responseStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
			responseText = e.toString();
		}
			
		he.sendResponseHeaders(responseStatus, responseText.length());
		byte[] res = responseText.toString().getBytes();

		he.getResponseBody().write(res);
		he.close();
	}
}
