package travel.kiri.backend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import travel.kiri.backend.algorithm.LatLon;

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
public class ServiceListener extends AbstractHandler {

	public static final String PARAMETER_START = "start";
	public static final String PARAMETER_FINISH = "finish";
	public static final String PARAMETER_MAXIMUM_WALKING = "mw";
	public static final String PARAMETER_WALKING_MULTIPLIER = "wm";
	public static final String PARAMETER_PENALTY_TRANSFER = "pt";
	
	Worker worker;

	public ServiceListener(Worker w) {
		this.worker = w;
		//this.worker.global_verbose=true;
	}
	
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String query = request.getQueryString();
		Map<String, String> params = new HashMap<String, String>();
		params = parseQuery(query);

		String responseText = "Internal error: not updated";
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR_500;

		try {		
			LatLon start = null;
			try
			{
				start = new LatLon(params.get(PARAMETER_START));
			}
			catch (NullPointerException e)
			{
				
			}
			
			LatLon finish = null;
			try
			{
				finish = new LatLon(params.get(PARAMETER_FINISH));
			}
			catch (NullPointerException e)
			{
				
			}
			
			String maximumWalking = params.get(PARAMETER_MAXIMUM_WALKING);
			String walkingMultiplier = params.get(PARAMETER_WALKING_MULTIPLIER);
			String penaltyTransfer = params.get(PARAMETER_PENALTY_TRANSFER);
			if(start!=null && finish!=null)
			{
				//findroute
				responseText = worker.findRoute(start, finish,
						maximumWalking == null ? null : new Double(maximumWalking),
						walkingMultiplier == null ? null : new Double(walkingMultiplier),
						penaltyTransfer == null ? null : new Double(penaltyTransfer));
				responseCode = HttpStatus.OK_200;
			}
			else if(start!=null && finish==null)
			{
				//findnearby
				responseText = worker.findNearbyTransports(start, 
						maximumWalking == null ? null : new Double(maximumWalking));	
				responseCode = HttpStatus.OK_200;			
			}
			else
			{
				responseText = "Please provide start and finish location";
				responseCode = HttpStatus.BAD_REQUEST_400;
			}
			
		} catch (Exception e) {
			responseText = e.toString();
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR_500;
		}
		
		response.setStatus(responseCode);
		baseRequest.setHandled(true);
		response.getWriter().println(responseText);
	}

	private Map<String, String> parseQuery(String query)
			throws UnsupportedEncodingException, NullPointerException {
		Map<String, String> params = new HashMap<String, String>();		
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
		} else {
			throw new NullPointerException("query is null");
		}
		return params;
	}

}
