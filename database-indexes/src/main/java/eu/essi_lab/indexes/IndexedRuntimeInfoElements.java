package eu.essi_lab.indexes;

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

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedRuntimeInfoElement;

/**
 * This class groups elements related to {@link RuntimeInfoElement}s
 * 
 * @author Fabrizio
 */
public final class IndexedRuntimeInfoElements extends IndexedElementsGroup {

    /**
     * Retrieves all the {@link IndexedElementInfo}s owned by the {@link IndexedElement}s declared in this class
     * 
     * @param impl
     */
    public static List<IndexedElementInfo> getIndexesInfo(DatabaseImpl impl) {

	return getIndexesInfo(IndexedRuntimeInfoElements.class, impl);
    }

    public static final IndexedRuntimeInfoElement RUNTIME_ID = new IndexedRuntimeInfoElement(RuntimeInfoElement.RUNTIME_ID); //
    public static final IndexedRuntimeInfoElement RUNTIME_CONTEXT = new IndexedRuntimeInfoElement(RuntimeInfoElement.RUNTIME_CONTEXT); //

    public static final IndexedRuntimeInfoElement WEB_REQUEST_HOST = new IndexedRuntimeInfoElement(RuntimeInfoElement.WEB_REQUEST_HOST); //
    public static final IndexedRuntimeInfoElement WEB_REQUEST_TIME_STAMP = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.WEB_REQUEST_TIME_STAMP); //
    public static final IndexedRuntimeInfoElement WEB_REQUEST_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.WEB_REQUEST_TIME_STAMP_MILLIS); //

    public static final IndexedRuntimeInfoElement WEB_REQUEST_ORIGIN = new IndexedRuntimeInfoElement(RuntimeInfoElement.WEB_REQUEST_ORIGIN); //
    public static final IndexedRuntimeInfoElement WEB_REQUEST_X_FORWARDER_FOR = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_SCHEDULED = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_SCHEDULED); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_TIME_STAMP = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP_MILLIS); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_SOURCE_LABEL = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_SOURCE_ID = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_ID); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_EAST = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_EAST); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_WEST = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_WEST); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_NORTH = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NORTH); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_SOUTH = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SOUTH); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_NE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NE); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_NW = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NW); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_SE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SE); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_BBOX_SW = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SW); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_TMP_EXTENT_BEGIN = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_TMP_EXTENT_BEGIN); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_TMP_EXTENT_END = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_TMP_EXTENT_END); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_ABSTRACT = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_ABSTRACT); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_SUBJECT = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_SUBJECT); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_TITLE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_TITLE); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_KEYWORD = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_KEYWORD); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_ORGANISATION_NAME = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_ORGANISATION_NAME); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_PAGE_SIZE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_SIZE); //
    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_PAGE_START = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_START); //

    public static final IndexedRuntimeInfoElement RESULT_SET_TIME_STAMP = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_TIME_STAMP); //

    public static final IndexedRuntimeInfoElement RESULT_SET_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_TIME_STAMP_MILLIS); //

    public static final IndexedRuntimeInfoElement RESULT_SET_RETURNED = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_RETURNED); //

    public static final IndexedRuntimeInfoElement RESULT_SET_MATCHED = new IndexedRuntimeInfoElement(RuntimeInfoElement.RESULT_SET_MATCHED); //

    public static final IndexedRuntimeInfoElement RESULT_SET_RESOURCE_TITLE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_RESOURCE_TITLE); //

    public static final IndexedRuntimeInfoElement CHRONOMETER_TIME_STAMP = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.CHRONOMETER_TIME_STAMP); //

    public static final IndexedRuntimeInfoElement CHRONOMETER_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.CHRONOMETER_TIME_STAMP_MILLIS); //

    public static final IndexedRuntimeInfoElement CHRONOMETER_ELAPSED_TIME = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.CHRONOMETER_ELAPSED_TIME_MILLIS); //

    public static final IndexedRuntimeInfoElement DISCOVERY_MESSAGE_VIEW_ID = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.DISCOVERY_MESSAGE_VIEW_ID);

    public static final IndexedRuntimeInfoElement RESULT_SET_DISCOVERY_SOURCE_ID = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID);

    public static final IndexedRuntimeInfoElement RESULT_SET_DISCOVERY_SOURCE_LABEL = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_LABEL);

    public static final IndexedRuntimeInfoElement PROFILER_NAME = new IndexedRuntimeInfoElement(RuntimeInfoElement.PROFILER_NAME);

    public static final IndexedRuntimeInfoElement ACCESS_MESSAGE_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.ACCESS_MESSAGE_TIME_STAMP_MILLIS); //

    public static final IndexedRuntimeInfoElement ACCESS_MESSAGE_VIEW_ID = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.ACCESS_MESSAGE_VIEW_ID);

    public static final IndexedRuntimeInfoElement ACCESS_MESSAGE_CRS = new IndexedRuntimeInfoElement(RuntimeInfoElement.ACCESS_MESSAGE_CRS);

    public static final IndexedRuntimeInfoElement ACCESS_MESSAGE_DATA_FORMAT = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.ACCESS_MESSAGE_DATA_FORMAT);

    public static final IndexedRuntimeInfoElement ACCESS_MESSAGE_DATA_TYPE = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.ACCESS_MESSAGE_DATA_TYPE);

    public static final IndexedRuntimeInfoElement RESULT_SET_ACCESS_SOURCE_ID = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_ID);

    public static final IndexedRuntimeInfoElement RESULT_SET_ACCESS_SOURCE_LABEL = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_LABEL);

    public static final IndexedRuntimeInfoElement PROFILER_TIME_STAMP_MILLIS = new IndexedRuntimeInfoElement(
	    RuntimeInfoElement.PROFILER_TIME_STAMP_MILLIS);

    /**
     * @return
     */
    public static List<IndexedElement> getIndexes() {

	return getIndexes(IndexedRuntimeInfoElements.class).//
		stream().//
		map(index -> (IndexedRuntimeInfoElement) index).//
		collect(Collectors.toList());
    }

    private static List<IndexedRuntimeInfoElement> getStatisticalIndexes() {

	return getIndexes(IndexedRuntimeInfoElements.class).//
		stream().//
		map(index -> (IndexedRuntimeInfoElement) index).//
		collect(Collectors.toList());
    }

    static {
	// --------------------------------------------------------------------------------------------------------------------------
	// defined only for MarkLogic
	//

	DISCOVERY_MESSAGE_BBOX_NE.getInfoList().add(//
		new IndexedElementInfo( //
			DISCOVERY_MESSAGE_BBOX_NE.getElementName(), //
			DatabaseImpl.MARK_LOGIC.getName(), //
			MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
			null));

	DISCOVERY_MESSAGE_BBOX_SE.getInfoList().add(//
		new IndexedElementInfo( //
			DISCOVERY_MESSAGE_BBOX_SE.getElementName(), //
			DatabaseImpl.MARK_LOGIC.getName(), //
			MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
			null));

	DISCOVERY_MESSAGE_BBOX_NW.getInfoList().add(//
		new IndexedElementInfo( //
			DISCOVERY_MESSAGE_BBOX_NW.getElementName(), //
			DatabaseImpl.MARK_LOGIC.getName(), //
			MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
			null));

	DISCOVERY_MESSAGE_BBOX_SW.getInfoList().add(//
		new IndexedElementInfo( //
			DISCOVERY_MESSAGE_BBOX_SW.getElementName(), //
			DatabaseImpl.MARK_LOGIC.getName(), //
			MarkLogicIndexTypes.GEOSPATIAL_ELEMENT_INDEX.getType(), //
			null));

	for (IndexedRuntimeInfoElement index : getStatisticalIndexes()) {

	    switch (index.getStatisticalElement().getContentType()) {
	    /**
	     * this is in fact not directly indexed, it is indexed through its accessory indexes
	     */
	    case SPATIAL:

		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				null, //
				null));//
		break;
	    /**
	     * int values
	     */
	    case INTEGER:

		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.INT.getType()));
		break;

	    /**
	     * long values
	     */
	    case LONG:

		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.LONG.getType()));
		break;

	    /**
	     * double values
	     */
	    case DOUBLE:

		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.DOUBLE.getType()));
		break;

	    /**
	     * string values
	     */
	    default:
		index.getInfoList().add(//
			new IndexedElementInfo(//
				index.getElementName(), //
				DatabaseImpl.MARK_LOGIC.getName(), //
				MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType(), //
				MarkLogicScalarType.STRING.getType()));
	    }
	}
    }
}
