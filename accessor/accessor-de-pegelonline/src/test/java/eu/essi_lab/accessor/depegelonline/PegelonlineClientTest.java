package eu.essi_lab.accessor.depegelonline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.AbstractMap.SimpleEntry;

import org.json.JSONObject;
import org.junit.Test;

public class PegelonlineClientTest {

    @Test
    public void parseTimeseriesIdSplitsUuidAndShortname() {

	SimpleEntry<String, String> parts = PegelonlineClient
		.parseTimeseriesId("593647aa-9fea-43ec-a7d6-6476a76ae868/W");

	assertNotNull(parts);
	assertEquals("593647aa-9fea-43ec-a7d6-6476a76ae868", parts.getKey());
	assertEquals("W", parts.getValue());
    }

    @Test
    public void mergeStationIntoTimeseriesBuildsMetadataRecord() {

	JSONObject station = new JSONObject(
		"{\"uuid\":\"593647aa-9fea-43ec-a7d6-6476a76ae868\",\"number\":\"2710080\",\"shortname\":\"BONN\",\"longname\":\"BONN\","
			+ "\"km\":654.8,\"agency\":\"STANDORT KÖLN\",\"longitude\":7.108045,\"latitude\":50.736398,"
			+ "\"water\":{\"shortname\":\"RHEIN\",\"longname\":\"RHEIN\"}}");
	JSONObject timeseries = new JSONObject(
		"{\"shortname\":\"W\",\"longname\":\"WASSERSTAND ROHDATEN\",\"unit\":\"cm\",\"equidistance\":15}");

	PegelonlineEntity target = new PegelonlineEntity(new JSONObject(), PegelonlineEntity.EntityType.TIMESERIES);
	PegelonlineClient.mergeStationIntoTimeseries(new PegelonlineEntity(station), timeseries, target);

	JSONObject merged = target.getObject();
	assertEquals("593647aa-9fea-43ec-a7d6-6476a76ae868/W", merged.getString(PegelonlineClient.TIMESERIES_ID));
	assertEquals("BONN", merged.getString(PegelonlineClient.STATION_LABEL));
	assertEquals("RHEIN", merged.getString(PegelonlineClient.WATER_NAME));
	assertEquals("cm", merged.getString(PegelonlineClient.UNIT_NAME));
	assertEquals(900, merged.getInt(PegelonlineClient.PERIOD));
    }
}
