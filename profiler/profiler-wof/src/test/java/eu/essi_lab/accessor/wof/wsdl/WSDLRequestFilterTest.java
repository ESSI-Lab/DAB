package eu.essi_lab.accessor.wof.wsdl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public class WSDLRequestFilterTest {

    private static final String PATH = "http://localhost:123/path?";
    private WSDLRequestFilter filter;

    @Before
    public void init() {
	this.filter = new WSDLRequestFilter();
    }

    @Test
    public void testGood() throws GSException {

	assertTrue(filter.accept(WebRequest.createGET(PATH + "WSDL")));

	assertTrue(filter.accept(WebRequest.createGET(PATH + "wsdl")));

    }

    @Test
    public void testBad() throws GSException {

	// POST request
	assertFalse(filter.accept(WebRequest.createGET(PATH, "")));

	assertFalse(filter.accept(WebRequest.createGET(PATH + "WSDLsomething")));

	assertFalse(filter.accept(WebRequest.createGET(PATH + "somethingWSDL")));

	assertFalse(filter.accept(WebRequest.createGET(PATH + "hrewiher")));

    }
}
