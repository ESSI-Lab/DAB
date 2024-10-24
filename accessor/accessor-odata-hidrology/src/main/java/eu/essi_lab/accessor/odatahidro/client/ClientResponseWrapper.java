/**
 * 
 */
package eu.essi_lab.accessor.odatahidro.client;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public class ClientResponseWrapper {

    private JSONObject response;

    /**
     * 
     */
    public ClientResponseWrapper(JSONObject response) {

	this.response = response;
    }

    /**
     * @return
     */
    public int getResponseSize() {

	return response.getJSONArray("value").length();
    }

    //
    // place query response
    //

    /**
     * @return
     */
    public Optional<String> getName(int arrayIndex) {

	return getValue("Nimi", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getHardwareId(int arrayIndex) {

	return getValue("Laitteisto_Id", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getOwnerId(int arrayIndex) {

	return getValue("Omistaja_Id", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getStatusId(int arrayIndex) {

	return getValue("Tila_Id", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getLat(int arrayIndex) {

	return getValue("KoordErTmPohj", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getLon(int arrayIndex) {

	return getValue("KoordErTmIta", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getMunicipalityName(int arrayIndex) {

	return getValue("KuntaNimi", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getPlaceId(int arrayIndex) {

	return getValue("Paikka_Id", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getLakeName(int arrayIndex) {

	return getValue("JarviNimi", arrayIndex);
    }
    
    /**
     * @return
     */
    public Optional<String> getStationCode(int arrayIndex) {

	return getValue("Nro", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getWaterAreaName(int arrayIndex) {

	return getValue("VesalNimi", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getVariableId(int arrayIndex) {

	return getValue("Suure_Id", arrayIndex);
    }

    //
    // information/variable query response
    //

    /**
     * @return
     */
    public Optional<String> getDate(int arrayIndex) {

	return getValue("Aika", arrayIndex);
    }

    /**
     * @return
     */
    public Optional<String> getValue(int arrayIndex) {

	return getValue("Arvo", arrayIndex);
    }

    /**
     * @param key
     * @param arrayIndex
     * @return
     */
    private Optional<String> getValue(String key, int arrayIndex) {

	JSONObject object = response.getJSONArray("value").getJSONObject(arrayIndex);

	if (object.has(key)) {

	    String string = object.get(key).toString();

	    if (!string.equals("null")) {

		return Optional.of(object.get(key).toString());
	    }
	}

	return Optional.empty();
    }

}
