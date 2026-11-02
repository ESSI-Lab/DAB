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
 * A Trentino floods time series: one quantity (water level or discharge) monitored at a site, described by its entry in the series
 * index
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
     * @return the series identifier, e.g. "32207"
     */
    public String getSeriesId() {

	return getObject().optString(TrentinoFloodsClient.SERIES_ID, null);
    }

    /**
     * @return the site name, e.g. "Ponte Arche"
     */
    public String getSite() {

	return getObject().optString(TrentinoFloodsClient.SITE, "Series " + getSeriesId());
    }

    /**
     * @return the monitored quantity, if known
     */
    public Optional<TrentinoFloodsProperty> getProperty() {

	return TrentinoFloodsProperty.decode(getString(TrentinoFloodsClient.QUANTITY));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
