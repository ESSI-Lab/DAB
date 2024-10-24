package eu.essi_lab.cfga.gs.setting.downloadsetting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.driver.LocalFolderSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class LocalFolderSettingTest {

    @Test
    public void test() {

	LocalFolderSetting setting = new LocalFolderSetting();

	initTest(setting);
	initTest(new LocalFolderSetting(setting.getObject()));
	initTest(new LocalFolderSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, LocalFolderSetting.class, true));

	setting.setFolderPath("file://D:\\Desktop\\driverDownload");

	valueTest(setting);
	valueTest(new LocalFolderSetting(setting.getObject()));
	valueTest(new LocalFolderSetting(setting.getObject().toString()));
	valueTest(SettingUtils.downCast(setting, LocalFolderSetting.class, true));

    }

    /**
     * @param setting
     */
    private void valueTest(LocalFolderSetting setting) {

	String folderPath = setting.getFolderPath();

	Assert.assertEquals("D:\\Desktop\\driverDownload".replace("\\", "/"), folderPath);
    }

    /**
     * @param setting
     */
    private void initTest(LocalFolderSetting setting) {

	String folderPath = setting.getFolderPath();

	Assert.assertEquals(System.getProperty("java.io.tmpdir").replace("\\", "/"), folderPath);
    }

}
