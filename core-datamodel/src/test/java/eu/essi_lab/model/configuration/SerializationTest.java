//package eu.essi_lab.model.configuration;
//
//import java.io.IOException;
//import java.io.StringWriter;
//import java.io.Writer;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import eu.essi_lab.model.configuration.GSInitConfigurationResponse.Message;
//
//public class SerializationTest {
//
//    @Test
//    public void serializeGSInitConfigurationResponse() throws IOException {
//
//	Writer jsonWriter = new StringWriter();
//
//	JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
//
//	GSInitConfigurationResponse obj = new GSInitConfigurationResponse();
//
//	obj.setMessage(Message.INVALID_URL);
//
//	new ObjectMapper().writer().writeValue(jsonGenerator, obj);
//
//	jsonGenerator.flush();
//
//	String string = jsonWriter.toString();
//
//	Assert.assertTrue("No \"result\" expected", !string.contains("\"result\""));
//
//	Assert.assertTrue("Expected " + Message.INVALID_URL.toString(), string.contains(Message.INVALID_URL.toString()));
//
//    }
//}
