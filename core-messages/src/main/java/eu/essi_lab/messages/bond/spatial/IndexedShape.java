/**
 * 
 */
package eu.essi_lab.messages.bond.spatial;

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
 * @author Fabrizio
 */
public class IndexedShape implements SpatialEntity {

    private final String id;

    /**
     * @param id
     */
    public IndexedShape(String id) {

	this.id = id;
    }

    /**
     * Resolves the shape-files folder entry name from a {@code predefinedLayer} parameter value
     * (e.g. {@code opensearch://shapeFiles:my_dataset_feature.1}).
     *
     * @param predefinedLayerValue full predefined layer online identifier
     * @return entry name as stored in the shape-files folder
     */
    public static String entryNameFromPredefinedLayer(String predefinedLayerValue) {

	if (predefinedLayerValue == null || predefinedLayerValue.isEmpty()) {
	    return predefinedLayerValue;
	}

	int colon = predefinedLayerValue.lastIndexOf(':');

	if (colon < 0) {
	    return predefinedLayerValue;
	}

	return predefinedLayerValue.substring(colon + 1);
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
