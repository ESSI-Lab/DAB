package eu.essi_lab.accessor.eurobis.ld;

import java.io.IOException;

import org.junit.Test;

public class MarineOrganizationExternalTestIT {

    @Test
    public void test() throws IOException {
	MarineOrganization mi = new MarineOrganization("https://marineinfo.org/id/institute/91.ttl");
	mi.print();
    }

}
