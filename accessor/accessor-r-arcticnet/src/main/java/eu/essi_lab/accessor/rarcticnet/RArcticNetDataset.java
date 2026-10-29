package eu.essi_lab.accessor.rarcticnet;

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

import java.util.Optional;

/**
 * Discharge datasets of the R-ArcticNet v4.0 archive, each one made of a site attributes table and of a discharge table (tab
 * separated text files)
 *
 * @author boldrini
 */
public enum RArcticNetDataset {

    /**
     * Mean monthly discharge of all the R-ArcticNet gauges: one row per gauge (PointID) and year, with the 12 monthly values
     */
    MONTHLY("data/SiteAttributes.txt", "data/Discharge_ms.txt", "P1M", "monthly"), //
    /**
     * Mean daily discharge of the Russian gauges of the NSF-funded UCLA/UNH project: one row per gauge (Code), year and day of
     * the month, with the 12 values of that day in each month
     */
    DAILY("russia-arcticnet/Daily_SiteAttributes.txt", "russia-arcticnet/discharge_m3_s_UNH-UCLA.txt", "P1D", "daily");

    private final String siteAttributesPath;
    private final String dischargePath;
    private final String duration;
    private final String label;

    /**
     * @param siteAttributesPath
     * @param dischargePath
     * @param duration
     * @param label
     */
    private RArcticNetDataset(String siteAttributesPath, String dischargePath, String duration, String label) {

	this.siteAttributesPath = siteAttributesPath;
	this.dischargePath = dischargePath;
	this.duration = duration;
	this.label = label;
    }

    /**
     * @return the path of the site attributes table, relative to the endpoint
     */
    public String getSiteAttributesPath() {

	return siteAttributesPath;
    }

    /**
     * @return the path of the discharge table, relative to the endpoint
     */
    public String getDischargePath() {

	return dischargePath;
    }

    /**
     * @return the ISO 8601 duration of the time step (and of the aggregation period) of the values, e.g. "P1M"
     */
    public String getDuration() {

	return duration;
    }

    /**
     * @return e.g. "monthly"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param name e.g. "MONTHLY"
     * @return
     */
    public static Optional<RArcticNetDataset> decode(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	for (RArcticNetDataset dataset : values()) {
	    if (dataset.name().equalsIgnoreCase(name.trim())) {
		return Optional.of(dataset);
	    }
	}

	return Optional.empty();
    }
}
