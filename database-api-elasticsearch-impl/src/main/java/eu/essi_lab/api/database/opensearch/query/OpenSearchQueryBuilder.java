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
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 */
public class OpenSearchQueryBuilder {

    private HashMap<String, String> dfMap;
    private StringBuilder builder;

    /**
     * 
     */
    public OpenSearchQueryBuilder(HashMap<String, String> dataFolderMap) {

	this.dfMap = dataFolderMap;
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
			buildFieldQuery(MetaFolderMapping.DATA_FOLDER, dataFolder)//

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
    public static Query buildDataFolderPostfixQuery(String databaseId, String sourceId) {

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(buildDatabaseIdQuery(databaseId), //
			buildIndexQuery(MetaFolderMapping.get().getIndex()), //
			buildSourceIdQuery(sourceId))
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

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(filterList).//
		should(shouldList).//
		minimumShouldMatch("1").//

		build();

	return boolQuery.toQuery();
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

	    shouldList.add(buildFieldQuery(field, v));
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
    public Query build() {

	return ConversionUtils.toQuery(new JSONObject(builder.toString()));
    }

    /**
     * @param creator
     * @return
     */
    private static Query buildViewCreatorQuery(String creator) {

	return buildFieldQuery(ViewsMapping.VIEW_CREATOR, creator);
    }

    /**
     * @param visibility
     * @return
     */
    private static Query buildViewVisibilityQuery(String visibility) {

	return buildFieldQuery(ViewsMapping.VIEW_VISIBILITY, visibility);
    }

    /**
     * @param owner
     * @return
     */
    private static Query buildViewOwnerQuery(String owner) {

	return buildFieldQuery(ViewsMapping.VIEW_OWNER, owner);
    }

    /**
     * @return
     */
    private static Query buildRegistyIndexQuery() {

	return buildFieldQuery(IndexData._INDEX, FolderRegistryMapping.get().getIndex());
    }

    /**
     * @param databaseId
     */
    private static Query buildDatabaseIdQuery(String databaseId) {

	return buildFieldQuery(IndexData.DATABASE_ID, databaseId);
    }

    /**
     * @param folder
     * @return
     */
    private static Query buildFolderNameQuery(OpenSearchFolder folder) {

	return buildFieldQuery(IndexData.FOLDER_NAME, folder.getName());
    }

    /**
     * @param sourceId
     * @return
     */
    private static Query buildSourceIdQuery(String sourceId) {

	return buildFieldQuery(MetaFolderMapping.SOURCE_ID, sourceId);
    }

    /**
     * @param index
     * @return
     */
    private static Query buildIndexQuery(String index) {

	return buildFieldQuery(IndexData._INDEX, index);
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

    /**
     * @param field
     * @param value
     * @return
     */
    private static Query buildFieldQuery(String field, String value) {

	return new MatchPhraseQuery.Builder().//
		field(field).//
		query(value).//
		build().//
		toQuery();

    }
}
