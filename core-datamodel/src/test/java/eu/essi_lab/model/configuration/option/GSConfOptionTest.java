//package eu.essi_lab.model.configuration.option;
//
//import java.io.IOException;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.slf4j.Logger;
//
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.model.GSSource;
//import eu.essi_lab.model.ResultsPriority;
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class GSConfOptionTest {
//
//    private Logger logger = GSLoggerFactory.getLogger(GSConfOptionTest.class);
//
//    @Test
//    public void serializeDeserialize() throws GSException, IOException {
//
//	GSConfOptionString opt = new GSConfOptionString();
//	opt.setKey("key");
//	opt.setMandatory(true);
//	opt.setValue("value");
//
//	String serialized = opt.serialize();
//
//	GSConfOption<?> deserialized = new Deserializer().deserialize(serialized, GSConfOption.class);
//
//	Assert.assertEquals("key", deserialized.getKey());
//
//	Assert.assertEquals("value", deserialized.getValue());
//
//	Assert.assertEquals(true, deserialized.isMandatory());
//
//    }
//
//    @Test
//    public void serializeDeserializeNovalueConcrete() throws GSException, IOException {
//
//	GSConfOptionString opt = new GSConfOptionString();
//	opt.setKey("key");
//	opt.setMandatory(true);
//
//	String serialized = opt.serialize();
//
//	GSConfOption<?> deserialized = new Deserializer().deserialize(
//		"{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionString\",\"value\":\"val\",\"key\":\"k\",\"mandatory\":true}",
//		GSConfOption.class);
//
//	Assert.assertEquals("val", deserialized.getValue());
//
//    }
//
//    @Test
//    public void serializeDeserializeTypeLevel() throws GSException, IOException {
//
//	GSConfOptionGSSource opt = new GSConfOptionGSSource();
//	opt.setKey("key");
//	opt.setMandatory(true);
//	GSSource s = new GSSource();
//	s.setUniqueIdentifier("sid");
//	s.setResultsPriority(ResultsPriority.COLLECTION);
//
//	opt.setValue(s);
//
//	String serialized = opt.serialize();
//
//	logger.debug("Serialized GSConfOptionGSSource {}", serialized);
//
//	Assert.assertEquals(GSSource.class, new Deserializer().deserialize(serialized, GSConfOptionGSSource.class).getType());
//    }
//
//
//    @Test
//    public void serializeDeserializeGSConfOptionResultsPriority() throws GSException, IOException {
//
//	GSConfOptionResultsPriority opt = new GSConfOptionResultsPriority();
//	opt.setKey("key");
//	opt.setMandatory(true);
//
//	opt.setValue(ResultsPriority.COLLECTION);
//
//	String serialized = opt.serialize();
//
//	logger.debug("Serialized GSConfOptionResultsPriority {}", serialized);
//
//	Assert.assertEquals(ResultsPriority.class, new Deserializer().deserialize(serialized, GSConfOptionResultsPriority.class).getType());
//    }
//
//}
