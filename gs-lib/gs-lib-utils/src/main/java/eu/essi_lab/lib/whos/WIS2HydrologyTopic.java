package eu.essi_lab.lib.whos;

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

/**
 * Data topics of the hydrology Earth system discipline of the WIS2 topic hierarchy where observations are published,
 * as defined in https://github.com/wmo-im/wis2-topic-hierarchy (topic-hierarchy/earth-system-discipline/hydrology)
 *
 * @author boldrini
 */
public enum WIS2HydrologyTopic {

    STREAM("surface-based-observations/water-quantity/stream"), //
    LAKE("surface-based-observations/water-quantity/lake"), //
    RESERVOIR("surface-based-observations/water-quantity/reservoir"), //
    SNOW("surface-based-observations/water-quantity/snow"), //
    ICE("surface-based-observations/water-quantity/ice"), //
    PRECIPITATION("surface-based-observations/atmospheric/precipitation"), //
    GROUNDWATER("subsurface-based-observations/water-quantity/groundwater"), //
    SOIL("subsurface-based-observations/water-quantity/soil");

    private static final String EARTH_SYSTEM_DISCIPLINE = "hydrology";

    private static final String HYDRO_ONTOLOGY_CONCEPT = "http://hydro.geodab.eu/hydro-ontology/concept/";

    private final String path;

    WIS2HydrologyTopic(String path) {

	this.path = path;
    }

    /**
     * @return the path of this topic below the hydrology level (e.g. surface-based-observations/water-quantity/stream)
     */
    public String getPath() {

	return path;
    }

    /**
     * @param centreId the WIS2 centre id
     * @param dataPolicy the data policy of the published data
     * @return the topic where the data notifications are published (e.g.
     *         origin/a/wis2/it-cnr-itiam-whos-dab/data/core/hydrology/surface-based-observations/water-quantity/stream)
     */
    public String getTopic(String centreId, WIS2DataPolicy dataPolicy) {

	return "origin/a/wis2/" + centreId + "/data/" + dataPolicy.getId() + "/" + EARTH_SYSTEM_DISCIPLINE + "/" + path;
    }

    /**
     * @param centreId the WIS2 centre id
     * @return the topic where the metadata notifications are published (e.g. origin/a/wis2/it-cnr-itiam-whos-dab/metadata)
     */
    public static String getMetadataTopic(String centreId) {

	return "origin/a/wis2/" + centreId + "/metadata";
    }

    /**
     * Maps the given observed property to its topic. Only the observed properties having a corresponding topic in the
     * official WIS2 topic hierarchy are mapped
     *
     * @param observedPropertyURI a concept of the WHOS hydro ontology
     * @return the topic of the given observed property, if any
     */
    public static Optional<WIS2HydrologyTopic> decode(String observedPropertyURI) {

	if (observedPropertyURI == null || !observedPropertyURI.startsWith(HYDRO_ONTOLOGY_CONCEPT)) {

	    return Optional.empty();
	}

	switch (observedPropertyURI.substring(HYDRO_ONTOLOGY_CONCEPT.length())) {
	case "11": // level, stream
	case "76": // discharge
	case "78": // discharge, stream
	    return Optional.of(STREAM);

	case "65": // precipitation
	    return Optional.of(PRECIPITATION);

	default:
	    return Optional.empty();
	}
    }
}
