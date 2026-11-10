package eu.essi_lab.accessor.chslf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;

import org.junit.Test;

public class SLFClientTest {

    @Test
    public void timeseriesIdRoundTrip() {

	String id = SLFClient.buildTimeseriesId(SLFNetwork.IMIS_PRECIPITATION, "DAV2", "RR_10MIN_SUM");
	assertEquals("imis-precipitation/DAV2/RR_10MIN_SUM", id);

	SimpleEntry<String, SLFParameter> parsed = SLFClient.parseTimeseriesId(id);
	assertEquals("DAV2", parsed.getKey());
	assertEquals(SLFParameter.IMIS_RR, parsed.getValue());

	// the same code in different networks maps to different parameters
	assertEquals(SLFParameter.STUDY_PLOT_HS, SLFClient.parseTimeseriesId("study-plot/5WJ0/HS").getValue());
	assertEquals(SLFParameter.IMIS_HS, SLFClient.parseTimeseriesId("imis/DAV2/HS").getValue());

	assertNull(SLFClient.parseTimeseriesId("imis/DAV2/UNKNOWN"));
	assertNull(SLFClient.parseTimeseriesId("unknown/DAV2/HS"));
	assertNull(SLFClient.parseTimeseriesId("imis/HS"));
    }

    @Test
    public void periodInDays() {

	long hour = 60 * 60 * 1000l;
	long now = System.currentTimeMillis();

	assertEquals(1, SLFClient.getPeriodInDays(new Date(now - 2 * hour)));
	assertEquals(3, SLFClient.getPeriodInDays(new Date(now - 30 * hour)));
	assertEquals(7, SLFClient.getPeriodInDays(new Date(now - 100 * hour)));
	assertEquals(7, SLFClient.getPeriodInDays(new Date(now - 1000 * hour)));
	assertEquals(7, SLFClient.getPeriodInDays(null));
    }

    @Test
    public void parseDates() {

	Date withZone = SLFClient.parseDate("2026-10-09T00:00:00Z").get();
	// the daily snow endpoint omits the time zone designator
	Date withoutZone = SLFClient.parseDate("2026-10-09T00:00:00").get();

	assertEquals(withZone, withoutZone);
	assertTrue(SLFClient.parseDate("").isEmpty());
    }

    @Test
    public void everyNetworkHasParameters() {

	for (SLFNetwork network : SLFNetwork.values()) {
	    assertTrue(network.name(), !SLFParameter.getParameters(network).isEmpty());
	}
    }
}
