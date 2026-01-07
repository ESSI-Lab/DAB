package eu.essi_lab.profiler.csw;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractQueryType;
import eu.essi_lab.jaxb.csw._2_0_2.Constraint;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.filter._1_1_0.BBOXType;
import eu.essi_lab.jaxb.filter._1_1_0.BinaryComparisonOpType;
import eu.essi_lab.jaxb.filter._1_1_0.BinaryLogicOpType;
import eu.essi_lab.jaxb.filter._1_1_0.BinarySpatialOpType;
import eu.essi_lab.jaxb.filter._1_1_0.ComparisonOpsType;
import eu.essi_lab.jaxb.filter._1_1_0.DistanceBufferType;
import eu.essi_lab.jaxb.filter._1_1_0.FilterType;
import eu.essi_lab.jaxb.filter._1_1_0.LiteralType;
import eu.essi_lab.jaxb.filter._1_1_0.LogicOpsType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyIsLikeType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyIsNullType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.jaxb.filter._1_1_0.SpatialOpsType;
import eu.essi_lab.jaxb.filter._1_1_0.UnaryLogicOpType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import net.opengis.gml.v_3_2_0.EnvelopeType;

/**
 * @author Fabrizio
 */
public class CSWGetRecordsParser {

    private static final String CSW_GET_RECORDS_PARSER_FILTER_CHECK_ERROR = "CSW_GET_RECORDS_PARSER_FILTER_CHECK_ERROR";
    private static final String CSW_GET_RECORDS_PARSER_FILTER_PARSING_ERROR = "CSW_GET_RECORDS_PARSER_FILTER_PARSING_ERROR";
    private FilterType filter;
    private GetRecords getRecords;

    /**
     * @param getRecordsStream
     * @throws JAXBException
     */
    public CSWGetRecordsParser(InputStream getRecordsStream) throws JAXBException {

	this.getRecords = CommonContext.unmarshal(getRecordsStream, GetRecords.class);
	initFilter(getRecords);
    }

    /**
     * @param getRecords
     */
    public CSWGetRecordsParser(GetRecords getRecords) {

	this.getRecords = getRecords;
	initFilter(getRecords);
    }

    /**
     * @return
     * @throws GSException
     */
    public Bond parseFilter() throws GSException {

	if (filter != null) {

	    if (filter.isSetComparisonOps()) {

		return parseComparisonOps(filter.getComparisonOps());
	    }

	    try {

		if (filter.isSetSpatialOps()) {

		    return parseSpatialOps(filter.getSpatialOps());
		}

		if (filter.isSetLogicOps()) {

		    return parseLogicalOps(filter.getLogicOps());
		}

	    } catch (JAXBException ex) {

		throw GSException.createException(getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CSW_GET_RECORDS_PARSER_FILTER_PARSING_ERROR, //
			ex);
	    }

	    checkFilter(filter);
	}

	return null;
    }

    /**
     * @return
     */
    public GetRecords getGetRecords() {
	return getRecords;
    }

    /**
     * @param getRecords
     */
    private void initFilter(GetRecords getRecords) {

	JAXBElement<? extends AbstractQueryType> query = getRecords.getAbstractQuery();

	if (query != null) {

	    QueryType type = (QueryType) query.getValue();

	    Constraint constraint = type.getConstraint();
	    if (constraint != null) {
		filter = constraint.getFilter();
		if (filter != null) {
		    filter.getSpatialOps();
		}
	    }
	}
    }

    /**
     * @param filter
     * @throws GSException
     */
    private void checkFilter(FilterType filter) throws GSException {
	Boolean check = false;
	try {

	    ByteArrayOutputStream baos = CommonContext.asOutputStream(filter, true);

	    XMLDocumentReader reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));
	    check = reader.evaluateBoolean("count(.//*/*) > 0");

	} catch (Exception ex) {
	    throw GSException.createException(getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_RECORDS_PARSER_FILTER_CHECK_ERROR, //
		    ex);
	}

	if (check) {
	    throw new IllegalArgumentException("Unsupported filter");
	}
    }

    @SuppressWarnings("unchecked")
    private Bond parse(JAXBElement<?> element) throws JAXBException {

	Object object = element.getValue();

	if (object instanceof LogicOpsType) {
	    return parseLogicalOps((JAXBElement<? extends LogicOpsType>) element);
	}

	if (object instanceof ComparisonOpsType) {
	    return parseComparisonOps((JAXBElement<? extends ComparisonOpsType>) element);
	}

	if (object instanceof SpatialOpsType) {
	    return parseSpatialOps((JAXBElement<? extends SpatialOpsType>) element);
	}

	throw new IllegalArgumentException("Unsupported operator type: " + element);
    }

    /**
     * @param lo
     * @return
     * @throws JAXBException
     */
    private Bond parseLogicalOps(JAXBElement<? extends LogicOpsType> lo) throws JAXBException {

	// ----------------
	//
	// OR / AND
	//
	if (isOr(lo) || isAnd(lo)) {

	    BinaryLogicOpType blop = (BinaryLogicOpType) lo.getValue();
	    List<JAXBElement<?>> cals = blop.getComparisonOpsOrSpatialOpsOrLogicOps();
	    Collections.sort(cals, new Comparator<JAXBElement<?>>() {

		@Override
		public int compare(JAXBElement<?> o1, JAXBElement<?> o2) {
		    int v1 = getValue(o1.getValue());
		    int v2 = getValue(o2.getValue());
		    return v1 - v2;
		}

		private int getValue(Object object) {
		    if (object instanceof LogicOpsType) {
			return 10;
		    }
		    if (object instanceof SpatialOpsType) {
			return 5;
		    }
		    if (object instanceof BinaryComparisonOpType) {
			return 2;
		    }
		    if (object instanceof ComparisonOpsType) {
			return 0;
		    }
		    return 100;
		}
	    });

	    LogicalBond logicalBond = null;

	    if (isAnd(lo)) {

		logicalBond = BondFactory.createAndBond();

	    } else {

		logicalBond = BondFactory.createOrBond();
	    }

	    Bond bond = parse(cals.get(0));
	    logicalBond.getOperands().add(bond);

	    for (int i = 1; i < cals.size(); i++) {
		JAXBElement<?> element = cals.get(i);

		bond = parse(element);
		logicalBond.getOperands().add(bond);
	    }

	    return logicalBond;
	}

	// ----------------
	//
	// NOT
	//
	UnaryLogicOpType ulop = (UnaryLogicOpType) lo.getValue();
	Bond bond = null;

	JAXBElement<? extends ComparisonOpsType> cops = ulop.getComparisonOps();
	if (cops != null) {
	    bond = parseComparisonOps(cops);
	}

	JAXBElement<? extends LogicOpsType> lops = ulop.getLogicOps();
	if (lops != null) {
	    bond = parseLogicalOps(lops);
	}

	JAXBElement<? extends SpatialOpsType> sops = ulop.getSpatialOps();
	if (sops != null) {
	    bond = parseSpatialOps(sops);
	}

	if (bond == null) {
	    throw new IllegalArgumentException("Unsupported operator for the Not Logical operator");
	}

	return BondFactory.createNotBond(bond);
    }

    /**
     * We can manage comparison types of the following forms:
     * <ul>
     * <li>Property name, Literal</li>
     * </ul>
     */
    private Bond parseComparisonOps(JAXBElement<? extends ComparisonOpsType> ops) {

	ComparisonOpsType value = ops.getValue();

	// -------------------------------
	//
	// Binary operator
	//
	if (value instanceof BinaryComparisonOpType) {

	    BinaryComparisonOpType bcop = (BinaryComparisonOpType) value;
	    boolean matchCase = true;
	    if (bcop.isSetMatchCase() && !bcop.isMatchCase()) {
		matchCase = false;
	    }

	    PropertyNameType pnt = null;

	    LiteralType lt = null;
	    LiteralType slt = null;

	    List<JAXBElement<?>> expression = bcop.getExpression();
	    if (expression.isEmpty() || expression.size() == 1) {
		throw new IllegalArgumentException("Uncomplete expression of Binary Comparison operator");
	    }

	    // -------------------
	    //
	    // first expression
	    //
	    Object firstExpr = expression.get(0).getValue();
	    if (firstExpr instanceof PropertyNameType) {
		pnt = (PropertyNameType) firstExpr;
	    } else if (firstExpr instanceof LiteralType) {
		slt = (LiteralType) firstExpr;
	    }

	    // -------------------
	    //
	    // second expression
	    //

	    Object secondExpr = expression.get(1).getValue();
	    if (secondExpr instanceof LiteralType) {
		lt = (LiteralType) secondExpr;
	    }

	    // -------------------
	    //
	    // Property, Literal
	    //
	    if ((pnt != null) && (lt != null)) {

		String operator = ops.getName().getLocalPart();
		List<Object> content = lt.getContent();
		String literal = "";
		if (!content.isEmpty()) {
		    literal = content.get(0).toString();
		}

		BondOperator bondOperator = decode(operator);
		if (bondOperator == BondOperator.EQUAL && !matchCase) {
		    bondOperator = BondOperator.TEXT_SEARCH;
		}

		String propertyName = pnt.getContent().get(0).toString();

		return createBond(bondOperator, propertyName, literal);
	    }

	    // -------------------
	    //
	    // Literal, Literal
	    //
	    if ((slt != null) && (lt != null)) {

		throw new IllegalArgumentException("Literal, Literal Comparison operators not supported");
	    }
	}
	// -------------------------------
	//
	// Unary operator
	//
	else if (value instanceof PropertyIsNullType) {

	    PropertyIsNullType type = (PropertyIsNullType) value;

	    PropertyNameType propertyName = type.getPropertyName();
	    String name = propertyName.getContent().get(0).toString();

	    Bond bond = null;
	    try {
		name = removePrefix(name);
		ResourceProperty property = ResourceProperty.fromName(name);
		bond = BondFactory.createMissingResourcePropertyBond(property);
	    } catch (IllegalArgumentException ex) {
		// nothing to do
	    }
	    if (bond == null) {
		bond = BondFactory.createMissingSimpleValueBond(getElement(name));
	    }

	    return bond;

	} else if (value instanceof PropertyIsLikeType) {

	    PropertyIsLikeType type = (PropertyIsLikeType) value;

	    String name = null;
	    PropertyNameType propertyName = type.getPropertyName();
	    if (propertyName.isSetContent()) {
		name = propertyName.getContent().get(0).toString();
	    }

	    String typeValue = null;
	    if (type.isSetLiteral()) {
		typeValue = type.getLiteral().getContent().get(0).toString();
	    }

	    if (name != null && typeValue != null) {

		return createBond(BondOperator.TEXT_SEARCH, name, typeValue);
	    }
	}

	throw new IllegalArgumentException("PropertyIsBetween operator not supported");
    }

    /**
     * @param sops
     * @return
     * @throws JAXBException
     */
    private Bond parseSpatialOps(JAXBElement<? extends SpatialOpsType> sops) throws JAXBException {

	SpatialOpsType value = sops.getValue();

	if (value instanceof DistanceBufferType) {

	    throw new IllegalArgumentException("Distance Spatial Operator not supported");
	}

	EnvelopeType envelope = null;
	BondOperator areaOperator = BondOperator.INTERSECTS;

	Unmarshaller unmarshaller = CommonContext.createUnmarshaller();

	// -------------------
	//
	// BBOX
	//
	if (value instanceof BBOXType) {

	    BBOXType box = (BBOXType) value;

	    List<Object> content = box.getPropertyName().getContent();
	    if (!content.isEmpty()) {
		String propertyName = content.get(0).toString().toLowerCase();
		if (!propertyName.contains("boundingbox")) {
		    throw new IllegalArgumentException("Unsupported geometry-valued property");
		}
	    } else {
		throw new IllegalArgumentException("Missing mandatory geometry-valued property");
	    }

	    if (box.getEnvelope() != null) {

		envelope = (EnvelopeType) ((JAXBElement<?>) unmarshaller.unmarshal(box.getEnvelope())).getValue();
	    } else {

		throw new IllegalArgumentException("Missing Envelope of BBOX Spatial operator");
	    }

	    areaOperator = BondOperator.BBOX;
	}

	// ---------------------------------
	//
	// Intersects,Contains,Disjoint
	//
	if (value instanceof BinarySpatialOpType) {

	    if (sops.getName().getLocalPart().equals("Intersects")) {
		areaOperator = BondOperator.INTERSECTS;
	    } else if (sops.getName().getLocalPart().equals("Contains")) {
		areaOperator = BondOperator.CONTAINS;
	    } else if (sops.getName().getLocalPart().equals("Disjoint")) {
		areaOperator = BondOperator.DISJOINT;
	    } else {

		throw new IllegalArgumentException("Unsupported spatial operator: " + sops.getName().getLocalPart());
	    }

	    BinarySpatialOpType bsot = (BinarySpatialOpType) value;

	    List<Object> content = bsot.getPropertyName().getContent();
	    if (!content.isEmpty()) {
		String propertyName = content.get(0).toString().toLowerCase();
		if (!propertyName.contains("boundingbox")) {
		    throw new IllegalArgumentException("Unsupported geometry-valued property");
		}
	    } else {
		throw new IllegalArgumentException("Missing mandatory geometry-valued property");
	    }

	    Element el = bsot.getEnvelope() == null ? bsot.getAbstractGeometry() : bsot.getEnvelope();
	    if (!isEnvelope(el)) {
		throw new IllegalArgumentException("Unsupported geometry: " + el.getLocalName());
	    }

	    envelope = (EnvelopeType) ((JAXBElement<?>) unmarshaller.unmarshal(el)).getValue();
	}

	if (envelope == null || envelope.getUpperCorner() == null || envelope.getUpperCorner().getValue().isEmpty()) {
	    throw new IllegalArgumentException("Missing Envelope upper corner values");
	}
	if (envelope.getLowerCorner() == null || envelope.getLowerCorner().getValue().isEmpty()) {
	    throw new IllegalArgumentException("Missing Envelope Lower Corner values");
	}

	List<Double> upper = envelope.getUpperCorner().getValue();
	if (upper.size() == 1) {
	    throw new IllegalArgumentException("Missing latitude value in the upper corner");
	}

	List<Double> lower = envelope.getLowerCorner().getValue();
	if (lower.size() == 1) {
	    throw new IllegalArgumentException("Missing latitude value in the lower corner");
	}

	Double w = lower.get(0);
	Double s = lower.get(1);

	Double e = upper.get(0);
	Double n = upper.get(1);

	if (w < -180 || w > 180) {
	    throw new IllegalArgumentException("Longitude value of lower corner must be >= -180 and <= 180");
	}

	if (e < -180 || e > 180) {
	    throw new IllegalArgumentException("Longitude value of upper corner must be >= -180 and <= 180");
	}

	if (s < -90 || s > 90) {
	    throw new IllegalArgumentException("Latitude value of lower corner must be >= -90 and <= 90");
	}

	if (n < -90 || n > 90) {
	    throw new IllegalArgumentException("Latitude value of upper corner must be >= -90 and <= 90");
	}

	if (s > n) {
	    throw new IllegalArgumentException("Latitude value of upper corner must be greater than latitude value of lower corner");
	}

	SpatialExtent spatialExtent = new SpatialExtent(s, w, n, e);
	return BondFactory.createSpatialEntityBond(areaOperator, spatialExtent);
    }

    /**
     * @param lo
     * @return
     */
    private boolean isOr(JAXBElement<? extends LogicOpsType> lo) {
	return (lo.getName().getLocalPart().equalsIgnoreCase("or"));

    }

    /**
     * @param lo
     * @return
     */
    private boolean isAnd(JAXBElement<? extends LogicOpsType> lo) {
	return (lo.getName().getLocalPart().equalsIgnoreCase("and"));
    }

    /**
     * @param element
     * @return
     */
    private boolean isEnvelope(Element element) {
	return element.getLocalName().equalsIgnoreCase("envelope");
    }

    /**
     * @param operator
     * @return
     */
    private BondOperator decode(String operator) {

	if (operator.matches("(?i).*PropertyIsLike.*")) {
	    return BondOperator.TEXT_SEARCH;
	}
	if (operator.matches("(?i).*PropertyIsEqualTo.*")) {
	    return BondOperator.EQUAL;
	}
	if (operator.matches("(?i).*PropertyIsNull.*")) {
	    return BondOperator.NOT_EXISTS;
	}
	if (operator.matches("(?i).*PropertyIsNotEqualTo.*")) {
	    return BondOperator.NOT_EQUAL;
	}
	if (operator.matches("(?i).*PropertyIsLessThanOrEqualTo.*")) {
	    return BondOperator.LESS_OR_EQUAL;
	}
	if (operator.matches("(?i).*PropertyIsGreaterThanOrEqualTo.*")) {
	    return BondOperator.GREATER_OR_EQUAL;
	}
	if (operator.matches("(?i).*PropertyIsLessThan.*")) {
	    return BondOperator.LESS;
	}
	if (operator.matches("(?i).*PropertyIsGreaterThan.*")) {
	    return BondOperator.GREATER;
	}

	throw new IllegalArgumentException("No operator found for: " + operator);
    }

    /**
     * @param bondOperator
     * @param propertyName
     * @param literal
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    private Bond createBond(BondOperator bondOperator, String propertyName, String literal) {

	propertyName = removePrefix(propertyName);
	literal = removeStars(literal);

	// ----------------------------------------------
	//
	// core metadata properties (table 6, OGC 07-045)
	//

	MetadataElement element = getElement(propertyName);
	if (element != null) {

	    switch (element.getContentType()) {
	    case TEXTUAL:
	    case ISO8601_DATE:
	    case ISO8601_DATE_TIME:

		return BondFactory.createSimpleValueBond(bondOperator, element, literal);

	    case DOUBLE:

		return BondFactory.createSimpleValueBond(bondOperator, element, Double.valueOf(literal));

	    case INTEGER:

		return BondFactory.createSimpleValueBond(bondOperator, element, Integer.valueOf(literal));

	    case BOOLEAN:
		if (literal.equals("true") || literal.equals("false")) {
		    return BondFactory.createSimpleValueBond(element, Boolean.valueOf(literal));
		} else {
		    throw new IllegalArgumentException("Invalid boolean value: " + literal);
		}
	    }
	}

	// ----------------------------------------------
	//
	// ESSI extended properties
	//

	if (propertyName.equalsIgnoreCase(ResourceProperty.SOURCE_ID.getName())) {

	    return BondFactory.createSourceIdentifierBond(literal);
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.ORIGINAL_ID.getName())) {

	    return BondFactory.createOriginalIdentifierBond(literal);
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.PRIVATE_ID.getName())) {

	    return BondFactory.createPrivateIdentifierBond(literal);
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.IS_ISO_COMPLIANT.getName())) {

	    return BondFactory.createIsISOCompliantBond(Boolean.valueOf(literal));
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.IS_DELETED.getName())) {

	    return BondFactory.createIsDeletedBond(Boolean.valueOf(literal));
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.IS_VALIDATED.getName())) {

	    return BondFactory.createIsValidatedBond(Boolean.valueOf(literal));
	}

	if (propertyName.equalsIgnoreCase(ResourceProperty.IS_GEOSS_DATA_CORE.getName())) {

	    return BondFactory.createIsGEOSSDataCoreBond(Boolean.valueOf(literal));
	}

	throw new IllegalArgumentException("Unsupported property name: " + propertyName);
    }

    /**
     * @param propertyName
     * @return
     */
    private MetadataElement getElement(String propertyName) {

	propertyName = removePrefix(propertyName);

	// -------------------------------------
	//
	// core metadata properties (table 6, OGC 07-045)
	//
	if (propertyName.equalsIgnoreCase("AnyText")) {

	    return MetadataElement.ANY_TEXT;
	}
	if (propertyName.equalsIgnoreCase("BoundingBox")) {

	    return MetadataElement.BOUNDING_BOX;
	}
	if (propertyName.equalsIgnoreCase("Subject")) {

	    return MetadataElement.SUBJECT;
	}
	if (propertyName.equalsIgnoreCase("Title")) {

	    return MetadataElement.TITLE;
	}
	if (propertyName.equalsIgnoreCase("Abstract")) {

	    return MetadataElement.ABSTRACT;
	}
	if (propertyName.equalsIgnoreCase("Format")) {

	    return MetadataElement.DISTRIBUTION_FORMAT;
	}
	if (propertyName.equalsIgnoreCase("Identifier")) {

	    return MetadataElement.IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("date") || propertyName.equalsIgnoreCase("Modified")) {

	    return MetadataElement.DATE_STAMP;
	}
	if (propertyName.equalsIgnoreCase("Type")) {

	    return MetadataElement.HIERARCHY_LEVEL_CODE_LIST_VALUE;
	}
	// this queryable is here just to pass the cite test
	if (propertyName.equalsIgnoreCase("Relation")) {

	    return MetadataElement.AGGREGATED_RESOURCE_IDENTIFIER;
	}

	// -------------------------------------
	//
	// additional queryable properties common to all information resources (table 10, OGC 07-045)
	//
	if (propertyName.equalsIgnoreCase("RevisionDate")) {

	    return MetadataElement.REVISION_DATE;
	}
	if (propertyName.equalsIgnoreCase("AlternateTitle")) {

	    return MetadataElement.ALTERNATE_TITLE;
	}
	if (propertyName.equalsIgnoreCase("CreationDate")) {

	    return MetadataElement.CREATION_DATE;
	}
	if (propertyName.equalsIgnoreCase("PublicationDate")) {

	    return MetadataElement.PUBLICATION_DATE;
	}
	if (propertyName.equalsIgnoreCase("OrganisationName")) {

	    return MetadataElement.ORGANISATION_NAME;
	}
	if (propertyName.equalsIgnoreCase("HasSecurityConstraints")) {

	    return MetadataElement.HAS_SECURITY_CONSTRAINTS;
	}
	if (propertyName.equalsIgnoreCase("Language")) {

	    return MetadataElement.LANGUAGE;
	}
	if (propertyName.equalsIgnoreCase("ResourceIdentifier")) {

	    return MetadataElement.RESOURCE_IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("ParentIdentifier")) {

	    return MetadataElement.PARENT_IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("KeywordType")) {

	    return MetadataElement.KEYWORD_TYPE;
	}

	// -------------------------------------
	//
	// additional queryable properties (dataset, datasetcollection, application) (table 11, OGC 07-045)
	//
	if (propertyName.equalsIgnoreCase("TopicCategory")) {

	    return MetadataElement.TOPIC_CATEGORY;
	}
	if (propertyName.equalsIgnoreCase("ResourceLanguage")) {

	    return MetadataElement.RESOURCE_LANGUAGE;
	}
	if (propertyName.equalsIgnoreCase("GeographicDescriptionCode")) {

	    return MetadataElement.GEOGRAPHIC_DESCRIPTION_CODE;
	}

	// ---- spatial resolution
	if (propertyName.equalsIgnoreCase("Denominator")) {

	    return MetadataElement.DENOMINATOR;
	}
	if (propertyName.equalsIgnoreCase("DistanceValue")) {

	    return MetadataElement.DISTANCE_VALUE;
	}
	if (propertyName.equalsIgnoreCase("DistanceUOM")) {

	    return MetadataElement.DISTANCE_UOM;
	}
	// ----- end of spatial resolution

	// ---- temporal extent
	if (propertyName.equalsIgnoreCase("TempExtent_begin")) {

	    return MetadataElement.TEMP_EXTENT_BEGIN;
	}
	if (propertyName.equalsIgnoreCase("TempExtent_end")) {

	    return MetadataElement.TEMP_EXTENT_END;
	}
	// ----- end of temporal extent

	// -------------------------------------
	//
	// additional queryable properties (service) (table 14, OGC 07-045)
	//
	if (propertyName.equalsIgnoreCase("ServiceType")) {

	    return MetadataElement.SERVICE_TYPE;
	}
	if (propertyName.equalsIgnoreCase("ServiceTypeVersion")) {

	    return MetadataElement.SERVICE_TYPE_VERSION;
	}
	if (propertyName.equalsIgnoreCase("Operation")) {

	    return MetadataElement.OPERATION;
	}
	// ---- operates on data
	if (propertyName.equalsIgnoreCase("CouplingType")) {

	    return MetadataElement.COUPLING_TYPE;
	}
	if (propertyName.equalsIgnoreCase("OperatesOn")) {

	    return MetadataElement.OPERATES_ON;
	}
	if (propertyName.equalsIgnoreCase("OperatesOnIdentifier")) {

	    return MetadataElement.OPERATES_ON_IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("OperatesOnName")) {

	    return MetadataElement.OPERATES_ON_NAME;
	}
	// ---- end of operates on data

	// ADDITIONAL QUERYABLEs
	if (propertyName.equalsIgnoreCase("Attribute")) {

	    return MetadataElement.ATTRIBUTE_DESCRIPTION;
	}
	if (propertyName.equalsIgnoreCase("InstrumentIdentifier")) {

	    return MetadataElement.INSTRUMENT_IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("PlatformIdentifier")) {

	    return MetadataElement.PLATFORM_IDENTIFIER;
	}
	if (propertyName.equalsIgnoreCase("OriginatorOrganisationIdentifier")) {

	    return MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER;
	}

	return null;
    }

    /**
     * @param propertyName
     * @return
     */
    private String removePrefix(String propertyName) {

	if (propertyName.contains(":")) {
	    // removing prefix
	    propertyName = propertyName.substring(propertyName.indexOf(':') + 1, propertyName.length());
	}

	return propertyName;
    }

    /**
     * @param literal
     * @return
     */
    private String removeStars(String literal) {
	if (literal.startsWith("*")) {
	    literal = literal.substring(1, literal.length());
	}
	if (literal.endsWith("*")) {
	    literal = literal.substring(0, literal.length() - 1);
	}
	return literal.trim();
    }
}
