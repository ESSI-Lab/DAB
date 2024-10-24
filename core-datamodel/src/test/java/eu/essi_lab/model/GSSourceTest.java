//package eu.essi_lab.model;
//
//import org.junit.Test;
//
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
//import eu.essi_lab.model.configuration.option.GSConfOptionGSSource;
//import eu.essi_lab.model.configuration.option.GSConfOptionString;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class GSSourceTest {
//
//    @Test
//    public void test() throws GSException {
//
//	GSConfOptionString opt = new GSConfOptionString();
//
//	opt.setValue("valueval");
//
//	// System.out.println(opt.serialize());
//
//	GSConfOptionBoolean opt2 = new GSConfOptionBoolean();
//
//	opt2.setValue(true);
//
//	System.out.println(opt2.serialize());
//
//	GSSource s = new GSSource();
//
//	s.setUniqueIdentifier("id");
//	s.setEndpoint("endpoiunt");
//	s.setVersion("ver");
//
//	System.out.println(s.serialize());
//
//	GSConfOptionGSSource so = new GSConfOptionGSSource();
//
//	so.setValue(s);
//
//	so.setSource(s);
//
//	System.out.println(so.serialize());
//
//	GSConfOptionGSSource ds = new Deserializer().deserialize(
//		"{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionGSSource\",\"value\":{\"valueConcrete\":\"eu.essi_lab.model.GSSource\",\"uniqueIdentifier\":\"id\"},\"source\":{\"uniqueIdentifier\":\"id\"},\"mandatory\":false}",
//		// "{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionGSSource\",\"value\":{\"uniqueIdentifier\":\"id\"},\"source\":{\"uniqueIdentifier\":\"id\"},\"mandatory\":false}",
//		GSConfOptionGSSource.class);
//	// System.out.println("OK 1");
//
//	GSConfOptionGSSource ds2 = new Deserializer().deserialize(
//		"{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionGSSource\",\"source\":{\"uniqueIdentifier\":\"id\"},\"mandatory\":false}",
//		GSConfOptionGSSource.class);
//
//	// System.out.println("OK 2");
//    }
//}
