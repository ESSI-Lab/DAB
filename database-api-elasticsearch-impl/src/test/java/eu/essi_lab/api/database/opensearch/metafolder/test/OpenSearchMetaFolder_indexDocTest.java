/**
 * 
 */
package eu.essi_lab.api.database.opensearch.metafolder.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;

/**
 * @author Fabrizio
 */
public class OpenSearchMetaFolder_indexDocTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = OpenSearchMetaFolder_harvestingPropertiesTest.getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(SOURCE_ID);

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_PREFIX);

	String key = SOURCE_ID + SourceStorageWorker.DATA_1_PREFIX;

	folder.store(//
		key, //
		FolderEntry.of(doc.getDocument()), //
		EntryType.DATA_FOLDER_INDEX_DOC);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getResourceId(folder, key), wrapper.getResourceId());

	Assert.assertEquals(key, wrapper.getResourceKey());

	Assert.assertEquals(MetaFolderMapping.INDEX_DOC, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());

	//
	// meta-folder-index property
	//

	Assert.assertEquals(SOURCE_ID, wrapper.getSourceId().get());

	Assert.assertEquals(//
		IndexData.encode(FolderEntry.of(doc.getDocument())), wrapper.getIndexDoc().get());

	Assert.assertTrue(wrapper.getHarvestingProperties().isEmpty());
	Assert.assertTrue(wrapper.getWarnReport().isEmpty());
	Assert.assertTrue(wrapper.getErrorsReport().isEmpty());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = OpenSearchMetaFolder_harvestingPropertiesTest.getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(SOURCE_ID);

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_PREFIX);

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX.substring(1), doc.getDataFolder());

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX, doc.getDataFolderPrefix());

	String key = SOURCE_ID + SourceStorageWorker.DATA_1_PREFIX;

	//
	//
	//

	folder.store(key, FolderEntry.of(doc.getDocument()), EntryType.DATA_FOLDER_INDEX_DOC);

	//
	//
	//

	Node node = folder.get(key);

	DataFolderIndexDocument doc2 = new DataFolderIndexDocument((Document) node);

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX.substring(1), doc2.getDataFolder());

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX, doc2.getDataFolderPrefix());

	//
	// trying to get a doc as a binary always works
	//

	InputStream binary = folder.getBinary(key);

	DataFolderIndexDocument doc3 = new DataFolderIndexDocument(binary);

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX.substring(1), doc3.getDataFolder());

	Assert.assertEquals(SourceStorageWorker.DATA_1_PREFIX, doc3.getDataFolderPrefix());
    }
}
