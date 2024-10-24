//package eu.essi_lab.model.configuration.option;
//
//import java.text.ParseException;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class GSConfOptionDateTest {
//
//    @Test
//    public void serializeDeserialize() throws GSException, ParseException {
//	GSConfOptionDate d = new GSConfOptionDate();
//
//	String now = ISO8601DateTimeUtils.getISO8601DateTime();
//
//	System.out.println(now);
//	d.setValue(ISO8601DateTimeUtils.parseISO8601(now));
//
//	System.out.println(d.serialize());
//
//	GSConfOptionDate deserialized = new Deserializer().deserialize(d.serialize(), GSConfOptionDate.class);
//
//	System.out.println(deserialized.getValue());
//
//	System.out.println(now);
//
//	Assert.assertEquals(0, ISO8601DateTimeUtils.parseISO8601(now).getTime() - deserialized.getValue().getTime());
//
//    }
//
//    @Test
//    public void deserializeJSON() throws GSException, ParseException {
//
//	String now = ISO8601DateTimeUtils.getISO8601DateTime();
//
//	System.out.println(now);
//
//	String json = " {\"concrete\": \"eu.essi_lab.model.configuration.option.GSConfOptionDate\",\"key\": \"START_DATE_KEY\","
//		+ "\"mandatory\": false,\"value\": \"" + now + "\",\"label\": \"Start Date\",\"type\": \"java.util.Date\"}";
//
//	GSConfOptionDate deserialized = new Deserializer().deserialize(json, GSConfOptionDate.class);
//
//	System.out.println(deserialized.getValue());
//
//	Assert.assertEquals(0, ISO8601DateTimeUtils.parseISO8601(now).getTime() - deserialized.getValue().getTime());
//    }
//
//}