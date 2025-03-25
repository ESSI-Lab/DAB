/**
 * 
 */
package eu.essi_lab.api.database.opensearch.test;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.datafolder.test.TestUtils;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class OpenSearchFolderTest extends OpenSearchTest {

    /**
     * @param wrapper
     * @param folder
     * @return
     * @throws Exception
     */
    public static List<String> listEntryIdentifiers(OpenSearchFolder folder) throws Exception {

	String index = IndexData.detectIndex(folder);

	Query searchQuery = OpenSearchQueryBuilder.buildFolderEntriesQuery(folder);

	return folder.getWrapper().searchField(//
		index, //
		searchQuery, //
		IndexData.ENTRY_ID);//
    }

    @Test
    public void sizeTooBigTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	Assert.assertThrows(OpenSearchException.class, () -> {

	    folder.getWrapper().searchField(//
		    ViewsMapping.get().getIndex(), //
		    OpenSearchQueryBuilder.buildMatchAllQuery(), //
		    ViewsMapping.VIEW_ID, //
		    0, //
		    Integer.MAX_VALUE);
	});

    }

    /**
     * Not a test, use it to remove folders
     * 
     * @throws Exception
     */
    public void test() throws Exception {

	StorageInfo info = new StorageInfo();

	info.setIdentifier("identifier");
	info.setName("identifier");
	info.setUser("user");
	info.setPassword("password");
	info.setUri("https://");
	info.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());

	OpenSearchDatabase osDB_ = new OpenSearchDatabase();
	osDB_.initialize(info);

	osDB_.removeFolder("foldername");
    }

    @Test
    public void testWithBinary() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	View view1 = new View();
	view1.setId("viewId");
	view1.setCreator("Creator1");

	View view2 = new View();
	view2.setId("viewId");
	view2.setCreator("Creator2");

	String key = view1.getId();

	//
	//
	//

	Assert.assertFalse(folder.remove(key));
	Assert.assertFalse(folder.exists(key));

	assertEmpty(folder, key, view1, view2);

	Assert.assertTrue(store(folder, view1, key));
	Assert.assertFalse(store(folder, view1, key));

	Assert.assertNull(folder.get(key));// not a document
	Assert.assertNotNull(folder.getBinary(key));
	Assert.assertTrue(folder.exists(key));

	Assert.assertEquals("Creator1", View.fromStream(folder.getBinary(key)).getCreator());

	Assert.assertEquals(1, folder.size());
	Assert.assertEquals(1, folder.listKeys().length);
	Assert.assertEquals(1, listEntryIdentifiers(folder).size());

	Assert.assertTrue(replace(folder, view2, key));
	Assert.assertEquals(1, folder.size());
	Assert.assertEquals(1, folder.listKeys().length);
	Assert.assertEquals(1, listEntryIdentifiers(folder).size());

	Assert.assertEquals("Creator2", View.fromStream(folder.getBinary(key)).getCreator());

	Assert.assertTrue(folder.remove(key));

	assertEmpty(folder, key, view1, view2);

	Assert.assertTrue(store(folder, view1, key));
	Assert.assertFalse(store(folder, view1, key));

	folder.clear();

	assertEmpty(folder, key, view1, view2);
    }

    @Test
    public void testWithDoc() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getMetaFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(TestUtils.SOURCE_ID);

	DataFolderIndexDocument doc1 = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_POSTFIX);

	DataFolderIndexDocument doc2 = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_2_POSTFIX);

	String key = UUID.randomUUID().toString();

	//
	//
	//

	Assert.assertFalse(folder.remove(key));
	Assert.assertFalse(folder.exists(key));

	assertEmpty(folder, key, doc1, doc2);

	Assert.assertTrue(store(folder, doc1, key));
	Assert.assertFalse(store(folder, doc1, key));

	Assert.assertNotNull(folder.get(key));
	Assert.assertNotNull(folder.getBinary(key));
	Assert.assertTrue(folder.exists(key));

	Assert.assertEquals(SourceStorageWorker.DATA_1_POSTFIX, new DataFolderIndexDocument(folder.getBinary(key)).getDataFolderPrefix());

	Assert.assertEquals(1, folder.size());
	Assert.assertEquals(1, folder.listKeys().length);
	Assert.assertEquals(1, listEntryIdentifiers(folder).size());

	Assert.assertTrue(replace(folder, doc2, key));
	Assert.assertEquals(1, folder.size());
	Assert.assertEquals(1, folder.listKeys().length);
	Assert.assertEquals(1, listEntryIdentifiers(folder).size());

	Assert.assertEquals(SourceStorageWorker.DATA_2_POSTFIX, new DataFolderIndexDocument(folder.getBinary(key)).getDataFolderPrefix());

	Assert.assertTrue(folder.remove(key));

	assertEmpty(folder, key, doc1, doc2);

	Assert.assertTrue(store(folder, doc1, key));
	Assert.assertFalse(store(folder, doc1, key));

	folder.clear();

	assertEmpty(folder, key, doc1, doc2);
    }

    /**
     * @param folder
     * @param key
     * @param view
     * @throws Exception
     */
    private void assertEmpty(OpenSearchFolder folder, //
	    String key, //
	    DataFolderIndexDocument doc1, //
	    DataFolderIndexDocument doc2) throws Exception {

	Assert.assertNull(folder.get(key));
	Assert.assertNull(folder.getBinary(key));
	Assert.assertFalse(folder.exists(key));
	Assert.assertFalse(folder.remove(key));
	Assert.assertFalse(replace(folder, doc1, key));
	Assert.assertFalse(replace(folder, doc2, key));

	Assert.assertEquals(0, folder.size());
	Assert.assertEquals(0, folder.listKeys().length);
	Assert.assertEquals(0, listEntryIdentifiers(folder).size());
    }

    /**
     * @param folder
     * @param key
     * @param view
     * @throws Exception
     */
    private void assertEmpty(OpenSearchFolder folder, String key, View view1, View view2) throws Exception {

	Assert.assertNull(folder.get(key));
	Assert.assertNull(folder.getBinary(key));
	Assert.assertFalse(folder.exists(key));
	Assert.assertFalse(folder.remove(key));
	Assert.assertFalse(replace(folder, view1, key));
	Assert.assertFalse(replace(folder, view2, key));

	Assert.assertEquals(0, folder.size());
	Assert.assertEquals(0, folder.listKeys().length);
	Assert.assertEquals(0, listEntryIdentifiers(folder).size());
    }

    /**
     * @param folder
     * @param view
     * @param key
     * @return
     * @throws JAXBException
     * @throws Exception
     */
    private boolean store(OpenSearchFolder folder, View view, String key) throws JAXBException, Exception {

	return folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW);
    }

    /**
     * @param folder
     * @param view
     * @param key
     * @return
     * @throws JAXBException
     * @throws Exception
     */
    private boolean replace(OpenSearchFolder folder, View view, String key) throws JAXBException, Exception {

	return folder.replace(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW);
    }

    /**
     * @param folder
     * @param doc
     * @param key
     * @return
     * @throws JAXBException
     * @throws Exception
     */
    private boolean replace(OpenSearchFolder folder, DataFolderIndexDocument doc, String key) throws JAXBException, Exception {

	return folder.replace(//
		key, //
		FolderEntry.of(doc.getDocument()), //
		EntryType.DATA_FOLDER_INDEX_DOC);
    }

    /**
     * @param folder
     * @param doc
     * @param key
     * @return
     * @throws JAXBException
     * @throws Exception
     */
    private boolean store(OpenSearchFolder folder, DataFolderIndexDocument doc, String key) throws JAXBException, Exception {

	return folder.store(//
		key, //
		FolderEntry.of(doc.getDocument()), //
		EntryType.DATA_FOLDER_INDEX_DOC);
    }
}
