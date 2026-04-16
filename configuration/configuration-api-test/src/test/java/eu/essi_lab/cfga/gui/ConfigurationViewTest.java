//package eu.essi_lab.cfga.gui;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.cfga.Configuration;
//import eu.essi_lab.cfga.gs.DefaultConfiguration;
//
//public class ConfigurationViewTest {
//
//    /**
//     * 
//     */
//    @Test
//    public void tabsTest() {
//
//	@SuppressWarnings("serial")
//	ConfigurationView configView = new ConfigurationView() {
//
//	    @Override
//	    protected Configuration initConfiguration() {
//
//		try {
//		    return new DefaultConfiguration();
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//
//		return null;
//	    }
//	};
//
//	Assert.assertNotNull(configView.getTabs());
//
//	int componentCount = configView.getTabs().getComponentCount();
//
//	Assert.assertEquals(8, componentCount);
//    }
//}
