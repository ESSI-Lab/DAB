package eu.essi_lab.lib.sensorthings._1_1.model;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * @author Fabrizio
 */
public class ObservedArea extends JSONObjectWrapper {

    /**
     * @param object
     */
    public ObservedArea(JSONObject object) {

	super(object);
    }

    /**
     * @return
     */
    public String getType() {

	return getObject().getString("type");
    }

    /**
     * @return
     */
    public JSONArray getCoordinates() {

	return getObject().getJSONArray("type");
    }
}
