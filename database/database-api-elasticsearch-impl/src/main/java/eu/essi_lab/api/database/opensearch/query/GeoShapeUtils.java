package eu.essi_lab.api.database.opensearch.query;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import eu.essi_lab.indexes.SpatialIndexHelper;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.bond.spatial.WKT;

/**
 * @author Fabrizio
 */
public class GeoShapeUtils {

    /**
     * @param extent
     * @return
     */
    public static double getArea(SpatialExtent extent) {

	String north = String.valueOf(extent.getNorth());
	String south = String.valueOf(extent.getSouth());
	String east = String.valueOf(extent.getEast());
	String west = String.valueOf(extent.getWest());

	return SpatialIndexHelper.computeArea(//
		Double.parseDouble(west), //
		Double.parseDouble(east), //
		Double.parseDouble(south), //
		Double.parseDouble(north));

    }

    /**
     * @param wkt
     * @return
     * @throws ParseException
     */
    public static double getArea(WKT wkt) throws ParseException {

	WKTReader reader = new WKTReader();
	Geometry geometry = reader.read(wkt.getValue());

	return geometry.getArea();
    }

    /**
     * @param extent
     * @return
     * @throws Exception
     */
    public static JSONObject convert(SpatialExtent extent) {

	JSONObject object = new JSONObject();

	JSONArray coord = new JSONArray();
	object.put("coordinates", coord);
	object.put("type", "envelope");

	JSONArray nw = new JSONArray();
	nw.put(extent.getWest());
	nw.put(extent.getNorth());

	JSONArray se = new JSONArray();
	se.put(extent.getEast());
	se.put(extent.getSouth());

	coord.put(nw);
	coord.put(se);

	return object;
    }

    /**
     * @param wkt
     * @return
     * @throws Exception
     */
    public static JSONObject convert(WKT wkt) throws ParseException {

	WKTReader reader = new WKTReader();
	Geometry geometry = reader.read(wkt.getValue());

	return convert(geometry);
    }

    /**
     * @param geometry
     * @return
     */
    private static JSONObject convert(Geometry geometry) {

	JSONObject json = new JSONObject();

	switch (geometry) {
	case Point p -> {
	    json.put("type", "point");
	    json.put("coordinates", coordToList(p.getCoordinate()));
	}
	case LineString line -> {
	    json.put("type", "linestring");
	    json.put("coordinates", lineToList(line));
	}
	case Polygon polygon -> {
	    json.put("type", "polygon");
	    json.put("coordinates", polygonToList(polygon));
	}
	case MultiPoint mp -> {
	    json.put("type", "multipoint");
	    json.put("coordinates", multiPointToList(mp));
	}
	case MultiLineString mls -> {
	    json.put("type", "multilinestring");
	    json.put("coordinates", multiLineStringToList(mls));
	}
	case MultiPolygon mpoly -> {
	    json.put("type", "multipolygon");
	    json.put("coordinates", multiPolygonToList(mpoly));
	}

	default -> throw new IllegalArgumentException("Unsupported geometry: " + geometry.getClass().getSimpleName());
	}

	return json;
    }

    /**
     * @param coord
     * @return
     */
    private static List<Double> coordToList(Coordinate coord) {

	return List.of(coord.x, coord.y);
    }

    /**
     * @param line
     * @return
     */
    private static List<List<Double>> lineToList(LineString line) {

	List<List<Double>> coords = new ArrayList<>();
	for (Coordinate c : line.getCoordinates()) {
	    coords.add(coordToList(c));
	}
	return coords;
    }

    /**
     * @param polygon
     * @return
     */
    private static List<List<List<Double>>> polygonToList(Polygon polygon) {

	List<List<List<Double>>> poly = new ArrayList<>();
	poly.add(lineToList(polygon.getExteriorRing()));
	for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
	    poly.add(lineToList(polygon.getInteriorRingN(i)));
	}
	return poly;
    }

    /**
     * @param mp
     * @return
     */
    private static List<List<Double>> multiPointToList(MultiPoint mp) {

	List<List<Double>> points = new ArrayList<>();
	for (int i = 0; i < mp.getNumGeometries(); i++) {
	    Point p = (Point) mp.getGeometryN(i);
	    points.add(coordToList(p.getCoordinate()));
	}
	return points;
    }

    /**
     * @param mls
     * @return
     */
    private static List<List<List<Double>>> multiLineStringToList(MultiLineString mls) {

	List<List<List<Double>>> lines = new ArrayList<>();
	for (int i = 0; i < mls.getNumGeometries(); i++) {
	    lines.add(lineToList((LineString) mls.getGeometryN(i)));
	}
	return lines;
    }

    /**
     * @param mp
     * @return
     */
    private static List<List<List<List<Double>>>> multiPolygonToList(MultiPolygon mp) {

	List<List<List<List<Double>>>> polys = new ArrayList<>();
	for (int i = 0; i < mp.getNumGeometries(); i++) {
	    polys.add(polygonToList((Polygon) mp.getGeometryN(i)));
	}
	return polys;
    }
}
