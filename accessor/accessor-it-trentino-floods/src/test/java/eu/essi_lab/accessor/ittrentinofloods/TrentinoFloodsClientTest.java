package eu.essi_lab.accessor.ittrentinofloods;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class TrentinoFloodsClientTest {

    /**
     * Excerpt of https://www.floods.it/api/v1/monitoring/index.json
     */
    static final String INDEX = "{\"type\":\"FeatureCollection\",\"language\":\"en-US\",\"features\":[" //
	    + feature("42437", "Ponte Arche", "fiume Sarca", "m3/s", "discharge", "derived from measurement", "10.87,46.037222") + "," //
	    + feature("32207", "Ponte Arche", "fiume Sarca", "m", "gage height", "measurement", "10.870244,46.037477") + "," //
	    + feature("28210", "Ala", "torrente Ala", "m", "gage height", "measurement", "11.006637,45.758606") + "," //
	    + feature("99999", "Unknown", "", "mm", "precipitation", "measurement", "11.0,46.0") + "," //
	    // no series identifier
	    + "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[11.0,46.0]},\"properties\":{\"metadata\":{\"site\":\"X\"}}}"
	    + "]}";

    /**
     * Excerpt of https://www.floods.it/api/v1/monitoring/32207.json
     */
    static final String DATA = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\","
	    + "\"coordinates\":[0,0]},\"properties\":{\"metadata\":{\"time_format\":\"epoch\",\"tz\":\"UTC\",\"api_warnings\":null},"
	    + "\"data\":{\"timestamp\":[1793055600,1793056500,1793057400,1793058300,1793059200],"
	    + "\"value\":[0.18,null,\"n.d.\",0.17,0.18],\"quality\":null}}}]}";

    /**
     * @return
     */
    static String feature(String id, String site, String waterbody, String uom, String quantity, String method, String coordinates) {

	return "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + coordinates + "]},\"properties\":{\"metadata\":{"
		+ "\"site\":\"" + site + "\",\"water body\":\"" + waterbody + "\",\"uom\":\"" + uom + "\",\"quantity\":\"" + quantity
		+ "\",\"quantity type\":\"instantaneous\",\"collection method\":\"" + method + "\","
		+ "\"author\":\"Provincia Autonoma di Trento – Ufficio Dighe\",\"series id\":\"" + id + "\","
		+ "\"url json\":\"https://www.floods.it/api/v1/monitoring/" + id + ".json\","
		+ "\"url csv\":\"https://www.floods.it/api/v1/monitoring/" + id + ".csv\"}}}";
    }

    /**
     * @return
     */
    static List<TrentinoFloodsEntity> createSeries() {

	return TrentinoFloodsClient.parseIndex(INDEX);
    }

    @Test
    public void indexTest() {

	List<TrentinoFloodsEntity> series = createSeries();

	// the feature without identifier is discarded, sorted by site and series
	assertEquals(4, series.size());
	assertEquals("28210", series.get(0).getSeriesId());
	assertEquals("32207", series.get(1).getSeriesId());
	assertEquals("42437", series.get(2).getSeriesId());
	assertEquals("99999", series.get(3).getSeriesId());

	TrentinoFloodsEntity discharge = series.get(2);
	assertEquals(TrentinoFloodsProperty.DISCHARGE, discharge.getProperty().get());
	assertEquals("Ponte Arche", discharge.getSite());
	assertEquals("fiume Sarca", discharge.getString(TrentinoFloodsClient.WATERBODY));
	assertEquals("m3/s", discharge.getString(TrentinoFloodsClient.UOM));
	assertEquals("derived from measurement", discharge.getString(TrentinoFloodsClient.COLLECTION_METHOD));
	assertEquals("Provincia Autonoma di Trento – Ufficio Dighe", discharge.getString(TrentinoFloodsClient.AUTHOR));

	// GeoJSON coordinates are longitude, latitude
	assertEquals(new BigDecimal("46.037222"), discharge.getObject().getBigDecimal(TrentinoFloodsClient.LAT));
	assertEquals(new BigDecimal("10.87"), discharge.getObject().getBigDecimal(TrentinoFloodsClient.LON));

	assertEquals(TrentinoFloodsProperty.LEVEL, series.get(1).getProperty().get());

	// unknown quantities are kept, with their original name and unit
	assertFalse(series.get(3).getProperty().isPresent());
	assertEquals("precipitation", series.get(3).getString(TrentinoFloodsClient.QUANTITY));
    }

    @Test
    public void dataTest() {

	TreeMap<Date, BigDecimal> values = TrentinoFloodsClient.parseData(DATA);

	// missing and not numeric values are skipped
	assertEquals(3, values.size());

	// epoch times are UTC
	assertEquals("2026-10-26T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	assertEquals(new BigDecimal("0.18"), values.firstEntry().getValue());
	assertEquals("2026-10-27T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));

	assertTrue(TrentinoFloodsClient.parseData("{\"type\":\"FeatureCollection\",\"features\":[]}").isEmpty());
    }

    @Test
    public void seriesIdTest() {

	TrentinoFloodsEntity series = TrentinoFloodsClient.parseSeriesId(" 32207 ");
	assertEquals("32207", series.getSeriesId());

	assertNull(TrentinoFloodsClient.parseSeriesId(""));
	assertNull(TrentinoFloodsClient.parseSeriesId("../index"));
	assertNull(TrentinoFloodsClient.parseSeriesId(null));
    }

    @Test
    public void endpointTest() {

	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT, TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/api/v1/monitoring"));
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT,
		TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/api/v1/monitoring/index.json"));
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT, TrentinoFloodsClient.normalizeEndpoint(null));

	// the discontinued open data page is redirected to the monitoring API
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT, TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/public/Opendata/"));
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT,
		TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/public/Opendata/ODdownload.php?Dataset=0"));
    }
}
