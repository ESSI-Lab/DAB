package eu.essi_lab.indexes;

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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * This class groups elements written by {@link IndexedMetadataElement}s. They store additional
 * element information used to build a discovery query for the Database
 * 
 * @author Fabrizio
 */
public final class IndexedElements extends IndexedElementsGroup {

    /**
     * Retrieves all the {@link IndexedElementInfo}s owned by the {@link IndexedElement}s declared in this class
     * 
     * @param impl
     * @return
     */
    public static List<IndexedElementInfo> getIndexesInfo(DatabaseImpl impl) {

	return getIndexesInfo(IndexedElements.class, impl);
    }

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to store the area in degree of a bounding box, and used to make contains queries
     */
    public static final IndexedElement AREA = new IndexedElement(BoundingBox.AREA_ELEMENT_NAME);

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement SOUTH = new IndexedElement(BoundingBox.SOUTH_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement NORTH = new IndexedElement(BoundingBox.NORTH_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement WEST = new IndexedElement(BoundingBox.WEST_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement EAST = new IndexedElement(BoundingBox.EAST_ELEMENT_NAME);

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build disjoint spatial queries
     */
    public static final IndexedElement DISJ_SOUTH = new IndexedElement(BoundingBox.DISJ_SOUTH_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build disjoint spatial queries
     */
    public static final IndexedElement DISJ_NORTH = new IndexedElement(BoundingBox.DISJ_NORTH_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build disjoint spatial queries
     */
    public static final IndexedElement DISJ_WEST = new IndexedElement(BoundingBox.DISJ_WEST_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build disjoint spatial queries
     */
    public static final IndexedElement DISJ_EAST = new IndexedElement(BoundingBox.DISJ_EAST_ELEMENT_NAME);

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement SW = new IndexedElement(BoundingBox.SW_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement SE = new IndexedElement(BoundingBox.SE_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement NW = new IndexedElement(BoundingBox.NW_ELEMENT_NAME);
    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Used to build spatial queries
     */
    public static final IndexedElement NE = new IndexedElement(BoundingBox.NE_ELEMENT_NAME);

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Set during metadata storing after a values check
     */
    public static final IndexedElement IS_CROSSED = new IndexedElement(BoundingBox.IS_CROSSED_ELEMENT_NAME);

    /**
     * Accessory to {@link IndexedMetadataElements#BOUNDING_BOX}<br>
     * Set (with empty value) by the the correspondent {@link IndexedMetadataElement}
     */
    public static final IndexedElement BOUNDING_BOX_NULL = new IndexedElement("bbox_Null") {

	public List<String> getValues() {

	    ArrayList<String> list = Lists.newArrayList();
	    list.add("");

	    return list;
	}
    };

    /**
     * Accessory to {@link IndexedMetadataElements#TEMP_EXTENT_BEGIN}<br>
     * Set (with empty value) by the the correspondent {@link IndexedMetadataElement}
     */
    public static final IndexedElement TEMP_EXTENT_BEGIN_NOW = new IndexedElement(MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName()) {

	public List<String> getValues() {

	    ArrayList<String> list = Lists.newArrayList();
	    list.add("");

	    return list;
	}
    };

    /**
     * Accessory to {@link IndexedMetadataElements#TEMP_EXTENT_BEGIN}<br>
     * Set (with empty value) by the the correspondent {@link IndexedMetadataElement}
     */
    public static final IndexedElement TEMP_EXTENT_BEGIN_NULL = new IndexedElement("tmpExtentBegin_Null") {

	public List<String> getValues() {

	    ArrayList<String> list = Lists.newArrayList();
	    list.add("");

	    return list;
	}
    };

    /**
     * Accessory to {@link IndexedMetadataElements#TEMP_EXTENT_END}<br>
     * Set (with empty value) by the the correspondent {@link IndexedMetadataElement}
     */
    public static final IndexedElement TEMP_EXTENT_END_NOW = new IndexedElement(MetadataElement.TEMP_EXTENT_END_NOW.getName()) {

	public List<String> getValues() {

	    ArrayList<String> list = Lists.newArrayList();
	    list.add("");

	    return list;
	}
    };

    /**
     * Accessory to {@link IndexedMetadataElements#TEMP_EXTENT_END}<br>
     * Set (with empty value) by the the correspondent {@link IndexedMetadataElement}
     */
    public static final IndexedElement TEMP_EXTENT_END_NULL = new IndexedElement("tmpExtentEnd_Null") {

	public List<String> getValues() {

	    ArrayList<String> list = Lists.newArrayList();
	    list.add("");

	    return list;
	}
    };

    static {

	for (IndexedElement index : getIndexes(IndexedElements.class)) {

	    switch (index.getElementName()) {
	    /**
	     * geospatial indexes
	     */
	    case BoundingBox.NE_ELEMENT_NAME:
	    case BoundingBox.NW_ELEMENT_NAME:
	    case BoundingBox.SE_ELEMENT_NAME:
	    case BoundingBox.SW_ELEMENT_NAME:
		index.getInfoList().add(//
			new IndexedElementInfo( //
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
				null));
		break;
	    /**
	     * double indexes
	     */
	    case BoundingBox.SOUTH_ELEMENT_NAME:
	    case BoundingBox.EAST_ELEMENT_NAME:
	    case BoundingBox.NORTH_ELEMENT_NAME:
	    case BoundingBox.WEST_ELEMENT_NAME:
	    case BoundingBox.AREA_ELEMENT_NAME:
	    case BoundingBox.DISJ_EAST_ELEMENT_NAME:
	    case BoundingBox.DISJ_NORTH_ELEMENT_NAME:
	    case BoundingBox.DISJ_SOUTH_ELEMENT_NAME:
	    case BoundingBox.DISJ_WEST_ELEMENT_NAME:

		index.getInfoList()
			.add(new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.DOUBLE.getType()));
		break;
	    /**
	     * string indexes
	     */
	    default:
		index.getInfoList()
			.add(new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.STRING.getType()));
	    }
	}
    }
}
