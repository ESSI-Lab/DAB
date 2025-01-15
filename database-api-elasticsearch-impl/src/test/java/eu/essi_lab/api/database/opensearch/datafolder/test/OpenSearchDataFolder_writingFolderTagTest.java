/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_writingFolderTagTest extends OpenSearchTest {

    /**
     * @param database
     * @return
     */
    static String getFolderName(OpenSearchDatabase database) {

	return SOURCE_ID + // source id
		SourceStorageWorker.DATA_1_PREFIX;//
    }

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	InputStream tag = SourceStorageWorker.createWritingFolderTag();

	Assert.assertEquals(0, tag.available());

	String key = SourceStorageWorker.WRITING_FOLDER_TAG;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(tag), //
		EntryType.WRITING_FOLDER_TAG);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(DataFolderMapping.DATA_FOLDER_INDEX, wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(DataFolderMapping.WRITING_FOLDER_TAG, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	//
	// binary data test
	//

	Assert.assertTrue(wrapper.hasWritingFolderTag());

	Assert.assertTrue(folder.exists(key));

	InputStream binary = folder.getBinary(key);

	Assert.assertEquals(0, binary.available());

	folder.remove(key);

	wrapper = folder.getSourceWrapper(key);
	Assert.assertFalse(wrapper.hasWritingFolderTag());
    }
}
