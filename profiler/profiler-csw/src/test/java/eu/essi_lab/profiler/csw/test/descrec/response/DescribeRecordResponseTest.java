package eu.essi_lab.profiler.csw.test.descrec.response;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWDescribeRecordHandler;

public class DescribeRecordResponseTest {

    static {

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Test
    public void test1() {

	CSWDescribeRecordHandler handler = new CSWDescribeRecordHandler();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.CSW_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMD_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMI_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.SRV_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void test2() {

	CSWDescribeRecordHandler handler = new CSWDescribeRecordHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmi:MI_Metadata,gmd:MD_Metadata");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMD_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMI_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.SRV_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.CSW_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void test3() {

	CSWDescribeRecordHandler handler = new CSWDescribeRecordHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmi:MI_Metadata");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMD_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMI_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.SRV_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.CSW_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void test4() {

	CSWDescribeRecordHandler handler = new CSWDescribeRecordHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmd:MD_Metadata");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMD_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMI_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.SRV_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.CSW_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void test6() {

	CSWDescribeRecordHandler handler = new CSWDescribeRecordHandler();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/csw?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=csw:Record");
	try {

	    String document = handler.getStringResponse(request);
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(inputStream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMD_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.GMI_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.SRV_NS_URI + "'])");
		Assert.assertFalse(exists);
	    }
	    {
		Boolean exists = reader
			.evaluateBoolean("exists(//csw:SchemaComponent[@targetNamespace='" + CommonNameSpaceContext.CSW_NS_URI + "'])");
		Assert.assertTrue(exists);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

}
