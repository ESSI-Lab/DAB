//package eu.essi_lab.model.configuration;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InOrder;
//import org.mockito.Mockito;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//import eu.essi_lab.model.configuration.composite.GSConfiguration;
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
//import eu.essi_lab.model.configuration.option.GSConfOptionString;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class GSConfigurationTest {
//
//    @Test
//    public void testOnOptionSetNoSubcomponent() throws GSException {
//
//	GSConfiguration conf = new GSConfiguration();
//
//	IGSConfigurableComposed mockSubComponent = Mockito.mock(IGSConfigurableComposed.class);
//
//	Mockito.when(mockSubComponent.getKey()).thenReturn("testsubkey");
//	conf.getConfigurableComponents().put("testsubkey", mockSubComponent);
//
//	GSConfOption<?> subComponentOption = Mockito.mock(GSConfOption.class);
//	conf.onOptionSet(subComponentOption);
//
//	//So far I test only that no stack overflow is triggered by conf.onOptionSet
//    }
//
//    @Test
//    public void serializeDeserialize() throws GSException, IOException {
//
//	GSConfiguration conf = new GSConfiguration();
//	conf.setKey("key");
//
//	String serialized = conf.serialize();
//
//	GSConfiguration deserialized = new Deserializer().deserialize(serialized, GSConfiguration.class);
//
//	Assert.assertEquals(0, deserialized.getSupportedOptions().size());
//
//	Assert.assertEquals("key", deserialized.getKey());
//
//    }
//
//    @Test
//    public void testWithOneSupportedOption() throws JsonProcessingException, IOException, GSException {
//
//	GSConfiguration conf = new GSConfiguration();
//
//	GSConfOptionString opt = new GSConfOptionString();
//
//	opt.setKey("key");
//	opt.setMandatory(true);
//
//	conf.getSupportedOptions().put(opt.getKey(), opt);
//
//	Assert.assertEquals(1, conf.getSupportedOptions().size());
//
//	Assert.assertNull(conf.read("key").getValue());
//
//	String serialized = conf.serialize();
//
//	Assert.assertNull(new Deserializer().deserialize(serialized, GSConfiguration.class).read("key").getValue());
//
//	Assert.assertEquals(1, new Deserializer().deserialize(serialized, GSConfiguration.class).getSupportedOptions().size());
//
//    }
//
//    @Test
//    public void testWithOneOption() throws JsonProcessingException, IOException, GSException {
//
//	GSConfiguration conf = new GSConfiguration();
//
//	GSConfOptionString opt = new GSConfOptionString();
//
//	opt.setKey("key");
//	opt.setMandatory(true);
//
//	conf.getSupportedOptions().put(opt.getKey(), opt);
//
//	GSConfOptionString optset = new GSConfOptionString();
//	optset.setValue("value");
//	optset.setKey("key");
//
//	//	conf.setOption(conf.getKey(), optset);
//	conf.setOption(optset);
//
//	Assert.assertEquals(1, conf.getSupportedOptions().size());
//
//	Assert.assertNotNull(conf.read("key"));
//	Assert.assertEquals("value", conf.read("key").getValue());
//
//	Assert.assertNotNull(new Deserializer().deserialize(conf.serialize(), GSConfiguration.class).read("key"));
//	Assert.assertEquals("value", new Deserializer().deserialize(conf.serialize(), GSConfiguration.class).read("key").getValue());
//
//	Assert.assertEquals(1, new Deserializer().deserialize(conf.serialize(), GSConfiguration.class).getSupportedOptions().size());
//
//    }
//
//    @Test
//    public void serializeDeserializeWithOptions() throws GSException, IOException {
//
//	GSConfiguration conf = new GSConfiguration();
//	conf.setKey("confkey");
//
//	GSConfOptionBoolean bool = new GSConfOptionBoolean();
//
//	bool.setMandatory(true);
//
//	bool.setKey("boolkey");
//
//	conf.getSupportedOptions().put("boolkey", bool);
//
//	String serialized = conf.serialize();
//
//	GSConfiguration deserialized = new Deserializer().deserialize(serialized, GSConfiguration.class);
//
//	Assert.assertEquals(1, deserialized.getSupportedOptions().size());
//
//	Assert.assertEquals("confkey", deserialized.getKey());
//
//    }
//
//    @Test
//    public void testOnFlushInvokation() throws GSException {
//
//	GSConfiguration configuration = new GSConfiguration();
//
//	IGSConfigurableComposed composed1 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed1Children = new HashMap<>();
//
//	IGSConfigurableComposed composed11 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed11Children = new HashMap<>();
//
//	IGSConfigurable composed111 = Mockito.mock(IGSConfigurable.class);
//	composed11Children.put("composed111", composed111);
//
//	IGSConfigurable composed112 = Mockito.mock(IGSConfigurable.class);
//	composed11Children.put("composed112", composed112);
//
//	Mockito.when(composed11.getConfigurableComponents()).thenReturn(composed11Children);
//
//	composed1Children.put("composed11", composed11);
//
//	IGSConfigurable composed12 = Mockito.mock(IGSConfigurable.class);
//	composed1Children.put("composed12", composed12);
//
//	Mockito.when(composed1.getConfigurableComponents()).thenReturn(composed1Children);
//
//	configuration.getConfigurableComponents().put("composed1", composed1);
//
//	IGSConfigurableComposed composed2 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed2Child = new HashMap<>();
//
//	IGSConfigurable composed21 = Mockito.mock(IGSConfigurable.class);
//	composed2Child.put("composed21", composed21);
//
//	Mockito.when(composed2.getConfigurableComponents()).thenReturn(composed2Child);
//
//	configuration.getConfigurableComponents().put("composed2", composed2);
//
//	IGSConfigurableComposed composed3 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed3Children = new HashMap<>();
//
//	IGSConfigurableComposed composed31 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed31Children = new HashMap<>();
//
//	Mockito.when(composed31.getConfigurableComponents()).thenReturn(composed31Children);
//
//	composed3Children.put("composed31", composed31);
//
//	Mockito.when(composed3.getConfigurableComponents()).thenReturn(composed3Children);
//
//	configuration.getConfigurableComponents().put("composed3", composed3);
//
//	Mockito.when(composed1.toString()).thenReturn("composed1");
//	Mockito.when(composed11.toString()).thenReturn("composed11");
//	Mockito.when(composed111.toString()).thenReturn("composed111");
//	Mockito.when(composed112.toString()).thenReturn("composed112");
//	Mockito.when(composed12.toString()).thenReturn("composed12");
//	Mockito.when(composed2.toString()).thenReturn("composed2");
//	Mockito.when(composed21.toString()).thenReturn("composed21");
//	Mockito.when(composed3.toString()).thenReturn("composed3");
//	Mockito.when(composed31.toString()).thenReturn("composed31");
//
//	configuration.onFlush();
//
//	InOrder inorder1 = Mockito.inOrder(composed1, composed11, composed111, composed112, composed12);
//
//	InOrder inorder2 = Mockito.inOrder(composed2, composed21);
//	InOrder inorder3 = Mockito.inOrder(composed3, composed31);
//
//	inorder1.verify(composed1).onFlush();
//
//	inorder1.verify(composed11).onFlush();
//
//	inorder1.verify(composed111).onFlush();
//
//	inorder1.verify(composed112).onFlush();
//
//	inorder1.verify(composed12).onFlush();
//
//	inorder2.verify(composed2).onFlush();
//
//	inorder2.verify(composed21).onFlush();
//
//	inorder3.verify(composed3).onFlush();
//
//	inorder3.verify(composed31).onFlush();
//
//    }
//
//
//
//    @Test
//    public void testOnStartupInvokation() throws GSException {
//
//	GSConfiguration configuration = new GSConfiguration();
//
//	IGSConfigurableComposed composed1 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed1Children = new HashMap<>();
//
//	IGSConfigurableComposed composed11 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed11Children = new HashMap<>();
//
//	IGSConfigurable composed111 = Mockito.mock(IGSConfigurable.class);
//	composed11Children.put("composed111", composed111);
//
//	IGSConfigurable composed112 = Mockito.mock(IGSConfigurable.class);
//	composed11Children.put("composed112", composed112);
//
//	Mockito.when(composed11.getConfigurableComponents()).thenReturn(composed11Children);
//
//	composed1Children.put("composed11", composed11);
//
//	IGSConfigurable composed12 = Mockito.mock(IGSConfigurable.class);
//	composed1Children.put("composed12", composed12);
//
//	Mockito.when(composed1.getConfigurableComponents()).thenReturn(composed1Children);
//
//	configuration.getConfigurableComponents().put("composed1", composed1);
//
//	IGSConfigurableComposed composed2 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed2Child = new HashMap<>();
//
//	IGSConfigurable composed21 = Mockito.mock(IGSConfigurable.class);
//	composed2Child.put("composed21", composed21);
//
//	Mockito.when(composed2.getConfigurableComponents()).thenReturn(composed2Child);
//
//	configuration.getConfigurableComponents().put("composed2", composed2);
//
//	IGSConfigurableComposed composed3 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed3Children = new HashMap<>();
//
//	IGSConfigurableComposed composed31 = Mockito.mock(IGSConfigurableComposed.class);
//
//	Map<String, IGSConfigurable> composed31Children = new HashMap<>();
//
//	Mockito.when(composed31.getConfigurableComponents()).thenReturn(composed31Children);
//
//	composed3Children.put("composed31", composed31);
//
//	Mockito.when(composed3.getConfigurableComponents()).thenReturn(composed3Children);
//
//	configuration.getConfigurableComponents().put("composed3", composed3);
//
//	Mockito.when(composed1.toString()).thenReturn("composed1");
//	Mockito.when(composed11.toString()).thenReturn("composed11");
//	Mockito.when(composed111.toString()).thenReturn("composed111");
//	Mockito.when(composed112.toString()).thenReturn("composed112");
//	Mockito.when(composed12.toString()).thenReturn("composed12");
//	Mockito.when(composed2.toString()).thenReturn("composed2");
//	Mockito.when(composed21.toString()).thenReturn("composed21");
//	Mockito.when(composed3.toString()).thenReturn("composed3");
//	Mockito.when(composed31.toString()).thenReturn("composed31");
//
//	configuration.onStartUp();
//
//	InOrder inorder1 = Mockito.inOrder(composed1, composed11, composed111, composed112, composed12);
//
//	InOrder inorder2 = Mockito.inOrder(composed2, composed21);
//	InOrder inorder3 = Mockito.inOrder(composed3, composed31);
//
//	inorder1.verify(composed1).onStartUp();
//
//	inorder1.verify(composed11).onStartUp();
//
//	inorder1.verify(composed111).onStartUp();
//
//	inorder1.verify(composed112).onStartUp();
//
//	inorder1.verify(composed12).onStartUp();
//
//	inorder2.verify(composed2).onStartUp();
//
//	inorder2.verify(composed21).onStartUp();
//
//	inorder3.verify(composed3).onStartUp();
//
//	inorder3.verify(composed31).onStartUp();
//
//    }
//
//
//
//}
