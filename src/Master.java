import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {

    public static void main(String[] args) {
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	HashMap<String, Integer> pcMap = new HashMap<String, Integer>();
	FileReader fs = null;
	
	try{
	    fs = new FileReader("pcs.txt");
	    BufferedReader br = new BufferedReader(fs);

	    String line;
	    while((line = br.readLine()) != null) {
		String words[] = line.split(" ");
		for(int i = 1; i <= Integer.parseInt(words[1]); i++) {
		    GetHostName ghn = new GetHostName("tp-" +
						      words[0] + "-" +
						      Integer.toString(i), pcs);
		    threads.add(ghn);
		    ghn.start();
		}
	    }
	    for(Object obj : threads) {
		GetHostName ghn = (GetHostName) obj;
		ghn.join();
	    }
	    System.out.println(pcs.size());
	    threads = new ArrayList<Object>();
	    int i = 0;
	    for(String pcName : pcs) {
		CopySplit cs = new CopySplit(pcName, i);
		threads.add(cs);
		cs.start();
		pcMap.put(pcName, i);
		i = (i + 1) % 3;
	    }
	    
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
	    String pcName;
	    for (Map.Entry<String, Integer> entry : pcMap.entrySet()) {
		try {
		    // BufferedReader reader =
		    // 	new BufferedReader(new InputStreamReader(pc.getInputStream()));
		    // BufferedReader readerErr =
		    // 	new BufferedReader(new InputStreamReader(pc.getErrorStream()));

		    // Boolean writen = false;
		    // OutReader std = new OutReader(reader, false, writen);
		    // OutReader err = new OutReader(readerErr, true, writen);
		    // std.start();
		    // err.start();

		    // int timeout = Integer.parseInt(args[0]);
		    // pc.waitFor(timeout, TimeUnit.SECONDS);

		    // if(!writen) {
		    // 	pc.destroy();
		    // 	std.interrupt();
		    // 	err.interrupt();
		    // 	System.out.println("Timeout!");
		    // }
		    // else {
		    // 	std.join();
		    // 	err.join();
		    // }

		    // int exitCode = pc.waitFor();
	    
		    // if(exitCode != 0) {
		    // 	System.err.println("\nExited with error code : " + exitCode);
		    ExecSlave es = new ExecSlave(entry.getKey(),
						 "S" + Integer.toString(entry.getValue()) + ".txt");
		    threads.add(es);
		    es.start();
		}catch(Exception e){}
	    }
	    for(Object obj : threads) {
		ExecSlave es = (ExecSlave) obj;
		es.join();
	    }
	    System.out.println("MAP FINISHED");
	}catch(Exception e){}
	finally{
	    try{fs.close();}
	    catch(Exception e){}}
    }
}

class CopySplit extends Thread {
    String pcName;
    int i;
    public CopySplit(String pcName, int i) {
	this.pcName = pcName;
	this.i = i;
    }
    public void run() {
	try {
	    ProcessBuilder pb = new ProcessBuilder("bash", "-c", "ssh " + pcName +
						   " mkdir -p /tmp/cordeiro/splits ; scp /tmp/cordeiro/S" + Integer.toString(i) + ".txt " +
						   pcName + ":/tmp/cordeiro/splits");
	    Process process = pb.start();
	    process.waitFor();
	}catch(Exception e){}
    }
}

class ExecSlave extends Thread {
    String fileName, pcName;
    public ExecSlave(String pcName, String fileName) {
	this.pcName = pcName;
	this.fileName = fileName;
    }
    public void run() {
	try {
	    ProcessBuilder pb = new ProcessBuilder("ssh", pcName, "java", "-jar",
						   "/tmp/cordeiro/Slave.jar", fileName);
	    Process process = pb.start();
	    process.waitFor();
	}catch(Exception e){}
    }
}
