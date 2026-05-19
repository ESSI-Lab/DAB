package eu.essi_lab.cfga.database.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class SourceStorageProviderSettingTest {

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

	setting.setMarkDeleted("source1", "source2", "source3");
	setting.setDisableSmartStorage("source1", "source2", "source3");

	test2(setting);
	test2(new SourceStorageSetting(setting.getObject()));
	test2(new SourceStorageSetting(setting.getObject().toString()));
	test2(SettingUtils.downCast(setting, SourceStorageSetting.class, true));

	//
	//
	//

	setting.removeMarkDeleted("source1");

	setting.removeSmartStorageDisabledSet("source1");
	setting.removeSmartStorageDisabledSet("source3");

	Assert.assertFalse(setting.isMarkDeletedOption("source1"));
	Assert.assertTrue(setting.isMarkDeletedOption("source2"));
	Assert.assertTrue(setting.isMarkDeletedOption("source3"));

	Assert.assertFalse(setting.isSmartStorageDisabledSet("source1"));
	Assert.assertTrue(setting.isSmartStorageDisabledSet("source2"));
	Assert.assertFalse(setting.isSmartStorageDisabledSet("source3"));

	//
	//
	//

	setting.disableMarkDeleted();
	setting.enableSmartStorage();

	test1(setting);
	test1(new SourceStorageSetting(setting.getObject()));
	test1(new SourceStorageSetting(setting.getObject().toString()));
	test1(SettingUtils.downCast(setting, SourceStorageSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test1(SourceStorageSetting setting) {

	Boolean markDeletedOption = setting.isMarkDeletedOption("");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source1");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source2");
	Assert.assertFalse(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source3");
	Assert.assertFalse(markDeletedOption);

	//
	//
	//

	Boolean smartStorageDisabled = setting.isSmartStorageDisabledSet("");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source1");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source2");
	Assert.assertFalse(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source3");
	Assert.assertFalse(smartStorageDisabled);
    }

    /**
     * @param setting
     */
    private void test2(SourceStorageSetting setting) {

	Boolean markDeletedOption = setting.isMarkDeletedOption("source1");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source2");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source3");
	Assert.assertTrue(markDeletedOption);

	markDeletedOption = setting.isMarkDeletedOption("source4");
	Assert.assertFalse(markDeletedOption);

	//
	//
	//

	Boolean smartStorageDisabled = setting.isSmartStorageDisabledSet("source1");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source2");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source3");
	Assert.assertTrue(smartStorageDisabled);

	smartStorageDisabled = setting.isSmartStorageDisabledSet("source4");
	Assert.assertFalse(smartStorageDisabled);
    }
}
