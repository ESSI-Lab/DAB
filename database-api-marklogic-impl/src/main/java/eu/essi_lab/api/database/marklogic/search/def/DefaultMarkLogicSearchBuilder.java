package eu.essi_lab.api.database.marklogic.search.def;

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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSpatialQueryBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.indexes.CustomIndexedElements;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.indexes.IndexedResourceElements;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio https://developer.marklogic.com/try/ninja/page10
 *         https://help.marklogic.com/knowledgebase/article/View/73/0/what-is-a-
 *         directory-in-marklogic https://docs.marklogic.com/guide/app-dev/properties
 */
public class DefaultMarkLogicSearchBuilder implements MarkLogicSearchBuilder {

    /**
     * Maximum number of term frequency items
     */
    private int maxFrequencyMapItems;
    private boolean deletedIncluded;
    protected boolean dataFolderCheckEnabled;
    protected RankingStrategy ranking;
    protected MarkLogicDatabase markLogicDB;
    private MarkLogicSpatialQueryBuilder spatialBuilder;
    private List<Queryable> tfTargets;
    private boolean registerQuery;
    private Optional<SortOrder> orderingDirection;
    private Optional<Queryable> orderingProperty;
    private boolean unfilteredQuery;

    private static final String UTF8_ENCODING = "UTF-8";
    private static final String ENABLE_FILTERED_TRAILING_WILDCARDS_QUERIES = "enableFilteredTrailingWildcardQueries";

    private static final List<IndexedMetadataElement> INDEXED_ELEMENTS = IndexedMetadataElements.getIndexes();

    /**
     * 
     */
    public DefaultMarkLogicSearchBuilder() {

    }

    /**
     * @param message
     * @param markLogicDB
     */
    public DefaultMarkLogicSearchBuilder(DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	this.markLogicDB = markLogicDB;
	this.ranking = message.getRankingStrategy();
	this.dataFolderCheckEnabled = message.isDataFolderCheckEnabled();
	this.deletedIncluded = message.isDeletedIncluded();
	this.maxFrequencyMapItems = message.getMaxFrequencyMapItems();
	this.spatialBuilder = createSpatialQueryBuilder(this);
	this.tfTargets = message.getTermFrequencyTargets();
	this.registerQuery = message.isQueryRegistrationEnabled();
	this.unfilteredQuery = true;
	this.orderingDirection = message.getSortOrder();
	this.orderingProperty = message.getSortProperty();
    }

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildCTSSearchQuery(String query, boolean estimate) {

	if (query != null) {

	    return buildCTSLogicQuery(CTSLogicOperator.AND, query, buildBasicQuery(estimate));
	}

	return buildNoConstraintsCTSSearchQuery(estimate);
    }

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildNoConstraintsCTSSearchQuery(boolean estimate) {

	return buildCTSLogicQuery(CTSLogicOperator.AND, buildBasicQuery(estimate));
    }

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildCTSSearch(String query, boolean estimate) {

	if (query != null) {

	    return buildCTSSearch_(buildCTSSearchQuery(query, estimate), estimate);
	}

	return buildNoConstraintsCTSSearch(estimate);
    }

    /**
     * @param estimate
     * @return
     */
    public String buildNoConstraintsCTSSearch(boolean estimate) {

	return buildCTSSearch_(buildNoConstraintsCTSSearchQuery(estimate), estimate);
    }

    public String buildQuery(QueryableBond<String> bond) {

	List<IndexedMetadataElement> customIndexes = new ArrayList<>();

	PluginsLoader<CustomIndexedElements> loader = new PluginsLoader<>();
	List<CustomIndexedElements> plugins = loader.loadPlugins(CustomIndexedElements.class);
	for (CustomIndexedElements p : plugins) {
	    customIndexes.addAll(p.getIndexes());
	}

	return buildQuery(bond, customIndexes);
    }

    /**
     * @param bond
     * @return
     */
    public String buildQuery(SimpleValueBond bond) {

	return buildQuery(bond, INDEXED_ELEMENTS);
    }

    @Override
    public String buildQuery(RuntimeInfoElementBond bond) {

	RuntimeInfoElement property = bond.getProperty();

	String value = bond.getPropertyValue() != null ? bond.getPropertyValue() : null;

	BondOperator operator = bond.getOperator();
	QualifiedName name = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, property.getName());

	if (operator == BondOperator.NOT_EXISTS || operator == BondOperator.EXISTS) {

	    return createExistsOrNotExistsQuery(operator, name, property);
	}

	switch (property.getContentType()) {

	case TEXTUAL:
	case ISO8601_DATE:
	case ISO8601_DATE_TIME:

	    return buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, true);

	case BOOLEAN:
	case DOUBLE:
	case INTEGER:
	case LONG:

	    return buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, false);
	case SPATIAL:
	    break;
	default:
	    break;
	}

	return null;
    }

    /**
     * @param bond
     * @return
     */
    public String buildQuery(SpatialBond bond) {

	return spatialBuilder.buildSpatialQuery(bond);
    }

    /**
     * @param bond
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public String buildQuery(ResourcePropertyBond bond) {

	ResourceProperty property = bond.getProperty();

	String value = bond.getPropertyValue() != null ? bond.getPropertyValue() : null;

	BondOperator operator = bond.getOperator();
	QualifiedName name = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, property.getName());

	if (operator == BondOperator.NOT_EXISTS || operator == BondOperator.EXISTS) {

	    return createExistsOrNotExistsQuery(operator, name, property);
	}

	switch (property) {
	case SOURCE_ID: {

	    return buildSourceIdQuery(name, value, property, operator);
	}

	case RESOURCE_TIME_STAMP: {

	    QualifiedName timeStampName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, property.getName());

	    // -----------------------------------
	    //
	    // min or max resource time stamp bond
	    //
	    if (bond.getOperator() == BondOperator.MAX || bond.getOperator() == BondOperator.MIN) {

		String sourceIdentifier = bond.getPropertyValue() != null ? bond.getPropertyValue().toString() : null;
		String minOrMax = computeMinOrMax(timeStampName, bond.getOperator(), sourceIdentifier);

		String rangeQuery = buildCTSElementRangeQuery(timeStampName, BondOperator.EQUAL.asMathOperator(), minOrMax, true);

		if (Objects.nonNull(sourceIdentifier)) {

		    String sourceIdQuery = buildSourceIdQuery(new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, //

			    ResourceProperty.SOURCE_ID.getName()), //

			    sourceIdentifier, ResourceProperty.SOURCE_ID); //

		    return buildCTSLogicQuery(CTSLogicOperator.AND, new String[] { sourceIdQuery, rangeQuery });
		}

		return rangeQuery;

	    } else {

		return buildCTSElementRangeQuery(timeStampName, bond.getOperator().asMathOperator(), value, true);
	    }
	}
	case ORIGINAL_ID:
	case PRIVATE_ID:
	case IS_ISO_COMPLIANT:
	case IS_DELETED:
	case IS_VALIDATED:
	case OAI_PMH_HEADER_ID:
	case COMPLIANCE_LEVEL:
	case IS_DOWNLOADABLE:
	case IS_TIMESERIES:
	case IS_EIFFEL_RECORD:
	case IS_GRID:
	case IS_VECTOR:
	case IS_EXECUTABLE:
	case IS_TRANSFORMABLE:
	case SUCCEEDED_TEST:
	case TEST_TIME_STAMP:
	case TYPE:
	case RECOVERY_REMOVAL_TOKEN:
	case LAST_DOWNLOAD_DATE:
	case LAST_FAILED_DOWNLOAD_DATE:

	    return buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, true);

	case IS_GEOSS_DATA_CORE:

	    List<String> operands = new ArrayList<String>();

	    List<String> ids = ConfigurationWrapper.getGDCSourceSetting().getSelectedSourcesIds();

	    QualifiedName sourceName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, ResourceProperty.SOURCE_ID.getName());

	    for (String id : ids) {
		operands.add(buildCTSElementRangeQuery(sourceName, BondOperator.EQUAL.asMathOperator(), id, true));
	    }

	    operands.add(buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, true));

	    return buildCTSLogicQuery(CTSLogicOperator.OR, operands.toArray(new String[] {}));

	case METADATA_QUALITY:
	case ESSENTIAL_VARS_QUALITY:
	case ACCESS_QUALITY:
	case DOWNLOAD_TIME:
	case EXECUTION_TIME:
	case SSC_SCORE:

	    return buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, false);
	}

	return null;
    }

    public String buildTrueQuery() {

	return "cts:and-query(())";
    }

    public String buildCTSElementRangeQuery(QualifiedName el, String operator, String value, double weight, boolean quoteValue) {

	String w = ",(\"score-function=linear\")," + weight;

	String s = quoteValue ? "'" : "";
	return "cts:element-range-query(fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalPart() + "'),'" + operator + "'," + s + ""
		+ value + "" + s + w + ")";
    }

    /**
     * @param operator
     * @param ordered
     * @param operands
     * @return
     */
    public String buildCTSLogicQuery(CTSLogicOperator operator, boolean ordered, String... operands) {

	String ord = ordered ? ",'ordered'" : "";

	String query = "\ncts:" + (operator == CTSLogicOperator.OR ? "or" : "and") + "-query((\n";
	query += buildCTSLogicQueryOperands(operands);

	return query + ")" + ord + ")";
    }

    /**
     * @param operator
     * @return
     */
    @Override
    public String getCTSLogicQueryName(LogicalOperator operator) {

	switch (operator) {
	case AND:
	    return "cts:and-query";
	case OR:
	    return "cts:or-query";
	case NOT:
	default:
	    return "cts:not-query";
	}
    }

    /**
     * @param operands
     * @return
     */
    protected String buildCTSLogicQueryOperands(String... operands) {

	String out = "";
	for (int i = 0; i < operands.length; i++) {
	    out += operands[i];
	    if (i < operands.length - 1) {
		out += ",\n";
	    }
	}
	return out;
    }

    /**
     * 
     */
    @Override
    public String buildCTSLogicQuery(CTSLogicOperator operator, String... operands) {

	return buildCTSLogicQuery(operator, false, operands);
    }

    /**
     * @param operator
     * @param ordered
     * @param operands
     * @return
     */
    public String buildCTSLogicQuery(CTSLogicOperator operator, boolean ordered, List<String> operands) {

	String[] array = operands.toArray(new String[] {});

	return buildCTSLogicQuery(operator, ordered, array);
    }

    /**
     * @param operator
     * @param operands
     * @return
     */
    public String buildCTSLogicQuery(CTSLogicOperator operator, List<String> operands) {

	String[] array = operands.toArray(new String[] {});

	return buildCTSLogicQuery(operator, array);
    }

    public String buildCTSElementRangeQuery(QualifiedName el, String operator, String value, boolean quoteValue) {

	return buildCTSElementRangeQuery(el, operator, value, 0, quoteValue);
    }

    public RankingStrategy getRankingStrategy() {

	return ranking;
    }

    public MarkLogicSpatialQueryBuilder createSpatialQueryBuilder(MarkLogicSearchBuilder builder) {

	return new DefaultMarkLogicSpatialQueryBuilder(this);
    }

    /**
     * @param name
     * @param value
     * @param property
     * @param bond
     * @return
     */
    protected String buildSourceIdQuery(QualifiedName name, String value, Queryable property) {

	return buildSourceIdQuery(name, value, property, BondOperator.EQUAL);
    }

    /**
     * @param name
     * @param value
     * @param property
     * @param operator
     * @return
     */
    protected String buildSourceIdQuery(QualifiedName name, String value, Queryable property, BondOperator operator) {

	//
	// this is the current strategy, according to the module
	//

	String query = "cts:directory-query(concat('/" + markLogicDB.getIdentifier() + "_" + value.trim() + "',";
	query += "cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',concat('" + value.trim()
		+ "','_dataFolder'))),";
	query += "'/') ,'infinity')";

	//
	// these fallback queries are required in a very particular cases, that is during the legacy ISO compliance test
	// which is performed before the harvesting finalization when the source data folder is not yet available
	//

	String fallbackQuery1 = "cts:directory-query(concat('/" + markLogicDB.getIdentifier() + "_" + value.trim()
		+ "','-data-1/'),'infinity')";
	String fallbackQuery2 = "cts:directory-query(concat('/" + markLogicDB.getIdentifier() + "_" + value.trim()
		+ "','-data-2/'),'infinity')";

	return buildCTSLogicQuery(CTSLogicOperator.OR, query, fallbackQuery1, fallbackQuery2);
    }

    /**
     * @return
     */
    protected String buildDelectedExcludedQuery() {

	return buildCTSNotQuery(buildCTSElementRangeQuery(IndexedResourceElements.IS_DELETED.asQualifiedName(), "!=", "", true));
    }

    protected String buildGDCWeightQuery() {

	return buildCTSLogicQuery(CTSLogicOperator.OR,

		buildCTSElementWordQuery(IndexedResourceElements.IS_GEOSS_DATA_CORE.asQualifiedName(), "false", 0),

		buildCTSElementWordQuery(IndexedResourceElements.IS_GEOSS_DATA_CORE.asQualifiedName(), "true",

			ranking.computePropertyWeight(ResourceProperty.IS_GEOSS_DATA_CORE)));
    }

    /**
     * @param tempExtent
     * @param value
     * @return
     */
    protected String buildTempExtentQuery(Queryable element, BondOperator operator, String value) {

	QualifiedName sns = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, element.getName());

	return buildCTSElementRangeQuery(sns, operator.asMathOperator(), value, true);
    }

    /**
     * @return
     */
    public String buildLastSixWeeksTemporalQuery() {

	String now = ISO8601DateTimeUtils.getISO8601DateTime();
	String lastWeek = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)));
	String last2Weeks = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)));
	String last3Weeks = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(21)));
	String last4Weeks = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(28)));
	String last5Weeks = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(35)));
	String last6Weeks = ISO8601DateTimeUtils.getISO8601DateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(42)));

	String query = buildCTSLogicQuery(CTSLogicOperator.OR,

		buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", last6Weeks, 0, true),

		buildCTSLogicQuery(CTSLogicOperator.AND,

			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", last6Weeks, 14, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", last6Weeks, 14, true)),

		buildCTSLogicQuery(CTSLogicOperator.AND,

			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", last5Weeks, 24, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", last5Weeks, 24, true)),

		buildCTSLogicQuery(CTSLogicOperator.AND,

			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", last4Weeks, 34, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", last4Weeks, 34, true)),

		buildCTSLogicQuery(CTSLogicOperator.AND,

			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", last3Weeks, 44, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", last2Weeks, 44, true)),

		buildCTSLogicQuery(CTSLogicOperator.AND,
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", last2Weeks, 54, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<", lastWeek, 54, true)),

		buildCTSLogicQuery(CTSLogicOperator.AND,
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), ">=", lastWeek, 64, true),
			buildCTSElementRangeQuery(IndexedMetadataElements.TEMP_EXTENT_BEGIN.asQualifiedName(), "<=", now, 64, true)),

		buildCTSElementRangeQuery(IndexedElements.TEMP_EXTENT_BEGIN_NOW.asQualifiedName(), "=", "", 64, true));

	return query;
    }

    /**
     * @param element
     * @return
     */
    protected String buildTempExtentNowQuery(Queryable element) {

	QualifiedName now = element == MetadataElement.TEMP_EXTENT_BEGIN ? //
		IndexedElements.TEMP_EXTENT_BEGIN_NOW.asQualifiedName() : //
		IndexedElements.TEMP_EXTENT_END_NOW.asQualifiedName();

	return buildCTSElementRangeQuery(now, "=", "", true);
    }

    /**
     * @param element
     * @return
     */
    protected String buildTempExtentBeforeNowQuery(Queryable element, String longTime) {

	QualifiedName nowElement = IndexedMetadataElements.TEMP_EXTENT_BEGIN_BEFORE_NOW.asQualifiedName();

	return buildCTSElementRangeQuery(nowElement, "=", longTime, true);
    }

    private String buildFalseQuery() {

	return buildCTSNotQuery(buildCTSLogicQuery(CTSLogicOperator.AND, ""));
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    //
    // private methods
    //
    // -----------------------------------------------------------------------------------------------------------------------------------

    private String buildQuery(QueryableBond<?> bond, List<IndexedMetadataElement> indexes) {

	IndexedMetadataElement index = indexes.//
		stream().//
		filter(i -> i.getElementName().equals(bond.getProperty().getName())).//
		findFirst().//
		get();

	Queryable property = bond.getProperty();
	BondOperator filterOp = bond.getOperator();

	Object objValue = bond.getPropertyValue();
	String value = objValue != null ? cleanValue(objValue.toString()) : null;

	QueryableStrategy strategy = QueryableStrategy.DEFAULT_STRATEGY;

	QueryableStrategy runTimestrategy = QueryableStrategy.getStrategy(index);

	if (runTimestrategy != null) {
	    strategy = runTimestrategy;
	}

	switch (strategy) {
	case DEFAULT_STRATEGY:

	    return execDefaultStrategy(property, filterOp, value);

	case NUMERICAL_STRATEGY:

	    return execNumericalStrategy(property, filterOp, value);

	case TEMP_EXTENT_STRATEGY:

	    return execTempExtentStrategy(property, filterOp, value);

	case ANY_TEXT_STRATEGY:

	    return execAnyTextStrategy(value);

	case SUBJECT_STRATEGY:

	    return execSubjectStrategy(value, bond.getOperator());
	case BOUNDING_BOX_NULL_STRATEGY:

	    return execBoundingBoxNullStrategy();
	default:
	    break;
	}

	// return buildCTSLogicQuery(CTSLogicOperator.AND, "");// an always true query
	return null;
    }

    @SuppressWarnings("incomplete-switch")
    private String execDefaultStrategy(Queryable element, BondOperator operator, String value) {

	QualifiedName qName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, element.getName());
	value = decode(value);

	switch (operator) {

	case NOT_EXISTS:
	case EXISTS:

	    return createExistsOrNotExistsQuery(operator, qName, element);

	case NOT_EQUAL:
	case EQUAL:
	case GREATER:
	case GREATER_OR_EQUAL:
	case LESS:
	case LESS_OR_EQUAL:

	    return buildCTSElementRangeQuery(qName, operator.asMathOperator(), value, ranking.computePropertyWeight(element), true);

	case TEXT_SEARCH:

	    Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	    if (keyValueOption.isPresent()) {

		Properties properties = keyValueOption.get();
		Boolean enabled = Boolean.valueOf(properties.getOrDefault(ENABLE_FILTERED_TRAILING_WILDCARDS_QUERIES, "false").toString());

		if (enabled && (value.startsWith("*") || value.endsWith("*"))) {

		    unfilteredQuery = false;

		    return buildCTSElementWordQuery(qName, value, ranking.computePropertyWeight(element));
		}
	    }

	    //
	    // unfiltered queries with * wildcard, can result in one ore more false positive.
	    // E.g:: for the CSW compliance test there are 2 records with format "application/pdf" and
	    // "application/xhtml+xml"
	    // and in a test the request is: "records having format 'application/*xml'".
	    // Using a single word query 'application/*xml' would result in both records, since the 'xml' word after the
	    // * is not
	    // considered in a non unfiltered query (it would be unfiltered in the last phase of a unfiltered query).
	    // In order to avoid this issue, the query is mapped in n word queries inside an AND logic op., where each
	    // word query
	    // has as target the result of the original value split by '*'.
	    // So in the example above, the resulting word queries are 2, with the targets 'application/' and 'xml'.
	    // The original query is thus mapped to: "records with format which contains both application/' and 'xml'
	    // words"
	    // and only 'application/xhtml+xml' matches this constraint ('application/pdf' contains 'application' but
	    // do not contains 'xml')
	    //

	    List<String> operands = Arrays.asList(value.split("\\*")).//
		    stream().//
		    filter(v -> !v.isEmpty()).//
		    map(v -> buildCTSElementWordQuery(qName, v, ranking.computePropertyWeight(element))).//
		    collect(Collectors.toList());

	    return buildCTSLogicQuery(CTSLogicOperator.AND, true, operands);

	// this is not correct. the min function is not constrained to the current bonds, so the returned
	// value is the min or max value of all the db elements, not of the db elements which satisfy the
	// other conditions
	// case MIN:
	// case MAX:
	//
	// String minOrMax = computeMinOrMax(qName, operator, null);
	// return buildCTSElementRangeQuery(qName, BondOperator.EQUAL.asMathOperator(), minOrMax, true);
	}

	return null;
    }

    /**
     * @return
     */
    private String getCountFunction() {

	return unfilteredQuery ? "xdmp:estimate" : "xdmp:estimate";
    }

    /**
     * @return
     */
    private String getSearchFilter() {

	return unfilteredQuery ? "unfiltered" : "filtered";
    }

    private String execBoundingBoxNullStrategy() {

	QualifiedName qName = IndexedElements.BOUNDING_BOX_NULL.asQualifiedName();

	return buildCTSElementRangeQuery(qName, "=", "", ranking.computePropertyWeight(MetadataElement.BOUNDING_BOX), true);
    }

    @SuppressWarnings("incomplete-switch")
    private String execTempExtentNullStrategy(MetadataElement element) {

	QualifiedName qName = null;

	switch (element) {
	case TEMP_EXTENT_BEGIN:
	    qName = IndexedElements.TEMP_EXTENT_BEGIN_NULL.asQualifiedName();
	    break;
	case TEMP_EXTENT_END:
	    qName = IndexedElements.TEMP_EXTENT_END_NULL.asQualifiedName();
	    break;
	}

	return buildCTSElementRangeQuery(qName, "=", "", ranking.computePropertyWeight(element), true);
    }

    /**
     * @param operator
     * @param qName
     * @param element
     * @return
     */
    private String createExistsOrNotExistsQuery(BondOperator operator, QualifiedName qName, Queryable element) {

	String existsQuery = buildCTSElementRangeQuery(qName, "!=", "", ranking.computePropertyWeight(element), true);

	if (operator == BondOperator.NOT_EXISTS) {

	    return buildCTSNotQuery(existsQuery);
	}

	return existsQuery;
    }

    @SuppressWarnings("incomplete-switch")
    private String execNumericalStrategy(Queryable element, BondOperator operator, String value) {

	QualifiedName qName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, element.getName());
	value = decode(value);

	switch (operator) {
	/**
	 * to support this operator, each element must have a correspondent NULL element in the IndexedElements group
	 */
	case NOT_EXISTS:
	case EXISTS:

	    throw new IllegalArgumentException("NULL operator on double and integer elements not yet supported");

	case NOT_EQUAL:
	case EQUAL:
	case GREATER:
	case GREATER_OR_EQUAL:
	case LESS:
	case LESS_OR_EQUAL:

	    return buildCTSElementRangeQuery(qName, operator.asMathOperator(), value, false);
	}

	return null;
    }

    @SuppressWarnings("incomplete-switch")
    private String execTempExtentStrategy(Queryable element, BondOperator operator, String value) {

	if (operator == BondOperator.NOT_EXISTS) {

	    return execTempExtentNullStrategy((MetadataElement) element);
	}

	List<String> operands = new ArrayList<>();
	Date nowDate = new Date();
	DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(DateTimeZone.UTC));
	DateTime parsed;
	if (value.equals("now") || value.equals("NOW")) {
	    parsed = new DateTime();
	} else {
	    parsed = formatter.parseDateTime(value);
	}

	Date literalDate = parsed.toDate();

	switch ((MetadataElement) element) {

	case TEMP_EXTENT_BEGIN: {
	    // ------------------------------------------------------------
	    //
	    // creates the now query in case of TEMP_EXTENT_BEGIN element and:
	    //
	    // OP = '<' AND (TARGET > NOW) OR
	    // OP = '<=' AND (TARGET >= NOW) OR
	    // OP = '>' AND (TARGET < NOW) OR
	    // OP = '>=' AND (TARGET <= NOW)
	    //
	    if (operator == BondOperator.LESS && literalDate.compareTo(nowDate) > 0 || //
		    operator == BondOperator.LESS_OR_EQUAL && literalDate.compareTo(nowDate) >= 0 || //
		    operator == BondOperator.GREATER && literalDate.compareTo(nowDate) < 0 || //
		    operator == BondOperator.GREATER_OR_EQUAL && literalDate.compareTo(nowDate) <= 0) {

		operands.add(buildTempExtentNowQuery(element));
	    }

	    //
	    //
	    // ------------------------------------------------
	    //
	    //

	    for (FrameValue frameValue : FrameValue.values()) {

		Date dateBeforeNow = new Date(System.currentTimeMillis() - Long.valueOf(frameValue.asMillis()));

		if (operator == BondOperator.LESS && literalDate.compareTo(dateBeforeNow) > 0 || //
			operator == BondOperator.LESS_OR_EQUAL && literalDate.compareTo(dateBeforeNow) >= 0 || //

			operator == BondOperator.GREATER && literalDate.compareTo(dateBeforeNow) < 0 || //
			operator == BondOperator.GREATER_OR_EQUAL && literalDate.compareTo(dateBeforeNow) <= 0) {

		    String beforeNowQuery = buildTempExtentBeforeNowQuery(element, frameValue.name());

		    operands.add(beforeNowQuery);
		}
	    }

	    //
	    //
	    // ------------------------------------------------
	    //
	    //

	    break;
	}

	case TEMP_EXTENT_END:
	    // ------------------------------------------------------------
	    //
	    // creates the now query in case of TEMP_EXTENT_END element and:
	    //
	    // OP = '>' AND (TARGET < NOW) OR
	    // OP = '>=' AND (TARGET <= NOW) OR
	    // OP = '<' AND (TARGET > NOW) OR
	    // OP = '<=' AND (TARGET >= NOW)
	    //
	    if (operator == BondOperator.GREATER && literalDate.compareTo(nowDate) < 0 || //
		    operator == BondOperator.GREATER_OR_EQUAL && literalDate.compareTo(nowDate) <= 0 || //
		    operator == BondOperator.LESS && literalDate.compareTo(nowDate) > 0 || //
		    operator == BondOperator.LESS_OR_EQUAL && literalDate.compareTo(nowDate) >= 0) {

		operands.add(buildTempExtentNowQuery(element));
	    }
	}

	// -----------------
	//
	// creates the query
	//
	String literalQuery = buildTempExtentQuery(element, operator, value);

	if (operands.isEmpty()) {

	    return literalQuery;
	}

	operands.add(literalQuery);

	return buildCTSLogicQuery(CTSLogicOperator.OR, operands);
    }

    /**
     * @param millis
     * @return
     */
    private Duration getDuration(long millis) {
	DatatypeFactory df = null;
	try {
	    df = DatatypeFactory.newInstance();
	} catch (javax.xml.datatype.DatatypeConfigurationException e) {

	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Can't instantiate DatatypeFactory", e);

	    return null;
	}
	return df.newDuration(millis);

    }

    private String execSubjectStrategy(String value, BondOperator bondOperator) {

	String kwd = execDefaultStrategy(MetadataElement.KEYWORD, bondOperator, value);
	String topic = execDefaultStrategy(MetadataElement.TOPIC_CATEGORY, bondOperator, value);

	return buildCTSLogicQuery(CTSLogicOperator.OR, kwd, topic);
    }

    private String execAnyTextStrategy(String value) {

	return buildCTSWordQuery(value, ranking.computePropertyWeight(MetadataElement.ANY_TEXT));
    }

    private String buildCTSSearch_(String query, boolean estimate) {

	String ctsIndexOrder = "";

	if (orderingProperty.isPresent()) {

	    String direction = "ascending";

	    Queryable property = orderingProperty.get();
	    if (orderingDirection.isPresent()) {

		direction = orderingDirection.get().getLabel().toLowerCase();
	    }

	    ctsIndexOrder = ", " + createCTSIndexOrderOption(property, direction);
	}

	String search = "cts:search(" + getCTSSearchTarget() + //
		"," + query + //
		",(\"" + getSearchFilter() + "\",\"" + ranking.getScoreMethod() + "\"" + ctsIndexOrder + "),"//
		+ RankingStrategy.QUALITY_WEIGHT + ")";

	if (estimate) {

	    search = "cts:search(" + getCTSSearchTarget() + //
		    ",$query" + //
		    ",(\"" + getSearchFilter() + "\",\"" + ranking.getScoreMethod() + "\"),"//
		    + RankingStrategy.QUALITY_WEIGHT + ")";

	    if (registerQuery) {

		String s = "let $registeredQuery := cts:register( \n";
		s += query + ") \n";
		s += "let $query as cts:query := cts:registered-query($registeredQuery,'" + getSearchFilter() + "')";
		s += "return \n";
		s += "<gs:out>{<gs:estimate>{\n\n";
		s += getCountFunction() + "(" + search + ")\n\n";
		s += "}</gs:estimate>}\n\n";
		s += "{<gs:termFrequency xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">\n\n";
		for (Queryable term : tfTargets) {

		    s += buildTermFrequencyQuery(term, maxFrequencyMapItems) + "\n\n";
		}
		s += "</gs:termFrequency>}\n";
		s += "{<gs:registeredQuery>{$registeredQuery}</gs:registeredQuery>}\n";
		s += "</gs:out>";

		search = s;

	    } else {

		String s = "let $query as cts:query := \n" + query + "\n";

		s += "return \n";

		if (Objects.isNull(tfTargets) || tfTargets.isEmpty()) {

		    s += " " + getCountFunction() + "(" + search + ")\n\n";

		} else {

		    s += "<gs:out>{<gs:estimate>{\n\n";
		    s += getCountFunction() + "(" + search + ")\n\n";
		    s += "}</gs:estimate>}\n\n";
		    s += "{<gs:termFrequency xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">\n\n";
		    for (Queryable term : tfTargets) {

			s += buildTermFrequencyQuery(term, maxFrequencyMapItems) + "\n\n";
		    }
		    s += "</gs:termFrequency>}\n";
		    s += "</gs:out>";
		}

		search = s;
	    }
	}

	return search;
    }

    /**
     * The basic query. The constraints are GEOSS Data Core, metadata quality, essential variables and access quality.
     * This query also allows to filter in/out the deleted records.
     * For an estimate query, the weight query is omitted in order to resize the overall query
     */
    private String buildBasicQuery(boolean estimate) {

	String delExcludedQuery = buildDelectedExcludedQuery();

	String weightQuery = buildCTSLogicQuery(CTSLogicOperator.OR,

		//
		// an always true query is required in order to get results in
		// case all the others constraints do not match
		//
		buildTrueQuery(),

		//
		// the notDeletedQuery in AND is put in order to give no weight the deleted records,
		// so they are returned as last
		//
		buildCTSLogicQuery(CTSLogicOperator.AND, buildGDCWeightQuery(), delExcludedQuery),

		buildCTSLogicQuery(CTSLogicOperator.AND, buildMDQWeightQuery(), delExcludedQuery),

		buildCTSLogicQuery(CTSLogicOperator.AND, buildEVWeightQuery(), delExcludedQuery),

		buildCTSLogicQuery(CTSLogicOperator.AND, buildAQWeightQuery(), delExcludedQuery));

	if (estimate) {

	    return deletedIncluded ? buildTrueQuery() : delExcludedQuery;
	}

	return deletedIncluded ? weightQuery : buildCTSLogicQuery(CTSLogicOperator.AND, delExcludedQuery, weightQuery);
    }

    // ---------------------------------------------------
    //
    //
    //
    private String buildMDQWeightQuery() {

	return buildWeightQuery(IndexedResourceElements.MEDATADATA_QUALITY);
    }

    private String buildEVWeightQuery() {

	return buildWeightQuery(IndexedResourceElements.ESSENTIAL_VARS_QUALITY);
    }

    private String buildAQWeightQuery() {

	return buildWeightQuery(IndexedResourceElements.ACCESS_QUALITY);
    }

    protected String buildWeightQuery(IndexedElement element) {

	return buildCTSLogicQuery(CTSLogicOperator.OR,

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "1", ranking.computeRangeWeight(element, 1), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "2", ranking.computeRangeWeight(element, 2), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "3", ranking.computeRangeWeight(element, 3), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "4", ranking.computeRangeWeight(element, 4), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "5", ranking.computeRangeWeight(element, 5), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "6", ranking.computeRangeWeight(element, 6), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "7", ranking.computeRangeWeight(element, 7), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "8", ranking.computeRangeWeight(element, 8), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "9", ranking.computeRangeWeight(element, 9), false),

		buildCTSElementRangeQuery(element.asQualifiedName(), "=", "10", ranking.computeRangeWeight(element, 10), false));
    }

    /**
     * @param el
     * @param val
     * @param weight
     * @return
     */
    protected String buildCTSElementWordQuery(QualifiedName el, String val, Integer weight) {

	String w = weight == null ? "" : ",(\"case-insensitive\")," + weight + "";

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalPart() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalPart() + "')";

	return "cts:element-word-query(" + par + "," + "'" + val + "'" + w + ")";
    }

    private String buildCTSWordQuery(String keyword, int weight) {

	return "cts:word-query('" + keyword + "',()," + weight + ")";
    }

    private String buildCTSElementValues(QualifiedName el, String options) {

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalPart() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalPart() + "')";

	return "cts:element-values(" + par + "," + options + ")";
    }

    private String buildCTSElementValues(QualifiedName el, String options, String onError) {

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalPart() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalPart() + "')";

	return " try { cts:element-values(" + par + "," + options + ") } catch ($e) { '" + onError + "' }";
    }

    private String buildTermFrequencyOptions(int max) {

	return "(), (\"fragment-frequency\",\"frequency-order\",\"descending\",\"limit=" + max + "\",\"eager\"), $query";
    }

    protected String buildCTSNotQuery(String query) {

	return "cts:not-query(" + query + ")";
    }

    // @formatter:off
    protected String buildTermFrequencyQuery(Queryable target, int max) {

	QualifiedName ns = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, target.getName());

	String opt = buildTermFrequencyOptions(max);
	String values = buildCTSElementValues(ns, opt);

	if (target.getName().equals(ResourceProperty.SSC_SCORE_EL_NAME)) {

	    return createSSCScoreTermFrequencyQuery(values);
	}

	String checkEmptyString = target.getContentType() == ContentType.TEXTUAL ? "     where $term != ''" : "";
	String urlDecode = target.getContentType() == ContentType.TEXTUAL ? "\n  <gs:decodedTerm>{xdmp:url-decode($term)}</gs:decodedTerm> "
		: "\n  <gs:decodedTerm>{$term}</gs:decodedTerm> ";

	return "   <gs:" //
		+ target + ">{" //
		+ "\n" //
		+ "     let $x := " //
		+ values + "\n" //
		+ "     for $term in $x " //
		+ "\n" //
		+ "     let $f := cts:frequency($term) " //
		+ "\n" //

		+ checkEmptyString //

		+ "\n" //
		+ "     return " //
		+ "\n" //
		+ "     (" //
		+ "\n" + "     <gs:result>" //
		+ "\n" + "       <gs:term>{xdmp:url-encode($term)}</gs:term>" + urlDecode + "\n" + "       <gs:freq>" //
		+ "\n" + "         {$f}" //
		+ "\n" + "       </gs:freq>" //
		+ "\n" + "     </gs:result>" //
		+ "\n" //
		+ "     " //
		+ ") " //
		+ "\n" //
		+ "     }\n" //
		+ "   </gs:" + target + ">"; //
    }

    /**
     * Since the value can be already encoded for some reason, decodes it value several times in order to be sure
     *
     * @param value
     * @return
     */
    private String decode(String value) {

	if (value == null) {
	    return null;
	}

	try {
	    value = URLDecoder.decode(value, UTF8_ENCODING);
	    value = URLDecoder.decode(value, UTF8_ENCODING);
	    value = URLDecoder.decode(value, UTF8_ENCODING);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to decode value [{}]: {}", value, e.getMessage());
	}
	return value;
    }

    /**
     * @param value
     * @return
     */
    private String cleanValue(String value) {

	if (value == null) {
	    return value;
	}

	// escapes the single quote (if present) since it's used in the cts:element-word-query
	value = value.replace("'", "''");

	return value;
    }

    private String computeMinOrMax(QualifiedName name, BondOperator op, String sourceIdentifer) {

	try {
	    ResultSequence rs = markLogicDB.execXQuery(createCTSMinMaxQuery(name, op, sourceIdentifer));
	    return rs.asString().trim();
	} catch (RequestException e) {

	    // here a warning log should be enough
	    GSLoggerFactory.getLogger(getClass()).warn("Can't compute min or max", e);

	}

	return null;
    }

    private String createCTSIndexOrderOption(Queryable property, String direction) {

	return "cts:index-order(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', '" + property.getName()
		+ "')), '" + direction + "')";
    }

    private String createCTSMinMaxQuery(QualifiedName name, BondOperator operatorType, String sourceIdentifer) {

	String query = "()";
	if (sourceIdentifer != null) {

	    QualifiedName sourceIdName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, ResourceProperty.SOURCE_ID.getName());

	    query = buildCTSElementRangeQuery(sourceIdName, BondOperator.EQUAL.asMathOperator(), sourceIdentifer, true);
	}

	String importNS = "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	String ref = createElementReference(name);
	if (operatorType == BondOperator.MIN) {
	    return importNS + "cts:min(" + ref + ",()," + query + ")";

	}
	return importNS + "cts:max(" + ref + ",()," + query + ")";
    }

    private String createElementReference(QualifiedName name) {

	return createElementReference(name.getLocalPart());
    }

    private String createElementReference(String name) {

	return "cts:element-reference(" + MarkLogicSearchBuilder.createFNQName(name) + ")";
    }

    /**
     * @param values
     * @return
     */
    protected String createSSCScoreTermFrequencyQuery(String values) {

	values = values.replace("\"limit=" + maxFrequencyMapItems + "\"", "()");

	int rangeCount = readRangeCount();
	String[] ranges = new String[5];
	switch (rangeCount) {

	case 4:
	    ranges[3] = "0,25";
	    ranges[2] = "26,50";
	    ranges[1] = "51,75";
	    ranges[0] = "76,100";
	    break;
	case 5:
	    ranges[4] = "0,20";
	    ranges[3] = "21,40";
	    ranges[2] = "41,60";
	    ranges[1] = "61,80";
	    ranges[0] = "81,100";
	    break;
	case 3:
	default:
	    ranges[2] = "0,30";
	    ranges[1] = "31,60";
	    ranges[0] = "61,100";
	    break;
	}

	StringBuilder stringBuilder = new StringBuilder("   <gs:sscScore>\n");

	for (int i = 0; i < ranges.length; i++) {

	    String minRange = ranges[i].split(",")[0];
	    String maxRange = ranges[i].split(",")[1];

	    stringBuilder.append("{<el>{" + "\n");
	    stringBuilder.append(" let $sum := fn:sum(" + "\n" + "          let $x := " + values);
	    stringBuilder.append("\n" + "          for $term in $x ");
	    stringBuilder.append("\n" + "          let $f := cts:frequency($term)" + "\n");
	    stringBuilder.append("          where ($term >= " + minRange + " and $term <= ");
	    stringBuilder.append(maxRange + ") " + "\n" + "          return  $f)");
	    stringBuilder.append("\n" + " where $sum > 0 " + "\n" + "      return <gs:result>" + "\n");
	    stringBuilder.append("       <gs:term>" + minRange + " - " + maxRange + "</gs:term>" + "\n");
	    stringBuilder.append("       <gs:decodedTerm>" + minRange + " - " + maxRange + "</gs:decodedTerm>" + "\n");
	    stringBuilder.append("       <gs:freq>" + "\n" + "         {$sum}" + "\n");
	    stringBuilder.append("       </gs:freq>" + "\n" + "     </gs:result>" + "\n" + "     }</el>//gs:result}\n");
	}

	stringBuilder.append("   </gs:sscScore>");

	return stringBuilder.toString();
    }

    private int readRangeCount() {

	return 5;
    }

}
