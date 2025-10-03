package eu.essi_lab.cfga.similarity.test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.SimilarityMethod;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Property;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * 
 */
public class ConfigurationSimilarityTest {

    /**
     * This modifier modifies all the properties of a SystemSetting.<br>
     * These properties are excluded per default from the check,
     * so in a normal case, we expect a successful check as in {@link #systemSettingConsumerTest()}.<br>
     * By including one or more property in the check, we expect a failing check which involves that property
     * 
     * @author Fabrizio
     */
    private class SystemSettingConsumer implements Consumer<Setting> {

	@Override
	public void accept(Setting s) {

	    if (s.getSettingClass().equals(SystemSetting.class)) {

		s.setAfterCleanFunction(new AfterCleanFunction() {

		    @Override
		    public void afterClean(Setting setting) {
		    }
		});

		s.setCanBeCleaned(!s.canBeCleaned());
		s.setCanBeDisabled(!s.canBeDisabled());
		s.setCanBeRemoved(!s.canBeRemoved());
		s.enableCompactMode(!s.isCompactModeEnabled());
		s.setConfigurableType("type");
		s.setDescription("new desc");
		s.setEditable(!s.isEditable());
		s.setEnabled(!s.isEnabled());
		s.getObject().remove("extensionClass");
		s.enableFoldedMode(!s.isFoldedModeEnabled());
		s.getObject().remove("settingName");
		s.setSelected(!s.isSelected());
		s.setSelectionMode(SelectionMode.MULTI);
		s.setShowHeader(!s.isShowHeaderSet());
		s.setValidator(new Validator() {

		    @Override
		    public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {
			return null;
		    }
		});

		s.setVisible(!s.isVisible());
	    }
	}
    }

    @Test
    public void defaultTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	SimilarityMethod method = new SimilarityMethod();

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }

    @Test
    public void optionAddedTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	Class<SystemSetting> clazz = SystemSetting.class;

	SimilarityMethod method = new SimilarityMethod();

	method.setModifier(s -> {

	    if (s.getSettingClass().equals(clazz)) {

		//
		// option added
		//

		Option<String> option = new Option<String>(String.class);
		option.setKey("optionKey");
		s.addOption(option);
	    }
	});

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	Assert.assertEquals(1, response.getSettings().size());

	Assert.assertEquals(clazz, response.getSettings().get(0).getSettingClass());
    }

    @Test
    public void optionRemovedTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	Class<SystemSetting> clazz = SystemSetting.class;

	SimilarityMethod method = new SimilarityMethod();

	method.setModifier(s -> {
	    if (s.getSettingClass().equals(clazz)) {

		//
		// option removed
		//

		s.removeOption("emailSetting");
	    }
	});

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	Assert.assertEquals(1, response.getSettings().size());

	Assert.assertEquals(clazz, response.getSettings().get(0).getSettingClass());

    }

    @Test
    public void settingAddedTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	Class<SystemSetting> clazz = SystemSetting.class;

	SimilarityMethod method = new SimilarityMethod();

	method.setModifier(s -> {
	    if (s.getSettingClass().equals(clazz)) {

		//
		// setting added
		//

		Setting setting = new Setting();
		s.addSetting(setting);
	    }
	});

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	Assert.assertEquals(1, response.getSettings().size());

	Assert.assertEquals(clazz, response.getSettings().get(0).getSettingClass());
    }

    @Test
    public void settingRemovedTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	Class<SystemSetting> clazz = SystemSetting.class;

	SimilarityMethod method = new SimilarityMethod();

	method.setModifier(s -> {

	    if (s.getSettingClass().equals(clazz)) {

		//
		// setting removed
		//

		s.removeSetting("emailSetting");
	    }
	});

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	Assert.assertEquals(1, response.getSettings().size());

	Assert.assertEquals(clazz, response.getSettings().get(0).getSettingClass());
    }

    @Test
    public void systemSettingConsumerTest() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	SimilarityMethod method = new SimilarityMethod();

	method.setModifier(new SystemSettingConsumer());

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);

	Assert.assertEquals(0, response.getSettings().size());
    }

    @Test
    public void inclusionTest1() {

	inclusionTest(Setting.AFTER_CLEAN_FUNCTION, 1);
    }

    @Test
    public void inclusionTest2() {

	inclusionTest(Setting.CAN_BE_CLEANED, 1);
    }

    @Test
    public void inclusionTest3() {

	inclusionTest(Setting.CAN_BE_DISABLED, 1);
    }

    @Test
    public void inclusionTest4() {

	inclusionTest(Setting.CAN_BE_REMOVED, 1);
    }

    @Test
    public void inclusionTest5() {

	inclusionTest(Setting.COMPACT_MODE, 1);
    }

    @Test
    public void inclusionTest6() {

	CheckResponse response = inclusionTest(Setting.CONFIGURABLE_TYPE, 1);

	//
	// 1 SystemSetting
	//
	
	List<Setting> settings = response.//
		getSettings().//
		stream().//
		distinct().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());
	
	Assert.assertEquals(SystemSetting.class, settings.get(0).getSettingClass());
    }

    @Test
    public void inclusionTest7() {

	inclusionTest(Setting.DESCRIPTION, 1);
    }

    @Test
    public void inclusionTest8() {

	inclusionTest(Setting.EDITABLE, 1);
    }

    @Test
    public void inclusionTest9() {

	inclusionTest(Setting.ENABLED, 1);
    }

    @Test
    public void inclusionTest10() {

	inclusionTest(Setting.EXTENSION, 1);
    }

    @Test
    public void inclusionTest11() {

	inclusionTest(Setting.FOLDED_MODE, 1);
    }

    @Test
    public void inclusionTest12() {

	inclusionTest(Setting.NAME, 1);
    }

    @Test
    public void inclusionTest13() {

	CheckResponse response = inclusionTest(Setting.SELECTED, 2);

	List<Setting> settings = response.//
		getSettings().//
		stream().//
		distinct().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	//
	// - DatabaseSetting per default has the selected property (opt. key, def: false)
	// set to true (so the property key is visible) in the databaseConfiguration setting (the normal DB is selected
	// per default)
	// - The DatabaseSetting in the configuration has the volatile DB selected
	// -> so the two settings differ due to the selected property which in this test is included
	//
	Assert.assertEquals(DatabaseSetting.class, settings.get(0).getSettingClass());
    }

    @Test
    public void inclusionTest14() {

	inclusionTest(Setting.SELECTION_MODE, 1);
    }

    @Test
    public void inclusionTest15() {

	CheckResponse response = inclusionTest(Setting.SHOW_HEADER, 5);

	//
	// 1 SystemSetting
	// 3 Harvested/Mixed
	// 1 Distributed
	//

	List<Setting> settings = response.//
		getSettings().//
		stream().//
		distinct().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	//
	// - the accessorsSetting of DistributionSetting has the showHeader property (opt.key, def:true)
	// set to true (so the property key is hidden in accessorsSetting)
	// - in the configuration, he accessorsSetting of DistributionSetting has the showHeader property set to false,
	// so the property is visible with false value
	// -> they differ for the showHeader property which is included in this test
	// - the same for HarvestingSettingImpl and its sub-setting
	//
	Assert.assertEquals(DistributionSetting.class, settings.get(0).getSettingClass());
	Assert.assertEquals(HarvestingSettingImpl.class, settings.get(1).getSettingClass());
    }

    @Test
    public void inclusionTest16() {

	inclusionTest(Setting.VALIDATOR, 1);
    }

    @Test
    public void inclusionTest17() {

	inclusionTest(Setting.VISIBLE, 1);
    }

    /**
     * @param propertyToInclude
     * @param testFail
     */
    private CheckResponse inclusionTest(Property<?> propertyToInclude, int matchesSize) {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	SimilarityMethod method = new SimilarityMethod();

	GSLoggerFactory.getLogger(getClass()).debug("Included property: " + propertyToInclude.getName());

	method.getExclusions().remove(propertyToInclude);

	method.setModifier(new SystemSettingConsumer());

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	Assert.assertEquals(matchesSize, response.getSettings().size());

	response.getMessages().forEach(m -> System.out.println(m));

	return response;
    }
}
