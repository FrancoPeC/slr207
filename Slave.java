public class Slave {

    public static void main(String[] args) {
	int a = 3+5;
	try {
	    Thread.sleep(10000);
	}catch(Exception e){}
	System.out.println(a);
    }
}
