package eu.essi_lab.accessor.kisters;

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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * @author Fabrizio
 */
public class KISTERSEntity extends JSONObjectWrapper {

    /**
     * @author Fabrizio
     */
    public enum EntityType {

	STATION, TIME_SERIES, TIME_SERIES_VALUES;
    }

    /**
     * @param source
     */
    public KISTERSEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public KISTERSEntity(JSONObject object) {

	super(object);
    }

    /**
     * @param fields
     * @param values
     * @param type
     */
    public KISTERSEntity(List<String> fields, List<String> values, EntityType type) {

	this(fields, values);

	setType(type);
    }

    /**
     * @param fields
     * @param values
     */
    public KISTERSEntity(List<String> fields, List<String> values) {

	JSONObject object = new JSONObject();
	setObject(object);
	for (int i = 0; i < fields.size(); i++) {
	    object.put(fields.get(i), values.get(i));
	}
    }

    /**
     * @param fields
     * @param values
     */
    public KISTERSEntity(JSONArray fields, List<String> values) {

	JSONObject object = new JSONObject();
	setObject(object);
	for (int i = 0; i < fields.length(); i++) {
	    object.put(fields.getString(i), values.get(i));
	}
    }

    /**
     * @param type
     */
    public void setType(EntityType type) {

	switch (type) {
	case TIME_SERIES_VALUES:
	    getObject().put("entityType", "timeSeriesValues");
	    break;
	case TIME_SERIES:
	    getObject().put("entityType", "timeSeries");
	    break;
	case STATION:
	    getObject().put("entityType", "station");
	    break;
	}
    }

    /**
     * @return
     */
    public EntityType getType() {

	EntityType type = null;

	switch (getObject().getString("entityType")) {
	case "station":
	    type = EntityType.STATION;
	    break;

	case "timeSeries":
	    type = EntityType.TIME_SERIES;
	    break;
	case "timeSeriesValues":
	    type = EntityType.TIME_SERIES_VALUES;
	    break;
	}

	return type;
    }
}
