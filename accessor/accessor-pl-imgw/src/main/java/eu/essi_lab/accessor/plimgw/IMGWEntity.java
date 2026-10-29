package eu.essi_lab.accessor.plimgw;

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

import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * An IMGW time series: a station combined with one of its observed properties, described by the entry of the station in the real
 * time API and by the period of observations found in the archive
 *
 * @author boldrini
 */
public class IMGWEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public IMGWEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public IMGWEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "153190040|H"
     */
    public String getSeriesId() {

	return getObject().optString(IMGWClient.SERIES_ID, null);
    }

    /**
     * @return the station code, e.g. "153190040"
     */
    public String getStationCode() {

	return getObject().optString(IMGWClient.STATION_ID, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(IMGWClient.STATION_NAME);
	return name.isEmpty() ? getStationCode() : name;
    }

    /**
     * @return
     */
    public Optional<IMGWProperty> getProperty() {

	return IMGWProperty.decode(getObject().optString(IMGWClient.PROPERTY, null));
    }

    /**
     * @param key
     * @return the date, if set
     */
    public Optional<Date> getDate(String key) {

	String value = getString(key);
	return value.isEmpty() ? Optional.empty() : ISO8601DateTimeUtils.parseISO8601ToDate(value);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	if (getObject().isNull(key)) {
	    return "";
	}

	return getObject().optString(key, "").trim();
    }
}
