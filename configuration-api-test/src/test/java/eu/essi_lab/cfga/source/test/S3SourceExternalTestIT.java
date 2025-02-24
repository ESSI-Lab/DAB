package eu.essi_lab.cfga.source.test;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.S3Source;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * @author Fabrizio
 */
public class S3SourceExternalTestIT {

    /**
     * 
     */
    private static final String TEST_BUCKET_NAME = "dabconfigurationtest";

    /**
     * 
     */
    private static final String TEST_CONFIG_NAME = "testconfig.json";

    @Before
    public void deleteConfig() {

	S3TransferWrapper manager = createWrapper();

	List<S3Object> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);

	objectsSummaries.stream().map(s -> s.key()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));
    }

    @Test
    public void isMissingTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	boolean emptyOrMissing = source.isEmptyOrMissing();

	Assert.assertTrue(emptyOrMissing);
    }

    @Test
    public void isEmptyTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	//
	// uploads an empty config
	//

	JSONArray array = new JSONArray();

	S3Source.uploadConfig(array, manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	//
	//

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	boolean emptyOrMissing = source.isEmptyOrMissing();

	Assert.assertTrue(emptyOrMissing);

    }

    @Test
    public void isNotEmptyTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	//
	// uploads a default config
	//

	JSONArray array = new JSONArray();

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.list().forEach(s -> array.put(s));

	S3Source.uploadConfig(array, manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	//
	//

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	boolean emptyOrMissing = source.isEmptyOrMissing();

	Assert.assertFalse(emptyOrMissing);
    }

    @Test
    public void listTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	//
	// uploads a default config
	//

	JSONArray array = new JSONArray();

	DefaultConfiguration configuration = new DefaultConfiguration();

	int size = configuration.list().size();

	configuration.list().forEach(s -> array.put(new JSONObject(s.getObject().toString())));

	S3Source.uploadConfig(array, manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	//
	//

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	List<Setting> list = source.list();

	Assert.assertEquals(size, list.size());
    }

    @Test
    public void flushTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	// uploads a default config
	//

	DefaultConfiguration configuration = new DefaultConfiguration();

	S3Source.uploadConfig(configuration.toJSONArray(), manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	// in the default config the first ordered setting is a "ARPA HydroCSV"
	// a ProfilerSetting which is editable
	//

	Setting setting1 = configuration.//
		list().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get();

	String setting1Name = setting1.getName();

	boolean editable1 = setting1.isEditable();

	Assert.assertTrue(editable1);

	//
	// replaces the setting which now cannot be edited
	//

	setting1.setEditable(false);

	boolean replaced = configuration.replace(setting1);

	Assert.assertTrue(replaced);

	//
	// flushes the edited configuration
	//

	source.flush(configuration.list());

	//
	// get the configuration and asserts that the setting now is not
	// editable, so it has been flushed
	//

	Setting setting2 = source.//
		list().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get();

	Assert.assertEquals(setting1Name, setting2.getName());

	boolean editable2 = setting2.isEditable();

	Assert.assertFalse(editable2);
    }

    @Test
    public void backupTest() throws Exception {

	S3TransferWrapper manager = createWrapper();

	S3Source source = new S3Source(manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	Setting setting1 = new Setting();
	Setting setting2 = new Setting();
	Setting setting3 = new Setting();

	source.flush(Arrays.asList(setting1, setting2, setting3));

	S3Source backup = source.backup();

	Assert.assertFalse(backup.isEmptyOrMissing());

	List<Setting> list = backup.list();

	Assert.assertEquals(3, list.size());

	// Assert.assertEquals(source.getSource().getParent(), backup.getSource().getParent());

	Assert.assertTrue(backup.getLocation().endsWith(".backup"));
    }

    /**
     * @return
     */
    private S3TransferWrapper createWrapper() {

	String accessKey = System.getProperty("accessKey");
	String secretKey = System.getProperty("secretKey");

	S3TransferWrapper wrapper = new S3TransferWrapper();
	wrapper.setAccessKey(accessKey);
	wrapper.setSecretKey(secretKey);

	Assert.assertEquals(accessKey, wrapper.getAccessKey());
	Assert.assertEquals(secretKey, wrapper.getSecretKey());

	Assert.assertFalse(wrapper.getEndpoint().isPresent());

	return wrapper;
    }

}
