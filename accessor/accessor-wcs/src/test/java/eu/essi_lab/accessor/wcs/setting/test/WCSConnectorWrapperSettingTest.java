package eu.essi_lab.accessor.wcs.setting.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs.WCSConnectorWrapperSetting;

/**
 * @author Fabrizio
 */
public class WCSConnectorWrapperSettingTest {

    @Test
    public void test() {

	WCSConnectorWrapperSetting setting = new WCSConnectorWrapperSetting();

	List<String> connectorTypes = setting.getConnectorTypes();

	connectorTypes.forEach(type -> {

	    setting.selectConnectorType(type);

	    WCSConnector selectedConnector = setting.getSelectedConnector();

	    String name = selectedConnector.getSetting().getName();
	    
	    System.out.println(name);

	    Assert.assertEquals(name, type);
	});
    }

}
