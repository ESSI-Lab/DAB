package eu.essi_lab.cfga.gs.setting.downloadsetting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DownloadSettingTest {

    @Test
    public void test() {

	DownloadSetting setting = new DownloadSetting();

	initTest(setting);
	initTest(new DownloadSetting(setting.getObject()));
	initTest(new DownloadSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, DownloadSetting.class, true));

	setting.setDownloadStorage(DownloadStorage.S3_DOWNLOAD_STORAGE);
	setting.getS3StorageSetting().setEndpoint("uri");
	setting.getS3StorageSetting().setBucketName("bucketName");
	setting.getS3StorageSetting().setSecretKey("password");
	setting.getS3StorageSetting().setAccessKey("user");

	test(setting);
	test(new DownloadSetting(setting.getObject()));
	test(new DownloadSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, DownloadSetting.class, true));

	SelectionUtils.deepClean(setting);

	test(setting);
	test(new DownloadSetting(setting.getObject()));
	test(new DownloadSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, DownloadSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test(DownloadSetting setting) {

	DownloadStorage downloadStorage = setting.getDownloadStorage();
	Assert.assertEquals(DownloadStorage.S3_DOWNLOAD_STORAGE, downloadStorage);

	S3StorageSetting s3StorageSetting = setting.getS3StorageSetting();
	Assert.assertNotNull(s3StorageSetting);

	Assert.assertTrue(s3StorageSetting.isSelected());
	Assert.assertEquals("uri", s3StorageSetting.getEndpoint().get());

	StorageInfo storageUri = setting.getStorageUri();
	Assert.assertEquals("bucketName", storageUri.getName());
    }

    /**
     * @param setting
     */
    private void initTest(DownloadSetting setting) {

	Assert.assertFalse(setting.canBeCleaned());

	DownloadStorage downloadStorage = setting.getDownloadStorage();
	Assert.assertEquals(DownloadStorage.LOCAL_DOWNLOAD_STORAGE, downloadStorage);

	S3StorageSetting s3StorageSetting = setting.getS3StorageSetting();
	Assert.assertNotNull(s3StorageSetting);

	Assert.assertFalse(s3StorageSetting.isSelected());
	Assert.assertEquals("https://s3.amazonaws.com/", s3StorageSetting.getEndpoint().get());

	StorageInfo storageUri = setting.getStorageUri();
	Assert.assertEquals("localFS", storageUri.getName());
    }
}
