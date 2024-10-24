package eu.essi_lab.testit.connector.fdsn;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.essi_lab.accessor.fdsn.FDSNConnector;
import eu.essi_lab.lib.net.utils.HttpRequestExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class FDSNConnectorExternalTestIT {

    @Test
    public void runRealQuery() throws Exception {

	String q = "includearrivals=false&minmag=0.0&includeallmagnitudes=false&includeallorigins=false&limit=10&offset=1&orderby=time";
	String endpoint = "http://earthquake.usgs.gov/fdsnws/event/1/query?";

	FDSNConnector connector = new FDSNConnector();

	URI uri = connector.buildUri(endpoint, q);

	System.out.println(uri.getHost());
	System.out.println(uri.getPath());
	System.out.println(uri.getScheme());
	System.out.println(uri.getQuery());
	System.out.println(uri);
	System.out.println(uri.toURL());

	HttpRequestBase httprequestbase = new HttpGet(uri);

	HttpResponse response = new HttpRequestExecutor().execute(httprequestbase);

	InputStream is = response.getEntity().getContent();

	XMLDocumentReader xmlDocument = new XMLDocumentReader(is);

	Map<String, String> namespaces = new HashMap<String, String>();
	namespaces.put("q", "http://quakeml.org/xmlns/quakeml/1.2");
	xmlDocument.setNamespaces(namespaces);

	Node input = xmlDocument.evaluateNode("//q:quakeml");

	input.normalize();

	NodeList eventParameters = input.getChildNodes();

	NodeList listEvents = null;

	for (int i = 0; i < eventParameters.getLength(); i++) {

	    Node n = eventParameters.item(i);
	    if (n.hasChildNodes()) {
		listEvents = n.getChildNodes();
	    }

	}

	if (listEvents != null) {
	    for (int j = 0; j < listEvents.getLength(); j++) {

		if (listEvents.item(j).getNodeName() != null) {
		    if (listEvents.item(j).getNodeName().equals("event")) {

			Node eventNode = listEvents.item(j);
			Node eventParamNode = eventNode.getParentNode().cloneNode(false);
			Node rootNode = input.cloneNode(false);

			eventParamNode.appendChild(eventNode);
			rootNode.appendChild(eventParamNode);

			StringWriter sw = new StringWriter();
			Transformer serializer = XMLFactories.newTransformerFactory().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(rootNode), new StreamResult(sw));

			System.out.println(sw.toString());

		    }

		}

	    }
	}

    }

    @Test
    public void countRequest() throws GSException {

	FDSNConnector connector = new FDSNConnector();

	connector.setSourceURL("http://earthquake.usgs.gov/fdsnws/event/1/");

	ReducedDiscoveryMessage messageNull = Mockito.mock(ReducedDiscoveryMessage.class);

	DiscoveryCountResponse all = connector.count(messageNull);

	ReducedDiscoveryMessage messageBBox = Mockito.mock(ReducedDiscoveryMessage.class);

	SpatialExtent value = new SpatialExtent(0, 0, 10, 10);

	Bond bboxBond = BondFactory.createSpatialExtentBond(BondOperator.CONTAINS, value);
	Mockito.when(messageBBox.getReducedBond()).thenReturn(bboxBond);

	DiscoveryCountResponse bboxCount = connector.count(messageBBox);

	Assert.assertTrue("No constraint count: " + all + " -- boox count: " + bboxCount, all.getCount() > bboxCount.getCount());

    }

    @Test
    public void queryRequest() throws GSException {

	FDSNConnector connector = new FDSNConnector();

	connector.setSourceURL("http://earthquake.usgs.gov/fdsnws/event/1/");

	ReducedDiscoveryMessage messageBBox = Mockito.mock(ReducedDiscoveryMessage.class);

	SpatialExtent value = new SpatialExtent(0, 0, 80, 100);

	Bond bboxBond = BondFactory.createSpatialExtentBond(BondOperator.CONTAINS, value);
	Mockito.when(messageBBox.getReducedBond()).thenReturn(bboxBond);

	Page page = new Page();

	page.setSize(5);
	page.setStart(1);

	ResultSet<OriginalMetadata> res = connector.query(messageBBox, page);

	Assert.assertTrue("Expected max 5 metadata, found " + res.getResultsList().size(), res.getResultsList().size() <= 5);

    }
}
