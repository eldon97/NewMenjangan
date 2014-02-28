package travel.kiri.backend;

import java.io.IOException;

import com.sun.net.httpserver.*;

/**
 * <p>Kelas listener mendengarkan perintah-perintah dari luar saat 
 * service ini menjadi daemon. Referensi dari servicelistener.cc
 * dan adminlistener.cc. Sebelumnya adminlistener menggunakan pipe,
 * tetapi sekarang harus dari http, hanya saja dicek apakah
 * host nya dari localhost: hanya dari localhost yang diterima.</p>
 * 
 * <p>Note: kita akan menggunakan library dari Sun untuk mengurus HTTP
 * request nya ({@linkplain http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api}.
 * Jika tidak berhasil, baru coba gunakan Jetty ({@linkplain http://stackoverflow.com/questions/2717294/create-a-simple-http-server-with-java}).
 * </p>
 * 
 * @author PascalAlfadian
 *
 */
public class Listener implements HttpHandler {

	/**
	 * Menghandle request HTTP yang data. Pada servicelistener.cc,
	 * ini sama dengan fungsi answer_to_connection. Nantinya, akan
	 * digunakan juga untuk menghandle request dari admin
	 */
	@Override
	public void handle(HttpExchange t) throws IOException {
		// TODO Auto-generated method stub
	}

}
