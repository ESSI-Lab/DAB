package eu.essi_lab.accessor.chslf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Harvests the first page of records, maps them and downloads the data of the first one
 */
public class SLFHarvestExternalTestIT {

    @Test
    public void testHarvestMapAndDownload() throws Exception {

	SLFConnector connector = new SLFConnector();
	connector.setSourceURL(SLFClient.DEFAULT_ENDPOINT);
	connector.getSetting().setMaxRecords(20);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());

	GSSource source = new GSSource();
	source.setEndpoint(SLFClient.DEFAULT_ENDPOINT);

	SLFMapper mapper = new SLFMapper();
	GSResource first = null;
	int count = 0;

	Iterator<OriginalMetadata> records = response.getRecords();
	while (records.hasNext()) {
	    GSResource resource = mapper.map(records.next(), source);
	    assertNotNull(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	    if (first == null) {
		first = resource;
	    }
	    count++;
	}

	assertTrue(count > 0);

	Online online = first.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	SLFDownloader downloader = new SLFDownloader();
	downloader.setOnlineResource(first, online.getIdentifier());
	assertTrue(downloader.canDownload());

	List<DataDescriptor> descriptors = downloader.getRemoteDescriptors();
	File file = downloader.download(descriptors.get(0));
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains("<value") || wml.contains(":value"));
    }
}
