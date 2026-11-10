package eu.essi_lab.accessor.derlpwasser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;

/**
 * @author boldrini
 */
public class RLPWasserClientTest {

    /**
     * Extract of https://hochwasser.rlp.de/api/v1/config (single quotes replaced by double quotes)
     */
    static final String CONFIG = ("{'measurementsite':{" //
	    + "'26600128':{'id':387,'number':'26600128','riverMunicipalName':null,'type':'default','name':'Kronenburger See',"
	    + "'isExternal':false,'isSeaSite':true,'catchmentArea':0,'easting':320169.2,'eastingOffset':null,'northing':5581427,"
	    + "'northingOffset':null,'elevation':489.8,'riverKilometer':null,'hasPrediction':false,'rivers':['2660000000'],"
	    + "'riverAreas':[4],'slug':'kronenburger-see'}," //
	    + "'27180403':{'id':393,'number':'27180403','riverMunicipalName':null,'type':'default','name':'Altenahr',"
	    + "'isExternal':false,'isSeaSite':false,'catchmentArea':747.97,'easting':357407.4,'eastingOffset':null,"
	    + "'northing':5598011.6,'northingOffset':null,'elevation':160.522,'riverKilometer':null,'hasPrediction':true,"
	    + "'rivers':['2718000000'],'riverAreas':[8],'slug':'altenahr'}," //
	    + "'1034232320':{'id':719,'number':'1034232320','riverMunicipalName':'Kyll','type':'municipal','name':'Auw an der Kyll',"
	    + "'isExternal':false,'isSeaSite':false,'catchmentArea':0,'easting':328578.8,'eastingOffset':null,'northing':5530303.7,"
	    + "'northingOffset':null,'elevation':0,'riverKilometer':null,'hasPrediction':false,'rivers':['26600000000'],"
	    + "'riverAreas':[8888888888],'slug':'auw-an-der-kyll'}}," //
	    + "'operators':{" //
	    + "'26600128':{'ms_name':'Kronenburger See','op_name':'SGD Nord, Regionalstelle Trier',"
	    + "'op_url':'https://wasserportal.rlp-umwelt.de/servlet/is/10140/'}," //
	    + "'27180403':{'ms_name':'Altenahr','op_name':'SGD Nord, Regionalstelle Koblenz',"
	    + "'op_url':'https://wasserportal.rlp-umwelt.de/servlet/is/10140/'}," //
	    + "'1034232320':{'ms_name':'Auw an der Kyll','ms_url':null,'op_name':'Kommune / Kreis','op_url':null}}," //
	    + "'rivers':{'2660000000':{'name':'Kyll'},'2718000000':{'name':'Ahr'}}}").replace('\'', '"');

    /**
     * Extract of https://hochwasser.rlp.de/api/v1/measurement-site/27180403 (single quotes replaced by double quotes)
     */
    static final String SITE_DATA = ("{'Q':{'xLast':'2026-10-27T20:00:00Z','yLast':5.2,'legendColor':'#00c8ff','measurements':["
	    + "{'y':2.21,'x':'2026-10-22T20:00:00Z'},{'y':2.21,'x':'2026-10-22T20:15:00Z'},{'y':null,'x':'2026-10-22T20:30:00Z'},"
	    + "{'y':5.2,'x':'2026-10-27T20:00:00Z'}],"
	    + "'predictions':{'p50':[{'x':'2026-10-29T12:00:00Z','y':3.84}],'time':'2026-10-27T15:00:00Z',"
	    + "'nextUpdateTime':'2026-10-27T20:30:00Z'},'xMin':'2026-10-22T20:00:00Z','xMax':'2026-10-29T12:00:00Z'}," //
	    + "'extremeevents':{'W':[{'date':'1993-01-11T23:00:00Z','value':293,'dimension':'cm','confirmed':1}]}," //
	    + "'W':{'xLast':'2026-10-27T20:00:00Z','yLast':70,'legendColor':'#0000ff','measurements':["
	    + "{'y':55,'x':'2026-10-22T20:00:00Z'},{'y':55,'x':'2026-10-22T20:15:00Z'},{'y':null,'x':'2026-10-22T20:30:00Z'},"
	    + "{'y':70,'x':'2026-10-27T20:00:00Z'}],"
	    + "'predictions':{'p50':[{'x':'2026-10-29T12:00:00Z','y':71}],'time':'2026-10-27T15:00:00Z',"
	    + "'nextUpdateTime':'2026-10-27T20:30:00Z'},'xMin':'2026-10-22T20:00:00Z','xMax':'2026-10-29T12:00:00Z'}," //
	    + "'hint':{'type':'warning','text':'Bauarbeiten an der Stützwand des Bahnhofs Altenahr beeinflussen den Wasserstand.',"
	    + "'updatedAt':'2026-10-19T07:25:12Z'}}").replace('\'', '"');

    /**
     * Response of a site out of service
     */
    static final String EMPTY_SITE_DATA = ("{'Q':{'legendColor':'#f0f0f0','measurements':[]},"
	    + "'W':{'legendColor':'#f0f0f0','measurements':[]},"
	    + "'hint':{'type':'warning','text':'Der Pegel ist außer Betrieb.'}}").replace('\'', '"');

    /**
     * @param number
     * @return
     */
    static JSONObject getSite(String number) {

	return RLPWasserClient.parseSites(new JSONObject(CONFIG)).stream().//
		filter(s -> s.getString(RLPWasserClient.NUMBER).equals(number)).//
		findFirst().//
		get();
    }

    /**
     * @return the water level series of Altenahr
     */
    static RLPWasserEntity createWaterLevelSeries() {

	return RLPWasserClient.createSeries(getSite("27180403"), new JSONObject(SITE_DATA), Arrays.asList(RLPWasserParameter.W))
		.get(0);
    }

    @Test
    public void parseSitesTest() {

	List<JSONObject> sites = RLPWasserClient.parseSites(new JSONObject(CONFIG));
	assertEquals(3, sites.size());

	// sorted by number
	assertEquals("1034232320", sites.get(0).getString(RLPWasserClient.NUMBER));

	JSONObject altenahr = getSite("27180403");
	assertEquals("Altenahr", altenahr.getString(RLPWasserClient.NAME));
	assertEquals("Ahr", altenahr.getString(RLPWasserClient.RIVER));
	assertEquals("SGD Nord, Regionalstelle Koblenz", altenahr.getString(RLPWasserClient.OPERATOR));
	assertEquals("Germany", altenahr.getString(RLPWasserClient.COUNTRY));
	assertEquals(747.97, altenahr.getDouble(RLPWasserClient.CATCHMENT_AREA), 0.001);

	// Altenahr, about 50.516 N 6.989 E
	assertEquals(50.516, altenahr.getDouble(RLPWasserClient.LAT), 0.01);
	assertEquals(6.989, altenahr.getDouble(RLPWasserClient.LON), 0.01);

	// river name given by the municipality, no catchment area
	JSONObject auw = getSite("1034232320");
	assertEquals("Kyll", auw.getString(RLPWasserClient.RIVER));
	assertEquals(RLPWasserClient.MUNICIPAL_TYPE, auw.getString(RLPWasserClient.TYPE));
	assertFalse(auw.has(RLPWasserClient.CATCHMENT_AREA));
	assertFalse(auw.has(RLPWasserClient.ELEVATION));
	assertFalse(auw.has(RLPWasserClient.OPERATOR_URL));
    }

    @Test
    public void createSeriesTest() {

	List<RLPWasserEntity> series = RLPWasserClient.createSeries(getSite("27180403"), new JSONObject(SITE_DATA),
		Arrays.asList(RLPWasserParameter.values()));

	assertEquals(2, series.size());

	RLPWasserEntity waterLevel = series.get(0);
	assertEquals("27180403|W", waterLevel.getSeriesId());
	assertEquals("cm", waterLevel.getUnit());
	assertEquals("2026-10-22T20:00:00Z", waterLevel.getString(RLPWasserClient.BEGIN));
	assertEquals("2026-10-27T20:00:00Z", waterLevel.getString(RLPWasserClient.END));
	assertTrue(waterLevel.getString(RLPWasserClient.HINT).startsWith("Bauarbeiten"));

	RLPWasserEntity discharge = series.get(1);
	assertEquals("27180403|Q", discharge.getSeriesId());
	assertEquals("m³/s", discharge.getUnit());

	// lake level
	RLPWasserEntity lake = RLPWasserClient
		.createSeries(getSite("26600128"), new JSONObject(SITE_DATA), Arrays.asList(RLPWasserParameter.W)).get(0);
	assertEquals("m ü. NHN", lake.getUnit());

	// site out of service
	assertTrue(RLPWasserClient.createSeries(getSite("27180403"), new JSONObject(EMPTY_SITE_DATA),
		Arrays.asList(RLPWasserParameter.values())).isEmpty());
    }

    @Test
    public void parseMeasurementsTest() {

	TreeMap<Date, BigDecimal> values = RLPWasserClient.parseMeasurements(new JSONObject(SITE_DATA), RLPWasserParameter.Q);

	// the null value is skipped, the forecasts are ignored
	assertEquals(3, values.size());
	assertEquals(new BigDecimal("5.2"), values.lastEntry().getValue());
	assertEquals(RLPWasserClient.parseDate("2026-10-27T20:00:00Z").get(), values.lastKey());

	assertTrue(RLPWasserClient.parseMeasurements(new JSONObject("{}"), RLPWasserParameter.W).isEmpty());
    }

    @Test
    public void seriesIdTest() {

	RLPWasserEntity series = RLPWasserClient.parseSeriesId("27180403|Q");
	assertEquals("27180403", series.getSiteNumber());
	assertEquals(RLPWasserParameter.Q, series.getParameter().get());
	assertEquals("27180403|Q", series.getSeriesId());

	assertNull(RLPWasserClient.parseSeriesId("27180403|T"));
	assertNull(RLPWasserClient.parseSeriesId("27180403"));
	assertNull(RLPWasserClient.parseSeriesId("../config|W"));
	assertNull(RLPWasserClient.parseSeriesId(null));
    }

    @Test
    public void countryTest() {

	assertEquals("France", RLPWasserClient.getCountry("DREAL Grand Est"));
	assertEquals("Luxembourg", RLPWasserClient.getCountry("AGE Luxemburg"));
	assertEquals("Belgium", RLPWasserClient.getCountry("SPW (Belgien)"));
	assertEquals("Germany", RLPWasserClient.getCountry("WSA Rhein"));
    }

    @Test
    public void coordinatesTest() {

	// Mainz (city coordinates of the portal configuration)
	SimpleEntry<Double, Double> latLon = RLPWasserClient.toWGS84(447828.77588306, 5539106.9862407).get();
	assertEquals(50.0, latLon.getKey(), 0.02);
	assertEquals(8.27, latLon.getValue(), 0.02);
    }

    @Test
    public void endpointTest() {

	assertEquals(RLPWasserClient.DEFAULT_ENDPOINT, RLPWasserClient.normalizeEndpoint(null));
	assertEquals(RLPWasserClient.DEFAULT_ENDPOINT, RLPWasserClient.normalizeEndpoint("https://hochwasser.rlp.de"));
	assertEquals(RLPWasserClient.DEFAULT_ENDPOINT, RLPWasserClient.normalizeEndpoint("https://hochwasser.rlp.de/api/v1"));
    }
}
