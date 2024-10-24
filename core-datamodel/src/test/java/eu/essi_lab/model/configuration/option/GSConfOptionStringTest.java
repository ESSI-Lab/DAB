//package eu.essi_lab.model.configuration.option;
//
//import java.util.Arrays;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class GSConfOptionStringTest {
//
//    @Test
//    public void testAllowedValues() throws GSException {
//	GSConfOptionString o = new GSConfOptionString();
//
//	o.setKey("test");
//
//	o.setLabel("label");
//
//	o.setAllowedValues(Arrays.asList(new String[] { "prova" }));
//
//	System.out.println(o.serialize());
//
//	GSConfOptionString deserialized = new Deserializer().deserialize(o.serialize(), GSConfOptionString.class);
//
//	Assert.assertNotNull("Expected non null allowed values", deserialized.getAllowedValues());
//
//	Assert.assertTrue(deserialized.getAllowedValues().contains("prova"));
//    }
//
//    @Test
//    public void testSetFromAllowedValues() throws GSException {
//	GSConfOptionString o = new GSConfOptionString();
//
//	o.setKey("test");
//
//	o.setLabel("label");
//
//	o.setAllowedValues(Arrays.asList(new String[] { "prova" }));
//
//	String serialized = "{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionString\",\"key\":\"test\","
//		+ "\"mandatory\":false,\"allowedValues\":[\"prova\"],\"label\":\"label\",\"type\":\"java.lang.String\","
//		+ "\"value\":\"prova\"}";
//
//	GSConfOptionString deserialized = new Deserializer().deserialize(serialized, GSConfOptionString.class);
//
//	Assert.assertNotNull("Expected non null allowed values", deserialized.getAllowedValues());
//
//	Assert.assertEquals("prova", deserialized.getValue());
//    }
//
//}
