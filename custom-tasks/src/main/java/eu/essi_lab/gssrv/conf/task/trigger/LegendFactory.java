package eu.essi_lab.gssrv.conf.task.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;

public class LegendFactory {

    public static Legend getLegend(String var) {
	switch (var) {
	case "shiwe": return buildShiwe();
	case "2t": return buildTemperature();
	case "utci": return buildUtci();
	case "2r": return buildHumidity();
	default: return null;
	}
    }

    private static Legend buildShiwe() {
	return new Legend(
		"SHIWE (Synthetic Healthiness Index of Workplace Exposure)",
		List.of(
			new LegendItem("0.0 – 0.5", "No risk", "#006837"),
			new LegendItem("0.5 – 1.0", "Very low", "#31a354"),
			new LegendItem("1.0 – 1.5", "Low", "#78c679"),
			new LegendItem("1.5 – 2.0", "Slight", "#c2e699"),
			new LegendItem("2.0 – 2.5", "Moderate", "#ffffb2"),
			new LegendItem("2.5 – 3.0", "High", "#fecc5c"),
			new LegendItem("3.0 – 3.5", "Very high", "#fd8d3c"),
			new LegendItem("3.5 – 4.0", "Severe", "#f03b20"),
			new LegendItem("4.0 – 4.5", "Extreme", "#bd0026"),
			new LegendItem("4.5 – 5.0", "Critical", "#800026")
		)
	);
    }

    private static Legend buildTemperature() {
	return new Legend(
		"Air Temperature at 2 m (°C)",
		List.of(
			new LegendItem("≤ -20", "Extreme cold", "#313695"),
			new LegendItem("-20 – -13", "Very cold", "#4575b4"),
			new LegendItem("-13 – -6", "Cold", "#74add1"),
			new LegendItem("-6 – 1", "Near freezing", "#abd9e9"),
			new LegendItem("1 – 8", "Cool", "#e0f3f8"),
			new LegendItem("8 – 15", "Mild", "#ffffbf"),
			new LegendItem("15 – 22", "Warm", "#fee090"),
			new LegendItem("22 – 29", "Hot", "#fdae61"),
			new LegendItem("29 – 36", "Very hot", "#f46d43"),
			new LegendItem("36 – 43", "Extreme heat", "#d73027"),
			new LegendItem("≥ 43", "Exceptional heat", "#a50026")
		)
	);
    }

    private static Legend buildUtci() {
	return new Legend(
		"UTCI (°C)",
		List.of(
			new LegendItem("≤ -50", "Extreme cold stress", "#08306b"),
			new LegendItem("-50 – -40", "Very strong cold stress", "#2171b5"),
			new LegendItem("-40 – -30", "Strong cold stress", "#6baed6"),
			new LegendItem("-30 – -20", "Moderate cold stress", "#bdd7e7"),
			new LegendItem("-20 – -10", "Slight cold stress", "#eff3ff"),
			new LegendItem("-10 – 0", "No thermal stress", "#ffffbf"),
			new LegendItem("0 – 10", "Moderate heat stress", "#fee090"),
			new LegendItem("10 – 20", "Strong heat stress", "#fdae61"),
			new LegendItem("20 – 30", "Very strong heat stress", "#f46d43"),
			new LegendItem("30 – 40", "Extreme heat stress", "#d73027"),
			new LegendItem("> 40", "Exceptional heat stress", "#a50026")
		)
	);
    }

    private static Legend buildHumidity() {
	return new Legend(
		"Relative Humidity (%)",
		List.of(
			new LegendItem("0 – 10", "Extremely dry", "#ffffcc"),
			new LegendItem("10 – 20", "Very dry", "#ffeda0"),
			new LegendItem("20 – 30", "Dry", "#fed976"),
			new LegendItem("30 – 40", "Slightly dry", "#feb24c"),
			new LegendItem("40 – 50", "Comfortable", "#fd8d3c"),
			new LegendItem("50 – 60", "Humid", "#f03b20"),
			new LegendItem("60 – 70", "Very humid", "#bd0026"),
			new LegendItem("70 – 80", "Oppressive", "#9ecae1"),
			new LegendItem("80 – 90", "Extremely humid", "#4292c6"),
			new LegendItem("90 – 100", "Near saturation", "#08519c")
		)
	);
    }
}
