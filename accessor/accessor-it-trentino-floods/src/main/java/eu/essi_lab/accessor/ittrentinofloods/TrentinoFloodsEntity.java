package eu.essi_lab.accessor.ittrentinofloods;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * A Trentino floods time series: one hydrometric sensor (water level or calculated discharge) of a station, described by its
 * entry in the sensor registry
 *
 * @author boldrini
 */
public class TrentinoFloodsEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public TrentinoFloodsEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public TrentinoFloodsEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "1|5"
     */
    public String getSeriesId() {

	return getObject().optString(TrentinoFloodsClient.SERIES_ID, null);
    }

    /**
     * @return the sensor identifier, e.g. "5"
     */
    public String getSensorId() {

	return getObject().optString(TrentinoFloodsClient.SENSOR_ID, null);
    }

    /**
     * @return the dataset identifier, e.g. "1"
     */
    public String getDataset() {

	return getObject().optString(TrentinoFloodsClient.DATASET, null);
    }

    /**
     * @return the station name, e.g. "Borgo Valsugana"
     */
    public String getStationName() {

	return getObject().optString(TrentinoFloodsClient.STATION_NAME, "Sensor " + getSensorId());
    }

    /**
     * @return
     */
    public Optional<TrentinoFloodsProperty> getProperty() {

	return TrentinoFloodsProperty.decode(getDataset());
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
