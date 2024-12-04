package eu.essi_lab.accessor.wof.wsdl;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.wof.wsdl.HISCentralWSDLHandler;

public class HISCentralWSDLHandlerTest {
    
    static {

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    private HISCentralWSDLHandler handler;
    private WebRequest request;
    private HttpServletRequest servletRequest;

    @Before
    public void init() {
	this.handler = new HISCentralWSDLHandler();
	this.request = Mockito.mock(WebRequest.class);
	this.servletRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    public void test() throws Exception {

	Mockito.when(request.getServletRequest()).thenReturn(servletRequest);
	String mysrv = "http://my-custom-host.com/my-service";
	Mockito.when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(mysrv));
	String response = handler.getStringResponse(request);
	XMLDocumentReader reader = new XMLDocumentReader(response);
	String str = reader.evaluateString("//*:service/*:port/*:address/@location");
	assertEquals(mysrv, str);
    }

    @Test
    public void testMediaType() {
	Assert.assertEquals(new MediaType("text", "xml", "UTF-8"), handler.getMediaType(request));
    }

}
