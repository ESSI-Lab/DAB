package eu.essi_lab.lib.sensorthings._1_1.model.entities;

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

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;

/**
 * https://docs.ogc.org/is/18-088/18-088.html#observation
 * 
 * @author Fabrizio
 */
public class Observation extends Entity {

    /**
     * 
     */
    protected Observation() {
    }

    /**
     * @param entity
     */
    public Observation(JSONObject entity) {

	super(entity);
    }

    /**
     * @param entityUrl
     */
    public Observation(URL entityUrl) {

	super(entityUrl);
    }

    /**
     * @return
     */
    public Optional<String> getPhenomenonTime() {

	return getOptionalString("phenomenonTime");
    }

    /**
     * @return
     */
    public Optional<String> getResultTime() {
	
	return getOptionalString("resultTime");
    }

    /**
     * @return
     */
    public Optional<String> getValidTime() {
	
	return getOptionalString("validTime");
    }

    /**
     * @return
     */
    public Optional<JSONObject> getParameters() {

	return getObject().has("parameters") ? Optional.of(getObject().getJSONObject("parameters")) : Optional.empty();
    }

    /**
     * @return
     */
    public Optional<JSONObject> getResultQuality() {

	return getObject().has("resultQuality") ? Optional.of(getObject().getJSONObject("resultQuality")) : Optional.empty();
    }

    /**
     * @return
     */
    public Object getResult() {

	return getObject().get("result");
    }

    /**
     * @return
     */
    public Optional<Datastream> getDatastream() {

	List<Datastream> entities = getEntities(EntityRef.DATASTREAM, Datastream.class);
	return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

    /**
     * @return
     */
    public Optional<FeatureOfInterest> getFeatureOfInterest() {

	List<FeatureOfInterest> entities = getEntities(EntityRef.FEATURE_OF_INTEREST, FeatureOfInterest.class);
	return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }
}
