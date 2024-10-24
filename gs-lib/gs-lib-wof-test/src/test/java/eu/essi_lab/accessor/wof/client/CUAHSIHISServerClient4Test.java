package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;

/**
 * Same as the integration test, but with a mocked client
 * 
 * @author boldrini
 */
public class CUAHSIHISServerClient4Test extends CUAHSIHISServerClient4ExternalTestIT {

    @Before
    public void init() {
	this.client = new FakeHISServerClient(CUAHSIEndpoints.ENDPOINT4);
    }

    @Test
    public void testServer4() throws GSException, UnsupportedEncodingException, TransformerException {
	super.testServer4();
    }

    @Test
    public void testServer4BIS() throws ParseException, GSException {
	super.testServer4BIS();
    }

}
