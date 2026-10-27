package eu.essi_lab.access.datacache;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.whos.WIS2DataEncoding;

/**
 * @author boldrini
 */
public class WIS2DataItemTest {

    private static final Date DATE = new Date(1792746000000L); // 2026-10-23T09:00:00Z

    private static DataRecord createRecord(String value) {

	DataRecord record = new DataRecord();
	record.setDataIdentifier("urn:argentina-ina:52/stage:hourly");
	record.setSourceIdentifier("argentina-ina");
	record.setDate(DATE);
	record.setValue(new BigDecimal(value));
	record.setUom("m");
	record.setObservedProperty("Water level");
	record.setObservedPropertyURI("http://hydro.geodab.eu/hydro-ontology/concept/11");
	record.setLatitudeLongitude(new LatitudeLongitude(new BigDecimal("-34.600"), new BigDecimal("-58.3667")));
	return record;
    }

    @Test
    public void itemIdTest() {

	DataRecord record = createRecord("2.37");

	String itemId = WIS2DataItem.getItemId(record);

	// usable as a URL path segment
	Assert.assertTrue(itemId.matches("[A-Za-z0-9_.-]+"));

	Optional<SimpleEntry<String, Date>> parsed = WIS2DataItem.parseItemId(itemId);
	Assert.assertEquals("urn:argentina-ina:52/stage:hourly", parsed.get().getKey());
	Assert.assertEquals(DATE, parsed.get().getValue());

	Assert.assertTrue(WIS2DataItem.parseItemId(null).isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId("").isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId("abc").isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId("abc.").isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId(".123").isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId("abc.notanumber").isEmpty());
	Assert.assertTrue(WIS2DataItem.parseItemId("a*c.123").isEmpty());
    }

    @Test
    public void encodeTest() {

	String encoded = WIS2DataItem.encode(createRecord("2.370"));

	// numbers are normalized, so that the encoding does not depend on how values are stored
	Assert.assertEquals(encoded, WIS2DataItem.encode(createRecord("2.37")));
	Assert.assertEquals(encoded, WIS2DataItem.encode(createRecord("2.37")));

	Assert.assertEquals("{\"id\":\"" + WIS2DataItem.getItemId(createRecord("2.37")) + "\"," //
		+ "\"source\":\"argentina-ina\"," //
		+ "\"series\":\"urn:argentina-ina:52/stage:hourly\"," //
		+ "\"observedProperty\":\"Water level\"," //
		+ "\"observedPropertyUri\":\"http://hydro.geodab.eu/hydro-ontology/concept/11\"," //
		+ "\"uom\":\"m\"," //
		+ "\"phenomenonTime\":\"2026-10-23T09:00:00Z\"," //
		+ "\"result\":2.37," //
		+ "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-58.3667,-34.6]}}", encoded);

	JSONObject json = new JSONObject(encoded);
	Assert.assertEquals(2.37, json.getDouble("result"), 0);

	Assert.assertTrue(WIS2DataItem.encode(createRecord("0.000")).contains("\"result\":0,"));
	Assert.assertTrue(WIS2DataItem.encode(createRecord("1500")).contains("\"result\":1500,"));
    }

    @Test
    public void csvTest() {

	DataRecord record = createRecord("2.370");

	String csv = WIS2DataItem.encode(record, WIS2DataEncoding.CSV, Optional.empty());

	Assert.assertEquals("id,source,series,observedProperty,observedPropertyUri,uom,phenomenonTime,result,quality,longitude,latitude\n" //
		+ WIS2DataItem.getItemId(record) + ",argentina-ina,urn:argentina-ina:52/stage:hourly,Water level," //
		+ "http://hydro.geodab.eu/hydro-ontology/concept/11,m,2026-10-23T09:00:00Z,2.37,,-58.3667,-34.6\n", csv);

	// fields with commas or quotes are quoted
	record.setObservedProperty("Discharge, stream \"daily\"");
	record.setLatitudeLongitude(null);

	String[] lines = WIS2DataItem.encode(record, WIS2DataEncoding.CSV, Optional.empty()).split("\n");
	Assert.assertEquals(2, lines.length);
	Assert.assertTrue(lines[1].contains(",\"Discharge, stream \"\"daily\"\"\","));
	Assert.assertTrue(lines[1].endsWith(",2.37,,,"));
    }

    @Test
    public void omJSONTest() {

	DataRecord record = createRecord("2.370");
	record.setQuality("good");

	StationRecord station = new StationRecord();
	station.setDataIdentifier(record.getDataIdentifier());
	station.setPlatformIdentifier("urn:wmo:platform:52");
	station.setPlatformName("Rosario");

	String encoded = WIS2DataItem.encode(record, WIS2DataEncoding.OM_JSON, Optional.of(station));

	Assert.assertEquals("{\"id\":\"urn:argentina-ina:52/stage:hourly\"," //
		+ "\"type\":\"TimeSeriesObservation\"," //
		+ "\"featureOfInterest\":{\"href\":\"urn:wmo:platform:52\",\"title\":\"Rosario\"}," //
		+ "\"observedProperty\":{\"href\":\"http://hydro.geodab.eu/hydro-ontology/concept/11\",\"title\":\"Water level\"}," //
		+ "\"phenomenonTime\":{\"begin\":\"2026-10-23T09:00:00Z\",\"end\":\"2026-10-23T09:00:00Z\"}," //
		+ "\"parameter\":[{\"name\":\"sourceId\",\"value\":\"argentina-ina\"}]," //
		+ "\"result\":{\"metadata\":{},\"defaultPointMetadata\":{\"uom\":\"m\"}," //
		+ "\"points\":[{\"time\":{\"instant\":\"2026-10-23T09:00:00Z\"},\"value\":2.37,\"metadata\":{\"quality\":{\"term\":\"good\"}}}]}}",
		encoded);

	// without station, the feature of interest is omitted
	JSONObject json = new JSONObject(WIS2DataItem.encode(record, WIS2DataEncoding.OM_JSON, Optional.empty()));
	Assert.assertFalse(json.has("featureOfInterest"));
	Assert.assertEquals(2.37, json.getJSONObject("result").getJSONArray("points").getJSONObject(0).getDouble("value"), 0);
    }

    @Test
    public void jsonEncodingTest() {

	DataRecord record = createRecord("2.37");

	Assert.assertEquals(WIS2DataItem.encode(record), WIS2DataItem.encode(record, WIS2DataEncoding.JSON, Optional.empty()));
    }

    @Test
    public void encodingIdTest() {

	Assert.assertEquals(WIS2DataEncoding.CSV, WIS2DataEncoding.fromId("CSV").get());
	Assert.assertEquals(WIS2DataEncoding.OM_JSON, WIS2DataEncoding.fromId("omjson").get());
	Assert.assertEquals(WIS2DataEncoding.JSON, WIS2DataEncoding.fromId("json").get());
	Assert.assertTrue(WIS2DataEncoding.fromId("xml").isEmpty());
    }

    @Test
    public void missingPropertiesTest() {

	DataRecord record = new DataRecord();
	record.setDataIdentifier("id");
	record.setDate(DATE);

	JSONObject json = new JSONObject(WIS2DataItem.encode(record));

	Assert.assertEquals(3, json.length());
	Assert.assertTrue(json.has("id"));
	Assert.assertTrue(json.has("series"));
	Assert.assertTrue(json.has("phenomenonTime"));
    }
}
