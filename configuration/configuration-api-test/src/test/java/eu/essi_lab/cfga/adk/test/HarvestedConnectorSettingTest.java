/**
 * 
 */
package eu.essi_lab.cfga.adk.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;

/**
 * @author Fabrizio
 */
public class HarvestedConnectorSettingTest {

    @Test
    public void test() {

	HarvestedConnectorSetting setting = HarvestedConnectorSetting.create("name", "test");

	//
	//
	//

	Optional<Integer> maxRecords = setting.getMaxRecords();
	Assert.assertFalse(maxRecords.isPresent());

	int selectedPageSize = setting.getPageSize();
	Assert.assertEquals(50, selectedPageSize);

	//
	//
	//

	setting.setMaxRecords(100);
	setting.setPageSize(300);

	maxRecords = setting.getMaxRecords();
	Assert.assertEquals(Integer.valueOf(100), maxRecords.get());

	selectedPageSize = setting.getPageSize();
	Assert.assertEquals(300, selectedPageSize);

	//
	//
	//

	setting.setMaxRecords(0);
	setting.setPageSize(500);

	maxRecords = setting.getMaxRecords();
	Assert.assertFalse(maxRecords.isPresent());

	selectedPageSize = setting.getPageSize();
	Assert.assertEquals(500, selectedPageSize);
    }
}
