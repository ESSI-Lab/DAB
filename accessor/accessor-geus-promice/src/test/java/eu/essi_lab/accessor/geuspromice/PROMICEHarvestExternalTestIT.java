package eu.essi_lab.accessor.geuspromice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.thredds.THREDDSDataset;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Lists the site files of the daily L3 catalog, harvests the first site, maps its records and downloads the last
 * month of air temperature
 */
public class PROMICEHarvestExternalTestIT {

    @Test
    public void testHarvestMapAndDownload() throws Exception {

	List<THREDDSDataset> datasets = new PROMICEClient(PROMICEClient.DEFAULT_ENDPOINT).listDatasets();
	assertTrue(datasets.size() > 40);

	PROMICEConnector connector = new PROMICEConnector();
	connector.setSourceURL(PROMICEClient.DEFAULT_ENDPOINT);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	assertEquals("1", response.getResumptionToken());

	GSSource source = new GSSource();
	source.setEndpoint(PROMICEClient.DEFAULT_ENDPOINT);

	PROMICEMapper mapper = new PROMICEMapper();
	GSResource airTemperature = null;
	int count = 0;

	Iterator<OriginalMetadata> records = response.getRecords();
	while (records.hasNext()) {
	    GSResource resource = mapper.map(records.next(), source);
	    System.out.println("MAPPED " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	    if (resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName().equals("t_u")) {
		airTemperature = resource;
	    }
	    count++;
	}

	assertTrue(count > 5);
	assertNotNull(airTemperature);

	Online online = airTemperature.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	PROMICEDownloader downloader = new PROMICEDownloader();
	downloader.setOnlineResource(airTemperature, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - 30l * 24 * 60 * 60 * 1000), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 400)));
    }
}
