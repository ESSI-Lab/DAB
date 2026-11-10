package eu.essi_lab.cfga.gs.setting.wis2setting.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.MQTTBrokerSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.cfga.gs.setting.WIS2Setting;
import eu.essi_lab.cfga.gs.setting.WIS2Setting.DataNotificationsMode;
import eu.essi_lab.lib.whos.WIS2DataPolicy;
import eu.essi_lab.lib.whos.WIS2HydrologyTopic;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Tests the WIS2 publication options resolved by {@link WISUtils}: WIS2 setting, then legacy key-value options, then
 * defaults
 *
 * @author boldrini
 */
public class WISUtilsSettingsTest {

    private DefaultConfiguration configuration;

    @Before
    public void before() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void defaultsTest() {

	Assert.assertEquals(WISUtils.DEFAULT_CENTRE_ID, WISUtils.getCentreId());
	Assert.assertEquals(WISUtils.DEFAULT_PUBLISHER, WISUtils.getPublisher());
	Assert.assertEquals(WISUtils.DEFAULT_GENERATED_BY, WISUtils.getGeneratedBy());
	Assert.assertEquals(WISUtils.DEFAULT_HOSTNAME, WISUtils.getHostname());
	Assert.assertEquals(WISUtils.DEFAULT_TOKEN_REQUEST_CONTACT, WISUtils.getTokenRequestContact());
	Assert.assertTrue(WISUtils.getViewId().isEmpty());
    }

    @Test
    public void legacyKeyValueOptionsTest() {

	SystemSetting systemSetting = ConfigurationWrapper.getSystemSettings();
	systemSetting.putKeyValue(KeyValueOptionKeys.WIS_CENTRE_ID.getLabel(), "legacy-centre");
	systemSetting.putKeyValue(KeyValueOptionKeys.WIS_HOSTNAME.getLabel(), "https://legacy.example");
	configuration.replace(systemSetting);

	Assert.assertEquals("legacy-centre", WISUtils.getCentreId());
	Assert.assertEquals("https://legacy.example", WISUtils.getHostname());

	// the WIS2 setting options have precedence over the legacy key-value options
	WIS2Setting wis2Setting = ConfigurationWrapper.getWIS2Setting();
	wis2Setting.setCentreId("it-cnr-itiam-whos-dab-test");
	configuration.replace(wis2Setting);

	Assert.assertEquals("it-cnr-itiam-whos-dab-test", WISUtils.getCentreId());
	Assert.assertEquals("https://legacy.example", WISUtils.getHostname());
    }

    @Test
    public void dataPolicyTest() {

	String source0 = ConfigurationWrapper.getHarvestedSources().get(0).getUniqueIdentifier();
	String source1 = ConfigurationWrapper.getHarvestedSources().get(1).getUniqueIdentifier();

	Assert.assertEquals(WIS2DataPolicy.RECOMMENDED, WISUtils.getDataPolicy(source0));
	Assert.assertFalse(WISUtils.isCoreDataPublished(source0));

	// a new instance holds the sources of the configuration, so that they can be selected
	WIS2Setting wis2Setting = new WIS2Setting();
	wis2Setting.setIdentifier(DefaultConfiguration.SingletonSettingsId.WIS2_SETTING.getLabel());
	wis2Setting.selectCoreSources(Arrays.asList(source0));
	configuration.replace(wis2Setting);

	// disabled: core sources are neither declared nor served
	Assert.assertEquals(WIS2DataPolicy.RECOMMENDED, WISUtils.getDataPolicy(source0));
	Assert.assertFalse(WISUtils.isCoreDataPublished(source0));

	// private: core data are served and notified, but the records are unchanged
	setMode(wis2Setting, DataNotificationsMode.PRIVATE);
	Assert.assertEquals(WIS2DataPolicy.RECOMMENDED, WISUtils.getDataPolicy(source0));
	Assert.assertTrue(WISUtils.isCoreDataPublished(source0));
	Assert.assertFalse(WISUtils.isCoreDataPublished(source1));

	// experimental: the records declare the core data policy
	setMode(wis2Setting, DataNotificationsMode.EXPERIMENTAL);
	Assert.assertEquals(WIS2DataPolicy.CORE, WISUtils.getDataPolicy(source0));
	Assert.assertEquals(WIS2DataPolicy.RECOMMENDED, WISUtils.getDataPolicy(source1));
    }

    @Test
    public void topicsTest() {

	WIS2Setting wis2Setting = ConfigurationWrapper.getWIS2Setting();
	wis2Setting.setCentreId("it-cnr-itiam-whos-dab-test");

	String path = "/data/core/hydrology/surface-based-observations/water-quantity/stream";
	String experimentalPath = "/data/core/hydrology/experimental/surface-based-observations/water-quantity/stream";

	setMode(wis2Setting, DataNotificationsMode.DISABLED);
	Assert.assertTrue(WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM).isEmpty());
	Assert.assertTrue(WISUtils.getPublicationTopic(WIS2HydrologyTopic.STREAM).isEmpty());

	for (DataNotificationsMode mode : new DataNotificationsMode[] { DataNotificationsMode.DRY_RUN, DataNotificationsMode.PRIVATE }) {

	    setMode(wis2Setting, mode);
	    Assert.assertTrue(WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM).isEmpty());
	    Assert.assertEquals("test/a/wis2/it-cnr-itiam-whos-dab-test" + path,
		    WISUtils.getPublicationTopic(WIS2HydrologyTopic.STREAM).get());
	}

	setMode(wis2Setting, DataNotificationsMode.EXPERIMENTAL);
	Assert.assertEquals("origin/a/wis2/it-cnr-itiam-whos-dab-test" + experimentalPath,
		WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM).get());
	Assert.assertEquals(WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM), WISUtils.getPublicationTopic(WIS2HydrologyTopic.STREAM));

	setMode(wis2Setting, DataNotificationsMode.OPERATIONAL);
	Assert.assertEquals("origin/a/wis2/it-cnr-itiam-whos-dab-test" + path, WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM).get());
	Assert.assertEquals(WISUtils.getAdvertisedTopic(WIS2HydrologyTopic.STREAM), WISUtils.getPublicationTopic(WIS2HydrologyTopic.STREAM));
    }

    /**
     * @param wis2Setting
     * @param mode
     */
    private void setMode(WIS2Setting wis2Setting, DataNotificationsMode mode) {

	wis2Setting.setDataNotificationsMode(mode);
	configuration.replace(wis2Setting);
    }

    @Test
    public void brokerUrlTest() {

	Assert.assertTrue(WISUtils.getBrokerUrl().isEmpty());

	SystemSetting systemSetting = ConfigurationWrapper.getSystemSettings();
	MQTTBrokerSetting broker = systemSetting.getSetting("wis2MqttBroker", MQTTBrokerSetting.class).get();
	broker.setEnabled(true);
	broker.setHost("wis2.whos-dab.eu");
	broker.setPort(8883);
	broker.setUser("user");
	broker.setPassword("secret");
	configuration.replace(systemSetting);

	// no credentials in the public URL
	Assert.assertEquals("mqtts://wis2.whos-dab.eu:8883", WISUtils.getBrokerUrl().get());
    }
}
