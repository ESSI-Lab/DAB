package eu.essi_lab.cfga.s3storage.test;

import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class S3StorageSettingTest {

    @Test
    public void test() {

	S3StorageSetting setting = new S3StorageSetting();

	System.out.println(setting);

	try {
	    setting.createConfigurable();
	    fail("Exception not thrown");
	} catch (Exception e) {
	}

	initTest(setting);
	initTest(new S3StorageSetting(setting.getObject()));
	initTest(new S3StorageSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, S3StorageSetting.class, true));

	setting.setEndpoint("uri");
	setting.setAccessKey("userName");
	setting.setSecretKey("password");
	setting.setBucketName("name");

	test(setting);
	test(new S3StorageSetting(setting.getObject()));
	test(new S3StorageSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, S3StorageSetting.class, true));

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("uri2");
	storageUri.setUser("userName2");
	storageUri.setPassword("password2");
	storageUri.setName("name2");

	setting.setStorageUri(storageUri);

	test2(setting);
	test2(new S3StorageSetting(setting.getObject()));
	test2(new S3StorageSetting(setting.getObject().toString()));
	test2(SettingUtils.downCast(setting, S3StorageSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test2(S3StorageSetting setting) {

	Assert.assertEquals("password2", setting.getSecretKey().get());
	Assert.assertEquals("uri2", setting.getEndpoint().get());
	Assert.assertEquals("userName2", setting.getAccessKey().get());
	Assert.assertEquals("name2", setting.getBucketName().get());

	Optional<StorageInfo> storageUri = setting.asStorageUri();
	Assert.assertTrue(storageUri.isPresent());

	Assert.assertEquals("password2", storageUri.get().getPassword());
	Assert.assertEquals("uri2", storageUri.get().getUri());
	Assert.assertEquals("userName2", storageUri.get().getUser());
	Assert.assertEquals("name2", storageUri.get().getName());
    }

    /**
     * @param setting
     */
    private void test(S3StorageSetting setting) {

	Assert.assertEquals("password", setting.getSecretKey().get());
	Assert.assertEquals("uri", setting.getEndpoint().get());
	Assert.assertEquals("userName", setting.getAccessKey().get());
	Assert.assertEquals("name", setting.getBucketName().get());

	Optional<StorageInfo> storageUri = setting.asStorageUri();
	Assert.assertTrue(storageUri.isPresent());

	Assert.assertEquals("password", storageUri.get().getPassword());
	Assert.assertEquals("uri", storageUri.get().getUri());
	Assert.assertEquals("userName", storageUri.get().getUser());
	Assert.assertEquals("name", storageUri.get().getName());
    }

    /**
     * @param setting
     */
    private void initTest(S3StorageSetting setting) {

	Assert.assertFalse(setting.getSecretKey().isPresent());
	Assert.assertEquals("https://s3.amazonaws.com/", setting.getEndpoint().get());
	Assert.assertFalse(setting.getAccessKey().isPresent());
	Assert.assertFalse(setting.getBucketName().isPresent());
	Assert.assertFalse(setting.asStorageUri().isPresent());
    }
}
