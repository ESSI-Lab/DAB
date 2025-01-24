/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 */
@Ignore
public class OpenSearchDataFolder_GSResourceFromOAIListRecordsTest extends OpenSearchTest {

    @Test
    public void test() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = "test" + SourceStorageWorker.DATA_1_POSTFIX;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	Downloader downloader = new Downloader();

	Optional<InputStream> stream = downloader
		.downloadOptionalStream("https://gs-service-production.geodab.eu/gs-service/services/oaipmh?verb=ListSets");
	XMLDocumentReader doc = new XMLDocumentReader(stream.get());
	List<String> sets = doc.evaluateTextContent("//*:setSpec/text()");

	int emptySets = 0;

	for (int i = 0; i < sets.size(); i++) {

	    String set = sets.get(i);

	    GSLoggerFactory.getLogger(getClass()).info("SET [{}/{}] '{}' STARTED", (i + 1), sets.size(), set);

	    stream = downloader.downloadOptionalStream(//
		    "https://gs-service-production.geodab.eu/gs-service/services/oaipmh2?verb=ListRecords&set=" + set
			    + "&metadataPrefix=ISO19139-2006-GMI", //
		    HttpHeaderUtils.build("client_dentifier", "ESSILabClient"));

	    doc = new XMLDocumentReader(stream.get());
	    Node node = doc.evaluateNode("(//*:MI_Metadata)[1]");

	    if (node == null) {
		emptySets++;
		continue;
	    }

	    MIMetadata miMetadata = new MIMetadata(node);
	    Dataset dataset = new Dataset();
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    dataset.setPrivateId(UUID.randomUUID().toString());
	    dataset.setOriginalId(UUID.randomUUID().toString());

	    GSSource source = new GSSource();
	    source.setUniqueIdentifier(set);

	    dataset.setSource(source);

	    //
	    //
	    //

	    IndexedElementsWriter.write(dataset);

	    //
	    //
	    //

	    String key = dataset.getPrivateId();

	    //
	    //
	    //

	    if (!folder.store(key, //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE)) {

		throw new Exception("Not stored!");
	    }

	    //
	    //
	    //

	    SourceWrapper wrapper = folder.getSourceWrapper(key);

	    // System.out.println(wrapper.toStringHideBinary());

	    //
	    //
	    //

	    //
	    //
	    //

	    TestUtils.compareResources(wrapper, dataset, folder, key);

	    GSLoggerFactory.getLogger(getClass()).info("SET [{}/{}] '{}' ENDED", (i + 1), sets.size(), set);
	}

	GSLoggerFactory.getLogger(getClass()).info("Total sets: {}", sets.size());
	GSLoggerFactory.getLogger(getClass()).info("Empty sets: {}", emptySets);
	GSLoggerFactory.getLogger(getClass()).info("Stored entries: {}", folder.size());

	Assert.assertEquals(Integer.valueOf(folder.size()), Integer.valueOf(sets.size() - emptySets));
    }
}
