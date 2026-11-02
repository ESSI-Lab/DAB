package eu.essi_lab.accessor.waf.trigger;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class TRIGGERWafDownloaderTest {

    private static final String CAMS_FILE = "cams_eu_particulate_matter_2.5um_web_Bologna_2026110100.json";
    private static final String CAMS_URL = "https://aux.ecmwf.int/trigger/" + CAMS_FILE;

    @Test
    public void runDateFromJson() {

	JSONObject json = new JSONObject().put("date", "20261101").put("time", "0000");
	Date run = TRIGGERWafDownloader.getRunDate(json, CAMS_URL);
	Assert.assertEquals("2026-11-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(run));
    }

    @Test
    public void runDateFromFileName() {

	Date run = TRIGGERWafDownloader.getRunDate(new JSONObject(), CAMS_URL);
	Assert.assertEquals("2026-11-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(run));
    }

    @Test
    public void camsFileAnchoredToRunFromLeadZero() throws Exception {

	JSONObject json;
	try (InputStream stream = getClass().getClassLoader().getResourceAsStream("trigger/" + CAMS_FILE)) {
	    json = new JSONObject(IOUtils.toString(stream, StandardCharsets.UTF_8));
	}

	JSONArray median = json.getJSONObject("particulate_matter_2.5um-percentiles").getJSONArray("perc_50");
	JSONArray steps = json.getJSONObject("particulate_matter_2.5um").getJSONArray("steps");

	Map<String, BigDecimal> values = TRIGGERWafDownloader.getDateValue(median, true, TRIGGERWafDownloader.getRunDate(json, CAMS_URL),
		steps);

	List<String> times = new ArrayList<>(values.keySet());
	Assert.assertEquals(97, times.size());
	Assert.assertEquals("2026-11-01T00:00:00Z", times.get(0));
	Assert.assertEquals("2026-11-05T00:00:00Z", times.get(96));
	Assert.assertEquals(12.1655, values.get("2026-11-01T00:00:00Z").doubleValue(), 0.001);
	Assert.assertEquals(24.5471, values.get("2026-11-05T00:00:00Z").doubleValue(), 0.001);
    }

    @Test
    public void camsWithoutStepsStartsFromLeadZero() {

	Date run = TRIGGERWafDownloader.getRunDate(new JSONObject().put("date", "20261029").put("time", "0000"), CAMS_URL);
	List<String> times = new ArrayList<>(TRIGGERWafDownloader.getDateValue(series(97), true, run, null).keySet());

	Assert.assertEquals("2026-10-29T00:00:00Z", times.get(0));
	Assert.assertEquals("2026-11-02T00:00:00Z", times.get(96));
    }

    @Test
    public void mixedStepValuesAnchoredToRun() {

	Date run = TRIGGERWafDownloader.getRunDate(new JSONObject().put("date", "20261101").put("time", "0000"), "utci_x_2026110100.json");
	List<String> times = new ArrayList<>(TRIGGERWafDownloader.getDateValue(series(124), false, run, null).keySet());

	Assert.assertEquals("2026-11-01T01:00:00Z", times.get(0));
	Assert.assertEquals("2026-11-04T18:00:00Z", times.get(89));
	Assert.assertEquals("2026-11-07T00:00:00Z", times.get(107));
	Assert.assertEquals("2026-11-11T00:00:00Z", times.get(123));
    }

    private static JSONArray series(int size) {

	JSONArray array = new JSONArray();
	for (int i = 0; i < size; i++) {
	    array.put(i);
	}
	return array;
    }
}
