package eu.essi_lab.accessor.waf;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.waf.onamet_stations.ONAMETStationsAugmenterSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ONAMETStationsAugmenterSettingTest {

    @Test
    public void test() {

	ONAMETStationsAugmenterSetting setting = new ONAMETStationsAugmenterSetting();

	defaultValuesTest(setting);
	defaultValuesTest(new ONAMETStationsAugmenterSetting(setting.getObject()));
	defaultValuesTest(new ONAMETStationsAugmenterSetting(setting.getObject().toString()));
	defaultValuesTest(SettingUtils.downCast(setting, ONAMETStationsAugmenterSetting.class, true));

	setting.setNCPath("path");
	setting.setS3AccessKey("key");
	setting.setS3BucketName("bucket");
	setting.setS3SecretKey("secret");
	setting.setTHREDDSUrl("url");
	setting.setTHREDDSDataSubFolder("grid");

	valuesTest(setting);
	valuesTest(new ONAMETStationsAugmenterSetting(setting.getObject()));
	valuesTest(new ONAMETStationsAugmenterSetting(setting.getObject().toString()));
	valuesTest(SettingUtils.downCast(setting, ONAMETStationsAugmenterSetting.class, true));
    }

    /**
     * @param setting
     */
    private void valuesTest(ONAMETStationsAugmenterSetting setting) {

	Optional<String> ncPath = setting.getNCPath();
	Assert.assertEquals("path", ncPath.get());

	Optional<String> s3AccessKey = setting.getS3AccessKey();
	Assert.assertEquals("key", s3AccessKey.get());

	Optional<String> s3BucketName = setting.getS3BucketName();
	Assert.assertEquals("bucket", s3BucketName.get());

	Optional<String> s3SecretKey = setting.getS3SecretKey();
	Assert.assertEquals("secret", s3SecretKey.get());

	String threddsUrl = setting.getTHREDDSUrl();
	Assert.assertEquals("url", threddsUrl);

	Optional<String> threddsDataSubFolder = setting.getTHREDDSDataSubFolder();
	Assert.assertEquals("grid", threddsDataSubFolder.get());
    }

    /**
     * @param setting
     * @param clone
     */
    private void defaultValuesTest(ONAMETStationsAugmenterSetting setting) {

	Optional<String> ncPath = setting.getNCPath();
	Assert.assertFalse(ncPath.isPresent());

	Optional<String> s3AccessKey = setting.getS3AccessKey();
	Assert.assertFalse(s3AccessKey.isPresent());

	Optional<String> s3BucketName = setting.getS3BucketName();
	Assert.assertFalse(s3BucketName.isPresent());

	Optional<String> s3SecretKey = setting.getS3SecretKey();
	Assert.assertFalse(s3SecretKey.isPresent());

	String threddsUrl = setting.getTHREDDSUrl();
	Assert.assertEquals("http://localhost/thredds/", threddsUrl);

	Optional<String> threddsDataSubFolder = setting.getTHREDDSDataSubFolder();
	Assert.assertFalse(threddsDataSubFolder.isPresent());
    }
}
