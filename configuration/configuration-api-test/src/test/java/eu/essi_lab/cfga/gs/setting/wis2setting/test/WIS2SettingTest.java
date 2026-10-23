package eu.essi_lab.cfga.gs.setting.wis2setting.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.DefaultConfiguration.SingletonSettingsId;
import eu.essi_lab.cfga.gs.setting.WIS2Setting;
import eu.essi_lab.model.GSSource;

/**
 * @author boldrini
 */
public class WIS2SettingTest {

    @Test
    public void defaultConfigurationTest() throws Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	Assert.assertTrue(defaultConfiguration.get(SingletonSettingsId.WIS2_SETTING.getLabel(), WIS2Setting.class).isPresent());

	WIS2Setting setting = ConfigurationWrapper.getWIS2Setting();

	Assert.assertTrue(setting.getCentreId().isEmpty());
	Assert.assertTrue(setting.getPublisher().isEmpty());
	Assert.assertTrue(setting.getGeneratedBy().isEmpty());
	Assert.assertTrue(setting.getHostname().isEmpty());
	Assert.assertTrue(setting.getViewId().isEmpty());
	Assert.assertTrue(setting.getTokenRequestContact().isEmpty());
	Assert.assertTrue(setting.getCoreSourcesIds().isEmpty());
    }

    @Test
    public void missingSettingTest() throws Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();
	defaultConfiguration.remove(SingletonSettingsId.WIS2_SETTING.getLabel());

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	// a configuration saved before the WIS2 setting existed provides an empty setting
	WIS2Setting setting = ConfigurationWrapper.getWIS2Setting();

	Assert.assertTrue(setting.getCentreId().isEmpty());
	Assert.assertTrue(setting.getCoreSourcesIds().isEmpty());
    }

    @Test
    public void optionsTest() {

	WIS2Setting setting = new WIS2Setting();

	setting.setCentreId(" it-cnr-itiam-whos-dab-test ");
	setting.setPublisher("CNR-ITIAm");
	setting.setGeneratedBy("WHOS DAB");
	setting.setHostname("https://wis2.whos-dab.eu");
	setting.setViewId("whos");
	setting.setTokenRequestContact("whos@wmo.int");

	Assert.assertEquals("it-cnr-itiam-whos-dab-test", setting.getCentreId().get());
	Assert.assertEquals("CNR-ITIAm", setting.getPublisher().get());
	Assert.assertEquals("WHOS DAB", setting.getGeneratedBy().get());
	Assert.assertEquals("https://wis2.whos-dab.eu", setting.getHostname().get());
	Assert.assertEquals("whos", setting.getViewId().get());
	Assert.assertEquals("whos@wmo.int", setting.getTokenRequestContact().get());

	// blank values are treated as missing
	setting.setCentreId("  ");
	Assert.assertTrue(setting.getCentreId().isEmpty());

	// the setting is restored from its JSON representation
	WIS2Setting restored = new WIS2Setting(setting.getObject().toString());
	Assert.assertEquals("https://wis2.whos-dab.eu", restored.getHostname().get());
    }

    @Test
    public void coreSourcesTest() throws Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	// a new instance holds all the sources of the configuration, none of them selected
	WIS2Setting setting = new WIS2Setting();

	GSSource source0 = ConfigurationWrapper.getHarvestedSources().get(0);
	GSSource source1 = ConfigurationWrapper.getHarvestedSources().get(1);

	Assert.assertFalse(setting.isCoreSource(source0));
	Assert.assertFalse(setting.isCoreSource(source1));

	setting.selectCoreSources(Arrays.asList(source0.getUniqueIdentifier()));

	Assert.assertTrue(setting.isCoreSource(source0));
	Assert.assertTrue(setting.isCoreSource(source0.getUniqueIdentifier()));
	Assert.assertFalse(setting.isCoreSource(source1));
	Assert.assertEquals(Arrays.asList(source0.getUniqueIdentifier()), setting.getCoreSourcesIds());

	Assert.assertFalse(setting.deselectSource(source1.getUniqueIdentifier()));
	Assert.assertTrue(setting.deselectSource(source0.getUniqueIdentifier()));

	Assert.assertFalse(setting.isCoreSource(source0));
	Assert.assertTrue(setting.getCoreSourcesIds().isEmpty());
    }
}
