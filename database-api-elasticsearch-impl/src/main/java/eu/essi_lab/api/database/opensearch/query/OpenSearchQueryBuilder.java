/**
 * 
 */
package eu.essi_lab.api.database.opensearch.query;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.GeoShapeRelation;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.ExistsQuery;
import org.opensearch.client.opensearch._types.query_dsl.GeoShapeFieldQuery;
import org.opensearch.client.opensearch._types.query_dsl.GeoShapeQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchNoneQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery.Builder;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermsQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermsQueryField;
import org.opensearch.client.opensearch._types.query_dsl.WildcardQuery;
import org.opensearch.client.opensearch.core.msearch.MultisearchBody;
import org.opensearch.client.opensearch.core.msearch.MultisearchHeader;
import org.opensearch.client.opensearch.core.msearch.RequestItem;

import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.indexes.SpatialIndexHelper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.MetadataElement;
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
    private OpenSearchWrapper wrapper;

    /**
     * @param wrapper
     * @param ranking
     */
    public OpenSearchQueryBuilder(//
	    OpenSearchWrapper wrapper, //
	    RankingStrategy ranking, //
	    HashMap<String, String> dataFolderMap, //
	    boolean deletedIncluded) {

	this.wrapper = wrapper;
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

	return buildFilterQuery(buildSourceIdQuery(bond.getPropertyValue()), //
		buildMatchPhraseQuery(MetaFolderMapping.DATA_FOLDER, dataFolder));

    }

    /**
     * @param field
     * @param max
     * @param date
     * @return
     * @throws Exception
     */
    public Query buildMinMaxValueQuery(String field, boolean max, boolean date) throws Exception {

	return buildMinMaxValueQuery(buildMatchAllQuery(), field, max, date);
    }

    /**
     * @param query
     * @param field
     * @param max
     * @param date
     * @return
     * @throws Exception
     */
    public Query buildMinMaxValueQuery(Query query, String field, boolean max, boolean date) throws Exception {

	double minOrMax = wrapper.findMinMaxValue(//
		query, //
		field, //
		max);

	String stringVal = String.valueOf(minOrMax);

	Query out = OpenSearchQueryBuilder.buildRangeQuery(field, BondOperator.EQUAL, stringVal);

	if (query != null) {

	    out = buildFilterQuery(query, out);
	}

	return out;
    }

    /**
     * @param sourceId
     * @param operator
     * @return
     * @throws Exception
     */
    public Query buildMinMaxResourceTimeStampValue(String sourceId, BondOperator operator) throws Exception {

	Query sourceIdQuery = buildMatchAllQuery();

	if (sourceId != null) {

	    sourceIdQuery = OpenSearchQueryBuilder.buildRangeQuery(//
		    ResourceProperty.SOURCE_ID.getName(), //
		    BondOperator.EQUAL, //
		    sourceId);
	}

	return buildMinMaxValueQuery(//
		sourceIdQuery, //
		ResourceProperty.RESOURCE_TIME_STAMP.getName(), //
		operator == BondOperator.MAX, //
		true);
    }

    /**
     * @param bond
     * @return
     */
    public Query buildResourcePropertyQuery(ResourcePropertyBond bond) {

	ResourceProperty property = bond.getProperty();

	String value = bond.getPropertyValue();
	String name = property.getName();
	ContentType contentType = property.getContentType();

	BondOperator operator = bond.getOperator();

	if (operator == BondOperator.EXISTS) {

	    return buildExistsFieldQuery(name);

	}

	if (operator == BondOperator.NOT_EXISTS) {

	    return buildNotExistsFieldQuery(name);
	}

	if (operator == BondOperator.MAX || operator == BondOperator.MIN) {

	    //
	    // see BondFactory.createMinMaxResourceTimeStampBond and OAIPMH profiler
	    //
	    if (bond.getProperty() == ResourceProperty.RESOURCE_TIME_STAMP) {

		try {

		    return buildMinMaxResourceTimeStampValue(value, bond.getOperator());

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}
	    } else {
		//
		// see BondFactory.createMinMaxResourcePropertyBond
		//
		switch (bond.getProperty().getContentType()) {
		case DOUBLE:
		case INTEGER:
		case LONG:
		case ISO8601_DATE:
		case ISO8601_DATE_TIME:

		    try {
			return buildMinMaxValueQuery(name, operator == BondOperator.MAX, true);

		    } catch (Exception ex) {

			GSLoggerFactory.getLogger(getClass()).error(ex);
		    }

		default:
		    throw new IllegalArgumentException("Min/max query on non numeric field: " + name);
		}
	    }
	}

	switch (property) {
	case SOURCE_ID:

	    return buildSourceIdQuery(bond);

	case IS_GEOSS_DATA_CORE:

	    return buildIsGDCQuery(value);

	default:

	    if (contentType == ContentType.ISO8601_DATE || contentType == ContentType.ISO8601_DATE_TIME) {

		value = ConversionUtils.parseToLongString(value);
	    }

	    return buildRangeQuery(name, operator, value);
	}
    }

    /**
     * @param el
     * @param operator
     * @param value
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public Query buildMetadataElementQuery(MetadataElement el, BondOperator operator, String value) {

	switch (operator) {
	case EXISTS:
	    return buildExistsFieldQuery(el.getName());

	case NOT_EXISTS:
	    return buildNotExistsFieldQuery(el.getName());

	case NOT_EQUAL:
	case EQUAL:
	case GREATER:
	case GREATER_OR_EQUAL:
	case LESS:
	case LESS_OR_EQUAL:

	    if (el.getContentType() == ContentType.ISO8601_DATE || el.getContentType() == ContentType.ISO8601_DATE_TIME) {

		value = ConversionUtils.parseToLongString(value);
	    }

	    return buildRangeQuery(el.getName(), operator, value, ranking.computePropertyWeight(el));

	case TEXT_SEARCH:

	    List<String> terms = Arrays.asList(value.split(" "));

	    if (terms.size() == 1 || !isWildcardQuery(value)) {

		if (isWildcardQuery(value)) {

		    return buildWildCardQuery(DataFolderMapping.toKeywordField(el.getName()), value, ranking.computePropertyWeight(el));
		}

		return buildMatchPhraseQuery(el.getName(), value, ranking.computePropertyWeight(el));
	    }

	    return buildWildCardQuery(//
		    DataFolderMapping.toKeywordField(el.getName()), //
		    "*" + value + "*", //
		    ranking.computePropertyWeight(el));
	}

	throw new IllegalArgumentException("Operator " + operator + " not supported for field " + el.getName());
    }

    /**
     * @param value
     * @param operator
     */
    public Query buildSubjectQuery(String value, BondOperator operator) {

	return buildOrBoolQuery(//
		buildMetadataElementQuery(MetadataElement.KEYWORD, operator, value), //
		buildMetadataElementQuery(MetadataElement.TOPIC_CATEGORY, operator, value));

    }

    /**
     * @return
     */
    public Query build(boolean count) {

	Query searchQuery = ConversionUtils.toQuery(new JSONObject(builder.toString()));

	Query basicQuery = buildBasicQuery(count);

	return buildAndBoolQuery(searchQuery, basicQuery);
    }

    @SuppressWarnings("incomplete-switch")
    public Query buildGeoShapeQuery(SpatialBond bond) {

	JSONObject shape = buildEnvelope(bond);

	org.opensearch.client.opensearch._types.query_dsl.GeoShapeFieldQuery.Builder shapeBuilder = new GeoShapeFieldQuery.Builder().//
		shape(JsonData.of(shape.toMap()));

	Query weightedQuery = null;

	SpatialExtent extent = (SpatialExtent) bond.getPropertyValue();

	double w = extent.getWest();
	double e = extent.getEast();

	String north = String.valueOf(extent.getNorth());
	String south = String.valueOf(extent.getSouth());
	String east = String.valueOf(e);
	String west = String.valueOf(w);

	double area = SpatialIndexHelper.computeArea(//
		Double.valueOf(west), //
		Double.valueOf(east), //
		Double.valueOf(south), //
		Double.valueOf(north));

	BondOperator operator = bond.getOperator();
	switch (operator) {
	case BBOX:
	case INTERSECTS:

	    shapeBuilder = shapeBuilder.relation(GeoShapeRelation.Intersects);

	    break;

	case CONTAINED:

	    shapeBuilder = shapeBuilder.relation(GeoShapeRelation.Contains);

	    weightedQuery = buildContainedWeightQuery(area, 500);

	    break;

	case CONTAINS:

	    shapeBuilder = shapeBuilder.relation(GeoShapeRelation.Within);

	    List<Query> operands = new ArrayList<>();

	    for (int min = 0; min <= 90; min += 10) {

		operands.add(buildAreaWeightedQuery(min, area));
	    }

	    weightedQuery = buildOrBoolQuery(operands);

	    break;
	case DISJOINT:
	    shapeBuilder = shapeBuilder.relation(GeoShapeRelation.Disjoint);
	    break;
	case INTERSECTS_ANY_POINT_NOT_CONTAINS:
	    // not supported
	    break;
	}

	Query geoShapeQuery = new GeoShapeQuery.Builder().//
		field(MetadataElement.BOUNDING_BOX.getName()).//
		boost(1f).//
		shape(shapeBuilder.build()).//
		build().//
		toQuery();

	return weightedQuery == null ? geoShapeQuery : buildAndBoolQuery(weightedQuery, geoShapeQuery);
    }

    /**
     * @param value
     * @return
     */
    public static Query buildIsGDCQuery(String value) {

	ArrayList<Query> list = new ArrayList<Query>();

	List<String> ids = ConfigurationWrapper.getGDCSourceSetting().getSelectedSourcesIds();
	ids.forEach(id -> list.add(buildRangeQuery(//
		ResourceProperty.SOURCE_ID.getName(), BondOperator.EQUAL, id)));

	list.add(buildRangeQuery(ResourceProperty.IS_GEOSS_DATA_CORE.getName(), BondOperator.EQUAL, value));

	return buildFilterQuery(list);
    }

    /**
     * @param sourceId
     * @return
     */
    public static Query buildMinMaxQuery(String sourceId) {

	Query sourceIdQuery = buildRangeQuery(//
		ResourceProperty.SOURCE_ID.getName(), //
		BondOperator.EQUAL, //
		sourceId);

	Query indexQuery = buildIndexQuery(DataFolderMapping.get().getIndex());

	return buildFilterQuery(sourceIdQuery, indexQuery);

    }

    /**
     * @param databaseId
     * @return
     */
    public static Query buildSearchRegistryQuery(String databaseId) {

	return buildFilterQuery(buildDatabaseIdQuery(databaseId), buildRegistyIndexQuery());
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

	// return buildFilterQuery(buildDatabaseIdQuery(folder.getDatabase().getIdentifier()),
	// buildFolderNameQuery(folder));
    }

    /**
     * @param databaseId
     * @param sourceId
     * @return
     */
    public static Query buildDataFolderQuery(String databaseId, List<String> sourceIds) {

	ArrayList<Query> idsQueries = new ArrayList<Query>();
	sourceIds.forEach(id -> idsQueries.add(buildSourceIdQuery(id)));

	return buildFilterQuery(//
		buildDatabaseIdQuery(databaseId), //
		buildExistsFieldQuery(MetaFolderMapping.DATA_FOLDER), //
		buildIndexQuery(MetaFolderMapping.get().getIndex()), //
		buildOrBoolQuery(idsQueries)//
	);
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
     * <code>fieldValue</code>
     * 
     * @param databaseId
     * @param index
     * @param field
     * @param fieldValue
     * @return
     */
    public static Query buildSearchQuery(String databaseId, String field, String fieldValue) {

	return buildSearchQuery(databaseId, field, Arrays.asList(fieldValue));
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
    public static Query buildSearchQuery(String databaseId, String field, List<String> fieldValues) {

	List<Query> shouldList = new ArrayList<>();

	fieldValues.forEach(v -> {

	    shouldList.add(buildMatchPhraseQuery(field, v));
	});

	BoolQuery boolQuery = new BoolQuery.Builder().//

		filter(buildDatabaseIdQuery(databaseId)).//
		should(shouldList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
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
	
	return buildFilterQuery(buildDatabaseIdQuery(databaseId), buildIndexQuery(index));
    }

    /**
     * Builds a query which searches the entries in the database with id <code>databaseId</code>
     * 
     * @param databaseId
     * @param index
     * @return
     */
    public static Query buildSearchQuery(String databaseId) {

	return buildFilterQuery(buildDatabaseIdQuery(databaseId));

    }

    //
    //
    //

    /**
     * @param field
     * @return
     */
    public static Query buildExistsFieldQuery(String field) {

	return new ExistsQuery.Builder().field(field).build().toQuery();
    }

    /**
     * @param field
     * @return
     */
    public static Query buildNotExistsFieldQuery(String field) {

	return createNotQuery(buildExistsFieldQuery(field));
    }

    /**
     * Supported operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param field
     * @param operator
     * @param value
     * @param boost
     * @return
     */
    public static Query buildRangeQuery(String field, BondOperator operator, String value) {

	return buildRangeQuery(field, operator, value, 1);
    }

    /**
     * Supported operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * </ul>
     * 
     * @param field
     * @param operator
     * @param value
     * @param boost
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public static Query buildRangeQuery(String field, BondOperator operator, String value, double boost) {

	Builder builder = new RangeQuery.Builder().//
		field(value);

	if (boost > 1) {

	    builder = builder.boost(null);
	}

	switch (operator) {
	case EQUAL:

	    return buildMatchPhraseQuery(field, value);

	case NOT_EQUAL:

	    return createNotQuery(buildMatchPhraseQuery(field, value));

	case GREATER:

	    builder = builder.gt(JsonData.of(value)).field(field);
	    break;

	case GREATER_OR_EQUAL:

	    builder = builder.gte(JsonData.of(value)).field(field);
	    break;

	case LESS:

	    builder = builder.lt(JsonData.of(value)).field(field);
	    break;

	case LESS_OR_EQUAL:

	    builder = builder.lte(JsonData.of(value)).field(field);
	    break;
	}

	return builder.build().toQuery();
    }

    /**
     * @return
     */
    public static Query buildMatchAllQuery() {

	return new MatchAllQuery.Builder().build().toQuery();
    }

    /**
     * @return
     */
    public static Query buildMatchNoneQuery() {

	return new MatchNoneQuery.Builder().build().toQuery();
    }

    /**
     * @param distValues
     * @param target
     * @return
     */
    public static List<RequestItem> buildDistinctValuesItems(List<String> distValues, Queryable target) {

	return distValues.stream()

		.map(value -> buildMatchPhraseQuery(//
			DataFolderMapping.toKeywordField(target.getName()), value))//

		.map(query ->

		new RequestItem.Builder().//
			header(new MultisearchHeader.Builder().//
				index(DataFolderMapping.get().getIndex()).//

				build())
			.body(new MultisearchBody.Builder().//
				size(1).//
				query(query).//
				build())
			.//
			build()

		).collect(Collectors.toList());
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildFilterQuery(List<Query> operands) {

	return new BoolQuery.Builder().//
		filter(operands).//
		build().//
		toQuery();
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildFilterQuery(Query... operands) {

	return buildFilterQuery(Arrays.asList(operands));
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildAndBoolQuery(List<Query> operands) {

	return new BoolQuery.Builder().//
		must(operands).//
		build().//
		toQuery();
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildOrBoolQuery(List<Query> operands, int minimumShouldMatch) {

	return new BoolQuery.Builder().//
		should(operands).//
		minimumShouldMatch(String.valueOf(minimumShouldMatch)).//
		build().//
		toQuery();
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildOrBoolQuery(List<Query> operands) {

	return buildOrBoolQuery(operands, 1);
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildOrBoolQuery(int minimumShouldMatch, Query... operands) {

	return buildOrBoolQuery(Arrays.asList(operands), minimumShouldMatch);
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildOrBoolQuery(Query... operands) {

	return buildOrBoolQuery(Arrays.asList(operands), 1);
    }

    /**
     * @param operands
     * @return
     */
    private static Query buildAndBoolQuery(Query... operands) {

	return buildAndBoolQuery(Arrays.asList(operands));
    }

    /**
     * @param target
     * @param value
     * @return
     */
    private double percent(double target, int value) {

	return (target / 100) * value;
    }

    /**
     * @param minValue
     * @param area
     * @return
     */
    private Query buildAreaWeightedQuery(int minValue, double area) {

	int maxValue = minValue + 10;

	if (minValue == 0) {

	    return buildAndBoolQuery(//

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.GREATER_OR_EQUAL, String.valueOf(minValue),
			    ranking.computeBoundingBoxWeight(area, maxValue)), //

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.LESS,
			    String.valueOf(percent(area, maxValue)), ranking.computeBoundingBoxWeight(area, maxValue))//
	    );
	}

	if (minValue == 90) {

	    return buildAndBoolQuery(//

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.GREATER_OR_EQUAL, String.valueOf(minValue),
			    ranking.computeBoundingBoxWeight(area, maxValue)), //

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.LESS_OR_EQUAL, String.valueOf(area),
			    ranking.computeBoundingBoxWeight(area, maxValue))//
	    );
	}

	return buildAndBoolQuery(//

		buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.GREATER_OR_EQUAL,
			String.valueOf(percent(area, minValue)), ranking.computeBoundingBoxWeight(area, maxValue)), //

		buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.LESS, String.valueOf(percent(area, maxValue)),
			ranking.computeBoundingBoxWeight(area, maxValue))//
	);
    }

    /**
     * @param area
     * @param areaMultiplier how many times the resource area can be bigger than the target <code>area</code>
     * @return
     */
    private Query buildContainedWeightQuery(double area, int areaMultiplier) {

	int maxArea = areaMultiplier * 100;
	int steps = maxArea / 11;

	String innerQuery = "";

	int areaPercent = 1;

	ArrayList<Query> operands = new ArrayList<Query>();

	for (int weight = 0; weight <= 100; weight += 10) {

	    operands.add(buildAndBoolQuery(

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.GREATER_OR_EQUAL,
			    String.valueOf(percent(area, areaPercent)),

			    ranking.computeBoundingBoxWeight(area, weight)),

		    buildRangeQuery(BoundingBox.AREA_QUALIFIED_NAME.getLocalPart(), BondOperator.LESS,
			    String.valueOf(percent(area, areaPercent + steps)),

			    ranking.computeBoundingBoxWeight(area, weight))));

	    areaPercent += steps;
	}

	return buildOrBoolQuery(operands);
    }

    /**
     * @param value
     * @return
     */
    private boolean isWildcardQuery(String value) {

	return value.contains("*") || value.contains("?");
    }

    /**
     * @param term
     * @return
     */
    private String normalize(String term) {

	if (term.startsWith("*")) {

	    term = term.substring(1, term.length());
	}

	if (term.endsWith("*")) {

	    term = term.substring(0, term.length() - 1);
	}

	return term;
    }

    /**
     * @param bond
     * @return
     */
    private static JSONObject buildEnvelope(SpatialBond bond) {

	SpatialExtent extent = (SpatialExtent) bond.getPropertyValue();

	JSONObject object = new JSONObject();
	object.put("type", "envelope");

	JSONArray coord = new JSONArray();

	object.put("coordinates", coord);

	JSONArray nw = new JSONArray();
	nw.put(extent.getWest());
	nw.put(extent.getNorth());

	JSONArray se = new JSONArray();
	se.put(extent.getEast());
	se.put(extent.getSouth());

	coord.put(nw);
	coord.put(se);

	return object;
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

    //
    //
    //

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

    //
    //
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
	// list.add(buildEVWeightQuery());
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

	Query missingField = createNotQuery(buildExistsFieldQuery(ResourceProperty.IS_DELETED.getName()));

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

    /**
     * @param query
     * @return
     */
    private static Query createNotQuery(Query query) {

	return new BoolQuery.Builder().//
		mustNot(query).//
		build().//
		toQuery();
    }

    //
    //
    //

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
    private static Query buildMatchQuery(String field, String value, float boost) {

	org.opensearch.client.opensearch._types.query_dsl.MatchQuery.Builder builder = new MatchQuery.Builder().//
		field(field).//
		query(new FieldValue.Builder().stringValue(value).build()).//
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
     * @param field
     * @param value
     * @return
     */
    private static Query buildTermQuery(String field, String value) {

	return new TermQuery.Builder().//
		field(field).//
		value(new FieldValue.Builder().stringValue(value).build()).//
		build().//
		toQuery();

    }

    /**
     * @param field
     * @param values
     * @return
     */
    private static Query buildTermsQuery(String field, List<String> values) {

	return new TermsQuery.Builder().//
		field(field).//
		terms(new TermsQueryField.Builder().//
			value(values.//
				stream().//
				map(v -> new FieldValue.Builder().stringValue(v).build()).//
				collect(Collectors.toList()))
			.build())
		.build().//
		toQuery();
    }

    /**
     * @param field
     * @param value
     * @param boost
     * @return
     */
    private static Query buildWildCardQuery(String field, String value, float boost) {

	org.opensearch.client.opensearch._types.query_dsl.WildcardQuery.Builder builder = new WildcardQuery.Builder().//
		field(field).//
		caseInsensitive(true).//
		value(value).//
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
