package eu.essi_lab.messages.bond;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.bond.jaxb.ViewFactory;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;

public class ViewTest {

    @Test
    public void extractViewAndTokenIdTest() {

	WebRequest request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/view/view1/anotherpath");
	Assert.assertEquals("view1", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/token/pippo/view/view1/anotherpath");
	Assert.assertEquals("view1", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/token/pippo/view/view1/anotherpath");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/token/pluto/");
	Assert.assertEquals("pluto", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/token/pluto");
	Assert.assertEquals("pluto", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/view/view2/anotherpath/anotherpath2");
	Assert.assertEquals("view2", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/view/view3");
	Assert.assertEquals("view3", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/view/view4");
	Assert.assertEquals("view4", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/view/view5");
	Assert.assertEquals("view5", request.extractViewId().get());

	//
	//
	//

	request = WebRequest.createGET("http://localhost:9090/gs-service/views");
	Assert.assertTrue(request.extractViewId().isEmpty());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views?token=pippo");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views?token=pippo&other=pluto");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views?token=pippo&other=pluto&other2=topolino");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views?other=topolino&other2=topolino&token=topolino");
	Assert.assertEquals("topolino", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views/viewxxx");
	Assert.assertEquals("viewxxx", request.extractViewId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views/viewxxx?token=pippo");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views/viewxxx?token=pippo&other=pluto");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views/viewxxx?token=pippo&other=pluto&other2=topolino");
	Assert.assertEquals("pippo", request.extractTokenId().get());

	request = WebRequest.createGET("http://localhost:9090/gs-service/views/viewxxx?other=topolino&other2=topolino&token=topolino");
	Assert.assertEquals("topolino", request.extractTokenId().get());

	request = WebRequest.createGET(
		"http://localhost:9090/gs-service/services/essi/view/geoss/oaipmh?verb=ListRecords&metadataPrefix=ISO19139-2006-GMI&set=eurac-research&resumptionToken=restoken%2F201%2Fnull%2Fnull%2Feurac-research%2FISO19139-2006-GMI");
	Assert.assertFalse(request.extractTokenId().isPresent());

	//
	//
	//

	request = WebRequest.createGET("http://localhost:9090/gs-service/services/semantic/pippo/anotherpath");
	Assert.assertTrue(request.extractViewId().isEmpty());

	request = WebRequest.createGET("http://localhost:9090");
	Assert.assertTrue(request.extractViewId().isEmpty());

	request = WebRequest.createGET("http://localhost:9090/");
	Assert.assertTrue(request.extractViewId().isEmpty());

	request = WebRequest.createGET("http");
	Assert.assertTrue(request.extractViewId().isEmpty());

	request = WebRequest.createGET("");
	Assert.assertTrue(request.extractViewId().isEmpty());
    }

    @Test
    public void test() throws JAXBException {

	View view = new View();
	view.setSourceDeployment("geoss");

	Assert.assertEquals("geoss", view.getSourceDeployment());

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	Marshaller m = ViewFactory.createMarshaller();

	m.marshal(view, System.out);
	m.marshal(view, baos);

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	Unmarshaller u = ViewFactory.createUnmarshaller();

	View view2 = (View) u.unmarshal(bais);
	Assert.assertEquals(view.getSourceDeployment(), view2.getSourceDeployment());
    }

    @Test
    public void testJAXB() throws Exception {

	// view bond
	testJAXB(BondFactory.createViewBond("view1"));

	// resource property bond
	testJAXB(BondFactory.createMinMaxResourceTimeStampBond(BondOperator.MIN));

	// simple value bond
	testJAXB(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "my title"));

	testJAXB(BondFactory.createSimpleValueBond(//
		BondOperator.GREATER_OR_EQUAL, //
		MetadataElement.TEMP_EXTENT_END, //
		"1990-01-01T00:00:00Z"));

	// spatial bond
	testJAXB(BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, new SpatialExtent(-90, -180, 90, 180)));

	// logical bond
	testJAXB(BondFactory.createAndBond());

	testJAXB(BondFactory.createAndBond(BondFactory.createViewBond("view1"), BondFactory.createViewBond("view2")));

	testJAXB(BondFactory.createOrBond(BondFactory.createViewBond("view1"), BondFactory.createViewBond("view2")));

	testJAXB(BondFactory.createOrBond(
		BondFactory.createAndBond(BondFactory.createViewBond("view1"), BondFactory.createViewBond("view2")),
		BondFactory.createViewBond("view3")));

	Set<Bond> operands = new HashSet<>();
	operands.add(BondFactory.createSimpleValueBond(//
		BondOperator.GREATER_OR_EQUAL, //
		MetadataElement.TEMP_EXTENT_END, //
		"1990-01-01T00:00:00Z"));//
	operands.add(BondFactory.createSimpleValueBond(//
		BondOperator.LESS_OR_EQUAL, //
		MetadataElement.TEMP_EXTENT_BEGIN, //
		"2000-01-01T00:00:00Z"));//
	testJAXB(BondFactory.createAndBond(operands));

    }

    /**
     * This test marshals a bond to stream and then unmarshals back the bond in order to compare the result object with
     * the original bond for equality.
     * 
     * @param bond
     * @throws JAXBException
     */
    private void testJAXB(Bond bond) throws JAXBException {
	ViewFactory factory = new ViewFactory();
	View view = factory.createView("id1", "label 1", bond);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	Marshaller m = ViewFactory.createMarshaller();
	m.marshal(view, System.out);
	m.marshal(view, baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	Unmarshaller u = ViewFactory.createUnmarshaller();
	Object result = u.unmarshal(bais);
	assertEquals(view, result);
	System.out.println("Done!");
	System.out.println();
    }

}
