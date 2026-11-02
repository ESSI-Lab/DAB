package eu.essi_lab.accessor.rarcticnet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.rarcticnet.RArcticNetClient.Extent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class RArcticNetClientTest {

    /**
     * Rows of data/SiteAttributes.txt
     */
    static final String SITE_ATTRIBUTES = "\"PointID\"\t\"Code\"\t\"Name\"\t\"Lat\"\t\"Long\"\t\"X_Ease\"\t\"Y_Ease\"\t\"DArea\"\t"
	    + "\"DArea_effective\"\t\"Hydrozone\"\t\"Source\"\t\"Gauge_altitude\"\t\"DistanceToOutlet\"\t\"MinOfYear\"\t\"MaxOfYear\"\t"
	    + "\"CountOfYear\"\t\"PercentOfCoverage\"\t\"PercentOfCoverageByMonth\"\t\"Notes\"\r\n" //
	    + "1\t\"05AA001\"\t\"OLDMAN RIVER NEAR COWLEY\"\t49.60\t-114.04\t-4018301\t1792425\t1940.00\t1940.00\t\"Nelson\"\t\"Hydat\"\t"
	    + "\t\t1908.00\t1949.00\t30\t71\t57\t\r\n" //
	    + "5855\t\"1001\"\t\"Kolyma At Orotuk\"\t62.12\t148.47\t1605300\t2616535\t43600.00\t43600.00\t\"Anadyr Kolyma\"\t\"SHI\"\t"
	    + "478.24\t2059.00\t1957.00\t1997.00\t41\t100\t98\t\r\n" //
	    + "7782\t\"70617\"\t\"Izhma At Izhvail'skiy\"\t\t\t\t\t952.00\t952.00\t\"North European Russia\"\t\"SHI\"\t\t\t1997.00\t"
	    + "1998.00\t2\t100\t100\t\r\n";

    /**
     * Rows of data/Discharge_ms.txt (missing values are empty fields, trailing empty fields may be omitted)
     */
    static final String MONTHLY = "Point_ID\tCode\tYear\tJan\tFeb\tMar\tApr\tMay\tJun\tJul\tAug\tSep\tOct\tNov\tDec\tAnnual\r\n" //
	    + "1\t05AA001\t1908\t\t\t\t\t\t\t27.100\t8.810\t5.280\t5.110\t\t\t\r\n" //
	    + "1\t05AA001\t1909\t\t\t\t\t40.600\t96.100\t\t19.300\t7.150\t5.050\t\t\t\r\n" //
	    + "1\t05AA001\t1949\t2.130\t1.900\t1.780\t6.170\r\n" //
	    + "5855\t1001\t1997\t5.990\t3.390\t2.560\t2.270\t\t\t\t\t\t\t\t\t\r\n" //
	    + "5855\t1001\t1957.00\t3.810\t2.050\t0.950\t1.200\t135.000\t1020.000\t700.000\t618.000\t293.000\t97.600\t32.100\t10.000\t"
	    + "242.809\r\n";

    /**
     * Rows of russia-arcticnet/Daily_SiteAttributes.txt
     */
    static final String DAILY_SITE_ATTRIBUTES = "\"PointID\"\t\"Code\"\t\"Name\"\t\"Lat\"\t\"Long\"\t\"X_Ease\"\t\"Y_Ease\"\t\"DArea\"\t"
	    + "\"DArea_effective\"\t\"Hydrozone\"\t\"Source\"\t\"Gauge_altitude\"\t\"DistanceToOutlet\"\t\"MinOfYear\"\t\"MaxOfYear\"\t"
	    + "\"CountOfYear\"\t\"PercentOfCoverage\"\t\"PercentOfCoverageByMonth\"\t\"Notes\"\r\n" //
	    + "5855\t1001 \tKolyma At Orotuk\t62.12\t148.47\t1605300\t2616535\t43600.00\t43600.00\t\"Anadyr Kolyma\"\t\"SHI\"\t478.24\t"
	    + "2059.00\t1968.00\t1997.00\t20.00\t100\t98\t\r\n";

    /**
     * Rows of russia-arcticnet/discharge_m3_s_UNH-UCLA.txt: one row per day of the month, with the values of the 12 months
     */
    static final String DAILY = "Code\tYear\tDay\tJan\tFeb\tMar\tApr\tMay\tJun\tJul\tAug\tSep\tOct\tNov\tDec\r\n" //
	    + "1001 \t1968\t1\t4.50\t3.07\t1.91\t1.13\t1.06\t1930.00\t1190.00\t1400.00\t322.00\t177.00\t\t\r\n" //
	    + "1001 \t1968\t29\t3.21\t1.93\t1.22\t1.04\t2400.00\t2410.00\t458.00\t333.00\t179.00\t\t\t\r\n" //
	    + "1001 \t1968\t30\t3.16\t0\t1.19\t1.05\t2800.00\t1600.00\t607.00\t322.00\t175.00\t\t\t\r\n" //
	    + "1001 \t1968\t31\t3.11\t\t1.16\t\t2190.00\t\t934.00\t315.00\t\t\t\t\r\n" //
	    + "10012\t1968\t31\t1.00\t\t\t\t\t\t\t\t\t\t\t\r\n";

    /**
     * @return
     */
    static RArcticNetEntity createMonthlySeries() throws Exception {

	Map<String, JSONObject> sites = RArcticNetClient.parseSiteAttributes(reader(SITE_ATTRIBUTES));
	Map<String, Extent> extents = RArcticNetClient.parseExtents(reader(MONTHLY), RArcticNetDataset.MONTHLY);

	return RArcticNetClient.createSeries(sites.get("5855"), RArcticNetDataset.MONTHLY, extents.get("5855")).get();
    }

    /**
     * @return
     */
    static RArcticNetEntity createDailySeries() throws Exception {

	Map<String, JSONObject> sites = RArcticNetClient.parseSiteAttributes(reader(DAILY_SITE_ATTRIBUTES));
	Map<String, Extent> extents = RArcticNetClient.parseExtents(reader(DAILY), RArcticNetDataset.DAILY);

	JSONObject site = sites.get("5855");
	return RArcticNetClient.createSeries(site, RArcticNetDataset.DAILY,
		extents.get(RArcticNetClient.getRowKey(site, RArcticNetDataset.DAILY))).get();
    }

    @Test
    public void siteAttributesTest() throws Exception {

	Map<String, JSONObject> sites = RArcticNetClient.parseSiteAttributes(reader(SITE_ATTRIBUTES));

	assertEquals(3, sites.size());

	JSONObject site = sites.get("1");
	assertEquals("05AA001", site.getString(RArcticNetClient.CODE));
	assertEquals("OLDMAN RIVER NEAR COWLEY", site.getString(RArcticNetClient.NAME));
	assertEquals("49.60", site.getString(RArcticNetClient.LAT));
	assertEquals("-114.04", site.getString(RArcticNetClient.LON));
	assertEquals("Hydat", site.getString(RArcticNetClient.SOURCE));
	assertFalse(site.has(RArcticNetClient.GAUGE_ALTITUDE));

	// some sites have no coordinates
	assertFalse(sites.get("7782").has(RArcticNetClient.LAT));

	// unquoted fields with trailing spaces in the daily site attributes
	JSONObject daily = RArcticNetClient.parseSiteAttributes(reader(DAILY_SITE_ATTRIBUTES)).get("5855");
	assertEquals("1001", daily.getString(RArcticNetClient.CODE));
	assertEquals("Kolyma At Orotuk", daily.getString(RArcticNetClient.NAME));
	assertEquals("Anadyr Kolyma", daily.getString(RArcticNetClient.HYDROZONE));
    }

    @Test
    public void monthlyExtentsTest() throws Exception {

	Map<String, Extent> extents = RArcticNetClient.parseExtents(reader(MONTHLY), RArcticNetDataset.MONTHLY);

	assertEquals(2, extents.size());

	Extent extent = extents.get("1");
	assertEquals(date("1908-07-01T00:00:00Z"), extent.first);
	assertEquals(date("1949-04-01T00:00:00Z"), extent.last);
	// the annual value is not counted
	assertEquals(4 + 5 + 4, extent.count);

	// rows are not sorted
	extent = extents.get("5855");
	assertEquals(date("1957-01-01T00:00:00Z"), extent.first);
	assertEquals(date("1997-04-01T00:00:00Z"), extent.last);
	assertEquals(16, extent.count);
    }

    @Test
    public void dailyExtentsTest() throws Exception {

	Map<String, Extent> extents = RArcticNetClient.parseExtents(reader(DAILY), RArcticNetDataset.DAILY);

	Extent extent = extents.get("1001");
	assertEquals(date("1968-01-01T00:00:00Z"), extent.first);
	assertEquals(date("1968-10-01T00:00:00Z"), extent.last);
	// February 30 is discarded
	assertEquals(10 + 9 + 8 + 5, extent.count);

	assertEquals(1, extents.get("10012").count);
    }

    @Test
    public void createSeriesTest() throws Exception {

	RArcticNetEntity series = createMonthlySeries();

	assertEquals("5855|MONTHLY", series.getSeriesId());
	assertEquals("5855", series.getPointId());
	assertEquals("1001", series.getCode());
	assertEquals("Kolyma At Orotuk", series.getName());
	assertEquals(RArcticNetDataset.MONTHLY, series.getDataset().get());
	assertEquals("1957-01-01T00:00:00Z", series.getString(RArcticNetClient.BEGIN));
	assertEquals("1997-04-01T00:00:00Z", series.getString(RArcticNetClient.END));

	RArcticNetEntity daily = createDailySeries();
	assertEquals("5855|DAILY", daily.getSeriesId());
	assertEquals("1968-10-01T00:00:00Z", daily.getString(RArcticNetClient.END));

	// sites without values are discarded
	JSONObject site = RArcticNetClient.parseSiteAttributes(reader(SITE_ATTRIBUTES)).get("7782");
	assertFalse(RArcticNetClient.createSeries(site, RArcticNetDataset.MONTHLY, null).isPresent());
    }

    @Test
    public void parseObservationsTest() throws Exception {

	TreeMap<Date, BigDecimal> all = RArcticNetClient.parseObservations(reader(MONTHLY), RArcticNetDataset.MONTHLY, "1", null,
		null);
	assertEquals(13, all.size());
	assertEquals(new BigDecimal("27.100"), all.get(date("1908-07-01T00:00:00Z")));
	assertEquals(new BigDecimal("6.170"), all.lastEntry().getValue());

	TreeMap<Date, BigDecimal> range = RArcticNetClient.parseObservations(reader(MONTHLY), RArcticNetDataset.MONTHLY, "1",
		date("1909-01-01T00:00:00Z"), date("1909-06-01T00:00:00Z"));
	assertEquals(2, range.size());
	assertEquals(date("1909-05-01T00:00:00Z"), range.firstKey());

	// the code "10012" starts with "1001", but it is a different gauge
	TreeMap<Date, BigDecimal> daily = RArcticNetClient.parseObservations(reader(DAILY), RArcticNetDataset.DAILY, "1001", null,
		null);
	assertEquals(32, daily.size());
	assertEquals(new BigDecimal("1.93"), daily.get(date("1968-02-29T00:00:00Z")));
	assertNull(daily.get(date("1968-03-02T00:00:00Z")));
    }

    @Test
    public void seriesIdTest() {

	RArcticNetEntity series = RArcticNetClient.parseSeriesId("5855|DAILY");
	assertEquals("5855", series.getPointId());
	assertEquals(RArcticNetDataset.DAILY, series.getDataset().get());
	assertEquals("5855|DAILY", series.getSeriesId());

	assertNull(RArcticNetClient.parseSeriesId("5855"));
	assertNull(RArcticNetClient.parseSeriesId("5855|WEEKLY"));
	assertNull(RArcticNetClient.parseSeriesId("x|MONTHLY"));
	assertNull(RArcticNetClient.parseSeriesId(null));
    }

    @Test
    public void httpDateTest() {

	String text = "Wed, 27 Jul 2005 20:16:57 GMT";
	long millis = RArcticNetClient.parseHttpDate(text).get();
	assertEquals(date("2005-07-27T20:16:57Z").getTime(), millis);
	assertEquals(text, RArcticNetClient.formatHttpDate(millis));
	assertTrue(RArcticNetClient.parseHttpDate("invalid").isEmpty());
    }

    /**
     * @param text
     * @return
     */
    static BufferedReader reader(String text) {

	return new BufferedReader(new StringReader(text));
    }

    /**
     * @param text
     * @return
     */
    static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
