package eu.essi_lab.profiler.wis.metadata.collection;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

public class ViewMapper {

    private String west;
    private String east;
    private String north;
    private String south;
    private String platformName;
    private String originalPlatformCode;
    private String uniquePlatformCode;
    private String attributeName;
    private String attributeCode;
    private String country;
    private String tmpExtentBegin;
    private String tmpExtentBeginNow;
    private String tmpExtentEnd;
    private String tmpExtentEndNow;
    private String timeInterpolation;
    private String units;
    private String timeSupport;
    private String timeSpacing;
    private String timeUnits;
    private String onlineId;
    private String organisation;
    private String individual;
    private String role;
    private String email;
    private String url;

    /**
     * 
     */
    public ViewMapper() {

    }

    /**
     * @param result
     * @param properties
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    public JSONObject map(String result) throws XMLStreamException, IOException {

	JSONObject ret = new JSONObject();

	StAXDocumentParser parser = new StAXDocumentParser(result);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "west"), v -> west = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "south"), v -> south = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "east"), v -> east = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "north"), v -> north = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "platformTitle"), v -> this.platformName = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "platformId"), v -> this.originalPlatformCode = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "uniquePlatformId"), v -> this.uniquePlatformCode = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "uniqueAttributeId"), v -> this.attributeCode = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "attributeTitle"), v -> this.attributeName = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "Country"), v -> this.country = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentBegin"), v -> this.tmpExtentBegin = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentEnd"), v -> this.tmpExtentEnd = v);

	// commented, as it doesn't work for now, below there is a workaround

	// parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentBegin_Now"), v -> this.tmpExtentBeginNow =
	// "true");
	// parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "tmpExtentEnd_Now"), v -> this.tmpExtentEndNow =
	// "true");

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "timeInterpolation"), v -> this.timeInterpolation = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "attributeUnits"), v -> this.units = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "timeSupport"), v -> this.timeSupport = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "timeResolution"), v -> this.timeSpacing = v);
	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "timeUnits"), v -> this.timeUnits = v);

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "onlineId"), v -> this.onlineId = v);

	parser.parse();

	// workaround
	if (result.contains("tmpExtentBegin_Now")) {
	    this.tmpExtentBeginNow = "true";
	}
	if (result.contains("tmpExtentEnd_Now")) {
	    this.tmpExtentEndNow = "true";
	}

	ret.put("id", onlineId);
	ret.put("type", "Feature");
	ret.put("version", "v04");
	JSONObject geometry = new JSONObject();
	geometry.put("type", "Point");
	JSONArray coordinates = new JSONArray();
	coordinates.put(Double.parseDouble(west));
	coordinates.put(Double.parseDouble(south));
	// elevation TODO

	geometry.put("coordinates", coordinates);
	ret.put("geometry", geometry);

	JSONObject properties = new JSONObject();
	properties.put("data_id", onlineId);
	// properties.put("pubtime", onlineId); TODO
	JSONObject content = new JSONObject();
	content.put("length", 254);
	properties.put("content", content);
	ret.put("properties", properties);
	ret.put("wigos_station_identifier", uniquePlatformCode);
	ret.put("id", onlineId);

	List<String> foundParties = parser.find(new QName("CI_ResponsibleParty"));

	List<JSONObject> relatedParties = new ArrayList<>();

//	for (String party : foundParties) {
//
//	    StAXDocumentParser innerParser = new StAXDocumentParser(party);
//
//	    organisation = null;
//	    individual = null;
//	    role = null;
//	    email = null;
//	    url = null;
//
//	    innerParser.add(//
//		    new QName("organisationName"), //
//		    new QName("CharacterString")//
//		    , v -> organisation = v);
//
//	    innerParser.add(//
//		    new QName("individualName"), //
//		    new QName("CharacterString")//
//		    , v -> individual = v);
//
//	    innerParser.add(//
//		    new QName("role"), //
//		    new QName("CI_RoleCode")//
//		    , v -> role = v);
//
//	    innerParser.add(//
//		    new QName("electronicMailAddress"), //
//		    new QName("CharacterString")//
//		    , v -> email = v);
//
//	    innerParser.add(//
//		    new QName("linkage"), //
//		    new QName("URL")//
//		    , v -> url = v);
//
//	    innerParser.parse();
//
//	    relatedParties.add(JSONMonitoringPoint.createRelatedParty(organisation, individual, email, role, url));
//	}
//
//	//
//	//
//	//
//
//	if (west != null && !west.equals("") && east != null && !east.equals("") && north != null && !north.equals("") && south != null
//		&& !south.equals("")//
//
//	// && sourceID != null && !sourceID.equals("")//
//	) {
//
//	    ret.setLat(new BigDecimal(south));
//	    ret.setLon(new BigDecimal(west));
//	}
//
//	if (platformName != null && !platformName.equals("")//
//		&& uniquePlatformCode != null && !uniquePlatformCode.equals("")) {
//	    platform.setSampledFeature(uniquePlatformCode, platformName);
//	}
//
//	if (isIncluded(propertySet, Property.COUNTRY)) {
//	    if (country != null && !country.isEmpty()) {
//		platform.addParameter("country", country);
//
//	    }
//	}
//
//	if (originalPlatformCode != null && !originalPlatformCode.isEmpty()) {
//	    // workaround
//	    if (originalPlatformCode.startsWith("HIS-CENTRAL-TOSCANA")) {
//		originalPlatformCode = originalPlatformCode.replace("HIS-CENTRAL-TOSCANA", "");
//		if (originalPlatformCode.contains(":")) {
//		    originalPlatformCode = originalPlatformCode.substring(0, originalPlatformCode.indexOf(":"));
//		}
//	    } else if (originalPlatformCode.startsWith("argentina-ina:")) {
//		originalPlatformCode = originalPlatformCode.replace("argentina-ina:", "");
//	    } else if (originalPlatformCode.startsWith("urn:ANA:")) {
//		originalPlatformCode = originalPlatformCode.replace("urn:ANA:", "");
//	    } else if (originalPlatformCode.startsWith("urn:wmo:wigos:")) {
//		originalPlatformCode = originalPlatformCode.replace("urn:wmo:wigos:", "");
//	    } else if (originalPlatformCode.startsWith("urn:br.gov.ana.sar:reservoir:")) {
//		originalPlatformCode = originalPlatformCode.replace("urn:br.gov.ana.sar:reservoir:", "");
//	    } else if (originalPlatformCode.startsWith("meteorologia.gov.py:")) {
//		originalPlatformCode = originalPlatformCode.replace("meteorologia.gov.py:", "");
//	    } else if (originalPlatformCode.startsWith("https://apitempo.inmet.gov.br/plata/:station:")) {
//		originalPlatformCode = originalPlatformCode.replace("https://apitempo.inmet.gov.br/plata/:station:", "");
//	    } else if (originalPlatformCode.startsWith("urn:uruguay-dinagua:")) {
//		originalPlatformCode = originalPlatformCode.replace("urn:uruguay-dinagua:", "");
//	    } else if (originalPlatformCode.startsWith("https://api.inumet.gub.uy:station:")) {
//		originalPlatformCode = originalPlatformCode.replace("https://api.inumet.gub.uy:station:", "");
//	    }
//
//	    if (isIncluded(propertySet, Property.MONITORING_POINT_ORIGINAL_IDENTIFIER)) {
//		platform.addParameter("identifier", originalPlatformCode);
//	    }
//	}
//
//	//
//	//
//	//
//	relatedParties.forEach(p -> platform.addRelatedParty(p));
//
//	timeseries.setFeatureOfInterest(platform);
//
//	timeseries.setObservedProperty(attributeCode, attributeName);
//	if (timeSupport != null && timeUnits != null) {
//	    try {
//		BigDecimal bd = new BigDecimal(timeSupport);
//		Duration duration = ISO8601DateTimeUtils.getDuration(bd, timeUnits);
//		if (duration != null) {
//		    timeseries.setAggregationDuration(duration);
//		} else {
//		    GSLoggerFactory.getLogger(FeatureMapper.class).error("Unknown duration: {} {}", bd, timeUnits);
//		}
//	    } catch (Exception e) {
//		// TODO: handle exception
//	    }
//	}
//
//	if (timeSpacing != null && timeUnits != null) {
//	    try {
//		BigDecimal bd = new BigDecimal(timeSpacing);
//		Duration duration = ISO8601DateTimeUtils.getDuration(bd, timeUnits);
//		if (duration != null) {
//		    timeseries.setIntendedObservationSpacing(duration);
//		} else {
//		    GSLoggerFactory.getLogger(FeatureMapper.class).error("Unknown time spacing: {} {}", bd, timeUnits);
//		}
//	    } catch (Exception e) {
//		// TODO: handle exception
//	    }
//	}
//
//	Date begin = null;
//	Date end = null;
//
//	if (tmpExtentBegin != null) {
//	    Optional<Date> opt = ISO8601DateTimeUtils.parseISO8601ToDate(tmpExtentBegin);
//	    if (opt.isPresent()) {
//		begin = opt.get();
//	    }
//	}
//	if (tmpExtentEnd != null) {
//	    Optional<Date> opt = ISO8601DateTimeUtils.parseISO8601ToDate(tmpExtentEnd);
//	    if (opt.isPresent()) {
//		end = opt.get();
//	    }
//	}
//	if (tmpExtentBeginNow != null) {
//	    begin = new Date();
//	}
//	if (tmpExtentEndNow != null) {
//	    end = new Date();
//	}
//
//	if (begin != null && end != null) {
//	    timeseries.setPhenomenonTime(begin, end);
//	}
//
//	if (timeInterpolation != null) {
//	    InterpolationType interpolation = InterpolationType.decode(timeInterpolation);
//	    if (interpolation == null) {
//		timeseries.setInterpolationType("", timeInterpolation);
//	    } else {
//		String uri = interpolation.getWML2URI();
//		if (uri == null) {
//		    String label = interpolation.getLabel();
//		    timeseries.setInterpolationType("", label);
//		} else {
//		    timeseries.setInterpolationType(uri, interpolation.getLabel());
//		}
//	    }
//	}
//
//	timeseries.setUOM(units);
//
//	timeseries.setId(onlineId);
//
//	ret.setSeries(timeseries);
//
//	ret.setOnlineResource(onlineId);

	return ret;
    }

}
