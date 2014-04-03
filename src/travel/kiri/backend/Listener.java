package travel.kiri.backend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import travel.kiri.backend.algorithm.LatLon;

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

	Worker worker;
	
	public Listener(Worker w)
	{
		this.worker=w;
	}
	
	/**
	 * Menghandle request HTTP yang data. Pada servicelistener.cc,
	 * ini sama dengan fungsi answer_to_connection. Nantinya, akan
	 * digunakan juga untuk menghandle request dari admin
	 */
	@Override
	public void handle(HttpExchange he) throws IOException {
        // TODO Auto-generated method stub
		
		long startTime=System.currentTimeMillis(), endTime;
		
        URI reqUri = he.getRequestURI();
        String query = reqUri.getRawQuery();
        System.out.println(query);
        
        Map<String, Object> params = new HashMap();
        
        parseQuery(query, params);
        System.out.println(params.keySet());
        System.out.println(params.values());
        
        String response="";
        
        try
        {
        	Double slat = Double.parseDouble((String) params.get("slat"));
            Double slon = Double.parseDouble((String) params.get("slon"));

            Double flat = Double.parseDouble((String) params.get("flat"));
            Double flon = Double.parseDouble((String) params.get("flon"));

            LatLon start = new LatLon(slat, slon);
            LatLon finish = new LatLon(flat, flon);
            
            response = worker.startComputing(start, finish, -1.0,-1.0,-1.0);
        }
        catch(Exception ex)
        {
        	for(String key: params.keySet())
            {
                response+=key+" : "+params.get(key)+"\n";
            }
        }

        endTime=System.currentTimeMillis();
        response+="\nTime: "+(endTime-startTime);

        
        he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        byte[] res = response.getBytes();

        he.getResponseBody().write(res);
        he.close();
        
        
        
    }

    private void parseQuery(String query, Map<String, Object> params) throws UnsupportedEncodingException
    {
        if(query!=null)
        {
            String pairs[] = query.split("[&]");
            for(String pair:pairs)
            {
                String param[] = pair.split("[=]");
                
                String key = null;
                String value = null;
                if(param.length>0)
                {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }
                if(param.length>1)
                {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }
                
                if(params.containsKey(key))
                {
                    Object obj = params.get(key);
                     if(obj instanceof List<?>) {
                         List<String> values = (List<String>)obj;
                         values.add(value);
                     } else if(obj instanceof String) {
                         List<String> values = new ArrayList<String>();
                         values.add((String)obj);
                         values.add(value);
                         params.put(key, values);
                     }
                }
                else
                {
                    params.put(key, value);
                }
            }
        }
    }

}
