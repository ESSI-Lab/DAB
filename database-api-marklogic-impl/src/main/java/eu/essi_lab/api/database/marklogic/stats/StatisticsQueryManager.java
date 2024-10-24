/**
 * 
 */
package eu.essi_lab.api.database.marklogic.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;

import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.MarkLogicModuleQueryBuilder;
import eu.essi_lab.api.database.marklogic.XQueryBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.stats.RegionsManager;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsMessage.GroupByPeriod;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class StatisticsQueryManager {

    private static final StatisticsQueryManager INSTANCE = new StatisticsQueryManager();

    private StatisticsQueryManager() {
    }

    /**
     * @author Fabrizio
     */
    private enum Function {

	/**
	 * 
	 */
	MAX,
	/**
	 * 
	 */
	MIN,
	/**
	 * 
	 */
	SUM,
	/**
	 * 
	 */
	AVG
    }

    /**
     * @return
     */
    public static StatisticsQueryManager getInstance() {

	return INSTANCE;
    }

    /**
     * @param statsMessage
     * @return
     * @throws RequestException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public String createComputeQuery(//
	    StatisticsMessage statsMessage, //
	    MarkLogicDatabase dataBase) throws RequestException, UnsupportedEncodingException, JAXBException {

	DiscoveryMessage discoveryMessage = new DiscoveryMessage(statsMessage);

	MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(discoveryMessage, dataBase);
	DiscoveryBondParser bondParser = new DiscoveryBondParser(discoveryMessage);
	bondParser.parse(handler);

	String query = handler.getCTSSearchQuery(true);

	XQueryBuilder builder = new XQueryBuilder();

	builder.append("let $query := " + query);
	builder.appendCarriageReturn(2);

	Optional<List<Queryable>> countDistinctTargets = statsMessage.getCountDistinctTargets();
	boolean bboxUnionComputationSet = statsMessage.isBboxUnionComputationSet();
	boolean queryBboxUnionComputationSet = statsMessage.isQueryBboxUnionComputationSet();
	boolean tempExtentUnionComputationSet = statsMessage.isTempExtentUnionComputationSet();
	boolean queryTempExtentUnionComputationSet = statsMessage.isQueryTempExtentUnionComputationSet();

	Optional<List<Queryable>> maxTargets = statsMessage.getMaxTargets();
	Optional<List<Queryable>> minTargets = statsMessage.getMinTargets();
	Optional<List<Queryable>> sumTargets = statsMessage.getSumTargets();
	Optional<List<Queryable>> avgTargets = statsMessage.getAvgTargets();

	Optional<List<Queryable>> freqTargets = statsMessage.getFrequencyTargets();
	Optional<Integer> maxFrequencyItems = statsMessage.getMaxFrequencyItems();

	Optional<Queryable> groupByTarget = statsMessage.getGroupByTarget();
	Optional<GroupByPeriod> groupByPeriod = statsMessage.getGroupByPeriod();

	if (groupByTarget.isPresent()) {

	    Queryable groupBy = groupByTarget.get();

	    builder.append("let $values := cts:element-values(fn:QName('" + CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "','"
		    + groupBy.getName() + "'), (), ('eager'), $query)");

	} else if (groupByPeriod.isPresent()) {

	    Long period = groupByPeriod.get().getPeriod();
	    int fraction = (int) Math.ceil(groupByPeriod.get().getFraction());

	    long subPeriod = (long) (period / fraction);

	    long start = groupByPeriod.get().getStartTime();

	    builder.append("let $values := ( ");

	    for (long i = start, count = 0; count < fraction; i += subPeriod, count++) {

		builder.append("'" + i + ResponseItem.ITEMS_RANGE_SEPARATOR + (i + subPeriod) + "',");
	    }

	    //
	    // removes the last ','
	    //
	    builder.cutLast();

	    builder.append(" ) \n");
	}

	builder.appendCarriageReturn(2);

	builder.appendReturnStatement();

	builder.appendGSElement("gs:StatisticsResponse");

	builder.appendOpenBrace();

	builder.appendCarriageReturn(1);

	builder.appendGSAttribute();
	builder.appendComma();
	builder.appendCarriageReturn();

	builder.appendXSAttribute();
	builder.appendComma();
	builder.appendCarriageReturn();

	builder.appendXSIAttribute();
	builder.appendComma();
	builder.appendCarriageReturn(2);

	String forInVariable = "val";

	if (groupByTarget.isPresent() || groupByPeriod.isPresent()) {

	    Queryable groupBy = groupByTarget.isPresent() ? //
		    groupByTarget.get() : //
		    groupByPeriod.get().getTarget();

	    builder.appendAttribute("itemsCount", "count($values)", false);
	    builder.appendComma();

	    builder.appendCarriageReturn();

	    builder.appendAttribute("groupBy", groupBy.getName());
	    builder.appendComma();

	    builder.appendCarriageReturn(2);

	    Page page = statsMessage.getPage();

	    //
	    // pagination only works with grouped results items
	    //
	    builder.appendForInStatement(forInVariable);
	    builder.appendSubsequence("$values", page.getStart(), page.getSize());
	    builder.appendCarriageReturn(2);

	    builder.appendReturnStatement();

	    builder.appendGSElement("gs:ResponseItem");

	    builder.appendOpenBrace();
	    builder.appendCarriageReturn(2);

	    builder.appendAttribute("groupedBy", "$" + forInVariable, false);
	    builder.appendComma();


	} else {

	    builder.appendAttribute("itemsCount", "1");
	    builder.appendComma();
	    builder.appendCarriageReturn(2);

	    builder.appendGSElement("gs:ResponseItem");
	    builder.appendOpenBrace();
	    builder.appendCarriageReturn(2);
	}

	if (bboxUnionComputationSet) {

	    builder.appendCarriageReturn();
	    builder.append(MarkLogicModuleQueryBuilder.getInstance().getBboxUnionQuery(groupByTarget, forInVariable, groupByPeriod, false));
	    builder.appendComma();
	}

	if (queryBboxUnionComputationSet) {

	    builder.appendCarriageReturn();
	    builder.append(MarkLogicModuleQueryBuilder.getInstance().getBboxUnionQuery(groupByTarget, forInVariable, groupByPeriod, true));
	    builder.appendComma();
	}

	if (tempExtentUnionComputationSet) {

	    builder.appendCarriageReturn();
	    builder.append(
		    MarkLogicModuleQueryBuilder.getInstance().getTemporalExtentUnionQuery(groupByTarget, forInVariable, groupByPeriod, false));
	    builder.appendComma();
	}

	if (queryTempExtentUnionComputationSet) {

	    builder.appendCarriageReturn();
	    builder.append(
		    MarkLogicModuleQueryBuilder.getInstance().getTemporalExtentUnionQuery(groupByTarget, forInVariable, groupByPeriod, true));
	    builder.appendComma();
	}

	if (countDistinctTargets.isPresent()) {

	    List<Queryable> list = countDistinctTargets.get();

	    for (Queryable queryable : list) {

		builder.appendCarriageReturn();
		builder.append(MarkLogicModuleQueryBuilder.getInstance().getCountDistinctQuery(//
			queryable.getName(), //
			groupByTarget, //
			forInVariable, //
			groupByPeriod));
		builder.appendComma();
	    }
	}

	if (freqTargets.isPresent()) {

	    List<Queryable> list = freqTargets.get();

	    for (Queryable queryable : list) {

		if (queryable == MetadataElement.BOUNDING_BOX || queryable == RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX) {

		    final XQueryBuilder bboxesBuilder = new XQueryBuilder();
		    bboxesBuilder.append("(");

		    RegionsManager.getRegions().forEach(value -> {

			JSONObject bbox = new JSONObject(value.toString());

			String south = bbox.get("south").toString();
			String west = bbox.get("west").toString();
			String north = bbox.get("north").toString();
			String east = bbox.get("east").toString();

			bboxesBuilder.append("'" + south + "#" + west + "#" + north + "#" + east + "',");
		    });

		    bboxesBuilder.cutLast();
		    bboxesBuilder.append(")");

		    builder.appendCarriageReturn();
		    builder.append(MarkLogicModuleQueryBuilder.getInstance().getGeoSpatialFrequencyFunctionQuery(//
			    queryable.getName(), //
			    bboxesBuilder.build(),
			    maxFrequencyItems.orElse(10), //
			    groupByTarget, //
			    forInVariable, //
			    groupByPeriod));

		    builder.appendComma();

		} else {

		    builder.appendCarriageReturn();

		    builder.append(MarkLogicModuleQueryBuilder.getInstance().getFrequencyFunctionQuery(//
			    queryable.getName(), //
			    maxFrequencyItems.orElse(10), //
			    groupByTarget, //
			    forInVariable, //
			    groupByPeriod));

		    builder.appendComma();
		}
	    }
	}

	appendFunction(maxTargets, builder, groupByTarget, Function.MAX, forInVariable, groupByPeriod);
	appendFunction(minTargets, builder, groupByTarget, Function.MIN, forInVariable, groupByPeriod);
	appendFunction(sumTargets, builder, groupByTarget, Function.SUM, forInVariable, groupByPeriod);
	appendFunction(avgTargets, builder, groupByTarget, Function.AVG, forInVariable, groupByPeriod);

	//
	// removes the last ','
	//
	builder.cutLast();

	builder.appendClosedBrace();
	builder.appendCarriageReturn();

	builder.appendClosedBrace();

	builder.appendCarriageReturn();

	return builder.build();
    }

    /**
     * @param optional
     * @param builder
     * @param groupByTarget
     * @param groupByPeriod
     */
    private void appendFunction(Optional<List<Queryable>> optional, XQueryBuilder builder, Optional<Queryable> groupByTarget,
	    Function function, String forInVariable, Optional<GroupByPeriod> groupByPeriod) {

	if (optional.isPresent()) {

	    List<Queryable> list = optional.get();

	    for (Queryable queryable : list) {

		builder.appendCarriageReturn();

		builder.append(MarkLogicModuleQueryBuilder.getInstance().getStatisticsFunctionQuery(//
			function.name(), //
			queryable.getName(), //
			groupByTarget, //
			forInVariable, //
			groupByPeriod));

		builder.appendComma();
	    }
	}
    }
}
