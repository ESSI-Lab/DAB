package eu.essi_lab.accessor.ieopw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.ieopw.OPWClient.Observations;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class OPWClientTest {

    static final String LAYER_ENTRY = "{\"ts_id\":70613042,\"timestamp\":\"2026-10-27T09:15:00.000Z\",\"ts_value\":4.375,"
	    + "\"station_latitude\":54.83831806,\"station_longitude\":-7.575758056,\"ts_shortname\":\"WEB.Cmd.P-Continuous.Absolute\","
	    + "\"stationparameter_longname\":\"Stage\",\"station_no\":\"01041\",\"station_name\":\"Sandy Mills\",\"ts_unitsymbol\":\"m\","
	    + "\"stationparameter_name\":\"S\",\"catchment_name\":\"DEELE\",\"WTO_OBJECT\":\"DEELE\",\"RBD\":\"NORTH WESTERN RBD\","
	    + "\"GAUGE_DATUM\":\"3.655\",\"station_gauge_datum_unit\":\"Malin Head OSGM15\",\"Stn_Class\":\"Level & Flow\"}";

    static final String ARCHIVE = "#station_name;Sandy Mills\n" //
	    + "#station_no;01041\n" //
	    + "#stationparameter_name;S\n" //
	    + "#rows;6\n" //
	    + "#Timestamp;Value;Quality Code\n" //
	    + "1973-10-01T00:00:00.000Z;;-1\n" //
	    + "1973-10-01T00:15:00.000Z;3.960;31\n" //
	    + "1973-10-01T00:30:00.000Z;3.959;255\n" //
	    + "1973-10-01T00:45:00.000Z;3.958;31\n" //
	    + "1973-10-01T01:00:00.000Z;3.957;254\n" //
	    + "1973-10-01T01:15:00.000Z;3.956;151\n";

    /**
     * @return
     */
    static OPWEntity createWaterLevelSeries() {

	return OPWClient.createSeries(new JSONObject(LAYER_ENTRY), OPWParameter.S, null).get();
    }

    @Test
    public void createSeriesTest() {

	OPWEntity series = createWaterLevelSeries();

	assertEquals("01041|S", series.getSeriesId());
	assertEquals("01041", series.getStationNumber());
	assertEquals(OPWParameter.S, series.getParameter().get());
	assertEquals("2026-10-27T09:15:00Z", series.getObject().getString(OPWClient.END));

	JSONObject rainfall = new JSONObject(LAYER_ENTRY);
	rainfall.put(OPWClient.STATION_NO, "80101");
	assertFalse(OPWClient.createSeries(rainfall, OPWParameter.P, null).isPresent());

	JSONObject empty = new JSONObject(LAYER_ENTRY);
	empty.put(OPWClient.LATEST_TIMESTAMP, JSONObject.NULL);
	assertFalse(OPWClient.createSeries(empty, OPWParameter.S, null).isPresent());

	JSONObject station = new JSONObject();
	station.put(OPWClient.STATION_STATUS, "Active");
	assertEquals("Active",
		OPWClient.createSeries(new JSONObject(LAYER_ENTRY), OPWParameter.S, station).get().getString(OPWClient.STATION_STATUS));
    }

    @Test
    public void republishableTest() {

	assertTrue(OPWClient.isRepublishable("00001"));
	assertTrue(OPWClient.isRepublishable("01041"));
	assertTrue(OPWClient.isRepublishable("41000"));
	assertFalse(OPWClient.isRepublishable("00000"));
	assertFalse(OPWClient.isRepublishable("41001"));
	assertFalse(OPWClient.isRepublishable("80101"));
	assertFalse(OPWClient.isRepublishable("0000001041x"));
	assertFalse(OPWClient.isRepublishable(""));
	assertFalse(OPWClient.isRepublishable(null));
    }

    @Test
    public void seriesIdTest() {

	OPWEntity series = OPWClient.parseSeriesId("01041|TWater");
	assertEquals("01041", series.getStationNumber());
	assertEquals(OPWParameter.TWater, series.getParameter().get());
	assertEquals("01041|TWater", series.getSeriesId());
	assertEquals("data/internet/stations/0/01041/TWater/", OPWClient.getStationPath(series, OPWParameter.TWater));

	assertNull(OPWClient.parseSeriesId("01041"));
	assertNull(OPWClient.parseSeriesId("01041|X"));
	assertNull(OPWClient.parseSeriesId(null));
    }

    @Test
    public void parseArchiveTest() throws Exception {

	Observations all = OPWClient.parseArchive(new BufferedReader(new StringReader(ARCHIVE)), null, null, Integer.MAX_VALUE, false);

	assertTrue(all.complete);
	assertEquals(date("1973-10-01T00:00:00Z"), all.first);
	assertEquals(date("1973-10-01T01:15:00Z"), all.last);
	// missing and unusable values are discarded
	assertEquals(3, all.values.size());
	assertEquals(new BigDecimal("3.960"), all.values.firstEntry().getValue());

	Observations first = OPWClient.parseArchive(new BufferedReader(new StringReader(ARCHIVE)), null, null, Integer.MAX_VALUE, true);
	assertFalse(first.complete);
	assertEquals(1, first.values.size());
	assertEquals(date("1973-10-01T00:15:00Z"), first.values.firstKey());

	Observations range = OPWClient.parseArchive(new BufferedReader(new StringReader(ARCHIVE)), date("1973-10-01T00:30:00Z"),
		date("1973-10-01T01:00:00Z"), Integer.MAX_VALUE, false);
	assertFalse(range.complete);
	assertEquals(2, range.values.size());
	assertEquals(date("1973-10-01T00:45:00Z"), range.values.firstKey());
	assertEquals(date("1973-10-01T01:00:00Z"), range.values.lastKey());

	Observations limited = OPWClient.parseArchive(new BufferedReader(new StringReader(ARCHIVE)), null, null, 1, true);
	assertTrue(limited.values.isEmpty());
    }

    @Test
    public void parseFeedTest() {

	JSONArray feed = new JSONArray("[{\"stationparameter_name\":\"S\",\"ts_unitsymbol\":\"m\",\"rows\":\"3\","
		+ "\"columns\":\"Timestamp,Value,Quality Code\",\"data\":[[\"2026-09-27T23:00:00.000Z\",4.224,254],"
		+ "[\"2026-09-27T23:15:00.000Z\",null,-1],[\"2026-09-27T23:30:00.000Z\",4.213,254]]}]");

	Observations observations = OPWClient.parseFeed(feed);

	assertEquals(2, observations.values.size());
	assertEquals(date("2026-09-27T23:00:00Z"), observations.first);
	assertTrue(observations.covers(date("2026-09-28T00:00:00Z")));
	assertFalse(observations.covers(date("2026-09-27T00:00:00Z")));
	assertEquals(new BigDecimal("4.213"), observations.values.lastEntry().getValue());
    }

    @Test
    public void formatDateTest() {

	assertEquals("1973-10-01T00:15:00.000Z", OPWClient.formatDate(date("1973-10-01T00:15:00Z")));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
