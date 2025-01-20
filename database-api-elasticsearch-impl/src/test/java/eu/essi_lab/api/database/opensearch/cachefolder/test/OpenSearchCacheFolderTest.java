/**
 * 
 */
package eu.essi_lab.api.database.opensearch.cachefolder.test;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.CacheMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class OpenSearchCacheFolderTest extends OpenSearchTest {

    @Test
    public void test() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.CACHE_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	JSONObject cacheEntry1 = new JSONObject();
	cacheEntry1.put("key1", "value1");
	cacheEntry1.put("key2", "value2");

	JSONObject cacheEntry2 = new JSONObject();
	cacheEntry2.put("key3", "value3");
	cacheEntry2.put("key4", "value4");

	JSONObject cacheEntry3 = new JSONObject();
	cacheEntry3.put("key5", "value5");
	cacheEntry3.put("key6", "value6");

	String key1 = "key1";
	String key2 = "key2";
	String key3 = "key3";

	Assert.assertTrue(folder.store(//
		key1, //
		FolderEntry.of(IOStreamUtils.asStream(cacheEntry1.toString(3))), //
		EntryType.CACHE_ENTRY));

	Assert.assertTrue(folder.store(//
		key2, //
		FolderEntry.of(IOStreamUtils.asStream(cacheEntry2.toString(3))), //
		EntryType.CACHE_ENTRY));

	Assert.assertTrue(folder.store(//
		key3, //
		FolderEntry.of(IOStreamUtils.asStream(cacheEntry3.toString(3))), //
		EntryType.CACHE_ENTRY));

	test(database, folderName, folder, cacheEntry1, key1);
	test(database, folderName, folder, cacheEntry2, key2);
	test(database, folderName, folder, cacheEntry3, key3);
    }

    /**
     * @param database
     * @param folderName
     * @param folder
     * @param cacheEntry
     * @param key
     * @throws Exception
     */
    private void test(//
	    OpenSearchDatabase database, //
	    String folderName, //
	    OpenSearchFolder folder, //
	    JSONObject cacheEntry, //
	    String key) throws Exception {

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	Assert.assertEquals(CacheMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(CacheMapping.CACHED_ENTRY, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	JSONObject decodedObject = new JSONObject(//
		IOStreamUtils.asUTF8String(//
			IndexData.decode(wrapper.getCachedEntry().get())));

	Assert.assertEquals(cacheEntry.toString(), decodedObject.toString());
    }
}
