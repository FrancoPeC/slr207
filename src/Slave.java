import java.io.*;

public class Slave {

    public static void main(String[] args) {
	FileReader fr = null;
	FileWriter fw = null;
	ProcessBuilder pb = null;
	try {
	    fr = new FileReader("/tmp/cordeiro/splits/" + args[0]);
	    BufferedReader input = new BufferedReader(fr);

	    pb = new ProcessBuilder("mkdir", "/tmp/cordeiro/maps");
	    Process pc = pb.start();

	    int exitCode = pc.waitFor();

	    if(exitCode != 0) throw (new Exception());

	    fw = new FileWriter("/tmp/cordeiro/maps/UM" + args[0].substring(1, args[0].length()));
	    BufferedWriter output = new BufferedWriter(fw);
	    String line;
	    while((line = input.readLine()) != null) {
		line = line.replaceAll("\\p{P}", "");
		String[] words = line.split(" ");
		for(int i = 0; i < words.length; i++) {
		    if(!words[i].isBlank()) {
			output.write(words[i] + " 1");
			output.newLine();
		    }
		}
	    }
	    output.flush();
	}catch(Exception e){}
	finally {
	    try{fr.close(); fw.close();}
	    catch(Exception e){}
	}
    }
}
