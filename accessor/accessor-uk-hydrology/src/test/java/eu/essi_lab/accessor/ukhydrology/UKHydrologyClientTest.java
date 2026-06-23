package eu.essi_lab.accessor.ukhydrology;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class UKHydrologyClientTest {

    @Test
    public void getStationGuidPrefersNotationForCombinedStations() {

	JSONObject object = new JSONObject(
		"{\"@id\":\"http://environment.data.gov.uk/hydrology/id/stations/6c6c174e-519f-4b02-9e3e-1737f2139417_445508\","
			+ "\"notation\":\"6c6c174e-519f-4b02-9e3e-1737f2139417_445508\","
			+ "\"stationGuid\":[\"6c6c174e-519f-4b02-9e3e-1737f2139417_445508\",\"6c6c174e-519f-4b02-9e3e-1737f2139417\"]}");

	UKHydrologyEntity station = new UKHydrologyEntity(object);

	assertEquals("6c6c174e-519f-4b02-9e3e-1737f2139417_445508", UKHydrologyClient.getStationGuid(station));
    }

    @Test
    public void getStationGuidUsesMatchingArrayEntryFromIdWhenNotationMissing() {

	JSONObject object = new JSONObject(
		"{\"@id\":\"http://environment.data.gov.uk/hydrology/id/stations/6c6c174e-519f-4b02-9e3e-1737f2139417_445508\","
			+ "\"stationGuid\":[\"6c6c174e-519f-4b02-9e3e-1737f2139417_445508\",\"6c6c174e-519f-4b02-9e3e-1737f2139417\"]}");

	UKHydrologyEntity station = new UKHydrologyEntity(object);

	assertEquals("6c6c174e-519f-4b02-9e3e-1737f2139417_445508", UKHydrologyClient.getStationGuid(station));
    }

    @Test
    public void extractLabelReadsNestedConceptLabels() {

	JSONObject object = new JSONObject(
		"{\"observedProperty\":{\"@id\":\"http://environment.data.gov.uk/reference/def/op/waterFlow\",\"label\":\"Water Flow\"},"
			+ "\"valueStatistic\":{\"@id\":\"http://environment.data.gov.uk/reference/def/core/mean\",\"label\":\"mean\"}}");

	assertEquals("Water Flow", UKHydrologyClient.extractLabel(object, UKHydrologyClient.OBSERVED_PROPERTY));
	assertEquals("mean", UKHydrologyClient.extractLabel(object, UKHydrologyClient.VALUE_STATISTIC));
    }

    @Test
    public void mergeStationIntoMeasureFlattensNestedLabels() {

	JSONObject measure = new JSONObject(
		"{\"observedProperty\":{\"@id\":\"http://environment.data.gov.uk/reference/def/op/waterFlow\",\"label\":\"Water Flow\"},"
			+ "\"valueStatistic\":{\"@id\":\"http://environment.data.gov.uk/reference/def/core/mean\",\"label\":\"mean\"},"
			+ "\"valueType\":\"mean\",\"period\":86400,\"periodName\":\"daily\",\"parameterName\":\"Flow\",\"label\":\"Daily mean Flow\"}");
	JSONObject station = new JSONObject(
		"{\"notation\":\"052d0819-2a32-47df-9b99-c243c9c8235b\",\"label\":\"Ulting Sarasota\",\"lat\":51.746683,\"long\":0.624437,"
			+ "\"riverName\":\"River Chelmer\",\"dateOpened\":\"2008-10-31\"}");

	UKHydrologyClient.mergeStationIntoMeasure(new UKHydrologyEntity(station), new UKHydrologyEntity(measure));

	assertEquals("Water Flow", measure.getString(UKHydrologyClient.OBSERVED_PROPERTY));
	assertEquals("mean", measure.getString(UKHydrologyClient.VALUE_STATISTIC));
    }
}
