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
import java.util.AbstractMap.SimpleEntry;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;

/**
 * Reprojects shape geometries and bounding boxes to WGS84 using the CRS declared for the stored coordinates.
 */
public final class ShapeBoundingBoxNormalizer {

    private ShapeBoundingBoxNormalizer() {
    }

    /**
     * @param shapeCrsIdentifier CRS of {@code geometry} coordinates ({@code EPSG:xxxx}); blank means WGS84
     */
    public static Geometry toWgs84(Geometry geometry, String shapeCrsIdentifier) {

	if (geometry == null || isGeographicCrs(shapeCrsIdentifier)) {
	    return geometry;
	}

	try {

	    CoordinateReferenceSystem source = CRS.fromIdentifier(shapeCrsIdentifier).getDecodedCRS();
	    if (source == null) {
		return geometry;
	    }

	    MathTransform transform = org.geotools.referencing.CRS.findMathTransform(source, DefaultGeographicCRS.WGS84, true);
	    return JTS.transform(geometry, transform);

	} catch (Exception ex) {
	    return geometry;
	}
    }

    /**
     * @param west min X in {@code shapeCrsIdentifier}
     * @param east max X in {@code shapeCrsIdentifier}
     * @param south min Y in {@code shapeCrsIdentifier}
     * @param north max Y in {@code shapeCrsIdentifier}
     * @param shapeCrsIdentifier CRS of the bounds
     * @return {@code [south, west, north, east]} in WGS84 degrees
     */
    public static double[] toWgs84SouthWestNorthEast(double west, double east, double south, double north, String shapeCrsIdentifier)
	    throws Exception {

	if (isGeographicCrs(shapeCrsIdentifier)) {
	    return new double[] { south, west, north, east };
	}

	CRS sourceCrs = CRS.fromIdentifier(shapeCrsIdentifier);
	if (sourceCrs == null || sourceCrs.getDecodedCRS() == null) {
	    return new double[] { south, west, north, east };
	}

	SimpleEntry<Double, Double> lower = new SimpleEntry<>(west, south);
	SimpleEntry<Double, Double> upper = new SimpleEntry<>(east, north);

	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> corners = new SimpleEntry<>(lower, upper);

	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> wgs84 = CRSUtils.translateBBOX(corners, sourceCrs,
		CRS.EPSG_4326());

	// translateBBOX to EPSG:4326 returns corners as (latitude, longitude) in each SimpleEntry
	double southDeg = wgs84.getKey().getKey();
	double westDeg = wgs84.getKey().getValue();
	double northDeg = wgs84.getValue().getKey();
	double eastDeg = wgs84.getValue().getValue();

	return new double[] { southDeg, westDeg, northDeg, eastDeg };
    }

    /**
     * @param north
     * @param west
     * @param south
     * @param east
     * @param shapeCrsIdentifier CRS of the bounds
     * @return WGS84 bounds as {@code [north, west, south, east]}
     */
    public static double[] normalizeMetadataBbox(BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east,
	    String shapeCrsIdentifier) {

	try {

	    double[] bbox = toWgs84SouthWestNorthEast(west.doubleValue(), east.doubleValue(), south.doubleValue(), north.doubleValue(),
		    shapeCrsIdentifier);
	    return new double[] { bbox[2], bbox[1], bbox[0], bbox[3] };

	} catch (Exception ex) {
	    return new double[] { south.doubleValue(), west.doubleValue(), north.doubleValue(), east.doubleValue() };
	}
    }

    private static boolean isGeographicCrs(String shapeCrsIdentifier) {

	if (shapeCrsIdentifier == null || shapeCrsIdentifier.isBlank()) {
	    return true;
	}

	CRS crs = CRS.fromIdentifier(shapeCrsIdentifier);
	if (crs == null) {
	    return false;
	}

	CoordinateReferenceSystem decoded = crs.getDecodedCRS();
	if (decoded == null) {
	    return false;
	}

	if (decoded instanceof DefaultGeographicCRS) {
	    return true;
	}

	try {
	    return org.geotools.referencing.CRS.equalsIgnoreMetadata(decoded, DefaultGeographicCRS.WGS84);
	} catch (Exception ex) {
	    return false;
	}
    }
}
