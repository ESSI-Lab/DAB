/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import org.apache.jena.sparql.function.library.leviathan.sec;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery.Builder;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.RankingStrategy;

/**
 * @author Fabrizio
 */
public class OpenSearchDiscoveryBondHandler implements DiscoveryBondHandler {

    private RankingStrategy ranking;
    private boolean dataFolderCheckEnabled;
    private boolean deletedIncluded;
    private int maxFrequencyMapItems;
    private List<Queryable> tfTargets;
    private Optional<OrderingDirection> orderingDirection;
    private Optional<Queryable> orderingProperty;

    private Builder builder;

    private List<Query> mustList;
    private List<Query> queryList;
    private List<Query> mustNotList;
    private Stack<StackEntry> stack;

    /**
     * @param message
     */
    public OpenSearchDiscoveryBondHandler(DiscoveryMessage message) {

	this.ranking = message.getRankingStrategy();
	this.dataFolderCheckEnabled = message.isDataFolderCheckEnabled();
	this.deletedIncluded = message.isDeletedIncluded();
	this.maxFrequencyMapItems = message.getMaxFrequencyMapItems();
	this.tfTargets = message.getTermFrequencyTargets();
	this.orderingDirection = message.getOrderingDirection();
	this.orderingProperty = message.getOrderingProperty();

	this.mustList = new ArrayList<>();
	this.queryList = new ArrayList<>();
	this.mustNotList = new ArrayList<>();

	this.stack = new Stack<>();

    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	BoolQuery boolQuery = new BoolQuery.Builder().//
		build();

	stack.push(StackEntry.of(bond.getLogicalOperator(), boolQuery));
    }

    private static class StackEntry {

	private LogicalOperator op;
	private BoolQuery query;

	static StackEntry of(LogicalOperator op, BoolQuery query) {

	    return new StackEntry(op, query);
	}

	private StackEntry(LogicalOperator op, BoolQuery query) {
	    this.op = op;
	    this.query = query;

	}

	/**
	 * @return
	 */
	protected LogicalOperator getOperator() {
	    return op;
	}

	/**
	 * @return
	 */
	protected BoolQuery getQuery() {
	    return query;
	}

    }

    @Override
    public void endLogicalBond(LogicalBond bond) {

	StackEntry entry = stack.pop();
	LogicalOperator operator = entry.getOperator();

	BoolQuery query = entry.getQuery();

	switch (operator) {
	case AND:

	    if (!query.must().isEmpty()) {

		this.queryList.add(query.toQuery());

	    } else {

		builder = new BoolQuery.Builder().//
			must(this.queryList).//
			build().//
			toBuilder();
		this.queryList = new ArrayList<>();
	    }

	    break;

	case OR:

	    if (!query.should().isEmpty()) {

		this.queryList.add(query.toQuery());

	    } else {

		builder = new BoolQuery.Builder().//
			should(this.queryList).//
			minimumShouldMatch("1").//
			build().//
			toBuilder();

		this.queryList = new ArrayList<>();

	    }

	    break;

	case NOT:

	    if (!query.mustNot().isEmpty()) {

		this.queryList.add(query.toQuery());

	    } else {

		builder = new BoolQuery.Builder().//
			mustNot(this.queryList).//
			build().//
			toBuilder();

		this.queryList = new ArrayList<>();
	    }

	    break;
	}

	 
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	MatchPhraseQuery query = new MatchPhraseQuery.Builder().//
		field(bond.getProperty().getName()).//
		query(bond.getPropertyValue()).//
		build();

	int index = stack.indexOf(stack.peek());

	BoolQuery boolQuery = null;

	switch (stack.peek().getOperator()) {
	case AND:

	    List<Query> mustList = new ArrayList<>(stack.peek().getQuery().must());
	    mustList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    must(mustList).//
		    build();

	    break;
	case OR:

	    List<Query> shouldList = new ArrayList<>(stack.peek().getQuery().should());
	    shouldList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    should(shouldList).//
		    minimumShouldMatch("1").//
		    build();

	    break;

	case NOT:

	    List<Query> mustNotList = new ArrayList<>(stack.peek().getQuery().mustNot());
	    mustNotList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    mustNot(mustNotList).//
		    build();

	    break;
	}

	stack.set(index, StackEntry.of(stack.peek().getOperator(), boolQuery));
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MatchPhraseQuery query = new MatchPhraseQuery.Builder().//
		field(bond.getProperty().getName()).//
		query(bond.getPropertyValue()).//
		build();

	int index = stack.indexOf(stack.peek());

	BoolQuery boolQuery = null;

	switch (stack.peek().getOperator()) {
	case AND:

	    List<Query> mustList = new ArrayList<>(stack.peek().getQuery().must());
	    mustList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    must(mustList).//
		    build();

	    break;
	case OR:

	    List<Query> shouldList = new ArrayList<>(stack.peek().getQuery().should());
	    shouldList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    should(shouldList).//
		    minimumShouldMatch("1").//
		    build();

	    break;

	case NOT:

	    List<Query> mustNotList = new ArrayList<>(stack.peek().getQuery().mustNot());
	    mustNotList.add(query.toQuery());

	    boolQuery = new BoolQuery.Builder().//
		    mustNot(mustNotList).//
		    build();

	    break;
	}

	stack.set(index, StackEntry.of(stack.peek().getOperator(), boolQuery));
    }

    @Override
    public void separator() {

	System.out.println("SEPARATOR");
    }

    @Override
    public void spatialBond(SpatialBond bond) {

    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    /**
     * @return
     */
    public Query getQuery() {

	return builder.build().toQuery();
    }

    public static void main(String[] args) {

	//
	//
	//

	BoolQuery query1 = createQuery1();
	BoolQuery query2 = createQuery2();

	// BoolQuery.of(b -> b.)

	BoolQuery bool_query1 = BoolQuery.of(b -> b

		.must(t -> t.match(r -> r.field("metaData.cluster").query(FieldValue.of("value1")))) //
		.must(t -> t
			.bool(BoolQuery.of(bq -> bq.should(s -> s.match(m -> m.field("city.keyword").query(FieldValue.of("value1"))))))));
	//
	//
	//

	// System.out.println(ConversionUtils.toJsonObject(bool_query1.toQuery()).toString(3));

	Query query = query1.toBuilder().must(query2.toQuery()).build().toQuery();

	System.out.println(ConversionUtils.toJsonObject(query).toString(3));

    }

    /**
     * @return
     */
    private static BoolQuery createQuery1() {

	MatchPhraseQuery databaseIdQuery1 = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query("databaseId1").//
		build();

	MatchPhraseQuery folderNameQuery1 = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).query("folderName1").//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery1.toQuery());
	mustList.add(folderNameQuery1.toQuery());

	MatchPhraseQuery databaseIdQuery4 = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).//
		query("databaseId4").//
		build();

	MatchPhraseQuery folderNameQuery5 = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).//
		query("folderName5").//
		build();

	List<Query> mustNotList = new ArrayList<>();
	mustNotList.add(databaseIdQuery4.toQuery());
	mustNotList.add(folderNameQuery5.toQuery());

	mustList.add(databaseIdQuery1.toQuery());
	mustList.add(folderNameQuery1.toQuery());

	List<Query> shouldList = new ArrayList<>();

	MatchPhraseQuery index1Query = new MatchPhraseQuery.Builder().//
		field("_index").//
		query("index1").//
		build();
	MatchPhraseQuery index2Query = new MatchPhraseQuery.Builder().//
		field("_index").//
		query("index2").//
		build();

	shouldList.add(index1Query.toQuery());
	shouldList.add(index2Query.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		mustNot(mustNotList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery;
    }

    /**
     * @return
     */
    private static BoolQuery createQuery2() {

	MatchPhraseQuery databaseIdQuery1 = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query("databaseId2").//
		build();

	MatchPhraseQuery folderNameQuery1 = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).query("folderName2").//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery1.toQuery());
	mustList.add(folderNameQuery1.toQuery());

	List<Query> shouldList = new ArrayList<>();

	MatchPhraseQuery index1Query = new MatchPhraseQuery.Builder().//
		field("_index").//
		query("index2").//
		build();
	MatchPhraseQuery index2Query = new MatchPhraseQuery.Builder().//
		field("_index").//
		query("index3").//
		build();

	shouldList.add(index1Query.toQuery());
	shouldList.add(index2Query.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery;
    }

}
