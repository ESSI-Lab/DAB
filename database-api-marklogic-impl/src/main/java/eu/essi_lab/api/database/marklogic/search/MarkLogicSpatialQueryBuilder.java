/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search;

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

import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.index.jaxb.BoundingBox;

/**
 * @author Fabrizio
 */
public interface MarkLogicSpatialQueryBuilder {

    /**
     * @author Fabrizio
     */
    public enum CardinalPoint {

	SOUTH(BoundingBox.SOUTH_ELEMENT_NAME), //
	WEST(BoundingBox.WEST_ELEMENT_NAME), //
	NORTH(BoundingBox.NORTH_ELEMENT_NAME), //
	EAST(BoundingBox.EAST_ELEMENT_NAME), //
	SW(BoundingBox.SW_ELEMENT_NAME), //
	SE(BoundingBox.SE_ELEMENT_NAME), //
	NW(BoundingBox.NW_ELEMENT_NAME), //
	NE(BoundingBox.NE_ELEMENT_NAME); //

	private String name;

	private CardinalPoint(String name) {
	    this.name = name;
	}

	@Override
	public String toString() {
	    return name;
	}
    }

    /**
     * @param bond
     * @return
     */
    public String buildSpatialQuery(SpatialBond bond);

}
