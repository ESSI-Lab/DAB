package eu.essi_lab.model;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtendedMetadata;

public class ExtendedMetadataTest {

    @Test
    public void test() {

	try {

	    Dataset dataset = new Dataset();

	    ExtendedMetadata extendedMetadata = dataset.getHarmonizedMetadata().getExtendedMetadata();
	    Assert.assertNotNull(extendedMetadata);

	    // ----------------------
	    //
	    // initialization test
	    //
	    //
	    List<Node> list = extendedMetadata.get("//*");
	    Assert.assertNotNull(list);
	    Assert.assertEquals(1, list.size());

	    Assert.assertEquals(list.get(0).getNodeName(), "gs:extension");

	    String textContent = extendedMetadata.getTextContent("title");
	    Assert.assertNull(textContent);

	    textContent = extendedMetadata.getTextContent("gs", "title");
	    Assert.assertNull(textContent);

	    boolean set = extendedMetadata.setTextContent("title", "new title");
	    Assert.assertFalse(set);

	    // -----------------------------------------------------
	    //
	    // test equality of unmarhsalled dataset with extensions
	    //

	    // **********************
	    //
	    // 1) add some extensions
	    //
	    // **********************
	    extendedMetadata = dataset.getHarmonizedMetadata().getExtendedMetadata();
	    Assert.assertNotNull(extendedMetadata);

	    // add(String elementName, String textContent)
	    // getTextContent(xPath)
	    extendedMetadata.add("title", "title");
	    
	    extendedMetadata.add("title", "title");

	    String title = extendedMetadata.getTextContent("title");
	    Assert.assertEquals(title, "title");

	    // setTextContent(xPath)
	    set = extendedMetadata.setTextContent("title", "new title");
	    Assert.assertTrue(set);

	    // getTextContent(xPath)
	    title = extendedMetadata.getTextContent("title");
	    Assert.assertEquals(title, "new title");

	    title = extendedMetadata.getTextContent("gs", "title");
	    Assert.assertEquals(title, "new title");

	    // creates a node to add
	    DocumentBuilderFactory instance = XMLFactories.newDocumentBuilderFactory();
	    instance.setNamespaceAware(true);

	    DocumentBuilder builder = instance.newDocumentBuilder();
	    Element element = builder.newDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:element");
	    Element innerElement = element.getOwnerDocument().createElementNS(CommonNameSpaceContext.GMI_NS_URI, "gmi:innerElement");
	    innerElement.setTextContent("innerElementText");
	    element.appendChild(innerElement);

	    // adds the node
	    extendedMetadata.add(element);

	    list = extendedMetadata.get("//gmd:element");
	    Assert.assertEquals(1, list.size());

	    list = extendedMetadata.get("//gmd:element/gmi:innerElement");
	    Assert.assertEquals(1, list.size());

	    // here null is expected since innerElement has not the GI-suite name space
	    textContent = extendedMetadata.getTextContent("innerElement");
	    Assert.assertNull(textContent);

	    // here the right prefix is used
	    textContent = extendedMetadata.getTextContent("gmi", "innerElement");
	    Assert.assertEquals(textContent, "innerElementText");

	    extendedMetadata.setTextContent("gmi", "innerElement", "new content");
	    textContent = extendedMetadata.getTextContent("gmi", "innerElement");
	    Assert.assertEquals(textContent, "new content");

	    // *************************
	    //
	    // 2) unmarshals the dataset
	    //
	    // *************************

	    InputStream asStream = dataset.asStream();
	    dataset = Dataset.create(asStream);

	    extendedMetadata = dataset.getHarmonizedMetadata().getExtendedMetadata();

	    // *****************************
	    //
	    // 3) performs the equality test
	    //
	    // *****************************

	    list = extendedMetadata.get("//gmd:element");
	    Assert.assertEquals(1, list.size());

	    list = extendedMetadata.get("//gmd:element/gmi:innerElement");
	    Assert.assertEquals(1, list.size());

	    textContent = extendedMetadata.getTextContent("innerElement");
	    Assert.assertNull(textContent);

	    textContent = extendedMetadata.getTextContent("gmi", "innerElement");
	    Assert.assertEquals(textContent, "new content");

	    // -----------------------------------------------------
	    //
	    // tests the remove methods
	    //
	    //
	    boolean remove = extendedMetadata.remove("//rrr");
	    Assert.assertFalse(remove);

	    remove = extendedMetadata.remove("//gmi:innerElement");
	    Assert.assertTrue(remove);

	    textContent = extendedMetadata.getTextContent("gmi", "innerElement");
	    Assert.assertNull(textContent);

	    // clear
	    extendedMetadata.clear();

	    list = extendedMetadata.get("//*");
	    Assert.assertNotNull(list);
	    Assert.assertEquals(1, list.size());

	    Assert.assertEquals(list.get(0).getNodeName(), "gs:extension");

	    textContent = extendedMetadata.getTextContent("title");
	    Assert.assertNull(textContent);
	    textContent = extendedMetadata.getTextContent("gs", "title");
	    Assert.assertNull(textContent);

	    set = extendedMetadata.setTextContent("title", "new title");
	    Assert.assertFalse(set);

	    // ---------------------------------------------------------------
	    //
	    // checks that the extensions are empty on an unmarshalled dataset
	    //
	    //
	    asStream = dataset.asStream();
	    dataset = Dataset.create(asStream);

	    extendedMetadata = dataset.getHarmonizedMetadata().getExtendedMetadata();

	    list = extendedMetadata.get("//*");
	    Assert.assertNotNull(list);
	    Assert.assertEquals(1, list.size());

	    Assert.assertEquals(list.get(0).getNodeName(), "gs:extension");

	    textContent = extendedMetadata.getTextContent("title");
	    Assert.assertNull(textContent);
	    textContent = extendedMetadata.getTextContent("gs", "title");
	    Assert.assertNull(textContent);

	    set = extendedMetadata.setTextContent("title", "new title");
	    Assert.assertFalse(set);

	} catch (Exception ex) {

	    ex.printStackTrace();
	    fail("Exception thrown");
	}
    }

}
