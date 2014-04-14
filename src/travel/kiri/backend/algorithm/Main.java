package travel.kiri.backend.algorithm;

import java.awt.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import travel.kiri.backend.Listener;
import travel.kiri.backend.Worker;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Worker w = new Worker();
		long starttime = System.currentTimeMillis();
		w.init(0.75, 0.1, 10, 0.15);
		long endtime = System.currentTimeMillis();
		
		System.out.println("Init Time: "+(endtime-starttime));
		
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(8000),0);
			server.createContext("/test", new Listener(w));
	        server.setExecutor(null);
	        server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		//-6.9037544,107.6090005
		//-6.9034171,107.6108697
		
		LatLon start = new LatLon(-6.895034,107.603979);
		LatLon finish = new LatLon(-6.926434,107.635790);
		System.out.println(w.startComputing(start, finish, null, null, null));
		
		
		//System.out.println(w);
		
		//TESTING TRACK
		/*
		Scanner scan;
		
		String t = "angkot.kalapasukajadi	0.05	10	1 1 0 0 2 2 3 3 4 4 5 5 6 6 7 7 8 8 9 9 10 -10";
		
		scan = new Scanner(t);
		
		String fullyQualifiedTrackId = scan.next();
		double penalty = Double.parseDouble(scan.next());
		
		Track track = new Track(fullyQualifiedTrackId);
		track.penalty=penalty;
		
		int numberOfNode = scan.nextInt();
		
		for(int i=0;i<numberOfNode;i++)
		{
			double lat = Double.parseDouble(scan.next());
			double lon = Double.parseDouble(scan.next());
			
			LatLon ll = new LatLon(lat,lon);
			GraphNode node = new GraphNode(ll, track);
			track.addNode(node);
		}

		System.out.println("Track Type: "+track.trackTypeId);
		System.out.println("Track ID: "+track.trackId);
		
		for(GraphNode g : track.trackPath)
		{
			System.out.println(g);
		}
		*/
		//TESTING DIJKSTRA
		/*
		ArrayList<GraphNode> gl=new ArrayList<GraphNode>();
		
		GraphNode g;
		int startNode = 0;
		int finishNode = 6;
		
		//start point
		//0(0,0)
		g = new GraphNode(new LatLon(0,0), null);
		g.push_back(1, 6, (char)0);
		g.push_back(2, 4, (char)0);
		g.push_back(3, 5, (char)0);
		
		gl.add(g);
		
		//1(6,0)
		g = new GraphNode(new LatLon(6,0), null);
		g.push_back(4, 4, (char)0);
		
		gl.add(g);
		
		//2(0,4)
		g = new GraphNode(new LatLon(0,4), null);
		g.push_back(3, 3, (char)0);
		g.push_back(5, 4, (char)0);
		
		gl.add(g);
		
		//3(3,4)
		g = new GraphNode(new LatLon(3,4), null);
		g.push_back(6, 5, (char)0);
		
		gl.add(g);
		
		//4(6,4)
		g = new GraphNode(new LatLon(6,4), null);
		g.push_back(6, 4, (char)0);
		
		gl.add(g);
		
		//5(0,8)
		g = new GraphNode(new LatLon(0,8), null);
		g.push_back(6, 6, (char)0);
		
		gl.add(g);
		
		//6(0,8)
		g = new GraphNode(new LatLon(6,8), null);
		g.push_back(4, 8, (char)0);
		
		gl.add(g);
		
		
		
		Dijkstra dj = new Dijkstra(gl, startNode, finishNode, false);
		
		long start = System.currentTimeMillis();
		double result = dj.runAlgorithm();
		long end = System.currentTimeMillis();
		System.out.println("Result: "+result+ " - "+(end-start));
		
		int currentNode = finishNode, lastNode;
		
		for(int i=0;i<7;i++)
		{
			System.out.println(dj.getString(i));
		}
		
		System.out.println("\n==========================");
		
		while(dj.getParent(currentNode)!=dj.DJIKSTRA_NULLNODE)
		{
			lastNode = currentNode;
			currentNode = dj.getParent(currentNode);
			
			System.out.println(dj.getString(lastNode));
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		*/
		
	}
	
	
	
	
	
	
	
	
	/*
	
	static int nodesMinHeap[];
	static int heapsize;
	

	static int heapDeleteMin()
	{
		if(heapsize==0)
		{
			return -1;
		}
		
		int ret = nodesMinHeap[0];
		
		nodesMinHeap[0] = nodesMinHeap[heapsize - 1];
		heapsize--;
		heapPercolateDown(0);
		
		return ret;
	}
	
	
	static void heapPercolateDown(int index)
	{
		int minIndex = index;
		
		//kalau ada anak kiri DAN kalau distance anak kiri lebih kecil
		if((index*2+1<heapsize) && nodesMinHeap[index*2+1] < nodesMinHeap[minIndex])
		{
			minIndex = index*2+1;
		}

		//kalau ada anak kanan DAN kalau distance anak kanan lebih kecil
		if((index*2+2<heapsize) && nodesMinHeap[index*2+2] < nodesMinHeap[minIndex])
		{
			minIndex = index*2+2;
		}
		
		//kalau memang bukan yang paling kecil maka swap
		if(minIndex != index)
		{
			//swap!
			int temp = nodesMinHeap[index];
			nodesMinHeap[index] = nodesMinHeap[minIndex];
			nodesMinHeap[minIndex] = temp;
			
			//continue the percolation
			heapPercolateDown(minIndex);
		}
		
	}
	
	static void heapPercolateUp(int index)
	{
		//selama belum yang paling atas DAN yang di atasnya lebih besar
		while((index-1)/2 >=0 && nodesMinHeap[(index-1)/2] > nodesMinHeap[index])
		{
			//swap!
			int temp = nodesMinHeap[index];
			nodesMinHeap[index] = nodesMinHeap[(index-1)/2];
			nodesMinHeap[(index-1)/2] = temp;
			
			index = (index-1)/2;
		}
	}*/

}
