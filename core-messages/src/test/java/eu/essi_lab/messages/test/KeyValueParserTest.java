package eu.essi_lab.messages.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.web.KeyValueParser;

public class KeyValueParserTest {

    @Test
    public void test1() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdentification&XXXXX";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsKey("service"));
	Assert.assertTrue(map.containsKey("Sections"));
	Assert.assertTrue(map.containsKey("XXXXX"));

	Assert.assertTrue(map.containsValue("GetCapabilities"));
	Assert.assertTrue(map.containsValue("CSW"));
	Assert.assertTrue(map.containsValue("ServiceIdentification"));
	Assert.assertTrue(map.containsValue(KeyValueParser.UNDEFINED));

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections"));
	Assert.assertEquals(KeyValueParser.UNDEFINED, map.get("XXXXX"));
    }

    @Test
    public void test2() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdentification&XXXXX=";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsKey("service"));
	Assert.assertTrue(map.containsKey("Sections"));
	Assert.assertTrue(map.containsKey("XXXXX"));

	Assert.assertTrue(map.containsValue("GetCapabilities"));
	Assert.assertTrue(map.containsValue("CSW"));
	Assert.assertTrue(map.containsValue("ServiceIdentification"));
	Assert.assertTrue(map.containsValue(KeyValueParser.UNDEFINED));

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections"));
	Assert.assertEquals(KeyValueParser.UNDEFINED, map.get("XXXXX"));
    }

    @Test
    public void test3() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdentification&XXXXX=&";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsKey("service"));
	Assert.assertTrue(map.containsKey("Sections"));
	Assert.assertTrue(map.containsKey("XXXXX"));

	Assert.assertTrue(map.containsValue("GetCapabilities"));
	Assert.assertTrue(map.containsValue("CSW"));
	Assert.assertTrue(map.containsValue("ServiceIdentification"));
	Assert.assertTrue(map.containsValue(KeyValueParser.UNDEFINED));

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections"));
	Assert.assertEquals(KeyValueParser.UNDEFINED, map.get("XXXXX"));
    }

    @Test
    public void test4() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdentification";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsKey("service"));
	Assert.assertTrue(map.containsKey("Sections"));

	Assert.assertTrue(map.containsValue("GetCapabilities"));
	Assert.assertTrue(map.containsValue("CSW"));
	Assert.assertTrue(map.containsValue("ServiceIdentification"));

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections"));
    }

    @Test
    public void test5() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdent=ifi=cation";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsKey("service"));
	Assert.assertTrue(map.containsKey("Sections"));

	Assert.assertTrue(map.containsValue("GetCapabilities"));
	Assert.assertTrue(map.containsValue("CSW"));
	Assert.assertTrue(map.containsValue("ServiceIdent=ifi=cation"));

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdent=ifi=cation", map.get("Sections"));
    }

    @Test
    public void test6() {

	String query = "request=";

	KeyValueParser parser = new KeyValueParser(query);
	Map<String, String> map = parser.getParametersMap();

	Assert.assertTrue(map.containsKey("request"));
	Assert.assertTrue(map.containsValue(KeyValueParser.UNDEFINED));
	Assert.assertEquals(map.get("request"), KeyValueParser.UNDEFINED);
    }

    @Test
    public void test7() {

	String query = "request=GetCapabilities&service=CSW&Sections=ServiceIdentification";

	KeyValueParser parser = new KeyValueParser(query);

	Assert.assertEquals("GetCapabilities", parser.getValue("request"));
	Assert.assertEquals("CSW", parser.getValue("service"));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections"));
    }

    @Test
    public void test8() {

	String query = "reqUest=GetCapabilities&SeRvIce=CSW&SeCTtiOns=ServiceIdentification";

	KeyValueParser parser = new KeyValueParser(query);

	Assert.assertEquals(null, parser.getValue("request"));
	Assert.assertEquals(null, parser.getValue("service"));
	Assert.assertEquals(null, parser.getValue("Sections"));
    }

    @Test
    public void test9() {

	String query = "reqUest=GetCapabilities&SeRvIce=CSW&SeCTiOns=ServiceIdentification";

	KeyValueParser parser = new KeyValueParser(query);

	Assert.assertEquals("GetCapabilities", parser.getValue("request", true));
	Assert.assertEquals("CSW", parser.getValue("service", true));
	Assert.assertEquals("ServiceIdentification", parser.getValue("Sections", true));
    }

}
