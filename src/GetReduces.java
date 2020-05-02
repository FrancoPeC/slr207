import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

class GetReduces extends Thread {
    String pcName;
    ConcurrentHashMap<String, Integer> map;
    public GetReduces(String pcName, ConcurrentHashMap<String, Integer> map) {
	this.pcName = pcName;
	this.map = map;
    }
    public void run() {
	try {
	    ProcessBuilder pb =
		new ProcessBuilder("ssh", pcName, "ls", "/tmp/cordeiro/reduces");
	    Process pc = pb.start();
	    BufferedReader reader =
		new BufferedReader(new InputStreamReader(pc.getInputStream()));

	    String files = "";
	    String line;
	    while((line = reader.readLine()) != null)
		files += " /tmp/cordeiro/reduces/" + line;
	    pc.waitFor();
	    
	    try {
		pb = new ProcessBuilder("ssh", pcName, "cat", files);
		pc = pb.start();
		reader = new BufferedReader(new InputStreamReader(pc.getInputStream()));
		while((line = reader.readLine()) != null) {
		    String words[] = line.split(" ");
		    map.put(words[0], Integer.parseInt(words[1]));
		}
		pc.waitFor();
	    }catch(Exception e) {}
	}catch(Exception e){}
    }
}

