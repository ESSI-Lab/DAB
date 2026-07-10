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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LegendFactory {

    public static Legend getLegend(String var) {
	switch (var) {
	case "shiwe":
	    return buildShiwe();
	case "2t":
	    return buildTemperature();
	case "utci":
	    return buildUtci();
	case "2r":
	    return buildHumidity();
	case "wbgt":
	    return buildWbgt();
	case "wct":
	    return buildWct();
	case "at":
	    return buildAt();
	default:
	    return null;
	}
    }

    private static Legend buildShiwe() {
	return new Legend("SHIWE (Synthetic Healthiness Index of Workplace Exposure)",
		List.of(new LegendItem("0.0 – 0.5", "No risk", "#006837"), new LegendItem("0.5 – 1.0", "Very low", "#31a354"),
			new LegendItem("1.0 – 1.5", "Low", "#78c679"), new LegendItem("1.5 – 2.0", "Slight", "#c2e699"),
			new LegendItem("2.0 – 2.5", "Moderate", "#ffffb2"), new LegendItem("2.5 – 3.0", "High", "#fecc5c"),
			new LegendItem("3.0 – 3.5", "Very high", "#fd8d3c"), new LegendItem("3.5 – 4.0", "Severe", "#f03b20"),
			new LegendItem("4.0 – 4.5", "Extreme", "#bd0026"), new LegendItem("4.5 – 5.0", "Critical", "#800026")));
    }

    private static Legend buildTemperature() {
	return new Legend("Air Temperature at 2 m (°C)",
		List.of(new LegendItem("≤ -20.0", "Extreme cold", "#313695"), new LegendItem("-20 – -13", "Very cold", "#4575b4"),
			new LegendItem("-13.0 to -6.0", "Cold", "#74add1"), new LegendItem("-6 – 1", "Near freezing", "#abd9e9"),
			new LegendItem("1.0 to 8.0", "Cool", "#e0f3f8"), new LegendItem("8 – 15", "Mild", "#ffffbf"),
			new LegendItem("15.0 to 22.0", "Warm", "#fee090"), new LegendItem("22 – 29", "Hot", "#fdae61"),
			new LegendItem("29.0 to 36.0", "Very hot", "#f46d43"), new LegendItem("36 – 43", "Extreme heat", "#d73027"),
			new LegendItem("≥ 43.0", "Exceptional heat", "#a50026")));
    }

    private static Legend buildUtci() {
	return new Legend("Universal Thermal Climate Index (UTCI) (°C)", List.of(
		new LegendItem("≤ -40.0", "Extreme cold stress", "#000066"),
		new LegendItem("-40.0 to -27.0", "Very strong cold stress", "#0000ff"),
		new LegendItem("-27.0 to -13.0", "Strong cold stress", "#007fff"),
		new LegendItem("-13.0 to 0.0", "Moderate cold stress", "#add8e6"),
		new LegendItem("0.0 to 9.0", "Slight cold stress", "#e0f3f8"),
		new LegendItem("9.0 to 26.0", "No thermal stress", "#2ca25f"),
		new LegendItem("26.0 to 32.0", "Moderate heat stress", "#fee090"),
		new LegendItem("32.0 to 38.0", "Strong heat stress", "#fdae61"),
		new LegendItem("38.0 to 46.0", "Very strong heat stress", "#f46d43"),
		new LegendItem("≥ 46.0", "Extreme heat stress", "#d73027")));
    }

    private static Legend buildHumidity() {
	return new Legend("Relative Humidity (%)",
		List.of(new LegendItem("0 – 10", "Extremely dry", "#ffffcc"), new LegendItem("10 – 20", "Very dry", "#ffeda0"),
			new LegendItem("20 – 30", "Dry", "#fed976"), new LegendItem("30 – 40", "Slightly dry", "#feb24c"),
			new LegendItem("40 – 50", "Comfortable", "#fd8d3c"), new LegendItem("50 – 60", "Humid", "#f03b20"),
			new LegendItem("60 – 70", "Very humid", "#bd0026"), new LegendItem("70 – 80", "Oppressive", "#9ecae1"),
			new LegendItem("80 – 90", "Extremely humid", "#4292c6"), new LegendItem("90 – 100", "Near saturation", "#08519c")));
    }

    private static Legend buildWbgt() {
	return new Legend("WBGT (Wet Bulb Globe Temperature) (°C)", List.of(
		// --- HEAT RISK SPECTRUM ---
		new LegendItem("< 26.7", "Normal Conditions", "#e0f3f8"),
		new LegendItem("26.7 to 29.4", "Green Flag Condition", "#008000"),
		new LegendItem("29.4 to 31.1", "Yellow Flag Condition", "#ffff00"),
		new LegendItem("31.1 to 32.2", "Red Flag Condition", "#ff0000"),
		new LegendItem("≥ 32.2", "Black Flag Condition", "#000000")));
    }

    private static Legend buildWct() {
	return new Legend("Wind Chill Temperature (WCT) (°C)", List.of(
		// --- COLD SPECTRUM (Your exact detailed thresholds) ---
		new LegendItem("≥ -10.0", "Low Cold Stress", "#add8e6"), new LegendItem("-27.0 to -10.0", "Uncomfortable Cold", "#0000ff"),
		new LegendItem("-35.0 to -27.0", "Risk of frostbite during prolonged exposure", "#00008b"),
		new LegendItem("-40.0 to -35.0", "Frostbite possible in 10-15 min", "#7f00ff"),
		new LegendItem("-45.0 to -40.0", "Frostbite possible in < 10 min", "#ff00ff"),
		new LegendItem("< -45.0", "Frostbite possible within minutes", "#8b008b")));
    }

    private static Legend buildAt() {
	return new Legend("Apparent Temperature (AT) (°C)", List.of(
		// --- HEAT SPECTRUM ---
		new LegendItem("< 26.7", "Normal / No Stress", "#e0f3f8"), new LegendItem("26.7 to 32.2", "Caution", "#ffff00"),
		new LegendItem("32.2 to 39.4", "Extreme Caution", "#ffb300"), new LegendItem("39.4 to 51.1", "Danger", "#ff6600"),
		new LegendItem("≥ 51.7", "Extreme Danger", "#d73027")));

    }

    private static String escapeXml(String text) {
	return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void main(String[] args) throws IOException {
	String var = "wct";
	Legend legend = LegendFactory.getLegend(var);
	if (legend == null)
	    return;
	int width = 320;
	int itemHeight = 22;
	int spacing = 6;
	int margin = 10;
	int height = margin + 30 + legend.items.size() * (itemHeight + spacing);

	StringBuilder svg = new StringBuilder();
	svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(width).append("' height='").append(height).append("'>");
	svg.append("<rect width='100%' height='100%' fill='#f5f5f5'/>");
	svg.append("<text x='10' y='20' font-size='14' font-weight='bold'>").append(escapeXml(legend.title)).append("</text>");

	int y = 40;
	for (LegendItem item : legend.items) {
	    svg.append("<rect x='10' y='").append(y).append("' width='18' height='18' fill='").append(item.color)
		    .append("' stroke='black' stroke-width='0.5'/>");
	    svg.append("<text x='35' y='").append(y + 13).append("' font-size='12'>").append(escapeXml(item.range)).append(" (")
		    .append(escapeXml(item.label)).append(")</text>");
	    y += itemHeight + spacing;
	}
	svg.append("</svg>");
	Path workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "legends_");
	Path varDir = workingDir.resolve(var);

	// CRITICAL FIX: Create the full directory path (including the variable subfolder)
	Files.createDirectories(varDir);

	// Now this will write perfectly without throwing an exception
	Files.writeString(varDir.resolve("legend.svg"), svg.toString(), StandardCharsets.UTF_8);
    }

}
