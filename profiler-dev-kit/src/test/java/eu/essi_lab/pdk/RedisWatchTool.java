package eu.essi_lab.pdk;

public class RedisWatchTool {


    public static void main(String[] args) {
	RedisTool tool = new RedisTool("essi-lab.eu", 6379);
//	RedisTool tool = new RedisTool("localhost", 6379, 0);

	long seconds = 10000;

	while (seconds-- > 0) {
	    tool.printAll();	    
//	    tool.printSummary("prod-intensive");
//	    tool.printSummary();

	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	tool.close();
    }

}
