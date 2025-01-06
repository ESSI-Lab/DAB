package eu.essi_lab.indexes;

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.index.jaxb.DisjointValues;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class SpatialIndexHelper {

    private static final double MAX_AREA = 64800;

    private SpatialIndexHelper() {
	//force static usage
    }

    static void addBBoxes(GSResource resource, IndexedMetadataElement element) {

	Double[] ws = new Double[2];
	Double[] en = new Double[2];

	ArrayList<Double[]> values = new ArrayList<>();

	List<DataIdentification> dataIdList = Lists.newArrayList(
		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentifications());
	List<GeographicBoundingBox> bboxes = new ArrayList<>();

	for (DataIdentification dataId : dataIdList) {
	    bboxes.addAll(Lists.newArrayList(dataId.getGeographicBoundingBoxes()));
	}

	switch (bboxes.size()) {// safe, null not returned
	case 0:
	    resource.getIndexesMetadata().write(IndexedElements.BOUNDING_BOX_NULL);
	    return;
	case 1: // one bbox
	    try {
		ws = dataIdList.get(0).getWS();
		en = dataIdList.get(0).getEN();

		if (ws[0] != null && ws[1] != null && en[0] != null && en[1] != null) {

		    double west = ws[0];
		    double east = en[0];

		    double south = ws[1];
		    double north = en[1];

		    if (south > north) {
			double tmp = south;
			south = north;
			north = tmp;
		    }

		    values.add(new Double[] { west, south, east, north });
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(SpatialIndexHelper.class).warn("Exception parsing bbox", e);

	    }

	    break;
	default: // multiple boxes

	    Double minw = null;
	    Double mins = null;
	    Double maxe = null;
	    Double maxn = null;

	    for (GeographicBoundingBox bbox : bboxes) {

		Double w = bbox.getWest();
		Double s = bbox.getSouth();
		Double e = bbox.getEast();
		Double n = bbox.getNorth();

		if (w != null && s != null && e != null && n != null) {

		    values.add(new Double[] { w, s, e, n });
		    // TODO: check in case of crossed bbox
		    minw = 181.0;
		    mins = 91.0;
		    maxe = -181.0;
		    maxn = -91.0;

		    if (w < minw) {
			minw = w;
		    }
		    if (s < mins) {
			mins = s;
		    }
		    if (e > maxe) {
			maxe = e;
		    }
		    if (n > maxn) {
			maxn = n;
		    }
		}
	    }

	    if (maxe != null && maxn != null && minw != null && mins != null) {

		en = new Double[] { maxe, maxn };
		ws = new Double[] { minw, mins };
	    }
	}

	if (ws[0] != null && ws[1] != null && en[0] != null && en[1] != null) {
	    if (ws[0] != Double.NaN && ws[1] != Double.NaN && en[0] != Double.NaN && en[1] != Double.NaN) {

		BoundingBox bbox = createBbox(en, ws, values);
		element.setBoundingBox(bbox);
	    }
	} else {

	    resource.getIndexesMetadata().write(IndexedElements.BOUNDING_BOX_NULL);
	}
    }

    private static BoundingBox createBbox(Double[] en, Double[] ws, ArrayList<Double[]> values) {

	double nd = en[1];
	double ed = en[0];
	double sd = ws[1];
	double wd = ws[0];

	if (nd > 90) {
	    nd = 90;
	}

	if (sd < -90) {
	    sd = -90;
	}

	if (ed > 180) {
	    ed = 180;
	}

	if (wd < -180) {
	    wd = -180;
	}

	String n = String.valueOf(nd);
	String e = String.valueOf(ed);
	String s = String.valueOf(sd);
	String w = String.valueOf(wd);

	BoundingBox boundingBox = new BoundingBox();

	DisjointValues disjointValues = new DisjointValues();
	boundingBox.setDisjointValues(disjointValues);

	/**
	 * following values are used to build the MarkLogic geospatial indexes which are used to execute contains
	 * operation.in case of multiple boxes, following values are computed in the calling method as the bbox
	 * resulting from the union of all the bboxes
	 */
	boundingBox.setSw(s + " " + w);
	boundingBox.setSe(s + " " + e);
	boundingBox.setNw(n + " " + w);
	boundingBox.setNe(n + " " + e);

	/**
	 * following values are used to build the MarkLogic range indexes which are used to execute disjoint
	 * operations.in case of multiple boxes, following values are computed in the calling method as the bbox
	 * resulting from the union of all the bboxes
	 */
	disjointValues.setDisjSouth(s);
	disjointValues.setDisjNorth(n);
	disjointValues.setDisjWest(w);
	disjointValues.setDisjEast(e);

	/**
	 * following values are used to build the MarkLogic range indexes which are used to execute overlap operations
	 */
	for (Double[] val : values) {

	    CardinalValues cardinalValues = new CardinalValues();
	    boundingBox.addCardinalValues(cardinalValues);

	    cardinalValues.setSouth(String.valueOf(val[1]));
	    cardinalValues.setWest(String.valueOf(val[0]));
	    cardinalValues.setNorth(String.valueOf(val[3]));
	    cardinalValues.setEast(String.valueOf(val[2]));
	}

	double west = Double.parseDouble(w);
	double east = Double.parseDouble(e);
	double south = Double.parseDouble(s);
	double north = Double.parseDouble(n);

	if (west > east) {
	    boundingBox.setIsCrossed("true");
	} else {
	    boundingBox.setIsCrossed("false");
	}

	double area = computeArea(west, east, south, north);

	DecimalFormat df = new DecimalFormat();
	df.setGroupingUsed(false);

	DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	dfs.setDecimalSeparator('.');
	df.setDecimalFormatSymbols(dfs);
	df.setMaximumFractionDigits(3);

	boundingBox.setArea(df.format(area));

	double lonExt = east - west;
	double latExt = north - south;

	if (e.equals(w) && n.equals(s)) {

	    boundingBox.setCenter(n + " " + e);
	} else {

	    double centerX = Double.valueOf(w) + (lonExt / 2);
	    double centerY = Double.valueOf(s) + (latExt / 2);

	    boundingBox.setCenter(centerY + " " + centerX);
	}

	return boundingBox;
    }

    /**
     * @param west
     * @param east
     * @param south
     * @param north
     * @return
     */
    public static double computeArea(double west, double east, double south, double north) {

	if (west > east) {
	    double tmp = west;
	    west = east;
	    east = tmp;
	}

	if (south > north) {
	    double tmp = south;
	    south = north;
	    north = tmp;
	}

	double lonExt = (east + 180) - (west + 180);
	double latExt = (north + 90) - (south + 90);

	double area = (lonExt * latExt);

	if (area > MAX_AREA) {
	    area = 0;
	}

	return area;
    }

}
