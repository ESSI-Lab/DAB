/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;

/**
 * @author Fabrizio
 */
public class Shape {

    /**
     * 
     */
    private static final Double TOL = Math.pow(10, -4); // 11 meters

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final WKTReader READER = new WKTReader(GEOMETRY_FACTORY);

    public static String SHAPE = "shape";
    public static String AREA = "area";

    /**
     * @param bbox
     * @return
     */
    public static Optional<Shape> of(BoundingBox bbox) {

	Shape shape = new Shape(bbox);

	if (!shape.empty) {

	    return Optional.of(shape);
	}

	return Optional.empty();
    }

    /**
     * @param polygons
     * @return
     */
    public static Optional<Shape> of(BoundingPolygon polygon) {

	Shape shape = new Shape(polygon);

	if (!shape.empty) {

	    return Optional.of(shape);
	}

	return Optional.empty();
    }

    private boolean empty;
    private JSONObject objectBox;

    /**
     * @param shape
     * @param area
     * @throws ParseException
     */
    private Shape(BoundingBox bbox) {

	objectBox = new JSONObject();

	try {

	    Optional<String> shape = asShape(bbox);

	    if (shape.isPresent()) {

		Geometry geometry = READER.read(shape.get());

		double area = geometry.getArea();

		String _shape = shape.get();

		// OpenSearch do not likes the inner points with parentheses
		if (_shape.startsWith("MULTIPOINT")) {

		    _shape = _shape.replace("(", "");
		    _shape = _shape.replace(")", "");
		    _shape = _shape.replace("MULTIPOINT ", "MULTIPOINT (");
		    _shape = _shape + ")";
		}

		objectBox.put(SHAPE, _shape);
		objectBox.put(AREA, area);

	    } else {

		empty = true;
	    }

	} catch (ParseException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    /**
     * @param polygons
     */
    private Shape(BoundingPolygon polygon) {

	objectBox = new JSONObject();

	String shape = null;

	List<List<Double>> multiPoints = polygon.getMultiPoints();

	if (!multiPoints.isEmpty()) {

	    List<Double> first = multiPoints.get(0);

	    List<Double> last = multiPoints.get(multiPoints.size() - 1);

	    String closing = "";

	    if (first.equals(last)) {

		shape = "POLYGON ((";
		closing = "))";

	    } else {

		shape = "LINESTRING (";
		closing = ")";
	    }

	    for (int i = 0; i < multiPoints.size(); i++) {

		List<Double> list = multiPoints.get(i);

		String collect = list.stream().//
			map(v -> String.valueOf(v)).//
			collect(Collectors.joining(" "));

		shape += collect;

		if (i < multiPoints.size() - 1) {

		    shape += ", ";
		}
	    }

	    shape += closing;

	    try {

		Geometry geometry = READER.read(shape);

		double area = geometry.getArea();

		objectBox.put(SHAPE, shape);
		objectBox.put(AREA, area);

	    } catch (ParseException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	} else {

	    empty = true;
	}
    }

    /**
     * @return
     */
    public String getShape() {

	return objectBox.getString(SHAPE);
    }

    /**
     * @return
     */
    public double getArea() {

	return objectBox.getDouble(AREA);
    }

    @Override
    public String toString() {

	return "shape: " + getShape() + "\n" + "area: " + getArea();
    }

    /**
     * @param cardinalValues
     * @param single
     * @return
     */
    private static Optional<String> asPoint(CardinalValues cardinalValues) {

	double e = Double.parseDouble(cardinalValues.getEast());
	double w = Double.parseDouble(cardinalValues.getWest());
	double s = Double.parseDouble(cardinalValues.getSouth());
	double n = Double.parseDouble(cardinalValues.getNorth());

	String ws = w + " " + s;

	if (e > (180 + TOL)) {

	    e = e - 180;
	    w = w - 180;
	}

	if (n <= 90 && s >= -90 && e >= -180 && e <= 180 && w <= 180 && w >= -180) {

	    if (n >= s) {

		boolean snEqual = (Math.abs(n - s) < TOL);
		boolean weEqual = (Math.abs(w - e) < TOL);

		if (snEqual && weEqual) {

		    String shape = "POINT (" + ws + ")";
		    return Optional.of(shape);
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * @param cardinalValues
     * @param single
     * @return
     */
    private static Optional<String> asPolygon(CardinalValues cardinalValues) {

	double e = Double.parseDouble(cardinalValues.getEast());
	double w = Double.parseDouble(cardinalValues.getWest());
	double s = Double.parseDouble(cardinalValues.getSouth());
	double n = Double.parseDouble(cardinalValues.getNorth());

	String es = e + " " + s;
	String ws = w + " " + s;
	String en = e + " " + n;
	String wn = w + " " + n;

	if (e > (180 + TOL)) {

	    e = e - 180;
	    w = w - 180;
	}

	if (n <= 90 && s >= -90 && e >= -180 && e <= 180 && w <= 180 && w >= -180) {

	    if (n >= s) {

		boolean snEqual = (Math.abs(n - s) < TOL);
		boolean weEqual = (Math.abs(w - e) < TOL);

		if (snEqual && weEqual) {

		    return Optional.empty();

		} else if (!snEqual && !weEqual) {

		    String shape = "POLYGON ((" + ws + ", " + wn + ", " + en + ", " + es + ", " + ws + "))";
		    return Optional.of(shape);

		} else {
		    if (snEqual && !weEqual) {
			// line: let's give a width
			double length = Math.abs(w - e);
			double width = length / 10.0;
			s = s - width;
			n = n + width;
		    } else if (!snEqual && weEqual) {
			// line: let's give a width
			double length = Math.abs(s - n);
			double width = length / 10.0;
			w = w - width;
			e = e + width;
		    }

		    es = "180 " + s;
		    ws = "-180 " + s;
		    en = "180 " + n;
		    wn = "-180 " + n;

		    String shape = "POLYGON ((" + ws + ", " + wn + ", " + en + ", " + es + ", " + ws + "))";

		    return Optional.of(shape);
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * @param bbox
     * @return
     */
    private static Optional<String> asShape(BoundingBox bbox) {

	List<CardinalValues> cardinalValues = bbox.getCardinalValues();

	if (cardinalValues.size() == 1) {

	    //
	    // POLYGON
	    //
	    Optional<String> polygon = asPolygon(cardinalValues.get(0));

	    if (polygon.isPresent()) {

		return polygon;
	    }

	    //
	    // POINT
	    //
	    return asPoint(cardinalValues.get(0));

	} else {

	    String shape = null;

	    StringBuilder shapeBuilder = new StringBuilder();

	    boolean polygons = false;
	    boolean points = false;

	    for (int i = 0; i < cardinalValues.size(); i++) {

		CardinalValues val = cardinalValues.get(i);

		Optional<String> polygon = asPolygon(val);
		if (polygon.isPresent()) {

		    shapeBuilder.append(polygon.get());

		    polygons = true;
		}

		Optional<String> point = asPoint(val);
		if (point.isPresent()) {

		    shapeBuilder.append(point.get());

		    points = true;
		}

		if (i < cardinalValues.size() - 1) {

		    shapeBuilder.append(", ");
		}
	    }

	    //
	    // MULTYPOLYGON
	    //
	    if (polygons && !points) {

		shape = shapeBuilder.toString().replace("POLYGON ", "");

		shape = "MULTIPOLYGON (" + shape;

		//
		// MULTIPOINT
		//
	    } else if (!polygons && points) {

		shape = shapeBuilder.toString().replace("POINT ", "");

		shape = "MULTIPOINT (" + shape;

	    } else if (polygons && points) {

		//
		// GEOMETRYCOLLECTION
		//

		shape = "GEOMETRYCOLLECTION (" + shapeBuilder.toString();
	    }

	    shape += ")";

	    return Optional.ofNullable(shape);
	}
    }
}