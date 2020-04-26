import java.io.*;

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

