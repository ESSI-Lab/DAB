package eu.essi_lab.accessor.nonvemagasin;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class NVEMagasinClientTest {

    /**
     * Response of HentOmråder (only some of the areas)
     */
    static final String AREAS = "[{\"land\":[{\"navn\":\"Norge\",\"navn_langt\":\"Norge\",\"beskrivelse\":\"Hele landet\",\"omrType\":\"NO\","
	    + "\"omrnr\":0}],\"elspot\":[{\"navn\":\"NO 1\",\"navn_langt\":\"Elspotområde 1\",\"beskrivelse\":\"Sørøst-Norge. Omfatter"
	    + " østlige del av Østlandet fra Buskerud og nordover (bortsett fra den del av Innlandet som ligger vest og nord for"
	    + " Vågåmo).\",\"omrType\":\"EL\",\"omrnr\":1}],\"vassdrag\":[{\"navn\":\"VASS4\",\"navn_langt\":\"Vassdragsområde 4\","
	    + "\"beskrivelse\":\"Nord-Norge. Resten av Nordland og nordover.\",\"omrType\":\"VASS\",\"omrnr\":4}]}]";

    /**
     * Rows of HentOffentligData (the fylling_TWh value of EL 1 of 1995-01-15 was removed to test missing values)
     */
    static final String DATA = "[" //
	    + "{\"dato_Id\":\"2026-10-18\",\"omrType\":\"EL\",\"omrnr\":1,\"iso_aar\":2026,\"iso_uke\":42,\"fyllingsgrad\":0.7474989,"
	    + "\"kapasitet_TWh\":6.0032635,\"fylling_TWh\":4.487433,\"neste_Publiseringsdato\":\"2026-10-28T13:00:00\","
	    + "\"fyllingsgrad_forrige_uke\":0.74816257,\"endring_fyllingsgrad\":-0.0006636381}," //
	    + "{\"dato_Id\":\"1995-01-08\",\"omrType\":\"EL\",\"omrnr\":1,\"iso_aar\":1995,\"iso_uke\":1,\"fyllingsgrad\":0.6175737,"
	    + "\"kapasitet_TWh\":6.0032635,\"fylling_TWh\":3.7074575,\"neste_Publiseringsdato\":\"0001-01-01T00:00:00\","
	    + "\"fyllingsgrad_forrige_uke\":0.5837047,\"endring_fyllingsgrad\":0.03386897}," //
	    + "{\"dato_Id\":\"1995-01-15\",\"omrType\":\"EL\",\"omrnr\":1,\"iso_aar\":1995,\"iso_uke\":2,\"fyllingsgrad\":0.5792045,"
	    + "\"kapasitet_TWh\":6.0032635,\"fylling_TWh\":null,\"neste_Publiseringsdato\":\"0001-01-01T00:00:00\","
	    + "\"fyllingsgrad_forrige_uke\":0.6175737,\"endring_fyllingsgrad\":-0.03836918}," //
	    + "{\"dato_Id\":\"1995-01-08\",\"omrType\":\"NO\",\"omrnr\":0,\"iso_aar\":1995,\"iso_uke\":1,\"fyllingsgrad\":0.60798806,"
	    + "\"kapasitet_TWh\":87.43758,\"fylling_TWh\":53.161003,\"neste_Publiseringsdato\":\"0001-01-01T00:00:00\","
	    + "\"fyllingsgrad_forrige_uke\":0.75263125,\"endring_fyllingsgrad\":-0.14464319}," //
	    + "{\"dato_Id\":\"2026-10-18\",\"omrType\":\"NO\",\"omrnr\":0,\"iso_aar\":2026,\"iso_uke\":42,\"fyllingsgrad\":0.6925853,"
	    + "\"kapasitet_TWh\":87.43758,\"fylling_TWh\":60.55798,\"neste_Publiseringsdato\":\"2026-10-28T13:00:00\","
	    + "\"fyllingsgrad_forrige_uke\":0.68957406,\"endring_fyllingsgrad\":0.0030112267}" //
	    + "]";

    /**
     * @param areaTypes
     * @return
     */
    static List<NVEMagasinEntity> createSeries(String... areaTypes) {

	return NVEMagasinClient.createSeries(NVEMagasinClient.parseAreas(new JSONArray(AREAS)), new JSONArray(DATA),
		Arrays.asList(NVEMagasinProperty.values()), Arrays.asList(areaTypes));
    }

    @Test
    public void parseAreasTest() {

	List<JSONObject> areas = NVEMagasinClient.parseAreas(new JSONArray(AREAS));

	assertEquals(3, areas.size());
	assertEquals("NO", areas.get(0).getString(NVEMagasinClient.AREA_TYPE));
	assertEquals("NO 1", areas.get(1).getString(NVEMagasinClient.AREA_NAME));
	assertEquals("VASS4", areas.get(2).getString(NVEMagasinClient.AREA_NAME));
    }

    @Test
    public void createSeriesTest() {

	List<NVEMagasinEntity> series = createSeries();

	// the watercourse region 4 has no values
	List<String> ids = series.stream().map(NVEMagasinEntity::getSeriesId).collect(Collectors.toList());
	assertEquals(Arrays.asList("NO|0|fyllingsgrad", "NO|0|fylling_TWh", "NO|0|kapasitet_TWh", "EL|1|fyllingsgrad",
		"EL|1|fylling_TWh", "EL|1|kapasitet_TWh"), ids);

	NVEMagasinEntity ratio = series.get(3);
	assertEquals("EL|1", ratio.getAreaCode());
	assertEquals(NVEMagasinArea.NO1, ratio.getArea().get());
	assertEquals(NVEMagasinProperty.FILLING_RATIO, ratio.getProperty().get());
	assertEquals("Elspotområde 1", ratio.getString(NVEMagasinClient.AREA_LONG_NAME));
	assertEquals("1995-01-08T00:00:00Z", ratio.getString(NVEMagasinClient.BEGIN));
	assertEquals("2026-10-18T00:00:00Z", ratio.getString(NVEMagasinClient.END));
	assertEquals(3, ratio.getObject().getInt(NVEMagasinClient.VALUES_COUNT));

	// the missing value is discarded
	assertEquals(2, series.get(4).getObject().getInt(NVEMagasinClient.VALUES_COUNT));

	assertEquals(3, createSeries("EL").size());
	assertTrue(createSeries("VASS").isEmpty());
    }

    @Test
    public void parseObservationsTest() {

	NVEMagasinEntity filling = NVEMagasinClient.parseSeriesId("EL|1|fylling_TWh");

	TreeMap<Date, BigDecimal> all = NVEMagasinClient.parseObservations(new JSONArray(DATA), filling, null, null);
	assertEquals(2, all.size());
	assertEquals(new BigDecimal("3.7074575"), all.firstEntry().getValue());
	assertEquals(date("2026-10-18T00:00:00Z"), all.lastKey());

	NVEMagasinEntity ratio = NVEMagasinClient.parseSeriesId("EL|1|fyllingsgrad");
	TreeMap<Date, BigDecimal> range = NVEMagasinClient.parseObservations(new JSONArray(DATA), ratio, date("1995-01-09T00:00:00Z"),
		date("2000-01-01T00:00:00Z"));
	assertEquals(1, range.size());
	assertEquals(date("1995-01-15T00:00:00Z"), range.firstKey());
	assertEquals(new BigDecimal("0.5792045"), range.firstEntry().getValue());

	assertTrue(NVEMagasinClient
		.parseObservations(new JSONArray(DATA), NVEMagasinClient.parseSeriesId("VASS|4|fyllingsgrad"), null, null).isEmpty());
	assertTrue(NVEMagasinClient.parseObservations(new JSONArray(Collections.emptyList()), ratio, null, null).isEmpty());
    }

    @Test
    public void seriesIdTest() {

	NVEMagasinEntity series = NVEMagasinClient.parseSeriesId("VASS|3|KAPASITET_TWH");
	assertEquals("VASS", series.getAreaType());
	assertEquals(3, series.getAreaNumber());
	assertEquals(NVEMagasinProperty.CAPACITY, series.getProperty().get());
	assertEquals("VASS|3|kapasitet_TWh", series.getSeriesId());
	assertEquals("Watercourse region 3 (Central Norway)", series.getAreaName());

	assertNull(NVEMagasinClient.parseSeriesId("EL|1"));
	assertNull(NVEMagasinClient.parseSeriesId("EL|x|fyllingsgrad"));
	assertNull(NVEMagasinClient.parseSeriesId("EL|1|endring_fyllingsgrad"));
	assertNull(NVEMagasinClient.parseSeriesId(null));
    }

    @Test
    public void parseDateTest() {

	assertEquals(date("2026-10-18T00:00:00Z"), NVEMagasinClient.parseDate("2026-10-18").get());
	assertFalse(NVEMagasinClient.parseDate(null).isPresent());
	assertEquals("https://biapi.nve.no/magasinstatistikk/",
		NVEMagasinClient.normalizeEndpoint("https://biapi.nve.no/magasinstatistikk"));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
