/**
 * 
 */
package eu.essi_lab.api.database.opensearch.multiplefolders.test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 */
public class MultipleFoldersRemoveAndClearTest extends OpenSearchTest {

    @Test
    public void test() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	//
	//
	//

	int dataFolder1Size = 3;
	int dataFolder2Size = 5;
	int dataFolder3Size = 1;
	int dataFolder4Size = 2;

	OpenSearchFolder dataFolder1 = createDataFolder(database, "source1", dataFolder1Size);

	OpenSearchFolder dataFolder2 = createDataFolder(database, "source2", dataFolder2Size);

	OpenSearchFolder dataFolder3 = createDataFolder(database, "source3", dataFolder3Size);

	OpenSearchFolder dataFolder4 = createDataFolder(database, "source4", dataFolder4Size);

	Assert.assertEquals(Integer.valueOf(dataFolder1Size), Integer.valueOf(dataFolder1.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder2Size), Integer.valueOf(dataFolder2.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder3Size), Integer.valueOf(dataFolder3.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder4Size), Integer.valueOf(dataFolder4.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder1.listIds().size()), Integer.valueOf(dataFolder1.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder2.listIds().size()), Integer.valueOf(dataFolder2.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder3.listIds().size()), Integer.valueOf(dataFolder3.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder4.listIds().size()), Integer.valueOf(dataFolder4.listKeys().length));

	Assert.assertEquals(Integer.valueOf(dataFolder1Size), Integer.valueOf(dataFolder1.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder2Size), Integer.valueOf(dataFolder2.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder3Size), Integer.valueOf(dataFolder3.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder4Size), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	int metaFolder1Size = 1;
	int metaFolder2Size = 2;
	int metaFolder3Size = 3;
	int metaFolder4Size = 4;

	OpenSearchFolder metaFolder1 = createMetaFolder(database, "source1", metaFolder1Size);

	OpenSearchFolder metaFolder2 = createMetaFolder(database, "source2", metaFolder2Size);

	OpenSearchFolder metaFolder3 = createMetaFolder(database, "source3", metaFolder3Size);

	OpenSearchFolder metaFolder4 = createMetaFolder(database, "source4", metaFolder4Size);

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder1.listIds().size()), Integer.valueOf(metaFolder1.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder2.listIds().size()), Integer.valueOf(metaFolder2.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder3.listIds().size()), Integer.valueOf(metaFolder3.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder4.listIds().size()), Integer.valueOf(metaFolder4.listKeys().length));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	List<String> dataFolder1ids = dataFolder1.listIds();
	List<String> dataFolder2ids = dataFolder2.listIds();
	List<String> dataFolder3ids = dataFolder3.listIds();
	List<String> dataFolder4ids = dataFolder4.listIds();

	List<String> metaFolder1ids = metaFolder1.listIds();
	List<String> metaFolder2ids = metaFolder2.listIds();
	List<String> metaFolder3ids = metaFolder3.listIds();
	List<String> metaFolder4ids = metaFolder4.listIds();

	//
	//
	//

	Assert.assertTrue(allMatchTest(dataFolder1, dataFolder1ids));

	Assert.assertFalse(noneMatchTest(dataFolder1, dataFolder1ids));

	Assert.assertTrue(noneMatchTest(dataFolder1, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder1, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder1, dataFolder4ids));

	Assert.assertTrue(noneMatchTest(dataFolder1, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder1, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder1, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder1, metaFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(dataFolder2, dataFolder2ids));

	Assert.assertFalse(noneMatchTest(dataFolder2, dataFolder2ids));

	Assert.assertTrue(noneMatchTest(dataFolder2, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder2, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder2, dataFolder4ids));

	Assert.assertTrue(noneMatchTest(dataFolder2, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder2, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder2, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder2, metaFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(dataFolder3, dataFolder3ids));

	Assert.assertFalse(noneMatchTest(dataFolder3, dataFolder3ids));

	Assert.assertTrue(noneMatchTest(dataFolder3, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder3, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder3, dataFolder4ids));

	Assert.assertTrue(noneMatchTest(dataFolder3, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder3, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder3, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder3, metaFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(dataFolder4, dataFolder4ids));

	Assert.assertFalse(noneMatchTest(dataFolder4, dataFolder4ids));

	Assert.assertTrue(noneMatchTest(dataFolder4, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder4, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder4, dataFolder3ids));

	Assert.assertTrue(noneMatchTest(dataFolder4, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(dataFolder4, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(dataFolder4, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(dataFolder4, metaFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(metaFolder1, metaFolder1ids));

	Assert.assertFalse(noneMatchTest(metaFolder1, metaFolder1ids));

	Assert.assertTrue(noneMatchTest(metaFolder1, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder1, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder1, metaFolder4ids));

	Assert.assertTrue(noneMatchTest(metaFolder1, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder1, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder1, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder1, dataFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(metaFolder2, metaFolder2ids));

	Assert.assertFalse(noneMatchTest(metaFolder2, metaFolder2ids));

	Assert.assertTrue(noneMatchTest(metaFolder2, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder2, metaFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder2, metaFolder4ids));

	Assert.assertTrue(noneMatchTest(metaFolder2, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder2, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder2, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder2, dataFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(metaFolder3, metaFolder3ids));

	Assert.assertFalse(noneMatchTest(metaFolder3, metaFolder3ids));

	Assert.assertTrue(noneMatchTest(metaFolder3, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder3, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder3, metaFolder4ids));

	Assert.assertTrue(noneMatchTest(metaFolder3, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder3, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder3, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder3, dataFolder4ids));

	//
	//
	//

	Assert.assertTrue(allMatchTest(metaFolder4, metaFolder4ids));

	Assert.assertFalse(noneMatchTest(metaFolder4, metaFolder4ids));

	Assert.assertTrue(noneMatchTest(metaFolder4, metaFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder4, metaFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder4, metaFolder3ids));

	Assert.assertTrue(noneMatchTest(metaFolder4, dataFolder1ids));
	Assert.assertTrue(noneMatchTest(metaFolder4, dataFolder2ids));
	Assert.assertTrue(noneMatchTest(metaFolder4, dataFolder3ids));
	Assert.assertTrue(noneMatchTest(metaFolder4, dataFolder4ids));

	//
	//
	//

	dataFolder1.remove(dataFolder1.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder2.remove(dataFolder2.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder3.remove(dataFolder3.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder4.remove(dataFolder4.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	metaFolder1.remove(metaFolder1.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder2.remove(metaFolder2.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder3.remove(metaFolder3.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder4.remove(metaFolder4.listKeys()[0]);

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder1Size - 1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	dataFolder1.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.listKeys().length));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.listIds().size()));

	Assert.assertEquals(Integer.valueOf(dataFolder2Size - 1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder2.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder2.listKeys().length));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder2.listIds().size()));

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder3Size - 1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder3.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder3.listKeys().length));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder3.listIds().size()));

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder2.size()));

	Assert.assertEquals(Integer.valueOf(dataFolder4Size - 1), Integer.valueOf(dataFolder4.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	dataFolder4.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder4.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder4.listKeys().length));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder4.listIds().size()));

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(dataFolder3.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder1Size - 1), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	//
	//
	//

	storeDataset(dataFolder1);
	storeDataset(dataFolder2);
	storeDataset(dataFolder3);
	storeDataset(dataFolder4);

	//
	//
	//

	metaFolder1.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder1.size()));

	Assert.assertEquals(Integer.valueOf(metaFolder2Size - 1), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder2.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder3Size - 1), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder3.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(metaFolder4Size - 1), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder4.size()));

	//
	//
	//

	metaFolder4.clear();

	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder1.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder2.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder3.size()));
	Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(metaFolder4.size()));

	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder1.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder2.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder3.size()));
	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(dataFolder4.size()));
    }

    /**
     * @param folder
     * @param ids
     * @return
     * @return
     */
    private boolean allMatchTest(OpenSearchFolder folder, List<String> ids) {

	return ids.stream().allMatch(key -> {
	    try {
		return folder.listIds().contains(key);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    return false;
	});
    }

    /**
     * @param folder
     * @param ids
     * @return
     */
    private boolean noneMatchTest(OpenSearchFolder folder, List<String> ids) {

	return ids.stream().noneMatch(key -> {
	    try {
		return folder.listIds().contains(key);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    return false;
	});
    }

    /*
     * 
     */
    private OpenSearchFolder createMetaFolder(//
	    OpenSearchDatabase database, //
	    String sourceId, //
	    int entriesCount) throws Exception {

	String folderName = sourceId + SourceStorageWorker.META_PREFIX;//

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String indexName = SourceStorageWorker.createDataFolderIndexName(sourceId);

	DataFolderIndexDocument indexDoc = new DataFolderIndexDocument(//
		indexName, //
		SourceStorageWorker.DATA_1_PREFIX);

	String indexDocKey = sourceId + SourceStorageWorker.DATA_1_PREFIX;

	folder.store(//
		indexDocKey, //
		FolderEntry.of(indexDoc.getDocument()), //
		EntryType.DATA_FOLDER_INDEX_DOC);

	//
	//
	//

	HarvestingProperties properties = new HarvestingProperties();
	properties.put("key", "value");
	properties.put("key1", "value1");
	properties.put("key2", "value2");

	String harvPropKey = "harvesting.properties";

	//
	//
	//

	String errorsReport = //
		"Error id: NO_LIST_RECORDS_ERROR\r\n" + //
			"Error msg: Source URL [http://hydrolite.ddns.net/niger/hsl-yb/index.php/default/services/cuahsi_1_1.asmx?WSDL]";

	String errorsReportKey = "errorsReport";

	//
	//
	//

	String warnReport = //
		"Error id: NO_LIST_RECORDS_ERROR\r\n" + //
			"Error msg: Source URL [http://hydrolite.ddns.net/niger/hsl-yb/index.php/default/services/cuahsi_1_1.asmx?WSDL]";

	String warnReportKey = "warnReport";

	//
	//
	//

	switch (entriesCount) {

	case 2:

	    folder.store(//
		    harvPropKey, //
		    FolderEntry.of(properties.asStream()), //
		    EntryType.HARVESTING_PROPERTIES);

	    break;

	case 3:

	    folder.store(//
		    harvPropKey, //
		    FolderEntry.of(properties.asStream()), //
		    EntryType.HARVESTING_PROPERTIES);

	    folder.store(errorsReportKey, //
		    FolderEntry.of(IOStreamUtils.asStream(errorsReport)), //
		    EntryType.HARVESTING_ERROR_REPORT);

	    break;

	case 4:

	    folder.store(//
		    harvPropKey, //
		    FolderEntry.of(properties.asStream()), //
		    EntryType.HARVESTING_PROPERTIES);

	    folder.store(errorsReportKey, //
		    FolderEntry.of(IOStreamUtils.asStream(errorsReport)), //
		    EntryType.HARVESTING_ERROR_REPORT);

	    folder.store(warnReportKey, //
		    FolderEntry.of(IOStreamUtils.asStream(warnReport)), //
		    EntryType.HARVESTING_WARN_REPORT);

	}

	return folder;
    }

    /*
     * 
     */
    private OpenSearchFolder createDataFolder(//
	    OpenSearchDatabase database, //
	    String sourceId, //
	    int entriesCount) throws Exception {

	String folderName = sourceId + SourceStorageWorker.DATA_1_PREFIX;//

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	for (int i = 0; i < entriesCount; i++) {

	    String privateId = UUID.randomUUID().toString();

	    Dataset dataset = new Dataset();
	    dataset.setPrivateId(privateId);
	    dataset.setOriginalId(UUID.randomUUID().toString());
	    dataset.setPublicId(UUID.randomUUID().toString());

	    IndexedElementsWriter.write(dataset);

	    folder.store(privateId, //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE);
	}

	return folder;
    }

    /**
     * @param folder
     * @throws ParserConfigurationException
     * @throws JAXBException
     * @throws SAXException
     * @throws IOException
     * @throws Exception
     */
    private void storeDataset(OpenSearchFolder folder) throws Exception {

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());

	IndexedElementsWriter.write(dataset);

	folder.store(privateId, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);
    }
}
