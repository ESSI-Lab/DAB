package eu.essi_lab.accessor.esaca;

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
 * An ACA time series: a Sentilo sensor measuring one property at a component (station), described by the sensor entry of the
 * catalog
 *
 * @author boldrini
 */
public class ACAEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public ACAEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public ACAEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "AFORAMENT-EST|CALC001658"
     */
    public String getSeriesId() {

	return getObject().optString(ACAClient.SERIES_ID, null);
    }

    /**
     * @return the Sentilo provider, e.g. "AFORAMENT-EST"
     */
    public String getProvider() {

	return getObject().optString(ACAClient.PROVIDER, null);
    }

    /**
     * @return the sensor identifier, e.g. "CALC001658"
     */
    public String getSensor() {

	return getObject().optString(ACAClient.SENSOR, null);
    }

    /**
     * @return the component (station) identifier, e.g. "080060-001"
     */
    public String getComponent() {

	return getObject().optString(ACAClient.COMPONENT, "");
    }

    /**
     * @return the component (station) name, e.g. "Arenys de Munt (riera d'Arenys)"
     */
    public String getComponentName() {

	String name = getString(ACAClient.COMPONENT_DESCRIPTION);
	return name.isEmpty() ? getComponent() : name;
    }

    /**
     * @return the component type, e.g. "aforament"
     */
    public String getComponentType() {

	return getString(ACAClient.COMPONENT_TYPE);
    }

    /**
     * @return the Catalan description of the sensor, e.g. "Cabal riu"
     */
    public String getDescription() {

	return getString(ACAClient.DESCRIPTION);
    }

    /**
     * @return
     */
    public Optional<ACAProperty> getProperty() {

	return ACAProperty.decode(getDescription());
    }

    /**
     * @return the unit, e.g. "m³/s", or an empty string if not set
     */
    public String getUnit() {

	return getString(ACAClient.UNIT);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }

    /**
     * @param key
     * @return the value of the additional info of the component, or an empty string if not set
     */
    public String getComponentInfo(String key) {

	JSONObject info = getObject().optJSONObject(ACAClient.COMPONENT_ADDITIONAL_INFO);
	return info == null ? "" : info.optString(key, "").trim();
    }

    /**
     * @param key
     * @return the value of the additional info of the sensor, or an empty string if not set
     */
    public String getSensorInfo(String key) {

	JSONObject info = getObject().optJSONObject(ACAClient.ADDITIONAL_INFO);
	return info == null ? "" : info.optString(key, "").trim();
    }
}
