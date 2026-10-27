package eu.essi_lab.accessor.enhydris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Tests against OpenHi.net
 *
 * @author boldrini
 */
public class EnhydrisExternalTestIT {

    /**
     * Eurotas (NOA), mean stage, initial, 3 hours
     */
    static final String EUROTAS_MEAN_STAGE = "2022/1244/10846";

    @Test
    public void testStations() throws GSException {

	EnhydrisClient client = new EnhydrisClient();

	List<JSONObject> stations = client.retrieveStations();
	assertTrue(stations.size() > 70);
	assertTrue(stations.stream().anyMatch(s -> s.getInt(EnhydrisClient.ID) == 2022));

	assertTrue(client.retrieveVariables().get(14).toString().contains("Stage"));
	assertEquals("m", client.retrieveUnits().get(6).getString(EnhydrisClient.SYMBOL));
	assertTrue(client.retrieveOrganizations().containsKey(11));
    }

    @Test
    public void testExtentAndObservations() throws GSException {

	EnhydrisClient client = new EnhydrisClient();

	SimpleEntry<Date, Date> extent = client.retrieveExtent(EUROTAS_MEAN_STAGE).get();

	// the series starts on 2019-11-12 18:00 UTC
	assertEquals(new Date(1573581600000l), extent.getKey());
	assertTrue(System.currentTimeMillis() - extent.getValue().getTime() < TimeUnit.DAYS.toMillis(30));

	Date end = extent.getValue();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(EnhydrisClient.parseSeriesId(EUROTAS_MEAN_STAGE), begin, end);

	assertTrue(values.size() > 10);
	assertFalse(values.firstKey().before(begin));
	assertEquals(end, values.lastKey());
	// 3 hours time step, in UTC
	assertEquals(0, values.lastKey().getTime() % TimeUnit.HOURS.toMillis(3));
    }

    @Test
    public void testConnectorAndDownload() throws Exception {

	EnhydrisConnector connector = new EnhydrisConnector();
	connector.setSourceURL(EnhydrisClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("2022,1484,1485");

	GSSource source = new GSSource();
	source.setEndpoint(EnhydrisClient.DEFAULT_ENDPOINT);

	EnhydrisMapper mapper = new EnhydrisMapper();

	List<GSResource> resources = new ArrayList<>();
	GSResource eurotas = null;

	String token = null;
	do {
	    ListRecordsRequest request = new ListRecordsRequest();
	    request.setResumptionToken(token);

	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    for (OriginalMetadata record : response.getRecordsAsList()) {

		GSResource resource = mapper.map(record, source);
		System.out.println("MAPPED " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle() + " ["
			+ resource.getExtensionHandler().getAttributeUnitsAbbreviation().orElse("") + ", "
			+ resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition() + "]");
		resources.add(resource);

		if (resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getName().equals(EUROTAS_MEAN_STAGE)) {
		    eurotas = resource;
		}
	    }
	    token = response.getResumptionToken();

	} while (token != null);

	assertTrue(resources.size() >= 3);
	assertEquals(resources.size(), resources.stream().map(GSResource::getOriginalId).distinct().count());
	assertNotNull(eurotas);
	assertTrue(eurotas.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().isEndPositionIndeterminate());

	Online online = eurotas.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	EnhydrisDownloader downloader = new EnhydrisDownloader();
	downloader.setOnlineResource(eurotas, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(1)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 400)));
    }

    @Test
    public void testOwnerFilter() throws GSException {

	EnhydrisConnector connector = new EnhydrisConnector();
	connector.setSourceURL(EnhydrisClient.DEFAULT_ENDPOINT);
	// HCMR
	connector.getSetting().setOwners("11");
	connector.getSetting().setMaxRecords(5);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	assertFalse(response.getRecordsAsList().isEmpty());
	for (OriginalMetadata record : response.getRecordsAsList()) {
	    assertEquals(11, new EnhydrisEntity(record.getMetadata()).getObject().getInt(EnhydrisClient.OWNER_ID));
	}
    }
}
