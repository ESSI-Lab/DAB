/**
 * 
 */
package eu.essi_lab.api.database.opensearch.metafolder.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.datafolder.test.TestUtils;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class OpenSearchMetaFolder_errorsReportTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getMetaFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String errorsReport = //
		"Error id: NO_LIST_RECORDS_ERROR\r\n" + //
			"Error msg: Source URL [http://hydrolite.ddns.net/niger/hsl-yb/index.php/default/services/cuahsi_1_1.asmx?WSDL]";

	String key = "errorsReport";

	//
	//
	//

	Assert.assertTrue(folder.store(key, //
		FolderEntry.of(IOStreamUtils.asStream(errorsReport)), //
		EntryType.HARVESTING_ERROR_REPORT));

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

	Assert.assertEquals(MetaFolderMapping.ERRORS_REPORT, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	Assert.assertEquals(wrapper.getErrorsReport().get(), wrapper.getBinaryValue());

	//
	// meta-folder-index property
	//

	Assert.assertEquals(TestUtils.SOURCE_ID, wrapper.getSourceId().get());

	Assert.assertEquals(//
		OpenSearchUtils.encode(FolderEntry.of(IOStreamUtils.asStream(errorsReport))), wrapper.getErrorsReport().get());

	Assert.assertTrue(wrapper.getHarvestingProperties().isEmpty());
	Assert.assertTrue(wrapper.getWarnReport().isEmpty());
	Assert.assertTrue(wrapper.getIndexDoc().isEmpty());
	Assert.assertTrue(wrapper.getDataFolder().isEmpty());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getMetaFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String errorsReport = //
		"Error id: NO_LIST_RECORDS_ERROR\r\n" + //
			"Error msg: Source URL [http://hydrolite.ddns.net/niger/hsl-yb/index.php/default/services/cuahsi_1_1.asmx?WSDL]";

	String key = "errorsReport";

	//
	//
	//

	folder.store(key, FolderEntry.of(IOStreamUtils.asStream(errorsReport)), EntryType.HARVESTING_ERROR_REPORT);

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	String retrievedReport = IOStreamUtils.asUTF8String(binary);

	Assert.assertEquals(errorsReport, retrievedReport);

	//
	// trying to get a binary as a doc return null
	//

	Assert.assertNull(folder.get(key));
	
	//
	//
	//

	Assert.assertEquals(0, folder.listIdentifiers(IdentifierType.ORIGINAL).size());
	Assert.assertEquals(0, folder.listIdentifiers(IdentifierType.PRIVATE).size());
	Assert.assertEquals(0, folder.listIdentifiers(IdentifierType.PUBLIC).size());
	Assert.assertEquals(0, folder.listIdentifiers(IdentifierType.OAI_HEADER).size());

    }
}
