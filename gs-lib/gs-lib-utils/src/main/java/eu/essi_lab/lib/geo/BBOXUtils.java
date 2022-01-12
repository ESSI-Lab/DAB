package eu.essi_lab.lib.geo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.AbstractMap.SimpleEntry;

public class BBOXUtils {

    public static SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> getBBOX(
	    List<SimpleEntry<Double, Double>> latLongs) {
	Double south = null;
	Double north = null;
	Double minWest = null;
	Double maxWest = null;
	Double minEast = null;
	Double maxEast = null;
	for (SimpleEntry<Double, Double> latLong : latLongs) {
	    Double lat = latLong.getKey();
	    Double lon = latLong.getValue();
	    if(lat == null && lon == null) {
		continue;
	    }
	    if (south == null || lat < south) {
		south = lat;
	    }
	    if (north == null || lat > north) {
		north = lat;
	    }
	    if (lon < 0) {
		if (minWest == null || lon < minWest) {
		    minWest = lon;
		}
		if (maxWest == null || lon > maxWest) {
		    maxWest = lon;
		}
	    } else {
		if (minEast == null || lon < minEast) {
		    minEast = lon;
		}
		if (maxEast == null || lon > maxEast) {
		    maxEast = lon;
		}
	    }
	}
	Double west = null;
	Double east = null;
	if (minWest == null) {
	    west = minEast;
	    east = maxEast;
	} else if (minEast == null) {
	    west = minWest;
	    east = maxWest;
	} else {
	    Double normalBboxSize = maxEast - minWest;
	    Double crossBboxSize = 360 + maxWest - minEast;
	    if (normalBboxSize < crossBboxSize) {
		west = minWest;
		east = maxEast;
	    } else {
		west = minEast;
		east = maxWest;
	    }
	}
	SimpleEntry<Double, Double> lowerCorner = new SimpleEntry<>(south, west);
	SimpleEntry<Double, Double> upperCorner = new SimpleEntry<>(north, east);
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> ret = new SimpleEntry<>(lowerCorner, upperCorner);
	return ret;
    }
    
    public static String toBBOX(String polygon, boolean firstIsLat) {

	polygon = polygon.replace("MULTIPOLYGON", "").//
		replace("POLYGON", "").//
		replace("(((", "").//
		replace("((", "").//
		replace(")))", "").//
		replace("))", "").//
		replace(",", " ").//
		replace("  ", " ").//
		trim();

	String[] coords = polygon.split(" ");

	Double[] lats = new Double[coords.length / 2];
	Double[] lons = new Double[coords.length / 2];

	List<SimpleEntry<Double, Double>> latLongs = new ArrayList<>();	
	
	
	for (int i = 0; i < coords.length; i++) {

	    if (i % 2 == 0) {

		if (firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    } else {

		if (!firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    }
	}
	
	for(int j = 0; j < lats.length; j++) {
	    SimpleEntry<Double, Double> latLonMin = new SimpleEntry<>(lats[j],lons[j]);
	    latLongs.add(latLonMin);
	}

	Double minLat = null;
	Double minLon = null;
	Double maxLat = null;
	Double maxLon = null;

	
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = getBBOX(latLongs);
	SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	minLat = lowerCorner.getKey();
	minLon = lowerCorner.getValue();
	maxLat = upperCorner.getKey();
	maxLon = upperCorner.getValue();
	
	
	return minLat + " " + minLon + " " + maxLat + " " + maxLon;

//	for (int i = 1; i < lats.length; i++) {
//
//	    if (lats[i] < minLat) {
//		minLat = lats[i];
//	    }
//	    if (lats[i] > maxLat) {
//		maxLat = lats[i];
//	    }
//
//	    if (lons[i] < minLon) {
//		minLon = lons[i];
//	    }
//	    if (lons[i] > maxLon) {
//		maxLon = lons[i];
//	    }
//	}

	
    }

}
