package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;

/**
 * @author boldrini
 */
public class LombardiaOpenDataTest {

    @Test
    public void hydrometricZeroTest() {

	Optional<BigDecimal> zero = LombardiaWFSClient.parseHydrometricZero(
		"QUOTA ORTOMETRICA (GEOIDE ITALGEO2005)\nhort= 39,395 m s.l.m.\nQUOTA ORTOMETRICA - ZERO IDROMETRICO\n32,089 m s.l.m.\n");
	Assert.assertEquals(new BigDecimal("32.089"), zero.get());

	Assert.assertEquals(new BigDecimal("1125.345"),
		LombardiaWFSClient.parseHydrometricZero("ZERO IDROMETRICO 1.125,345 m s.l.m.").get());

	// gauge whose zero is not measurable
	Assert.assertFalse(LombardiaWFSClient.parseHydrometricZero("QUOTA ORTOMETRICA - ZERO IDROMETRICO … m s.l.m. Non rilevabile").isPresent());
    }

    @Test
    public void ratingCurvesTest() {

	List<JSONObject> features = new ArrayList<>();
	features.add(piece("2019/11/26 - 2020/05/12", "-", "transizione", "-"));
	features.add(piece("2017/06/13 - 2019/11/25", "2017.1", "Q=36.092*(h-0.089)^1.45", "0.2  < = h(m) < =  2.34"));
	features.add(piece("2021/03/17 - ad oggi", "2021.1", "Q=10*(h-0.1)^2", "0.1  < = h(m) < =  1"));
	features.add(piece("2021/03/17 - ad oggi", "2021.1", "Q=0.0691*(h*100)^1.5006", "1  < = h(m) < =  3"));

	RatingCurves curves = LombardiaWFSClient.toRatingCurves(features);

	Assert.assertEquals("11165", curves.getStationIdentifier());
	// the transition period is skipped, the two pieces of 2021 make a single curve
	Assert.assertEquals(2, curves.getCurves().size());

	List<RatingCurve> list = new ArrayList<>(curves.getCurves());
	RatingCurve old = list.get(0);
	Assert.assertEquals(LocalDate.of(2017, 6, 13), old.getBeginDate());
	Assert.assertEquals(LocalDate.of(2019, 11, 25), old.getEndDate());
	Assert.assertEquals(36.092 * Math.pow(1 - 0.089, 1.45), old.getFormula().evaluate(1), 1e-9);

	RatingCurve current = list.get(1);
	Assert.assertEquals(LocalDate.of(2021, 3, 17), current.getBeginDate());
	Assert.assertNull(current.getEndDate());
	Assert.assertEquals(2, current.getFormula().getRanges().size());
	Assert.assertEquals(0.1, current.getMinLevel(), 0);
	Assert.assertEquals(3, current.getMaxLevel(), 0);
	Assert.assertEquals(10 * Math.pow(0.5 - 0.1, 2), current.getFormula().evaluate(0.5), 1e-9);
	Assert.assertEquals(0.0691 * Math.pow(200, 1.5006), current.getFormula().evaluate(2), 1e-9);
	Assert.assertFalse(current.getPoints().isEmpty());

	Assert.assertTrue(curves.toWaterML2().contains("indeterminatePosition=\"now\""));
    }

    @Test
    public void resolutionTest() {

	Assert.assertEquals("PT10M", HISCentralLombardiaOpenDataMapper.getResolution(288, 2));
	Assert.assertEquals("PT10M", HISCentralLombardiaOpenDataMapper.getResolution(278, 2));
	Assert.assertEquals("PT5M", HISCentralLombardiaOpenDataMapper.getResolution(576, 2));
	Assert.assertEquals("PT1H", HISCentralLombardiaOpenDataMapper.getResolution(48, 2));
	Assert.assertEquals("P1D", HISCentralLombardiaOpenDataMapper.getResolution(2, 2));
	Assert.assertNull(HISCentralLombardiaOpenDataMapper.getResolution(0, 2));
    }

    @Test
    public void timeTest() {

	// data are in CET all year round
	Assert.assertEquals("2012-04-01T23:00:00Z", HISCentralLombardiaOpenDataMapper.toUTC("2012-04-02T00:00:00.000"));
	Assert.assertEquals("2026-07-01T11:00:00Z", HISCentralLombardiaOpenDataMapper.toUTC("2026-07-01T12:00:00.000"));
    }

    @Test
    public void propertiesTest() {

	Assert.assertEquals(2, LombardiaOpenDataProperty.fromTipologia("Velocità Vento").size());
	Assert.assertEquals(1, LombardiaOpenDataProperty.fromTipologia("Livello Idrometrico").size());
	Assert.assertEquals(11, LombardiaOpenDataProperty.values().length);

	// archives: until 2010, 2011-2020, from 2021
	List<LombardiaOpenDataProperty.Archive> archives = LombardiaOpenDataProperty.LIVELLO_IDROMETRICO.getArchives();
	Assert.assertTrue(archives.get(0).overlaps(1998, 2010));
	Assert.assertFalse(archives.get(0).overlaps(2011, 2026));
	Assert.assertTrue(archives.get(1).overlaps(2010, 2011));
	Assert.assertFalse(archives.get(1).overlaps(2021, 2026));
	Assert.assertTrue(archives.get(2).overlaps(2020, 2021));
	Assert.assertFalse(archives.get(2).overlaps(2019, 2020));
    }

    private static JSONObject piece(String period, String code, String equation, String range) {
	JSONObject ret = new JSONObject();
	ret.put("IdSensore", 11165);
	ret.put("Nome", "Asola // v. Carducci");
	ret.put("Fiume", "Chiese");
	ret.put("periodo", period);
	ret.put("codice", code);
	ret.put("EQUATION", equation);
	ret.put("range", range);
	return ret;
    }
}
