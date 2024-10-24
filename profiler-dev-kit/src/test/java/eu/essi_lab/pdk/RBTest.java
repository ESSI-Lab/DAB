package eu.essi_lab.pdk;

public class RBTest {
    public static void main(String[] args) throws Exception {
	DistributedRequestBouncer drb = new DistributedRequestBouncer("essi-lab.eu",6379,"prod-access",1,1,10);

	Thread.sleep(100000);
//	drb.checkForOldBlockingRequests();

	
    }
}
