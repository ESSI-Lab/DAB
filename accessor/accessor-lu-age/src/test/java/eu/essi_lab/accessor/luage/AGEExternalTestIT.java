package eu.essi_lab.accessor.luage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class AGEExternalTestIT {

    @Test
    public void testStationPages() throws GSException {

	List<String> pages = new AGEClient().retrieveStationPages();
	assertTrue(pages.size() > 35);
	assertTrue(pages.contains("fr/alzette/alzette/hesperange.html"));
    }

    @Test
    public void testStation() throws GSException {

	JSONObject station = new AGEClient().retrieveStation("fr/sure/our/gemund.html").get();

	assertEquals("Gemünd_Our", station.getString(AGEClient.CODE));
	assertEquals("Our", station.getString(AGEClient.WATER_COURSE));
	assertTrue(station.has(AGEClient.LAT));
    }

    @Test
    public void testSeriesAndObservations() throws GSException {

	AGEClient client = new AGEClient();

	JSONObject station = client.retrieveStation("fr/alzette/alzette/ettelbruck-alzette.html").get();

	List<AGEEntity> series = client.retrieveSeries(station);
	assertEquals(1, series.size());
	assertEquals("Ettelbrück-Alzette|W", series.get(0).getSeriesId());
	assertEquals("cm", series.get(0).getUnit());

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(AGEClient.parseSeriesId("Ettelbrück-Alzette|W"), begin, end);

	assertTrue(values.size() > 4 * 24);
	assertFalse(values.firstKey().before(begin));
	assertTrue(end.getTime() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));

	// older than the published window
	assertTrue(client.retrieveObservations(AGEClient.parseSeriesId("Ettelbrück-Alzette|W"), new Date(0), new Date(1000)).isEmpty());
    }

    @Test
    public void testConnector() throws GSException {

	AGEConnector connector = new AGEConnector();
	connector.setSourceURL(AGEClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("Hesperange,SN_Remich,Esch-Sure,Perl");

	List<String> ids = new ArrayList<>();

	String token = null;
	do {
	    ListRecordsRequest request = new ListRecordsRequest();
	    request.setResumptionToken(token);

	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    for (OriginalMetadata record : response.getRecordsAsList()) {
		AGEEntity entity = new AGEEntity(record.getMetadata());
		assertTrue(entity.getObject().has(AGEClient.BEGIN));
		ids.add(entity.getSeriesId());
	    }
	    token = response.getResumptionToken();

	} while (token != null);

	// SN_Remich is operated by the Service de la navigation fluviale
	assertEquals(ids.stream().sorted().collect(Collectors.toList()).toString(), 2, ids.size());
	assertTrue(ids.contains("Hesperange|W"));
	assertTrue(ids.contains("Esch-Sure|W"));
    }
}
