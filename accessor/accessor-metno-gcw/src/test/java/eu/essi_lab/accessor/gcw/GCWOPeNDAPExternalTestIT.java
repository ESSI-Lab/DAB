package eu.essi_lab.accessor.gcw;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;

/**
 * Describes and reads one dataset of each OPeNDAP family referenced by the GCW catalogue
 */
public class GCWOPeNDAPExternalTestIT {

    private static final long DAY = 24 * 60 * 60 * 1000l;

    @Test
    public void testSynop() throws Exception {

	GCWTimeSeriesDataset dataset = check("https://thredds.met.no/thredds/dodsC/arcticdata/obsSynop/01028", "TA");
	assertEquals("01028", dataset.getStation().getString(GCWTimeSeriesDataset.STATION_ID));
    }

    @Test
    public void testAWS() throws Exception {

	check("https://thredds.met.no/thredds/dodsC/arcticdata/infranor/UiO-Kongsvegen-AWS/UiO-Kongsvegen-AWS-sw250-agg.ncml", "ta");
    }

    @Test
    public void testPermafrostProfile() throws Exception {

	GCWTimeSeriesDataset dataset = check("https://thredds.met.no/thredds/dodsC/arcticdata/frost2netcdf-permafrost/SN99872/SN99872-aggregated.ncml",
		"soil_temperature[0]");
	JSONObject level = dataset.getSeries().get(0);
	assertTrue(level.has(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE));
    }

    @Test
    public void testGEUS() throws Exception {

	check("https://thredds.geus.dk/thredds/dodsC/aws/l2stations/netcdf/hour/QAS_M_hour.nc", "t_u");
    }

    @Test
    public void testHyrax() throws Exception {

	String url = "https://hyrax.igf.edu.pl/opendap/hyrax/SIOS_Core_Data/TASK1/IG_PAS_SIOS_SCD_2_8_SNOW_DEPTH_211110.nc";
	try {
	    GCWOPeNDAP.getTimeExtent(url);
	} catch (IOException e) {
	    // the server certificate is issued by the HARICA TLS RSA Root CA 2021, missing from older JVM trust stores
	    Assume.assumeNoException(e);
	}
	check(url, null);
    }

    /**
     * Describes the dataset and reads the last 30 days of the given series (the first series if null)
     */
    private GCWTimeSeriesDataset check(String url, String seriesName) throws Exception {

	long start = System.currentTimeMillis();
	GCWTimeSeriesDataset dataset = GCWOPeNDAP.describe(url, null);
	System.out.println("\n" + url + " described in " + (System.currentTimeMillis() - start) + " ms");
	System.out.println("  time steps " + dataset.getTimeSize() + " from " + new Date(dataset.getBegin()) + " to " + new Date(dataset.getEnd()));
	System.out.println("  station " + dataset.getStation());
	System.out.println("  global title: " + dataset.getGlobalAttributes().get("title"));
	for (JSONObject series : dataset.getSeries()) {
	    System.out.println("  series " + GCWOPeNDAP.getSeriesName(series.getString(GCWTimeSeriesDataset.SERIES_VARIABLE),
		    series.has(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) ? series.getInt(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) : null) + " | "
		    + series.optString("standard_name") + " | " + series.optString("long_name") + " | " + series.optString("units") + " | level "
		    + series.opt(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE));
	}

	assertFalse(dataset.getSeries().isEmpty());
	assertNotNull(dataset.getEnd());

	if (seriesName == null) {
	    JSONObject first = dataset.getSeries().get(0);
	    seriesName = GCWOPeNDAP.getSeriesName(first.getString(GCWTimeSeriesDataset.SERIES_VARIABLE),
		    first.has(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) ? first.getInt(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) : null);
	}

	start = System.currentTimeMillis();
	Date end = new Date(dataset.getEnd());
	List<SimpleEntry<Date, Double>> values = GCWOPeNDAP.readValues(url, seriesName, new Date(end.getTime() - 30 * DAY), end);
	System.out.println("  read " + values.size() + " values of " + seriesName + " in " + (System.currentTimeMillis() - start) + " ms, first "
		+ (values.isEmpty() ? "-" : values.get(0)) + " last " + (values.isEmpty() ? "-" : values.get(values.size() - 1)));
	assertFalse(values.isEmpty());
	assertTrue(values.get(0).getKey().getTime() >= end.getTime() - 30 * DAY);

	return dataset;
    }
}
