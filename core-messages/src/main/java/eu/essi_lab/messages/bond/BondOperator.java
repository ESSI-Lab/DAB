/**
 * 
 */
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

public enum BondOperator {

    TEXT_SEARCH, //
    
    EQUAL("="), //
    NOT_EQUAL("!="), //
    GREATER(">"), //
    LESS("<"), //
    GREATER_OR_EQUAL(">="), //
    LESS_OR_EQUAL("<="), //

    NOT_EXISTS, //
    EXISTS, //
    
    MIN, //
    MAX, //

    INTERSECTS,
    DISJOINT, 
    BBOX, // BBOX is "NOT DISJOINT"
    CONTAINS, // target bbox contains resources bbox
    CONTAINED, // resources bbox contains target bbox
    /**
     * Target bbox intersects resources bboxes only with at least one corner (south, west, east or north) of the
     * resource bboxes, which are not entirely contained in the requested bbox
     */
    INTERSECTS_ANY_POINT_NOT_CONTAINS;

    private String shortRepresentation;

    private BondOperator() {
    }

    private BondOperator(String shortRepresentation) {
	this.shortRepresentation = shortRepresentation;
    }

    public String getShortRepresentation() {
	if (shortRepresentation == null) {
	    return " " + toString() + " ";
	}
	return shortRepresentation;
    }

    /**
     * @return
     */
    public String asMathOperator() {

	return this.shortRepresentation;
    }

    public static BondOperator decode(String operation) {

	if (operation.matches("(?i).*Intersects.*") || operation.matches("(?i).*Overlaps.*")) {
	    return INTERSECTS;
	}
	if (operation.matches("(?i).*Disjoint.*")) {
	    return DISJOINT;
	}
	if (operation.matches("(?i).*Contains.*")) {
	    return CONTAINS;
	}
	if (operation.matches("(?i).*Contained.*")) {
	    return CONTAINED;
	}
	if (operation.matches("(?i).*IntersectsAnyPointNotContains.*")) {
	    return INTERSECTS_ANY_POINT_NOT_CONTAINS;
	}

	throw new IllegalArgumentException("Operation not supported: " + operation);
    }

    /**
     * @param operator
     * @return
     */
    public static BondOperator negate(BondOperator operator) {
	switch (operator) {
	case EQUAL:
	    return NOT_EQUAL;
	case NOT_EQUAL:
	    return EQUAL;
	case LESS:
	    return GREATER_OR_EQUAL;
	case GREATER:
	    return LESS_OR_EQUAL;
	case LESS_OR_EQUAL:
	    return GREATER;
	case GREATER_OR_EQUAL:
	    return LESS;
	default:
	    return null;
	}
    }
}
