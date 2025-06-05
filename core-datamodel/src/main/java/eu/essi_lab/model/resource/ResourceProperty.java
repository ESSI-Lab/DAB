package eu.essi_lab.model.resource;

import java.util.Arrays;
import java.util.List;

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

import java.util.NoSuchElementException;
import java.util.Optional;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.IndexedResourceProperty;

/**
 * Enumeration of non-harmonized, queryable {@link GSResource} properties. Some properties are mandatory, and they must
 * be set before to store the {@link GSResource} in a Database
 * 
 * @see GSResource#validate()
 * @see IndexedResourceProperty
 * @see MetadataElement
 * @author Fabrizio
 */
public enum ResourceProperty implements Queryable {
    /**
     * Mandatory property
     */
    ORIGINAL_ID("originalId"),

    /**
     * Mandatory property
     */
    PRIVATE_ID("privateId", false),

    /**
     * Mandatory property.<br>
     * This property has no related {@link IndexedResourceProperty} since the value is the same of the
     * {@link MetadataElement#IDENTIFIER} property which has its own {@link IndexedMetadataElement}
     */
    PUBLIC_ID("publicId"),

    /**
     * 
     */
    TYPE("resourceType"),

    /**
     * 
     */
    COLLECTION_TYPE("collectionType"),

    /**
     * 
     */
    IS_DELETED("isDeleted", ContentType.BOOLEAN),

    /**
     * 
     */
    IS_VALIDATED("isValidated", ContentType.BOOLEAN),

    /**
     * 
     */
    IS_GEOSS_DATA_CORE("isGDC", ContentType.BOOLEAN),

    /**
     * 
     */
    IS_ISO_COMPLIANT("isISOCompliant", ContentType.BOOLEAN),

    /**
     * 
     */
    OAI_PMH_HEADER_ID("oaiHeaderId"),

    /**
     * 
     */
    RESOURCE_TIME_STAMP("resourceTimeStamp", ContentType.ISO8601_DATE_TIME),

    /**
     * 
     */
    SOURCE_ID("sourceId"),

    /**
     * 
     */
    SOURCE_DEPLOYMENT("sourceDeployment"),

    /**
     * 
     */
    METADATA_QUALITY("metadataQuality", ContentType.INTEGER),

    /**
     *  
     */
    ESSENTIAL_VARS_QUALITY("essentialVarsQuality", ContentType.INTEGER),

    /**
     * Service Status Checker Score
     */
    SSC_SCORE(ResourceProperty.SSC_SCORE_EL_NAME, ContentType.INTEGER),

    /**
     * 
     */
    RECOVERY_REMOVAL_TOKEN("recoveryRemovalToken"),

    // ----------------------------
    //
    // Access properties
    //

    /**
     * 
     */
    ACCESS_QUALITY("accessQuality", ContentType.INTEGER),

    /**
     * 
     */
    TEST_TIME_STAMP("testTimeStamp", ContentType.ISO8601_DATE_TIME),

    /**
     * 
     */
    COMPLIANCE_LEVEL("complianceLevel", ContentType.TEXTUAL),

    /**
     * 
     */
    SUCCEEDED_TEST("succeededTest", ContentType.TEXTUAL),

    /**
     * BASIC TEST
     */
    IS_TRANSFORMABLE("isTransformable", ContentType.BOOLEAN),

    /**
     * DOWNLOAD TEST + VALIDATION TEST (true if both tests succeeded)
     */
    IS_DOWNLOADABLE("isDownloadable", ContentType.BOOLEAN),

    /**
     * DOWNLOAD TEST
     */
    DOWNLOAD_TIME("testDownloadTime", ContentType.LONG),

    /**
     * DOWNLOAD DATE
     */
    LAST_DOWNLOAD_DATE("lastDownloadDate", ContentType.ISO8601_DATE_TIME),

    /**
     * FAILED DOWNLOAD DATE
     */
    LAST_FAILED_DOWNLOAD_DATE("lastFailedDownloadDate", ContentType.ISO8601_DATE_TIME),

    /**
     * EXECUTION TEST
     */
    IS_EXECUTABLE("isExecutable", ContentType.BOOLEAN),

    /**
     * IS GRID
     */
    IS_GRID("isGrid", ContentType.BOOLEAN),

    /**
     * IS TIMESERIES
     */
    IS_TIMESERIES("isTimeseries", ContentType.BOOLEAN),

    /**
     * IS GRID
     */
    IS_VECTOR("isVector", ContentType.BOOLEAN),

    /**
     * IS TRAJECTORY
     */
    IS_TRAJECTORY("isTrajectory", ContentType.BOOLEAN),

    /**
     * IS EIFFEL RECORD
     */
    IS_EIFFEL_RECORD("eiffelRecord", ContentType.BOOLEAN),

    /**
     * EXECUTION TEST
     */
    EXECUTION_TIME("testExecutionTime", ContentType.LONG),

    /**
     * HAS_LOWEST_RANKING
     */
    HAS_LOWEST_RANKING("hasLowestRanging", ContentType.BOOLEAN);

    /**
     * 
     */
    public static final String SOURCE_ID_NAME = "sourceId";
    /**
     * 
     */
    public static final String SSC_SCORE_EL_NAME = "sscScore";

    private String name;
    private boolean isEnabled;
    private ContentType type;

    private ResourceProperty(String name) {

	this(name, ContentType.TEXTUAL, true);
    }

    private ResourceProperty(String name, boolean enabled) {

	this(name, ContentType.TEXTUAL, enabled);
    }

    private ResourceProperty(String name, ContentType type) {

	this(name, type, true);
    }

    private ResourceProperty(String name, ContentType type, boolean enabled) {

	this.name = name;
	this.type = type;
	this.isEnabled = enabled;
    }

    public String getName() {

	return name;
    }

    @Override
    public ContentType getContentType() {

	return type;
    }

    public boolean isVolatile() {

	return false;
    }

    @Override
    public String toString() {

	return getName();
    }

    @Override
    public boolean isEnabled() {

	return this.isEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {

	this.isEnabled = enabled;
    }

    /**
     * @param name
     * @return
     * @throws NoSuchElementException
     */
    public static ResourceProperty fromName(String name) throws IllegalArgumentException {

	return (ResourceProperty) Queryable.fromName(name, values());
    }

    /**
     * @param name
     * @return
     */
    public static Optional<ResourceProperty> optFromName(String name) {

	try {
	    return Optional.of(fromName(name));

	} catch (IllegalArgumentException ex) {
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public static List<ResourceProperty> listValues() {

	return Arrays.asList(values());
    }

    /**
     * @return
     */
    public static List<Queryable> listQueryables() {

	return Arrays.asList(values());
    }

}
