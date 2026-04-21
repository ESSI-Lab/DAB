package eu.essi_lab.cfga.gs.setting.gdcsourcesetting.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class GDCSourcesSettingTest {

    @Test
    public void test() throws IOException, Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	//
	// the GDCSourcesSetting in the configuration is clean, so since no source is selected per default, the
	// sub-setting with the available sources is empty
	//

	GDCSourcesSetting configGdcSourcesSetting = ConfigurationWrapper.getGDCSourceSetting();
	Assert.assertEquals(0, configGdcSourcesSetting.getSelectedSourcesIds().size());

	GDCSourcesSetting gdcSourcesSetting = new GDCSourcesSetting();

	//
	// creates a new instance, which is not clean so the sub-setting contains all
	// the sources provided by the configuration
	//

	List<Setting> settings = gdcSourcesSetting.getSettings();

	Assert.assertEquals(1, settings.size());

	List<String> selectedSourcesIds = gdcSourcesSetting.getSelectedSourcesIds();

	Assert.assertEquals(0, selectedSourcesIds.size());

	//
	// this setting holds the available sources settings
	//

	Setting setting = settings.get(0);
	List<Setting> availableSourcesSettings = setting.getSettings();
	List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	Assert.assertEquals(allSources.size(), availableSourcesSettings.size());

	//
	//
	//

	GSSource gsSource0 = ConfigurationWrapper.getHarvestedSources().get(0);
	GSSource gsSource1 = ConfigurationWrapper.getHarvestedSources().get(1);

	boolean gdcSource0 = gdcSourcesSetting.isGDCSource(gsSource0);
	boolean gdcSource1 = gdcSourcesSetting.isGDCSource(gsSource1);

	Assert.assertFalse(gdcSource0);
	Assert.assertFalse(gdcSource1);

	//
	// selects source 0 and source 1
	//

	gdcSourcesSetting.selectGDCSources(//
		Arrays.asList(//
			gsSource0.getUniqueIdentifier(), //
			gsSource1.getUniqueIdentifier()));

	gdcSource0 = gdcSourcesSetting.isGDCSource(gsSource0);
	gdcSource1 = gdcSourcesSetting.isGDCSource(gsSource1);

	Assert.assertTrue(gdcSource0);
	Assert.assertTrue(gdcSource1);

	//
	//
	//

	selectedSourcesIds = gdcSourcesSetting.getSelectedSourcesIds();

	Assert.assertEquals(2, selectedSourcesIds.size());

	//
	//
	//

	boolean deselected = gdcSourcesSetting.deselectSource(gsSource0.getUniqueIdentifier());
	Assert.assertTrue(deselected);

	Assert.assertFalse(gdcSourcesSetting.isGDCSource(gsSource0));
    }
}
