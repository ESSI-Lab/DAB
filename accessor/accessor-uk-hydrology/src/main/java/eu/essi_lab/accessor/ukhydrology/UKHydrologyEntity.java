package eu.essi_lab.accessor.ukhydrology;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * @author boldrini
 */
public class UKHydrologyEntity extends JSONObjectWrapper {

    /**
     * @author boldrini
     */
    public enum EntityType {

	STATION, MEASURE, READING;
    }

    /**
     * @param source
     */
    public UKHydrologyEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public UKHydrologyEntity(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     * @param type
     */
    public UKHydrologyEntity(JSONObject object, EntityType type) {

	super(object);
	setType(type);
    }

    /**
     * @param type
     */
    public void setType(EntityType type) {

	switch (type) {
	case STATION:
	    getObject().put("entityType", "station");
	    break;
	case MEASURE:
	    getObject().put("entityType", "measure");
	    break;
	case READING:
	    getObject().put("entityType", "reading");
	    break;
	}
    }

    /**
     * @return
     */
    public EntityType getType() {

	switch (getObject().getString("entityType")) {
	case "station":
	    return EntityType.STATION;
	case "measure":
	    return EntityType.MEASURE;
	case "reading":
	    return EntityType.READING;
	default:
	    throw new IllegalArgumentException("Unknown entity type: " + getObject().getString("entityType"));
	}
    }
}
