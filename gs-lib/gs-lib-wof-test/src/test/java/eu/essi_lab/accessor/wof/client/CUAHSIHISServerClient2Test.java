package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;

/**
 * Same as the integration test, but with a mocked client
 * 
 * @author boldrini
 */
public class CUAHSIHISServerClient2Test extends CUAHSIHISServerClient2ExternalTestIT {

    @Before
    public void init() {
	this.client = new FakeHISServerClient(CUAHSIEndpoints.ENDPOINT2);
    }

    @Test
    public void testServer2() throws GSException, UnsupportedEncodingException, TransformerException {
	super.testServer2();
    }

}