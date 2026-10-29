package eu.essi_lab.accessor.atehyd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class EHYDExternalTestIT {

    private static final String STATION = "201582";

    @Test
    public void testSeries() throws GSException {

	EHYDClient client = new EHYDClient(System.getProperty("ehydEndpoint", EHYDClient.DEFAULT_ENDPOINT));

	List<EHYDEntity> series = client.retrieveSeries(EHYDProduct.getProducts(EHYDProduct.SURFACE_WATER));
	GSLoggerFactory.getLogger(getClass()).info("{} surface water series", series.size());
	assertTrue(series.size() > 3000);

	for (EHYDProduct product : EHYDProduct.getProducts(EHYDProduct.SURFACE_WATER)) {
	    long count = series.stream().filter(s -> s.getProduct().get() == product).count();
	    GSLoggerFactory.getLogger(getClass()).info("{}: {} series", product.getId(), count);
	    assertTrue(count > 0);
	}

	EHYDEntity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(STATION + "|owf_wasserst_tagesmittel")).findFirst()
		.get();
	assertTrue(client.completeSeries(waterLevel));

	GSLoggerFactory.getLogger(getClass()).info("{}", waterLevel.getObject().toString(3));

	assertEquals("Mühlen", waterLevel.getStationName());
	assertEquals("cm", waterLevel.getUnit());
	assertEquals("1970-12-31T23:00:00Z", waterLevel.getString(EHYDClient.BEGIN));
	assertEquals("2021-12-30T23:00:00Z", waterLevel.getString(EHYDClient.END));
    }

    @Test
    public void testObservations() throws GSException {

	EHYDClient client = new EHYDClient();
	EHYDEntity discharge = EHYDClient.parseSeriesId(STATION + "|owf_abfl_tagesmittel");

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1990-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1990-01-31T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge, begin, end);
	assertEquals(30, values.size());
	assertFalse(values.firstKey().before(begin));

	assertEquals("2021-12-30T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(client.retrieveLastObservation(discharge).get()));
    }

    @Test
    public void testConnector() throws GSException {

	EHYDConnector connector = new EHYDConnector();
	connector.setSourceURL(EHYDClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION + ",200147");
	connector.getSetting().setMaxRecords(20);

	long start = System.currentTimeMillis();
	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	GSLoggerFactory.getLogger(getClass()).info("{} records in {} ms", records.size(), System.currentTimeMillis() - start);
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new EHYDEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(EHYDClient.BEGIN))).//
		map(EHYDEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("{}", ids);

	assertTrue(ids.contains(STATION + "|owf_wasserst_tagesmittel"));
	assertTrue(ids.contains(STATION + "|owf_abfl_monatsmaxima"));
	// suspended sediment
	assertTrue(ids.contains("200147|owf_schwebstoff_tagesfracht"));

	GSSource source = new GSSource();
	source.setEndpoint(EHYDClient.DEFAULT_ENDPOINT);

	for (OriginalMetadata record : records) {
	    GSResource resource = new EHYDMapper().map(record, source);
	    GSLoggerFactory.getLogger(getClass()).info("{} [{} / {}]", resource.getHarmonizedMetadata().getCoreMetadata().getTitle(),
		    resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition(),
		    resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getEndPosition());
	}
    }

    @Test
    public void testGroundwater() throws GSException {

	EHYDConnector connector = new EHYDConnector();
	connector.setSourceURL(EHYDClient.DEFAULT_ENDPOINT);
	connector.getSetting().setProducts("");
	connector.getSetting().setGroundwater(true);
	connector.getSetting().setStations("328781");

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();

	List<String> ids = records.stream().map(r -> new EHYDEntity(r.getMetadata()).getSeriesId()).collect(Collectors.toList());
	GSLoggerFactory.getLogger(getClass()).info("{}", ids);

	assertTrue(ids.containsAll(Arrays.asList("328781|gw_monatsmittel", "328781|gw_jahresmaxima", "328781|gw_jahresminima")));
    }
}
