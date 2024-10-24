//package eu.essi_lab.model.configuration.option;
//
//import java.io.IOException;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import eu.essi_lab.model.exceptions.GSException;
//
//public class GSConfOptionSerializationDeserializationTest {
//
//    private ObjectMapper objectMapper;
//    private String optionAsString;
//    private GSConfOptionBoolean optionAsBean;
//
//    @Before
//    public void init() throws GSException {
//	objectMapper = new ObjectMapper();
//	optionAsString = "{\"concrete\":\"eu.essi_lab.model.configuration.option.GSConfOptionBoolean\",\"key\":\"key\","
//		+ "\"mandatory\":true,\"value\":true,\"allowedValues\":[true,false],\"type\":\"java.lang.Boolean\"}";
//	optionAsBean = new GSConfOptionBoolean();
//	optionAsBean.setKey("key");
//	optionAsBean.setValue(true);
//	optionAsBean.setMandatory(true);
//
//    }
//
//    @Test
//    public void deserializeBooleanOption() throws IOException {
//	GSConfOptionBoolean expected = optionAsBean;
//	GSConfOptionBoolean actual = objectMapper.readValue(optionAsString, GSConfOptionBoolean.class);
//	Assert.assertEquals(expected, actual);
//
//    }
//
//    @Test
//    public void serializeBooleanOption() throws JsonProcessingException {
//	String expected = optionAsString;
//	String actual = objectMapper.writeValueAsString(optionAsBean);
//	Assert.assertEquals(expected, actual);
//    }
//}
