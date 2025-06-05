package eu.essi_lab.messages.bond;

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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * A factory to create custom {@link Bond}s<br>
 * <br>
 * <b>Name conventions</b><br>
 * <br>
 * The names of the {@link Queryable}s used to build the custom {@link QueryableBond} MUST comply to the conventions specified in the {@link
 * #checkName(String)} method
 *
 * @author Fabrizio
 */
public class CustomBondFactory {

    private static final String UNSUPPORTED_BINARY_OP = "Unsupported binary operator: ";

    private CustomBondFactory() {
	//force static usage
    }

    /**
     * Creates a custom bond applied on the given string <code>value</code>.<br> The supplied <code>queryable</code> must have one of the
     * following {@link ContentType}:
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
     * @param queryable the target property
     * @param operator the operator to apply on the target property
     * @param value the value to use with the operator
     * @return the new custom bond
     * @throws IllegalArgumentException if:
     * <ul>
     * <li>the supplied <code>operator</code> is not supported</li>
     * <li>the supplied <code>queryable</code> has invalid {@link ContentType}</li>
     * <li>the {@link #checkName(String)} method fails</li>
     * </ul>
     */
    public static QueryableBond<String> createCustomBond(Queryable queryable, BondOperator operator, String value) {

	checkName(queryable.getName());

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.TEXT_SEARCH && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException(UNSUPPORTED_BINARY_OP + operator);
	}

	if (queryable.getContentType() != ContentType.TEXTUAL && //
		queryable.getContentType() != ContentType.ISO8601_DATE && //
		queryable.getContentType() != ContentType.ISO8601_DATE_TIME) {//

	    throw new IllegalArgumentException("Unsupported content type: " + queryable.getContentType());
	}

	QueryableBond<String> bond = new QueryableBond<>();
	bond.setOperator(operator);
	bond.setProperty(queryable);
	bond.setPropertyValue(value);

	return bond;
    }

    /**
     * Creates a custom bond applied on the given double <code>value</code>.<br> The supplied <code>queryable</code> must have the {@link
     * ContentType#DOUBLE}.<br> The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     *
     * @param queryable the target property
     * @param operator the operator to apply on the target property
     * @param value the value to use with the operator
     * @return the new custom bond
     * @throws IllegalArgumentException if:
     * <ul>
     * <li>the supplied <code>operator</code> is not supported</li>
     * <li>the supplied <code>queryable</code> has not {@link ContentType#DOUBLE}</li>
     * <li>the {@link #checkName(String)} method fails</li>
     * </ul>
     */
    public static QueryableBond<String> createCustomBond(Queryable queryable, BondOperator operator, double value) {

	checkName(queryable.getName());

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException(UNSUPPORTED_BINARY_OP + operator);
	}

	if (queryable.getContentType() != ContentType.DOUBLE) {

	    throw new IllegalArgumentException("Not double content type: " + queryable.getContentType());
	}

	QueryableBond<String> bond = new QueryableBond<>();
	bond.setOperator(operator);
	bond.setProperty(queryable);
	bond.setPropertyValue(String.valueOf(value));

	return bond;
    }

    /**
     * Creates a custom bond applied on the given integer <code>value</code>.<br> The supplied <code>queryable</code> must have the {@link
     * ContentType#INTEGER}.<br> The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     *
     * @param queryable the target property
     * @param operator the operator to apply on the target property
     * @param value the value to use with the operator
     * @return the new custom bond
     * @throws IllegalArgumentException if:
     * <ul>
     * <li>the supplied <code>operator</code> is not supported</li>
     * <li>the supplied <code>queryable</code> has not {@link ContentType#INTEGER}</li>
     * <li>the {@link #checkName(String)} method fails</li>
     * </ul>
     * @see Queryable#getContentType()
     */
    public static QueryableBond<String> createCustomBond(Queryable queryable, BondOperator operator, int value) {

	checkName(queryable.getName());

	if (operator != BondOperator.EQUAL && //
		operator != BondOperator.NOT_EQUAL && //
		operator != BondOperator.LESS && //
		operator != BondOperator.LESS_OR_EQUAL && //
		operator != BondOperator.GREATER && //
		operator != BondOperator.GREATER_OR_EQUAL) {

	    throw new IllegalArgumentException(UNSUPPORTED_BINARY_OP + operator);
	}

	if (queryable.getContentType() != ContentType.INTEGER) {

	    throw new IllegalArgumentException("Not integer content type: " + queryable.getContentType());
	}

	QueryableBond<String> bond = new QueryableBond<>();
	bond.setOperator(operator);
	bond.setProperty(queryable);
	bond.setPropertyValue(String.valueOf(value));

	return bond;
    }

    /**
     * Creates a custom bond applied applied on the given boolean <code>value</code> with the {@link BondOperator#EQUAL} operator.<br> The
     * supplied <code>queryable</code> must have the {@link ContentType#BOOLEAN}
     *
     * @param queryable the target property
     * @param value the value to use with the operator
     * @throws IllegalArgumentException if:
     * <ul>
     * <li>the supplied <code>queryable</code> has not {@link ContentType#BOOLEAN}</li>
     * <li>the {@link #checkName(String)} method fails</li>
     * </ul>
     * @see Queryable#getContentType()
     */
    public static QueryableBond<String> createCustomBond(Queryable queryable, boolean value) {

	checkName(queryable.getName());

	if (queryable.getContentType() != ContentType.BOOLEAN) {

	    throw new IllegalArgumentException("Not boolean content type: " + queryable.getContentType());
	}

	QueryableBond<String> bond = new QueryableBond<>();
	bond.setOperator(BondOperator.EQUAL);
	bond.setProperty(queryable);
	bond.setPropertyValue(String.valueOf(value));

	return bond;
    }

    /**
     * Creates a custom bond using {@link BondOperator#NOT_EXISTS} as operator
     *
     * @param queryable the target property
     * @return
     * @throws IllegalArgumentException if the {@link #checkName(String)} method fails
     */
    public static QueryableBond<String> createMissingCustomBond(Queryable queryable) {

	checkName(queryable.getName());

	return createCustomBond(queryable, BondOperator.NOT_EXISTS, null);
    }

    /**
     * Utility method which prints the names of all the {@link MetadataElement} or {@link ResourceProperty} defined in the GI-suite
     */
    public static void printNames() {

	List<String> mes = Arrays.asList(MetadataElement.values()).//
		stream().//
		map(MetadataElement::getName).//
		collect(Collectors.toList());

	List<String> rps = Arrays.asList(ResourceProperty.values()).//
		stream().//
		map(ResourceProperty::getName).//
		collect(Collectors.toList());

	mes.addAll(rps);
	mes.forEach(n -> GSLoggerFactory.getLogger(CustomBondFactory.class).trace("{}", n));
    }

    /**
     * Checks whether or not the given <code>name</code> complies with the following conventions:
     * <ul>
     * <li>it matches the regular expression [a-zA-Z]</li>
     * <li>the only allowed exception to the above rule is the character '_'</li>
     * <li>it is unique. To avoid name clash with {@link MetadataElement} or {@link ResourceProperty} names,
     * check the names list with the {@link #printNames()} method</li>
     * </ul>
     *
     * @param n
     */
    public static void checkName(String n) {

	String name = n.replace("_", "");
	boolean allLetters = Pattern.matches("[a-zA-Z]+", name);

	if (!allLetters) {

	    throw new IllegalArgumentException("Given queryable name " + n + " contains invalid characters");
	}

	List<String> mes = Arrays.asList(MetadataElement.values()).//
		stream().//
		map(e -> e.getName()).//
		collect(Collectors.toList());

	List<String> rps = Arrays.asList(ResourceProperty.values()).//
		stream().//
		map(e -> e.getName()).//
		collect(Collectors.toList());

	mes.addAll(rps);

	if (rps.//
		stream().//
		anyMatch(e -> e.equals(n))) {

	    throw new IllegalArgumentException("A queryable with given name " + n + " is already defined");
	}
    }
}
