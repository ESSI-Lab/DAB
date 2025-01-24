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

    private HashMap<String, String> dataFolderMap;
    private StringBuilder builder;

    /**
     * 
     */
    public OpenSearchQueryBuilder(HashMap<String, String> dataFolderMap) {

	this.dataFolderMap = dataFolderMap;
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
     *  
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

	MatchPhraseQuery sourceIdQuery = new MatchPhraseQuery.Builder().//
		field(bond.getProperty().getName()).//
		query(bond.getPropertyValue()).//
		build();

	MatchPhraseQuery dataFolderQuery = new MatchPhraseQuery.Builder().//
		field(MetaFolderMapping.DATA_FOLDER).//
		query(this.dataFolderMap.get(bond.getPropertyValue())).//
		build();

	ArrayList<Query> filterList = new ArrayList<>();
	filterList.add(sourceIdQuery.toQuery());
	filterList.add(dataFolderQuery.toQuery());

	return new BoolQuery.Builder().//
		filter(filterList).//
		build().//
		toQuery();
    }

    /**
     * @param databaseId
     * @return
     */
    public static Query buildSearchRegistryQuery(String databaseId) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	MatchPhraseQuery folderRegistryIndexQuery = new MatchPhraseQuery.Builder().//
		field("_index").query(FolderRegistryMapping.get().getIndex()).//
		build();

	List<Query> queryList = Arrays.asList(//
		databaseIdQuery.toQuery(), //
		folderRegistryIndexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().must(queryList).build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches all entries of the given <code>folder</code>.<br>
     * <b>Constraints</b>: databaseId = getDatabase().getIdentifier() AND folderName = getName()
     */
    public static Query buildSearchEntriesQuery(OpenSearchFolder folder) {

	// MatchQuery databaseIdQuery = new MatchQuery.Builder().//
	// field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
	// stringValue(folder.getDatabase().getIdentifier()).//
	// build())
	// .//
	// build();
	//
	// MatchQuery folderNameQuery = new MatchQuery.Builder().//
	// field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
	// stringValue(folder.getName()).//
	// build())
	// .//
	// build();

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(folder.getDatabase().getIdentifier()).//
		build();

	MatchPhraseQuery folderNameQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).query(folder.getName()).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(folderNameQuery.toQuery());

	List<Query> shouldList = buildIndexesQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
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

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).//
		query(databaseId).//
		build();

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(MetaFolderMapping.get().getIndex()).//
		build();

	MatchPhraseQuery sourceIdQuery = new MatchPhraseQuery.Builder().//
		field(MetaFolderMapping.SOURCE_ID).//
		query(sourceId).//
		build();

	List<Query> filterList = new ArrayList<>();
	filterList.add(databaseIdQuery.toQuery());
	filterList.add(indexQuery.toQuery());
	filterList.add(sourceIdQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		filter(filterList).//
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

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).//
		query(databaseId).//
		build();

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(ViewsMapping.get().getIndex()).//
		build();

	List<Query> mustList = new ArrayList<>();

	if (creator.isPresent()) {

	    MatchPhraseQuery creatorQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_CREATOR).//
		    query(creator.get()).//
		    build();

	    mustList.add(creatorQuery.toQuery());
	}

	if (owner.isPresent()) {

	    MatchPhraseQuery ownerQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_OWNER).//
		    query(owner.get()).//
		    build();

	    mustList.add(ownerQuery.toQuery());
	}

	if (visibility.isPresent()) {

	    MatchPhraseQuery visibilityQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_VISIBILITY).//
		    query(visibility.get().name()).//
		    build();
	    mustList.add(visibilityQuery.toQuery());
	}

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	List<Query> shouldList = buildIndexesQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		minimumShouldMatch("1").//

		build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches the entries with the given <code>property</code> and
     * <code>propertyValue</code>
     * 
     * @param databaseId
     * @param index
     * @param property
     * @param propertyValue
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String index, String property, String propertyValue) {

	return buildSearchQuery(databaseId, index, property, Arrays.asList(propertyValue));
    }

    /**
     * Builds a query which searches the entries with the given <code>property</code> and
     * matching one or more <code>propertyValues</code>
     * 
     * @param databaseId
     * @param index
     * @param property
     * @param propertyValues
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String index, String property, List<String> propertyValues) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	List<Query> shouldList = new ArrayList<>();

	propertyValues.forEach(v -> {

	    MatchPhraseQuery propertyQuery = new MatchPhraseQuery.Builder().//
		    field(property).//
		    query(v).//
		    build();

	    shouldList.add(propertyQuery.toQuery());
	});

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(index).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		should(shouldList).//
		must(mustList).//
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

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(index).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
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
     * @return
     */
    private static List<Query> buildIndexesQuery() {
    
        List<String> indexes = IndexMapping.getIndexes();
    
        List<Query> queryList = new ArrayList<>();
    
        indexes.forEach(index -> {
    
            queryList.add(new MatchPhraseQuery.Builder().//
        	    field("_index").//
        	    query(index).//
        	    build().//
        	    toQuery());
    
        });
    
        return queryList;
    }

    /**
     * Builds a query which searches the entry of the given <code>folder</code>
     * having the given <code>key</code>.<br>
     * <b>Constraints</b>: databaseId = getDatabase().getIdentifier() AND folderName = getName() AND key = key
     * 
     * @param key
     */
    private Query buildSearchQuery(OpenSearchFolder folder, String key) {
    
        // MatchQuery databaseIdQuery = new MatchQuery.Builder().//
        // field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
        // stringValue(folder.getDatabase().getIdentifier()).//
        // build())
        // .//
        // build();
        //
        // MatchQuery folderNameQuery = new MatchQuery.Builder().//
        // field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
        // stringValue(folder.getName()).//
        // build())
        // .//
        // build();
        //
        // MatchQuery keyQuery = new MatchQuery.Builder().//
        // field(IndexData.ENTRY_NAME).query(new FieldValue.Builder().//
        // stringValue(key).//
        // build())
        // .//
        // build();
    
        MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
        	field(IndexData.DATABASE_ID).query(folder.getDatabase().getIdentifier()).//
        	build();
    
        MatchPhraseQuery folderNameQuery = new MatchPhraseQuery.Builder().//
        	field(IndexData.FOLDER_NAME).query(folder.getName()).//
        	build();
    
        MatchPhraseQuery keyQuery = new MatchPhraseQuery.Builder().//
        	field(IndexData.ENTRY_NAME).query(key).//
        	build();
    
        List<Query> mustList = new ArrayList<>();
    
        mustList.add(databaseIdQuery.toQuery());
        mustList.add(folderNameQuery.toQuery());
        mustList.add(keyQuery.toQuery());
    
        List<Query> shouldList = buildIndexesQuery();
    
        BoolQuery boolQuery = new BoolQuery.Builder().//
        	must(mustList).//
        	should(shouldList).//
        	minimumShouldMatch("1").//
        	build();
    
        return boolQuery.toQuery();
    }
}
