package eu.essi_lab.accessor.usnrcsawdb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.Iterator;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Harvests the first page of records, maps them and downloads one month of data of a daily snow water equivalent
 * series
 */
public class NRCSAWDBHarvestExternalTestIT {

    @Test
    public void testHarvestMapAndDownload() throws Exception {

	NRCSAWDBConnector connector = new NRCSAWDBConnector();
	connector.setSourceURL(NRCSAWDBClient.DEFAULT_ENDPOINT);
	connector.getSetting().setMaxRecords(50);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());

	GSSource source = new GSSource();
	source.setEndpoint(NRCSAWDBClient.DEFAULT_ENDPOINT);

	NRCSAWDBMapper mapper = new NRCSAWDBMapper();
	GSResource swe = null;
	int count = 0;

	Iterator<OriginalMetadata> records = response.getRecords();
	while (records.hasNext()) {
	    GSResource resource = mapper.map(records.next(), source);
	    assertNotNull(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	    String name = resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName();
	    if (swe == null && name.contains("/WTEQ:") && name.endsWith("/DAILY")) {
		swe = resource;
	    }
	    count++;
	}

	assertTrue(count > 0);
	assertNotNull(swe);

	Online online = swe.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	NRCSAWDBDownloader downloader = new NRCSAWDBDownloader();
	downloader.setOnlineResource(swe, online.getIdentifier());
	assertTrue(downloader.canDownload());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	// last 30 days of the series
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - 30l * 24 * 60 * 60 * 1000), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains("<value") || wml.contains(":value"));
    }
}
