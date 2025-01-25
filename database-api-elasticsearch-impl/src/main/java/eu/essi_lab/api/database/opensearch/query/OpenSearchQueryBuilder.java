/**
 * 
 */
package eu.essi_lab.api.database.opensearch.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.ExistsQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery.Builder;

import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchQueryBuilder {

    private HashMap<String, String> dfMap;
    private StringBuilder builder;
    private RankingStrategy ranking;
    private boolean deletedIncluded;

    /**
     * @param ranking
     */
    public OpenSearchQueryBuilder(RankingStrategy ranking, HashMap<String, String> dataFolderMap, boolean deletedIncluded) {

	this.ranking = ranking;
	this.dfMap = dataFolderMap;
	this.deletedIncluded = deletedIncluded;
	this.builder = new StringBuilder();
    }

    /**
     * @param query
     */
    public void append(Query query) {

	builder.append(ConversionUtils.toJSONObject(query).toString(3));
    }

    /**
     * 
     */
    public void appendSeparator() {

	builder.append(", \n");
    }

    /**
     *  
     */
    public void appendBoolMustOpenTag() {

	builder.append("{ \"bool\": \n{ \"must\": [ \n ");
    }

    /**
     *  
     */
    public void appendBoolShouldOpenTag() {

	builder.append("{ \"bool\": \n{ \"should\": [ \n ");
    }

    /**
     *  
     */
    public void appendBoolMustNotOpenTag() {

	builder.append("{ \"bool\": \n{ \"mustNot\": [ \n ");
    }

    /**
     * @param minimumShouldMatch
     */
    public void appendClosingTag(boolean minimumShouldMatch) {

	if (minimumShouldMatch) {

	    builder.append("\n], \"minimum_should_match\": \"1\" \n} \n}");

	} else {

	    builder.append("\n] \n} \n}");
	}
    }

    /**
     * @param bond
     * @return
     */
    public Query buildSourceIdQuery(ResourcePropertyBond bond) {

	String dataFolder = dfMap.get(bond.getPropertyValue());

	if (dataFolder == null) {
	    // it is null in case of distributed source
	    return buildSourceIdQuery(bond.getPropertyValue());
	}

	return new BoolQuery.Builder().//
		filter(//
			buildSourceIdQuery(bond.getPropertyValue()), //
			buildMatchPhraseQuery(MetaFolderMapping.DATA_FOLDER, dataFolder)//

		).//
		build().//
		toQuery();
    }

    /**
     * @param databaseId
     * @return
     */
    public static Query buildSearchRegistryQuery(String databaseId) {

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(buildDatabaseIdQuery(databaseId), //
			buildRegistyIndexQuery())
		.build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches all entries of the given <code>folder</code>.<br>
     * <b>Constraints</b>: databaseId = getDatabase().getIdentifier() AND folderName = getName()
     */
    public static Query buildSearchEntriesQuery(OpenSearchFolder folder) {

	BoolQuery boolQuery = new BoolQuery.Builder().//

		filter(buildDatabaseIdQuery(folder.getDatabase().getIdentifier()), //
			buildFolderNameQuery(folder))
		.//

		should(buildIndexesQueryList()).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @param databaseId
     * @param sourceId
     * @return
     */
    public static Query buildDataFolderQuery(String databaseId, List<String> sourceIds) {

	ArrayList<Query> idsQueries = new ArrayList<Query>();
	sourceIds.forEach(id -> idsQueries.add(buildSourceIdQuery(id)));

	Query query = new BoolQuery.Builder().should(idsQueries).minimumShouldMatch("1").build().toQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(buildDatabaseIdQuery(databaseId), //

			buildExistsFieldQuery(MetaFolderMapping.DATA_FOLDER), //

			buildIndexQuery(MetaFolderMapping.get().getIndex()), //

			query)
		.//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @param databaseId
     * @param creator
     * @param owner
     * @param visibility
     * @return
     */
    public static Query buildSearchViewsQuery(//
	    String databaseId, //
	    Optional<String> creator, //
	    Optional<String> owner, //
	    Optional<ViewVisibility> visibility) {

	Query databaseIdQuery = buildDatabaseIdQuery(databaseId);

	Query indexQuery = buildIndexQuery(ViewsMapping.get().getIndex());

	List<Query> filterList = new ArrayList<>();

	if (creator.isPresent()) {

	    filterList.add(buildViewCreatorQuery(creator.get()));
	}

	if (owner.isPresent()) {

	    filterList.add(buildViewOwnerQuery(owner.get()));
	}

	if (visibility.isPresent()) {

	    filterList.add(buildViewVisibilityQuery(visibility.get().name()));
	}

	filterList.add(databaseIdQuery);
	filterList.add(indexQuery);

	List<Query> shouldList = buildIndexesQueryList();

	return new BoolQuery.Builder().//
		filter(filterList).//
		should(shouldList).//
		minimumShouldMatch("1").//
		build().//
		toQuery();
    }

    /**
     * Builds a query which searches the entries with the given <code>field</code> and
     * <code>fieldValue</code>
     * 
     * @param databaseId
     * @param index
     * @param field
     * @param fieldValue
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String index, String field, String fieldValue) {

	return buildSearchQuery(databaseId, index, field, Arrays.asList(fieldValue));
    }

    /**
     * Builds a query which searches the entries with the given <code>field</code> and
     * matching one or more <code>fieldValues</code>
     * 
     * @param databaseId
     * @param index
     * @param field
     * @param fieldValues
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String index, String field, List<String> fieldValues) {

	List<Query> shouldList = new ArrayList<>();

	fieldValues.forEach(v -> {

	    shouldList.add(buildMatchPhraseQuery(field, v));
	});

	BoolQuery boolQuery = new BoolQuery.Builder().//

		filter(buildDatabaseIdQuery(databaseId), //
			buildIndexQuery(index))
		.//
		should(shouldList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches the entries in the database with id <code>databaseId</code> and in the index
     * <code>index</code>
     * 
     * @param databaseId
     * @param index
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String index) {

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(buildDatabaseIdQuery(databaseId), //
			buildIndexQuery(index)//
		).//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @return
     */
    public Query build(boolean count) {

	Query searchQuery = ConversionUtils.toQuery(new JSONObject(builder.toString()));

	Query basicQuery = buildBasicQuery(count);

	Query indexQuery = buildIndexQuery(DataFolderMapping.get().getIndex());

	return new BoolQuery.Builder().//
		must(searchQuery, basicQuery, indexQuery).//
		build().//
		toQuery();
    }

    /**
     * @param creator
     * @return
     */
    private static Query buildViewCreatorQuery(String creator) {

	return buildMatchPhraseQuery(ViewsMapping.VIEW_CREATOR, creator);
    }

    /**
     * @param visibility
     * @return
     */
    private static Query buildViewVisibilityQuery(String visibility) {

	return buildMatchPhraseQuery(ViewsMapping.VIEW_VISIBILITY, visibility);
    }

    /**
     * @param owner
     * @return
     */
    private static Query buildViewOwnerQuery(String owner) {

	return buildMatchPhraseQuery(ViewsMapping.VIEW_OWNER, owner);
    }

    /**
     * @return
     */
    private static Query buildRegistyIndexQuery() {

	return buildMatchPhraseQuery(IndexData._INDEX, FolderRegistryMapping.get().getIndex());
    }

    /**
     * @param databaseId
     */
    private static Query buildDatabaseIdQuery(String databaseId) {

	return buildMatchPhraseQuery(IndexData.DATABASE_ID, databaseId);
    }

    /**
     * @param folder
     * @return
     */
    private static Query buildFolderNameQuery(OpenSearchFolder folder) {

	return buildMatchPhraseQuery(IndexData.FOLDER_NAME, folder.getName());
    }

    /**
     * @param sourceId
     * @return
     */
    private static Query buildSourceIdQuery(String sourceId) {

	return buildMatchPhraseQuery(MetaFolderMapping.SOURCE_ID, sourceId);
    }

    /**
     * @param index
     * @return
     */
    private static Query buildIndexQuery(String index) {

	return buildMatchPhraseQuery(IndexData._INDEX, index);
    }

    /**
     * @return
     */
    private static Query buildMatchAllQuery() {

	return new MatchAllQuery.Builder().build().toQuery();
    }

    /**
     * @return
     */
    private static List<Query> buildIndexesQueryList() {

	List<String> indexes = IndexMapping.getIndexes();

	List<Query> queryList = new ArrayList<>();

	indexes.forEach(index -> {

	    queryList.add(buildIndexQuery(index));
	});

	return queryList;
    }

    //
    // base queries
    //

    /**
     * The basic query. The constraints are GEOSS Data Core, metadata quality, essential variables and access quality.
     * This query also allows to filter in/out the deleted records.
     * For a count query, the weight query is omitted in order to resize the overall query
     */
    private Query buildBasicQuery(boolean count) {

	if (count) {

	    return deletedIncluded ? buildDeletedExcludedQuery() : buildDeletedExcludedQuery();
	}

	ArrayList<Query> list = new ArrayList<>();

	// an always true query is required in order to get results in case
	// all the others constraints do not match
	list.add(buildMatchAllQuery());
	list.add(buildGDCWeightQuery());
	list.add(buildMDQWeightQuery());
	list.add(buildEVWeightQuery());
	list.add(buildAQWeightQuery());

	if (!deletedIncluded) {

	    list.add(buildDeletedExcludedQuery());
	}

	return new BoolQuery.Builder().//
		should(list).//
		minimumShouldMatch("1").//
		build().//
		toQuery();
    }

    /**
     * @return
     */
    private Query buildDeletedExcludedQuery() {

	Query missingField = new BoolQuery.Builder().//
		mustNot(buildExistsFieldQuery(ResourceProperty.IS_DELETED.getName())).build().//
		toQuery();

	return new BoolQuery.Builder().//
		should(missingField, //
			buildMatchPhraseQuery(ResourceProperty.IS_DELETED.getName(), "false"))
		.//
		minimumShouldMatch("1").//
		build().//
		toQuery();
    }

    /**
     * @return
     */
    private Query buildGDCWeightQuery() {

	ArrayList<Query> shouldList = new ArrayList<>();

	shouldList.add(buildMatchPhraseQuery(ResourceProperty.IS_GEOSS_DATA_CORE.getName(), "false"));
	shouldList.add(buildMatchPhraseQuery(ResourceProperty.IS_GEOSS_DATA_CORE.getName(), "true", //
		ranking.computePropertyWeight(ResourceProperty.IS_GEOSS_DATA_CORE)));

	return new BoolQuery.Builder().//
		should(shouldList).//
		minimumShouldMatch("1").//
		build().//
		toQuery();
    }

    /**
     * @return
     */
    private Query buildMDQWeightQuery() {

	return buildWeightQuery(ResourceProperty.METADATA_QUALITY.getName());
    }

    /**
     * @return
     */
    private Query buildEVWeightQuery() {

	return buildWeightQuery(ResourceProperty.ESSENTIAL_VARS_QUALITY.getName());
    }

    /**
     * @return
     */
    private Query buildAQWeightQuery() {

	return buildWeightQuery(ResourceProperty.ACCESS_QUALITY.getName());
    }

    /**
     * @param field
     * @return
     */
    private Query buildWeightQuery(String field) {

	ArrayList<Query> list = new ArrayList<>();

	for (int i = 1; i <= RankingStrategy.MAX_VARIABLE_VALUE; i++) {

	    list.add(//
		    buildMatchPhraseQuery(field, String.valueOf(i), //
			    ranking.computeRangeWeight(field, i)));
	}

	BoolQuery boolQuery = new BoolQuery.Builder().//
		should(list).//
		build();

	return boolQuery.toQuery();
    }

    //
    //
    //

    /**
     * @param field
     * @return
     */
    private static Query buildExistsFieldQuery(String field) {

	return new ExistsQuery.Builder().field(field).build().toQuery();
    }

    /**
     * @param field
     * @param operator
     * @param value
     * @param boost
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    private Query buildRangeQuery(String field, BondOperator operator, String value, float boost) {

	Builder builder = new RangeQuery.Builder().//
		field(value).//
		boost(boost);//

	switch (operator) {
	case GREATER:

	    builder = builder.gt(JsonData.of(value));
	    break;

	case GREATER_OR_EQUAL:

	    builder = builder.gte(JsonData.of(value));
	    break;

	case LESS:

	    builder = builder.lt(JsonData.of(value));
	    break;

	case LESS_OR_EQUAL:

	    builder = builder.lte(JsonData.of(value));
	    break;
	}

	return builder.build().toQuery();
    }

    /**
     * @see https://opensearch.org/docs/latest/query-dsl/term-vs-full-text/
     * @param field
     * @param value
     * @return
     */
    private static Query buildMatchPhraseQuery(String field, String value, float boost) {

	org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery.Builder builder = new MatchPhraseQuery.Builder().//
		field(field).//
		query(value).//
		build().//
		toBuilder();

	if (boost > 1) {

	    builder = builder.boost(boost);
	}

	return builder.//
		build().//
		toQuery();
    }

    /**
     * @see https://opensearch.org/docs/latest/query-dsl/term-vs-full-text/
     * @param field
     * @param value
     * @return
     */
    private static Query buildMatchPhraseQuery(String field, String value) {

	return buildMatchPhraseQuery(field, value, 1);
    }
}
