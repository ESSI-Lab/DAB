package eu.essi_lab.lib.sensorthings._1_1.client.request;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.FeatureOfInterest;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.HistoricalLocation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Observation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.ObservedProperty;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;

/**
 * @author Fabrizio
 */
public class EntityRef {

    /**
     * This is the navigation name of the Datastream entity
     */
    public static final EntityRef THING = new EntityRef("Thing", Thing.class);
    /**
     * 
     */
    public static final EntityRef THINGS = new EntityRef("Things", Thing.class);
    /**
     * 
     */
    public static final EntityRef LOCATIONS = new EntityRef("Locations", Location.class);
    /**
     * 
     */
    public static final EntityRef HISTORICAL_LOCATIONS = new EntityRef("HistoricalLocations", HistoricalLocation.class);
    /**
     * 
     */
    public static final EntityRef DATASTREAMS = new EntityRef("Datastreams", Datastream.class);
    /**
     * This is the navigation name of the Observation entity
     */
    public static final EntityRef DATASTREAM = new EntityRef("Datastream", Datastream.class);
    /**
     * 
     */
    public static final EntityRef SENSORS = new EntityRef("Sensors", Sensor.class);
    /**
     * 
     */
    public static final EntityRef SENSOR = new EntityRef("Sensor", Sensor.class);
    /**
     * 
     */
    public static final EntityRef OBSERVATIONS = new EntityRef("Observations", Observation.class);
    /**
     * 
     */
    public static final EntityRef OBSERVED_PROPERTIES = new EntityRef("ObservedProperties", ObservedProperty.class);
    /**
     * 
     */
    public static final EntityRef OBSERVED_PROPERTY = new EntityRef("ObservedProperty", ObservedProperty.class);
    /**
     * 
     */
    public static final EntityRef FEATURES_OF_INTEREST = new EntityRef("FeaturesOfInterest", FeatureOfInterest.class);
    /**
     * 
     */
    public static final EntityRef FEATURE_OF_INTEREST = new EntityRef("FeatureOfInterest", FeatureOfInterest.class);

    /**
     * @param name
     * @return
     */
    public static EntityRef of(String name) {

	return new EntityRef(name, Entity.class);
    }

    private String name;
    private Class<? extends Entity> targetClass;

    /**
     * @param name
     */
    private EntityRef(String name, Class<? extends Entity> targetClass) {

	this.name = name;
	this.targetClass = targetClass;
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @return the navigationLink
     */
    public String getLink() {

	return name + "@iot.navigationLink";
    }

    /**
     * @return
     */
    public Class<? extends Entity> getTargetClass() {

	return targetClass;
    }
}
