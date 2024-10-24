package eu.essi_lab.cfga.source.test;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.S3Source;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;

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
    private static final String TEST_CONFIG_NAME = "testconfig";

    @Before
    public void deleteConfig() {

	S3TransferWrapper manager = createWrapper();

	List<S3ObjectSummary> objectsSummaries = manager.listObjectsSummaries(TEST_BUCKET_NAME);
	objectsSummaries.stream().map(s -> s.getKey()).forEach(key -> manager.deleteObject(TEST_BUCKET_NAME, key));
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

	JSONArray array = new JSONArray();

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.list().forEach(s -> array.put(new JSONObject(s.getObject().toString())));

	S3Source.uploadConfig(array, manager, TEST_BUCKET_NAME, TEST_CONFIG_NAME);

	//
	// in the current uploaded config the first ordered setting is not editable
	//

	Setting setting1 = configuration.//
		list().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get();

	String setting1Name = setting1.getName();
	
	boolean editable1 = setting1.isEditable();

	Assert.assertFalse(editable1);

	//
	// replaces the setting which now can be edited
	//

	setting1.setEditable(true);

	boolean replaced = configuration.replace(setting1);

	Assert.assertTrue(replaced);

	//
	// flushes the edited configuration
	//

	source.flush(configuration.list());

	//
	// get the configuration and asserts that the is edited so it has been flushed
	//

	Setting setting2 = source.//
		list().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get();

	Assert.assertEquals(setting1Name, setting2.getName());

	boolean editable2 = setting2.isEditable();

	Assert.assertTrue(editable2);
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
