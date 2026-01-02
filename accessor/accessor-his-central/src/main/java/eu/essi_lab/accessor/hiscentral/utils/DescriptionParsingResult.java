package eu.essi_lab.accessor.hiscentral.utils;

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

import eu.essi_lab.model.resource.InterpolationType;

public class DescriptionParsingResult {

    private String fullString;

    public String getFullString() {
	return fullString;
    }

    public InterpolationType getInterpolation() {
	return interpolation;
    }

    public String getTerm() {
	return term;
    }

    public String getRest() {
	return rest;
    }

    private InterpolationType interpolation;
    private String term;
    private String rest;

    public DescriptionParsingResult(String string, InterpolationType interpolation, String term) {
	this.fullString = string;
	this.interpolation = interpolation;
	this.term = term;
	String rest = string;
	if (term != null) {
	    rest = rest.replace(term, "");
	}
	rest = rest.replace("  ", " ");
	rest = rest.trim();

	String direction = findDirection(rest);
	if (direction != null) {
	    rest = rest.replace(direction, "").trim();
	}

	String distance = findDistance(rest);
	if (distance != null) {
	    rest = rest.replace(distance, "").trim();
	}

	this.rest = rest;

    }

    private String findDirection(String rest) {
	String[] directions = new String[] { "da N", "da S", "da W", "da E", "da SW", "da NE", "da NW", "da SE" };
	for (String direction : directions) {
	    if (rest.endsWith(direction)) {
		return direction;
	    }
	}
	return null;

    }

    private String findDistance(String rest) {
	String[] distances = new String[] { "a 2m", "a 10m", "a 0cm", "a 50 cm", "a 180 cm", "a -10 cm", "a 0 cm", "a 10", "a 180cm" };
	for (String distance : distances) {
	    if (rest.endsWith(distance)) {
		return distance;
	    }
	}
	return null;

    }

}
