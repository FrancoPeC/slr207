import java.io.BufferedReader;

public class OutReader extends Thread{
    BufferedReader stream;
    boolean err;
    Boolean writen;
    public OutReader(BufferedReader stream, boolean err, Boolean writen){
	    this.stream = stream;
	    this.err = err;
	    this.writen = writen;
	}
	public void run() {
	    try {
		String line;
		if(!err)
		    while ((line = stream.readLine()) != null){
			writen = true;
			System.out.println(line);
		    }
		else
		    while ((line = stream.readLine()) != null){
			writen = true;
			System.err.println(line);
		    }
	    }catch(Exception e){}
	}
    }
