package eu.essi_lab.accessor.mxconagua;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.mxconagua.CONAGUAClient.StationData;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class CONAGUAClientTest {

    /**
     * Beginning of the catalogue (0_Catalogo de estaciones hidrometricas.csv), with an additional station without coordinates
     */
    static final String CATALOGUE = "Clave,Nombre de la estación,Latitud,Longitud,Altitud,Estado,Municipio,R.H.,Cuenca\r\n" //
	    + "CNDCA,\"Candelaria, Camp.\",18.183056,-91.046111,50,Campeche,Candelaria,30,Grijalva - Usumacinta\r\n" //
	    + "CANCA,\"Canasayab, Camp.\",19.295,-90.575556,17,Campeche,Champotón,31,Grijalva - Usumacinta\r\n" //
	    + "XXXXX,Without coordinates,,,,Campeche,Candelaria,30,Grijalva - Usumacinta\r\n" //
	    + "B600NAR,LOS NARANJOS,24.625,-103.305556,1650,Durango,General Simón Bolívar,,\r\n";

    /**
     * Header and some rows of the file of station B01002 (the values of 1960 are followed by empty rows until 2021/06/07)
     */
    static final String STATION_FILE = "Comisión Nacional del Agua\r\n" //
	    + "Subdirección General Técnica\r\n" //
	    + "Estación: \r\n" //
	    + "Clave: B01002\r\n" //
	    + "Estado: Baja California\r\n" //
	    + "Municipio: Tecate\r\n" //
	    + "\r\n" //
	    + "Fecha, Nivel(m), Gasto(m³/s)\r\n" //
	    + "1960/01/01,,0.00\r\n" //
	    + "1960/01/11,,0.00\r\n" //
	    + "1960/01/12,,0.07\r\n" //
	    + "1960/12/31,,0.00\r\n" //
	    + "1961/01/01,,\r\n" //
	    + "2021/06/07,,\r\n";

    /**
     * A file in the same format with both the properties
     */
    static final String LEVEL_FILE = "Comisión Nacional del Agua\r\n" //
	    + "Subdirección General Técnica\r\n" //
	    + "Estación: Candelaria, Camp.\r\n" //
	    + "Clave: CNDCA\r\n" //
	    + "\r\n" //
	    + "Fecha, Nivel(m), Gasto(m³/s)\r\n" //
	    + "1990/01/01,,\r\n" //
	    + "1990/01/02,2.35,\r\n" //
	    + "1990/01/03,2.40,120.5\r\n" //
	    + "1990/01/04,-,121.0\r\n" //
	    + "1990/01/05,,\r\n";

    /**
     * @return the station CNDCA
     */
    static JSONObject createStation() {

	return CONAGUAClient.parseCatalogue(CATALOGUE).get(0);
    }

    @Test
    public void catalogueTest() {

	List<JSONObject> stations = CONAGUAClient.parseCatalogue(CATALOGUE);

	// the station without coordinates is discarded
	assertEquals(3, stations.size());

	JSONObject station = stations.get(0);
	assertEquals("CNDCA", station.getString(CONAGUAClient.STATION_CODE));
	assertEquals("Candelaria, Camp.", station.getString(CONAGUAClient.STATION_NAME));
	assertEquals("18.183056", station.getString(CONAGUAClient.LAT));
	assertEquals("-91.046111", station.getString(CONAGUAClient.LON));
	assertEquals("50", station.getString(CONAGUAClient.ALTITUDE));
	assertEquals("Campeche", station.getString(CONAGUAClient.STATE));
	assertEquals("30", station.getString(CONAGUAClient.HYDROLOGICAL_REGION));
	assertEquals("Grijalva - Usumacinta", station.getString(CONAGUAClient.BASIN));

	assertEquals("Champotón", stations.get(1).getString(CONAGUAClient.MUNICIPALITY));
	assertFalse(stations.get(2).has(CONAGUAClient.BASIN));
    }

    @Test
    public void decodeTest() {

	// the catalogue is ISO-8859-1 encoded, the station files UTF-8
	assertEquals(CATALOGUE, CONAGUAClient.decode(CATALOGUE.getBytes(StandardCharsets.ISO_8859_1)));
	assertEquals(STATION_FILE, CONAGUAClient.decode(STATION_FILE.getBytes(StandardCharsets.UTF_8)));
	assertEquals(STATION_FILE, CONAGUAClient.decode(("﻿" + STATION_FILE).getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void splitTest() {

	assertEquals(Arrays.asList("a", "b, c", "", "d\"e"), CONAGUAClient.splitCSVLine("a,\"b, c\",,\"d\"\"e\""));
    }

    @Test
    public void stationFileTest() {

	StationData data = CONAGUAClient.parseStationFile(STATION_FILE);

	assertTrue(data.getValues(CONAGUAProperty.LEVEL).isEmpty());
	assertEquals(4, data.getValues(CONAGUAProperty.DISCHARGE).size());
	assertEquals("m³/s", data.units.get(CONAGUAProperty.DISCHARGE));
	assertEquals("m", data.units.get(CONAGUAProperty.LEVEL));

	assertEquals(new BigDecimal("0.07"),
		data.getValues(CONAGUAProperty.DISCHARGE).get(ISO8601DateTimeUtils.parseISO8601ToDate("1960-01-12T00:00:00Z").get()));

	// no series without values
	assertFalse(CONAGUAClient.createSeries(createStation(), CONAGUAProperty.LEVEL, data).isPresent());

	// the end is the last valid value, not the last row
	CONAGUAEntity discharge = CONAGUAClient.createSeries(createStation(), CONAGUAProperty.DISCHARGE, data).get();
	assertEquals("1960-01-01T00:00:00Z", discharge.getString(CONAGUAClient.BEGIN));
	assertEquals("1960-12-31T00:00:00Z", discharge.getString(CONAGUAClient.END));
    }

    @Test
    public void seriesTest() {

	StationData data = CONAGUAClient.parseStationFile(LEVEL_FILE);

	CONAGUAEntity level = CONAGUAClient.createSeries(createStation(), CONAGUAProperty.LEVEL, data).get();
	assertEquals("CNDCA|LEVEL", level.getSeriesId());
	assertEquals("CNDCA", level.getStationCode());
	assertEquals("Candelaria, Camp.", level.getStationName());
	assertEquals(CONAGUAProperty.LEVEL, level.getProperty().get());
	assertEquals("m", level.getUnit());
	assertEquals("1990-01-02T00:00:00Z", level.getString(CONAGUAClient.BEGIN));
	// the not numeric value is skipped
	assertEquals("1990-01-03T00:00:00Z", level.getString(CONAGUAClient.END));

	CONAGUAEntity discharge = CONAGUAClient.createSeries(createStation(), CONAGUAProperty.DISCHARGE, data).get();
	assertEquals("1990-01-03T00:00:00Z", discharge.getString(CONAGUAClient.BEGIN));
	assertEquals("1990-01-04T00:00:00Z", discharge.getString(CONAGUAClient.END));
	assertEquals(new BigDecimal("121.0"), data.getValues(CONAGUAProperty.DISCHARGE).lastEntry().getValue());
    }

    @Test
    public void seriesIdTest() {

	CONAGUAEntity series = CONAGUAClient.parseSeriesId("B600NAR|DISCHARGE");
	assertEquals("B600NAR", series.getStationCode());
	assertEquals(CONAGUAProperty.DISCHARGE, series.getProperty().get());
	assertEquals("B600NAR|DISCHARGE", series.getSeriesId());

	assertNull(CONAGUAClient.parseSeriesId("B600NAR"));
	assertNull(CONAGUAClient.parseSeriesId("B600NAR|TEMPERATURE"));
    }

    @Test
    public void dateTest() {

	assertEquals("2021-06-07T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(CONAGUAClient.parseDate("2021/06/07").get()));
	assertFalse(CONAGUAClient.parseDate("Fecha").isPresent());
	assertEquals(CONAGUAClient.DEFAULT_ENDPOINT, CONAGUAClient.normalizeEndpoint("https://sih.conagua.gob.mx/basedatos/Hidros"));
    }
}
