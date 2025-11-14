/**
 * 
 */
package eu.essi_lab.shared.driver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
public abstract class DatabaseCacheDriverTest {

    private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(3);
    private StorageInfo uri;

    /**
     * @param uri
     */
    public DatabaseCacheDriverTest() {

    }

    /**
     * @param uri
     */
    public void setUri(StorageInfo uri) {

	this.uri = uri;
    }

    /**
     * @throws Exception
     */
    @Test
    public void jsonContentTypeTest() throws Exception {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();
	setting.setCategory(SharedContentCategory.DATABASE_CACHE);

	setting.getDatabaseCacheSetting().get().setStorageUri(uri);
	

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getConfiguredDriver(setting, false);

	// driver.configure(setting);

	String contentId = UUID.randomUUID().toString();

	//
	//
	//

	SharedContent<JSONObject> sharedContent = new SharedContent<>();

	sharedContent.setType(SharedContentType.JSON_TYPE);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("testKey", "testValue");

	sharedContent.setContent(jsonObject);
	sharedContent.setIdentifier(contentId);

	//
	//
	//

	driver.store(sharedContent);

	//
	// wait until the content is stored
	//

	Thread.sleep(SLEEP_TIME);

	//
	//
	//

	SharedContent<?> outContent = driver.read(contentId, SharedContentType.JSON_TYPE);
	JSONObject content = (JSONObject) outContent.getContent();

	Assert.assertEquals(jsonObject.get("testKey"), content.get("testKey"));
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void gsResourceContentTypeTest() throws Exception {

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.DATABASE_CACHE, false);

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	setting.getDatabaseCacheSetting().get().setStorageUri(uri);

	setting.setCategory(SharedContentCategory.DATABASE_CACHE);
	

	driver.configure(setting);

	//
	//
	//

	SharedContent<Dataset> sharedContent = new SharedContent<>();

	sharedContent.setType(SharedContentType.GS_RESOURCE_TYPE);

	String privateId = UUID.randomUUID().toString();
	String contentId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);

	sharedContent.setContent(dataset);
	sharedContent.setIdentifier(contentId);

	//
	//
	//

	driver.store(sharedContent);

	//
	// wait until the content is stored
	//

	Thread.sleep(SLEEP_TIME);

	//
	//
	//

	SharedContent<?> outContent = driver.read(contentId, SharedContentType.GS_RESOURCE_TYPE);
	Dataset content = (Dataset) outContent.getContent();

	Assert.assertEquals(content.getPrivateId(), privateId);
    }

    /**
     * @throws Exception
     */
    @Test
    public void genericContentTypeTest() throws Exception {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	setting.getDatabaseCacheSetting().get().setStorageUri(uri);
	setting.setCategory(SharedContentCategory.DATABASE_CACHE);
	

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getConfiguredDriver(setting, false);

	//
	//
	//

	SharedContent<StorageInfo> sharedContent = new SharedContent<>();

	sharedContent.setType(SharedContentType.GENERIC_TYPE);

	String uri = UUID.randomUUID().toString();
	String contentId = UUID.randomUUID().toString();

	StorageInfo dataset = new StorageInfo();
	dataset.setUri(uri);

	sharedContent.setContent(dataset);
	sharedContent.setIdentifier(contentId);

	//
	//
	//

	driver.store(sharedContent);

	//
	// wait until the content is stored
	//

	Thread.sleep(SLEEP_TIME);

	//
	//
	//

	SharedContent<?> outContent = driver.read(contentId, SharedContentType.GENERIC_TYPE);
	StorageInfo content = (StorageInfo) outContent.getContent();

	Assert.assertEquals(content.getUri(), uri);
    }
}
