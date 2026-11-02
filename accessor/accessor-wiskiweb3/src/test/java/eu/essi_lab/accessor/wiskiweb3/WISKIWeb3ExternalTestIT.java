package eu.essi_lab.accessor.wiskiweb3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Live tests with the Hesse (HLNUG) site and with the North Rhine-Westphalia (LANUV) site
 *
 * @author boldrini
 */
public class WISKIWeb3ExternalTestIT {

    static final String NRW_ENDPOINT = "https://www.hochwasserportal.nrw/data/";

    private static final String WATER_LEVEL = "0/23940359/W|week|15m.Cmd.RelAbs.P";
    private static final String DAILY_WATER_LEVEL = "0/23940359/W|year|Day.Mean";

    @Test
    public void testSeries() throws GSException {

	WISKIWeb3Client client = new WISKIWeb3Client();

	List<WISKIWeb3Entity> series = client.retrieveSeries(Arrays.asList(), Arrays.asList(WISKIWeb3Client.WEEK_FILE));
	GSLoggerFactory.getLogger(getClass()).info("Hesse week series: {}", series.size());
	assertTrue(series.size() > 400);

	WISKIWeb3Entity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();
	assertEquals("Fahrenbach", waterLevel.getStationName());

	// precipitation is listed by three layers, a single series is harvested
	assertEquals(1, series.stream().filter(s -> s.getStationPath().equals("0/L121/N")).count());
    }

    @Test
    public void testExtent() throws GSException {

	WISKIWeb3Client client = new WISKIWeb3Client();

	SimpleEntry<Date, Date> week = client.retrieveExtent(WISKIWeb3Client.parseSeriesId(WATER_LEVEL)).get();
	assertTrue(System.currentTimeMillis() - week.getValue().getTime() < TimeUnit.DAYS.toMillis(2));
	assertTrue(week.getValue().getTime() - week.getKey().getTime() > TimeUnit.DAYS.toMillis(6));

	SimpleEntry<Date, Date> year = client.retrieveExtent(WISKIWeb3Client.parseSeriesId(DAILY_WATER_LEVEL)).get();
	GSLoggerFactory.getLogger(getClass()).info("Daily water level extent: {} {}", year.getKey(), year.getValue());
	assertTrue(year.getKey().before(week.getKey()));
	assertTrue(System.currentTimeMillis() - year.getValue().getTime() < TimeUnit.DAYS.toMillis(5));
    }

    @Test
    public void testObservations() throws GSException {

	WISKIWeb3Client client = new WISKIWeb3Client();

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(WISKIWeb3Client.parseSeriesId(WATER_LEVEL), begin, end);
	GSLoggerFactory.getLogger(getClass()).info("15 minutes values of 2 days: {}", values.size());
	assertTrue(values.size() > 150);
	assertFalse(values.firstKey().before(begin));

	begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(3650));
	values = client.retrieveObservations(WISKIWeb3Client.parseSeriesId(DAILY_WATER_LEVEL), begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Daily values of 10 years: {}", values.size());
	assertTrue(values.size() > 3000);
    }

    @Test
    public void testSiteInfo() {

	WISKIWeb3Client client = new WISKIWeb3Client();
	WISKIWeb3Client.SiteInfo info = client.retrieveSiteInfo(WISKIWeb3Client.getDefaultAppURL(WISKIWeb3Client.DEFAULT_ENDPOINT));

	GSLoggerFactory.getLogger(getClass()).info("Hesse: {} - {}", info.getOrganisation().orElse(null), info.getLicense().orElse(null));
	assertEquals("Hessisches Landesamt für Naturschutz, Umwelt und Geologie", info.getOrganisation().get());
	assertTrue(info.getLicense().get().contains("CC BY 4.0"));
    }

    @Test
    public void testConnector() throws GSException {

	List<OriginalMetadata> records = harvest(WISKIWeb3Client.DEFAULT_ENDPOINT, "23940359,461502,L121", 20);

	List<String> ids = records.stream().map(r -> new WISKIWeb3Entity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(WISKIWeb3Client.BEGIN))).//
		map(WISKIWeb3Entity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested: {}", ids);

	assertTrue(ids.contains(WATER_LEVEL));
	assertTrue(ids.contains(DAILY_WATER_LEVEL));
	assertTrue(ids.contains("0/23940359/Q|week|15m.Cmd.RunOff.P"));
	// spring discharge: only daily values
	assertTrue(ids.contains("0/461502/Schuettung|year|Day.Mean"));
	// precipitation: hourly values, no daily values published
	assertTrue(ids.contains("0/L121/N|week|h.CmdTotal"));
	assertFalse(ids.stream().anyMatch(id -> id.startsWith("0/L121/N|year")));

	// mapping and download
	GSSource source = new GSSource();
	source.setEndpoint(WISKIWeb3Client.DEFAULT_ENDPOINT);

	OriginalMetadata record = records.stream().filter(r -> new WISKIWeb3Entity(r.getMetadata()).getSeriesId().equals(WATER_LEVEL))
		.findFirst().get();
	GSResource resource = new WISKIWeb3Mapper().map(record, source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	GSLoggerFactory.getLogger(getClass()).info("Licence: {}", new WISKIWeb3Entity(record.getMetadata()).getString(WISKIWeb3Client.LICENSE));
    }

    @Test
    public void testNRW() throws GSException {

	WISKIWeb3Client client = new WISKIWeb3Client(NRW_ENDPOINT);
	List<WISKIWeb3Entity> series = client.retrieveSeries(Arrays.asList(), Arrays.asList(WISKIWeb3Client.WEEK_FILE));
	GSLoggerFactory.getLogger(getClass()).info("NRW week series: {}", series.size());
	assertTrue(series.size() > 300);

	WISKIWeb3Client.SiteInfo info = client.retrieveSiteInfo(WISKIWeb3Client.getDefaultAppURL(NRW_ENDPOINT));
	GSLoggerFactory.getLogger(getClass()).info("NRW: {} - {}", info.getOrganisation().orElse(null), info.getLicense().orElse(null));

	List<OriginalMetadata> records = harvest(NRW_ENDPOINT, "", 6);
	assertFalse(records.isEmpty());
	records.forEach(r -> GSLoggerFactory.getLogger(getClass()).info("NRW record: {} {} {}", new WISKIWeb3Entity(r.getMetadata()).getSeriesId(),
		new WISKIWeb3Entity(r.getMetadata()).getString(WISKIWeb3Client.BEGIN), new WISKIWeb3Entity(r.getMetadata()).getString(WISKIWeb3Client.END)));
    }

    /**
     * @param endpoint
     * @param stations
     * @param maxRecords
     * @return
     * @throws GSException
     */
    private List<OriginalMetadata> harvest(String endpoint, String stations, int maxRecords) throws GSException {

	WISKIWeb3Connector connector = new WISKIWeb3Connector();
	connector.setSourceURL(endpoint);
	connector.getSetting().setStations(stations);
	connector.getSetting().setMaxRecords(maxRecords);
	connector.getSetting().setCountry("Germany");

	List<OriginalMetadata> ret = new java.util.ArrayList<>();
	ListRecordsRequest request = new ListRecordsRequest();
	while (true) {
	    eu.essi_lab.messages.listrecords.ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    ret.addAll(response.getRecordsAsList());
	    if (response.getResumptionToken() == null) {
		return ret;
	    }
	    request.setResumptionToken(response.getResumptionToken());
	}
    }
}
