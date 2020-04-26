import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {

    public static void main(String[] args) {
	String thisPc = null;
	try {thisPc = InetAddress.getLocalHost().getHostName();}
	catch(Exception e){}
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	HashMap<String, Integer> pcMap = new HashMap<String, Integer>();
	FileReader fs = null;
	FileWriter fw = null;
	
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
		if(i == 3) break;
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
						   " mkdir -p /tmp/cordeiro/splits ; scp /tmp/cordeiro/S" +  (i) + ".txt " +
						   pcName + ":/tmp/cordeiro/splits");
	    Process process = pb.start();
	    process.waitFor();
	}catch(Exception e){}
    }
}

class ExecSlave extends Thread {
    String mode, fileName, pcName;
    public ExecSlave(String mode, String pcName, String fileName) {
	this.mode = mode;
	this.pcName = pcName;
	this.fileName = fileName;
    }
    public void run() {
	boolean flag = true;
	while(flag) {
	    try {
		ProcessBuilder pb = new ProcessBuilder("ssh", pcName, "java", "-jar",
						       "/tmp/cordeiro/Slave.jar",
						       mode, fileName);
		Process process = pb.start();
	    
		int exitCode = process.waitFor();
	    
		if(exitCode == 0) flag = false;
	    }catch(Exception e){}
	}
	if(mode.equals("0")) {
	    flag = true;
	    while(flag) {
		try{
		    ProcessBuilder pb = new ProcessBuilder("scp", "machines.txt",
						       pcName + ":/tmp/cordeiro/");

		    Process process = pb.start();

		    int exitCode = process.waitFor();

		    if(exitCode == 0) flag = false;
		}catch(Exception e){}
	    }
	}
    }
}

