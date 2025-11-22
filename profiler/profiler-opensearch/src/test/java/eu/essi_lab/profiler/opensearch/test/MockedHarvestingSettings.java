package eu.essi_lab.profiler.opensearch.test;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Mattia Santoro
 */
public class MockedHarvestingSettings extends HarvestingSetting {

    public MockedHarvestingSettings() {
    }

    public MockedHarvestingSettings(String object) {
	super(object);
    }

    @Override
    protected Setting initAugmentersSetting() {
	return new Setting();
    }

    @Override
    protected String getAugmentersSettingIdentifier() {
	return "";
    }

    @Override
    protected Setting initAccessorsSetting() {
	return new Setting();
    }

    @Override
    protected String getAccessorsSettingIdentifier() {
	return "";
    }

    @Override
    protected String initConfigurableType() {
	return "";
    }

}
