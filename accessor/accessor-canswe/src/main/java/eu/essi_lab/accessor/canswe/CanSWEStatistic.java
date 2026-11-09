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

/**
 * How the values of a CanSWE site time series are obtained from the values of its stations. A site is a group of
 * stations at the same location: when, for a variable, more than one station of the site has a value at the same
 * time, two time series are published, one with the maximum and one with the minimum of the values; otherwise a single
 * time series with the station values is published.
 *
 * @author boldrini
 */
public enum CanSWEStatistic {

    /**
     * At each time there is at most one value
     */
    VALUE("", ""),

    /**
     * At each time, the maximum of the values of the site stations
     */
    MAXIMUM("maximum", "Maximum"),

    /**
     * At each time, the minimum of the values of the site stations
     */
    MINIMUM("minimum", "Minimum");

    private final String label;
    private final String title;

    private CanSWEStatistic(String label, String title) {

	this.label = label;
	this.title = title;
    }

    /**
     * @return a lower case label, empty for {@link #VALUE}
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return a capitalized label, empty for {@link #VALUE}
     */
    public String getTitle() {

	return title;
    }

    /**
     * @param value aggregated value of a time step: [minimum, maximum]
     * @return
     */
    double select(double[] value) {

	return this == MINIMUM ? value[0] : value[1];
    }
}
