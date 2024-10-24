/**
 * 
 */
package eu.essi_lab.shared.driver.es;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.DriverFactory;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
import eu.essi_lab.shared.messages.SharedContentQuery;

/**
 * @author Fabrizio
 */
public class ESPersistentDriverInternalTestIT {

    // docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node"
    // docker.elastic.co/elasticsearch/elasticsearch:5.5.3
    // public static final StorageUri ES_STORAGE = new StorageUri("http://localhost:9200");

    public static final StorageInfo ES_STORAGE = new StorageInfo(System.getProperty("es.host"));

    @Before
    public void before() {

	ES_STORAGE.setUser(System.getProperty("es.user"));

	ES_STORAGE.setPassword(System.getProperty("es.password"));

	ES_STORAGE.setName(UUID.randomUUID().toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws GSException {

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT, false);

	SharedPersistentDriverSetting setting = new SharedPersistentDriverSetting();
	setting.getElasticSearchSetting().get().setStorageUri(ES_STORAGE);

	driver.configure(setting);

	Assert.assertEquals(ESPersistentDriver.class, driver.getClass());

	// genericTypeTest(driver);

	driver = DriverFactory.getDriver(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT, false);
	driver.configure(setting);

	Assert.assertEquals(ESPersistentDriver.class, driver.getClass());
	jsonTypeTest(driver);

	//
	//
	//

	multipleGetTest(driver);

	// driver = DriverFactory.getDriver(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT, false);
	// driver.configure(setting);
	//
	// Assert.assertEquals(ESPersistentDriver.class, driver.getClass());
	//
	// gsResourceTypeTest(driver);
    }

    @SuppressWarnings("rawtypes")
    private void jsonTypeTest(ISharedRepositoryDriver driver) throws GSException {

	SharedContent<JSONObject> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(UUID.randomUUID().toString());

	sharedContent.setType(SharedContentType.JSON_TYPE);

	JSONObject object = new JSONObject();
	object.put("key", "value");

	sharedContent.setContent(object);

	readTest(driver, sharedContent, null, SharedContentType.JSON_TYPE);
	countTest(driver, SharedContentType.JSON_TYPE, 0);

	//
	//
	//

	storeTest(driver, sharedContent, object, SharedContentType.JSON_TYPE);

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	}

	readTest(driver, sharedContent, object, SharedContentType.JSON_TYPE);

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	}

	countTest(driver, SharedContentType.JSON_TYPE, 1);

	try {

	    readTest(driver, sharedContent, null, SharedContentType.GENERIC_TYPE);

	} catch (GSException ex) {
	    // OK
	}

	try {

	    countTest(driver, SharedContentType.GENERIC_TYPE, 0);

	} catch (GSException ex) {
	    // OK
	}

	//
	//
	//

	try {

	    readTest(driver, sharedContent, null, SharedContentType.GS_RESOURCE_TYPE);

	} catch (GSException ex) {
	    // OK
	}

	try {
	    countTest(driver, SharedContentType.GS_RESOURCE_TYPE, 0);

	} catch (GSException ex) {
	    // OK
	}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void multipleGetTest(ISharedRepositoryDriver driver) throws GSException {

	SharedContentQuery query = new SharedContentQuery();

	for (int i = 0; i < 5; i++) {

	    SharedContent<JSONObject> sharedContent = new SharedContent<>();
	    sharedContent.setIdentifier("id_" + i);
	    sharedContent.setType(SharedContentType.JSON_TYPE);

	    query.addIdentifier(sharedContent.getIdentifier());

	    JSONObject object = new JSONObject();
	    object.put("key", "value_" + i);
	    sharedContent.setContent(object);

	    driver.store(sharedContent);
	}

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	}

	//
	//
	//

	Long count = driver.count(SharedContentType.JSON_TYPE);

	Assert.assertEquals(new Long(6), count);

	//
	//
	//

	List<SharedContent> list = driver.read(SharedContentType.JSON_TYPE, query);
	list.//
		stream().//
		sorted((s1, s2) -> ((JSONObject) s1.getContent()).getString("key")
			.compareTo(((JSONObject) s2.getContent()).getString("key")))
		.//
		collect(Collectors.toList());

	Assert.assertEquals(5, list.size());

	for (int i = 0; i < 5; i++) {

	    Assert.assertEquals("value_" + i, ((JSONObject) list.get(i).getContent()).getString("key"));
	}
    }

    @SuppressWarnings("rawtypes")
    private void storeTest(ISharedRepositoryDriver driver, SharedContent<?> sharedContent, Object object, SharedContentType responseType)
	    throws GSException {

	driver.store(sharedContent);
    }

    @SuppressWarnings("rawtypes")
    private void readTest(ISharedRepositoryDriver driver, SharedContent<?> sharedContent, Object object, SharedContentType responseType)
	    throws GSException {

	SharedContent<?> content = driver.read(sharedContent.getIdentifier(), responseType);

	if (content != null) {

	    Assert.assertEquals(responseType, content.getType());

	    Object responseContent = content.getContent();

	    Assert.assertEquals(((JSONObject) object).getString("key"), ((JSONObject) responseContent).getString("key"));

	} else {

	    Assert.assertNull(object);
	}
    }

    @SuppressWarnings("rawtypes")
    private void countTest(ISharedRepositoryDriver driver, SharedContentType responseType, int expected) throws GSException {

	Long count = driver.count(responseType);
	Assert.assertEquals(new Long(expected), new Long(count));
    }
}
