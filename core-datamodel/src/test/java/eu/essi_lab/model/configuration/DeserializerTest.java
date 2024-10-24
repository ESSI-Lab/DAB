//package eu.essi_lab.model.configuration;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
//import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class DeserializerTest {
//
//    @Test
//    public void testDeserializerErroGSException() throws GSException {
//
//	GSConfOptionBoolean bool = new GSConfOptionBoolean();
//
//	bool.setKey("bool-key");
//	bool.setValue(true);
//
//	String serialized = bool.serialize();
//
//	String replaced = serialized.replace("\"value\":true", "\"value\":\"stringValue\"");
//
//	try {
//
//	    new Deserializer().deserialize(replaced, GSConfOptionBoolean.class);
//
//	    Assert.assertTrue("Expected exception deserializing a non valid boolean configuration option", false);
//
//	} catch (GSException ex) {
//
//	    DefaultGSExceptionReader reader = new DefaultGSExceptionReader(ex);
//
//	    Assert.assertNull(reader.getAlienException());
//
//	}
//    }
//}
