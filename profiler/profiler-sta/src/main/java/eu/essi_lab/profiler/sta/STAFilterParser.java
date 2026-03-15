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

package eu.essi_lab.profiler.sta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.spatial.SpatialEntity;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;

/**
 * Parses OGC STA $filter expressions into Bond trees. Supports logical operators (and, or),
 * location type filters, and geo.intersects spatial predicates.
 */
public class STAFilterParser {

    private static final Pattern GEO_INTERSECTS_PATTERN = Pattern.compile(
	    "geo\\.intersects\\s*\\(\\s*[^,]+\\s*,\\s*geography\\s*'([^']*(?:''[^']*)*)'\\s*\\)",
	    Pattern.CASE_INSENSITIVE);

    private static final Pattern LOCATION_TYPE_PATTERN = Pattern.compile(
	    "location/(?:geometry/)?type\\s+eq\\s+'([^']*)'",
	    Pattern.CASE_INSENSITIVE);

    /**
     * Parses the filter string and returns a Bond, or null if the filter is empty/unparseable.
     */
    public static Bond parse(String filter) {
	if (filter == null || filter.isEmpty()) {
	    return null;
	}
	String f = filter.trim();
	return parseExpression(f);
    }

    private static Bond parseExpression(String expr) {
	expr = expr.trim();
	expr = stripOuterParens(expr);

	// Split by top-level " and " (outside parentheses)
	List<String> andParts = splitByTopLevel(expr, " and ");
	if (andParts.size() > 1) {
	    List<Bond> bonds = new ArrayList<>();
	    for (String part : andParts) {
		Bond b = parseExpression(part);
		if (b != null) {
		    bonds.add(b);
		}
	    }
	    return bonds.size() == 0 ? null : bonds.size() == 1 ? bonds.get(0) : BondFactory.createAndBond(bonds);
	}

	// Split by top-level " or "
	List<String> orParts = splitByTopLevel(expr, " or ");
	if (orParts.size() > 1) {
	    List<Bond> bonds = new ArrayList<>();
	    for (String part : orParts) {
		Bond b = parseExpression(part);
		if (b != null) {
		    bonds.add(b);
		}
	    }
	    return bonds.size() == 0 ? null : bonds.size() == 1 ? bonds.get(0) : BondFactory.createOrBond(bonds);
	}

	return parseAtomicExpression(expr);
    }

    private static String stripOuterParens(String s) {
	s = s.trim();
	while (s.startsWith("(") && s.endsWith(")")) {
	    int depth = 0;
	    boolean balanced = true;
	    for (int i = 1; i < s.length() - 1; i++) {
		char c = s.charAt(i);
		if (c == '(') {
		    depth++;
		} else if (c == ')') {
		    depth--;
		    if (depth < 0) {
			balanced = false;
			break;
		    }
		}
	    }
	    if (balanced && depth == 0) {
		s = s.substring(1, s.length() - 1).trim();
	    } else {
		break;
	    }
	}
	return s;
    }

    private static List<String> splitByTopLevel(String expr, String delimiter) {
	List<String> result = new ArrayList<>();
	int depth = 0;
	int start = 0;
	String lower = expr.toLowerCase();
	String delimLower = delimiter.toLowerCase();
	int i = 0;
	while (i < expr.length()) {
	    char c = expr.charAt(i);
	    if (c == '(') {
		depth++;
		i++;
		continue;
	    }
	    if (c == ')') {
		depth--;
		i++;
		continue;
	    }
	    if (c == '\'') {
		i++;
		while (i < expr.length()) {
		    char ch = expr.charAt(i);
		    if (ch == '\'' && (i + 1 >= expr.length() || expr.charAt(i + 1) != '\'')) {
			i++;
			break;
		    }
		    if (ch == '\'' && i + 1 < expr.length() && expr.charAt(i + 1) == '\'') {
			i += 2;
			continue;
		    }
		    i++;
		}
		continue;
	    }
	    if (depth == 0 && i <= expr.length() - delimLower.length()
		    && lower.startsWith(delimLower, i)) {
		result.add(expr.substring(start, i).trim());
		i += delimLower.length();
		start = i;
		continue;
	    }
	    i++;
	}
	if (start < expr.length()) {
	    result.add(expr.substring(start).trim());
	}
	return result;
    }

    private static Bond parseAtomicExpression(String expr) {
	expr = expr.trim();
	expr = stripOuterParens(expr);

	// geo.intersects(location, geography'Polygon ((...))')
	Matcher geoMatcher = GEO_INTERSECTS_PATTERN.matcher(expr);
	if (geoMatcher.find()) {
	    String wktLiteral = geoMatcher.group(1).replace("''", "'");
	    if (!wktLiteral.isEmpty()) {
		SpatialEntity entity = parseGeometryToSpatialEntity(wktLiteral);
		if (entity != null) {
		    return BondFactory.createSpatialEntityBond(BondOperator.INTERSECTS, entity);
		}
	    }
	}

	// location/type eq 'Point' or location/geometry/type eq 'Point'
	Matcher locMatcher = LOCATION_TYPE_PATTERN.matcher(expr);
	boolean hasLocationType = false;
	boolean isPoint = false;
	while (locMatcher.find()) {
	    hasLocationType = true;
	    if ("Point".equalsIgnoreCase(locMatcher.group(1).trim())) {
		isPoint = true;
	    }
	}
	if (hasLocationType) {
	    return isPoint ? BondFactory.createIsTimeSeriesBond(true) : BondFactory.getFalseBond();
	}

	return null;
    }

    /**
     * Parses WKT geometry. For rectangle polygons, returns SpatialExtent (bbox) for better
     * index support; otherwise returns WKT.
     */
    private static SpatialEntity parseGeometryToSpatialEntity(String wkt) {
	String w = wkt.trim();
	if (!w.regionMatches(true, 0, "POLYGON", 0, 7)) {
	    return SpatialEntity.of(wkt);
	}
	// Extract coordinates from Polygon ((x1 y1, x2 y2, ...))
	int start = w.indexOf('(');
	if (start < 0) {
	    return SpatialEntity.of(wkt);
	}
	start = w.indexOf('(', start + 1);
	if (start < 0) {
	    return SpatialEntity.of(wkt);
	}
	int end = w.lastIndexOf(')');
	if (end <= start) {
	    return SpatialEntity.of(wkt);
	}
	String coords = w.substring(start + 1, end).trim();
	coords = coords.replace(")","").trim();
	String[] points = coords.split(",\\s*");
	if (points.length < 4 || points.length > 5) {
	    return SpatialEntity.of(wkt);
	}
	double minX = Double.MAX_VALUE;
	double maxX = -Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxY = -Double.MAX_VALUE;
	try {
	    for (String pt : points) {
		String[] xy = pt.trim().split("\\s+");
		if (xy.length < 2) {
		    return SpatialEntity.of(wkt);
		}
		double x = Double.parseDouble(xy[0]);
		double y = Double.parseDouble(xy[1]);
		minX = Math.min(minX, x);
		maxX = Math.max(maxX, x);
		minY = Math.min(minY, y);
		maxY = Math.max(maxY, y);
	    }
	    // WKT uses (lon lat) for geographic; SpatialExtent = (south, west, north, east)
	    return new SpatialExtent(minY, minX, maxY, maxX);
	} catch (NumberFormatException e) {
	    return SpatialEntity.of(wkt);
	}
    }
}
