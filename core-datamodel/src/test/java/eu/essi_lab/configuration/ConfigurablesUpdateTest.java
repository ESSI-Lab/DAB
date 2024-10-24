//package eu.essi_lab.configuration;
//
//import java.io.IOException;
//import java.io.StringWriter;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.model.configuration.Deserializer;
//import eu.essi_lab.model.configuration.IGSConfigurable;
//import eu.essi_lab.model.configuration.IGSConfigurableComposed;
//import eu.essi_lab.model.configuration.composite.GSConfiguration;
//import eu.essi_lab.model.configuration.option.GSConfOptionString;
//import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class ConfigurablesUpdateTest {
//
//    @Test
//    public void test() throws GSException, IOException {
//
//	StringWriter writer = new StringWriter();
//	IOUtils.copy(getClass().getClassLoader().getResourceAsStream("oldconf.json"), writer, StandardCharsets.UTF_8);
//	String confString = writer.toString();
//
//	GSConfiguration oldconf = new Deserializer().deserialize(confString, GSConfiguration.class);
//
//	Assert.assertEquals("value1", ((GSConfOptionString) oldconf.getConfigurableComponents().get("comp1").getSupportedOptions().get(
//		"op1")).getValue());
//
//	Assert.assertNotNull("Expected to find new supported option op2", oldconf.getConfigurableComponents().get("comp1")
//		.getSupportedOptions().get("op2"));
//
//    }
//
//    @Test
//    public void test2() throws GSException, IOException {
//
//	StringWriter writer = new StringWriter();
//	IOUtils.copy(getClass().getClassLoader().getResourceAsStream("oldconfsub.json"), writer, StandardCharsets.UTF_8);
//	String confString = writer.toString();
//
//	GSConfiguration oldconf = new Deserializer().deserialize(confString, GSConfiguration.class);
//
//	GSConfOptionSubcomponent op = (GSConfOptionSubcomponent) oldconf.getConfigurableComponents().get("comp1").getSupportedOptions().get(
//		"op1");
//
//	Assert.assertEquals(2, op.getAllowedValues().size());
//
//    }
//
//    @Test
//    public void test3() throws GSException, IOException {
//
//	StringWriter writer = new StringWriter();
//	IOUtils.copy(getClass().getClassLoader().getResourceAsStream("oldconfsub2.json"), writer,StandardCharsets.UTF_8);
//	String confString = writer.toString();
//
//	GSConfiguration oldconf = new Deserializer().deserialize(confString, GSConfiguration.class);
//
//	Map<String, IGSConfigurable> components = ((IGSConfigurableComposed) oldconf.getConfigurableComponents().get("comp1"))
//		.getConfigurableComponents();
//
//	Assert.assertEquals(2, components.values().size());
//
//	Assert.assertEquals("val1", ((GSConfOptionString) components.get("mysub1").getSupportedOptions().get("op1")).getValue());
//
//    }
//}
