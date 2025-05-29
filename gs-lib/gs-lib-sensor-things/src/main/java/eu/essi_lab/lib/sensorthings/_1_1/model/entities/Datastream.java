package eu.essi_lab.lib.sensorthings._1_1.model.entities;

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

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.model.ObservedArea;
import eu.essi_lab.lib.sensorthings._1_1.model.UnitOfMeasurement;

/**
 * https://docs.ogc.org/is/18-088/18-088.html#datastream
 * 
 * @author Fabrizio
 */
public class Datastream extends Entity {

    /**
     * 
     */
    protected Datastream() {
    }

    /**
     * @param entity
     */
    public Datastream(JSONObject entity) {

	super(entity);
    }

    /**
     * @param entityUrl
     */
    public Datastream(URL entityUrl) {

	super(entityUrl);
    }

    /**
     * @return
     */
    public String getObservationType() {

	return getObject().getString("observationType");
    }

    /**
     * @return
     */
    public UnitOfMeasurement getUnitOfMeasurement() {

	return new UnitOfMeasurement(getObject().getJSONObject("unitOfMeasurement"));
    }

    /**
     * @return
     */
    public Optional<ObservedArea> getObservedArea() {

	return getObject().has("observedArea") ? Optional.of(new ObservedArea(getObject().getJSONObject("observedArea")))
		: Optional.empty();
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
    public Optional<Thing> getThing() {

	List<Thing> entities = getEntities(EntityRef.THING, Thing.class);
	return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

    /**
     * @return
     */
    public Optional<Sensor> getSensor() {

	List<Sensor> entities = getEntities(EntityRef.SENSOR, Sensor.class);
	return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

    /**
     * @return
     */
    public Optional<ObservedProperty> getObservedProperty() {

	List<ObservedProperty> entities = getEntities(EntityRef.OBSERVED_PROPERTY, ObservedProperty.class);
	return entities.isEmpty() ? Optional.empty() : Optional.of(entities.get(0));
    }

    /**
     * @return
     */
    public List<Observation> getObservations() {

	return getEntities(EntityRef.OBSERVATIONS, Observation.class);
    }
}
