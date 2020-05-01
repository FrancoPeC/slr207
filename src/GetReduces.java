import java.io.*;
import java.util.ArrayList;

class GetReduces extends Thread {
    String pcName;
    public GetReduces(String pcName) {
	this.pcName = pcName;
    }
    public void run() {
	try {
	    ProcessBuilder pb =
		new ProcessBuilder("ssh", pcName, "ls", "/tmp/cordeiro/reduces");
	    Process pc = pb.start();
	    BufferedReader reader =
		new BufferedReader(new InputStreamReader(pc.getInputStream()));

	    ArrayList<String> files = new ArrayList<String>();
	    String line;
	    while((line = reader.readLine()) != null)
		files.add(line);
	    pc.waitFor();

	    for(String fileName : files) {
		try {
		    pb = new ProcessBuilder("ssh", pcName, "cat", "/tmp/cordeiro/reduces/" + fileName);
		    pc = pb.start();
		    reader = new BufferedReader(new InputStreamReader(pc.getInputStream()));
		    while((line = reader.readLine()) != null)
			System.out.println(line);
		    
		    pc.waitFor();
		}catch(Exception e) {}
	    }
	}catch(Exception e){}
    }
}

