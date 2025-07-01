package eu.essi_lab.profiler.os;

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

import java.util.ArrayList;
import java.util.Optional;

import org.h2.util.StringUtils;

import eu.essi_lab.lib.what3words.What3Words;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.spatial.SpatialEntity;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceType;
import eu.essi_lab.pdk.LayerFeatureRetrieval;
import eu.essi_lab.profiler.os.OSBox.CardinalPoint;

public abstract class OSParameters {

    /**
     *
     */
    public static final OSParameter START_INDEX = new OSParameter("si", "int", "1", "{startIndex}");

    /**
     *
     */
    public static final OSParameter COUNT = new OSParameter("ct", "int", "10", "{count}");

    /**
    *
    */
    public static final OSParameter SOURCES = new OSParameter("sources", "identifiers", null, "{gs:sources}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.split(",");
	    if (split.length > 1) {
		LogicalBond orBond = BondFactory.createOrBond();
		for (int i = 0; i < split.length; i++) {
		    orBond.getOperands().add(BondFactory.createSourceIdentifierBond(split[i]));
		}
		return Optional.of(orBond);
	    }

	    return Optional.of(BondFactory.createSourceIdentifierBond(value));
	}
    };

    /**
    *
    */
    public static final OSParameter PARENTS = new OSParameter("parents", "identifiers", null, "{gs:parents}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.split(",");
	    if (split.length > 1) {
		LogicalBond orBond = BondFactory.createOrBond();
		for (int i = 0; i < split.length; i++) {
		    orBond.getOperands().add(//
			    BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, split[i]));
		}
		return Optional.of(orBond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, value));
	}
    };

    /**
    *
    */
    public static final OSParameter IDENTIFIER = new OSParameter("identifier", "string", null, "{gs:identifier}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, value));
	}
    };

    /**
    *
    */
    public static final OSParameter TARGETID = new OSParameter("targetId", "string", null, "{gs:targetId}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, value));
	}
    };
    
    public static final OSParameter PREDEFINED_LAYER = new OSParameter("predefinedLayer", "string", null, "{gs:predefinedLayer}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    // Get WKT from layer
	    String wkt = LayerFeatureRetrieval.getInstance().getFeature(value);
	    if (wkt != null) {
		return Optional.of(create(wkt, relatedValues));
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter ID = new OSParameter("id", "identifiers", null, "{gs:id}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.split(",");
	    if (split.length > 1) {
		LogicalBond orBond = BondFactory.createOrBond();
		for (int i = 0; i < split.length; i++) {
		    orBond.getOperands().add(//
			    BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, split[i]));
		}
		return Optional.of(orBond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, value));
	}
    };

    /**
    *
    */
    public static final OSParameter TARGETIDS = new OSParameter("targetIds", "identifiers", null, "{gs:targetIds}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.split(",");
	    if (split.length > 1) {
		LogicalBond orBond = BondFactory.createOrBond();
		for (int i = 0; i < split.length; i++) {
		    orBond.getOperands().add(//
			    BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, split[i]));
		}
		return Optional.of(orBond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, value));
	}
    };

    /**
    *
    */
    public static final OSParameter KEYWORD = new OSParameter("kwd", "string", null, "{gs:keyword}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    return createBond(value, MetadataElement.KEYWORD);
	}
    };

    /**
    *
    */
    public static final OSParameter VIEW_ID = new OSParameter("viewid", "string", null, "{gs:viewId}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    return Optional.of(BondFactory.createViewBond(value));
	}
    };

    /**
    *
    */
    public static final OSParameter FORMAT = new OSParameter("frmt", "string", null, "{gs:format}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    return createBond(value, MetadataElement.DISTRIBUTION_FORMAT);
	}
    };

    /**
    *
    */
    public static final OSParameter PROTOCOL = new OSParameter("prot", "string", null, "{gs:protocol}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    return createBond(value, MetadataElement.ONLINE_PROTOCOL);
	}
    };

    /**
     *
     */
    public static final OSParameter SEARCH_TERMS = new OSParameter("st", "freeText", null, "{searchTerms}");

    /**
    *
    */
    public static final OSParameter SEARCH_FIELDS = new OSParameter("searchFields", "enumeration", "title,subject", "{gs:searchFields}");

    /**
    *
    */

    /**
    *
    */
    public static final OSParameter HL = new OSParameter("hl", "string", null, "{gs:hl}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    ResourceType type = null;

	    if (value.equals("series") || value.equals("DatasetCollection")) {
		type = ResourceType.DATASET_COLLECTION;
	    }

	    if (value.equals("service") || value.equals("Service")) {
		type = ResourceType.SERVICE;
	    }

	    if (value.equals("dataset") || value.equals("Dataset")) {
		type = ResourceType.DATASET;
	    }

	    return Optional.of(BondFactory.createResourceTypeBond(type));
	}
    };

    /**
    *
    */
    public static final OSParameter INSTRUMENT_IDENTIFIER = new OSParameter("instrumentId", "string", null, "{gs:instrumentIdentifier}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.INSTRUMENT_IDENTIFIER);
	}
    };

    /**
    *
    */
    public static final OSParameter INSTRUMENT_DESCRIPTION = new OSParameter("instrumentDesc", "string", null,
	    "{gs:instrumentDescription}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.INSTRUMENT_DESCRIPTION);
	}
    };

    /**
    *
    */
    public static final OSParameter INSTRUMENT_TITLE = new OSParameter("instrumentTitle", "string", null, "{gs:instrumentTitle}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(BondOperator.TEXT_SEARCH, value, MetadataElement.INSTRUMENT_TITLE);
	}
    };

    /**
    *
    */
    public static final OSParameter PLATFORM_IDENTIFIER = new OSParameter("platformId", "string", null, "{gs:platformIdentifier}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.PLATFORM_IDENTIFIER);
	}
    };

    /**
    *
    */
    public static final OSParameter PLATFORM_TITLE = new OSParameter("platformTitle", "string", null, "{gs:platformTitle}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(BondOperator.TEXT_SEARCH, value, MetadataElement.PLATFORM_TITLE);
	}
    };

    public static final OSParameter INTENDED_OBSERVATION_SPACING = new OSParameter("intendedObservationSpacing", "string", null,
	    "{gs:intendedObservationSpacing}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.TIME_RESOLUTION_DURATION_8601);
	}
    };

    public static final OSParameter AGGREGATION_DURATION = new OSParameter("aggregationDuration", "string", null,
	    "{gs:aggregationDuration}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.TIME_AGGREGATION_DURATION_8601);
	}
    };

    public static final OSParameter TIME_INTERPOLATION = new OSParameter("timeInterpolation", "string", null, "{gs:timeInterpolation}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.TIME_INTERPOLATION);
	}
    };

    /**
    *
    */
    public static final OSParameter PLATFORM_DESCRIPTION = new OSParameter("platformDesc", "string", null, "{gs:platformDescription}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.PLATFORM_DESCRIPTION);
	}
    };

    /**
    *
    */
    public static final OSParameter ORIGINATOR_ORGANISATION_IDENTIFIER = new OSParameter("origOrgId", "string", null,
	    "{gs:originatorOrganisationIdenfitier}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER);
	}
    };

    /**
    *
    */
    public static final OSParameter ORIGINATOR_ORGANISATION_DESCRIPTION = new OSParameter("origOrgDesc", "string", null,
	    "{gs:originatorOrganisationDescription}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION);
	}
    };

    /**
    *
    */
    public static final OSParameter TEAM_CATEGORY = new OSParameter("themeCategory", "string", null, "{gs:themeCategory}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.THEME_CATEGORY);
	}
    };

    /**
    *
    */
    public static final OSParameter ORGANISATION_NAME = new OSParameter("organisationName", "string", null, "{gs:organisationName}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.ORGANISATION_NAME);
	}
    };

    /**
    *
    */
    public static final OSParameter ROSETTA = new OSParameter("rosetta", "string", null, "{gs:rosetta}");

    /**
    *
    */
    public static final OSParameter SEMANTICS = new OSParameter("semantics", "string", null, "{gs:semantics}");

    /**
    *
    */
    public static final OSParameter ONTOLOGY = new OSParameter("ontology", "string", null, "{gs:ontology}");

    /**
    *
    */
    public static final OSParameter ATTRIBUTE_IDENTIFIER = new OSParameter("attributeId", "string", null, "{gs:attributeIdentifier}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.ATTRIBUTE_IDENTIFIER);
	}
    };

    /**
    *
    */
    public static final OSParameter ATTRIBUTE_DESCRIPTION = new OSParameter("attributeDesc", "string", null, "{gs:attributeDescription}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.ATTRIBUTE_DESCRIPTION);
	}
    };

    /**
    *
    */
    public static final OSParameter ATTRIBUTE_TITLE = new OSParameter("attributeTitle", "string", null, "{gs:attributeTitle}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(BondOperator.TEXT_SEARCH, value, MetadataElement.ATTRIBUTE_TITLE);
	}
    };

    /**
    *
    */
    public static final OSParameter OBSERVED_PROPERTY_URI = new OSParameter("observedPropertyURI", "string", null,
	    "{gs:observedPropertyURI}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return createBond(value, MetadataElement.OBSERVED_PROPERTY_URI);
	}
    };

    /**
     *
     */
    public static final OSParameter TIME_START = new OSParameter("ts", "dateTime", null, "{time:start}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String timeRelation = "OVERLAPS"; // by default

	    if (relatedValues.length > 0) {
		timeRelation = relatedValues[0];
	    }

	    BondOperator operator = BondOperator.decode(timeRelation);

	    switch (operator) {
	    case BBOX: // same as not disjoint, as OGC filter encoding 09-026r2
	    case INTERSECTS:
		return Optional
			.of(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, value));
	    case CONTAINS:
		return Optional.of(BondFactory.createSimpleValueBond(BondOperator.GREATER, MetadataElement.TEMP_EXTENT_BEGIN, value));
	    case DISJOINT:
	    default:
		throw new IllegalArgumentException("Operator not yet implemented: " + operator);
	    }

	}
    };

    /**
    *
    */
    public static final OSParameter PUB_DATE_FROM = new OSParameter("pubDatefrom", "dateTime", null, "{gs:pubDatefrom}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, value));
	}
    };

    /**
    *
    */
    public static final OSParameter PUB_DATE_UNTIL = new OSParameter("pubDateuntil", "dateTime", null, "{gs:pubDateuntil}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, value));
	}
    };

    /**
     *
     */
    public static final OSParameter TIME_END = new OSParameter("te", "dateTime", null, "{time:end}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String timeRelation = "OVERLAPS"; // by default

	    if (relatedValues.length > 0) {
		timeRelation = relatedValues[0];
	    }

	    BondOperator operator = BondOperator.decode(timeRelation);

	    switch (operator) {
	    case BBOX: // same as not disjoint, as OGC filter encoding 09-026r2
	    case INTERSECTS:
		return Optional.of(BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, value));

	    case CONTAINS:
		return Optional.of(BondFactory.createSimpleValueBond(BondOperator.LESS, MetadataElement.TEMP_EXTENT_END, value));

	    case DISJOINT:
	    default:
		throw new IllegalArgumentException("Operator not yet implemented: " + operator);
	    }

	}
    };

    /**
    *
    */
    public static final OSParameter MAX_MAG = new OSParameter("maxmag", "double", null, "{gs:maxmag}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.QML_MAGNITUDE_VALUE,
		    Double.valueOf(value)));
	}
    };

    /**
    *
    */
    public static final OSParameter MIN_MAG = new OSParameter("minmag", "double", null, "{gs:minmag}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.QML_MAGNITUDE_VALUE,
		    Double.valueOf(value)));
	}
    };

    /**
    *
    */
    public static final OSParameter MAX_DEPTH = new OSParameter("maxdepth", "double", null, "{gs:maxdepth}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(
		    BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.QML_DEPTH_VALUE, Double.valueOf(value)));
	}
    };

    /**
    *
    */
    public static final OSParameter MIN_DEPTH = new OSParameter("mindepth", "double", null, "{gs:mindepth}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.QML_DEPTH_VALUE,
		    Double.valueOf(value)));
	}
    };

    /**
    *
    */
    public static final OSParameter MAX_TYPE = new OSParameter("magtype", "double", null, "{gs:magtype}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.QML_MAGNITUDE_TYPE, value));
	}
    };

    public static final OSParameter EVENT_ORDER = new OSParameter("evtOrd", "enumeration", null, "{gs:evtOrd}");

    /**
    *
    */
    public static final OSParameter SPATIAL_RELATION = new OSParameter("rel", "string", "OVERLAPS", "{gs:rel}");

    /**
    *
    */
    public static final OSParameter TIME_RELATION = new OSParameter("timeRel", "string", "OVERLAPS", "{gs:timeRel}");

    /**
    *
    */
    public static final OSParameter GDC = new OSParameter("gdc", "boolean", null, "{gs:gdc}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createIsGEOSSDataCoreBond(Boolean.valueOf(value)));
	}
    };

    /**
    *
    */
    public static final OSParameter INSITU = new OSParameter("inSitu", "boolean", null, "{gs:inSitu}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(MetadataElement.IN_SITU, Boolean.valueOf(value)));
	}
    };

    /**
     * BondFactory.createSimpleValueBond(element, Boolean.valueOf(literal));
     */
    public static final OSParameter TERM_FREQUENCY = new OSParameter("tf", "string", "", "{gs:tf}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter SSC_SCORE = new OSParameter("sscScore", "string", "", "{gs:sscScore}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    // e.g: [0,20]
	    if (!value.equals("")) {

		String[] range = value.replace("[", "").replace("]", "").split(",");
		LogicalBond andBond = BondFactory.createAndBond();
		ResourcePropertyBond min = BondFactory.createSSCScoreBond(BondOperator.GREATER_OR_EQUAL, Integer.valueOf(range[0]));
		ResourcePropertyBond max = BondFactory.createSSCScoreBond(BondOperator.LESS_OR_EQUAL, Integer.valueOf(range[1]));
		andBond.getOperands().add(min);
		andBond.getOperands().add(max);
		return Optional.of(andBond);
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter CLOUD_COVER_PERC = new OSParameter("cloudcp", "string", "", "{gs:cloudcp}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    // e.g: [0,20]
	    if (!value.equals("")) {

		String[] range = value.replace("[", "").replace("]", "").split(",");
		LogicalBond andBond = BondFactory.createAndBond();
		SimpleValueBond min = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.CLOUD_COVER_PERC,
			Double.valueOf(range[0]));
		SimpleValueBond max = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.CLOUD_COVER_PERC,
			Double.valueOf(range[1]));
		andBond.getOperands().add(min);
		andBond.getOperands().add(max);
		return Optional.of(andBond);
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter PRODUCT_TYPE = new OSParameter("prodType", "string", null, "{gs:prodType}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PRODUCT_TYPE, value));
	}
    };

    /**
    *
    */
    public static final OSParameter SENSOR_OP_MODE = new OSParameter("sensorOpMode", "string", null, "{gs:sensorOpMode}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.SENSOR_OP_MODE, value));
	}
    };

    /**
    *
    */
    public static final OSParameter SENSOR_SWATH = new OSParameter("sensorSwath", "string", null, "{gs:sensorSwath}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.SENSOR_SWATH, value));
	}
    };

    /**
    *
    */
    public static final OSParameter S3_INSTRUMENT_IDX = new OSParameter("s3InstrumentIdx", "string", null, "{gs:s3InstrumentIdx}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.S3_INSTRUMENT_IDX, value));
	}
    };

    /**
    *
    */
    public static final OSParameter S3_PRODUCT_LEVEL = new OSParameter("s3ProductLevel", "string", null, "{gs:s3ProductLevel}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.S3_PRODUCT_LEVEL, value));
	}
    };

    /**
    *
    */
    public static final OSParameter S3_TIMELINESS = new OSParameter("s3Timeliness", "string", null, "{gs:s3Timeliness}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.S3_TIMELINESS, value));
	}
    };

    /**
    *
    */
    public static final OSParameter EOP_POLARIZATION_ORIENTATION_CODE = new OSParameter("sarPolCh", "string", null, "{gs:sarPolCh}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.SAR_POL_CH, value));
	}
    };

    /**
    *
    */
    public static final OSParameter RELATIVE_ORBIT = new OSParameter("relOrbit", "string", null, "{gs:relOrbit}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.RELATIVE_ORBIT, value));
	}
    };

    /**
    *
    */
    public static final OSParameter ROW = new OSParameter("row", "int", null, "{gs:row}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ROW, value));
	}
    };

    /**
    *
    */
    public static final OSParameter PATH = new OSParameter("path", "int", null, "{gs:path}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PATH, value));
	}
    };

    /**
    *
    */
    public static final OSParameter IS_VALIDATED = new OSParameter("isValidated", "string", null, "{gs:isValidated}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(BondFactory.createIsValidatedBond(Boolean.valueOf(value)));
	}
    };

    /**
     *
     */
    public static final OSParameter BBOX = new OSParameter("bbox", "bbox", null, "{geo:box}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.split("_");

	    ArrayList<Bond> bondList = new ArrayList<Bond>();

	    for (String bbox : split) {

		OSBox osBox = new OSBox(bbox);

		SpatialBond s = create( //
			osBox.getDouble(CardinalPoint.SOUTH), //
			osBox.getDouble(CardinalPoint.WEST), //
			osBox.getDouble(CardinalPoint.NORTH), //
			osBox.getDouble(CardinalPoint.EAST), relatedValues); //

		bondList.add(s);
	    }

	    Bond bond = null;
	    if (bondList.size() > 1) {
		bond = BondFactory.createAndBond(bondList.toArray(new Bond[] {}));
	    } else if (bondList.size() == 1) {
		bond = bondList.get(0);
	    }

	    return Optional.of(bond);
	}
    };

    /**
    *
    */
    public static final OSParameter WKT = new OSParameter("wkt", "wkt", null, "{wkt}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {

		return Optional.empty();
	    }

	    SpatialBond spatialBond = create(StringUtils.urlDecode(value), relatedValues);

	    return Optional.of(spatialBond);
	}
    };

    /**
    *
    */
    public static final OSParameter W3W = new OSParameter("w3w", "string", null, "{gs:w3w}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) throws Exception {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    String[] split = value.trim().split(",");
	    String words1 = split[0];

	    if (split.length > 1) {

		String words2 = split[1];
		return Optional.of(from3Words(words1, words2, relatedValues));
	    }

	    return Optional.of(from3Words(words1, 5, relatedValues));
	}
    };

    public static final OSParameter CROP_TYPES = new OSParameter("cropTypes", "string", null, "{gs:cropTypes}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    if (value.contains(",")) {
		String[] split = value.split(",");
		LogicalBond bond = BondFactory.createOrBond();
		for (String s : split) {
		    bond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.CROP_TYPES, s.trim()));
		}
		return Optional.of(bond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.CROP_TYPES, value));
	}
    };

    public static final OSParameter QUANTITY_TYPES = new OSParameter("quantityTypes", "string", null, "{gs:quantityTypes}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    if (value.contains(",")) {
		String[] split = value.split(",");
		LogicalBond bond = BondFactory.createOrBond();
		for (String s : split) {
		    bond.getOperands().add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.QUANTITY_TYPES, s.trim()));
		}
		return Optional.of(bond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.QUANTITY_TYPES, value));
	}
    };

    public static final OSParameter LAND_COVER_TYPES = new OSParameter("landCoverTypes", "string", null, "{gs:landCoverTypes}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    if (value.contains(",")) {
		String[] split = value.split(",");
		LogicalBond bond = BondFactory.createOrBond();
		for (String s : split) {
		    bond.getOperands()
			    .add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.LAND_COVER_TYPES, s.trim()));
		}
		return Optional.of(bond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.LAND_COVER_TYPES, value));
	}
    };

    public static final OSParameter IRRIGATION_TYPES = new OSParameter("irrigationTypes", "string", null, "{gs:irrigationTypes}") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    if (value.contains(",")) {
		String[] split = value.split(",");
		LogicalBond bond = BondFactory.createOrBond();
		for (String s : split) {
		    bond.getOperands()
			    .add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IRRIGATION_TYPES, s.trim()));
		}
		return Optional.of(bond);
	    }

	    return Optional.of(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IRRIGATION_TYPES, value));
	}
    };

    /**
    *
    */
    public static final OSParameter CROP_CONFIDENCE = new OSParameter("cropConfidence", "string", "", "{gs:cropConfidence}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    // e.g: [0,20]
	    if (!value.equals("")) {

		String[] range = value.replace("[", "").replace("]", "").split(",");
		LogicalBond andBond = BondFactory.createAndBond();
		SimpleValueBond min = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.CONFIDENCE_CROP_TYPE,
			Double.valueOf(range[0]));
		SimpleValueBond max = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.CONFIDENCE_CROP_TYPE,
			Double.valueOf(range[1]));
		andBond.getOperands().add(min);
		andBond.getOperands().add(max);
		return Optional.of(andBond);
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter LAND_COVER_CONFIDENCE = new OSParameter("landCoverConfidence", "string", "",
	    "{gs:landCoverConfidence}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    // e.g: [0,20]
	    if (!value.equals("")) {

		String[] range = value.replace("[", "").replace("]", "").split(",");
		LogicalBond andBond = BondFactory.createAndBond();
		SimpleValueBond min = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.CONFIDENCE_LC_TYPE,
			Double.valueOf(range[0]));
		SimpleValueBond max = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.CONFIDENCE_LC_TYPE,
			Double.valueOf(range[1]));
		andBond.getOperands().add(min);
		andBond.getOperands().add(max);
		return Optional.of(andBond);
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter IRRIGATION_CONFIDENCE = new OSParameter("irrigationConfidence", "string", "",
	    "{gs:irrigationConfidence}") {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    // e.g: [0,20]
	    if (!value.equals("")) {

		String[] range = value.replace("[", "").replace("]", "").split(",");
		LogicalBond andBond = BondFactory.createAndBond();
		SimpleValueBond min = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.CONFIDENCE_IRR_TYPE,
			Double.valueOf(range[0]));
		SimpleValueBond max = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.CONFIDENCE_IRR_TYPE,
			Double.valueOf(range[1]));
		andBond.getOperands().add(min);
		andBond.getOperands().add(max);
		return Optional.of(andBond);
	    }

	    return Optional.empty();
	}
    };

    /**
    *
    */
    public static final OSParameter EIFFEL_DISCOVERY = new OSParameter("eiffelDiscovery", "string", null, "{gs:eiffelDiscovery}");

    /**
    *
    */
    public static final OSParameter OUTPUT_VERSION = new OSParameter("outputVersion", "versionString", "1.0", "{gs:version}");

    /**
    *
    */
    public static final OSParameter OUTPUT_FORMAT = new OSParameter("outputFormat", "freeText", null, null);

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     * @param relatedValues
     * @return
     */
    private static SpatialBond create(double south, double west, double north, double east, String... relatedValues) {

	SpatialEntity entity = SpatialEntity.of(south, west, north, east);

	String spatialRelation = relatedValues[0];
	BondOperator operator = BondOperator.decode(spatialRelation);

	return BondFactory.createSpatialEntityBond(operator, entity);
    }

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     * @param relatedValues
     * @return
     */
    public static SpatialBond create(String wkt, String... relatedValues) {

	SpatialEntity entity = SpatialEntity.of(wkt);

	String spatialRelation = relatedValues[0];
	BondOperator operator = BondOperator.decode(spatialRelation);

	return BondFactory.createSpatialEntityBond(operator, entity);
    }

    /**
     * @param words
     * @param additionalDegrees
     * @param relatedValues
     * @return
     * @throws Exception
     */
    private static SpatialBond from3Words(String words, double additionalDegrees, String... relatedValues) throws Exception {

	String[] split = words.split("\\.");

	What3Words what3Words = new What3Words(split[0], split[1], split[2]);

	String latitude = what3Words.getLatitude();
	String longitude = what3Words.getLongitude();

	return create( //
		Double.valueOf(latitude) - additionalDegrees, // south
		Double.valueOf(longitude) + additionalDegrees, // west
		Double.valueOf(latitude) - additionalDegrees, // north
		Double.valueOf(longitude) + additionalDegrees, relatedValues); // east
    }

    /**
     * @param words1
     * @param words2
     * @param relatedValues
     * @return
     * @throws Exception
     */
    private static SpatialBond from3Words(String words1, String words2, String... relatedValues) throws Exception {

	String[] split1 = words1.split("\\.");
	String[] split2 = words2.split("\\.");

	What3Words what3Words1 = new What3Words(split1[0], split1[1], split1[2]);
	What3Words what3Words2 = new What3Words(split2[0], split2[1], split2[2]);

	double lat1 = Double.valueOf(what3Words1.getLatitude());
	double lon1 = Double.valueOf(what3Words1.getLongitude());

	double lat2 = Double.valueOf(what3Words2.getLatitude());
	double lon2 = Double.valueOf(what3Words2.getLongitude());

	return create( //
		lat1 <= lat2 ? lat1 : lat2, // south
		lon1 <= lon2 ? lon1 : lon2, // west
		lat1 >= lat2 ? lat1 : lat2, // north
		lon1 >= lon2 ? lon1 : lon2, relatedValues); // east
    }

    private static Optional<Bond> createBond(String value, MetadataElement element) {
	return createBond(BondOperator.EQUAL, value, element);
    }

    /**
     * @param value
     * @param element
     * @return
     */
    public static Optional<Bond> createBond(BondOperator operator, String value, MetadataElement element) {

	if (value == null || value.equals("")) {
	    return Optional.empty();
	}

	if (value.contains(" AND ")) {
	    String[] split = value.split(" AND ");
	    LogicalBond bond = BondFactory.createAndBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	if (value.contains(" OR ")) {
	    String[] split = value.split(" OR ");
	    LogicalBond bond = BondFactory.createOrBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	return Optional.of(BondFactory.createSimpleValueBond(operator, element, value.trim()));
    }

}
