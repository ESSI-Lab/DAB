package eu.essi_lab.accessor.csw;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class CSWConnectorWrapperSettingTest {

    @Test
    public void test() {

	CSWConnectorWrapperSetting setting = new CSWConnectorWrapperSetting();

	List<String> connectorTypes = setting.getConnectorTypes();

	connectorTypes.forEach(type -> {

	    setting.selectConnectorType(type);

	    CSWConnector selectedConnector = setting.getSelectedConnector();

	    String name = selectedConnector.getSetting().getName();
	    
	    System.out.println(name);

	    Assert.assertEquals(name, type);
	});
    }

}
