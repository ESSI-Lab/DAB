package eu.essi_lab.accessor.wof.client;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.ServiceInfo;

public class CUAHSIHISCentralClientExternalTestIT {

    private CUAHSIHISCentralClient client;

    @Before
    public void init() {
	this.client = new CUAHSIHISCentralClient();
    }

    @Test
    public void test() throws Exception {
	List<ServiceInfo> services = this.client.getServicesInBox("-180", "-90", "180", "90");
	int i = 0;
	for (ServiceInfo service : services) {
	    String endpoint = service.getServiceURL();
	    System.out.println(i++ + ": " + service.getTitle()+" - "+endpoint);
	    assertTrue(service.getTitle() != null && !service.getTitle().isEmpty());
	    
	}
	System.out.println(services.size() + " services retrieved.");
	assertTrue(services.size() > 90);
    }
}
