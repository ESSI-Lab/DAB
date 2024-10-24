package eu.essi_lab.profiler.csw.test.getcap.response;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetCapabilities;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptFormatsType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWGetCapabilitiesHandler;

/**
 * @author Fabrizio
 */
public class GetCapabilitiesResponseTest {

    static {

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Test
    public void test1() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void test2() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&sections=ServiceIdentification,ServiceProvider");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    @Test
    public void test3() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&sections=Filter_Capabilities");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    @Test
    public void test4() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&sections=OperationsMetadata");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    @Test
    public void test5() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/csw?service=CSW&version=2.0.2&request=GetCapabilities");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    @Test
    public void test6() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();

	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptFormats(new AcceptFormatsType());
	getCapabilities.getAcceptFormats().getOutputFormat().add("application/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/csv");

	WebRequest request = null;
	try {
	    request = createWEBRequest(getCapabilities);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    @Test
    public void test7() {

	CSWGetCapabilitiesHandler handler = new CSWGetCapabilitiesHandler();

	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptFormats(new AcceptFormatsType());
	getCapabilities.getAcceptFormats().getOutputFormat().add("application/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/csv");

	WebRequest request = null;
	try {
	    request = createWEBRequest(getCapabilities);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceIdentification)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:ServiceProvider)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ows:OperationsMetadata)");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader.evaluateBoolean("exists(//ogc:Filter_Capabilities)");
		Assert.assertTrue(exists);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

    }

    private WebRequest createWEBRequest(GetCapabilities getCap) throws Exception {

	ByteArrayInputStream inputStream = CommonContext.asInputStream(getCap, true);
	return WebRequest.createPOST("http://localhost/gs-service/services/essi/csw?", inputStream);
    }
}
