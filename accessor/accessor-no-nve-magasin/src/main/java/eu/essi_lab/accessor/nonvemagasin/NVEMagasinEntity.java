package eu.essi_lab.accessor.nonvemagasin;

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
 * A NVE reservoir statistics time series: an area combined with one of the weekly properties, described by the entry of the area
 * of the HentOmråder operation
 *
 * @author boldrini
 */
public class NVEMagasinEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public NVEMagasinEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public NVEMagasinEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "EL|1|fyllingsgrad"
     */
    public String getSeriesId() {

	return getObject().optString(NVEMagasinClient.SERIES_ID, null);
    }

    /**
     * @return the area type, e.g. "EL"
     */
    public String getAreaType() {

	return getObject().optString(NVEMagasinClient.AREA_TYPE, null);
    }

    /**
     * @return the area number, e.g. 1
     */
    public int getAreaNumber() {

	return getObject().optInt(NVEMagasinClient.AREA_NUMBER, -1);
    }

    /**
     * @return the area code, e.g. "EL|1"
     */
    public String getAreaCode() {

	return NVEMagasinClient.buildAreaCode(getAreaType(), getAreaNumber());
    }

    /**
     * @return
     */
    public Optional<NVEMagasinArea> getArea() {

	return NVEMagasinArea.decode(getAreaType(), getAreaNumber());
    }

    /**
     * @return the English name of the area, or the long Norwegian name if the area is unknown
     */
    public String getAreaName() {

	return getArea().map(NVEMagasinArea::getEnglishName).orElse(getString(NVEMagasinClient.AREA_LONG_NAME).isEmpty() ? //
		getAreaCode() : getString(NVEMagasinClient.AREA_LONG_NAME));
    }

    /**
     * @return
     */
    public Optional<NVEMagasinProperty> getProperty() {

	return NVEMagasinProperty.decode(getObject().optString(NVEMagasinClient.PROPERTY, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
