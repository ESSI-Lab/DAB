package eu.essi_lab.shared.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
@Ignore
public class DriverTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void reusingTest() throws GSException {

	StorageInfo object = new StorageInfo();
	object.setIdentifier("storageFolder");
	object.setPassword("password");
	object.setName("dataBaseName");
	object.setUri("url");
	object.setUser("user");

	String contentIdentifier = UUID.randomUUID().toString();

	{

	    // first call, true or false is the same
	    ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, true);

	    SharedContent<StorageInfo> sharedContent = new SharedContent<>();

	    sharedContent.setIdentifier(contentIdentifier);

	    sharedContent.setType(SharedContentType.GENERIC_TYPE);

	    sharedContent.setContent(object);

	    driver.store(sharedContent);

	    SharedContent<?> content = driver.read(sharedContent.getIdentifier(), SharedContentType.GENERIC_TYPE);

	    Object responseContent = content.getContent();

	    Assert.assertEquals(object, responseContent);
	}

	{

	    //
	    // Reusing an existing one. No need to store the content
	    //

	    ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, true);

	    SharedContent<?> content = driver.read(contentIdentifier, SharedContentType.GENERIC_TYPE);

	    Object responseContent = content.getContent();

	    Assert.assertEquals(object, responseContent);

	}

	{

	    //
	    // Creating new one, no content is found
	    //

	    ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, false);

	    SharedContent<?> content = driver.read(contentIdentifier, SharedContentType.GENERIC_TYPE);

	    Assert.assertNull(content);
	}
    }

    @Test
    public void localCacheDriverTest() throws GSException, IOException {

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, false);
	Assert.assertEquals(LocalCacheDriver.class, driver.getClass());

	SharedContent<?> content = driver.read("noid", SharedContentType.GENERIC_TYPE);

	Assert.assertNull(content);

	//
	// Generic type test
	//

	genericTypeTest(driver);

	driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, false);

	//
	// JSON type test
	//

	Assert.assertEquals(LocalCacheDriver.class, driver.getClass());
	jsonTypeTest(driver);

	//
	// GSResource type test
	//

	driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_CACHE, false);
	Assert.assertEquals(LocalCacheDriver.class, driver.getClass());

	gsResourceTypeTest(driver);
    }

    @Test
    public void localPersistentDriverTest() throws GSException, UnsupportedEncodingException, IOException {

	LocalPersistentDriver driver = (LocalPersistentDriver) DriverFactory.getDriver(SharedContentCategory.LOCAL_PERSISTENT, false);

	Assert.assertEquals(LocalPersistentDriver.class, driver.getClass());

	String testPath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString();

	driver.getSetting().getLocalPersistentSetting().get().setFolderPath(testPath);

	SharedContent<?> content = driver.read("noid", SharedContentType.GENERIC_TYPE);

	Assert.assertNull(content);

	//
	// Generic type test
	//

	genericTypeTest(driver);

	//
	// JSON type test
	//

	driver = (LocalPersistentDriver) DriverFactory.getDriver(SharedContentCategory.LOCAL_PERSISTENT, false);

	driver.getSetting().getLocalPersistentSetting().get().setFolderPath(testPath);

	Assert.assertEquals(LocalPersistentDriver.class, driver.getClass());

	jsonTypeTest(driver);

	//
	// GSResource type test
	//

	driver = (LocalPersistentDriver) DriverFactory.getDriver(SharedContentCategory.LOCAL_PERSISTENT, false);

	driver.getSetting().getLocalPersistentSetting().get().setFolderPath(testPath);

	Assert.assertEquals(LocalPersistentDriver.class, driver.getClass());

	gsResourceTypeTest(driver);

	//
	// File type test
	//

	driver = (LocalPersistentDriver) DriverFactory.getDriver(SharedContentCategory.LOCAL_PERSISTENT, false);

	driver.getSetting().getLocalPersistentSetting().get().setFolderPath(testPath);

	Assert.assertEquals(LocalPersistentDriver.class, driver.getClass());

	fileTypeTest(driver);
    }

    /**
     * @param driver
     * @throws GSException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void fileTypeTest(LocalPersistentDriver driver) throws GSException, UnsupportedEncodingException, IOException {

	SharedContent<File> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(UUID.randomUUID().toString());

	sharedContent.setType(SharedContentType.FILE_TYPE);

	String path = System.getProperty("java.io.tmpdir") + File.separator + "fileTypeTest";

	File file = new File(path);
	FileOutputStream stream = new FileOutputStream(file);
	stream.write("fileTypeTest".getBytes("UTF-8"));
	stream.flush();
	stream.close();

	sharedContent.setContent(file);

	storeTest(driver, sharedContent, SharedContentType.FILE_TYPE);
	readTest(driver, sharedContent, file, SharedContentType.FILE_TYPE);
	countTest(driver, SharedContentType.FILE_TYPE, 1);
    }

    @SuppressWarnings("rawtypes")
    private void genericTypeTest(ISharedRepositoryDriver driver) throws GSException, IOException {

	SharedContent<StorageInfo> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(UUID.randomUUID().toString());

	sharedContent.setType(SharedContentType.GENERIC_TYPE);

	StorageInfo object = new StorageInfo();
	object.setIdentifier("storageFolder");
	object.setPassword("password");
	object.setName("dataBaseName");
	object.setUri("url");
	object.setUser("user");

	sharedContent.setContent(object);

	storeTest(driver, sharedContent, SharedContentType.GENERIC_TYPE);
	readTest(driver, sharedContent, object, SharedContentType.GENERIC_TYPE);
	countTest(driver, SharedContentType.GENERIC_TYPE, 1);
    }

    @SuppressWarnings("rawtypes")
    private void jsonTypeTest(ISharedRepositoryDriver driver) throws GSException, IOException {

	SharedContent<JSONObject> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(UUID.randomUUID().toString());

	sharedContent.setType(SharedContentType.JSON_TYPE);

	JSONObject object = new JSONObject();
	object.put("key", "value");

	sharedContent.setContent(object);

	storeTest(driver, sharedContent, SharedContentType.JSON_TYPE);
	readTest(driver, sharedContent, object, SharedContentType.JSON_TYPE);
	countTest(driver, SharedContentType.JSON_TYPE, 1);
    }

    @SuppressWarnings("rawtypes")
    private void gsResourceTypeTest(ISharedRepositoryDriver driver) throws GSException, IOException {

	SharedContent<GSResource> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(UUID.randomUUID().toString());

	sharedContent.setType(SharedContentType.GS_RESOURCE_TYPE);

	Dataset dataset = new Dataset();
	dataset.setPrivateId(UUID.randomUUID().toString());

	sharedContent.setContent(dataset);

	storeTest(driver, sharedContent, SharedContentType.GS_RESOURCE_TYPE);
	readTest(driver, sharedContent, dataset, SharedContentType.GS_RESOURCE_TYPE);
	countTest(driver, SharedContentType.GS_RESOURCE_TYPE, 1);
    }

    @SuppressWarnings("rawtypes")
    private void storeTest(ISharedRepositoryDriver driver, SharedContent<?> sharedContent, SharedContentType responseType)
	    throws GSException {

	driver.store(sharedContent);
    }

    @SuppressWarnings("rawtypes")
    private void readTest(ISharedRepositoryDriver driver, SharedContent<?> sharedContent, Object object, SharedContentType responseType)
	    throws GSException, IOException {

	SharedContent<?> content = driver.read(sharedContent.getIdentifier(), responseType);

	if (content != null) {

	    Assert.assertEquals(responseType, content.getType());

	    Object responseContent = content.getContent();

	    switch (sharedContent.getType()) {

	    case GENERIC_TYPE:
		Assert.assertEquals(object, responseContent);
		break;

	    case GS_RESOURCE_TYPE:
		Assert.assertEquals(((Dataset) object).getPrivateId(), ((Dataset) responseContent).getPrivateId());
		break;

	    case JSON_TYPE:
		Assert.assertEquals(((JSONObject) object).getString("key"), ((JSONObject) responseContent).getString("key"));
		break;

	    case FILE_TYPE:

		File file = (File) object;
		FileInputStream fileInputStream = new FileInputStream(file);
		String asUTF8String = IOStreamUtils.asUTF8String(fileInputStream);

		Assert.assertEquals("fileTypeTest", asUTF8String);

		fileInputStream.close();
	    }
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