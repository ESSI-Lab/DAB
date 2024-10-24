//package eu.essi_lab.model.configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class AbstractGSConfigurationTest {
//
//    private boolean onOptionSet = false;
//
//    @Test
//    public void testSetUnsupportedOption() throws GSException {
//
//	AbstractGSconfigurable conf = new AbstractGSconfigurable() {
//
//	    @Override
//	    public Map<String, GSConfOption<?>> getSupportedOptions() {
//		return new HashMap<String, GSConfOption<?>>();
//	    }
//
//	    @Override
//	    public void onOptionSet(GSConfOption<?> opt) throws GSException {
//		onOptionSetCalled();
//	    }
//
//	    @Override
//	    public void onFlush() throws GSException {
//
//	    }
//
//	};
//
//	GSConfOption<?> opt = Mockito.mock(GSConfOption.class);
//	Mockito.when(opt.getKey()).thenReturn("optKey");
//
//	Assert.assertEquals(false, conf.setOption(opt));
//	//	Assert.assertEquals(true, onOptionSet);
//
//    }
//
//    private void onOptionSetCalled() {
//	onOptionSet = true;
//    }
//}
