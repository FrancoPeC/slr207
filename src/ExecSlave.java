import java.io.*;

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

