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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;

/**
 * Utility class to read/write extended metadata
 * 
 * @see ExtendedMetadata
 * @author Fabrizio
 */
public class ExtensionHandler implements PropertiesAdapter<ExtensionHandler> {

    private static final String MAG_LEVEL = "magLevel";
    private static final String ORIGINATOR_ORGANISATION_IDENTIFIER = "OriginatorOrganisationIdentifier";
    private static final String ORIGINATOR_ORGANISATION_DESCRIPTION = "OriginatorOrganisationDescription";
    private static final String FEDEO_SECOND_LEVEL_TEMPLATE = "fedeoSecondLevel";
    private static final String STAC_SECOND_LEVEL_TEMPLATE = "STACSecondLevel";
    private static final String NC_FILE_CORRUPTED = "ncFileCorrupted";
    private static final String AVAILABLE_GRANULES = "availableGranules";
    private static final String THEME_CATEGORY = "themeCategory";
    private static final String IN_SITU = "inSitu";

    private ExtendedMetadata metadata;

    public ExtendedMetadata getMetadata() {
	return metadata;
    }

    /**
     * @param harmonizedMetadata
     */
    ExtensionHandler(HarmonizedMetadata harmonizedMetadata) {

	this.metadata = harmonizedMetadata.getExtendedMetadata();
    }

    /**
     * @param resource
     */
    ExtensionHandler(GSResource resource) {

	this(resource.getHarmonizedMetadata());
    }

    /**
     * @return
     */
    public Boolean isNCFileCorrupted() {

	try {

	    String textContent = this.metadata.getTextContent(NC_FILE_CORRUPTED);

	    if (textContent == null) {

		return false;
	    }

	    return textContent.equals("true") ? true : false;

	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }

    /**
     * @param info
     */
    public void setIsNCFileCorrupted() {
	try {
	    this.metadata.add(NC_FILE_CORRUPTED, "true");
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getFEDEOSecondLevelInfo() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(FEDEO_SECOND_LEVEL_TEMPLATE));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param info
     */
    public void setFEDEOSecondLevelInfo(String info) {
	try {
	    this.metadata.add(FEDEO_SECOND_LEVEL_TEMPLATE, info);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
    
    /**
     * @return
     */
    public Optional<String> getSTACSecondLevelInfo() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(STAC_SECOND_LEVEL_TEMPLATE));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param info
     */
    public void setSTACSecondLevelInfo(String info) {
	try {
	    this.metadata.add(STAC_SECOND_LEVEL_TEMPLATE, info);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getAvailableGranules() {

	try {
	    if (this.metadata.getTextContent(AVAILABLE_GRANULES) == null) {
		return Optional.empty();
	    }
	    return Optional.of(this.metadata.getTextContent(AVAILABLE_GRANULES));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param info
     */
    public void setAvailableGranules(String condition) {
	try {
	    this.metadata.add(AVAILABLE_GRANULES, condition);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param id
     */
    public void setUniqueInstrumentIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER_EL_NAME, id);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public Optional<String> getBNHSInfo() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.BNHS_INFO_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setBNHSInfo(String info) {
	try {
	    this.metadata.add(MetadataElement.BNHS_INFO_EL_NAME, info);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public Optional<String> getCountry() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.COUNTRY_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setCountry(String country) {
	try {
	    this.metadata.add(MetadataElement.COUNTRY_EL_NAME, country);
	    Country c = Country.decode(country);
	    if (c != null && !getCountryISO3().isPresent()) {
		setCountryISO3(c.getISO3());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public Optional<String> getCountryISO3() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.COUNTRY_ISO3_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setCountryISO3(String country) {
	try {
	    this.metadata.add(MetadataElement.COUNTRY_ISO3_EL_NAME, country);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniqueInstrumentIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * The size of the dataset (it is the multiplication of the sizes along each dimension, e.g. d_s = d1_s * d2_s * ...
     * * dn_s
     * 
     * @param size
     */
    public void setDataSize(Long size) {
	try {
	    this.metadata.add(MetadataElement.DATA_SIZE_EL_NAME, "" + size);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return The size of the dataset (it is the multiplication of the sizes along each dimension, e.g. d_s = d1_s *
     *         d2_s * ... * dn_s
     */
    public Optional<Long> getDataSize() {

	try {
	    String str = this.metadata.getTextContent(MetadataElement.DATA_SIZE_EL_NAME);
	    if (str == null || str.isEmpty()) {
		return Optional.empty();
	    }

	    long size = Long.parseLong(str);
	    return Optional.of(size);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param id
     */
    public void setUniquePlatformIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER_EL_NAME, id);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniquePlatformIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param id
     */
    public void setUniqueAttributeIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER_EL_NAME, id);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniqueAttributeIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER_EL_NAME));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param scene
     */
    public void setSatelliteScene(SatelliteScene scene) {

	try {
	    this.metadata.remove("//*:satelliteScene");
	    this.metadata.add(scene.asDocument(true).getDocumentElement());
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<SatelliteScene> getSatelliteScene() {

	try {
	    List<Node> list = this.metadata.get("//*:satelliteScene");
	    if (!list.isEmpty()) {

		Node node = list.get(0);
		return Optional.of(SatelliteScene.create(node));
	    }
	} catch (XPathExpressionException | JAXBException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }
    
    /**
     * @param scene
     */
    public void setWorldCereal(WorldCerealMap map) {

	try {
	    this.metadata.remove("//*:worldCerealMap");
	    this.metadata.add(map.asDocument(true).getDocumentElement());
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<WorldCerealMap> getWorldCereal() {

	try {
	    List<Node> list = this.metadata.get("//*:worldCerealMap");
	    if (!list.isEmpty()) {

		Node node = list.get(0);
		return Optional.of(WorldCerealMap.create(node));
	    }
	} catch (XPathExpressionException | JAXBException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }
    

    /**
     * @return
     */
    public Optional<String> getCropTypes() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.CROP_TYPES_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setCropTypes(String cropTypes) {
	try {
	    this.metadata.add(MetadataElement.CROP_TYPES_EL_NAME, cropTypes);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }
    
    
    /**
     * @return
     */
    public Optional<String> getMagnitudeLevel() {
	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MAG_LEVEL));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param magLevel
     */
    public void setMagnitudeLevel(String magLevel) {
	try {
	    this.metadata.add(MAG_LEVEL, magLevel);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<InterpolationType> getTimeInterpolation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_INTERPOLATION_EL_NAME);
	    InterpolationType interpolation = InterpolationType.decode(str);
	    return Optional.ofNullable(interpolation);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }
    public Optional<String> getTimeInterpolationString() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_INTERPOLATION_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }
    

    public void setTimeInterpolation(InterpolationType interpolationType) {
	if (interpolationType != null) {
	    setTimeInterpolation(interpolationType.name());
	}

    }

    public void setTimeInterpolation(String interpolationType) {
	if (interpolationType == null) {
	    return;
	}
	try {
	    this.metadata.add(MetadataElement.TIME_INTERPOLATION_EL_NAME, interpolationType);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeSupport() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_SUPPORT_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeSupport(String timeSupport) {
	try {
	    this.metadata.add(MetadataElement.TIME_SUPPORT_EL_NAME, timeSupport);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeResolution() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_RESOLUTION_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeResolution(String timeResolution) {
	try {
	    this.metadata.add(MetadataElement.TIME_RESOLUTION_EL_NAME, timeResolution);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeUnits() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_UNITS_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeUnits(String timeUnits) {
	try {
	    this.metadata.add(MetadataElement.TIME_UNITS_EL_NAME, timeUnits);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }
    
    /**
     * @return
     */
    public Optional<String> getTimeResolutionDuration8601() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_RESOLUTION_DURATION_8601_EL_NAME);
	    Optional<String> ret = Optional.ofNullable(str);
	    if (ret.isPresent()) {
		return ret;
	    }
	    Optional<String> timeUnits = getTimeUnits();
	    Optional<String> timeResolution = getTimeResolution();

	    if (timeUnits.isPresent() && timeResolution.isPresent()) {
		String units = timeUnits.get();
		BigDecimal value = new BigDecimal(timeResolution.get());
		Duration res = ISO8601DateTimeUtils.getDuration(value, units);
		if (res != null) {
		    return Optional.ofNullable(res.toString());
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("Unknown time resolution: {} {}", value, units);
		}
	    }
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeResolutionDuration8601(String resolution) {
	try {
	    this.metadata.add(MetadataElement.TIME_RESOLUTION_DURATION_8601_EL_NAME, resolution);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }
    
    /**
     * @return
     */
    public Optional<String> getTimeAggregationDuration8601() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_AGGREGATION_DURATION_8601_EL_NAME);
	    Optional<String> ret = Optional.ofNullable(str);
	    if (ret.isPresent()) {
		return ret;
	    }
	    Optional<String> timeUnits = getTimeUnits();
	    Optional<String> timeSupport = getTimeSupport();

	    if (timeUnits.isPresent() && timeSupport.isPresent()) {
		String units = timeUnits.get();
		BigDecimal value = new BigDecimal(timeSupport.get());
		Duration duration = ISO8601DateTimeUtils.getDuration(value, units);
		if (duration != null) {
		    return Optional.ofNullable(duration.toString());
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("Unknown time aggregation: {} {}", value, units);
		}
	    }
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeAggregationDuration8601(String timeAggregation) {
	try {
	    this.metadata.add(MetadataElement.TIME_AGGREGATION_DURATION_8601_EL_NAME, timeAggregation);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeUnitsAbbreviation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_UNITS_ABBREVIATION_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setTimeUnitsAbbreviation(String timeUnitsAbbreviation) {
	try {
	    this.metadata.add(MetadataElement.TIME_UNITS_ABBREVIATION_EL_NAME, timeUnitsAbbreviation);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getAttributeUnitsURI() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS_URI_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    public void setAttributeUnitsURI(String attributeURI) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS_URI_EL_NAME, attributeURI);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    public void setObservedPropertyURI(String attributeUnitsURI) {
	try {
	    this.metadata.add(MetadataElement.OBSERVED_PROPERTY_URI_EL_NAME, attributeUnitsURI);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getObservedPropertyURI() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.OBSERVED_PROPERTY_URI_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }
    
    public void setWISTopicHierarchy(String topic) {
   	try {
   	    this.metadata.add(MetadataElement.WIS_TOPIC_HIERARCHY_EL_NAME, topic);
   	} catch (Exception e) {
   	    e.printStackTrace();
   	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
   	}

       }

       /**
        * @return
        */
       public Optional<String> getWISTopicHierarchy() {
   	try {
   	    String str = this.metadata.getTextContent(MetadataElement.WIS_TOPIC_HIERARCHY_EL_NAME);
   	    return Optional.ofNullable(str);
   	} catch (XPathExpressionException e) {
   	    e.printStackTrace();
   	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
   	}
   	return Optional.empty();
       }

    /**
     * @return
     */
    public Optional<String> getAttributeUnits() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setAttributeUnits(String attributeUnits) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS_EL_NAME, attributeUnits);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getAttributeUnitsAbbreviation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setAttributeUnitsAbbreviation(String attributeUnitsAbbreviation) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION_EL_NAME, attributeUnitsAbbreviation);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getAttributeMissingValue() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_MISSING_VALUE_EL_NAME);
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setAttributeMissingValue(String attributeMissingValue) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_MISSING_VALUE_EL_NAME, attributeMissingValue);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public List<String> getOriginatorOrganisationIdentifiers() {
	try {
	    return this.metadata.getTextContents(ORIGINATOR_ORGANISATION_IDENTIFIER);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    /**
     * @param originatorOrganisationIdentifier
     */
    public void addOriginatorOrganisationIdentifier(String originatorOrganisationIdentifier) {
	try {
	    this.metadata.add(ORIGINATOR_ORGANISATION_IDENTIFIER, originatorOrganisationIdentifier);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public List<String> getOriginatorOrganisationDescriptions() {
	try {
	    return this.metadata.getTextContents(ORIGINATOR_ORGANISATION_DESCRIPTION);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    /**
     * @param originatorOrganisationDescription
     */
    public void addOriginatorOrganisationDescription(String originatorOrganisationDescription) {
	try {
	    this.metadata.add(ORIGINATOR_ORGANISATION_DESCRIPTION, originatorOrganisationDescription);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * 
     */
    public void clearOriginatorOrganisationDescriptions() {

	try {
	    this.metadata.remove("//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":" + ORIGINATOR_ORGANISATION_DESCRIPTION);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * 
     */
    public void clearOriginatorOrganisationIdentifiers() {

	try {
	    this.metadata.remove("//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":" + ORIGINATOR_ORGANISATION_IDENTIFIER);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public void adapt(ExtensionHandler targetHandler, AdaptPolicy policy, String... properties) {

	switch (policy) {
	case ADD:

	    getOriginatorOrganisationDescriptions().forEach(d -> targetHandler.addOriginatorOrganisationDescription(d));
	    getOriginatorOrganisationIdentifiers().forEach(d -> targetHandler.addOriginatorOrganisationIdentifier(d));

	    break;

	case ON_EMPTY:

	    if (targetHandler.getOriginatorOrganisationDescriptions().isEmpty()) {

		getOriginatorOrganisationDescriptions().forEach(d -> targetHandler.addOriginatorOrganisationDescription(d));
	    }

	    if (targetHandler.getOriginatorOrganisationIdentifiers().isEmpty()) {

		getOriginatorOrganisationIdentifiers().forEach(d -> targetHandler.addOriginatorOrganisationIdentifier(d));
	    }

	    break;
	case OVERRIDE:

	    if (!getOriginatorOrganisationDescriptions().isEmpty()) {
		clearOriginatorOrganisationDescriptions();
		getOriginatorOrganisationDescriptions().forEach(d -> targetHandler.addOriginatorOrganisationDescription(d));
	    }

	    if (!getOriginatorOrganisationIdentifiers().isEmpty()) {
		clearOriginatorOrganisationIdentifiers();
		getOriginatorOrganisationIdentifiers().forEach(d -> targetHandler.addOriginatorOrganisationIdentifier(d));
	    }

	    break;
	}

	if (!this.getOriginatorOrganisationDescriptions().isEmpty() && //
		targetHandler.getOriginatorOrganisationDescriptions().isEmpty()) {

	    this.getOriginatorOrganisationDescriptions().forEach(d -> targetHandler.addOriginatorOrganisationDescription(d));
	}

	if (!this.getOriginatorOrganisationIdentifiers().isEmpty() && //
		targetHandler.getOriginatorOrganisationIdentifiers().isEmpty()) {

	    this.getOriginatorOrganisationIdentifiers().forEach(d -> targetHandler.addOriginatorOrganisationIdentifier(d));
	}
    }

    public Optional<String> getBNHSProperty(BNHSProperty property) {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(property.getElement().getName()));
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    public void setBNHSProperty(BNHSProperty property, String value) {
	try {
	    this.metadata.add(property.getElement().getName(), value);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param themeCategory
     */
    public void addThemeCategory(String themeCategory) {
	try {
	    this.metadata.add(THEME_CATEGORY, themeCategory);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	
    }
    
    /**
     * @return
     */
    public Optional<String> getThemeCategory() {
	try {
	    String str = this.metadata.getTextContent("themeCategory");
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }
    
    /**
     * 
     */
    public void clearThemeCategory() {

	try {
	    this.metadata.remove("//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":" + THEME_CATEGORY);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
    
    /**
     * @param themeCategory
     */
    public void setIsInSitu() {
	try {
	    this.metadata.add(IN_SITU, "true");
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	
    }
    
    /**
     * @return
     */
    public boolean isInSitu() {
	try {

	    String textContent = this.metadata.getTextContent(IN_SITU);

	    if (textContent == null) {

		return false;
	    }

	    return textContent.equals("true") ? true : false;

	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }
    
    
    
    


}
