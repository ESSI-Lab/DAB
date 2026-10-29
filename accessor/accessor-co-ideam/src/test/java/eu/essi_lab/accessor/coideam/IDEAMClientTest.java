package eu.essi_lab.accessor.coideam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class IDEAMClientTest {

    static final String AGGREGATION_ROW = "{\"codigoestacion\":\"0021209920\",\"codigosensor\":\"0230\","
	    + "\"begin\":\"2019-03-31T07:00:00.000\",\"end\":\"2026-10-26T23:00:00.000\",\"count\":\"32486\"}";

    static final String LATEST_ROW = "{\"codigoestacion\":\"0021209920\",\"codigosensor\":\"0230\","
	    + "\"fechaobservacion\":\"2026-10-26T23:00:00.000\",\"valorobservado\":\"0.54\",\"nombreestacion\":\"SANTA ROSITA [21209920]\","
	    + "\"departamento\":\"Cundinamarca\",\"municipio\":\"Suesca\",\"zonahidrografica\":\"Alto Magdalena\",\"latitud\":\"5.105878\","
	    + "\"longitud\":\"-73.753646\",\"descripcionsensor\":\"NIVEL INSTANTANEO\",\"unidadmedida\":\"mt\"}";

    static final String CATALOG_ROW = "{\"codigo\":\"0021209920\",\"nombre\":\"SANTA ROSITA [21209920]\",\"categoria\":\"Limnigráfica\","
	    + "\"tecnologia\":\"Automática con Telemetría, Convencional\",\"estado\":\"Activa\",\"departamento\":\"Cundinamarca\","
	    + "\"municipio\":\"Suesca\",\"altitud\":\"2598\",\"longitud\":\"-73.753646\",\"latitud\":\"5.105878\","
	    + "\"fecha_instalacion\":\"19/05/2005\",\"corriente\":\"BOGOTÁ\",\"area_hidrografica\":\"Magdalena Cauca\","
	    + "\"zona_hidrografica\":\"Alto Magdalena\",\"subzona_hidrografica\":\"Río Bogotá\","
	    + "\"entidad\":\"INSTITUTO DE HIDROLOGÍA METEOROLOGÍA Y ESTUDIOS AMBIENTALES\"}";

    /**
     * @return
     */
    static IDEAMEntity createWaterLevelSeries() {

	IDEAMEntity series = IDEAMClient.createSeries(IDEAMDataset.NIVEL, new JSONObject(AGGREGATION_ROW)).get();
	IDEAMClient.addLatestRow(series, new JSONObject(LATEST_ROW));
	IDEAMClient.addCatalogEntry(series, new JSONObject(CATALOG_ROW));
	return series;
    }

    @Test
    public void createSeriesTest() {

	IDEAMEntity series = IDEAMClient.createSeries(IDEAMDataset.NIVEL, new JSONObject(AGGREGATION_ROW)).get();

	assertEquals("NIVEL|0021209920|0230", series.getSeriesId());
	assertEquals(IDEAMDataset.NIVEL, series.getDataset().get());
	assertEquals("0021209920", series.getStationCode());
	assertEquals("0230", series.getSensorCode());
	// Colombian legal time is UTC-5
	assertEquals("2019-03-31T12:00:00Z", series.getString(IDEAMClient.BEGIN));
	assertEquals("2026-10-27T04:00:00Z", series.getString(IDEAMClient.END));
	assertEquals(32486, series.getObject().getLong(IDEAMClient.COUNT));

	JSONObject invalid = new JSONObject(AGGREGATION_ROW);
	invalid.remove("end");
	assertFalse(IDEAMClient.createSeries(IDEAMDataset.NIVEL, invalid).isPresent());

	IDEAMEntity complete = createWaterLevelSeries();
	assertEquals("SANTA ROSITA [21209920]", complete.getStationName());
	assertEquals("mt", complete.getUnit());
	assertEquals("BOGOTÁ", complete.getString(IDEAMClient.STREAM));
	assertEquals("Limnigráfica", complete.getString(IDEAMClient.CATEGORY));
    }

    @Test
    public void seriesIdTest() {

	IDEAMEntity series = IDEAMClient.parseSeriesId("NIVEL_MAX|0021209920|0231");
	assertEquals(IDEAMDataset.NIVEL_MAX, series.getDataset().get());
	assertEquals("0021209920", series.getStationCode());
	assertEquals("0231", series.getSensorCode());
	assertEquals("codigoestacion = '0021209920' AND codigosensor = '0231'", IDEAMClient.getSeriesFilter(series));

	assertNull(IDEAMClient.parseSeriesId("NIVEL|0021209920"));
	assertNull(IDEAMClient.parseSeriesId("X|0021209920|0230"));
	assertNull(IDEAMClient.parseSeriesId("NIVEL||0230"));
	assertNull(IDEAMClient.parseSeriesId(null));
    }

    @Test
    public void parseObservationsTest() {

	JSONArray rows = new JSONArray("[{\"fechaobservacion\":\"2026-10-26T21:00:00.000\",\"valorobservado\":\"0.54\"},"
		+ "{\"fechaobservacion\":\"2026-10-26T22:00:00.000\"},"
		+ "{\"fechaobservacion\":\"2026-10-26T23:00:00.000\",\"valorobservado\":\"0.55\"}]");

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	String last = IDEAMClient.parseObservations(rows, values);

	assertEquals("2026-10-26T23:00:00.000", last);
	// rows without value are discarded
	assertEquals(2, values.size());
	assertEquals(date("2026-10-27T02:00:00Z"), values.firstKey());
	assertEquals(new BigDecimal("0.55"), values.lastEntry().getValue());

	assertNull(IDEAMClient.parseObservations(new JSONArray(), values));
    }

    @Test
    public void datesTest() {

	assertEquals("2026-10-26T23:00:00.000", IDEAMClient.formatDate(date("2026-10-27T04:00:00Z")));
	assertEquals(date("2001-01-01T05:32:00Z"), IDEAMClient.parseDate("2001-01-01T00:32:00.000").get());
	assertFalse(IDEAMClient.parseDate("").isPresent());
    }

    @Test
    public void urlTest() {

	IDEAMClient client = new IDEAMClient("https://www.datos.gov.co/resource/bdmn-sqnh.json");

	LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	parameters.put("$where", "codigoestacion = '0021209920'");
	parameters.put("$limit", "1");

	assertEquals("https://www.datos.gov.co/resource/bdmn-sqnh.json?$where=codigoestacion%20%3D%20%270021209920%27&$limit=1",
		client.buildURL("bdmn-sqnh", parameters));
    }

    @Test
    public void datasetTest() {

	assertEquals(IDEAMDataset.NIVEL, IDEAMDataset.decode("bdmn-sqnh").get());
	assertEquals(IDEAMDataset.PRECIPITACION, IDEAMDataset.decode("precipitacion").get());
	assertFalse(IDEAMDataset.decode("caudal").isPresent());

	IDEAMConnectorSetting setting = new IDEAMConnectorSetting();
	assertEquals(Arrays.asList(IDEAMDataset.NIVEL, IDEAMDataset.NIVEL_MIN, IDEAMDataset.NIVEL_MAX), setting.getDatasets());
	assertNull(setting.getAppToken());
	assertTrue(setting.getStations().isEmpty());
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
