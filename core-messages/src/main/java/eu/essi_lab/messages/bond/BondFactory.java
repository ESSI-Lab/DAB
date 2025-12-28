package eu.essi_lab.messages.bond;

import java.util.*;

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

import java.util.stream.Collectors;

import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.spatial.SpatialEntity;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.ontology.OntologyObjectProperty;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourceType;
import eu.essi_lab.model.resource.composed.ComposedElementItem;

/**
 * A factory to create all types of {@link Bond}s provide by the GI-suite
 * 
 * @author Fabrizio
 */
public class BondFactory {

    private BondFactory() {
    }

    /**
     * @param bonds
     * @param op
     * @return
     */
    public static Optional<Bond> aggregate(List<Bond> bonds, LogicalOperator op) {

	return switch (bonds.size()) {
	case 0 -> Optional.empty();
	case 1 -> Optional.of(bonds.getFirst());
	default -> Optional.of(BondFactory.createLogicalBond(op, bonds));
	};
    }

    /**
     * Creates a view bond according to the view identifier
     * 
     * @param viewIdentifier
     * @return
     */
    public static ViewBond createViewBond(String viewIdentifier) {

	return new ViewBond(viewIdentifier);
    }

    /**
     * Creates a logical bond according to the supplied <code>operator</code>
     * 
     * @param operator
     * @param operands
     * @return
     */
    public static LogicalBond createLogicalBond(LogicalOperator operator, Collection<Bond> operands) {

	switch (operator) {
	case AND:
	    return createAndBond(operands);
	case OR:
	    return createOrBond(operands);
	case NOT:
	    if (operands.isEmpty()) {
		throw new IllegalArgumentException("No operand supplied, one is required");
	    }
	    if (operands.size() > 1) {
		throw new IllegalArgumentException("Only one operand supported for NOT logical bond");
	    }
	    return createNotBond(operands.iterator().next());
	}

	return null;
    }

    /**
     * Creates a conjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.size()</code> < 2
     */
    public static LogicalBond createAndBond(Collection<Bond> operands) {

	if (operands.size() < 2) {
	    throw new IllegalArgumentException("Use at least 2 operands");
	}

	return new LogicalBond(LogicalOperator.AND, operands);
    }

    /**
     * Creates a conjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.length</code> < 2
     */
    public static LogicalBond createAndBond(Bond... operands) {

	if (operands.length < 2) {
	    throw new IllegalArgumentException("Use at least 2 operands");
	}

	return new LogicalBond(LogicalOperator.AND, operands);
    }

    /**
     * Creates a conjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.size()</code> < 2
     */
    public static LogicalBond createAndBond(List<Bond> operands) {

	if (operands.size() < 2) {
	    throw new IllegalArgumentException("Use at least 2 operands");
	}

	return new LogicalBond(LogicalOperator.AND, operands.toArray(new Bond[] {}));
    }

    /**
     * Creates an empty conjunction of {@link Bond}s
     * 
     * @return
     */
    public static LogicalBond createAndBond() {

	return new LogicalBond(LogicalOperator.AND);
    }

    /**
     * Creates a disjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.size()</code> < 2
     */
    public static LogicalBond createOrBond(Collection<Bond> operands) {

	return new LogicalBond(LogicalOperator.OR, operands);
    }

    /**
     * Creates a disjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.length</code> < 2
     */
    public static LogicalBond createOrBond(Bond... operands) {

	if (operands.length < 2) {
	    throw new IllegalArgumentException("Use at least 2 operands");
	}

	return new LogicalBond(LogicalOperator.OR, operands);
    }

    /**
     * Creates a disjunction of the supplied <code>operands</code>
     * 
     * @param operands
     * @return
     * @throws IllegalArgumentException if <code>operands.size()</code> < 2
     */
    public static LogicalBond createOrBond(List<Bond> operands) {

	if (operands.size() < 2) {
	    throw new IllegalArgumentException("Use at least 2 operands");
	}

	return new LogicalBond(LogicalOperator.OR, operands.toArray(new Bond[] {}));
    }

    /**
     * Creates an empty disjunction of {@link Bond}s
     * 
     * @return
     */
    public static LogicalBond createOrBond() {

	return new LogicalBond(LogicalOperator.OR);
    }

    /**
     * Creates a negation of the supplied <code>operand</code>
     * 
     * @param operand
     * @return
     */
    public static LogicalBond createNotBond(Bond operand) {

	return new LogicalBond(LogicalOperator.NOT, operand);
    }

    /**
     * @param operator
     * @param property
     * @param value
     * @return
     */
    public static OntologyPropertyBond createOntologyPropertyBond(BondOperator operator, OntologyObjectProperty property, String value) {

	return new OntologyPropertyBond(operator, property, value);
    }

    /**
     * Creates a {@link SimpleValueBond} using {@link BondOperator#NOT_EXISTS} as operator
     * 
     * @param element
     * @return
     */
    public static SimpleValueBond createMissingSimpleValueBond(MetadataElement element) {

	return new SimpleValueBond(BondOperator.NOT_EXISTS, element, null);
    }

    /**
     * Creates a {@link ResourcePropertyBond} using {@link BondOperator#NOT_EXISTS} as operator.
     * 
     * @param property
     * @return
     */
    public static ResourcePropertyBond createMissingResourcePropertyBond(ResourceProperty property) throws IllegalArgumentException {

	return new ResourcePropertyBond(BondOperator.NOT_EXISTS, property);
    }

    /**
     * Creates a {@link SimpleValueBond} using {@link BondOperator#EXISTS} as operator
     * 
     * @param element
     * @return
     */
    public static SimpleValueBond createExistsSimpleValueBond(MetadataElement element) {

	return new SimpleValueBond(BondOperator.EXISTS, element, null);
    }

    /**
     * Creates a {@link ResourcePropertyBond} using {@link BondOperator#EXISTS} as operator.
     * 
     * @param property
     * @return
     */
    public static ResourcePropertyBond createExistsResourcePropertyBond(ResourceProperty property) throws IllegalArgumentException {

	return new ResourcePropertyBond(BondOperator.EXISTS, property);
    }

    /**
     * Creates a simple value bond applied on the given string <code>value</code>.<br>
     * The supplied <code>element</code> must have one of the following {@link ContentType}:
     * <ul>
     * <li>{@link ContentType#TEXTUAL}</li>
     * <li>{@link ContentType#ISO8601_DATE}</li>
     * <li>{@link ContentType#ISO8601_DATE_TIME}</li>
     * </ul>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#TEXT_SEARCH}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has invalid {@link ContentType}
     */
    public static SimpleValueBond createSimpleValueBond(BondOperator operator, MetadataElement element, String value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.TEXT_SEARCH && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.TEXTUAL && //
		element.getContentType() != ContentType.ISO8601_DATE && //
		element.getContentType() != ContentType.ISO8601_DATE_TIME) {//

	    throw new IllegalArgumentException("Unsupported content type: " + element.getContentType());
	}

	return new SimpleValueBond(operator, element, value);
    }

    /**
     * @param keywords
     * @param op
     * @return
     */
    public static Bond createKeywordListBond(List<String> keywords, LogicalOperator op) {

	List<Bond> resList = keywords.stream().//
		map(kwd -> BondFactory.createOrBond(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, kwd), //
			BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, kwd)))
		.collect(Collectors.toList());

	return resList.size() == 1 ? resList.getFirst() : switch (op) {
	case OR -> BondFactory.createOrBond(resList);
	case AND -> BondFactory.createAndBond(resList);
	case NOT -> throw new UnsupportedOperationException("Unimplemented case: " + op);
	};
    }

    /**
     * Creates a statistical element bond applied on the given string <code>value</code>.<br>
     * The supplied <code>element</code> must have one of the following {@link ContentType}:
     * <ul>
     * <li>{@link ContentType#TEXTUAL}</li>
     * <li>{@link ContentType#ISO8601_DATE}</li>
     * <li>{@link ContentType#ISO8601_DATE_TIME}</li>
     * </ul>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#TEXT_SEARCH}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has invalid {@link ContentType}
     */
    public static RuntimeInfoElementBond createRuntimeInfoElementBond(BondOperator operator, RuntimeInfoElement element, String value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.TEXT_SEARCH && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.TEXTUAL && //
		element.getContentType() != ContentType.ISO8601_DATE && //
		element.getContentType() != ContentType.ISO8601_DATE_TIME) {//

	    throw new IllegalArgumentException("Unsupported content type: " + element.getContentType());
	}

	return new RuntimeInfoElementBond(operator, element, value);
    }

    /**
     * Creates a statistical element bond applied on the given double <code>value</code>.<br>
     * The supplied <code>element</code> must have the {@link ContentType#DOUBLE}.<br>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has not {@link ContentType#DOUBLE}
     */
    public static RuntimeInfoElementBond createRuntimeInfoElementBond(BondOperator operator, RuntimeInfoElement element, double value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.DOUBLE) {

	    throw new IllegalArgumentException("Not double content type: " + element.getContentType());
	}

	return new RuntimeInfoElementBond(operator, element, String.valueOf(value));
    }

    /**
     * Creates a statistical element bond applied on the given double <code>value</code>.<br>
     * The supplied <code>element</code> must have the {@link ContentType#LONG}.<br>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has not {@link ContentType#LONG}
     */
    public static RuntimeInfoElementBond createRuntimeInfoElementBond(BondOperator operator, RuntimeInfoElement element, long value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.LONG) {

	    throw new IllegalArgumentException("Not long content type: " + element.getContentType());
	}

	return new RuntimeInfoElementBond(operator, element, String.valueOf(value));
    }

    /**
     * Creates a statistical element bond applied on the given double <code>value</code>.<br>
     * The supplied <code>element</code> must have the {@link ContentType#INTEGER}.<br>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has not {@link ContentType#INTEGER}
     */
    public static RuntimeInfoElementBond createRuntimeInfoElementBond(BondOperator operator, RuntimeInfoElement element, int value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.INTEGER) {

	    throw new IllegalArgumentException("Not integer content type: " + element.getContentType());
	}

	return new RuntimeInfoElementBond(operator, element, String.valueOf(value));
    }

    /**
     * Creates a simple value bond applied on the given double <code>value</code>.<br>
     * The supplied <code>element</code> must have the {@link ContentType#DOUBLE}.<br>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has not {@link ContentType#DOUBLE}
     */
    public static SimpleValueBond createSimpleValueBond(BondOperator operator, MetadataElement element, double value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.DOUBLE) {

	    throw new IllegalArgumentException("Not double content type: " + element.getContentType());
	}

	return new SimpleValueBond(operator, element, value);
    }

    /**
     * Creates a simple value bond applied on the given int <code>value</code>.
     * The supplied <code>element</code> must have the {@link ContentType#INTEGER}.
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not supported or
     *         the supplied <code>element</code> has not {@link ContentType#DOUBLE}
     */
    public static SimpleValueBond createSimpleValueBond(BondOperator operator, MetadataElement element, int value)
	    throws IllegalArgumentException {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported binary operator: " + operator);
	}

	if (element.getContentType() != ContentType.INTEGER) {

	    throw new IllegalArgumentException("Not integer content type: " + element.getContentType());
	}

	return new SimpleValueBond(operator, element, value);
    }

    /**
     * @param operator
     * @param element
     * @param item
     * @return
     * @throws IllegalArgumentException
     */
    public static ComposedElementBond createComposedElementBond(//
	    BondOperator operator, //
	    MetadataElement element, //
	    ComposedElementItem item) throws IllegalArgumentException {

	return createComposedElementBond(operator, LogicalOperator.AND, element, Collections.singletonList(item));
    }

    /**
     * @param operator
     * @param logicalOp
     * @param element
     * @param value
     * @return
     * @throws IllegalArgumentException
     */
    public static ComposedElementBond createComposedElementBond(//
	    BondOperator operator, //
	    LogicalOperator logicalOp, //
	    MetadataElement element, //
	    List<ComposedElementItem> value) throws IllegalArgumentException {

	if (!element.hasComposedElement()) {

	    throw new IllegalArgumentException("Composed element missing");
	}

	if (logicalOp == LogicalOperator.NOT) {

	    throw new IllegalArgumentException("NOT logical operator not suported");
	}

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL && //
		operator != BondOperator.TEXT_SEARCH) {

	    throw new IllegalArgumentException("Unsupported operator: " + operator);
	}

	return new ComposedElementBond(operator, logicalOp, element, value.toArray(new ComposedElementItem[] {}));
    }

    /**
     * Creates a simple value bond applied on the given boolean <code>value</code> with the {@link BondOperator#EQUAL}
     * operator.<br>
     * The supplied <code>element</code> must have the {@link ContentType#BOOLEAN}.
     * 
     * @see Queryable#getContentType()
     * @param element
     * @param value
     * @throws IllegalArgumentException if the supplied <code>element</code> has not {@link ContentType#BOOLEAN}
     */
    public static SimpleValueBond createSimpleValueBond(MetadataElement element, boolean value) throws IllegalArgumentException {

	if (element.getContentType() != ContentType.BOOLEAN) {

	    throw new IllegalArgumentException("Not boolean content type: " + element.getContentType());
	}

	return new SimpleValueBond(element, value);
    }

    /**
     * Supported operators are spatial operators:
     * <ul>
     * <li>{@link BondOperator#BBOX}</li>
     * <li>{@link BondOperator#CONTAINS}</li>
     * <li>{@link BondOperator#INTERSECTS}</li>
     * <li>{@link BondOperator#DISJOINT}</li>
     * </ul>
     * 
     * @param operator
     * @param extent
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     */
    public static SpatialBond createSpatialEntityBond(BondOperator operator, SpatialEntity extent) {

	if (operator != BondOperator.BBOX && operator != BondOperator.CONTAINS && operator != BondOperator.INTERSECTS
		&& operator != BondOperator.DISJOINT) {

	    throw new IllegalArgumentException("Not spatial operator: " + operator);
	}

	return new SpatialBond(operator, extent);
    }

    // ------------------------------------------------------
    //
    // RESOURCE PROPERTIES BONDS
    //
    // ------------------------------------------------------

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param quality
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createAccessQualityBond(BondOperator operator, int quality) {

	return createResourcePropertyBond(operator, ResourceProperty.ACCESS_QUALITY, String.valueOf(quality));
    }

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param quality
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createEssentialVarsQualityBond(BondOperator operator, int quality) {

	return createResourcePropertyBond(operator, ResourceProperty.ESSENTIAL_VARS_QUALITY, String.valueOf(quality));
    }

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param timeStamp
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createTestTimeStampBond(BondOperator operator, String timeStamp) {

	return createResourcePropertyBond(operator, ResourceProperty.TEST_TIME_STAMP, String.valueOf(timeStamp));
    }

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param time
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createDownloadTimeBond(BondOperator operator, long time) {

	return createResourcePropertyBond(operator, ResourceProperty.DOWNLOAD_TIME, String.valueOf(time));
    }

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param time
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createExecutionTimeBond(BondOperator operator, long time) {

	return createResourcePropertyBond(operator, ResourceProperty.EXECUTION_TIME, String.valueOf(time));
    }

    /**
     * Supported operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param quality
     * @throws IllegalArgumentException if the supplied <code>operator</code> is not accepted
     * @return
     */
    public static ResourcePropertyBond createMetadataQualityBond(BondOperator operator, int quality) {

	return createResourcePropertyBond(operator, ResourceProperty.METADATA_QUALITY, String.valueOf(quality));
    }

    /**
     * @param level
     * @return
     */
    public static ResourcePropertyBond createComplianceLevelBond(String level) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.COMPLIANCE_LEVEL, level);
    }

    /**
     * @param identifier
     * @return
     */
    public static ResourcePropertyBond createOAIPMHHeaderIdentifierBond(String identifier) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.OAI_PMH_HEADER_ID, identifier);
    }

    /**
     * Applies this bond by comparing the supplied <code>resourceTimeStamp</code> using the supplied
     * <code>operator</code> which must be one of the following binary operator:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param resourceTimeStamp
     */
    public static ResourcePropertyBond createResourceTimeStampBond(BondOperator operator, String resourceTimeStamp) {

	return createResourcePropertyBond(operator, ResourceProperty.RESOURCE_TIME_STAMP, resourceTimeStamp);
    }

    /**
     * Applies this bond to resources which satisfy the following conditions:
     * <ul>
     * <li>they have the minimum or maximum resource time stamp</li>
     * <li>they origin from a {@link GSSource} with the supplied <code>sourceIdentifier</code></li>
     * </ul>
     * The supplied <code>operator</code> <b>MUST</b> be {@link BondOperator#MIN} or {@link BondOperator#MAX}
     * 
     * @param sourceIdentifier the identifier of an existent {@link GSSource}
     * @param operator {@link BondOperator#MIN} or {@link BondOperator#MAX}
     */
    public static ResourcePropertyBond createMinMaxResourceTimeStampBond(BondOperator operator, String sourceIdentifier)
	    throws IllegalArgumentException {

	if (operator != BondOperator.MAX && operator != BondOperator.MIN) {
	    throw new IllegalArgumentException("Unsupported operator: " + operator);
	}

	return new ResourcePropertyBond(operator, ResourceProperty.RESOURCE_TIME_STAMP, sourceIdentifier);
    }

    /**
     * Applies this bond to resources having the minimum or maximum resource time stamp
     * 
     * @param operator {@link BondOperator#MIN} or {@link BondOperator#MAX}
     */
    public static ResourcePropertyBond createMinMaxResourceTimeStampBond(BondOperator operator) throws IllegalArgumentException {

	return new ResourcePropertyBond(operator, ResourceProperty.RESOURCE_TIME_STAMP, null);
    }

    /**
     * Applies this bond to resources having the minimum or maximum value of the given <code>property</code>
     * 
     * @param operator {@link BondOperator#MIN} or {@link BondOperator#MAX}
     */
    public static ResourcePropertyBond createMinMaxResourcePropertyBond(ResourceProperty property, BondOperator operator)
	    throws IllegalArgumentException {

	return new ResourcePropertyBond(operator, property, null);
    }

    /**
     * @param identifier
     * @return
     */
    public static ResourcePropertyBond createSourceIdentifierBond(String identifier) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.SOURCE_ID, identifier);
    }

    /**
     * @param identifier
     * @return
     */
    public static SimpleValueBond createOnlineIdentifierBond(String identifier) {

	return new SimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, identifier);
    }

    /**
     * @param test
     * @return
     */
    public static ResourcePropertyBond createRecoveryRemovalTokenBond(String token) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.RECOVERY_REMOVAL_TOKEN, token);
    }

    /**
     * @param test
     * @return
     */
    public static ResourcePropertyBond createLastSucceededTestBond(String test) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.SUCCEEDED_TEST, test);
    }

    /**
     * @param identifier
     * @return
     */
    public static ResourcePropertyBond createOriginalIdentifierBond(String identifier) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.ORIGINAL_ID, identifier);
    }

    /**
     * @param identifier
     * @return
     */
    public static ResourcePropertyBond createPrivateIdentifierBond(String identifier) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.PRIVATE_ID, identifier);
    }

    /**
     * @param deleted
     * @return
     */
    public static ResourcePropertyBond createIsDeletedBond(boolean deleted) {

	if (!deleted) {

	    return createMissingResourcePropertyBond(ResourceProperty.IS_DELETED);
	}

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_DELETED, "true");
    }

    /**
     * @param validated
     * @return
     */
    public static ResourcePropertyBond createIsValidatedBond(boolean validated) {

	if (!validated) {

	    return createMissingResourcePropertyBond(ResourceProperty.IS_VALIDATED);
	}

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_VALIDATED, "true");
    }

    /**
     * @param downloadable
     * @return
     */
    public static ResourcePropertyBond createIsDownloadableBond(boolean downloadable) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_DOWNLOADABLE, String.valueOf(downloadable));
    }

    /**
     * @param isTimeSeries
     * @return
     */
    public static ResourcePropertyBond createIsTimeSeriesBond(boolean isTimeSeries) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_TIMESERIES, String.valueOf(isTimeSeries));
    }

    /**
     * @param isEiffelRecord
     * @return
     */
    public static ResourcePropertyBond createIsEiffelRecordBond(boolean isEiffelRecord) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_EIFFEL_RECORD, String.valueOf(isEiffelRecord));
    }

    /**
     * @param isGridBond
     * @return
     */
    public static ResourcePropertyBond createIsGridBond(boolean isGridBond) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_GRID, String.valueOf(isGridBond));
    }

    /**
     * @param isVectorBond
     * @return
     */
    public static ResourcePropertyBond createIsVectorBond(boolean isVectorBond) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_VECTOR, String.valueOf(isVectorBond));
    }

    /**
     * @param isRatingCurve
     * @return
     */
    public static ResourcePropertyBond createIsRatingCurveBond(boolean isRatingCurve) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_RATING_CURVE, String.valueOf(isRatingCurve));
    }

    /**
     * @param executable
     * @return
     */
    public static ResourcePropertyBond createIsExecutableBond(boolean executable) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_EXECUTABLE, String.valueOf(executable));
    }

    /**
     * @param transformble
     * @return
     */
    public static ResourcePropertyBond createIsTransformableBond(boolean transformble) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_TRANSFORMABLE, String.valueOf(transformble));
    }

    /**
     * @param isCompliant
     * @return
     */
    public static ResourcePropertyBond createIsISOCompliantBond(boolean isCompliant) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_ISO_COMPLIANT, String.valueOf(isCompliant));
    }

    /**
     * @param isGDC
     * @return
     */
    public static ResourcePropertyBond createIsGEOSSDataCoreBond(boolean isGDC) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_GEOSS_DATA_CORE, String.valueOf(isGDC));
    }

    /**
     * @param score
     * @return
     */
    public static ResourcePropertyBond createSSCScoreBond(BondOperator operator, int score) {

	return new ResourcePropertyBond(operator, ResourceProperty.SSC_SCORE, String.valueOf(score));
    }

    /**
     * @param type
     * @return
     */
    public static ResourcePropertyBond createResourceTypeBond(ResourceType type) {

	return new ResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.TYPE, type.getType());
    }

    /**
     * @param bond
     * @param property
     * @return
     */
    public static boolean isResourcePropertyBond(Bond bond, ResourceProperty property) {

	return bond instanceof ResourcePropertyBond && //
		((ResourcePropertyBond) bond).getProperty() == property;
    }

    // ------------------------------------------------------
    //
    // RESOURCE PROPERTIES BONDS
    //
    // ------------------------------------------------------

    public static ResourcePropertyBond createResourcePropertyBond(BondOperator operator, ResourceProperty property, String value) {

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.TEXT_SEARCH && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL) {

	    throw new IllegalArgumentException("Unsupported operator: " + operator);
	}

	return new ResourcePropertyBond(operator, property, String.valueOf(value));
    }

}
