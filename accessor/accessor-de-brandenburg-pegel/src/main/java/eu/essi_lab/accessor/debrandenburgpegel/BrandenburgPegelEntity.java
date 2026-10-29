package eu.essi_lab.accessor.debrandenburgpegel;

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
 * A Pegelportal Brandenburg time series: a gauge combined with one of its parameters, described by the feature of the gauge in
 * the map of the start page
 *
 * @author boldrini
 */
public class BrandenburgPegelEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public BrandenburgPegelEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public BrandenburgPegelEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "0491101|W"
     */
    public String getSeriesId() {

	return getObject().optString(BrandenburgPegelClient.SERIES_ID, null);
    }

    /**
     * @return the name of the online resource, e.g. "0491101|W|1" (series identifier and river basin)
     */
    public String getOnlineName() {

	String riverBasin = getRiverBasinId();
	return riverBasin.isEmpty() ? getSeriesId() : getSeriesId() + BrandenburgPegelClient.SERIES_ID_SEPARATOR + riverBasin;
    }

    /**
     * @return the gauge number (Pegelkennzahl), e.g. "0491101"
     */
    public String getStationNumber() {

	return getObject().optString(BrandenburgPegelClient.STATION_NO, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(BrandenburgPegelClient.STATION_NAME, getStationNumber());
    }

    /**
     * @return the identifier of the river basin (Flussgebiet) of the gauge, e.g. "1", or an empty string if not set
     */
    public String getRiverBasinId() {

	return getString(BrandenburgPegelClient.RIVER_BASIN_ID);
    }

    /**
     * @return
     */
    public Optional<BrandenburgPegelParameter> getParameter() {

	return BrandenburgPegelParameter.decode(getObject().optString(BrandenburgPegelClient.PARAMETER, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
