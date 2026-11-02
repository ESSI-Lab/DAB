package eu.essi_lab.accessor.adhi;

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

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.adhi.ADHIClient.DatasetInfo;
import eu.essi_lab.accessor.adhi.ADHIClient.SeriesSummary;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public class ADHIClientTest {

    static final String STATIONS = "ID,Station_co,Name,Data_sourc,Catchment,Mean_altit,Maximum_al,Minimum_al,Mean_annual_precip,"
	    + "Mean_annual_pet,Mean_annual_temp,lc_forest,lc_urban,lc_crop,lc_crop_irrig,lc_grass,lc_shrub,lc_sparse,lc_bare,Starting_y,"
	    + "Ending_yea,Number_dams,Country,Longitude,Latitude,Flag,Comment\n" //
	    + "ADHI_1,CMQ0001,VINA NORD (BINI) a PONT DE BEREM,SIEREM,1566.639483,1111,1241,926,1492.93,1201.01,23.07,0.81,0.01,0.11,"
	    + "0.00,0.00,0.08,0.00,0.00,1963,1980,0,Cameroon,13.95,7.55,0, \n" //
	    + "ADHI_2,CMQ5002,KADEY a PANA,SIEREM,20015.85357,723.1,1015,602,1548.05,1059.42,24.17,0.88,0.00,0.07,0.00,0.04,0.00,0.00,"
	    + "0.00,1974,1992,0,Cameroon,14.6833,4.2,1,gap filling\n" //
	    + "ADHI_414,TNQ0046,TinÃ© a Pont,SIEREM,373.1876446,291.1,394,173,633.27,1258.81,17.59,0.00,0.00,0.99,0.00,0.00,"
	    + "0.00,0.00,0.00,1998,2013,0,Tunisia,9.67,36.87916667,0, \n";

    static final String MONTHLY = "1963,3,NaN,NaN,NaN,31\n" //
	    + "1963,4,NaN,NaN,NaN,30\n" //
	    + "1963,5,10.31,16.29,5.46,7\n" //
	    + "1963,6,18.98,33.29,7.46,0\n" //
	    + "1963,7,20.55,27.6,14.69,0\n" //
	    + "1963,8,NaN,NaN,NaN,31\n";

    static final String ANNUAL_MAX = "1964,9,18,126\n" //
	    + "1965,9,3,218\n" //
	    + "1966,9,19,147\n" //
	    + "1967,8,4,70.09\n";

    static final String ANNUAL_PERCENTILES = "1964,5.46,6.48,10.6,22,46.29,62.55,70.04,89.77\n" //
	    + "1965,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN\n" //
	    + "1966,4.64,5.61,11.1,23.6,42.05,63.79,72.5,99.02\n";

    static final String DATASET = "{\"status\":\"OK\",\"data\":{\"id\":1416,\"identifier\":\"LXGXQ9\",\"latestVersion\":{"
	    + "\"versionNumber\":6,\"versionMinorNumber\":1,\"versionState\":\"RELEASED\",\"license\":\"NONE\","
	    + "\"termsOfUse\":\"<a rel=\\\"license\\\" href=\\\"http://creativecommons.org/licenses/by/4.0/\\\">CC BY</a>\","
	    + "\"files\":[{\"label\":\"ADHI_MonthlySeries.zip\",\"dataFile\":{\"id\":4916,\"filename\":\"ADHI_MonthlySeries.zip\","
	    + "\"md5\":\"e8cda031b91251175c290c43aa544835\",\"checksum\":{\"type\":\"MD5\",\"value\":\"e8cda031b91251175c290c43aa544835\"}}},"
	    + "{\"label\":\"ADHI_stations.tab\",\"dataFile\":{\"id\":4944,\"filename\":\"ADHI_stations.tab\","
	    + "\"originalFileName\":\"ADHI_stations.csv\",\"md5\":\"3bb0d89737193f32b6948dae6867c9eb\","
	    + "\"checksum\":{\"type\":\"MD5\",\"value\":\"3bb0d89737193f32b6948dae6867c9eb\"}}},"
	    + "{\"label\":\" ADHI_Catchment_boundaries.zip\",\"dataFile\":{\"id\":5052,\"filename\":\" ADHI_Catchment_boundaries.zip\"}}"
	    + "]}}}";

    /**
     * @return
     */
    static ADHIEntity createMonthlySeries() {

	JSONObject station = ADHIClient.parseStations(STATIONS).get(1);

	SeriesSummary summary = new SeriesSummary();
	ADHIClient.parseSeries(MONTHLY, ADHIVariable.MONTHLY_MEAN).keySet().forEach(summary::add);

	return ADHIClient.createSeries(station, ADHIVariable.MONTHLY_MEAN, summary,
		ADHIClient.parseDatasetInfo(new JSONObject(DATASET)));
    }

    @Test
    public void stationsTest() {

	List<JSONObject> stations = ADHIClient.parseStations(STATIONS);
	assertEquals(3, stations.size());

	JSONObject station = stations.get(0);
	assertEquals("ADHI_1", station.getString(ADHIClient.STATION_ID));
	assertEquals("CMQ0001", station.getString(ADHIClient.STATION_CODE));
	assertEquals("VINA NORD (BINI) a PONT DE BEREM", station.getString(ADHIClient.STATION_NAME));
	assertEquals("13.95", station.getString(ADHIClient.LON));
	assertEquals("7.55", station.getString(ADHIClient.LAT));
	assertFalse(station.has(ADHIClient.COMMENT));

	assertEquals("gap filling", stations.get(1).getString(ADHIClient.COMMENT));

	// repaired double UTF-8 encoding
	assertEquals("Tiné a Pont", stations.get(2).getString(ADHIClient.STATION_NAME));
	assertEquals("Tiné", ADHIClient.repairEncoding("Tiné"));
    }

    @Test
    public void seriesTest() {

	TreeMap<Date, BigDecimal> mean = ADHIClient.parseSeries(MONTHLY, ADHIVariable.MONTHLY_MEAN);
	assertEquals(3, mean.size());
	assertEquals("1963-05-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(mean.firstKey()));
	assertEquals(new BigDecimal("10.31"), mean.firstEntry().getValue());
	assertEquals("1963-07-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(mean.lastKey()));

	assertEquals(new BigDecimal("16.29"), ADHIClient.parseSeries(MONTHLY, ADHIVariable.MONTHLY_MAX).firstEntry().getValue());
	assertEquals(new BigDecimal("5.46"), ADHIClient.parseSeries(MONTHLY, ADHIVariable.MONTHLY_MIN).firstEntry().getValue());

	TreeMap<Date, BigDecimal> max = ADHIClient.parseSeries(ANNUAL_MAX, ADHIVariable.ANNUAL_MAX);
	assertEquals(4, max.size());
	assertEquals("1964-01-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(max.firstKey()));
	assertEquals(new BigDecimal("126"), max.firstEntry().getValue());

	TreeMap<Date, BigDecimal> median = ADHIClient.parseSeries(ANNUAL_PERCENTILES, ADHIVariable.ANNUAL_Q50);
	assertEquals(2, median.size());
	assertEquals(new BigDecimal("22"), median.firstEntry().getValue());
	assertEquals(new BigDecimal("99.02"), ADHIClient.parseSeries(ANNUAL_PERCENTILES, ADHIVariable.ANNUAL_Q99).lastEntry().getValue());
    }

    @Test
    public void datasetInfoTest() {

	DatasetInfo info = ADHIClient.parseDatasetInfo(new JSONObject(DATASET));

	assertEquals("6.1", info.getVersion());
	assertEquals(ADHIMapper.CC_BY_4, info.getLicense());

	assertEquals(4916, info.getFile(ADHIVariable.MONTHLY_FILE).get().getId());
	assertEquals("e8cda031b91251175c290c43aa544835", info.getFile(ADHIVariable.MONTHLY_FILE).get().getMd5());

	// by original name
	assertEquals(4944, info.getFile(ADHIClient.STATIONS_FILE).get().getId());

	// trimmed name
	assertEquals(5052, info.getFile("ADHI_Catchment_boundaries.zip").get().getId());

	// not listed: known identifier
	assertEquals(4919, info.getFile(ADHIVariable.ANNUAL_MEAN_FILE).get().getId());
	assertNull(info.getFile(ADHIVariable.ANNUAL_MEAN_FILE).get().getMd5());
	assertFalse(info.getFile("unknown.zip").isPresent());
    }

    @Test
    public void seriesIdTest() {

	assertEquals("ADHI_1:MONTHLY_MEAN", ADHIClient.buildSeriesId("ADHI_1", ADHIVariable.MONTHLY_MEAN));

	ADHIEntity entity = ADHIClient.parseSeriesId("ADHI_1:ANNUAL_7DAY_MIN");
	assertEquals("ADHI_1", entity.getStationId());
	assertEquals(ADHIVariable.ANNUAL_7DAY_MIN, entity.getVariable().get());

	assertNull(ADHIClient.parseSeriesId("ADHI_1"));
	assertNull(ADHIClient.parseSeriesId("ADHI_1:UNKNOWN"));

	assertEquals("ADHI_1", ADHIClient.getStationId("MonthlySeries/monthly_ADHI_1.txt").get());
	assertEquals("ADHI_1000", ADHIClient.getStationId("AnnualMax/annualmax_ADHI_1000.txt").get());
	assertFalse(ADHIClient.getStationId("MonthlySeries/").isPresent());
    }

    @Test
    public void createSeriesTest() {

	ADHIEntity series = createMonthlySeries();

	assertEquals("ADHI_2:MONTHLY_MEAN", series.getSeriesId());
	assertEquals("1963-05-01T00:00:00Z", series.getString(ADHIClient.BEGIN));
	assertEquals("1963-07-01T00:00:00Z", series.getString(ADHIClient.END));
	assertEquals(3, series.getObject().getInt(ADHIClient.VALUES));
	assertEquals("6.1", series.getString(ADHIClient.DATASET_VERSION));
    }

    @Test
    public void endpointTest() {

	assertEquals("https://dataverse.ird.fr/", ADHIClient.getBaseURL(ADHIClient.DEFAULT_ENDPOINT));
	assertEquals("https://dataverse.ird.fr/", ADHIClient.getBaseURL("https://dataverse.ird.fr"));
	assertEquals("doi:10.23708/LXGXQ9", ADHIClient.getPersistentId(ADHIClient.DEFAULT_ENDPOINT));
	assertEquals("doi:10.23708/LXGXQ9", ADHIClient.getPersistentId("https://dataverse.ird.fr/"));
	assertEquals("doi:10.23708/LXGXQ9",
		ADHIClient.getPersistentId("https://dataverse.ird.fr/api/datasets/:persistentId/?persistentId=doi%3A10.23708%2FLXGXQ9"));
    }

    @Test
    public void cachedFileTest() throws Exception {

	// a fake file of the dataset, already in the cache
	JSONObject json = new JSONObject(DATASET);
	JSONObject dataFile = json.getJSONObject("data").getJSONObject("latestVersion").getJSONArray("files").getJSONObject(0)
		.getJSONObject("dataFile");
	dataFile.put("id", 999999916L);
	dataFile.getJSONObject("checksum").put("value", "test");

	DatasetInfo info = ADHIClient.parseDatasetInfo(json);

	Path folder = Path.of(System.getProperty("java.io.tmpdir"), ADHIClient.CACHE_FOLDER);
	Files.createDirectories(folder);
	File zip = folder.resolve("999999916_test_" + ADHIVariable.MONTHLY_FILE).toFile();

	try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip.toPath()))) {
	    out.putNextEntry(new ZipEntry("MonthlySeries/"));
	    out.closeEntry();
	    out.putNextEntry(new ZipEntry("MonthlySeries/monthly_ADHI_1.txt"));
	    out.write(MONTHLY.getBytes(StandardCharsets.UTF_8));
	    out.closeEntry();
	    out.putNextEntry(new ZipEntry("MonthlySeries/monthly_ADHI_10.txt"));
	    out.write("1963,1,NaN,NaN,NaN,31\n".getBytes(StandardCharsets.UTF_8));
	    out.closeEntry();
	}

	try {

	    ADHIClient client = new ADHIClient();

	    Map<ADHIVariable, Map<String, SeriesSummary>> summaries = client.retrieveSummaries(info,
		    Arrays.asList(ADHIVariable.MONTHLY_MEAN, ADHIVariable.MONTHLY_MAX));

	    assertEquals(1, summaries.get(ADHIVariable.MONTHLY_MEAN).size());
	    SeriesSummary summary = summaries.get(ADHIVariable.MONTHLY_MEAN).get("ADHI_1");
	    assertEquals(3, summary.getValues());
	    assertEquals("1963-05-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(summary.getFirst()));
	    assertEquals("1963-07-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(summary.getLast()));
	    assertTrue(summaries.get(ADHIVariable.MONTHLY_MAX).containsKey("ADHI_1"));

	    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1963-06-01T00:00:00Z").get();
	    TreeMap<Date, BigDecimal> values = client.retrieveObservations(info, "ADHI_1", ADHIVariable.MONTHLY_MIN, begin, null);
	    assertEquals(2, values.size());
	    assertEquals(new BigDecimal("7.46"), values.firstEntry().getValue());

	    assertTrue(client.retrieveObservations(info, "ADHI_2", ADHIVariable.MONTHLY_MIN, null, null).isEmpty());

	} finally {

	    zip.delete();
	}
    }

    @Test(expected = GSException.class)
    public void unknownFileTest() throws GSException {

	new ADHIClient().getFile(new DatasetInfo(), "unknown.zip");
    }
}
