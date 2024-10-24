package eu.essi_lab.accessor.waf;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.waf.onamet.ONAMETAccessor;
import eu.essi_lab.accessor.waf.onamet.ONAMETConnector;
import eu.essi_lab.accessor.waf.onamet.ONAMETConnectorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class ONAMETConnectorSettingTest {

    @Test
    public void test1() throws Exception {

	ONAMETAccessor accessor = new ONAMETAccessor();

	HarvestedConnectorSetting generalTypeSetting = accessor.getSetting().getHarvestedConnectorSetting();

	ONAMETConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    @Test
    public void test2() throws Exception {

	ONAMETAccessor accessor = new ONAMETAccessor();
	AccessorSetting setting = accessor.getSetting();

	//
	//
	//

	BrokeringStrategy brokeringStrategy = setting.getBrokeringStrategy();
	Assert.assertEquals(BrokeringStrategy.HARVESTED, brokeringStrategy);

	//
	//
	//

	String configurableType = setting.getConfigurableType();
	Assert.assertEquals(ONAMETAccessor.TYPE, configurableType);

	//
	//
	//

	HarvestedConnectorSetting harvConnectorSetting = setting.getHarvestedConnectorSetting();
	Assert.assertNotNull(harvConnectorSetting);

	ONAMETConnector connector = harvConnectorSetting.createConfigurable();
	Assert.assertEquals(ONAMETConnector.class, connector.getClass());

	//
	//
	//

	GSSource source = setting.getSource();
	GSSource asSource = setting.getGSSourceSetting().asSource();

	Assert.assertEquals(source, asSource);

	Assert.assertEquals(BrokeringStrategy.HARVESTED, source.getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, asSource.getBrokeringStrategy());

	Assert.assertNull(source.getUniqueIdentifier());
	Assert.assertNull(asSource.getUniqueIdentifier());

	Assert.assertNull(source.getEndpoint());
	Assert.assertNull(asSource.getEndpoint());

	Assert.assertNull(source.getLabel());
	Assert.assertNull(asSource.getLabel());

	//
	//
	//

	String distAccessorType = setting.getDistributedAccessorType();
	Assert.assertNull(distAccessorType);

	DistributedConnectorSetting distConnectorSetting = setting.getDistributedConnectorSetting();
	Assert.assertNull(distConnectorSetting);
    }

    @Test
    public void test() {

	ONAMETConnectorSetting setting = new ONAMETConnectorSetting();

	defaultValuesTest(setting);
	defaultValuesTest(new ONAMETConnectorSetting(setting.getObject()));
	defaultValuesTest(new ONAMETConnectorSetting(setting.getObject().toString()));
	defaultValuesTest(SettingUtils.downCast(setting, ONAMETConnectorSetting.class, true));

	setting.setExtractionPath("path");
	setting.setMaxProcessedEntries(1);
	setting.setExtractionTimeout(1);
	setting.setStartFolderUrl("url");
	setting.setS3AccessKey("key");
	setting.setS3BucketName("bucket");
	setting.setS3SecretKey("secret");
	setting.setTHREDDSUrl("url");
	setting.setTHREDDSDataSubFolder("grid");

	valuesTest(setting);
	valuesTest(new ONAMETConnectorSetting(setting.getObject()));
	valuesTest(new ONAMETConnectorSetting(setting.getObject().toString()));
	valuesTest(SettingUtils.downCast(setting, ONAMETConnectorSetting.class, true));
    }

    /**
     * @param setting
     */
    private void valuesTest(ONAMETConnectorSetting setting) {

	Optional<String> extractionPath = setting.getExtractionPath();
	Assert.assertEquals("path", extractionPath.get());

	Optional<Integer> maxProcessedEntries = setting.getMaxProcessedEntries();
	Assert.assertEquals(new Integer(1), maxProcessedEntries.get());

	Optional<Integer> extractionTimeout = setting.getExtractionTimeout();
	Assert.assertEquals(new Integer(1), extractionTimeout.get());

	Optional<String> startFolderUrl = setting.getStartFolderUrl();
	Assert.assertEquals("url", startFolderUrl.get());

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
    private void defaultValuesTest(ONAMETConnectorSetting setting) {

	Optional<String> extractionPath = setting.getExtractionPath();
	Assert.assertFalse(extractionPath.isPresent());

	Optional<Integer> maxProcessedEntries = setting.getMaxProcessedEntries();
	Assert.assertFalse(maxProcessedEntries.isPresent());

	Optional<Integer> extractionTimeout = setting.getExtractionTimeout();
	Assert.assertEquals(new Integer(360), extractionTimeout.get());
	
	Optional<String> startFolderUrl = setting.getStartFolderUrl();
	Assert.assertFalse(startFolderUrl.isPresent());

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
