/**
 * 
 */
package eu.essi_lab.messages.bond.spatial;

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

/**
 * @author Fabrizio
 */
public class IndexedShape implements SpatialEntity {

    private String id;

    /**
     * @param id
     */
    public IndexedShape(String id) {

	this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {

	return id;
    }

    @Override
    public String toString() {

	return id;
    }

    @Override
    public boolean equals(Object obj) {

	if (obj instanceof IndexedShape) {

	    return this.toString().equals(obj.toString());
	}

	return false;
    }

    @Override
    public IndexedShape clone() {

	return new IndexedShape(getId());
    }
}
