package eu.essi_lab.lib.sensorthings._1_1.model.entities;

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

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;

/**
 * @author Fabrizio
 */
public class Sensor extends Entity {

    /**
     * 
     */
    protected Sensor() {
    }

    /**
     * @param entity
     */
    public Sensor(JSONObject entity) {

	super(entity);
    }

    /**
     * @param entityUrl
     */
    public Sensor(URL entityUrl) {

	super(entityUrl);
    }

    /**
     * @return
     */
    public List<Datastream> getDatastreams() {

	return getEntities(EntityRef.DATASTREAMS, Datastream.class);
    }

    /**
     * Sensor encodingType ValueCode Value
     * <ul>
     * <li>PDF -> application/pdf</li>
     * <li>SensorML -> http://www.opengis.net/doc/IS/SensorML/2.0</li>
     * <li>HTML -> text/html</li>
     * </ul>
     * 
     * @return
     */
    public Optional<String> getEncodingType() {

	return getOptionalString("encodingType");
    }

    /**
     * @return
     */
    public Optional<Object> getMetadata() {

	return getObject().has("metadata") ? Optional.of(getObject().get("metadata")) : Optional.empty();
    }
}
