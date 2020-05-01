import java.util.*;
import java.io.*;
import java.net.*;

public class Slave {
    
    // Main function that executes the right phase according to the argument received.
    public static void main(String[] args) {
	switch(Integer.parseInt(args[0])) {
	case 0: mapPhase(args[1]);
	case 1: shufflePhase(args[1]);
	case 2: reducePhase();
	}
    }

    // Executes the map phase.
    private static void mapPhase(String split) {
	FileReader fr = null;
	FileWriter fw = null;
	ProcessBuilder pb = null;

	/* 
	 * Tries to read the split file then creates the maps directory and a map file.
	 * On the map file, writes one line for each word on the split file.
	 */
	try {
	    fr = new FileReader("/tmp/cordeiro/splits/" + split);
	    BufferedReader input = new BufferedReader(fr);

	    pb = new ProcessBuilder("mkdir", "/tmp/cordeiro/maps");
	    Process pc = pb.start();

	    pc.waitFor();

	    fw = new FileWriter("/tmp/cordeiro/maps/UM" + split.substring(1, split.length()));
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

    // Executes the shuffle phase.
    private static void shufflePhase(String map) {
	FileReader fr = null;
	FileWriter fw = null;
	ProcessBuilder pb = null;
	
	try {
	    fr = new FileReader("/tmp/cordeiro/maps/" + map);
	    BufferedReader input = new BufferedReader(fr);

	    pb = new ProcessBuilder("mkdir", "/tmp/cordeiro/shuffles");
	    Process pc = pb.start();

	    pc.waitFor();

	    HashSet<String> shuffles = new HashSet<String>();

	    // For each line on the map file, copy it to the corresponding shuffle file.
	    String line;
	    while((line = input.readLine()) != null) {
		String[] words = line.split(" ");
		String fileName = "/tmp/cordeiro/shuffles/" +
		    Integer.toString(words[0].hashCode()) +
		    "-" + InetAddress.getLocalHost().getHostName() + ".txt";
		shuffles.add(Integer.toString(words[0].hashCode()));
		try {
		    fw = new FileWriter(new File(fileName), true);
		    fw.write(line);
		    fw.write("\n");
		    fw.close();
		}catch(Exception e) {}
	    }
	    fr.close();
	    
	    fr = new FileReader("/tmp/cordeiro/machines.txt");
	    input = new BufferedReader(fr);
	    
	    ArrayList<String> pcs = new ArrayList<String>();
	    ArrayList<Boolean> createdDir = new ArrayList<Boolean>();
	    while((line = input.readLine()) != null) {
		pcs.add(line);
		createdDir.add(false);
	    }

	    // Copies the shuffle files to the corresponding machines.
	    for(String hash : shuffles) {
		String fileName = "/tmp/cordeiro/shuffles/" + hash +
		    "-" + InetAddress.getLocalHost().getHostName() + ".txt";
		int machine = Integer.parseInt(hash) % pcs.size();
		if(!createdDir.get(machine)) {
		    try {
			pb = new ProcessBuilder("ssh", pcs.get(machine), 
						"mkdir", "-p",
						"/tmp/cordeiro/shufflesreceived");
			pc = pb.start();
			pc.waitFor();
			createdDir.set(machine, true);
		    }catch(Exception e) {}
		}
		try {
		    pb = new ProcessBuilder("scp", fileName, pcs.get(machine) + ":/tmp/cordeiro/shufflesreceived/");
		    pc = pb.start();
		    pc.waitFor();
		}catch(Exception e) {}
	    }
	    
	}catch(Exception e) {}
	finally{
	    try{fr.close(); fw.close();}
	    catch(Exception e){}
	}
    }

    // Executes the reduce phase.
    private static void reducePhase() {
	FileReader fr = null;
	FileWriter fw = null;
	ProcessBuilder pb = null;
	
	try {
	    pb = new ProcessBuilder("mkdir", "/tmp/cordeiro/reduces");
	    Process pc = pb.start();

	    pc.waitFor();

	    pb = new ProcessBuilder("ls", "/tmp/cordeiro/shufflesreceived");

	    pc = pb.start();
	    BufferedReader reader =
		new BufferedReader(new InputStreamReader(pc.getInputStream()));

	    ArrayList<String> files = new ArrayList<String>();
	    String line;
	    while((line = reader.readLine()) != null) {
		files.add(line);
	    }
	    
	    pc.waitFor();

	    /*
	     * For each file on the shufflesreceived directory, counts the number of
	     * lines on the file and saves it on the reduce file.
	     */
	    String lastHash = null;
	    for(int i = 0; i < files.size();) {
		String firstHash = files.get(i).split("-")[0];
		System.out.println(firstHash);
		String hash = firstHash;
		String key = null;
		int count = 0;
		while(hash.equals(firstHash)) {
		    try {
			fr = new FileReader("/tmp/cordeiro/shufflesreceived/" +
					    files.get(i));
			BufferedReader br = new BufferedReader(fr);

			while((line = br.readLine()) != null) {
			    if(count == 0) key = line.split(" ")[0];
			    count++;
			}
		    
		    i++;
		    if(i == files.size()) break;
		    hash = files.get(i).split("-")[0];
		    }catch(Exception e){}
		    finally{
			try{fr.close();}catch(Exception e){}
		    }
		}
		
		try {
		fw = new FileWriter("/tmp/cordeiro/reduces/" + firstHash + ".txt");
		fw.write(key + " " + Integer.toString(count) + "\n");
		}catch(Exception e) {}
		finally {
		    try{fw.close();}catch(Exception e){}
		}
	    }
	}catch(Exception e){}
    }
}
