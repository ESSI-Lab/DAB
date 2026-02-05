package eu.essi_lab.messages.bond.spatial;

import com.fasterxml.jackson.annotation.*;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.*;
import eu.essi_lab.model.ontology.*;
import eu.essi_lab.model.resource.*;

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

/**
 * Interface for spatial entities used as target of a {@link SpatialBond}
 * 
 * @author Fabrizio
 */
@JsonTypeInfo( //
	use = JsonTypeInfo.Id.NAME,//
	include = JsonTypeInfo.As.PROPERTY,//
	property = "type"//
)
@JsonSubTypes({//
	@JsonSubTypes.Type(value = IndexedShape.class, name = "indexedShape"),//
	@JsonSubTypes.Type(value = SpatialExtent.class, name = "spatialExtent"),//
	@JsonSubTypes.Type(value = WKT.class, name = "wkt")//
})
public interface SpatialEntity extends Cloneable {

    SpatialEntity clone();

    /**
     * @param shapeId
     * @return
     */
    static IndexedShape ofIndexedShape(String shapeId) {

	return new IndexedShape(shapeId);
    }

    /**
     * @param wkt
     * @return
     */
    static WKT of(String wkt) {

	return new WKT(wkt);
    }

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     * @return
     */
    static SpatialExtent of(double south, double west, double north, double east) {

	return new SpatialExtent(south, west, north, east);
    }
}
