package eu.essi_lab.api.database.marklogic.search.module;

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

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.api.database.marklogic.MarkLogicModuleManager;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSpatialQueryBuilder;
import eu.essi_lab.indexes.SpatialIndexHelper;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;

/**
 * @author Fabrizio
 */
public class ModuleMarkLogicSpatialQueryBuilder extends DefaultMarkLogicSpatialQueryBuilder {

    /**
     * @param builder
     */
    public ModuleMarkLogicSpatialQueryBuilder(MarkLogicSearchBuilder builder) {
	super(builder);
    }

    public String buildSpatialQuery(SpatialBond bond) {

	BondOperator op = bond.getOperator();
	SpatialExtent extent = (SpatialExtent) bond.getPropertyValue();

	String north = String.valueOf(extent.getNorth());
	String south = String.valueOf(extent.getSouth());
	String east = String.valueOf(extent.getEast());
	String west = String.valueOf(extent.getWest());

	switch (op) {
	case BBOX:
	case INTERSECTS:

	    return MarkLogicModuleManager.getInstance().getSpatialIntersectsQuery(south, west, north, east);

	case DISJOINT:

	    return super.buildSpatialQuery(bond);

	case CONTAINS:

	    double area = SpatialIndexHelper.computeArea(//
		    Double.valueOf(west),//
		    Double.valueOf(east),//
		    Double.valueOf(south),//
		    Double.valueOf(north));

	    List<AbstractMap.SimpleEntry<Double,Double>> ranges = new ArrayList<>();
	    
	    ranges.add(new SimpleEntry<Double, Double>(0.0, ranking.computeBoundingBoxWeight(area, 10)));	    	    
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 10), ranking.computeBoundingBoxWeight(area, 10)));
	    
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 10), ranking.computeBoundingBoxWeight(area, 20)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 20), ranking.computeBoundingBoxWeight(area, 20)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 20), ranking.computeBoundingBoxWeight(area, 30)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 30), ranking.computeBoundingBoxWeight(area, 30)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 30), ranking.computeBoundingBoxWeight(area, 40)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 40), ranking.computeBoundingBoxWeight(area, 40)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 40), ranking.computeBoundingBoxWeight(area, 50)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 50), ranking.computeBoundingBoxWeight(area, 50)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 50), ranking.computeBoundingBoxWeight(area, 60)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 60), ranking.computeBoundingBoxWeight(area, 60)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 60), ranking.computeBoundingBoxWeight(area, 70)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 70), ranking.computeBoundingBoxWeight(area, 70)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 70), ranking.computeBoundingBoxWeight(area, 80)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 80), ranking.computeBoundingBoxWeight(area, 80)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 80), ranking.computeBoundingBoxWeight(area, 90)));
	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 90), ranking.computeBoundingBoxWeight(area, 90)));

	    ranges.add(new SimpleEntry<Double, Double>(percent(area, 90), ranking.computeBoundingBoxWeight(area, 100)));
	    ranges.add(new SimpleEntry<Double, Double>(area, ranking.computeBoundingBoxWeight(area, 100)));

	    return MarkLogicModuleManager.getInstance().getSpatialContainsQuery(south, west, north, east, ranges);

	default:
	    throw new IllegalArgumentException("Unknown area operation: " + op);
	}
    }
}
