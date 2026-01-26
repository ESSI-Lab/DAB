package eu.essi_lab.model.resource;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.composed.ComposedElement;
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
    private static final String DATA_DISCLAIMER = "data_disclaimer";
    private static final String IN_SITU = "inSitu";
    private static final String GEOMETRY = "geometry";
    private static final String CENTROID = "centroid";
    private static final String AREA = "area";

    private ExtendedMetadata metadata;

    /**
     * @return
     */
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
     * @param element
     */
    public void addComposedElement(ComposedElement element) {

	try {
	    this.metadata.add(element.asDocument(true).getDocumentElement());

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param name
     * @return
     */
    public List<ComposedElement> getComposedElements(String name) {

	try {
	    List<Node> list = this.metadata.get("//*:composedElement[*:name='" + name + "']");

	    return list.stream().map(node -> {
		try {
		    return ComposedElement.of(node);
		} catch (JAXBException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		    return null;
		}
	    }).filter(Objects::nonNull).//

		    collect(Collectors.toList());

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    // -------------------------------------------------
    //
    // Queryables properties
    //
    // -------------------------------------------------

    /**
     * @return
     */
    public Optional<String> getBNHSInfo() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.BNHS_INFO.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param info
     */
    public void setBNHSInfo(String info) {
	try {
	    this.metadata.add(MetadataElement.BNHS_INFO.getName(), info);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getRiverBasin() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.RIVER_BASIN.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param riverBasin
     */
    public void setRiverBasin(String riverBasin) {
	try {
	    this.metadata.add(MetadataElement.RIVER_BASIN.getName(), riverBasin);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getRiver() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.RIVER.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param river
     */
    public void setRiver(String river) {
	try {
	    this.metadata.add(MetadataElement.RIVER.getName(), river);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
    
    /**
     * @return
     */
    public Optional<String> getDataDisclaimer() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.DATA_DISCLAIMER.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param dataDisclaimer
     */
    public void setDataDisclaimer(String dataDisclaimer) {
	try {
	    this.metadata.add(MetadataElement.DATA_DISCLAIMER.getName(), dataDisclaimer);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getCountry() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.COUNTRY.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param country
     */
    public void setCountry(String country) {
	try {
	    this.metadata.add(MetadataElement.COUNTRY.getName(), country);
	    Country c = Country.decode(country);
	    if (c != null && !getCountryISO3().isPresent()) {
		setCountryISO3(c.getISO3());
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getCountryISO3() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.COUNTRY_ISO3.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param country
     */
    public void setCountryISO3(String country) {
	try {
	    this.metadata.add(MetadataElement.COUNTRY_ISO3.getName(), country);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniqueInstrumentIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param id
     */
    public void setUniqueInstrumentIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER.getName(), id);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * The size of the dataset (it is the multiplication of the sizes along each dimension, e.g. d_s = d1_s * d2_s * ...
     * dn_s
     * 
     * @param size
     */
    public void setDataSize(Long size) {
	try {
	    this.metadata.add(MetadataElement.DATA_SIZE.getName(), "" + size);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return The size of the dataset (it is the multiplication of the sizes along each dimension, e.g. d_s = d1_s *
     *         d2_s * ... * dn_s
     */
    public Optional<Long> getDataSize() {

	try {
	    String str = this.metadata.getTextContent(MetadataElement.DATA_SIZE.getName());
	    if (str == null || str.isEmpty()) {
		return Optional.empty();
	    }

	    long size = Long.parseLong(str);
	    return Optional.of(size);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param id
     */
    public void setUniquePlatformIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER.getName(), id);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniquePlatformIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param id
     */
    public void setUniqueAttributeIdentifier(String id) {
	try {
	    this.metadata.add(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER.getName(), id);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getUniqueAttributeIdentifier() {

	try {
	    return Optional.ofNullable(this.metadata.getTextContent(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER.getName()));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getCropTypes() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.CROP_TYPES.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param cropTypes
     */
    public void setCropTypes(String cropTypes) {
	try {
	    this.metadata.add(MetadataElement.CROP_TYPES.getName(), cropTypes);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<InterpolationType> getTimeInterpolation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_INTERPOLATION.getName());
	    InterpolationType interpolation = InterpolationType.decode(str);
	    return Optional.ofNullable(interpolation);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getTimeInterpolationString() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_INTERPOLATION.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    /**
     * @param interpolationType
     */
    public void setTimeInterpolation(InterpolationType interpolationType) {

	if (interpolationType != null) {

	    setTimeInterpolation(interpolationType.name());
	}
    }

    /**
     * @param interpolationType
     */
    public void setTimeInterpolation(String interpolationType) {

	if (interpolationType == null) {
	    return;
	}

	try {
	    this.metadata.add(MetadataElement.TIME_INTERPOLATION.getName(), interpolationType);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getTimeSupport() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_SUPPORT.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param timeSupport
     */
    public void setTimeSupport(String timeSupport) {
	try {
	    this.metadata.add(MetadataElement.TIME_SUPPORT.getName(), timeSupport);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeResolution() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_RESOLUTION.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param timeResolution
     * @deprecated  use setTimeResolutionDuration8601
     */
    @Deprecated
    public void setTimeResolution(String timeResolution) {
	try {
	    this.metadata.add(MetadataElement.TIME_RESOLUTION.getName(), timeResolution);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getTimeUnits() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_UNITS.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param timeUnits
     */
    public void setTimeUnits(String timeUnits) {
	try {
	    this.metadata.add(MetadataElement.TIME_UNITS.getName(), timeUnits);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getTimeResolutionDuration8601() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_RESOLUTION_DURATION_8601.getName());
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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param resolution8601
     */
    public void setTimeResolutionDuration8601(String resolution8601) {
	try {
	    resolution8601 = ISO8601DateTimeUtils.normalizeISO8601Duration(resolution8601);
	    this.metadata.add(MetadataElement.TIME_RESOLUTION_DURATION_8601.getName(), resolution8601);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to parse value '{}': {}",resolution8601,e.getMessage());
	}
    }

    /**
     * @return
     */
    public Optional<String> getTimeAggregationDuration8601() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_AGGREGATION_DURATION_8601.getName());
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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param timeAggregation8601
     */
    public void setTimeAggregationDuration8601(String timeAggregation8601) {
	try {
	    timeAggregation8601 = ISO8601DateTimeUtils.normalizeISO8601Duration(timeAggregation8601);
	    this.metadata.add(MetadataElement.TIME_AGGREGATION_DURATION_8601.getName(), timeAggregation8601);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getTimeUnitsAbbreviation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.TIME_UNITS_ABBREVIATION.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param timeUnitsAbbreviation
     */
    public void setTimeUnitsAbbreviation(String timeUnitsAbbreviation) {
	try {
	    this.metadata.add(MetadataElement.TIME_UNITS_ABBREVIATION.getName(), timeUnitsAbbreviation);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getAttributeUnitsURI() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS_URI.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    /**
     * @param attributeURI
     */
    public void setAttributeUnitsURI(String attributeURI) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS_URI.getName(), attributeURI);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param obsPropURI
     */
    public void setObservedPropertyURI(String obsPropURI) {
	try {
	    this.metadata.add(MetadataElement.OBSERVED_PROPERTY_URI.getName(), obsPropURI);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    public void clearObservedPropertyURI() {

	try {
	    this.metadata.remove("//" + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":" + MetadataElement.OBSERVED_PROPERTY_URI.getName());
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getObservedPropertyURI() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.OBSERVED_PROPERTY_URI.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    /**
     * @param topic
     */
    public void setWISTopicHierarchy(String topic) {
	try {
	    this.metadata.add(MetadataElement.WIS_TOPIC_HIERARCHY.getName(), topic);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

    }

    /**
     * @return
     */
    public Optional<String> getWISTopicHierarchy() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.WIS_TOPIC_HIERARCHY.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getAttributeUnits() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param attributeUnits
     */
    public void setAttributeUnits(String attributeUnits) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS.getName(), attributeUnits);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getAttributeUnitsAbbreviation() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param attributeUnitsAbbreviation
     */
    public void setAttributeUnitsAbbreviation(String attributeUnitsAbbreviation) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION.getName(), attributeUnitsAbbreviation);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getAttributeMissingValue() {
	try {
	    String str = this.metadata.getTextContent(MetadataElement.ATTRIBUTE_MISSING_VALUE.getName());
	    return Optional.ofNullable(str);
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param attributeMissingValue
     */
    public void setAttributeMissingValue(String attributeMissingValue) {
	try {
	    this.metadata.add(MetadataElement.ATTRIBUTE_MISSING_VALUE.getName(), attributeMissingValue);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    // -------------------------------------------------------
    //
    // Shape properties
    //
    // -------------------------------------------------------

    /**
     * @param shape
     */
    public void setShape(String shape) {

	try {
	    this.metadata.add(GEOMETRY, shape);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getShape() {

	try {
	    return Optional.of(this.metadata.getTextContent(GEOMETRY));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param centroid
     */
    public void setCentroid(String centroid) {

	try {
	    this.metadata.add(CENTROID, centroid);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<String> getCentroid() {

	try {
	    return Optional.of(this.metadata.getTextContent(CENTROID));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param area
     */
    public void setArea(double area) {

	try {
	    this.metadata.add(AREA, String.valueOf(area));
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    public Optional<Double> getArea() {

	try {
	    return Optional.of(this.metadata.getTextContent(AREA)).map(v -> Double.valueOf(v));
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    // ---------------------------------------------
    //
    // Non queryable properties
    //
    // ---------------------------------------------

    /**
     * @param property
     * @param value
     */
    public void setBNHSProperty(BNHSProperty property, String value) {
	try {
	    this.metadata.add(property.getElement().getName(), value);
	} catch (Exception e) {

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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }

    /**
     * @return
     */
    public List<String> getOriginatorOrganisationIdentifiers() {
	try {
	    return this.metadata.getTextContents(ORIGINATOR_ORGANISATION_IDENTIFIER);
	} catch (XPathExpressionException e) {

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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param scene
     */
    public void setSatelliteScene(SatelliteScene scene) {

	try {
	    this.metadata.remove("//*:satelliteScene");
	    this.metadata.add(scene.asDocument(true).getDocumentElement());
	} catch (Exception e) {

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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.empty();
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

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    //
    //
    //

    @Override
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

}
