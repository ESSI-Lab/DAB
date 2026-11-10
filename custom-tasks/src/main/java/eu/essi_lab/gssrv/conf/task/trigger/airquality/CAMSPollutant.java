package eu.essi_lab.gssrv.conf.task.trigger.airquality;

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

import eu.essi_lab.gssrv.conf.task.trigger.Legend;
import eu.essi_lab.gssrv.conf.task.trigger.LegendItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Main pollutants of the CAMS European air quality forecasts (dataset cams-europe-air-quality-forecasts), all expressed as surface
 * concentrations in µg/m³.<br>
 * Legend classes follow the European Air Quality Index (EAQI) of the European Environment Agency, revised in 2024 to align with the
 * 2021 WHO air quality guidelines.
 */
public enum CAMSPollutant {

    PM25("pm25", "particulate_matter_2.5um", "pm2p5", "PM2.5 (Particulate Matter < 2.5 µm)", //
	    5, 15, 50, 90, 140),

    PM10("pm10", "particulate_matter_10um", "pm10", "PM10 (Particulate Matter < 10 µm)", //
	    15, 45, 120, 195, 270),

    NO2("no2", "nitrogen_dioxide", "no2", "NO₂ (Nitrogen Dioxide)", //
	    10, 25, 60, 100, 150),

    O3("o3", "ozone", "o3", "O₃ (Ozone)", //
	    60, 100, 120, 160, 180),

    SO2("so2", "sulphur_dioxide", "so2", "SO₂ (Sulphur Dioxide)", //
	    20, 40, 125, 190, 275);

    private static final String[] EAQI_LABELS = { "Good", "Fair", "Moderate", "Poor", "Very poor", "Extremely poor" };
    private static final String[] EAQI_COLORS = { "#50f0e6", "#50ccaa", "#f0e641", "#ff5050", "#960032", "#7d2181" };

    private final String key;
    private final String adsVariable;
    private final String netcdfPrefix;
    private final String title;
    private final double[] thresholds;

    CAMSPollutant(String key, String adsVariable, String netcdfPrefix, String title, double... thresholds) {
	this.key = key;
	this.adsVariable = adsVariable;
	this.netcdfPrefix = netcdfPrefix;
	this.title = title;
	this.thresholds = thresholds;
    }

    /**
     * Short name used for the S3 folder, file names and in the index.json
     */
    public String getKey() {
	return key;
    }

    /**
     * Variable name expected by the ADS request
     */
    public String getAdsVariable() {
	return adsVariable;
    }

    /**
     * Prefix of the variable name in the NetCDF delivered by ADS (e.g. pm2p5_conc)
     */
    public String getNetcdfPrefix() {
	return netcdfPrefix;
    }

    public String getTitle() {
	return title;
    }

    /**
     * Upper bounds (µg/m³) of the EAQI classes, from "Good" to "Very poor"
     */
    public double[] getThresholds() {
	return thresholds.clone();
    }

    public Legend getLegend() {

	List<LegendItem> items = new ArrayList<>();
	for (int i = 0; i <= thresholds.length; i++) {
	    String range;
	    if (i == 0) {
		range = "< " + format(thresholds[0]);
	    } else if (i == thresholds.length) {
		range = "≥ " + format(thresholds[i - 1]);
	    } else {
		range = format(thresholds[i - 1]) + " – " + format(thresholds[i]);
	    }
	    items.add(new LegendItem(range + " µg/m³", EAQI_LABELS[i], EAQI_COLORS[i]));
	}

	return new Legend(title + " (µg/m³)", items);
    }

    public static Optional<CAMSPollutant> fromKey(String key) {
	return Arrays.stream(values()).filter(p -> p.key.equalsIgnoreCase(key.trim())).findFirst();
    }

    private static String format(double value) {
	return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
