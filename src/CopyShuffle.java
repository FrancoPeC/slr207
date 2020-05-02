import java.io.*;
    
class CopyShuffle extends Thread {

    String fileName, machine;
    
    public CopyShuffle(String fileName, String machine) {
	this.fileName = fileName;
	this.machine = machine;
    }
    public void run() {
	while(true) {
	    try {
		ProcessBuilder pb = new ProcessBuilder("scp", fileName, machine + ":/tmp/cordeiro/shufflesreceived/");
		Process pc = pb.start();
		int exitCode = pc.waitFor();
		if(exitCode == 0) break;
	    }catch(Exception e) {}
	}
    }
}
