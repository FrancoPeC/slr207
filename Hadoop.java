import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Hadoop {

    public static void main(String[] args) {
	try {
	    FileReader rinput;
	    if(args.length == 0) {
		 rinput = new FileReader("input.txt");
	    }else {
		 rinput = new FileReader(args[0]);
	    }
	    BufferedReader input = new  BufferedReader(rinput);

	    HashMap<String, Integer> numberOccurencies = new HashMap<String, Integer>();
	    String line;
	    long startTime = System.currentTimeMillis();
	    while((line = input.readLine()) != null) {
		line = line.replaceAll("\\p{P}", "");
		String[] words = line.split(" ");
		for(int i = 0; i < words.length; i++) {
		    if(!words[i].isBlank()) {
			Integer current = numberOccurencies.get(words[i].toLowerCase());
			if(current == null)
			    numberOccurencies.put(words[i].toLowerCase(), 1);
			else
			    numberOccurencies.replace(words[i].toLowerCase(), current + 1);
		    }
		}
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("Time to count: " + (endTime-startTime));
	    
	    startTime = System.currentTimeMillis();
	    HashMap<String, Integer> sorted = numberOccurencies.entrySet().stream()
		.sorted(HashMap.Entry.comparingByKey())
		.sorted(HashMap.Entry.comparingByValue(Comparator.reverseOrder()))
		.collect(Collectors.toMap(HashMap.Entry::getKey, HashMap.Entry::getValue,
                              (e1, e2) -> e1, LinkedHashMap::new));
	    
	    endTime = System.currentTimeMillis();
	    System.out.println("Time to sort: " + (endTime-startTime));
	    
	    sorted.forEach((word, count) -> System.out.println(word + " " + count));
	}catch(Exception e) {
	    System.out.println("Could not open file");
	}
    }
}
