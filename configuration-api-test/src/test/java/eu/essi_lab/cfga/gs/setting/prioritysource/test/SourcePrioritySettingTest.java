package eu.essi_lab.cfga.gs.setting.prioritysource.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class SourcePrioritySettingTest {

    @Test
    public void test() throws IOException, Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	SourcePrioritySetting sourcePrioritySetting = new SourcePrioritySetting();

	//
	//
	//

	List<Setting> settings = sourcePrioritySetting.getSettings();

	Assert.assertEquals(1, settings.size());

	List<String> selectedSourcesIds = sourcePrioritySetting.getSelectedSourcesIds();

	Assert.assertEquals(0, selectedSourcesIds.size());

	//
	// this setting holds the available sources settings (only harvested and mixed)
	//

	Setting setting = settings.get(0);
	List<Setting> availableSourcesSettings = setting.getSettings();
	List<GSSource> allSources = ConfigurationWrapper.getHarvestedAndMixedSources();
	Assert.assertEquals(allSources.size(), availableSourcesSettings.size());

	//
	//
	//

	GSSource gsSource0 = ConfigurationWrapper.getHarvestedSources().get(0);
	GSSource gsSource1 = ConfigurationWrapper.getHarvestedSources().get(1);

	boolean gdcSource0 = sourcePrioritySetting.isPrioritySource(gsSource0);
	boolean gdcSource1 = sourcePrioritySetting.isPrioritySource(gsSource1);

	Assert.assertFalse(gdcSource0);
	Assert.assertFalse(gdcSource1);

	//
	//
	//

	sourcePrioritySetting.selectPrioritySources(//
		Arrays.asList(//
			gsSource0.getUniqueIdentifier(), //
			gsSource1.getUniqueIdentifier()));

	gdcSource0 = sourcePrioritySetting.isPrioritySource(gsSource0);
	gdcSource1 = sourcePrioritySetting.isPrioritySource(gsSource1);

	Assert.assertTrue(gdcSource0);
	Assert.assertTrue(gdcSource1);

	//
	//
	//

	selectedSourcesIds = sourcePrioritySetting.getSelectedSourcesIds();

	Assert.assertEquals(2, selectedSourcesIds.size());

	//
	//
	//

	boolean deselected = sourcePrioritySetting.deselectSource(gsSource0.getUniqueIdentifier());
	Assert.assertTrue(deselected);

	Assert.assertFalse(sourcePrioritySetting.isPrioritySource(gsSource0));
    }
}
