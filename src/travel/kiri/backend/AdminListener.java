package travel.kiri.backend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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

	/**
	 * Menghandle request HTTP yang data. Pada servicelistener.cc, ini sama
	 * dengan fungsi answer_to_connection. Nantinya, akan digunakan juga untuk
	 * menghandle request dari admin
	 */
	HttpServer server;

	public AdminListener(HttpServer server) {
		this.server = server;
	}

	@Override
	public void handle(HttpExchange he) throws IOException {
		String query = he.getRequestURI().getRawQuery();
		int responseStatus = HttpURLConnection.HTTP_FORBIDDEN;
		String responseText = "";
		
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
			} else if (query.equals("ping")) {
				responseStatus = HttpURLConnection.HTTP_OK;
				responseText = "pong";
			} else {
				responseText = "Invalid command: " + query;
			}
		} else {
			responseText = "Command must be provided";
		}

		he.sendResponseHeaders(responseStatus, responseText.length());
		byte[] res = responseText.toString().getBytes();

		he.getResponseBody().write(res);
		he.close();
	}
}
