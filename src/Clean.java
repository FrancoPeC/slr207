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
	    FileInputStream fs = new FileInputStream("tousMachines.txt");
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
	    System.out.println("Number of machines found:" + Integer.toString(pcs.size()));
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
	}catch(Exception e){}
	}
    }

