package eu.essi_lab.model.resource;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.MetadataQualifier;

/**
 * An utility class to read/write not mandatory {@link ResourceProperty}s
 * 
 * @author Fabrizio
 */
public class ResourcePropertyHandler {

    private GSResource resource;

    /**
     * @param resource
     */
    ResourcePropertyHandler(GSResource resource) {

	this.resource = resource;
    }

    /**
     * @param recoveryRemovalToken
     */
    public void setRecoveryRemovalToken(String recoveryRemovalToken) {

	resource.setProperty(ResourceProperty.RECOVERY_REMOVAL_TOKEN, recoveryRemovalToken);
    }

    @XmlTransient
    public Optional<String> getRecoveryRemovalToken() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.RECOVERY_REMOVAL_TOKEN);

	if (value.isPresent() && value.get().isEmpty()) {

	    return Optional.empty();
	}

	return value;
    }

    /**
     * Set the {@link ResourceProperty#RESOURCE_TIME_STAMP} property with the current date-time in ISO8601 format
     */
    public void setResourceTimeStamp() {

	setResourceTimeStamp(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
    }

    public void setLastDownloadDate() {
    	resource.setProperty(ResourceProperty.LAST_DOWNLOAD_DATE, ISO8601DateTimeUtils.getISO8601DateTime());
    }
    
    public Optional<String> getLastDownloadDate() {
    	return resource.getPropertyValue(ResourceProperty.LAST_DOWNLOAD_DATE);
    }
    
    public void setLastFailedDownloadDate() {
    	resource.setProperty(ResourceProperty.LAST_FAILED_DOWNLOAD_DATE, ISO8601DateTimeUtils.getISO8601DateTime());
    }
    
    public Optional<String> getLastFailedDownloadDate() {
    	return resource.getPropertyValue(ResourceProperty.LAST_FAILED_DOWNLOAD_DATE);
    }
    
    
    /**
     * Set the {@link ResourceProperty#RESOURCE_TIME_STAMP} property with the supplied date-time in ISO8601 format
     */
    public void setResourceTimeStamp(String timeStamp) {

	resource.setProperty(ResourceProperty.RESOURCE_TIME_STAMP, timeStamp);
    }

    /**
     * Get the {@link ResourceProperty#RESOURCE_TIME_STAMP} property
     */
    public Optional<String> getResourceTimeStamp() {

	return resource.getPropertyValue(ResourceProperty.RESOURCE_TIME_STAMP);
    }

    /**
     * Set the quality of the metadata
     * 
     * @param quality an integer >= 0
     */
    public void setMetadataQuality(int quality) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.METADATA_QUALITY, String.valueOf(quality));
    }

    /**
     * Set the quality of the metadata using an instance of {@link MetadataQualifier}
     */
    public void setMetadataQuality() {

	if (hasLowestRanking()) {

	    return;
	}

	MetadataQualifier qualifier = new MetadataQualifier(resource.getHarmonizedMetadata());
	int quality = qualifier.getQuality();

	setMetadataQuality(quality);
    }

    /**
     * Get the metadata quality of the resource
     */
    public Optional<Integer> getMetadataQuality() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.METADATA_QUALITY);
	if (value.isPresent()) {

	    return Optional.of(Integer.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Set the access quality of the resource
     * 
     * @param quality an integer >= 0
     */
    public void setAccessQuality(int quality) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.ACCESS_QUALITY, String.valueOf(quality));
    }

    /**
     * Get the access quality of the resource
     */
    public Optional<Integer> getAccessQuality() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.ACCESS_QUALITY);
	if (value.isPresent()) {

	    return Optional.of(Integer.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * @param score
     */
    public void setSSCSCore(int score) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.SSC_SCORE, String.valueOf(score));
    }

    /**
     * @return
     */
    public Optional<Integer> getSSCScore() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.SSC_SCORE);
	if (value.isPresent()) {

	    return Optional.of(Integer.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Set the essential vars quality of the resource
     * 
     * @param quality an integer >= 0
     */
    public void setEssentialVarsQuality(int quality) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.ESSENTIAL_VARS_QUALITY, String.valueOf(quality));
    }

    /**
     * Get the essential vars quality of the resource
     */
    public Optional<Integer> getEssentialVarsQuality() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.ESSENTIAL_VARS_QUALITY);
	if (value.isPresent()) {

	    return Optional.of(Integer.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Set the {@link ResourceProperty#IS_DELETED} property. If set is <code>true</code> an indexed
     * element <isDeleted>true</isDeleted> is added, otherwise the element (if already exists) is removed
     * (instead of adding an element <isDeleted>false</isDeleted>) since the latter case is the default one
     * 
     * @param set
     */
    public void setIsDeleted(boolean set) {

	if (!set) {
	    resource.getIndexesMetadata().remove(ResourceProperty.IS_DELETED.getName());
	} else {
	    resource.setProperty(ResourceProperty.IS_DELETED, String.valueOf(set));
	}
    }

    /**
     * Get the {@link ResourceProperty#IS_DELETED} property
     */
    public boolean isDeleted() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_DELETED).orElse("false"));
    }

    /**
     * Set the {@link ResourceProperty#IS_VALIDATED} property. If set is <code>true</code> an indexed
     * element <isValidated>true</isValidated> is added, otherwise the element (if already exists) is removed
     * (instead of adding an element <isValidated>false</isValidated>) since the latter case is the default one
     * 
     * @param set
     */
    public void setIsValidated(boolean set) {

	if (!set) {
	    resource.getIndexesMetadata().remove(ResourceProperty.IS_VALIDATED.getName());
	} else {
	    resource.setProperty(ResourceProperty.IS_VALIDATED, String.valueOf(set));
	}
    }

    /**
     * Get the {@link ResourceProperty#IS_VALIDATED} property
     */
    public boolean isValidated() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_VALIDATED).orElse("false"));
    }

    /**
     * Set the {@link ResourceProperty#IS_GEOSS_DATA_CORE} property
     * 
     * @param set
     */
    public void setIsGDC(boolean set) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.IS_GEOSS_DATA_CORE, String.valueOf(set));

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	if (dataIdentification != null) {

	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation("geossdatacore");
	    dataIdentification.addLegalConstraints(legalConstraints);
	}
    }

    /**
     * Get the {@link ResourceProperty#IS_GEOSS_DATA_CORE} property
     */
    public boolean isGDC() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_GEOSS_DATA_CORE).orElse("false"));
    }

    /**
     * Set the {@link ResourceProperty#IS_ISO_COMPLIANT} property
     * 
     * @param set
     */
    public void setIsISOCompliant(boolean set) {

	resource.setProperty(ResourceProperty.IS_ISO_COMPLIANT, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_ISO_COMPLIANT} property
     */
    public Optional<Boolean> isISOCompliant() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.IS_ISO_COMPLIANT);
	if (value.isPresent()) {

	    return Optional.of(Boolean.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Get the {@link ResourceProperty#OAI_PMH_HEADER_ID} property
     */
    public Optional<String> getOAIPMHHeaderIdentifier() {

	return resource.getPropertyValue(ResourceProperty.OAI_PMH_HEADER_ID);
    }

    /**
     * Set the {@link ResourceProperty#OAI_PMH_HEADER_ID} property
     * 
     * @param identifier
     */
    public void setOAIPMHHeaderIdentifier(String identifier) {

	resource.setProperty(ResourceProperty.OAI_PMH_HEADER_ID, String.valueOf(identifier));
    }

    // ----------------------------------------
    //
    // ACCESS PROPERTIES SET BY AccessAugmenter
    //
    // ----------------------------------------

    /**
     * Set the {@link ResourceProperty#TEST_TIME_STAMP} property in date-time ISO8601 format
     * 
     * @param timeStamp
     */
    public void setTestTimeStamp(String timeStamp) {

	resource.setProperty(ResourceProperty.TEST_TIME_STAMP, timeStamp);
    }

    /**
     * Get the {@link ResourceProperty#TEST_TIME_STAMP} property
     */
    public Optional<String> getTestTimeStamp() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.TEST_TIME_STAMP);
	if (value.isPresent()) {

	    return Optional.of(value.get());
	}

	return Optional.empty();
    }

    /**
     * Set the {@link ResourceProperty#SUCCEEDED_TEST} property
     * 
     * @param complianceLevel
     */
    public void setSucceededTest(String complianceLevel) {

	resource.setProperty(ResourceProperty.SUCCEEDED_TEST, complianceLevel);
    }

    /**
     * Get the {@link ResourceProperty#SUCCEEDED_TEST} property
     */
    public Optional<String> getSucceededTest() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.SUCCEEDED_TEST);
	if (value.isPresent()) {

	    return Optional.of(value.get());
	}

	return Optional.empty();
    }

    /**
     * Set the {@link ResourceProperty#COMPLIANCE_LEVEL} property
     * 
     * @param complianceLevel
     */
    public void addComplianceLevel(String complianceLevel) {

	resource.addProperty(ResourceProperty.COMPLIANCE_LEVEL, complianceLevel);
    }

    /**
     * Get the {@link ResourceProperty#COMPLIANCE_LEVEL} property
     */
    public List<String> getComplianceLevelList() {

	return resource.getPropertyValues(ResourceProperty.COMPLIANCE_LEVEL);
    }

    /**
     * Set the {@link ResourceProperty#IS_TRANSFORMABLE} property
     * 
     * @param set
     */
    public void setIsTransformable(boolean set) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.IS_TRANSFORMABLE, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_TRANSFORMABLE} property
     */
    public Optional<Boolean> isTransformable() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.IS_TRANSFORMABLE);
	if (value.isPresent()) {

	    return Optional.of(Boolean.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Set the {@link ResourceProperty#IS_DOWNLOADABLE} property
     * 
     * @param set
     */
    public void setIsDownloadable(boolean set) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.IS_DOWNLOADABLE, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_DOWNLOADABLE} property
     */
    public Optional<Boolean> isDownloadable() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.IS_DOWNLOADABLE);
	if (value.isPresent()) {

	    return Optional.of(Boolean.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Set the {@link ResourceProperty#IS_EXECUTABLE} property
     * 
     * @param set
     */
    public void setIsExecutable(boolean set) {

	if (hasLowestRanking()) {

	    return;
	}

	resource.setProperty(ResourceProperty.IS_EXECUTABLE, String.valueOf(set));
    }

    /**
     * Set the {@link ResourceProperty#IS_GRID} property
     * 
     * @param set
     */
    public void setIsGrid(boolean set) {

	resource.setProperty(ResourceProperty.IS_GRID, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_GRID} property
     */
    public boolean isGrid() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_GRID).orElse("false"));
    }

    /**
     * Set the {@link ResourceProperty#IS_TIMESERIES} property
     * 
     * @param set
     */
    public void setIsTimeseries(boolean set) {

	resource.setProperty(ResourceProperty.IS_TIMESERIES, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_TIMESERIES} property
     */
    public boolean isTimeSeries() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_TIMESERIES).orElse("false"));
    }
    
    /**
     * Set the {@link ResourceProperty#IS_VECTOR} property
     * 
     * @param set
     */
    public void setIsVector(boolean set) {

	resource.setProperty(ResourceProperty.IS_VECTOR, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_VECTOR} property
     */
    public boolean isVector() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_VECTOR).orElse("false"));
    }

    /**
     * Get the {@link ResourceProperty#IS_EIFFEL_RECORD} property
     */
    public boolean isEiffelRecord() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_EIFFEL_RECORD).orElse("false"));
    }

    /**
     * Set the {@link ResourceProperty#IS_TRAJECTORY} property
     * 
     * @param set
     */
    public void setIsTrajectory(boolean set) {

	resource.setProperty(ResourceProperty.IS_TRAJECTORY, String.valueOf(set));
    }

    /**
     * Get the {@link ResourceProperty#IS_TRAJECTORY} property
     */
    public boolean isTrajectory() {

	return Boolean.valueOf(resource.getPropertyValue(ResourceProperty.IS_TRAJECTORY).orElse("false"));
    }

    /**
     * Get the {@link ResourceProperty#IS_EXECUTABLE} property
     */
    public Optional<Boolean> isExecutable() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.IS_EXECUTABLE);
	if (value.isPresent()) {

	    return Optional.of(Boolean.valueOf(value.get()));
	}

	return Optional.empty();
    }

    /**
     * Add a {@link ResourceProperty#DOWNLOAD_TIME} property value
     * 
     * @param downloadTime
     */
    public void addDownloadTime(long downloadTime) {

	resource.addProperty(ResourceProperty.DOWNLOAD_TIME, String.valueOf(downloadTime));
    }

    /**
     * Get the {@link ResourceProperty#DOWNLOAD_TIME} property values
     */
    public List<Long> getDownloadTimeList() {

	return resource.getPropertyValues(ResourceProperty.DOWNLOAD_TIME).//
		stream().//
		map(t -> Long.valueOf(t)).//
		collect(Collectors.toList());
    }

    /**
     * Adds a {@link ResourceProperty#EXECUTION_TIME} property value
     * 
     * @param execTime
     */
    public void addExecutionTime(long execTime) {

	resource.addProperty(ResourceProperty.EXECUTION_TIME, String.valueOf(execTime));
    }

    /**
     * Get the {@link ResourceProperty#EXECUTION_TIME} property values
     */
    public List<Long> getExecutionTimeList() {

	return resource.getPropertyValues(ResourceProperty.EXECUTION_TIME).//
		stream().//
		map(t -> Long.valueOf(t)).//
		collect(Collectors.toList());
    }

    /**
     * 
     */
    public void setLowestRanking() {

	resource.addProperty(ResourceProperty.HAS_LOWEST_RANKING, "true");
    }

    /**
     * @return
     */
    public boolean hasLowestRanking() {

	Optional<String> value = resource.getPropertyValue(ResourceProperty.HAS_LOWEST_RANKING);
	return value.isPresent();
    }
}
