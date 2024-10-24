//package eu.essi_lab.model.configuration.option;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.model.BrokeringStrategy;
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class GSConfOptionBrokeringStrategyTest {
//
//    @Test
//    public void testSerializeDeserialize() throws GSException {
//	List<BrokeringStrategy> list = new ArrayList<>();
//	list.add(BrokeringStrategy.HARVESTED);
//
//	GSConfOptionBrokeringStrategy opt = new GSConfOptionBrokeringStrategy(list);
//
//	String serialized = opt.serialize();
//	System.out.println(serialized);
//	GSConfOptionBrokeringStrategy deserialized = new Deserializer().deserialize(serialized, GSConfOptionBrokeringStrategy.class);
//
//	Assert.assertEquals(list, deserialized.getAllowedValues());
//
//    }
//
//    @Test
//    public void serializeDeserializeNovalueConcreteComplexValue() throws GSException, IOException {
//
//	String serialized = "{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionBrokeringStrategy\",\"value\":{\"valueConcrete\":\"eu.essi_lab.model.BrokeringStrategy\",\"value\":\"DISTRIBUTED\"},\"mandatory\":true}";
//
//	GSConfOptionBrokeringStrategy deserialized = new Deserializer().deserialize(serialized, GSConfOptionBrokeringStrategy.class);
//
//	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, deserialized.getValue());
//
//    }
//
//    @Test
//    public void serializeDeserialize() throws GSException, IOException {
//
//	List<BrokeringStrategy> list = new ArrayList<BrokeringStrategy>();
//	list.add(BrokeringStrategy.DISTRIBUTED);
//	GSConfOptionBrokeringStrategy opt = new GSConfOptionBrokeringStrategy(list);
//	opt.setKey("key");
//	opt.setMandatory(true);
//	opt.setValue(BrokeringStrategy.DISTRIBUTED);
//
//	String serialized = opt.serialize();
//
//	GSConfOptionBrokeringStrategy deserialized = new Deserializer().deserialize(serialized, GSConfOptionBrokeringStrategy.class);
//
//	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, deserialized.getValue());
//
//	Assert.assertEquals(1, new Deserializer().deserialize(serialized, GSConfOptionBrokeringStrategy.class).getAllowedValues().size());
//
//    }
//}
