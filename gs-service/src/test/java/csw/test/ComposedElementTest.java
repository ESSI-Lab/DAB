/**
 * 
 */
package csw.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementItem;

/**
 * @author Fabrizio
 */
public class ComposedElementTest {

    @Test
    public void test() throws JAXBException, ParserConfigurationException, SAXException, IOException {

	ComposedElement composedElement = MetadataElement.KEYWORD_SA.getComposeElement().get();

	composedElement.getProperty("value").get().setValue("erggvb");
	composedElement.getProperty("uri").get().setValue("xvfhg");
	composedElement.getProperty("SA_uri").get().setValue("4566g");
	composedElement.getProperty("SA_matchType").get().setValue("xzcx");

	String asString1 = composedElement.asString(false);

	InputStream asStream = composedElement.asStream();
	ComposedElement composedElement2 = ComposedElement.create(asStream);

	String asString2 = composedElement2.asString(false);

	Document asDocument = composedElement.asDocument(false);
	ComposedElement composedElement3 = ComposedElement.create(asDocument);

	String asString3 = composedElement3.asString(false);

	Assert.assertTrue(asString1.equals(asString2));
	Assert.assertTrue(asString1.equals(asString3));
    }

    @Test
    public void fromJSONtoXMLTest() throws Exception {

	JSONObject object = new JSONObject(//
		"{\"keyword_SA\": {" //
			+ "\"SA_uri\": \"4566g\"," //
			+ "\"SA_matchType\": \"xzcx\"," //
			+ "\"value\": \"erggvb\"," //
			+ "\"uri\": \"xvfhg\"" + "}})");

	ComposedElement el = ComposedElement.create(object);

	JSONObject asJSON = el.asJSON();

	Assert.assertTrue(object.similar(asJSON));

	String name = el.getName();
	Assert.assertEquals("keyword_SA", name);

	List<ComposedElementItem> properties = el.getProperties();

	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("SA_uri")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("SA_matchType")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("value")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("uri")).findFirst().isPresent());

	Assert.assertEquals("4566g", properties.stream().filter(p -> p.getName().equals("SA_uri")).findFirst().get().getValue());
	Assert.assertEquals("xzcx", properties.stream().filter(p -> p.getName().equals("SA_matchType")).findFirst().get().getValue());
	Assert.assertEquals("xvfhg", properties.stream().filter(p -> p.getName().equals("uri")).findFirst().get().getValue());
	Assert.assertEquals("erggvb", properties.stream().filter(p -> p.getName().equals("value")).findFirst().get().getValue());

	Assert.assertEquals(ContentType.TEXTUAL, properties.stream().filter(p -> p.getName().equals("SA_uri")).findFirst().get().getType());
	Assert.assertEquals(ContentType.TEXTUAL,
		properties.stream().filter(p -> p.getName().equals("SA_matchType")).findFirst().get().getType());
	Assert.assertEquals(ContentType.TEXTUAL, properties.stream().filter(p -> p.getName().equals("uri")).findFirst().get().getType());
	Assert.assertEquals(ContentType.TEXTUAL, properties.stream().filter(p -> p.getName().equals("value")).findFirst().get().getType());
    }

    @Test
    public void fromJSONtoXMLTest2() throws Exception {

	JSONObject object = new JSONObject(//
		"{\"keyword_SA\": {" //
			+ "\"integer\": 4566," //
			+ "\"double\": 89.03," //
			+ "\"date\": \"2025-01-01\"," //
			+ "\"dateTime\": \"2025-01-01T00:00:00Z\"" + "}})");
	
	ComposedElement el = ComposedElement.create(object);

	JSONObject asJSON = el.asJSON();

	Assert.assertTrue(object.similar(asJSON));

	String name = el.getName();
	Assert.assertEquals("keyword_SA", name);

	List<ComposedElementItem> properties = el.getProperties();

	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("integer")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("double")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("date")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().isPresent());

	Assert.assertEquals("4566", properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getValue());
	Assert.assertEquals("89.03", properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getValue());
	Assert.assertEquals("2025-01-01", properties.stream().filter(p -> p.getName().equals("date")).findFirst().get().getValue());
	Assert.assertEquals("2025-01-01T00:00:00Z", properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().get().getValue());

	Assert.assertEquals(ContentType.INTEGER, properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getType());
	Assert.assertEquals(ContentType.DOUBLE,properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getType());
	Assert.assertEquals(ContentType.ISO8601_DATE, properties.stream().filter(p -> p.getName().equals("date")).findFirst().get().getType());
	Assert.assertEquals(ContentType.ISO8601_DATE_TIME, properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().get().getType());
    }

    @Test
    public void asJSONTest() throws JAXBException, ParserConfigurationException, SAXException, IOException {

	ComposedElement composedElement = MetadataElement.KEYWORD_SA.getComposeElement().get();

	composedElement.getProperty("value").get().setValue("erggvb");
	composedElement.getProperty("uri").get().setValue("xvfhg");
	composedElement.getProperty("SA_uri").get().setValue("4566g");
	composedElement.getProperty("SA_matchType").get().setValue("xzcx");

	JSONObject json = composedElement.asJSON();

	System.out.println(json.toString(3));

	Assert.assertEquals(MetadataElement.KEYWORD_SA.getName(), json.keys().next());

	JSONObject properties = json.getJSONObject(MetadataElement.KEYWORD_SA.getName());

	Assert.assertEquals("erggvb", properties.get("value"));
	Assert.assertEquals("xvfhg", properties.get("uri"));
	Assert.assertEquals("4566g", properties.get("SA_uri"));
	Assert.assertEquals("xzcx", properties.get("SA_matchType"));
    }
}
