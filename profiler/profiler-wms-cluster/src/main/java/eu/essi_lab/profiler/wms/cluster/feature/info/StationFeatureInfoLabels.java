package eu.essi_lab.profiler.wms.cluster.feature.info;

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

/**
 * Localized labels for the station GetFeatureInfo HTML popup.
 */
public final class StationFeatureInfoLabels {

    private static final StationFeatureInfoLabels ENGLISH = new StationFeatureInfoLabels(//
	    "DAB GetFeatureInfo output", //
	    "Station", //
	    "Source", //
	    "Station info", //
	    "Close", //
	    "of");

    private static final StationFeatureInfoLabels ITALIAN = new StationFeatureInfoLabels(//
	    "Output GetFeatureInfo DAB", //
	    "Stazione", //
	    "Sorgente", //
	    "Info stazione", //
	    "Chiudi", //
	    "di");

    private final String pageTitle;
    private final String station;
    private final String source;
    private final String stationInfo;
    private final String close;
    private final String countOfWord;

    private StationFeatureInfoLabels(String pageTitle, String station, String source, String stationInfo, String close,
	    String countOfWord) {

	this.pageTitle = pageTitle;
	this.station = station;
	this.source = source;
	this.stationInfo = stationInfo;
	this.close = close;
	this.countOfWord = countOfWord;
    }

    public static StationFeatureInfoLabels of(String lang) {

	if (lang != null) {
	    String normalized = lang.trim().toLowerCase();
	    if (normalized.startsWith("it")) {
		return ITALIAN;
	    }
	}

	return ENGLISH;
    }

    public String getPageTitle() {

	return pageTitle;
    }

    public String getStation() {

	return station;
    }

    public String getSource() {

	return source;
    }

    public String getStationInfo() {

	return stationInfo;
    }

    public String getClose() {

	return close;
    }

    public String countPartial(int returned, int total) {

	return " (" + returned + " " + countOfWord + " " + total + ")";
    }
}
