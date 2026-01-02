package eu.essi_lab.model.resource.stax;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

public class GIResourceParser extends StAXDocumentParser {

    public String west;

    public String getWest() {
	if (!west.isEmpty()) {
	    return west;
	}
	GeographicBoundingBox bbox = getBBOX();
	if (bbox != null) {
	    return bbox.getBigDecimalWest().toString();
	}
	return west;
    }

    public String getEast() {
	if (!east.isEmpty()) {
	    return east;
	}
	GeographicBoundingBox bbox = getBBOX();
	if (bbox != null) {
	    return bbox.getBigDecimalEast().toString();
	}
	return east;
    }

    public GeographicBoundingBox getBBOX() {
	if (geometry.isEmpty()) {
	    return null;
	}
	String geometry = this.geometry.trim().toLowerCase();
	if (geometry.startsWith("point")) {
	    geometry = geometry.substring(geometry.indexOf("(") + 1).replace("(", "").replace(")", "").trim();
	    String[] split = geometry.split(" ");
	    GeographicBoundingBox ret = new GeographicBoundingBox();
	    ret.setBigDecimalEast(new BigDecimal(split[0]));
	    ret.setBigDecimalWest(new BigDecimal(split[0]));
	    ret.setBigDecimalNorth(new BigDecimal(split[1]));
	    ret.setBigDecimalSouth(new BigDecimal(split[1]));
	    return ret;
	} else if (geometry.startsWith("polygon")) {
	    geometry = geometry.substring(geometry.indexOf("(") + 1).replace("(", "").replace(")", "").trim();
	    String[] pps = geometry.split(",");
	    BigDecimal west = null;
	    BigDecimal east = null;
	    BigDecimal north = null;
	    BigDecimal south = null;
	    for (String pp : pps) {
		String[] split = pp.trim().split(" ");
		BigDecimal lon = new BigDecimal(split[0]);
		BigDecimal lat = new BigDecimal(split[1]);
		if (west == null || west.compareTo(lon) == 1) {
		    west = lon;
		}
		if (east == null || east.compareTo(lon) == -1) {
		    east = lon;
		}
		if (south == null || south.compareTo(lat) == 1) {
		    south = lat;
		}
		if (north == null || north.compareTo(lat) == -1) {
		    north = lat;
		}
	    }
	    GeographicBoundingBox ret = new GeographicBoundingBox();
	    ret.setBigDecimalEast(east);
	    ret.setBigDecimalWest(west);
	    ret.setBigDecimalNorth(north);
	    ret.setBigDecimalSouth(south);
	    return ret;
	} else {
	    return null;
	}
    }

    public String getNorth() {
	if (!north.isEmpty()) {
	    return north;
	}
	GeographicBoundingBox bbox = getBBOX();
	if (bbox != null) {
	    return bbox.getBigDecimalNorth().toString();
	}
	return north;
    }

    public String getSouth() {
	if (!south.isEmpty()) {
	    return south;
	}
	GeographicBoundingBox bbox = getBBOX();
	if (bbox != null) {
	    return bbox.getBigDecimalSouth().toString();
	}
	return south;
    }

    public List<String> getPlatformURIs() {
	return platformNames;
    }

    public List<String> getPlatformNames() {
	return platformNames;
    }

    public String getPlatformName() {
	if (platformNames.isEmpty()) {
	    return "";
	} else {
	    return platformNames.get(0);
	}
    }

    public String getOriginalPlatformCode() {
	return originalPlatformCode;
    }

    public String getUniquePlatformCode() {
	return uniquePlatformCode;
    }

    public List<String> getInstrumentNames() {
	return instrumentNames;
    }

    public List<String> getAttributeNames() {
	return attributeNames;
    }

    public String getAttributeName() {
	if (attributeNames.isEmpty()) {
	    return "";
	}
	return attributeNames.get(0);
    }

    public List<String> getAttributeDescriptions() {
	return attributeDescriptions;
    }

    public String getAttributeDescription() {
	if (attributeDescriptions.isEmpty()) {
	    return "";
	}
	return attributeDescriptions.get(0);
    }

    public String getAttributeCode() {
	return attributeCode;
    }

    public List<String> getAttributeURIs() {
	return observedPropertyURIs;
    }

    public String getAttributeURI() {
	if (observedPropertyURIs.isEmpty()) {
	    return "";
	} else {
	    return observedPropertyURIs.get(0);
	}
    }

    public String getCountry() {
	return country;
    }

    public String getTmpExtentBegin() {
	return tmpExtentBegin;
    }

    public String getTmpExtentBeginNow() {
	return tmpExtentBeginNow;
    }

    public String getTmpExtentBeginBeforeNow() {
	return tmpExtentBeginBeforeNow;
    }

    public String getTmpExtentEnd() {
	return tmpExtentEnd;
    }

    public String getTmpExtentEndNow() {
	return tmpExtentEndNow;
    }

    public String getTimeInterpolation() {
	return timeInterpolation;
    }

    public String getUnits() {
	return units;
    }

    public String getUnitsAbbreviation() {
	return unitsAbbreviation;
    }

    public String getTimeSupport() {
	return timeSupport;
    }

    public String getTimeSpacing() {
	return timeSpacing;
    }

    public String getTimeUnits() {
	return timeUnits;
    }

    public String getOnlineId() {
	return onlineId;
    }

    public String getSourceId() {
	return sourceId;
    }

    public List<String> getPoints() {
	return points;
    }

    public String east = "";
    public String north = "";
    public String south = "";
    public List<String> oranizationNames = new ArrayList<String>();
    public List<String> oranizationRoles = new ArrayList<String>();
    public List<String> organizationURIs = new ArrayList<String>();
    public List<String> platformNames = new ArrayList<String>();
    public List<String> platformURIs = new ArrayList<String>();
    public String originalPlatformCode = "";
    public String uniquePlatformCode = "";
    public List<String> attributeNames = new ArrayList<String>();
    public List<String> attributeDescriptions = new ArrayList<String>();
    public List<String> instrumentNames = new ArrayList<String>();
    public String attributeCode = "";
    public List<String> observedPropertyURIs = new ArrayList<String>();
    public List<String> intrumentNames = new ArrayList<String>();
    public List<String> intrumentURIs = new ArrayList<String>();
    public String country = "";
    public String tmpExtentBegin = "";
    public String tmpExtentBeginNow = "";
    public String tmpExtentBeginBeforeNow = "";
    public String tmpExtentEnd = "";
    public String tmpExtentEndNow = "";
    public String timeInterpolation = "";
    public String units = "";
    public String unitsAbbreviation = "";
    public String timeSupport = "";
    public String timeSpacing = "";
    public String timeUnits = "";
    public String fileIdentifier = "";
    public String geometry = "";

    public String getGeometry() {
	return geometry;
    }

    public String getFileIdentifier() {
	return fileIdentifier;
    }

    public String onlineId = "";

    public String sourceId = "";
    public List<String> points = new ArrayList<>();
    public List<String> keywords = new ArrayList<>();

    public List<String> getKeywords() {
	return keywords;
    }

    public List<String> keywordTypes = new ArrayList<>();

    public List<String> getKeywordTypes() {
	return keywordTypes;
    }

    private String title = "";
    private String abstractz = "";
    private String resourceId = "";
    private String distributionLinkage = "";
    private String distributionProtocol = "";
    private String distributionName = "";
    private String graphicOverview = "";

    public String getDistributionName() {
	return distributionName;
    }

    public GIResourceParser(String result) throws XMLStreamException, IOException {
	super(result);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.IDENTIFIER.getName()), v -> fileIdentifier = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, ResourceProperty.SOURCE_ID_NAME), v -> sourceId = normalize(v));

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "west"), v -> west = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "south"), v -> south = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "east"), v -> east = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "north"), v -> north = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "geometry"), v -> geometry = normalize(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "bbox"), v -> geometry = normalize(v));

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.RESOURCE_IDENTIFIER.getName()), v -> this.resourceId = v);

	// INSTRUMENT

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.INSTRUMENT_TITLE_EL_NAME), v -> this.instrumentNames.add(v));

	// PLATFORM
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.PLATFORM_TITLE_EL_NAME), v -> this.platformNames.add(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.PLATFORM_IDENTIFIER_EL_NAME), v -> this.originalPlatformCode = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER_EL_NAME),
		v -> this.uniquePlatformCode = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.PLATFORM_URI_EL_NAME), v -> this.platformURIs.add(v));

	// ATTRIBUTE

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ATTRIBUTE_TITLE_EL_NAME), v -> this.attributeNames.add(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ATTRIBUTE_DESCRIPTION_EL_NAME),
		v -> this.attributeDescriptions.add(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER_EL_NAME),
		v -> this.attributeCode = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.OBSERVED_PROPERTY_URI_EL_NAME),
		v -> this.observedPropertyURIs.add(v));
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TITLE.getName()), v -> this.title = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ABSTRACT.getName()), v -> this.abstractz = v);
	add(new QName("http://www.isotc211.org/2005/gmd", "fileName"), new QName("http://www.isotc211.org/2005/gco", "CharacterString"),
		v -> this.graphicOverview += v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.COUNTRY.getName()), v -> this.country = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TEMP_EXTENT_BEGIN.getName()), v -> this.tmpExtentBegin = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TEMP_EXTENT_END.getName()), v -> this.tmpExtentEnd = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ONLINE_LINKAGE.getName()), v -> this.distributionLinkage += v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ONLINE_PROTOCOL_EL_NAME), v -> this.distributionProtocol = v);

	add(new QName("http://www.isotc211.org/2005/gmd", "name"), new QName("http://www.isotc211.org/2005/gco", "CharacterString"),
		v -> this.distributionName = v);

	// commented, as it doesn't work for now, below there is a workaround

	// add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentBegin_Now"), v -> this.tmpExtentBeginNow =
	// "true");
	// add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentEnd_Now"), v -> this.tmpExtentEndNow =
	// "true");

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW.getName()),
		v -> this.tmpExtentBeginBeforeNow = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TIME_INTERPOLATION_EL_NAME), v -> this.timeInterpolation = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ATTRIBUTE_UNITS_EL_NAME), v -> this.units = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION_EL_NAME),
		v -> this.unitsAbbreviation = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TIME_SUPPORT_EL_NAME), v -> this.timeSupport = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TIME_RESOLUTION_EL_NAME), v -> this.timeSpacing = v);
	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.TIME_UNITS_EL_NAME), v -> this.timeUnits = v);

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.ONLINE_ID.getName()), v -> this.onlineId = v);

	add(new QName("http://www.opengis.net/gml", "pos"), v -> this.points.add(v));

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, MetadataElement.KEYWORD_EL_NAME), v -> this.keywords.add(v));

	add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "keywordType"), v -> this.keywordTypes.add(v));

	parse();

	// workaround
	if (result.contains("tmpExtentBegin_Now")) {
	    this.tmpExtentBeginNow = "true";
	}
	if (result.contains("tmpExtentEnd_Now")) {
	    this.tmpExtentEndNow = "true";
	}
    }

    private String normalize(String v) {
	if (v==null) {
	    return null;
	}
	return v.trim();
    }

    public String getTitle() {
	return title;
    }

    public String getAbstract() {
	return abstractz;
    }

    public String getResourceId() {
	return resourceId;
    }

    public String getDistributionLinkage() {
	return distributionLinkage;
    }

    public String getDistributionProtocol() {
	return distributionProtocol;
    }

    public String getGraphicOverview() {
	return graphicOverview;
    }

}
