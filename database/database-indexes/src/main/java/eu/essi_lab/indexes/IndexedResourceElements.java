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

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourcePropertyHandler;

/**
 * This class groups elements related to {@link ResourceProperty}s, see fields doc for more info
 * 
 * @see ResourceProperty
 * @see IsDeletedBond
 * @see IsGDC_Bond
 * @see IsValidBond
 * @see IsRepairedBond
 * @see TimeStampBond
 * @author Fabrizio
 */
public final class IndexedResourceElements extends IndexedElementsGroup {

    /**
     * Retrieves all the {@link IndexedElementInfo}s owned by the {@link IndexedElement}s declared in this class
     * 
     * @param impl
     */
    public static List<IndexedElementInfo> getIndexesInfo(DatabaseImpl impl) {

	return getIndexesInfo(IndexedResourceElements.class, impl);
    }

    /**
     * This index is written by {@link GSResource} in the constructor
     */
    public static final IndexedResourceProperty TYPE = new IndexedResourceProperty(ResourceProperty.TYPE);

    /**
     * This index is written by {@link GSResource#setPrivateId(String)}
     */
    public static final IndexedResourceProperty PRIVATE_ID = new IndexedResourceProperty(ResourceProperty.PRIVATE_ID);

    /**
     * This index is written by {@link GSResource#setOriginalId(String)}
     */
    public static final IndexedResourceProperty ORIGINAL_ID = new IndexedResourceProperty(ResourceProperty.ORIGINAL_ID);

    /**
     * See {@link ResourcePropertyHandler#setOAIPMHHeaderIdentifier(String)}
     */
    public static final IndexedResourceProperty OAI_PMH_HEADER_ID = new IndexedResourceProperty(ResourceProperty.OAI_PMH_HEADER_ID);

    /**
     * Set by Harvester
     */
    public static final IndexedResourceProperty RECOVERY_REMOVAL_TOKEN = new IndexedResourceProperty(
	    ResourceProperty.RECOVERY_REMOVAL_TOKEN);

    /**
     * This index is written by {@link GSResource#setSource(eu.essi_lab.model.GSSource)}
     */
    public static final IndexedResourceProperty SOURCE_ID = new IndexedResourceProperty(ResourceProperty.SOURCE_ID);

    /**
     * The value is set by the {@link IndexedElementsWriter} when the metadata is stored.<br>
     * Used by the {@link MarkLogicSearchBuilder} when building the {@link SourceIdentifierBond} query.<br>
     * See {@link ResourcePropertyHandler#setResourceTimeStamp()}
     */
    public static final IndexedResourceProperty RESOURCE_TIME_STAMP = new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP);

    /**
     * See {@link ResourcePropertyHandler#setIsDeleted(boolean)}
     */
    public static final IndexedResourceProperty IS_DELETED = new IndexedResourceProperty(ResourceProperty.IS_DELETED);
    /**
     * See {@link ResourcePropertyHandler#setIsValidated(boolean)}
     */
    public static final IndexedResourceProperty IS_VALIDATED = new IndexedResourceProperty(ResourceProperty.IS_VALIDATED);

    /**
     * See {@link ResourcePropertyHandler#setIsGDC(boolean)}
     */
    public static final IndexedResourceProperty IS_GEOSS_DATA_CORE = new IndexedResourceProperty(ResourceProperty.IS_GEOSS_DATA_CORE);

    /**
     * See {@link ResourcePropertyHandler#setIsISOCompliant(boolean)}
     */
    public static final IndexedResourceProperty IS_ISO_COMPLIANT = new IndexedResourceProperty(ResourceProperty.IS_ISO_COMPLIANT);

    /**
     * The value is set by the {@link IndexedElementsWriter} when the metadata is stored.<br>
     * See {@link ResourcePropertyHandler#setMetadataQuality()}
     */
    public static final IndexedResourceProperty MEDATADATA_QUALITY = new IndexedResourceProperty(ResourceProperty.METADATA_QUALITY);

    /**
     * See {@link ResourcePropertyHandler#setEssentialVarsQuality(int)()}
     * Set by .... ?
     */
    public static final IndexedResourceProperty ESSENTIAL_VARS_QUALITY = new IndexedResourceProperty(
	    ResourceProperty.ESSENTIAL_VARS_QUALITY);

    /**
     * Set by the Service Status Checker Augmenter
     */
    public static final IndexedResourceProperty SSC_SCORE = new IndexedResourceProperty(ResourceProperty.SSC_SCORE);

    /**
     * Set by the AccessQualifier
     */
    public static final IndexedResourceProperty ACCESS_QUALITY = new IndexedResourceProperty(ResourceProperty.ACCESS_QUALITY);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty TEST_TIME_STAMP = new IndexedResourceProperty(ResourceProperty.TEST_TIME_STAMP);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty COMPLIANCE_LEVEL = new IndexedResourceProperty(ResourceProperty.COMPLIANCE_LEVEL);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty LAST_SUCCEEDED_TEST = new IndexedResourceProperty(ResourceProperty.SUCCEEDED_TEST);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_TRANSFORMABLE = new IndexedResourceProperty(ResourceProperty.IS_TRANSFORMABLE);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_DOWNLOADABLE = new IndexedResourceProperty(ResourceProperty.IS_DOWNLOADABLE);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty DOWNLOAD_TIME = new IndexedResourceProperty(ResourceProperty.DOWNLOAD_TIME);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_EXECUTABLE = new IndexedResourceProperty(ResourceProperty.IS_EXECUTABLE);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty EXECUTION_TIME = new IndexedResourceProperty(ResourceProperty.EXECUTION_TIME);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_GRID = new IndexedResourceProperty(ResourceProperty.IS_GRID);
        
    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_VECTOR = new IndexedResourceProperty(ResourceProperty.IS_VECTOR);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_TIMESERIES = new IndexedResourceProperty(ResourceProperty.IS_TIMESERIES);

    /**
     * Set by the AccessAugmenter
     */
    public static final IndexedResourceProperty IS_RATING_CURVE = new IndexedResourceProperty(ResourceProperty.IS_RATING_CURVE);

    /**
     * Set once using a tool
     */
    public static final IndexedResourceProperty IS_EIFFEL_RECORD = new IndexedResourceProperty(ResourceProperty.IS_EIFFEL_RECORD);

    private static List<IndexedResourceProperty> getIndexes() {

	return getIndexes(IndexedResourceElements.class).//
		stream().//
		map(index -> (IndexedResourceProperty) index).//
		collect(Collectors.toList());
    }

    static {
	// --------------------------------------------------------------------------------------------------------------------------
	// defined only for MarkLogic
	//
	for (IndexedResourceProperty index : getIndexes()) {

	    switch (index.getResourceProperty().getContentType()) {
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
