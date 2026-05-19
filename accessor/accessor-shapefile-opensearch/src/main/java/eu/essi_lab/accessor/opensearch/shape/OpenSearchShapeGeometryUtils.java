package eu.essi_lab.accessor.opensearch.shape;

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

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import eu.essi_lab.accessor.s3.FeatureMetadata;

/**
 * Converts geometries stored in the OpenSearch shape-file index to JTS and {@link FeatureMetadata} bounds.
 */
public class OpenSearchShapeGeometryUtils {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private OpenSearchShapeGeometryUtils() {
    }

    /**
     * @param shape geojson-like object from the shape-file index ({@code shape} property)
     */
    public static Geometry toGeometry(JSONObject shape) {

	String type = shape.getString("type").toLowerCase();
	JSONArray coordinates = shape.getJSONArray("coordinates");

	return switch (type) {
	case "polygon" -> toPolygon(coordinates);
	case "multipolygon" -> toMultiPolygon(coordinates);
	default -> throw new IllegalArgumentException("Unsupported shape type: " + type);
	};
    }

    public static void setBoundingBox(FeatureMetadata metadata, Geometry wgs84Geometry) {

	org.locationtech.jts.geom.Envelope envelope = wgs84Geometry.getEnvelopeInternal();
	metadata.setWest(BigDecimal.valueOf(envelope.getMinX()));
	metadata.setEast(BigDecimal.valueOf(envelope.getMaxX()));
	metadata.setSouth(BigDecimal.valueOf(envelope.getMinY()));
	metadata.setNorth(BigDecimal.valueOf(envelope.getMaxY()));
    }



    private static Polygon toPolygon(JSONArray rings) {

	LinearRing shell = toLinearRing(rings.getJSONArray(0));
	LinearRing[] holes = new LinearRing[rings.length() - 1];
	for (int i = 1; i < rings.length(); i++) {
	    holes[i - 1] = toLinearRing(rings.getJSONArray(i));
	}
	return GEOMETRY_FACTORY.createPolygon(shell, holes);
    }

    private static MultiPolygon toMultiPolygon(JSONArray polygons) {

	Polygon[] polygonArray = new Polygon[polygons.length()];
	for (int i = 0; i < polygons.length(); i++) {
	    polygonArray[i] = toPolygon(polygons.getJSONArray(i));
	}
	return GEOMETRY_FACTORY.createMultiPolygon(polygonArray);
    }

    private static LinearRing toLinearRing(JSONArray ring) {

	Coordinate[] coordinates = new Coordinate[ring.length()];
	for (int i = 0; i < ring.length(); i++) {
	    JSONArray point = ring.getJSONArray(i);
	    coordinates[i] = new Coordinate(point.getDouble(0), point.getDouble(1));
	}
	return GEOMETRY_FACTORY.createLinearRing(coordinates);
    }
}
