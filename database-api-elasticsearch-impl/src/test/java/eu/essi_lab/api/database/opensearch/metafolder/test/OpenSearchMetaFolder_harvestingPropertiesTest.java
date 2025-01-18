/**
 * 
 */
package eu.essi_lab.api.database.opensearch.metafolder.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;
import eu.essi_lab.messages.HarvestingProperties;

/**
 * @author Fabrizio
 */
public class OpenSearchMetaFolder_harvestingPropertiesTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	HarvestingProperties properties = new HarvestingProperties();
	properties.put("key", "value");
	properties.put("key1", "value1");
	properties.put("key2", "value2");

	String key = "harvesting.properties";

	folder.store(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(MetaFolderMapping.META_FOLDER_INDEX, wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(MetaFolderMapping.HARVESTING_PROPERTIES, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	//
	// meta-folder-index property
	//

	Assert.assertEquals(SOURCE_ID, wrapper.getSourceId().get());

	Assert.assertTrue(wrapper.getIndexDoc().isEmpty());
	Assert.assertTrue(wrapper.getWarnReport().isEmpty());
	Assert.assertTrue(wrapper.getErrorsReport().isEmpty());
	Assert.assertTrue(wrapper.getDataFolder().isEmpty());

	//
	// binary data test
	//

	String binaryData = wrapper.getHarvestingProperties().get();
	InputStream stream = IndexData.decode(binaryData);
	HarvestingProperties fromStream = HarvestingProperties.fromStream(stream);

	Assert.assertEquals(properties, fromStream);

	Assert.assertEquals(properties.toString(), fromStream.toString());

	//
	// low level test omitted since it is not guaranteed that the base 64 encoded strings
	// are always equals
	//
	// Assert.assertEquals(//
	// IndexData.encode(FolderEntry.of(properties.asStream())), //
	// wrapper.getHarvestingProperties().get() //
	// );
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	HarvestingProperties properties = new HarvestingProperties();
	properties.put("key1", "value1");
	properties.put("key2", "value2");
	properties.put("key3", "value3");

	String key = "harvesting.properties";

	//
	//
	//

	Assert.assertFalse(folder.remove(key));

	Assert.assertFalse(folder.exists(key));

	Assert.assertEquals(0, folder.size());

	//
	//
	//

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	//
	//
	//

	Assert.assertEquals(1, folder.size());

	Assert.assertTrue(folder.exists(key));

	//
	//
	//

	Assert.assertFalse(folder.store(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	Assert.assertNotNull(binary);

	HarvestingProperties fromStream = HarvestingProperties.fromStream(binary);

	Assert.assertEquals(properties.get("key1"), fromStream.get("key1"));
	Assert.assertEquals(properties.get("key2"), fromStream.get("key2"));
	Assert.assertEquals(properties.get("key3"), fromStream.get("key3"));

	binary = folder.getBinary(key);

	Assert.assertNotNull(binary);

	fromStream = HarvestingProperties.fromStream(binary);

	Assert.assertEquals(properties.get("key1"), fromStream.get("key1"));
	Assert.assertEquals(properties.get("key2"), fromStream.get("key2"));
	Assert.assertEquals(properties.get("key3"), fromStream.get("key3"));

	//
	//
	//

	Assert.assertTrue(folder.remove(key));

	Assert.assertFalse(folder.exists(key));

	//
	//
	//

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	//
	//
	//

	Assert.assertTrue(folder.remove(key));

	Assert.assertFalse(folder.exists(key));

	Assert.assertEquals(0, folder.size());

	//
	//
	//

	folder.store(//
		key + "_1", //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES);

	folder.store(//
		key + "_2", //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES);

	folder.store(//
		key + "_3", //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES);

	Assert.assertEquals(3, folder.size());

	//
	//
	//

	List<String> keys = Arrays.asList(folder.listKeys()).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	Assert.assertEquals(key + "_1", keys.get(0));
	Assert.assertEquals(key + "_2", keys.get(1));
	Assert.assertEquals(key + "_3", keys.get(2));

	//
	//
	//

	folder.clear();

	Assert.assertEquals(0, folder.size());

	Assert.assertEquals(0, folder.listKeys().length);

	//
	//
	//

	Assert.assertFalse(folder.replace(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	properties.put("key4", "value4");

	Assert.assertTrue(folder.replace(//
		key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.HARVESTING_PROPERTIES));

	InputStream binary2 = folder.getBinary(key);

	HarvestingProperties fromStream2 = HarvestingProperties.fromStream(binary2);

	Assert.assertEquals(fromStream2.get("key4"), properties.get("key4"));

	binary2 = folder.getBinary(key);

	fromStream2 = HarvestingProperties.fromStream(binary2);

	Assert.assertEquals(fromStream2.get("key4"), properties.get("key4"));

	//
	// trying to get a binary as a doc throws an exception
	//

	Assert.assertThrows(Exception.class, () -> folder.get(key));

	//
	Assert.assertThrows(Exception.class, () -> folder.get("notFound"));
	Assert.assertThrows(Exception.class, () -> folder.getBinary("notFound"));
    }

    /**
     * @param database
     * @return
     */
    static String getFolderName(OpenSearchDatabase database) {

	return SOURCE_ID + // source id
		SourceStorageWorker.META_PREFIX;//
    }
}
