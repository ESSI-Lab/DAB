package eu.essi_lab.workflow.processor.timeseries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.joda.time.DateTimeZone;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class TimeZoneTest {

    @Test
    public void test() {
	DateTimeFormatter timezoneFormatter = DateTimeFormatter.ofPattern("z");
	TemporalAccessor temporalAccessor = timezoneFormatter.parse("AKST");
	ZoneId zoneId = ZoneId.from(temporalAccessor);
	DateTimeZone dtz = DateTimeZone.forID(zoneId.getId());

	assertTrue(isDaylightSavingOn(dtz, ISO8601DateTimeUtils.parseISO8601ToDate("2017-08-01").get().getTime()));
	assertFalse(isDaylightSavingOn(dtz, ISO8601DateTimeUtils.parseISO8601ToDate("2017-12-01").get().getTime()));

    }

    public boolean isDaylightSavingOn(DateTimeZone zone, long time) {

	return !zone.isStandardOffset(time);

    }

}
