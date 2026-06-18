package eu.essi_lab.accessor.chexistenzbafu;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;

import org.json.JSONObject;
import org.junit.Test;

public class CHExistenzBafuClientTest {

    @Test
    public void getLocationIdReadsDetailsId() {

	JSONObject object = new JSONObject(
		"{\"name\":\"2135\",\"details\":{\"id\":\"2135\",\"name\":\"Bern, Schönau\",\"water-body-name\":\"Aare\",\"water-body-type\":\"river\",\"lat\":46.9331,\"lon\":7.448}}");

	CHExistenzBafuEntity location = new CHExistenzBafuEntity(object);

	assertEquals("2135", CHExistenzBafuClient.getLocationId(location));
    }

    @Test
    public void parseMeasureNotationSplitsLocationAndParameter() {

	SimpleEntry<String, String> ids = CHExistenzBafuClient.parseMeasureNotation("2135:temperature");

	assertEquals("2135", ids.getKey());
	assertEquals("temperature", ids.getValue());
    }

    @Test
    public void createMeasureMergesLocationAndParameter() {

	JSONObject locationObject = new JSONObject(
		"{\"name\":\"2135\",\"details\":{\"id\":\"2135\",\"name\":\"Bern, Schönau\",\"water-body-name\":\"Aare\",\"water-body-type\":\"river\",\"lat\":46.9331,\"lon\":7.448}}");
	JSONObject parameterObject = new JSONObject(
		"{\"name\":\"temperature\",\"unit\":\"°C\",\"details\":{\"name\":\"Wassertemperatur\",\"unit\":\"°C\"}}");

	CHExistenzBafuEntity measure = CHExistenzBafuClient.createMeasure(//
		new CHExistenzBafuEntity(locationObject), //
		new CHExistenzBafuEntity(parameterObject), //
		1781707800L);

	JSONObject object = measure.getObject();
	assertEquals("2135:temperature", object.getString(CHExistenzBafuClient.MEASURE_NOTATION));
	assertEquals("Bern, Schönau", object.getString(CHExistenzBafuClient.LOCATION_NAME));
	assertEquals("Wassertemperatur", object.getString(CHExistenzBafuClient.PARAMETER_NAME));
	assertEquals("°C", object.getString(CHExistenzBafuClient.PARAMETER_UNIT));
	assertEquals("1989-12-31T23:00:00Z", object.getString(CHExistenzBafuClient.FROM));
	assertEquals("2026-06-17T14:50:00Z", object.getString(CHExistenzBafuClient.TO));
    }

    @Test
    public void swissLocalDateTimeToIso8601UtcConvertsCestToUtc() {

	assertEquals("2026-06-17T18:10:00Z", CHExistenzBafuClient.toIso8601Utc("2026-06-17 20:10:00"));
    }

    @Test
    public void swissLocalDateTimeToIso8601UtcConvertsCetToUtc() {

	assertEquals("1989-12-31T23:00:00Z", CHExistenzBafuClient.toIso8601Utc("1990-01-01 00:00:00"));
    }
}
