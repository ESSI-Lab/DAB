//package eu.essi_lab.model.configuration.option;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.configuration.composite.GSConfiguration;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class VolatileOptionSetTest {
//
//    @Test
//    public void test() throws GSException {
//
//	VolatileOptionSet vos = new VolatileOptionSet();
//
////	GSConfOptionBoolean opt = new GSConfOptionBoolean();
////	opt.setKey("testkey");
////	opt.setValue(false);
//
////	GSConfOptionString opt2 = new GSConfOptionString();
////	opt2.setKey("testkey2");
////	opt2.setValue("value2");
//
////	vos.getOptions().put("testkey", opt);
////	vos.getOptions().put("testkey2", opt2);
//
//	vos.setContext(new GSConfiguration());
//
//	VolatileOptionSet deserialized = new Deserializer().deserialize(vos.serialize(), VolatileOptionSet.class);
//
////	Assert.assertEquals(false, deserialized.getOptions().get("testkey").getValue());
////
////	Assert.assertEquals("value2", deserialized.getOptions().get("testkey2").getValue());
//	Assert.assertEquals(new GSConfiguration().getLabel(), deserialized.getContext().getLabel());
//
//    }
//}