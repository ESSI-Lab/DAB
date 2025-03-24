package eu.essi_lab.cfga.resetandselect.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class ResetAndSelectTest {

    @Test
    public void test() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	AugmenterWorkerSetting setting = ConfigurationWrapper.getAugmenterWorkerSettings().get(0);

	Option<String> option = setting.getOption("sourcesOption", String.class).get();
	option.setSelectionMode(SelectionMode.MULTI);

	SettingUtils.loadValues(setting.getOption("sourcesOption", String.class).get(), Optional.empty());

	setting.setSelectedSources(Arrays.asList("DAB OAIPMH Service", "Atlas of the Cryosphere: Southern Hemisphere (WCS)", "GBIF"));

	test(setting);

	Setting resetAndSelect = SelectionUtils.resetAndSelect(setting, true);
	setting = SettingUtils.downCast(resetAndSelect, AugmenterWorkerSettingImpl.class);

	test(setting);
    }

    /**
     * @param setting
     */
    private void test(AugmenterWorkerSetting setting) {

	List<Integer> selectedIndexes = getSelectedIndexes(setting.getOption("sourcesOption", String.class).get()).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	Assert.assertEquals(3, selectedIndexes.size());

	Assert.assertEquals(Integer.valueOf(0), selectedIndexes.get(0));
	Assert.assertEquals(Integer.valueOf(1), selectedIndexes.get(1));
	Assert.assertEquals(Integer.valueOf(2), selectedIndexes.get(2));

	List<GSSource> selectedSources = setting.getSelectedSources().//
		stream().//
		sorted((s1, s2) -> s1.getLabel().compareTo(s2.getLabel())).//
		collect(Collectors.toList());

	Assert.assertEquals("Atlas of the Cryosphere: Southern Hemisphere (WCS)", selectedSources.get(0).getLabel());
	Assert.assertEquals("DAB OAIPMH Service", selectedSources.get(1).getLabel());
	Assert.assertEquals("GBIF", selectedSources.get(2).getLabel());

	List<String> selectedSourcesIds = setting.getSelectedSourcesIds().//
		stream().//
		sorted().//
		collect(Collectors.toList());

	Assert.assertEquals("atlasSouth", selectedSourcesIds.get(0));
	Assert.assertEquals("defaultGBIFMixedSource", selectedSourcesIds.get(1));
	Assert.assertEquals("defaultOAISource", selectedSourcesIds.get(2));

	List<String> selectedValues = setting.getOption("sourcesOption", String.class).get().//
		getSelectedValues().//
		stream().//
		sorted().//
		collect(Collectors.toList());

	Assert.assertEquals("Atlas of the Cryosphere: Southern Hemisphere (WCS)", selectedValues.get(0));
	Assert.assertEquals("DAB OAIPMH Service", selectedValues.get(1));
	Assert.assertEquals("GBIF", selectedValues.get(2));
    }

    /**
     * @return
     */
    private static List<Integer> getSelectedIndexes(Option<String> option) {

	if (option.getObject().has("selectedIndexes")) {

	    List<Integer> out = option.getObject().//
		    getJSONArray("selectedIndexes").//
		    toList().//
		    stream().//
		    map(v -> (Integer) v).//
		    collect(Collectors.toList());

	    return out;
	}

	return new ArrayList<>();
    }

}
