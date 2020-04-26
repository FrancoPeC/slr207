import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {

    private static void ExecPhase(String mode, String fileName,
				  HashMap<String, Integer> pcMap,
				  ArrayList<Object> threads) throws Exception {
	for (Map.Entry<String, Integer> entry : pcMap.entrySet()) {
	    try {
		ExecSlave es = new ExecSlave(mode, entry.getKey(),
					     fileName +  (entry.getValue()) + ".txt");
		threads.add(es);
		es.start();
	    }catch(Exception e){}
	}
	for(Object obj : threads) {
	    ExecSlave es = (ExecSlave) obj;
	    es.join();
	}
	threads.clear();
    }

    public static void main(String[] args) {
	String thisPc = null;
	try {thisPc = InetAddress.getLocalHost().getHostName();}
	catch(Exception e){}
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	HashMap<String, Integer> pcMap = new HashMap<String, Integer>();
	FileReader fs = null;
	FileWriter fw = null;
	String inputFile = null;

	try{inputFile = args[1];}
	catch(Exception e){}
	
	try{
	    fs = new FileReader("deploySuccess.txt");
	    BufferedReader br = new BufferedReader(fs);

	    String line;
	    while((line = br.readLine()) != null) {
		if(!line.equals(thisPc)) {
		    GetHostName ghn = new GetHostName(line, pcs);
		    threads.add(ghn);
		    ghn.start();
		}
	    }
	    for(Object obj : threads) {
		GetHostName ghn = (GetHostName) obj;
		ghn.join();
	    }
	    System.out.println(pcs.size());
	    threads.clear();
	    
	    if(inputFile != null)
		fs = new FileReader(inputFile);
	    else
		fs = new FileReader("input.txt");
	    br = new BufferedReader(fs);

	    Queue<String> fileLines = new LinkedList<String>();
	    int fileSize = 0;
	    while((line = br.readLine()) != null) {
		fileLines.offer(line);
		fileSize += line.length();
	    }
	    
	    int splitSize = fileSize/Integer.parseInt(args[0]);
	    line = fileLines.poll();
	    int offset = 0;
	    
	    for(int i = 0; i < Integer.parseInt(args[0]); i++) {
		try{
		    fw = new FileWriter("/tmp/cordeiro/S" +
					Integer.toString(i) + ".txt");
		    BufferedWriter bw = new BufferedWriter(fw);
		    int currentSize = 0;
		    while(currentSize < splitSize && line != null) {
			if((currentSize + line.length() - offset) < splitSize) {
			    bw.write(line, offset, line.length() - offset);
			    bw.newLine();
			    bw.flush();
			    line = fileLines.poll();
			    currentSize += line.length();
			    offset = 0;
			}
			else {
			    bw.write(line, offset, splitSize - currentSize);
			    bw.flush();
			    String c = null;
			    try {
				c = line.substring(offset + splitSize - currentSize,
						   offset + splitSize - currentSize + 1);
			    }catch(Exception e){}
			    offset += splitSize - currentSize + 1;
			    while(c != null && !c.equals(" ")) {
				bw.write(c);
				try{
				c = line.substring(offset, offset + 1);
				}catch(Exception e){c = null;}
				offset++;
			    }
			    bw.newLine();
			    bw.flush();
			    fw.close();
			    if(c == null) {
				line = fileLines.poll();
				offset = 0;
			    }
			    break;
			}
		    }
		}catch(Exception e){}
	    }
	    
	    fw = new FileWriter("machines.txt");
	    BufferedWriter bw = new BufferedWriter(fw);

	    int i = 0;
	    for(String pcName : pcs) {
		CopySplit cs = new CopySplit(pcName, i);
		threads.add(cs);
		cs.start();
		pcMap.put(pcName, i);
		bw.write(pcName);
		bw.newLine();
		i = i + 1;
		if(i == Integer.parseInt(args[0])) break;
	    }
	    bw.flush();
	    fw.close();
	    for(Object obj : threads) {
		CopySplit cs = (CopySplit) obj;
		cs.join();
	    }
	}catch(FileNotFoundException f) {
	    System.out.println("File not found!!");
	}catch(IOException ioe) {}
	catch(InterruptedException ie) {}
	finally{
	    try {
		fs.close();
		threads.clear();
		pcs.clear();
	    }
	    catch(Exception e){}
	}

	System.out.println(pcMap);
	ProcessBuilder pb = null;
	fs = null;
	
	try {
	    long startTime = System.currentTimeMillis();
	    ExecPhase("0", "S", pcMap, threads);
	    long endTime = System.currentTimeMillis();
	    System.out.println("MAP FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");

	    startTime = System.currentTimeMillis();
	    ExecPhase("1", "UM", pcMap, threads);
	    endTime = System.currentTimeMillis();
	    System.out.println("SHUFFLE FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");
	    
	    startTime = System.currentTimeMillis();
	    ExecPhase("2", "", pcMap, threads);
	    endTime = System.currentTimeMillis();	    
	    System.out.println("REDUCE FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");
	}catch(Exception e){}
	finally{
	    try{fs.close();}
	    catch(Exception e){}}
    }
}
