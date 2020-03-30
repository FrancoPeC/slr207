import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Deploy {
    public static void main(String[] args) {
	ArrayList<Object> threads = new ArrayList<Object>();
	ConcurrentLinkedQueue<String> pcs = new ConcurrentLinkedQueue<String>();
	FileInputStream fs = null;
	FileOutputStream fos = null;
	try{
	    fs = new FileInputStream("pcs.txt");
	    InputStreamReader rs = new InputStreamReader(fs);
	    BufferedReader br = new BufferedReader(rs);

	    fos = new FileOutputStream("pcSuccess.txt");
	    OutputStreamWriter ws = new OutputStreamWriter(fos);
	    BufferedWriter bw = new BufferedWriter(ws);

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
