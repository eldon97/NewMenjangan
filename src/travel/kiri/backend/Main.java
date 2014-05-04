package travel.kiri.backend;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		
		//System.out.println("Init Time: "+(endtime-starttime));
		
		int portNumber = 8080;
		if(args.length>0)
		{
			try
			{
				portNumber = Integer.decode(args[0]);
			}
			catch(Exception ex)
			{
				
			}
		}
		
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(portNumber),0);
			server.createContext("/admin", new AdminListener(server));
	        server.setExecutor(null);
	        server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		//-6.9037544,107.6090005
		//-6.9034171,107.6108697
		System.out.println("OK");
		//LatLon start = new LatLon(-6.895034,107.603979);
		//LatLon finish = new LatLon(-6.926434,107.635790);
		//System.out.println(w.startComputing(start, finish, null, null, null));

	}
	
}
	
	
	
	
	
	