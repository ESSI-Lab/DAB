package eu.essi_lab.cfga.adk.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.arcgis.AGOLAccessor;
import eu.essi_lab.accessor.fdsn.FDSNAccessor;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;

/**
 * @author Fabrizio
 */
public class DistributionSettingTest {

    @Test
    public void test() {

	DistributionSetting distributionSetting = new DistributionSetting();

	initTest(distributionSetting);
	initTest(new DistributionSetting(distributionSetting.getObject()));
	initTest(new DistributionSetting(distributionSetting.getObject().toString()));

	distributionSetting.getAccessorsSetting().select(s -> s.getName().equals(FDSNAccessor.NAME));

	afterSelectionTest(distributionSetting);
	afterSelectionTest(new DistributionSetting(distributionSetting.getObject()));
	afterSelectionTest(new DistributionSetting(distributionSetting.getObject().toString()));

	distributionSetting.afterClean();

	afterCleanTest(distributionSetting);
	afterCleanTest(new DistributionSetting(distributionSetting.getObject()));
	afterCleanTest(new DistributionSetting(distributionSetting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void initTest(DistributionSetting setting) {

	AccessorSetting selectedAccessorSetting = setting.getSelectedAccessorSetting();

	Assert.assertNotNull(selectedAccessorSetting);

	Assert.assertEquals(AGOLAccessor.NAME, selectedAccessorSetting.getName());

    }

    /**
     * @param setting
     */
    private void afterSelectionTest(DistributionSetting setting) {

	AccessorSetting selectedAccessorSetting = setting.getSelectedAccessorSetting();

	Assert.assertNotNull(selectedAccessorSetting);

	Assert.assertEquals(FDSNAccessor.NAME, selectedAccessorSetting.getName());

    }

    private void afterCleanTest(DistributionSetting setting) {

	AccessorSetting selectedAccessorSetting = setting.getSelectedAccessorSetting();

	Assert.assertNotNull(selectedAccessorSetting);

	Assert.assertEquals(FDSNAccessor.NAME, selectedAccessorSetting.getName());
    }
}
