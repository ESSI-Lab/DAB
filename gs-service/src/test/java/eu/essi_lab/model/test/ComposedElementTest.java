/**
 * 
 */
package eu.essi_lab.model.test;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementBuilder;
import eu.essi_lab.model.resource.composed.ComposedElementItem;

/**
 * @author Fabrizio
 */
public class ComposedElementTest {

    @Test
    public void test1() throws JSONException, Exception {

	ComposedElement composedElement = MetadataElement.KEYWORD_SA.createComposedElement().get();

	Assert.assertEquals(ComposedElementItem.DEFAULT_STRING_VALUE, composedElement.getProperty("value").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_STRING_VALUE, composedElement.getProperty("uri").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_STRING_VALUE, composedElement.getProperty("SA_uri").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_STRING_VALUE, composedElement.getProperty("SA_matchType").get().getValue());

	ComposedElement composedElement1 = ComposedElement.create(//
		new JSONObject(composedElement.asJSON().toString()),//
		MetadataElement.KEYWORD_SA.createComposedElement().get());

	Assert.assertEquals(composedElement, composedElement1);

	ComposedElement composedElement2 = ComposedElement.create(//
		composedElement.asJSON(),//
		MetadataElement.KEYWORD_SA.createComposedElement().get());

	Assert.assertEquals(composedElement, composedElement2);

	ComposedElement composedElement3 = ComposedElement.create(composedElement.asStream());

	Assert.assertEquals(composedElement, composedElement3);

	ComposedElement composedElement4 = ComposedElement.create(composedElement.asDocument(false));

	Assert.assertEquals(composedElement, composedElement4);

	//
	//
	//

	composedElement.getProperty("value").get().setValue("erggvb");
	composedElement.getProperty("uri").get().setValue("xvfhg");
	composedElement.getProperty("SA_uri").get().setValue("4566g");
	composedElement.getProperty("SA_matchType").get().setValue("xzcx");

	Assert.assertEquals("erggvb", composedElement.getProperty("value").get().getValue());
	Assert.assertEquals("xvfhg", composedElement.getProperty("uri").get().getValue());
	Assert.assertEquals("4566g", composedElement.getProperty("SA_uri").get().getValue());
	Assert.assertEquals("xzcx", composedElement.getProperty("SA_matchType").get().getValue());
    }

    @Test
    public void defaultValuesTest() throws JSONException, Exception {

	ComposedElement composedElement = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		addItem("boolean", ContentType.BOOLEAN).//
		addItem("text", ContentType.TEXTUAL).//
		addItem("date", ContentType.ISO8601_DATE).//
		addItem("dateTime", ContentType.ISO8601_DATE_TIME).//
		build();//

	String name = composedElement.getName();
	Assert.assertEquals("test", name);

	Assert.assertEquals(ComposedElementItem.DEFAULT_INT_VALUE, composedElement.getProperty("integer").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_DOUBLE_VALUE, composedElement.getProperty("double").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_LONG_VALUE, composedElement.getProperty("long").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_BOOLEAN_VALUE, composedElement.getProperty("boolean").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_STRING_VALUE, composedElement.getProperty("text").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_ISO8601_DATE_VALUE, composedElement.getProperty("date").get().getValue());
	Assert.assertEquals(ComposedElementItem.DEFAULT_SO8601_DATE_TIME_VALUE, composedElement.getProperty("dateTime").get().getValue());

	String iso8601Date = ISO8601DateTimeUtils.getISO8601Date();
	String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10.5);
	composedElement.getProperty("long").get().setValue(Long.MAX_VALUE);
	composedElement.getProperty("boolean").get().setValue(true);
	composedElement.getProperty("text").get().setValue("xxx");
	composedElement.getProperty("date").get().setValue(iso8601Date);
	composedElement.getProperty("dateTime").get().setValue(iso8601DateTime);

	Assert.assertEquals(10, composedElement.getProperty("integer").get().getValue());
	Assert.assertEquals(10.5, composedElement.getProperty("double").get().getValue());
	Assert.assertEquals(Long.MAX_VALUE, composedElement.getProperty("long").get().getValue());
	Assert.assertEquals(true, composedElement.getProperty("boolean").get().getValue());
	Assert.assertEquals("xxx", composedElement.getProperty("text").get().getValue());
	Assert.assertEquals(iso8601Date, composedElement.getProperty("date").get().getValue());
	Assert.assertEquals(iso8601DateTime, composedElement.getProperty("dateTime").get().getValue());
    }

    @Test
    public void fromJSONtoXMLTest() throws JSONException, Exception {

	ComposedElement composedElement = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		addItem("boolean", ContentType.BOOLEAN).//
		addItem("text", ContentType.TEXTUAL).//
		addItem("date", ContentType.ISO8601_DATE).//
		addItem("dateTime", ContentType.ISO8601_DATE_TIME).//
		build();//

	String iso8601Date = ISO8601DateTimeUtils.getISO8601Date();
	String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10.5);
	composedElement.getProperty("long").get().setValue(Long.MAX_VALUE);
	composedElement.getProperty("boolean").get().setValue(true);
	composedElement.getProperty("text").get().setValue("xxx");
	composedElement.getProperty("date").get().setValue(iso8601Date);
	composedElement.getProperty("dateTime").get().setValue(iso8601DateTime);

	Assert.assertEquals(10, composedElement.getProperty("integer").get().getValue());
	Assert.assertEquals(10.5, composedElement.getProperty("double").get().getValue());
	Assert.assertEquals(Long.MAX_VALUE, composedElement.getProperty("long").get().getValue());
	Assert.assertEquals(true, composedElement.getProperty("boolean").get().getValue());
	Assert.assertEquals("xxx", composedElement.getProperty("text").get().getValue());
	Assert.assertEquals(iso8601Date, composedElement.getProperty("date").get().getValue());
	Assert.assertEquals(iso8601DateTime, composedElement.getProperty("dateTime").get().getValue());

	//
	//
	//

	ComposedElement fromJSON = ComposedElement.create(composedElement.asJSON(), composedElement);

	Assert.assertEquals(composedElement, fromJSON);

	String name = fromJSON.getName();
	Assert.assertEquals("test", name);

	List<ComposedElementItem> properties = fromJSON.getProperties();

	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("integer")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("double")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("long")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("boolean")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("text")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("date")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().isPresent());

	Assert.assertEquals(10, properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getValue());
	Assert.assertEquals(10.5, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getValue());
	Assert.assertEquals(Long.MAX_VALUE, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getValue());
	Assert.assertEquals(true, properties.stream().filter(p -> p.getName().equals("boolean")).findFirst().get().getValue());
	Assert.assertEquals("xxx", properties.stream().filter(p -> p.getName().equals("text")).findFirst().get().getValue());
	Assert.assertEquals(iso8601Date, properties.stream().filter(p -> p.getName().equals("date")).findFirst().get().getValue());
	Assert.assertEquals(iso8601DateTime, properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().get().getValue());

	Assert.assertEquals(ContentType.INTEGER,
		properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getType());
	Assert.assertEquals(ContentType.DOUBLE, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getType());
	Assert.assertEquals(ContentType.LONG, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getType());
	Assert.assertEquals(ContentType.BOOLEAN,
		properties.stream().filter(p -> p.getName().equals("boolean")).findFirst().get().getType());
	Assert.assertEquals(ContentType.TEXTUAL, properties.stream().filter(p -> p.getName().equals("text")).findFirst().get().getType());
	Assert.assertEquals(ContentType.ISO8601_DATE,
		properties.stream().filter(p -> p.getName().equals("date")).findFirst().get().getType());
	Assert.assertEquals(ContentType.ISO8601_DATE_TIME,
		properties.stream().filter(p -> p.getName().equals("dateTime")).findFirst().get().getType());
    }

    @Test
    public void fromJSONtoXMLTest2() throws Exception {

	ComposedElement composedElement = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		build();//

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10);
	composedElement.getProperty("long").get().setValue(10);

	Assert.assertEquals(10, composedElement.getProperty("integer").get().getValue());
	Assert.assertEquals(10.0, composedElement.getProperty("double").get().getValue());
	Assert.assertEquals(10l, composedElement.getProperty("long").get().getValue());

	//
	//
	//

	JSONObject asJSON = composedElement.asJSON();

	ComposedElement fromJSON = ComposedElement.create(asJSON, composedElement);

	Assert.assertEquals(composedElement, fromJSON);

	String name = fromJSON.getName();
	Assert.assertEquals("test", name);

	List<ComposedElementItem> properties = fromJSON.getProperties();

	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("integer")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("double")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("long")).findFirst().isPresent());

	Assert.assertEquals(10, fromJSON.getProperty("integer").get().getValue());
	Assert.assertEquals(10.0, fromJSON.getProperty("double").get().getValue());
	Assert.assertEquals(10l, fromJSON.getProperty("long").get().getValue());

	Assert.assertEquals(10, properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getValue());
	Assert.assertEquals(10.0, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getValue());
	Assert.assertEquals(10l, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getValue());

	Assert.assertEquals(ContentType.INTEGER,
		properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getType());
	Assert.assertEquals(ContentType.DOUBLE, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getType());
	Assert.assertEquals(ContentType.LONG, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getType());
    }

    

    @Test
    public void fromJSONtoXMLTestWithModelTest() throws Exception {

	ComposedElement model = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		build();//

	model.getProperty("integer").get().setValue(10);
	model.getProperty("double").get().setValue(10);
	model.getProperty("long").get().setValue(10);

	Assert.assertEquals(10, model.getProperty("integer").get().getValue());
	Assert.assertEquals(10.0, model.getProperty("double").get().getValue());
	Assert.assertEquals(10l, model.getProperty("long").get().getValue());

	JSONObject asJSON = new JSONObject("{\"test\":{\"double\":0,\"integer\":0,\"long\":0}}");

	ComposedElement fromJSON = ComposedElement.create(asJSON, model);

	String name = fromJSON.getName();
	Assert.assertEquals("test", name);

	List<ComposedElementItem> properties = fromJSON.getProperties();

	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("integer")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("double")).findFirst().isPresent());
	Assert.assertTrue(properties.stream().filter(p -> p.getName().equals("long")).findFirst().isPresent());

	Assert.assertEquals(0, fromJSON.getProperty("integer").get().getValue());
	Assert.assertEquals(0.0, fromJSON.getProperty("double").get().getValue());
	Assert.assertEquals(0l, fromJSON.getProperty("long").get().getValue());

	Assert.assertEquals(0, properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getValue());
	Assert.assertEquals(0.0, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getValue());
	Assert.assertEquals(0l, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getValue());

	Assert.assertEquals(ContentType.INTEGER,
		properties.stream().filter(p -> p.getName().equals("integer")).findFirst().get().getType());
	Assert.assertEquals(ContentType.DOUBLE, properties.stream().filter(p -> p.getName().equals("double")).findFirst().get().getType());
	Assert.assertEquals(ContentType.LONG, properties.stream().filter(p -> p.getName().equals("long")).findFirst().get().getType());
    }

    @Test
    public void test2() throws JSONException, Exception {

	ComposedElement composedElement = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		addItem("boolean", ContentType.BOOLEAN).//
		addItem("text", ContentType.TEXTUAL).//
		addItem("date", ContentType.ISO8601_DATE).//
		addItem("dateTime", ContentType.ISO8601_DATE_TIME).//
		build();//

	String iso8601Date = ISO8601DateTimeUtils.getISO8601Date();
	String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10.5);
	composedElement.getProperty("long").get().setValue(Long.MAX_VALUE);
	composedElement.getProperty("boolean").get().setValue(true);
	composedElement.getProperty("text").get().setValue("xxx");
	composedElement.getProperty("date").get().setValue(iso8601Date);
	composedElement.getProperty("dateTime").get().setValue(iso8601DateTime);

	ComposedElement composedElement1 = ComposedElement.create(new JSONObject(composedElement.asJSON().toString()), composedElement);

	Assert.assertEquals(composedElement, composedElement1);

	ComposedElement composedElement2 = ComposedElement.create(composedElement.asJSON(), composedElement);

	Assert.assertEquals(composedElement, composedElement2);

	ComposedElement composedElement3 = ComposedElement.create(composedElement.asStream());

	Assert.assertEquals(composedElement, composedElement3);

	ComposedElement composedElement4 = ComposedElement.create(composedElement.asDocument(false));

	Assert.assertEquals(composedElement, composedElement4);
    }

    @Test
    public void test3() throws JSONException, Exception {

	ComposedElement composedElement = ComposedElementBuilder.get("test").//
		addItem("integer", ContentType.INTEGER).//
		addItem("double", ContentType.DOUBLE).//
		addItem("long", ContentType.LONG).//
		build();//

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10);
	composedElement.getProperty("long").get().setValue(10);

	Assert.assertEquals(10, composedElement.getProperty("integer").get().getValue());
	Assert.assertEquals(10.0, composedElement.getProperty("double").get().getValue());
	Assert.assertEquals(10l, composedElement.getProperty("long").get().getValue());

	composedElement.getProperty("integer").get().setValue(10);
	composedElement.getProperty("double").get().setValue(10.5);
	composedElement.getProperty("long").get().setValue(Long.MAX_VALUE);

	ComposedElement composedElement1 = ComposedElement.create(new JSONObject(composedElement.asJSON().toString()), composedElement);

	Assert.assertEquals(composedElement, composedElement1);

	ComposedElement composedElement2 = ComposedElement.create(composedElement.asJSON(), composedElement);

	Assert.assertEquals(composedElement, composedElement2);

	ComposedElement composedElement3 = ComposedElement.create(composedElement.asStream());

	Assert.assertEquals(composedElement, composedElement3);

	ComposedElement composedElement4 = ComposedElement.create(composedElement.asDocument(false));

	Assert.assertEquals(composedElement, composedElement4);
    }

}
