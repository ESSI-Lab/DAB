package eu.essi_lab.accessor.essaihjucar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class SAIHJucarClientTest {

    /**
     * Entry of https://saih.chj.es/mapa-aforos
     */
    static final String GAUGE = "{\"idVariable\":\"14099\",\"fldTNombre\":\"AZUD FONT D'EN CARRÓS\",\"fldTTipo\":\"Af\","
	    + "\"fldTNombreVariable\":\"CAUDAL RÍO SERPIS SOBRE EL AZUD\",\"fldTCodigo\":\"9L01\",\"idEstacionRemota\":\"349\","
	    + "\"fldTEstado\":true,\"fldNCoordGPSLat\":742223.0284451315,\"fldNCoordGPSLon\":4309753.964542853,"
	    + "\"fldTPoblacion\":\"Villalonga\",\"fldTProvincia\":\"Valencia\",\"fldFUmbralBajo\":20,\"fldFUmbralMedio\":50,"
	    + "\"fldFUmbralAlto\":150,\"fldNUltimoValor\":3.4404934330638555,\"fldDFechaComunicacion\":\"2026-10-27T20:05:00.000Z\","
	    + "\"fldBEstado\":0,\"fldTSubCuenca\":\"Serpis\",\"fldNOrdenSubCuenca\":100,\"fldNOrdenEstacion\":723110,\"tieneFicha\":true}";

    /**
     * Entry of https://saih.chj.es/mapa-embalses
     */
    static final String RESERVOIR = "{\"fldTNombre\":\"EMBALSE DE ULLDECONA\",\"fldTCodigo\":\"1E01\",\"idEstacionRemota\":\"243\","
	    + "\"fldTEstado\":true,\"fldNCoordGPSLat\":773321.0092183154,\"fldNCoordGPSLon\":4507235.015607328,\"fkNSubCuenca\":\"9\","
	    + "\"idCotaEmbalse\":\"2488\",\"idVolumenEmbalse\":\"2489\",\"fldTPoblacion\":\"La Pobla de Benifassà\","
	    + "\"fldTProvincia\":\"Castellón\",\"fldFVolumenNMN\":9.11,\"fldFCotaVertido\":473.53,\"valorCotaEmbalse\":469.432495,"
	    + "\"valorVolumenEmbalse\":6.914400419100016,\"fechaComunicacionVol\":\"2026-10-27T20:05:00.000Z\","
	    + "\"estadoVolumenEmbalse\":128,\"fechaComunicacionCota\":\"2026-10-27T20:05:00.000Z\",\"fldTSubCuenca\":\"Cenia - Maestrazgo\","
	    + "\"idCaudalRecibido\":\"13862\",\"idCaudalSalida\":\"13848\",\"idCaudalSalidaRio\":\"16683\"}";

    /**
     * Entry of https://saih.chj.es/mapa-lluvias
     */
    static final String RAIN_STATION = "{\"idEstacionRemota\":\"340\",\"fldTNombre\":\"ABDET\",\"fldTCodigo\":\"8P06\",\"fldTEstado\":true,"
	    + "\"fldNCoordGPSLat\":738519.9909288103,\"fldNCoordGPSLon\":4286486.022239707,\"fldTPoblacion\":\"Confrides\","
	    + "\"fldTProvincia\":\"Alicante\",\"lluvia_1h\":0,\"fecha_1h\":\"2026-10-27T20:00:00.000Z\"}";

    /**
     * Extract of https://saih.chj.es/chart-lluvia/340
     */
    static final String RAIN_CHART_PAGE = "<script>let title = \"ABDET - Variables de lluvia\";\n" //
	    + "let varLluvia = [\n" //
	    + "\t{\"idVariable\":\"14481\",\"fldNTipo\":1,\"fkNFuncion\":\"12\",\"fldTNombre\":\"INTENSIDAD DE LLUVIA CALCULADA\","
	    + "\"fkNEstacion\":\"340\",\"fldTCodigo\":\"AA01MVCR\",\"fldTDescripcion\":\"Intensidad de lluvia cincominutal\","
	    + "\"unidadVariable\":\"mm/h\",\"nombreVariable\":\"INTENSIDAD DE LLUVIA CALCULADA\",\"codigoVariable\":\"AA01MVCR\","
	    + "\"nombreEstacion\":\"ABDET\"},\n" //
	    + "\t{\"idVariable\":\"14474\",\"fldNTipo\":0,\"fkNFuncion\":null,\"fldTNombre\":\"LLUVIA ACUMULADA\",\"fkNEstacion\":\"340\","
	    + "\"fldTCodigo\":\"AQ01MCQR\",\"fldTDescripcion\":\"\",\"unidadVariable\":\"L/m²\",\"nombreVariable\":\"LLUVIA ACUMULADA\"},\n" //
	    + "].filter(Boolean);\n" //
	    + "let varLluvia24 = [\n" //
	    + "\t{\"idVariable\":\"16532\",\"nombreVariable\":\"LLUVIA ACUMULADA EN 24 HORAS\"},\n" //
	    + "].filter(Boolean);</script>";

    /**
     * Response of https://saih.chj.es/admin/variables/valor/14099/2025-10-01T00:00:00/2025-10-01T00:30:00 (modified)
     */
    static final String VALUES = "[{\"valor\":1.3859265218546424,\"fecha\":\"2025-09-30T22:00:00.000Z\",\"estado\":0},"
	    + "{\"valor\":1.2644945226487325,\"fecha\":\"2025-09-30T22:05:00.000Z\",\"estado\":null},"
	    + "{\"valor\":null,\"fecha\":\"2025-09-30T22:10:00.000Z\",\"estado\":0},"
	    + "{\"valor\":9.9,\"fecha\":\"2025-09-30T22:15:00.000Z\",\"estado\":2},"
	    + "{\"valor\":20,\"fecha\":\"2025-09-30T22:20:00.000Z\",\"estado\":128},"
	    + "{\"valor\":1.2052016560410594,\"fecha\":\"2025-09-30T22:25:00.000Z\",\"estado\":129}]";

    /**
     * @return
     */
    static SAIHJucarEntity createGaugeVariable() {

	return SAIHJucarClient.createGaugeVariable(new JSONObject(GAUGE)).get();
    }

    /**
     * @return
     */
    static List<SAIHJucarEntity> createReservoirVariables() {

	return SAIHJucarClient.createReservoirVariables(new JSONObject(RESERVOIR));
    }

    @Test
    public void gaugeVariableTest() {

	SAIHJucarEntity variable = createGaugeVariable();

	assertEquals("14099", variable.getVariableId());
	assertEquals(SAIHJucarVariableType.RIVER_DISCHARGE, variable.getType().get());
	assertEquals("AZUD FONT D'EN CARRÓS", variable.getStationName());
	assertEquals("349", variable.getStationId());
	assertEquals("CAUDAL RÍO SERPIS SOBRE EL AZUD", variable.getVariableName());
	assertEquals("m³/s", variable.getUnit());
	assertEquals("Serpis", variable.getString(SAIHJucarClient.SUB_BASIN));

	// the station page gives 38.903287, -0.206736
	assertEquals(38.903287, variable.getObject().getDouble(SAIHJucarClient.LAT), 0.00001);
	assertEquals(-0.206736, variable.getObject().getDouble(SAIHJucarClient.LON), 0.00001);

	JSONObject reservoirGauge = new JSONObject(GAUGE);
	reservoirGauge.put("fldTTipo", "Em");
	assertEquals(SAIHJucarVariableType.RESERVOIR_RIVER_OUTFLOW,
		SAIHJucarClient.createGaugeVariable(reservoirGauge).get().getType().get());

	JSONObject noVariable = new JSONObject(GAUGE);
	noVariable.remove("idVariable");
	assertFalse(SAIHJucarClient.createGaugeVariable(noVariable).isPresent());
    }

    @Test
    public void reservoirVariablesTest() {

	List<SAIHJucarEntity> variables = createReservoirVariables();

	assertEquals(5, variables.size());

	SAIHJucarEntity level = variables.get(0);
	assertEquals("2488", level.getVariableId());
	assertEquals(SAIHJucarVariableType.RESERVOIR_LEVEL, level.getType().get());
	assertEquals("COTA DE EMBALSE", level.getVariableName());
	assertEquals("m s.n.m.", level.getUnit());
	assertEquals("9.11", level.getString(SAIHJucarClient.RESERVOIR_CAPACITY));

	// the station page gives 40.670727, 0.233592
	assertEquals(40.670727, level.getObject().getDouble(SAIHJucarClient.LAT), 0.00001);
	assertEquals(0.233592, level.getObject().getDouble(SAIHJucarClient.LON), 0.00001);

	assertEquals("2489", variables.get(1).getVariableId());
	assertEquals("hm³", variables.get(1).getUnit());
	assertEquals(SAIHJucarVariableType.RESERVOIR_INFLOW, variables.get(2).getType().get());
	assertEquals(SAIHJucarVariableType.RESERVOIR_OUTFLOW, variables.get(3).getType().get());
	assertEquals("16683", variables.get(4).getVariableId());
	assertEquals(SAIHJucarVariableType.RESERVOIR_RIVER_OUTFLOW, variables.get(4).getType().get());
    }

    @Test
    public void rainVariableTest() {

	JSONArray variables = SAIHJucarClient.parseInlineArray(RAIN_CHART_PAGE, SAIHJucarClient.RAIN_CHART_ARRAY);
	assertEquals(2, variables.length());

	SAIHJucarEntity variable = SAIHJucarClient.createRainVariable(new JSONObject(RAIN_STATION), variables).get();

	assertEquals("14481", variable.getVariableId());
	assertEquals(SAIHJucarVariableType.PRECIPITATION_INTENSITY, variable.getType().get());
	assertEquals("mm/h", variable.getUnit());
	assertEquals("ABDET", variable.getStationName());
	assertEquals("Intensidad de lluvia cincominutal", variable.getString(SAIHJucarClient.VARIABLE_DESCRIPTION));

	assertFalse(SAIHJucarClient.createRainVariable(new JSONObject(RAIN_STATION), new JSONArray()).isPresent());
    }

    @Test
    public void parseInlineArrayTest() {

	String page = "<script>let aforos = [" + GAUGE + "];\nlet otro = 1;</script>";

	JSONArray gauges = SAIHJucarClient.parseInlineArray(page, SAIHJucarClient.GAUGES_ARRAY);
	assertEquals(1, gauges.length());
	assertEquals("14099", gauges.getJSONObject(0).getString("idVariable"));

	assertTrue(SAIHJucarClient.parseInlineArray(page, "embalses").isEmpty());
	// varLluvia24 is not varLluvia
	assertEquals(1, SAIHJucarClient.parseInlineArray(RAIN_CHART_PAGE, "varLluvia24").length());
    }

    @Test
    public void parseValuesTest() {

	TreeMap<Date, BigDecimal> values = SAIHJucarClient.parseValues(new JSONArray(VALUES));

	// missing values and wrong values (state with bit 2) are discarded
	assertEquals(4, values.size());
	assertEquals(date("2025-09-30T22:00:00Z"), values.firstKey());
	assertFalse(values.containsKey(date("2025-09-30T22:10:00Z")));
	assertFalse(values.containsKey(date("2025-09-30T22:15:00Z")));
	assertEquals(new BigDecimal("1.2052016560410594"), values.lastEntry().getValue());
    }

    @Test
    public void formatDateTest() {

	// local time of Spain (summer and winter time)
	assertEquals("2025-10-01T00:00:00", SAIHJucarClient.formatDate(date("2025-09-30T22:00:00Z")));
	assertEquals("2025-01-01T00:00:00", SAIHJucarClient.formatDate(date("2024-12-31T23:00:00Z")));
    }

    @Test
    public void seriesIdTest() {

	assertEquals("14099", SAIHJucarClient.parseSeriesId("14099").getVariableId());
	assertNull(SAIHJucarClient.parseSeriesId("14099/x"));
	assertNull(SAIHJucarClient.parseSeriesId(""));
	assertNull(SAIHJucarClient.parseSeriesId(null));
    }

    @Test
    public void coordinatesTest() {

	SimpleEntry<Double, Double> latLon = SAIHJucarClient.toLatLon(742223.0284451315, 4309753.964542853).get();
	assertEquals(38.903287, latLon.getKey(), 0.00001);
	assertEquals(-0.206736, latLon.getValue(), 0.00001);

	assertFalse(SAIHJucarClient.toLatLon(0, 0).isPresent());
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
