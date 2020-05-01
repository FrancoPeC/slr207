import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {

    /*
     * Execute one of the following phases: map, shuffle or reduce.
     * It creates and executes threads of the type ExecSlave and gives it the mode 
     * value that corresponds to that phase.
     */
    private static void ExecPhase(String mode, String fileName,
				  ArrayList<String> pcList,
				  ArrayList<Object> threads) throws Exception {
	for (int i = 0; i < pcList.size(); i++) {
	    try {
		ExecSlave es = new ExecSlave(mode, pcList.get(i),
					     fileName +  Integer.toString(i) + ".txt");
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

    /* 
     * Main function that counts the number of words from an input file.
     * It receives the number of splits used and the name of the input file.
     */
    public static void main(String[] args) {
	String thisPc = null;
	try {thisPc = InetAddress.getLocalHost().getHostName();}
	catch(Exception e){}
	
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	ArrayList<String> pcList = new ArrayList<String>();
	FileReader fs = null;
	FileWriter fw = null;
	String inputFileName = null;
	int numSplits = 0;
	
	try{numSplits = Integer.parseInt(args[0]);}
	catch(Exception e){
	    System.out.println("Please enter the number of splits");
	    return;		
	}
	
	try{inputFileName = args[1];}
	catch(Exception e){}

	// Checks which machines are currently working.
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
	}catch(Exception e){}
	finally{
	    try{fs.close();}catch(Exception e){}
	    threads.clear();
	}
	System.out.println("Number of active pcs: " + Integer.toString(pcs.size()));

	// Creates the split files.
	RandomAccessFile inputFile = null;
	try {
	    if(inputFileName != null)
		inputFile = new RandomAccessFile(inputFileName, "r");
	    else
		inputFile = new RandomAccessFile("input.txt", "r");
	    
	    int splitSize = (int) (inputFile.length() / numSplits);
	    FileOutputStream split = null;
	    for(int i = 0; i < numSplits; i++) {
		try{
		    split = new FileOutputStream("/tmp/cordeiro/S" + Integer.toString(i) + ".txt");
		    byte readData[] = new byte[splitSize];
		    int numBytes = inputFile.read(readData);
		    split.write(readData, 0 , numBytes);
		    int c;
		    c = inputFile.read();
		    while(c != (int) ' ' && c != (int) '\n' && c != -1) {
			split.write(c);
			c = inputFile.read();
		    }
		}catch(Exception e){}
		finally{
		    try{split.close();}catch(Exception e){}
		}
	    }
	}catch(Exception e){}
	finally{
	    try{inputFile.close();}catch(Exception e){}
	}

	/* 
	 * Copies the splits to the computers and generates the file containing
	 * the machines used for this run.
	 */
	try {
	    fw = new FileWriter("machines.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
	    Object pcArray[] = pcs.toArray();
	    int i = 0;
	    while(i != numSplits) {
		String pcName = (String) pcArray[i % numSplits];
		CopySplit cs = new CopySplit(pcName, i);
		threads.add(cs);
		cs.start();
		pcList.add(pcName);
		if(i < pcs.size()) {
		    bw.write(pcName);
		    bw.newLine();
		}
		i++;
	    }
	    bw.flush();
	    fw.close();
	    for(Object obj : threads) {
		CopySplit cs = (CopySplit) obj;
		cs.join();
	    }
	}catch(Exception e){}
	finally{
	    try {
		fw.close();
		threads.clear();
		pcs.clear();
	    }
	    catch(Exception e){}
	}

	// Executes the three concurrent phases while timing them.
	try {
	    long startTime = System.currentTimeMillis();
	    ExecPhase("0", "S", pcList, threads);
	    long endTime = System.currentTimeMillis();
	    System.out.println("MAP FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");

	    startTime = System.currentTimeMillis();
	    ExecPhase("1", "UM", pcList, threads);
	    endTime = System.currentTimeMillis();
	    System.out.println("SHUFFLE FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");
	    
	    startTime = System.currentTimeMillis();
	    ExecPhase("2", "", pcList, threads);
	    endTime = System.currentTimeMillis();	    
	    System.out.println("REDUCE FINISHED");
	    System.out.println("Time taken: " + (endTime - startTime) + " ms");
	}catch(Exception e){}
	finally{
	    try{fs.close();}
	    catch(Exception e){}}

	// Collects and prints the number of occurences of each word.
	try {
	    for(String pcName : pcList) {
		GetReduces gr = new GetReduces(pcName);
		threads.add(gr);
		gr.start();
	    }

	    for(Object obj : threads) {
		GetReduces gr = (GetReduces) obj;
		gr.join();
	    }
	}catch(Exception e){}
    }
}
