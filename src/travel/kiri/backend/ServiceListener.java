package travel.kiri.backend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import travel.kiri.backend.algorithm.LatLon;

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
public class ServiceListener implements HttpHandler {

	Worker worker;

	public ServiceListener(Worker w) {
		this.worker = w;
	}

	/**
	 * Menghandle request HTTP yang data. Pada servicelistener.cc, ini sama
	 * dengan fungsi answer_to_connection. Nantinya, akan digunakan juga untuk
	 * menghandle request dari admin
	 */
	@Override
	public void handle(HttpExchange he) throws IOException {
		URI reqUri = he.getRequestURI();
		String query = reqUri.getRawQuery();
		Map<String, String> params = new HashMap<String, String>();

		parseQuery(query, params);

		String responseText = "Internal error: not updated";
		int responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;

		try {

			LatLon start = new LatLon(params.get("start"));
			LatLon finish = new LatLon(params.get("finish"));

			responseText = worker.startComputing(start, finish, null, null,
					null);
		} catch (Exception ex) {
			responseText = ex.toString();
			responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
		}

		he.sendResponseHeaders(responseCode, responseText.length());
		byte[] res = responseText.getBytes();
		he.getResponseBody().write(res);
		he.close();

	}

	private void parseQuery(String query, Map<String, String> params)
			throws UnsupportedEncodingException {
		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");

				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0],
							System.getProperty("file.encoding"));
				}
				if (param.length > 1) {
					value = URLDecoder.decode(param[1],
							System.getProperty("file.encoding"));
				}

				params.put(key, value);
			}
		}
	}

}
