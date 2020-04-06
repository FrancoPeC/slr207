import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Deploy {
    public static void main(String[] args) {
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	FileReader fs = null;
	FileWriter fos = null;
	try{
	    fs = new FileReader("pcs.txt");
	    BufferedReader br = new BufferedReader(fs);

	    fos = new FileWriter("pcSuccess.txt");
	    BufferedWriter bw = new BufferedWriter(fos);

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
	    for(String pcName : pcs) {
		bw.write(pcName);
		bw.newLine();
		CopySlave cs = new CopySlave(pcName);
		threads.add(cs);
		cs.start();
	    }
	    bw.flush();
	    for(Object obj : threads) {
		CopySlave cs = (CopySlave) obj;
		cs.join();
	    }
	}catch(FileNotFoundException f) {
	    System.out.println("File not found!!");
	}catch(IOException ioe) {}
	catch(InterruptedException ie) {}
	finally{
	    try {fs.close(); fos.close();}
	    catch(Exception e){}}
    }
}

class CopySlave extends Thread {
    String pcName;
    public CopySlave(String pcName) {
	this.pcName = pcName;
    }
    public void run() {
	try {
	    ProcessBuilder pb = new ProcessBuilder("bash", "-c", "ssh " + pcName +
						   " mkdir -p /tmp/cordeiro ; scp Slave.jar " +
						   pcName + ":/tmp/cordeiro");
	    Process process = pb.start();
	    process.waitFor();
	}catch(Exception e){}
    }
}
