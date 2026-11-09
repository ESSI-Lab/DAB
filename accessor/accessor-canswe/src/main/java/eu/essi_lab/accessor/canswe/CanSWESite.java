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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A group of CanSWE stations at the same location. In the CanSWE dataset different station ids can share the same
 * coordinates (e.g. a snow course and its replacement, or a manual snow course and a snow pillow), so that at the same
 * location there can be two (or more) values at the same time.
 *
 * @author boldrini
 */
public class CanSWESite {

    /**
     * Separator of the station ids in the site identifier
     */
    public static final String ID_SEPARATOR = ",";

    private final List<CanSWEStation> stations = new ArrayList<>();

    /**
     * @param stations
     */
    public CanSWESite(List<CanSWEStation> stations) {

	this.stations.addAll(stations);
	this.stations.sort(Comparator.comparing(CanSWEStation::getId));
    }

    /**
     * @return the stations, sorted by id
     */
    public List<CanSWEStation> getStations() {

	return Collections.unmodifiableList(stations);
    }

    /**
     * @return the station ids, sorted and joined by {@link #ID_SEPARATOR}
     */
    public String getId() {

	return stations.stream().map(CanSWEStation::getId).collect(Collectors.joining(ID_SEPARATOR));
    }

    /**
     * @param variable
     * @return for each index of the time dimension having at least one valid value: minimum, maximum and number of the
     *         values of the site stations
     */
    public TreeMap<Integer, double[]> aggregate(String variable) {

	TreeMap<Integer, double[]> ret = new TreeMap<>();

	for (CanSWEStation station : stations) {

	    int[] times = station.getTimeIndexes(variable);
	    float[] values = station.getValues(variable);

	    for (int i = 0; i < times.length; i++) {

		double value = values[i];
		double[] aggregate = ret.get(times[i]);
		if (aggregate == null) {
		    ret.put(times[i], new double[] { value, value, 1 });
		} else {
		    aggregate[0] = Math.min(aggregate[0], value);
		    aggregate[1] = Math.max(aggregate[1], value);
		    aggregate[2]++;
		}
	    }
	}

	return ret;
    }

    /**
     * @param aggregate as returned by {@link #aggregate(String)}
     * @return the statistics to publish: none if there are no values, {@link CanSWEStatistic#VALUE} if there is at most
     *         one value at each time, otherwise {@link CanSWEStatistic#MAXIMUM} and {@link CanSWEStatistic#MINIMUM}
     */
    public static List<CanSWEStatistic> getStatistics(TreeMap<Integer, double[]> aggregate) {

	List<CanSWEStatistic> ret = new ArrayList<>();

	if (aggregate.isEmpty()) {
	    return ret;
	}

	boolean multiple = aggregate.values().stream().anyMatch(v -> v[2] > 1);
	if (multiple) {
	    ret.add(CanSWEStatistic.MAXIMUM);
	    ret.add(CanSWEStatistic.MINIMUM);
	} else {
	    ret.add(CanSWEStatistic.VALUE);
	}

	return ret;
    }

    /**
     * @param aggregate as returned by {@link #aggregate(String)}
     * @param statistic
     * @param beginIndex inclusive index of the time dimension, null for no lower limit
     * @param endIndex inclusive index of the time dimension, null for no upper limit
     * @return the series of the given statistic: time index and value. At the times when there is a single value, it is
     *         both the maximum and the minimum.
     */
    public static List<Entry<Integer, Double>> getSeries(TreeMap<Integer, double[]> aggregate, CanSWEStatistic statistic, Integer beginIndex,
	    Integer endIndex) {

	List<Entry<Integer, Double>> ret = new ArrayList<>();

	for (Entry<Integer, double[]> entry : aggregate.entrySet()) {

	    if ((beginIndex != null && entry.getKey() < beginIndex) || (endIndex != null && entry.getKey() > endIndex)) {
		continue;
	    }

	    ret.add(new java.util.AbstractMap.SimpleEntry<>(entry.getKey(), statistic.select(entry.getValue())));
	}

	return ret;
    }
}
