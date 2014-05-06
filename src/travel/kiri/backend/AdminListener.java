package travel.kiri.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
	double maximum_walking_distance;
	double maximum_transfer_distance;
	double multiplier_walking;
	double penalty_transfer;
	Map<String, String> map;
	Worker w = null;

	public AdminListener(HttpServer server) {
		this.server = server;
	}

	@Override
	public void handle(HttpExchange he) throws IOException {
		URI reqUri = he.getRequestURI();
		String query = reqUri.getRawQuery();
		Map<String, String> params = new HashMap<String, String>();

		parseQuery(query, params);

		StringBuilder response = new StringBuilder();
		String mode = params.get("mode");

		if (mode != null) {
			if (mode.equals("start")) {
				if (w == null) {
					response.append("Please wait :)");
				} else {
					response.append("Worker already initialized");
				}
			} else if (mode.equals("stop")) {
				response.append("Stopped :(");
			}
		} else {
			response.append("Hello :)\n");
			response.append("use parameter mode=start to initialize\n");
			response.append("use parameter mode=stop to quit\n");
		}

		he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
		byte[] res = response.toString().getBytes();

		he.getResponseBody().write(res);
		he.close();

		if (mode != null) {
			if (mode.equals("start")) {
				System.out.println("START");
				map = new HashMap<String, String>();
				readConf("etc/mjnserve.conf");

				maximum_walking_distance = (map.get("maximum_walking_distance") != null ? Double
						.parseDouble(map.get("maximum_walking_distance"))
						: 0.75);
				maximum_transfer_distance = (map
						.get("maximum_transfer_distance") != null ? Double
						.parseDouble(map.get("maximum_transfer_distance"))
						: 0.1);
				multiplier_walking = (map.get("multiplier_walking") != null ? Double
						.parseDouble(map.get("multiplier_walking")) : 1);
				penalty_transfer = (map.get("penalty_transfer") != null ? Double
						.parseDouble(map.get("penalty_transfer")) : 0.15);

				long starttime = System.currentTimeMillis();
				w = new Worker();
				w.init(maximum_walking_distance, maximum_transfer_distance,
						multiplier_walking, penalty_transfer);
				long endtime = System.currentTimeMillis();

				server.createContext("/", new ServiceListener(w));
			} else if (mode.equals("stop")) {
				System.exit(0);
			}
		}

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

	public boolean readConf(String filename) {
		try {
			Scanner linescan = new Scanner(new File(filename));
			String line, word[];

			while (linescan.hasNextLine()) {
				line = linescan.nextLine();
				if (line.length() > 0 && line.charAt(0) != '#') {
					word = line.split(" = ");

					if (word.length > 1) {
						map.put(word[0], word[1]);
					}
				}
			}
			linescan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
