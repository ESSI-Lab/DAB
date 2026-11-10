package eu.essi_lab.accessor.essaihjucar;

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
 * A SAIH Júcar time series: a variable (idVariable) of a remote station, described by the entry of the station in the map page
 * of its category
 *
 * @author boldrini
 */
public class SAIHJucarEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public SAIHJucarEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public SAIHJucarEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the identifier of the variable, e.g. "14099"
     */
    public String getVariableId() {

	return getObject().optString(SAIHJucarClient.VARIABLE_ID, null);
    }

    /**
     * @return
     */
    public Optional<SAIHJucarVariableType> getType() {

	return SAIHJucarVariableType.decode(getObject().optString(SAIHJucarClient.TYPE, null));
    }

    /**
     * @return the identifier of the remote station, e.g. "349"
     */
    public String getStationId() {

	return getString(SAIHJucarClient.STATION_ID);
    }

    /**
     * @return the name of the station, e.g. "AZUD FONT D'EN CARRÓS"
     */
    public String getStationName() {

	String name = getString(SAIHJucarClient.STATION_NAME);
	return name.isEmpty() ? getVariableId() : name;
    }

    /**
     * @return the name of the variable given by the provider, e.g. "CAUDAL RÍO SERPIS SOBRE EL AZUD"
     */
    public String getVariableName() {

	return getString(SAIHJucarClient.VARIABLE_NAME);
    }

    /**
     * @return the unit used by the provider, e.g. "m³/s", or an empty string if not set
     */
    public String getUnit() {

	String unit = getString(SAIHJucarClient.UNIT);
	if (unit.isEmpty()) {
	    return getType().map(SAIHJucarVariableType::getUnit).orElse("");
	}

	return unit;
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
