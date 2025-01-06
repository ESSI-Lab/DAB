package eu.essi_lab.profiler.om;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import eu.essi_lab.model.resource.stax.ResponsiblePartyParser;
import eu.essi_lab.profiler.om.JSONObservation.ObservationType;

public class ObservationMapper {

    public enum Property {
	MONITORING_POINT_ORIGINAL_IDENTIFIER("monitoringPointOriginalIdentifier"), COUNTRY("country"), ALL("all");

	String id;

	Property(String id) {
	    this.id = id;
	}

	public String getId() {
	    return id;
	}

	public static Property decode(String s) {
	    for (Property p : values()) {
		if (s.equals(p.getId())) {
		    return p;
		}
	    }
	    return null;
	}
    }

    /**
     * 
     */
    public ObservationMapper() {

    }

    /**
     * @param result
     * @param properties
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    public JSONObservation map(Optional<View> view, String result, List<Property> properties) throws XMLStreamException, IOException {

	HashSet<Property> propertySet = new HashSet<Property>(properties);

	if (propertySet.isEmpty()) {
	    propertySet.add(Property.ALL);
	}

	GIResourceParser parser = new GIResourceParser(result);

	List<String> foundParties = parser.find(new QName("CI_ResponsibleParty"));

	List<JSONObject> relatedParties = new ArrayList<>();

	for (String party : foundParties) {

	    ResponsiblePartyParser innerParser = new ResponsiblePartyParser(party);

	    relatedParties.add(JSONFeature.createRelatedParty(innerParser.getOrganisation(), innerParser.getIndividual(),
		    innerParser.getEmail(), innerParser.getRole(), innerParser.getUrl()));
	}

	//
	//
	//

	// JSONTimeseries timeseries = new JSONTimeseries();

	JSONFeature platform;
	if (!view.isPresent() || !(view.get().getCreator().contains("change") || view.get().getCreator().contains("trigger"))) {
	    platform = new JSONFeature();
	} else {
	    platform = new IchangeJSONFeature();
	}
	JSONObservation observation = null;

	List<String> points = parser.getPoints();

	if (points != null && !points.isEmpty()) {
	    observation = createObservation(view.get().getId(), ObservationType.TrajectoryObservation);//

	    List<List<BigDecimal>> multiPoints = new ArrayList<List<BigDecimal>>();
	    for (String point : points) {
		point = point.trim();
		List<BigDecimal> decimals = new ArrayList<BigDecimal>();
		String[] split = point.split(" ");
		decimals.add(new BigDecimal(split[1]));
		decimals.add(new BigDecimal(split[0]));
		if (!view.isPresent() || !(view.get().getCreator().contains("change") || view.get().getCreator().contains("trigger"))) {
		    decimals.add(new BigDecimal(split[2]));
		}

		multiPoints.add(decimals);
	    }
	    platform.setMultiPoints(multiPoints);

	} else if (parser.west != null && !parser.west.equals("") && parser.east != null && !parser.east.equals("") && parser.north != null
		&& !parser.north.equals("") && parser.south != null && !parser.south.equals("")//
	) {
	    BigDecimal bigs = new BigDecimal(parser.south);
	    BigDecimal bigw = new BigDecimal(parser.west);
	    BigDecimal bign = new BigDecimal(parser.north);
	    BigDecimal bige = new BigDecimal(parser.east);

	    Double TOL = 0.000000001;
	    if ((bign.doubleValue() - bigs.doubleValue() > TOL) && //
		    (bige.doubleValue() - bigw.doubleValue() > TOL)) {

		observation = createObservation(view.get().getId(), ObservationType.SamplingSurfaceObservation);

		platform.setBBOX(bign, bigw, bigs, bige);

	    } else {
		observation = createObservation(view.get().getId(), ObservationType.TimeSeriesObservation);

		platform.setLatLon(bigs, bigw);
	    }

	} else {
	    observation = createObservation(view.get().getId(), ObservationType.TimeSeriesObservation);
	}

	if (parser.getPlatformName() != null && !parser.getPlatformName().equals("")) {
	    platform.setName(parser.getPlatformName());
	}

	if (parser.uniquePlatformCode != null && !parser.uniquePlatformCode.equals("")) {
	    platform.setId(parser.uniquePlatformCode);
	}

	if (isIncluded(propertySet, Property.COUNTRY)) {
	    if (parser.country != null && !parser.country.isEmpty()) {
		platform.addParameter("country", parser.country);

	    }
	}
	GSSource source = ConfigurationWrapper.getSource(parser.getSourceId());
	platform.addParameter("source", source.getLabel());

	String originalPlatformCode = parser.originalPlatformCode;

	if (originalPlatformCode != null && !originalPlatformCode.isEmpty()) {
	    // workaround
	    if (originalPlatformCode.startsWith("HIS-CENTRAL-TOSCANA")) {
		originalPlatformCode = originalPlatformCode.replace("HIS-CENTRAL-TOSCANA", "");
		if (originalPlatformCode.contains(":")) {
		    originalPlatformCode = originalPlatformCode.substring(0, originalPlatformCode.indexOf(":"));
		}
	    } else if (originalPlatformCode.startsWith("argentina-ina:")) {
		originalPlatformCode = originalPlatformCode.replace("argentina-ina:", "");
	    } else if (originalPlatformCode.startsWith("urn:ANA:")) {
		originalPlatformCode = originalPlatformCode.replace("urn:ANA:", "");
	    } else if (originalPlatformCode.startsWith("urn:wmo:wigos:")) {
		originalPlatformCode = originalPlatformCode.replace("urn:wmo:wigos:", "");
	    } else if (originalPlatformCode.startsWith("urn:br.gov.ana.sar:reservoir:")) {
		originalPlatformCode = originalPlatformCode.replace("urn:br.gov.ana.sar:reservoir:", "");
	    } else if (originalPlatformCode.startsWith("meteorologia.gov.py:")) {
		originalPlatformCode = originalPlatformCode.replace("meteorologia.gov.py:", "");
	    } else if (originalPlatformCode.startsWith("https://apitempo.inmet.gov.br/plata/:station:")) {
		originalPlatformCode = originalPlatformCode.replace("https://apitempo.inmet.gov.br/plata/:station:", "");
	    } else if (originalPlatformCode.startsWith("urn:uruguay-dinagua:")) {
		originalPlatformCode = originalPlatformCode.replace("urn:uruguay-dinagua:", "");
	    } else if (originalPlatformCode.startsWith("https://api.inumet.gub.uy:station:")) {
		originalPlatformCode = originalPlatformCode.replace("https://api.inumet.gub.uy:station:", "");
	    }

	    if (isIncluded(propertySet, Property.MONITORING_POINT_ORIGINAL_IDENTIFIER)) {
		platform.addParameter("identifier", parser.originalPlatformCode);
	    }
	}

	//
	//
	//
	relatedParties.forEach(p -> platform.addRelatedParty(p));

	// timeseries.setFeatureOfInterest(uniquePlatformCode, platformName);

	observation.addParameter("source", source.getLabel());
	
	observation.addParameter("observedPropertyDefinition", parser.getAttributeDescription());

	observation.setFeatureOfInterest(platform);

	SKOSConcept ontologyConcept = null;
	if (parser.getAttributeURI() != null && !parser.getAttributeURI().isEmpty()) {
	    HydroOntology ontology;
	    if (!view.isPresent() || !view.get().getCreator().equals("his_central")) {
		ontology = new WHOSOntology();
	    } else {
		ontology = new HISCentralOntology();
	    }
	    ontologyConcept = ontology.getConcept(parser.getAttributeURI());
	}
	if (ontologyConcept != null) {
	    String originalAttributeName = parser.getAttributeName();
	    parser.getAttributeNames().clear();
	    parser.getAttributeNames().add(ontologyConcept.getPreferredLabel().getKey());
	    observation.addParameter("originalObservedProperty", originalAttributeName);
	} else {
	    parser.getAttributeURIs().clear();
	    parser.getAttributeURIs().add(parser.attributeCode);
	    // attributeName = attributeName + " (" + sourceId + ")";
	}

	observation.setObservedProperty(parser.getAttributeURI(), parser.getAttributeName());
	if (parser.timeSupport != null && parser.timeUnits != null && !parser.timeSupport.isEmpty() && !parser.timeUnits.isEmpty()) {
	    try {
		BigDecimal bd = new BigDecimal(parser.timeSupport);
		Duration duration = ISO8601DateTimeUtils.getDuration(bd, parser.timeUnits);
		if (duration != null) {
		    observation.setAggregationDuration(duration);
		} else {
		    GSLoggerFactory.getLogger(ObservationMapper.class).error("Unknown duration: {} {}", bd, parser.timeUnits);
		}
	    } catch (Exception e) {

	    }
	}

	if (parser.timeSpacing != null && parser.timeUnits != null && !parser.timeSpacing.isEmpty() && !parser.timeUnits.isEmpty()) {
	    try {
		BigDecimal bd = new BigDecimal(parser.timeSpacing);
		Duration duration = ISO8601DateTimeUtils.getDuration(bd, parser.timeUnits);
		if (duration != null) {
		    observation.setIntendedObservationSpacing(duration);
		} else {
		    GSLoggerFactory.getLogger(ObservationMapper.class).error("Unknown time spacing: {} {}", bd, parser.timeUnits);
		}
	    } catch (Exception e) {
		// TODO: handle exception
	    }
	}

	Date begin = null;
	Date end = null;

	if (parser.tmpExtentBegin != null && !parser.tmpExtentBegin.isEmpty()) {
	    Optional<Date> opt = ISO8601DateTimeUtils.parseISO8601ToDate(parser.tmpExtentBegin);
	    if (opt.isPresent()) {
		begin = opt.get();
	    }
	}
	if (parser.tmpExtentEnd != null && !parser.tmpExtentEnd.isEmpty()) {
	    Optional<Date> opt = ISO8601DateTimeUtils.parseISO8601ToDate(parser.tmpExtentEnd);
	    if (opt.isPresent()) {
		end = opt.get();
	    }
	}
	if (parser.tmpExtentBeginNow != null && !parser.tmpExtentBeginNow.isEmpty()) {
	    begin = new Date();
	}
	if (parser.tmpExtentBeginBeforeNow != null && !parser.tmpExtentBeginBeforeNow.isEmpty()) {
	    begin = new Date(
		    new Date().getTime() - ISO8601DateTimeUtils.getDuration(parser.tmpExtentBeginBeforeNow).getTimeInMillis(new Date()));
	}
	if (parser.tmpExtentEndNow != null && !parser.tmpExtentEndNow.isEmpty()) {
	    end = new Date();
	}

	if (begin != null && end != null) {
	    observation.setPhenomenonTime(begin, end);
	}

	if (parser.timeInterpolation != null && !parser.timeInterpolation.isEmpty()) {
	    InterpolationType interpolation = InterpolationType.decode(parser.timeInterpolation);
	    if (interpolation == null) {
		observation.setInterpolationType("", parser.timeInterpolation);
	    } else {
		String uri = interpolation.getWML2URI();
		if (uri == null) {
		    String label = interpolation.getLabel();
		    observation.setInterpolationType("", label);
		} else {
		    observation.setInterpolationType(uri, interpolation.getLabel());
		}
	    }
	}

	if (parser.getUnits() == null || parser.getUnits().isEmpty()) {
	    if (!view.isPresent() || !(view.get().getCreator().contains("change") || view.get().getCreator().contains("trigger"))) {
		observation.setUOM(parser.unitsAbbreviation);
	    } else {
		String u = parser.unitsAbbreviation;
		if (u != null && (u.contains("°C") || u.contains("ºC"))) {
		    u = u.replace("°C", "K").replace("ºC", "K");
		    ;
		}
		parser.unitsAbbreviation = u;
		observation.setUOM(u);
	    }
	} else {
	    if (!view.isPresent() || !(view.get().getCreator().contains("change") || view.get().getCreator().contains("trigger"))) {
		observation.setUOM(parser.units);
	    } else {
		String u = parser.units;
		if (u != null && (u.contains("°C") || u.contains("ºC"))) {
		    u = u.replace("°C", "K").replace("ºC", "K");
		    ;
		}
		parser.units = u;
		observation.setUOM(u);
	    }
	}

	observation.setId(parser.onlineId);

	return observation;

    }

    private JSONObservation createObservation(String view, ObservationType type) {
	if (view == null || !(view.equals("i-change") || view.equals("trigger"))) {
	    return new JSONObservation(type);
	} else {
	    return new IchangeJSONObservation(type);
	}
    }

    private static boolean isIncluded(HashSet<Property> propertySet, Property property) {
	if (propertySet.contains(property)) {
	    return true;
	}
	if (propertySet.contains(Property.ALL)) {
	    return true;
	}
	return false;
    }
}
