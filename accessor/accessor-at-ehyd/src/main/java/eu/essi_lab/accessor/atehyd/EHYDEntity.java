package eu.essi_lab.accessor.atehyd;

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
 * An eHYD time series: a station (identified by its HZB number) combined with one of its archive products (see
 * {@link EHYDProduct}), described by the entry of the station list and by the header of the CSV file of the product
 *
 * @author boldrini
 */
public class EHYDEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public EHYDEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public EHYDEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "201582|owf_wasserst_tagesmittel"
     */
    public String getSeriesId() {

	return getObject().optString(EHYDClient.SERIES_ID, null);
    }

    /**
     * @return the HZB number of the station, e.g. "201582"
     */
    public String getHZBNumber() {

	return getObject().optString(EHYDClient.HZBNR, null);
    }

    /**
     * @return the station name, or the HZB number if not known
     */
    public String getStationName() {

	String name = getString(EHYDClient.STATION_NAME);
	return name.isEmpty() ? getHZBNumber() : name;
    }

    /**
     * @return
     */
    public Optional<EHYDProduct> getProduct() {

	return EHYDProduct.decode(getObject().optString(EHYDClient.PRODUCT, null));
    }

    /**
     * @return the number of the file of the product at the station, or null if not known
     */
    public Integer getFileNumber() {

	return getObject().has(EHYDClient.FILE_NUMBER) ? getObject().optInt(EHYDClient.FILE_NUMBER) : null;
    }

    /**
     * @return the unit reported by the file header (e.g. "cm"), or the default unit of the product
     */
    public String getUnit() {

	String unit = getString(EHYDClient.UNIT);
	if (unit.isEmpty()) {
	    return getProduct().map(EHYDProduct::getDefaultUnit).orElse("");
	}

	return unit;
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
