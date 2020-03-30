import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Clean {
    public static void main(String[] args) {
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	try{
	    FileInputStream fs = new FileInputStream("pcs.txt");
	    InputStreamReader rs = new InputStreamReader(fs);
	    BufferedReader br = new BufferedReader(rs);

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
		ClearSlave cs = new ClearSlave(pcName);
		threads.add(cs);
		cs.start();
	    }
	    for(Object obj : threads) {
		ClearSlave cs = (ClearSlave) obj;
		cs.join();
	    }
	}catch(FileNotFoundException f) {
	    System.out.println("File not found!!");
	}catch(IOException ioe) {}
	catch(InterruptedException ie) {}
    }

}

class GetHostName extends Thread{
    String pcName;
    ConcurrentLinkedQueue pcs;
    public GetHostName(String pcName, ConcurrentLinkedQueue pcs) {
	this.pcName = pcName;
	this.pcs = pcs;
    }

    public void run() {
	ProcessBuilder pb = new ProcessBuilder("ssh", pcName, "hostname", "-s");
	try {
	    Process pc = pb.start();
	    BufferedReader reader =
		new BufferedReader(new InputStreamReader(pc.getInputStream()));
	    
	    pc.waitFor(5, TimeUnit.SECONDS);
	    String line = reader.readLine();
	    pc.destroy();
	    if(line.equals(pcName))
		pcs.add(pcName);
	}catch(Exception e){}
    }
}

class ClearSlave extends Thread {
    String pcName;
    public ClearSlave(String pcName) {
	this.pcName = pcName;
    }
    public void run() {
	try {
	    ProcessBuilder pb = new ProcessBuilder("ssh", pcName, "rm", "-rf", "/tmp/cordeiro");
	    Process process = pb.start();
	    process.waitFor();
	    process = pb.start();
	    process.waitFor();
	}catch(Exception e){}
	}
    }

