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
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.datafolder.test.TestUtils;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;

/**
 * @author Fabrizio
 */
public class OpenSearchMetaFolder_indexDocTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getMetaFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(TestUtils.SOURCE_ID);

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_POSTFIX);

	String key = TestUtils.SOURCE_ID + SourceStorageWorker.DATA_1_POSTFIX;

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(doc.getDocument()), //
		EntryType.DATA_FOLDER_INDEX_DOC));

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(MetaFolderMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folder.getName(), wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(MetaFolderMapping.INDEX_DOC, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());

	Assert.assertEquals(wrapper.getIndexDoc().get(), wrapper.getBinaryValue());

	//
	// meta-folder-index property
	//

	Assert.assertEquals(TestUtils.SOURCE_ID, wrapper.getSourceId().get());

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX.substring(1), wrapper.getDataFolder().get());

	Assert.assertEquals(//
		OpenSearchUtils.encode(FolderEntry.of(doc.getDocument())), wrapper.getIndexDoc().get());

	Assert.assertTrue(wrapper.getHarvestingProperties().isEmpty());
	Assert.assertTrue(wrapper.getWarnReport().isEmpty());
	Assert.assertTrue(wrapper.getErrorsReport().isEmpty());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getMetaFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(TestUtils.SOURCE_ID);

	DataFolderIndexDocument doc = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_POSTFIX);

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX.substring(1), doc.getShortDataFolderPostfix());

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX, doc.getDataFolderPrefix());

	String key = TestUtils.SOURCE_ID + SourceStorageWorker.DATA_1_POSTFIX;

	//
	//
	//

	folder.store(key, FolderEntry.of(doc.getDocument()), EntryType.DATA_FOLDER_INDEX_DOC);

	//
	//
	//

	Node node = folder.get(key);

	DataFolderIndexDocument doc2 = new DataFolderIndexDocument((Document) node);

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX.substring(1), doc2.getShortDataFolderPostfix());

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX, doc2.getDataFolderPrefix());

	//
	// trying to get a doc as a binary always works
	//

	InputStream binary = folder.getBinary(key);

	DataFolderIndexDocument doc3 = new DataFolderIndexDocument(binary);

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX.substring(1), doc3.getShortDataFolderPostfix());

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX, doc3.getDataFolderPrefix());
    }
}
