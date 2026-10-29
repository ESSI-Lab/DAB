package eu.essi_lab.accessor.esaca;

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
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.esaca.ACAClient.Page;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class ACAClientTest {

    static final String DISCHARGE_SENSOR = "{\"sensor\":\"CALC001658\",\"description\":\"Cabal riu\",\"dataType\":\"NUMBER\","
	    + "\"location\":\"41.592783082 2.542640569\",\"type\":\"0014\",\"unit\":\"m³/s\",\"timeZone\":\"CET\",\"publicAccess\":true,"
	    + "\"state\":\"online\",\"component\":\"080060-001\",\"componentType\":\"aforament\","
	    + "\"componentDesc\":\"Arenys de Munt (riera d'Arenys)\",\"componentPublicAccess\":true,"
	    + "\"additionalInfo\":{\"Temps mostreig (min)\":\"15\",\"Rang mínim\":\"0\",\"Rang màxim\":\"90\"},"
	    + "\"componentAdditionalInfo\":{\"Codi ordre\":\"RM-05\",\"Comarca\":\"MARESME\",\"Província\":\"BARCELONA\","
	    + "\"Estat\":\"En servei\",\"Riu\":\"RIERA ARENYS\",\"Districte fluvial\":\"ACA\",\"Superfície conca drenada\":\"11,80 km²\","
	    + "\"Coordenada Y (UTM ETRS89)\":\"4604666\",\"Coordenada X (UTM ETRS89)\":\"461882\",\"Subconca\":\"RIERES DEL MARESME\","
	    + "\"Terme municipal\":\"ARENYS DE MUNT\",\"Conca\":\"RIERES DEL MARESME\",\"Topònim\":\"\"},"
	    + "\"createdAt\":1549702654605,\"updatedAt\":1549702660782,\"componentCreatedAt\":1549702620000,"
	    + "\"componentUpdatedAt\":1719482860034}";

    /**
     * A piezometer sensor without location and with a description ending with a new line
     */
    static final String TEMPERATURE_SENSOR = "{\"sensor\":\"171352-007-ANA02\",\"description\":\"Temperatura de l'aigua\\r\\n\","
	    + "\"dataType\":\"NUMBER\",\"location\":\"\",\"type\":\"0005\",\"unit\":\"ºC\",\"timeZone\":\"CET\",\"publicAccess\":true,"
	    + "\"state\":\"online\",\"component\":\"171352-007\",\"componentType\":\"piezometre\","
	    + "\"componentDesc\":\"Pont de Molins (Pm4pz)\",\"componentPublicAccess\":true,"
	    + "\"componentAdditionalInfo\":{\"Districte fluvial\":\"ACA\",\"Coordenada X (UTM ETRS89)\":\"494937\",\"Conca\":\"LA MUGA\","
	    + "\"Terme municipal\":\"PONT DE MOLINS\",\"Coordenada Y (UTM ETRS89)\":\"4684177\",\"Província\":\"Girona\","
	    + "\"Aqüífer\":\"Aqüífer superficial de la plana al·luvial del Fluvià i la Muga\",\"Comarca\":\"Alt Empordà\","
	    + "\"Estat\":\"En servei\",\"Subconca\\t\":\"LA MUGA\",\"Massa d'aigua\":\"Fluviodeltaic del Fluvià - Muga\"}}";

    static final String LATEST = "{\"sensors\":[{\"sensor\":\"080060-001-ANA01\",\"observations\":[{\"value\":\"0.401\","
	    + "\"timestamp\":\"27/10/2026T14:15:00\",\"time\":1793110500000}]},{\"sensor\":\"CALC001658\",\"observations\":[{\"value\":"
	    + "\"0.01\",\"timestamp\":\"27/10/2026T14:15:00\",\"time\":1793110500000}]},{\"sensor\":\"CALC000002\",\"observations\":[]}]}";

    static final String OBSERVATIONS = "{\"observations\":[{\"value\":\"0.02\",\"timestamp\":\"27/10/2026T14:15:00\","
	    + "\"time\":1793110500000},{\"value\":\"\",\"timestamp\":\"27/10/2026T14:10:00\",\"time\":1793110200000},"
	    + "{\"value\":\"0.01\",\"timestamp\":\"27/10/2026T14:05:00\"},{\"value\":\"0.01\",\"timestamp\":\"27/10/2026T14:00:00\","
	    + "\"time\":1793109600000}]}";

    static final Date NOW = date("2026-10-27T15:00:00Z");

    /**
     * @return
     */
    static ACAEntity createDischargeSeries() {

	return ACAClient.createSeries("AFORAMENT-EST", new JSONObject(DISCHARGE_SENSOR), date("2026-10-27T14:15:00Z"), NOW).get();
    }

    @Test
    public void createSeriesTest() {

	ACAEntity series = createDischargeSeries();

	assertEquals("AFORAMENT-EST|CALC001658", series.getSeriesId());
	assertEquals("AFORAMENT-EST", series.getProvider());
	assertEquals("CALC001658", series.getSensor());
	assertEquals("080060-001", series.getComponent());
	assertEquals(ACAProperty.RIVER_DISCHARGE, series.getProperty().get());
	assertEquals("RIERA ARENYS", series.getComponentInfo(ACAClient.RIVER));
	assertEquals("2026-10-27T14:15:00Z", series.getObject().getString(ACAClient.END));
	// begin of the rolling window
	assertEquals("2026-07-27T15:00:00Z", series.getObject().getString(ACAClient.BEGIN));
	assertEquals(new BigDecimal("41.592783082"), new BigDecimal(series.getObject().get(ACAClient.LAT).toString()));

	// no observations in the rolling window
	assertFalse(ACAClient.createSeries("AFORAMENT-EST", new JSONObject(DISCHARGE_SENSOR), null, NOW).isPresent());
	assertFalse(ACAClient.createSeries("AFORAMENT-EST", new JSONObject(DISCHARGE_SENSOR), date("2026-07-01T00:00:00Z"), NOW)
		.isPresent());

	// not public
	JSONObject restricted = new JSONObject(DISCHARGE_SENSOR);
	restricted.put(ACAClient.PUBLIC_ACCESS, false);
	assertFalse(ACAClient.createSeries("AFORAMENT-EST", restricted, date("2026-10-27T14:15:00Z"), NOW).isPresent());
    }

    @Test
    public void trimAndLocationTest() {

	ACAEntity series = ACAClient
		.createSeries("PIEZOMETRE-EST", new JSONObject(TEMPERATURE_SENSOR), date("2026-10-27T05:00:00Z"), NOW).get();

	assertEquals("Temperatura de l'aigua", series.getDescription());
	assertEquals(ACAProperty.WATER_TEMPERATURE, series.getProperty().get());
	assertEquals("LA MUGA", series.getComponentInfo(ACAClient.SUB_BASIN));

	// from the UTM coordinates
	double lat = Double.parseDouble(series.getObject().get(ACAClient.LAT).toString());
	double lon = Double.parseDouble(series.getObject().get(ACAClient.LON).toString());
	assertEquals(42.307, lat, 0.01);
	assertEquals(2.937, lon, 0.01);
    }

    @Test
    public void utmTest() {

	// the location and the UTM coordinates of station 080060-001
	double[] latLon = ACAClient.utmToLatLon(461882, 4604666, 31);
	assertEquals(41.592783082, latLon[0], 0.00002);
	assertEquals(2.542640569, latLon[1], 0.00002);
    }

    @Test
    public void parseLatestTest() {

	Map<String, Date> latest = ACAClient.parseLatestObservations(new JSONObject(LATEST));

	assertEquals(2, latest.size());
	assertEquals(date("2026-10-27T14:15:00Z"), latest.get("CALC001658"));
	assertNull(latest.get("CALC000002"));
    }

    @Test
    public void parseObservationsTest() {

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	Page page = ACAClient.parseObservations(new JSONObject(OBSERVATIONS), values);

	assertEquals(4, page.count);
	assertEquals(date("2026-10-27T14:00:00Z"), page.oldest);
	// missing values are discarded
	assertEquals(3, values.size());
	assertEquals(new BigDecimal("0.02"), values.lastEntry().getValue());
	// from the timestamp
	assertEquals(new BigDecimal("0.01"), values.get(date("2026-10-27T14:05:00Z")));
    }

    @Test
    public void seriesIdTest() {

	ACAEntity series = ACAClient.parseSeriesId("EMBASSAMENT-EST|080581-002-ANA006");
	assertEquals("EMBASSAMENT-EST", series.getProvider());
	assertEquals("080581-002-ANA006", series.getSensor());
	assertEquals("data/EMBASSAMENT-EST/080581-002-ANA006", ACAClient.getSensorPath(series));

	assertNull(ACAClient.parseSeriesId("EMBASSAMENT-EST"));
	assertNull(ACAClient.parseSeriesId("EMBASSAMENT-EST|"));
	assertNull(ACAClient.parseSeriesId(null));
    }

    @Test
    public void dateTest() {

	assertEquals("27/07/2026T00:05:00", ACAClient.formatDate(date("2026-07-27T00:05:00Z")));
	assertEquals(date("2026-07-27T00:05:00Z"), ACAClient.parseDate("27/07/2026T00:05:00").get());
	assertTrue(ACAClient.parseDate("2026-07-27").isEmpty());
    }

    /**
     * @param text
     * @return
     */
    static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
