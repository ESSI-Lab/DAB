package eu.essi_lab.model.resource.data;

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

import java.util.AbstractMap.SimpleEntry;

import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


public class CRSUtils {
    public static SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> translateBBOX(
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners, CRS sourceCRS, CRS targetCRS)
	    throws Exception {

	CoordinateReferenceSystem geotoolsSourceCRS = sourceCRS.getDecodedCRS();
	CoordinateReferenceSystem geotoolsTargetCRS = targetCRS.getDecodedCRS();
	MathTransform transform = org.geotools.referencing.CRS.findMathTransform(geotoolsSourceCRS, geotoolsTargetCRS);

	SimpleEntry<Double, Double> lowerCorner = sourceCorners.getKey();
	SimpleEntry<Double, Double> upperCorner = sourceCorners.getValue();

	Envelope fullEnvelope = new Envelope(lowerCorner.getKey(), upperCorner.getKey(), lowerCorner.getValue(), upperCorner.getValue());
	// Sample 10 points around the envelope
	Envelope fullTransformed = JTS.transform(fullEnvelope, null, transform, 10);

	double lower1 = fullTransformed.getMinX();
	double lower2 = fullTransformed.getMinY();
	double upper1 = fullTransformed.getMaxX();
	double upper2 = fullTransformed.getMaxY();

	lowerCorner = new SimpleEntry<Double, Double>(lower1, lower2);
	upperCorner = new SimpleEntry<Double, Double>(upper1, upper2);

	return new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(lowerCorner, upperCorner);

    }

    public static SimpleEntry<Double, Double> translatePoint(SimpleEntry<Double, Double> coordinate, CRS sourceCRS, CRS targetCRS)
	    throws Exception {

	CoordinateReferenceSystem geotoolsSourceCRS = sourceCRS.getDecodedCRS();
	CoordinateReferenceSystem geotoolsTargetCRS = targetCRS.getDecodedCRS();
	MathTransform transform = org.geotools.referencing.CRS.findMathTransform(geotoolsSourceCRS, geotoolsTargetCRS);

	Coordinate source = new Coordinate(coordinate.getKey(), coordinate.getValue());

	Coordinate dest = JTS.transform(source, null, transform);

	return new SimpleEntry<Double, Double>(dest.x, dest.y);

    }
    
    public static void main(String[] args) throws Exception {
	SimpleEntry<Double, Double> result = CRSUtils.translatePoint(new SimpleEntry<Double, Double>(15092.814748879056,-7546.407374442555), CRS.EPSG_3857(), CRS.EPSG_4326());
	System.out.println("LAT: "+result.getKey());
	System.out.println("LON: "+result.getValue());
	
    }
}
