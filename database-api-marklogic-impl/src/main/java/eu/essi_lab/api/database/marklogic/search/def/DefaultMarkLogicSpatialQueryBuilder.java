package eu.essi_lab.api.database.marklogic.search.def;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder.CTSLogicOperator;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSpatialQueryBuilder;
import eu.essi_lab.indexes.SpatialIndexHelper;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.RankingStrategy;

/**
 * @author Fabrizio
 */
public class DefaultMarkLogicSpatialQueryBuilder implements MarkLogicSpatialQueryBuilder {

    private MarkLogicSearchBuilder builder;
    protected RankingStrategy ranking;

    /**
     * @param builder
     */
    public DefaultMarkLogicSpatialQueryBuilder(MarkLogicSearchBuilder builder) {

	this.builder = builder;
	ranking = builder.getRankingStrategy();
    }

    /**
     * @param bond
     * @return
     */
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

	    return buildIntersetcsQuery(bond);

	case DISJOINT:

	    String c1 = buildDMDisjCardinalRangeQuery(CardinalPoint.SOUTH, ">", north);
	    String c2 = buildDMDisjCardinalRangeQuery(CardinalPoint.WEST, ">", east);
	    String c3 = buildDMDisjCardinalRangeQuery(CardinalPoint.NORTH, "<", south);
	    String c4 = buildDMDisjCardinalRangeQuery(CardinalPoint.EAST, "<", west);

	    return builder.buildCTSLogicQuery(CTSLogicOperator.OR, c1, c2, c3, c4);

	case CONTAINS:

	    return buildContainsQuery(bond);

	default:
	    throw new IllegalArgumentException("Unknown area operation: " + op);
	}
    }

    /**
     * @formatter:off
     * @param bond
     * @return
     */
    private String buildContainsQuery(SpatialBond bond) {

	SpatialExtent extent = (SpatialExtent) bond.getPropertyValue();

	double w = extent.getWest();
	double e = extent.getEast();

	String north = String.valueOf(extent.getNorth());
	String south = String.valueOf(extent.getSouth());
	String east = String.valueOf(e);
	String west = String.valueOf(w);

	double area = SpatialIndexHelper.computeArea(//
		Double.valueOf(west),//
		Double.valueOf(east),//
		Double.valueOf(south),
		Double.valueOf(north));

	String weightQuery = builder.buildCTSLogicQuery(CTSLogicOperator.OR,

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=", "0",
				ranking.computeBoundingBoxWeight(area, 10), false),
			
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 10)), ranking.computeBoundingBoxWeight(area, 10), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 10)), ranking.computeBoundingBoxWeight(area, 20), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 20)), ranking.computeBoundingBoxWeight(area, 20), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 20)), ranking.computeBoundingBoxWeight(area, 30), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 30)), ranking.computeBoundingBoxWeight(area, 30), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 30)), ranking.computeBoundingBoxWeight(area, 40), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 40)), ranking.computeBoundingBoxWeight(area, 40), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 40)), ranking.computeBoundingBoxWeight(area, 50), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 50)), ranking.computeBoundingBoxWeight(area, 50), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 50)), ranking.computeBoundingBoxWeight(area, 60), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 60)), ranking.computeBoundingBoxWeight(area, 60), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 60)), ranking.computeBoundingBoxWeight(area, 70), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 70)), ranking.computeBoundingBoxWeight(area, 70), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 70)), ranking.computeBoundingBoxWeight(area, 80), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<",
				String.valueOf(percent(area, 80)), ranking.computeBoundingBoxWeight(area, 80), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 80)), ranking.computeBoundingBoxWeight(area, 90), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<=",
				String.valueOf(percent(area, 90)), ranking.computeBoundingBoxWeight(area, 90), false)),

		builder.buildCTSLogicQuery(CTSLogicOperator.AND,
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, ">=",
				String.valueOf(percent(area, 90)), ranking.computeBoundingBoxWeight(area, 100), false),
			builder.buildCTSElementRangeQuery(BoundingBox.AREA_QUALIFIED_NAME, "<=", String.valueOf(area),
				ranking.computeBoundingBoxWeight(area, 100), false)));

	if (e >= w) {

	    String sw = buildCTSElementGeoQuery(west, south, east, north, CardinalPoint.SW);
	    String se = buildCTSElementGeoQuery(west, south, east, north, CardinalPoint.SE);
	    String nw = buildCTSElementGeoQuery(west, south, east, north, CardinalPoint.NW);
	    String ne = buildCTSElementGeoQuery(west, south, east, north, CardinalPoint.NE);

	    String notCrossed = builder.buildCTSElementRangeQuery(BoundingBox.IS_CROSSED_QUALIFIED_NAME, "=", "false",
		    true);

	    return builder.buildCTSLogicQuery(CTSLogicOperator.AND, sw, se, nw, ne, notCrossed, weightQuery);
	}

	String q4 = builder.buildCTSLogicQuery(CTSLogicOperator.AND, buildDMCardinalRangeQuery(CardinalPoint.WEST, ">=", west),
		buildDMCardinalRangeQuery(CardinalPoint.EAST, "<=", east));

	String q3 = builder.buildCTSLogicQuery(CTSLogicOperator.AND,
		builder.buildCTSElementRangeQuery(BoundingBox.IS_CROSSED_QUALIFIED_NAME, "=", "false", true),
		buildDMCardinalRangeQuery(CardinalPoint.WEST, ">", west));

	String q2 = builder.buildCTSLogicQuery(CTSLogicOperator.AND,
		builder.buildCTSElementRangeQuery(BoundingBox.IS_CROSSED_QUALIFIED_NAME, "=", "false", true),
		buildDMCardinalRangeQuery(CardinalPoint.EAST, "<=", east));

	String q1 = builder.buildCTSLogicQuery(CTSLogicOperator.AND, buildDMCardinalRangeQuery(CardinalPoint.NORTH, "<=", north),
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, ">=", south));

	return builder.buildCTSLogicQuery(CTSLogicOperator.AND, q1, weightQuery,
		builder.buildCTSLogicQuery(CTSLogicOperator.OR, q2, q3, q4));
    }

    /**
     * @formatter:on
     * @param bond
     * @return
     */
    private String buildIntersetcsQuery(SpatialBond bond) {

	SpatialExtent extent = (SpatialExtent) bond.getPropertyValue();

	String north = String.valueOf(extent.getNorth());
	String south = String.valueOf(extent.getSouth());
	String east = String.valueOf(extent.getEast());
	String west = String.valueOf(extent.getWest());

	// query bbox contains sw or se or nw or ne
	String anyPoint = buildCTSElementGeoQuery(//
		west, //
		south, //
		east, //
		north, //
		CardinalPoint.SW, //
		CardinalPoint.SE, //
		CardinalPoint.NW, //
		CardinalPoint.NE);

	// north-south overlapping
	String c1 = builder.buildCTSLogicQuery(//
		CTSLogicOperator.AND, //
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, "<=", north), //
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, ">=", south), //
		buildDMCardinalRangeQuery(CardinalPoint.WEST, "<=", west), //
		buildDMCardinalRangeQuery(CardinalPoint.EAST, ">=", east));

	// south-north overlapping
	String c2 = builder.buildCTSLogicQuery(//
		CTSLogicOperator.AND, //
		buildDMCardinalRangeQuery(CardinalPoint.NORTH, ">=", south), //
		buildDMCardinalRangeQuery(CardinalPoint.NORTH, "<=", north), //
		buildDMCardinalRangeQuery(CardinalPoint.WEST, "<=", west), //
		buildDMCardinalRangeQuery(CardinalPoint.EAST, ">=", east));

	// east-west overlapping
	String c3 = builder.buildCTSLogicQuery( //
		CTSLogicOperator.AND, //
		buildDMCardinalRangeQuery(CardinalPoint.WEST, "<=", east), //
		buildDMCardinalRangeQuery(CardinalPoint.WEST, ">=", west), //
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, "<=", south), //
		buildDMCardinalRangeQuery(CardinalPoint.NORTH, ">=", north));//

	// west-east overlapping
	String c4 = builder.buildCTSLogicQuery(//
		CTSLogicOperator.AND, //
		buildDMCardinalRangeQuery(CardinalPoint.EAST, ">=", west), //
		buildDMCardinalRangeQuery(CardinalPoint.EAST, "<=", east), //
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, "<=", south), //
		buildDMCardinalRangeQuery(CardinalPoint.NORTH, ">=", north));//

	// query bbox entirely contained in dest bbox
	String innerAnd = builder.buildCTSLogicQuery(//
		CTSLogicOperator.AND, //
		buildDMCardinalRangeQuery(CardinalPoint.SOUTH, "<=", south), //
		buildDMCardinalRangeQuery(CardinalPoint.NORTH, ">=", north), //
		buildDMCardinalRangeQuery(CardinalPoint.WEST, "<=", west), //
		buildDMCardinalRangeQuery(CardinalPoint.EAST, ">=", east));

	String out = builder.buildCTSLogicQuery(CTSLogicOperator.OR, anyPoint, c1, c2, c3, c4, innerAnd);

	return out;
    }

    @SuppressWarnings("incomplete-switch")
    private String buildDMDisjCardinalRangeQuery(CardinalPoint point, String operator, String value) {

	String pointIndex = "";
	switch (point) {
	case SOUTH:
	    pointIndex = "disjSouth";
	    break;
	case WEST:
	    pointIndex = "disjWest";
	    break;
	case NORTH:
	    pointIndex = "disjNorth";
	    break;
	case EAST:
	    pointIndex = "disjEast";
	    break;
	}

	QualifiedName iqe = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, pointIndex);

	return builder.buildCTSElementRangeQuery(iqe, operator, value, false);
    }

    private String buildCTSElementGeoQuery(String west, String south, String east, String north, CardinalPoint... point) {

	String query = "cts:element-geospatial-query((";
	for (int i = 0; i < point.length; i++) {
	    query += MarkLogicSearchBuilder.createFNQName(point[i].toString());
	    if (i < point.length - 1) {
		query += ",\n";
	    }
	}
	return query + ")," + buildCTSBox(west, south, east, north) + ")";
    }

    private String buildCTSBox(String west, String south, String east, String north) {

	return "cts:box(" + south + ", " + west + ", " + north + ", " + east + ")";
    }

    private String buildDMCardinalRangeQuery(CardinalPoint point, String operator, String value) {

	QualifiedName iqe = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, point.toString());

	return builder.buildCTSElementRangeQuery(iqe, operator, value, false);
    }

    protected double percent(double target, int value) {

	return (target / 100) * value;
    }
}
