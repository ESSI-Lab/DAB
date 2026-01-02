package eu.essi_lab.lib.sensorthings._1_1.client.response;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;

/**
 * <pre>
 * <code>
 * {
  "@iot.count":84,
  "value": [
    {
      "@iot.id": 1,
      "@iot.selfLink": "http://example.org/v1.1/ObservedProperties(1)",
      "Datastreams@iot.navigationLink": "ObservedProperties(1)/Datastreams",
      "description": "The dew point is the temperature at which the water
                      vapor in air at constant barometric pressure condenses
                      into liquid water at the same rate at which it evaporates.",
      "name": "DewPoint Temperature",
      "definition": "http://dbpedia.org/page/Dew_point"
    },
    {
      "@iot.id ": 2,
      "@iot.selfLink": "http://example.org/v1.1/ObservedProperties(2)",
      "Datastreams@iot.navigationLink": "ObservedProperties(2)/Datastreams",
      "description": "Relative humidity is the ratio of the partial pressure
                      of water vapor in an air-water mixture to the saturated
                      vapor pressure of water at a prescribed temperature.",
      "name": "Relative Humidity",
      "definition": "http://dbpedia.org/page/Relative_humidity"
    },{…},{…},{…}
  ],
  "@iot.nextLink":"http://example.org/v1.1/ObservedProperties?$top=5&$skip=5"
}</pre></code>
 * 
 * @author Fabrizio
 */
public class AddressableEntityResult<E extends Entity> extends PaginatedResult {

    /**
     * 
     */
    private Class<E> entityClass;

    /**
     * @param object
     * @param entityClass
     */
    public AddressableEntityResult(String object, Class<E> entityClass) {

	super(new JSONObject(object));
	this.entityClass = entityClass;
    }

    /**
     * @param object
     * @param entityClass
     */
    public AddressableEntityResult(JSONObject object, Class<E> entityClass) {

	super(object);
	this.entityClass = entityClass;
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<E> getEntities() {

	if (getObject().has("value")) {

	    return getObject().//
		    getJSONArray("value").//
		    toList().//
		    stream().//
		    map(v -> new JSONObject((HashMap<String, String>) v)).//
		    map(v -> Entity.create(v, this.entityClass)).//
		    collect(Collectors.toList());
	}

	return Arrays.asList(Entity.create(getObject(), this.entityClass));
    }

}
