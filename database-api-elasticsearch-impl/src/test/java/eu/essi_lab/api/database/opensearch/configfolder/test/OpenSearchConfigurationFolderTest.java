/**
 * 
 */
package eu.essi_lab.api.database.opensearch.configfolder.test;

import java.io.InputStream;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.ConfigurationMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class OpenSearchConfigurationFolderTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.CONFIGURATION_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	Configuration configuration = createConfig();

	String configName = "testConfig.json";

	String configKey = configName;

	folder.store(//
		configKey, //
		FolderEntry.of(IOStreamUtils.asStream(configuration.toString())), //
		EntryType.CONFIGURATION);

	JSONObject lockFile = new JSONObject();
	lockFile.put("owner", UUID.randomUUID().toString());
	lockFile.put("timeStamp", System.currentTimeMillis());

	String lockKey = "lock.json";

	Assert.assertTrue(folder.store(//
		lockKey, //
		FolderEntry.of(IOStreamUtils.asStream(lockFile.toString(3))), //
		EntryType.CONFIGURATION_LOCK));

	{

	    SourceWrapper wrapper = folder.getSourceWrapper(configKey);

	    //
	    // configuration: base properties
	    //

	    Assert.assertEquals(ConfigurationMapping.get().getIndex(), wrapper.getIndex());

	    Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	    Assert.assertEquals(folder.getName(), wrapper.getFolderName());

	    Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	    Assert.assertEquals(OpenSearchFolder.getEntryId(folder, configKey), wrapper.getEntryId());

	    Assert.assertEquals(configKey, wrapper.getEntryName());

	    Assert.assertEquals(ConfigurationMapping.CONFIGURATION, wrapper.getBinaryProperty());

	    Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	    Assert.assertEquals(wrapper.getConfiguration().get(), wrapper.getBinaryValue());

	    //
	    // configuaration-index property
	    //

	    Assert.assertEquals(configName, wrapper.getConfigurationName().get());

	    Assert.assertEquals(//
		    OpenSearchUtils.encode(FolderEntry.of(IOStreamUtils.asStream(configuration.toString()))), //
		    wrapper.getConfiguration().get());

	    //
	    //
	    //

	}

	{

	    SourceWrapper wrapper = folder.getSourceWrapper(lockKey);

	    //
	    // configuration lock: base properties
	    //

	    Assert.assertEquals(ConfigurationMapping.get().getIndex(), wrapper.getIndex());

	    Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	    Assert.assertEquals(folder.getName(), wrapper.getFolderName());

	    Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	    Assert.assertEquals(database.getIdentifier() + "_" + folder.getName() + "_" + lockKey, wrapper.getEntryId());

	    Assert.assertEquals(lockKey, wrapper.getEntryName());

	    Assert.assertEquals(ConfigurationMapping.CONFIGURATION_LOCK, wrapper.getBinaryProperty());

	    Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	    //
	    // configuaration-index property
	    //

	    Assert.assertEquals(//
		    OpenSearchUtils.encode(FolderEntry.of(IOStreamUtils.asStream(lockFile.toString(3)))), //
		    wrapper.getConfigurationLock().get());
	}
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.CONFIGURATION_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	Configuration configuration = createConfig();

	String configName = "testConfig.json";

	String key = configName;

	folder.store(//
		key, //
		FolderEntry.of(IOStreamUtils.asStream(configuration.toString())), //
		EntryType.CONFIGURATION);

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(binary));

	Configuration configuration2 = new Configuration(array);

	Assert.assertEquals(configuration, configuration2);
    }

    /**
     * @return
     */
    private Configuration createConfig() {

	Configuration configuration = new Configuration();

	Setting setting1 = new Setting();
	setting1.setIdentifier("setting1");

	Setting setting2 = new Setting();
	setting2.setIdentifier("setting2");

	Setting setting3 = new Setting();
	setting3.setIdentifier("setting3");

	configuration.put(setting1);
	configuration.put(setting2);
	configuration.put(setting3);

	return configuration;
    }
}
