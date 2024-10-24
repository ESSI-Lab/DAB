package eu.essi_lab.accessor.waf;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.waf.onamet.ONAMETAugmenterSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ONAMETAugmenterSettingTest {

    @Test
    public void initTest() {

	ONAMETAugmenterSetting setting = new ONAMETAugmenterSetting();

	defaultValuesTest(setting);
	defaultValuesTest(new ONAMETAugmenterSetting(setting.getObject()));
	defaultValuesTest(new ONAMETAugmenterSetting(setting.getObject().toString()));
	defaultValuesTest(SettingUtils.downCast(setting, ONAMETAugmenterSetting.class, true));

	setting.setS3AccessKey("key");
	setting.setS3BucketName("bucket");
	setting.setS3SecretKey("secret");
	setting.setDeleteNCFiles(false);

	valuesTest(setting);
	valuesTest(new ONAMETAugmenterSetting(setting.getObject()));
	valuesTest(new ONAMETAugmenterSetting(setting.getObject().toString()));
	valuesTest(SettingUtils.downCast(setting, ONAMETAugmenterSetting.class, true));
    }

    /**
     * @param setting
     */
    private void valuesTest(ONAMETAugmenterSetting setting) {

	Optional<String> s3AccessKey = setting.getS3AccessKey();
	Assert.assertEquals("key", s3AccessKey.get());

	Optional<String> s3BucketName = setting.getS3BucketName();
	Assert.assertEquals("bucket", s3BucketName.get());

	Optional<String> s3SecretKey = setting.getS3SecretKey();
	Assert.assertEquals("secret", s3SecretKey.get());

	boolean deleteNCFiles = setting.deleteNCFiles();
	Assert.assertEquals(false, deleteNCFiles);

    }

    /**
     * @param setting
     * @param clone
     */
    private void defaultValuesTest(ONAMETAugmenterSetting setting) {

	Optional<String> s3AccessKey = setting.getS3AccessKey();
	Assert.assertFalse(s3AccessKey.isPresent());

	Optional<String> s3BucketName = setting.getS3BucketName();
	Assert.assertFalse(s3BucketName.isPresent());

	Optional<String> s3SecretKey = setting.getS3SecretKey();
	Assert.assertFalse(s3SecretKey.isPresent());

	boolean deleteNCFiles = setting.deleteNCFiles();
	Assert.assertEquals(true, deleteNCFiles);
    }
}
