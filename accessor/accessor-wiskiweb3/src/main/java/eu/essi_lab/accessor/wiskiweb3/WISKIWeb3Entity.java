package eu.essi_lab.accessor.wiskiweb3;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * A WISKIweb3 time series: a station parameter combined with one of its files (week or year) and one of the series of the
 * file, described by the entry of the station in the layer of the parameter, completed with the station description and the
 * metadata of the series in the file
 *
 * @author boldrini
 */
public class WISKIWeb3Entity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public WISKIWeb3Entity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public WISKIWeb3Entity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "0/23940359/W|week|15m.Cmd.RelAbs.P"
     */
    public String getSeriesId() {

	return getObject().optString(WISKIWeb3Client.SERIES_ID, null);
    }

    /**
     * @return the directory of the station parameter, e.g. "0/23940359/W"
     */
    public String getStationPath() {

	return getObject().optString(WISKIWeb3Client.STATION_PATH, null);
    }

    /**
     * @return {@link WISKIWeb3Client#WEEK_FILE} or {@link WISKIWeb3Client#YEAR_FILE}
     */
    public String getFile() {

	return getObject().optString(WISKIWeb3Client.FILE, WISKIWeb3Client.WEEK_FILE);
    }

    /**
     * @return true if the series is the high resolution one of the week file
     */
    public boolean isRecent() {

	return getFile().equals(WISKIWeb3Client.WEEK_FILE);
    }

    /**
     * @return the ts_shortname of the series, e.g. "15m.Cmd.RelAbs.P" or "Day.Mean"
     */
    public String getTsShortName() {

	return getObject().optString(WISKIWeb3Client.TS_SHORT_NAME, "");
    }

    /**
     * @return the station number, e.g. "23940359"
     */
    public String getStationNumber() {

	return getObject().optString(WISKIWeb3Client.STATION_NO, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(WISKIWeb3Client.STATION_NAME);
	return name.isEmpty() ? getStationNumber() : name;
    }

    /**
     * @return the directory name of the parameter, e.g. "W"
     */
    public String getParameterCode() {

	String path = getStationPath();
	return path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * @return the unit symbol, e.g. "cm", or an empty string if not set
     */
    public String getUnit() {

	return getString(WISKIWeb3Client.UNIT);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
