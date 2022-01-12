package eu.essi_lab.api.database.marklogic;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.stats.StatisticsMessage.GroupByPeriod;
import eu.essi_lab.model.Queryable;

/**
 * @author Fabrizio
 */
public class MarkLogicModuleManager {

    private static final MarkLogicModuleManager INSTANCE = new MarkLogicModuleManager();

    public static MarkLogicModuleManager getInstance() {

	return INSTANCE;
    }

    /**
     * @param element
     * @param operator
     * @param value
     * @return
     */
    public String getTempExtentQuery(Queryable element, BondOperator operator, String value) {

	return "gs:temp-extent-query('" + element.getName() + "','" + operator.asMathOperator() + "','" + value + "')";
    }

    /**
     * @param name
     * @return
     */
    public String getTempExtentNowQuery(String name) {

	return "gs:temp-extent-now-query('" + name + "')";
    }

    /**
     * @param sourceId
     * @param suiteIdentifier
     * @return
     */
    public String getSourceIdQuery(String sourceId, String suiteIdentifier) {

	return "gs:sourceId-query('" + sourceId + "','" + suiteIdentifier + "')";
    }

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     * @return
     */
    public String getSpatialIntersectsQuery(String south, String west, String north, String east) {

	return "gs:spatial-intersects-query(" + south + "," + west + "," + north + "," + east + ")";
    }

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     * @return
     */
    public String getSpatialContainsQuery(String south, String west, String north, String east, List<SimpleEntry<Double, Double>> ranges) {

	double w = Double.valueOf(west);
	double e = Double.valueOf(east);

	String params = "";

	for (SimpleEntry<Double, Double> simpleEntry : ranges) {

	    Double key = simpleEntry.getKey();
	    Double value = simpleEntry.getValue();

	    params += "" + key + "," + value + ",";
	}

	params = params.substring(0, params.length() - 1);

	if (e >= w) {

	    return "gs:spatial-contains-ncr-query(" + south + "," + west + "," + north + "," + east + "," + params + ")";
	}

	return "gs:spatial-contains-cr-query(" + south + "," + west + "," + north + "," + east + "," + params + ")";
    }

    /**
     * @return
     */
    public String getDeletedExcludedQuery() {

	return "gs:deleted-excluded-query()";
    }

    /**
     * @param elementName
     * @param w0
     * @param w1
     * @param w2
     * @param w3
     * @param w4
     * @param w5
     * @param w6
     * @param w7
     * @param w8
     * @param w9
     * @return
     */
    public String getWeightQuery(String elementName, int w0, int w1, int w2, int w3, int w4, int w5, int w6, int w7, int w8, int w9) {

	return "gs:weight-query('" + elementName + "'," + w0 + "," + w1 + "," + w2 + "," + w3 + "," + w4 + "," + w5 + "," + w6 + "," + w7
		+ "," + w8 + "," + w9 + ")";
    }

    /**
     * @return
     */
    public String getGDCWeightQuery() {

	return "gs:gdc-weight-query()";
    }

    /**
     * @param groupByTarget
     * @param forInVariable
     * @param groupByPeriod
     * @param queryBbox
     * @return
     */
    public String getBboxUnionQuery(//
	    Optional<Queryable> groupByTarget, //
	    String forInVariable, //
	    Optional<GroupByPeriod> groupByPeriod, //
	    boolean queryBbox) {

	String isQueryBbox = Boolean.valueOf(queryBbox).toString();

	if (groupByTarget.isPresent()) {

	    return "gs:BboxUnion($query, \"" + groupByTarget.get().getName() + "\", $" + forInVariable + ", \"\" , \"\", \"" + isQueryBbox
		    + "\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:BboxUnion($query, \"\" , \"\" , $" + forInVariable + ", \"" + groupByPeriod.get().getTarget().getName() + "\", \""
		    + isQueryBbox + "\")";
	}

	return "gs:BboxUnion($query, \"\", \"\", \"\", \"\", \"" + isQueryBbox + "\")";
    }

    /**
     * @param groupByTarget
     * @param forInVariable
     * @param groupByPeriod
     * @param queryExtent
     * @return
     */
    public String getTemporalExtentUnionQuery(//
	    Optional<Queryable> groupByTarget, //
	    String forInVariable, //
	    Optional<GroupByPeriod> groupByPeriod, //
	    boolean queryExtent) {

	String isQueryExtent = Boolean.valueOf(queryExtent).toString();

	if (groupByTarget.isPresent()) {

	    return "gs:temporalExtentUnion($query, \"" + groupByTarget.get().getName() + "\", $" + forInVariable + ", \"\" ,\"\", \""
		    + isQueryExtent + "\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:temporalExtentUnion($query, \"\" ,\"\", $" + forInVariable + ", \"" + groupByPeriod.get().getTarget().getName()
		    + "\", \"" + isQueryExtent + "\")";
	}

	return "gs:temporalExtentUnion($query, \"\" ,\"\", \"\", \"\", \"" + isQueryExtent + "\")";
    }

    /**
     * @param target
     * @param groupByTarget
     * @param forInVariable
     * @param groupByPeriod
     * @return
     */
    public String getCountDistinctQuery(String target, Optional<Queryable> groupByTarget, String forInVariable,
	    Optional<GroupByPeriod> groupByPeriod) {

	if (groupByTarget.isPresent()) {

	    return "gs:countDistinct($query, \"" + target + "\", \"" + groupByTarget.get().getName() + "\", $" + forInVariable
		    + ", \"\" ,\"\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:countDistinct($query, \"" + target + "\", \"\" ,\"\", $" + forInVariable + ", \""
		    + groupByPeriod.get().getTarget().getName() + "\")";
	}

	return "gs:countDistinct($query, \"" + target + "\", \"\" ,\"\", \"\" ,\"\")";
    }

    /**
     * @param target
     * @param max
     * @param groupByTarget
     * @param forInVariable
     * @param groupByPeriod
     * @return
     */
    public String getFrequencyFunctionQuery(//
	    String target,//
	    int max,//
	    Optional<Queryable> groupByTarget,//
	    String forInVariable,//
	    Optional<GroupByPeriod> groupByPeriod) {

	if (groupByTarget.isPresent()) {

	    return "gs:frequency($query, \"" + target + "\", " + max + ",  \"" + groupByTarget.get().getName() + "\", $" + forInVariable
		    + ", \"\" ,\"\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:frequency($query, \"" + target + "\", " + max + ", \"\" ,\"\", $" + forInVariable + ", \""
		    + groupByPeriod.get().getTarget().getName() + "\")";
	}

	return "gs:frequency($query, \"" + target + "\", " + max + ", \"\" ,\"\", \"\" ,\"\")";
    }

    /**
     * @param target
     * @param bboxes
     * @return
     */
    public String getGeoSpatialFrequencyFunctionQuery(//
	    String target,//
	    String bboxes,//
	    int max,//
	    Optional<Queryable> groupByTarget,//
	    String forInVariable,
	    Optional<GroupByPeriod> groupByPeriod) {
	
	if (groupByTarget.isPresent()) {

	    return "gs:geoSpatialFrequency($query, \"" + target + "\", " + bboxes + ", " + max + ",  \"" + groupByTarget.get().getName() + "\", $" + forInVariable
		    + ", \"\" ,\"\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:geoSpatialFrequency($query, \"" + target + "\", " + bboxes + ", " + max + ", \"\" ,\"\", $" + forInVariable + ", \""
		    + groupByPeriod.get().getTarget().getName() + "\")";
	}

	return "gs:geoSpatialFrequency($query, \"" + target + "\", " + bboxes + ", " + max + ", \"\" ,\"\", \"\" ,\"\")";

//	return "gs:geoSpatialFrequency($query, \"" + target + "\", " + bboxes + ")";
    }

    /**
     * @param function
     * @param target
     * @param groupByTarget
     * @param forInVariable
     * @param groupByPeriod
     * @return
     */
    public String getStatisticsFunctionQuery(String function, String target, Optional<Queryable> groupByTarget, String forInVariable,
	    Optional<GroupByPeriod> groupByPeriod) {

	if (groupByTarget.isPresent()) {

	    return "gs:statFunction(\"" + function + "\", $query, \"" + target + "\", \"" + groupByTarget.get().getName() + "\", $"
		    + forInVariable + ", \"\" ,\"\")";

	} else if (groupByPeriod.isPresent()) {

	    return "gs:statFunction(\"" + function + "\", $query, \"" + target + "\", \"\" ,\"\", $" + forInVariable + ", \""
		    + groupByPeriod.get().getTarget().getName() + "\")";
	}

	return "gs:statFunction(\"" + function + "\", $query, \"" + target + "\", \"\" ,\"\", \"\" ,\"\")";
    }

   
}
