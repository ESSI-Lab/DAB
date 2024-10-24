package eu.essi_lab.accessor.bnhs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.model.GSSource;

public class BNHSConnectorExternalTestIT {

    @Test
    public void test() throws Exception {
	BNHSConnector connector = new BNHSConnector();
	GSSource source1 = new GSSource();
	source1.setEndpoint("https://docs.google.com/spreadsheets/d/1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI/edit#gid=253032876");
	assertTrue(connector.supports(source1));
	
	GSSource source2 = new GSSource();
	source2.setEndpoint("https://www.google.com");
	assertFalse(connector.supports(source2));
    }
}
