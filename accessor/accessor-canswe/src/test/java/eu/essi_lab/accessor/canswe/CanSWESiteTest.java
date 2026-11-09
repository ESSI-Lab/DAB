package eu.essi_lab.accessor.canswe;

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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

/**
 * @author boldrini
 */
public class CanSWESiteTest {

    private static CanSWEStation station(String id, int[] times, float[] values) {

	CanSWEStation ret = new CanSWEStation();
	ret.setId(id);
	ret.setSeries(CanSWEDataset.SNW, times, values);
	return ret;
    }

    @Test
    public void singleValuesGiveOneSeries() {

	// co-located stations with complementary periods
	CanSWESite site = new CanSWESite(Arrays.asList(//
		station("B", new int[] { 5, 6 }, new float[] { 50, 60 }), //
		station("A", new int[] { 1, 2 }, new float[] { 10, 20 })));

	assertEquals("A,B", site.getId());

	TreeMap<Integer, double[]> aggregate = site.aggregate(CanSWEDataset.SNW);
	assertEquals(Arrays.asList(CanSWEStatistic.VALUE), CanSWESite.getStatistics(aggregate));

	List<Entry<Integer, Double>> series = CanSWESite.getSeries(aggregate, CanSWEStatistic.VALUE, null, null);
	assertEquals(4, series.size());
	assertEquals(1, (int) series.get(0).getKey());
	assertEquals(60.0, series.get(3).getValue(), 0);

	// no values of another variable
	assertTrue(CanSWESite.getStatistics(site.aggregate(CanSWEDataset.SND)).isEmpty());
    }

    @Test
    public void multipleValuesGiveMaximumAndMinimumSeries() {

	CanSWESite site = new CanSWESite(Arrays.asList(//
		station("A", new int[] { 1, 2, 3 }, new float[] { 10, 20, 30 }), //
		station("B", new int[] { 2, 3, 4 }, new float[] { 25, 5, 40 })));

	TreeMap<Integer, double[]> aggregate = site.aggregate(CanSWEDataset.SNW);
	assertEquals(Arrays.asList(CanSWEStatistic.MAXIMUM, CanSWEStatistic.MINIMUM), CanSWESite.getStatistics(aggregate));

	List<Entry<Integer, Double>> max = CanSWESite.getSeries(aggregate, CanSWEStatistic.MAXIMUM, null, null);
	List<Entry<Integer, Double>> min = CanSWESite.getSeries(aggregate, CanSWEStatistic.MINIMUM, null, null);

	assertArrayEquals(new double[] { 10, 25, 30, 40 }, max.stream().mapToDouble(Entry::getValue).toArray(), 0);
	assertArrayEquals(new double[] { 10, 20, 5, 40 }, min.stream().mapToDouble(Entry::getValue).toArray(), 0);

	// time subset
	List<Entry<Integer, Double>> subset = CanSWESite.getSeries(aggregate, CanSWEStatistic.MAXIMUM, 2, 3);
	assertEquals(2, subset.size());
	assertEquals(25.0, subset.get(0).getValue(), 0);
    }

    @Test
    public void onlineName() {

	String name = CanSWEMapper.getOnlineName(CanSWEDataset.SND, CanSWEStatistic.MINIMUM, "ALE-05AA801,ALE-05AA805");
	String[] parts = CanSWEMapper.parseOnlineName(name).get();
	assertEquals(CanSWEDataset.SND, parts[0]);
	assertEquals("MINIMUM", parts[1]);
	assertEquals("ALE-05AA801,ALE-05AA805", parts[2]);

	assertFalse(CanSWEMapper.parseOnlineName("snw").isPresent());
	assertFalse(CanSWEMapper.parseOnlineName("snw;AVERAGE;A").isPresent());
    }

    @Test
    public void helpers() {

	assertEquals("kg m-2", CanSWEMapper.normalizeUnits("kg m**-2"));
	assertFalse(CanSWEDataset.isValid(9.96921e+36f, 9.96921e+36f));
	assertFalse(CanSWEDataset.isValid(Float.NaN, Double.NaN));
	assertTrue(CanSWEDataset.isValid(0, 9.96921e+36f));
	assertTrue(CanSWEClient.isCanSWE("https://zenodo.org/records/10835278"));
	assertTrue(CanSWEClient.isCanSWE("https://zenodo.org/records/19075529/files/CanSWE-CanEEN_1928-2025_v8.nc?download=1"));
	assertFalse(CanSWEClient.isCanSWE("https://zenodo.org/records/123"));
    }
}
