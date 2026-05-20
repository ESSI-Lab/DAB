package eu.essi_lab.cfga.database.test;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.model.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Fabrizio
 */
public class SourceStorageSettingTest {

    @Before
    public void init() {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);
    }

    @Test
    public void resetAndSelectTest() {

	SourceStorageSetting setting = new SourceStorageSetting();

	setting.setMarkDeleted("atlasSouth", "defaultOAISource", "defaultGBIFMixedSource");
	setting.setDisableSmartStorage("atlasSouth", "defaultOAISource", "defaultGBIFMixedSource");

	setting.clean();

	//
	//
	//

	SourceStorageSetting setting1 = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), SourceStorageSetting.class);

	test2(setting);
	test2(setting1);
    }


    @Test
    public void test() {

	SourceStorageSetting setting = new SourceStorageSetting();

	try {
	    setting.createConfigurable();
	    fail("Exception not thrown");
	} catch (Exception e) {
	    // configurable class not set
	}

	test1(setting);
	test1(new SourceStorageSetting(setting.getObject()));
	test1(new SourceStorageSetting(setting.getObject().toString()));
	test1(SettingUtils.downCast(setting, SourceStorageSetting.class, true));

	Assert.assertEquals(//
		setting.getName(), //
		new SourceStorageSetting(setting.getObject()).getName());

	//
	//
	//

	setting.setMarkDeleted("atlasSouth", "defaultOAISource", "defaultGBIFMixedSource");
	setting.setDisableSmartStorage("atlasSouth", "defaultOAISource", "defaultGBIFMixedSource");

	test2(setting);
	test2(new SourceStorageSetting(setting.getObject()));
	test2(new SourceStorageSetting(setting.getObject().toString()));
	test2(SettingUtils.downCast(setting, SourceStorageSetting.class, true));

	//
	//
	//

	setting.removeMarkDeleted("atlasSouth");

	setting.removeSmartStorageDisabled("atlasSouth", "defaultGBIFMixedSource");

	Assert.assertFalse(setting.isMarkDeleted("atlasSouth"));
	Assert.assertTrue(setting.isMarkDeleted("defaultOAISource"));
	Assert.assertTrue(setting.isMarkDeleted("defaultGBIFMixedSource"));

	Assert.assertFalse(setting.isSmartStorageDisabled("atlasSouth"));
	Assert.assertTrue(setting.isSmartStorageDisabled("defaultOAISource"));
	Assert.assertFalse(setting.isSmartStorageDisabled("defaultGBIFMixedSource"));
    }

    /**
     * @param setting
     */
    private void test1(SourceStorageSetting setting) {

	boolean markDeletedOption = setting.isMarkDeleted("");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("atlasSouth");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("defaultOAISource");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("defaultGBIFMixedSource");
	Assert.assertFalse(markDeletedOption);

	//
	//
	//

	boolean smartStorageDisabled = setting.isSmartStorageDisabled("");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("atlasSouth");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("defaultOAISource");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("defaultGBIFMixedSource");
	Assert.assertFalse(smartStorageDisabled);
    }

    /**
     * @param setting
     */
    private void test2(SourceStorageSetting setting) {

	boolean markDeletedOption = setting.isMarkDeleted("atlasSouth");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("defaultOAISource");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("defaultGBIFMixedSource");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeleted("source4");
	Assert.assertFalse(markDeletedOption);

	//
	//
	//

	boolean smartStorageDisabled = setting.isSmartStorageDisabled("atlasSouth");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("defaultOAISource");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("defaultGBIFMixedSource");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabled("source4");
	Assert.assertFalse(smartStorageDisabled);
    }
}
