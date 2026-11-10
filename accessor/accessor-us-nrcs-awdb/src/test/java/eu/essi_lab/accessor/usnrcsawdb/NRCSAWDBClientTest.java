package eu.essi_lab.accessor.usnrcsawdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.InterpolationType;

public class NRCSAWDBClientTest {

    @Test
    public void timeseriesIdRoundTrip() {

	JSONObject element = new JSONObject(
		"{\"elementCode\":\"WTEQ\",\"ordinal\":1,\"durationName\":\"DAILY\",\"storedUnitCode\":\"in\",\"beginDate\":\"1984-10-01 00:00\",\"endDate\":\"2100-01-01 00:00\"}");

	NRCSAWDBTimeseriesId id = NRCSAWDBTimeseriesId.of("301:CA:SNTL", element);
	assertEquals("301:CA:SNTL/WTEQ:*:1/DAILY", id.toString());
	assertEquals("WTEQ:*:1", id.getElementString());
	assertTrue(id.matches(element));

	NRCSAWDBTimeseriesId parsed = NRCSAWDBTimeseriesId.parse(id.toString()).get();
	assertEquals("301:CA:SNTL", parsed.getTriplet());
	assertNull(parsed.getHeightDepth());
	assertTrue(parsed.matches(element));

	JSONObject soil = new JSONObject("{\"elementCode\":\"SMS\",\"ordinal\":1,\"heightDepth\":-8,\"durationName\":\"DAILY\"}");
	NRCSAWDBTimeseriesId soilId = NRCSAWDBTimeseriesId.of("301:CA:SNTL", soil);
	assertEquals("301:CA:SNTL/SMS:-8:1/DAILY", soilId.toString());
	assertTrue(NRCSAWDBTimeseriesId.parse(soilId.toString()).get().matches(soil));
	assertTrue(!soilId.matches(element));

	assertTrue(NRCSAWDBTimeseriesId.parse("301:CA:SNTL/WTEQ/DAILY").isEmpty());
    }

    @Test
    public void finestDurationIsSelected() {

	JSONObject station = new JSONObject("{\"stationTriplet\":\"301:CA:SNTL\",\"stationElements\":["
		+ "{\"elementCode\":\"WTEQ\",\"ordinal\":1,\"durationName\":\"SEMIMONTHLY\"},"
		+ "{\"elementCode\":\"WTEQ\",\"ordinal\":1,\"durationName\":\"DAILY\"},"
		+ "{\"elementCode\":\"WTEQ\",\"ordinal\":2,\"durationName\":\"SEMIMONTHLY\"},"
		+ "{\"elementCode\":\"SNWD\",\"ordinal\":1,\"durationName\":\"HOURLY\"},"
		+ "{\"elementCode\":\"SNWD\",\"ordinal\":1,\"durationName\":\"MONTHLY\"}]}");

	List<JSONObject> selected = NRCSAWDBConnector.selectFinestDurations(station, Arrays.asList("DAILY", "SEMIMONTHLY", "MONTHLY"));

	assertEquals(3, selected.size());
	assertEquals("DAILY", selected.get(0).getString("durationName"));
	assertEquals("SEMIMONTHLY", selected.get(1).getString("durationName"));
	// hourly is not in the configured durations
	assertEquals("MONTHLY", selected.get(2).getString("durationName"));
    }

    @Test
    public void unitsConversion() {

	assertEquals("mm", NRCSAWDBUnits.getTargetUnit("in", "mm"));
	assertEquals("unknown", NRCSAWDBUnits.getTargetUnit("unknown", "other"));

	assertEquals(0, new BigDecimal("86.36").compareTo(NRCSAWDBUnits.convert(new BigDecimal("3.4"), "in", "mm")));
	assertEquals(0, new BigDecimal("22.86").compareTo(NRCSAWDBUnits.convert(new BigDecimal("9"), "in", "cm")));
	assertEquals(0, BigDecimal.ZERO.compareTo(NRCSAWDBUnits.convert(new BigDecimal("32"), "degF", "degC")));
	assertEquals(0, new BigDecimal("100").compareTo(NRCSAWDBUnits.convert(new BigDecimal("212"), "degF", "degC")));
	assertEquals(0, new BigDecimal("45.5").compareTo(NRCSAWDBUnits.convert(new BigDecimal("45.5"), "pct", "pct")));
    }

    @Test
    public void valueDates() {

	// daily values are at midnight of the station local standard time (PST)
	Date daily = NRCSAWDBClient.parseValueDate(new JSONObject("{\"date\":\"2026-03-01\",\"value\":3.4}"), -8).get();
	assertEquals("2026-03-01T08:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(daily));

	Date hourly = NRCSAWDBClient.parseValueDate(new JSONObject("{\"date\":\"2026-03-01 01:00\",\"value\":3.6}"), -8).get();
	assertEquals("2026-03-01T09:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(hourly));

	// snow courses carry the collection date
	Date course = NRCSAWDBClient.parseValueDate(
		new JSONObject("{\"month\":1,\"monthPart\":\"2\",\"year\":2020,\"collectionDate\":\"2020-01-28 00:00\",\"value\":12}"), 0).get();
	assertEquals("2020-01-28T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(course));

	Date semimonthly = NRCSAWDBClient.parseValueDate(new JSONObject("{\"month\":2,\"monthPart\":\"2\",\"year\":2020,\"value\":12}"), 0)
		.get();
	assertEquals("2020-02-16T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(semimonthly));

	assertTrue(NRCSAWDBClient.parseValue(new JSONObject("{\"date\":\"2026-03-01\"}")).isEmpty());
    }

    @Test
    public void mapperHelpers() {

	assertEquals("Snow water equivalent", NRCSAWDBMapper.capitalize("SNOW WATER EQUIVALENT"));
	assertEquals(InterpolationType.AVERAGE, NRCSAWDBMapper.getInterpolation("V"));
	assertEquals(InterpolationType.CONTINUOUS, NRCSAWDBMapper.getInterpolation("C"));
	assertEquals(InterpolationType.TOTAL, NRCSAWDBMapper.getInterpolation("D"));
	assertEquals("P1D", NRCSAWDBMapper.getISODuration("DAILY"));
	assertNull(NRCSAWDBMapper.getISODuration("SEASONAL"));
    }
}
