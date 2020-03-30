import java.io.*;
import java.util.concurrent.TimeUnit;

public class Master {

    public static void main(String[] args) {
	ProcessBuilder pb = null;
	FileInputStream fs = null;
	try {
	    fs = new FileInputStream("pcSuccess.txt");
	    InputStreamReader rs = new InputStreamReader(fs);
	    BufferedReader br = new BufferedReader(rs);

	    String pcName;
	    while((pcName = br.readLine()) != null) {
		try {
		    pb = new ProcessBuilder("ssh", pcName, "java",
				       "-jar", "/tmp/cordeiro/Slave.jar");
		    Process pc = pb.start();
		    BufferedReader reader =
			new BufferedReader(new InputStreamReader(pc.getInputStream()));
		    BufferedReader readerErr =
			new BufferedReader(new InputStreamReader(pc.getErrorStream()));

		    Boolean writen = false;
		    OutReader std = new OutReader(reader, false, writen);
		    OutReader err = new OutReader(readerErr, true, writen);
		    std.start();
		    err.start();

		    int timeout = Integer.parseInt(args[0]);
		    pc.waitFor(timeout, TimeUnit.SECONDS);

		    if(!writen) {
			pc.destroy();
			std.interrupt();
			err.interrupt();
			System.out.println("Timeout!");
		    }
		    else {
			std.join();
			err.join();
		    }

		    int exitCode = pc.waitFor();
	    
		    if(exitCode != 0) {
			System.err.println("\nExited with error code : " + exitCode);
		    }

		}catch(IOException e) {
		    e.printStackTrace();
		}catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}catch(Exception e){}
	finally{
	    try{fs.close();}
	    catch(Exception e){}}
    }
}
