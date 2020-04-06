import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GetHostName extends Thread{
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
