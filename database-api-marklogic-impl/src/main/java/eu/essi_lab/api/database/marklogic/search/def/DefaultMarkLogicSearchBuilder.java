package eu.essi_lab.api.database.marklogic.search.def;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import eu.essi_lab.api.database.marklogic.SourceStorageWorker;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSpatialQueryBuilder;
import eu.essi_lab.indexes.CustomIndexedElements;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.indexes.IndexedResourceElements;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.GDCSourcesReader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.RankingStrategy;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.QualifiedName;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
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
    private Optional<OrderingDirection> orderingDirection;
    private Optional<Queryable> orderingProperty;

    private static final String UTF8_ENCODING = "UTF-8";

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

	this.orderingDirection = message.getOrderingDirection();
	this.orderingProperty = message.getOrderingProperty();
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

	List<IndexedMetadataElement> indexes = IndexedMetadataElements.getIndexes();

	return buildQuery(bond, indexes);
    }

    @Override
    public String buildQuery(RuntimeInfoElementBond bond) {

	RuntimeInfoElement property = bond.getProperty();

	String value = bond.getPropertyValue() != null ? bond.getPropertyValue() : null;

	BondOperator operator = bond.getOperator();
	QualifiedName name = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, property.getName());

	if (operator == BondOperator.NULL) {

	    return createNullQuery(name, property);
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

	if (operator == BondOperator.NULL) {

	    return createNullQuery(name, property);
	}

	switch (property) {
	case SOURCE_ID: {

	    return buildSourceIdQuery(name, value, property);
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
	case OAI_PMH_HEADER_ID:
	case COMPLIANCE_LEVEL:
	case IS_DOWNLOADABLE:
	case IS_TIMESERIES:
	case IS_GRID:
	case IS_EXECUTABLE:
	case IS_TRANSFORMABLE:
	case SUCCEEDED_TEST:
	case TEST_TIME_STAMP:
	case TYPE:
	case RECOVERY_REMOVAL_TOKEN:

	    return buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, true);

	case IS_GEOSS_DATA_CORE:

	    List<String> operands = new ArrayList<String>();

	    List<String> ids = GDCSourcesReader.readSourceIds();

	    QualifiedName sourceName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, ResourceProperty.SOURCE_ID.getName());

	    for (String id : ids) {
		operands.add(buildCTSElementRangeQuery(sourceName, BondOperator.EQUAL.asMathOperator(), id, true));
	    }

	    operands.add(buildCTSElementRangeQuery(name, bond.getOperator().asMathOperator(), value, true));

	    return buildCTSLogicQuery(CTSLogicOperator.OR, operands.toArray(new String[] {}));

	case MEDATADATA_QUALITY:
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
	return "cts:element-range-query(fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalName() + "'),'" + operator + "'," + s + ""
		+ value + "" + s + w + ")";
    }

    public String buildCTSLogicQuery(CTSLogicOperator operator, String... operands) {
	String op = null;
	switch (operator) {
	case OR:
	    op = "or";
	    break;
	case AND:
	    op = "and";
	}
	String query = "\ncts:" + op + "-query((\n";
	for (int i = 0; i < operands.length; i++) {
	    query += operands[i];
	    if (i < operands.length - 1) {
		query += ",\n";
	    }
	}
	return query + "))";
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

	String sourceIdQuery = buildCTSElementRangeQuery(//
		name, //
		BondOperator.EQUAL.asMathOperator(), //
		value, //
		ranking.computePropertyWeight(property), //
		true);

	//
	// the time stamp check can be disabled with the DiscoveryMessage
	//
	if (!dataFolderCheckEnabled) {

	    return sourceIdQuery;
	}

	// ------------------------------------------------------------------------------------------------
	// this additional constraint forces the retrieval of resources harvested before the
	// source end harvesting time stamp.
	// this avoids, in case the target source is harvested right now, to retrieve also
	// the new resources while the harvesting is still in progress
	// ------------------------------------------------------------------------------------------------

	QualifiedName resTimeStampName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, //
		ResourceProperty.RESOURCE_TIME_STAMP.getName());//

	String resTimeStampQuery = buildCTSElementRangeQuery(//
		resTimeStampName, //
		BondOperator.LESS.asMathOperator(), //

		buildCTSElementValues(//
			new QualifiedName(//
				NameSpace.GI_SUITE_DATA_MODEL, //
				SourceStorageWorker.createDataFolderIndexName(value)),
			"'()'", ""), //

		false);//

	return buildCTSLogicQuery(CTSLogicOperator.AND, sourceIdQuery, resTimeStampQuery);
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

	for (IndexedMetadataElement index : indexes) {
	    
	    if (bond.getProperty().getName().equals(index.getElementName())) {

		Queryable property = bond.getProperty();
		BondOperator filterOp = bond.getOperator();

		Object objValue = bond.getPropertyValue();
		String value = objValue != null ? cleanValue(objValue.toString()) : null;

		QueryableStrategy strategy = QueryableStrategy.DEFAULT_STRATEGY;

		QueryableStrategy runTimestrategy = QueryableStrategy.getStrategy(index);

		if (runTimestrategy != null)
		    strategy = runTimestrategy;

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
	    }
	}

	// return buildCTSLogicQuery(CTSLogicOperator.AND, "");// an always true query
	return null;
    }

    @SuppressWarnings("incomplete-switch")
    private String execDefaultStrategy(Queryable element, BondOperator operator, String value) {

	QualifiedName qName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, element.getName());
	value = decode(value);

	switch (operator) {

	case NULL:

	    return createNullQuery(qName, element);

	case NOT_EQUAL:
	case EQUAL:
	case GREATER:
	case GREATER_OR_EQUAL:
	case LESS:
	case LESS_OR_EQUAL:

	    return buildCTSElementRangeQuery(qName, operator.asMathOperator(), value, ranking.computePropertyWeight(element), true);

	case LIKE:

	    // return buildCTSElementQuery(qName, buildCTSWordQuery(value, ranking.computeWordWeight(element)));

	    // this solution should be exactly like the above one, but more compact
	    return buildCTSElementWordQuery(qName, value, ranking.computePropertyWeight(element));

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

    private String createNullQuery(QualifiedName qName, Queryable element) {

	return buildCTSNotQuery(buildCTSElementRangeQuery(qName, "!=", "", ranking.computePropertyWeight(element), true));
    }

    @SuppressWarnings("incomplete-switch")
    private String execNumericalStrategy(Queryable element, BondOperator operator, String value) {

	QualifiedName qName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, element.getName());
	value = decode(value);

	switch (operator) {
	/**
	 * to support this operator, each element must have a correspondent NULL element in the IndexedElements group
	 */
	case NULL:

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

	if (operator == BondOperator.NULL) {

	    return execTempExtentNullStrategy((MetadataElement) element);
	}

	List<String> operands = new ArrayList<>();
	Date nowDate = new Date();
	DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser().withChronology(ISOChronology.getInstance(DateTimeZone.UTC));
	DateTime parsed = formatter.parseDateTime(value);
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

		direction = orderingDirection.get().getName().toLowerCase();
	    }

	    ctsIndexOrder = ", " + createCTSIndexOrderOption(property, direction);
	}

	String search = "cts:search(" + MarkLogicSearchBuilder.getCTSSearchTarget() + //
		"," + query + //
		",(\"unfiltered\",\"" + ranking.getScoreMethod() + "\"" + ctsIndexOrder + "),"//
		+ RankingStrategy.QUALITY_WEIGHT + ")";

	if (estimate) {

	    search = "cts:search(" + MarkLogicSearchBuilder.getCTSSearchTarget() + //
		    ",$query" + //
		    ",(\"unfiltered\",\"" + ranking.getScoreMethod() + "\"),"//
		    + RankingStrategy.QUALITY_WEIGHT + ")";

	    if (registerQuery) {

		String s = "let $registeredQuery := cts:register( \n";
		s += query + ") \n";
		s += "let $query as cts:query := cts:registered-query($registeredQuery,'unfiltered')";
		s += "return \n";
		s += "<gs:out>{<gs:estimate>{\n\n";
		s += "xdmp:estimate(" + search + ")\n\n";
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

		    s += " xdmp:estimate(" + search + ")\n\n";

		} else {

		    s += "<gs:out>{<gs:estimate>{\n\n";
		    s += "xdmp:estimate(" + search + ")\n\n";
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

    private String buildCTSElementWordQuery(QualifiedName el, String val, Integer weight) {

	String w = weight == null ? "" : ",(\"case-insensitive\")," + weight + "";

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalName() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalName() + "')";

	return "cts:element-word-query(" + par + "," + "'" + val + "'" + w + ")";
    }

    private String buildCTSWordQuery(String keyword, int weight) {

	return "cts:word-query('" + keyword + "',()," + weight + ")";
    }

    private String buildCTSElementValues(QualifiedName el, String options) {

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalName() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalName() + "')";

	return "cts:element-values(" + par + "," + options + ")";
    }

    private String buildCTSElementValues(QualifiedName el, String options, String onError) {

	String par = el.getNameSpaceURI() == null ? "xs:QName('" + el.getLocalName() + "')"
		: "fn:QName('" + el.getNameSpaceURI() + "','" + el.getLocalName() + "')";

	return " try { cts:element-values(" + par + "," + options + ") } catch ($e) { '" + onError + "' }";
    }

    private String buildTermFrequencyOptions(int max) {

	return "(), (\"fragment-frequency\",\"frequency-order\",\"descending\",\"limit=" + max + "\",\"eager\"), $query";
    }

    private String buildCTSNotQuery(String query) {

	return "cts:not-query(" + query + ")";
    }

    // @formatter:off
    private String buildTermFrequencyQuery(Queryable target, int max) {

	QualifiedName ns = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, target.getName());

	String opt = buildTermFrequencyOptions(max);
	String values = buildCTSElementValues(ns, opt);

	if (target.getName().equals(ResourceProperty.SSC_SCORE_EL_NAME)) {

	    return createSSCScoreTermFrequencyQuery(values);
	}

	String checkEmptyString = target.getContentType() == ContentType.TEXTUAL ? "     where $term != ''" : "";
	String urlDecode = target.getContentType()
		== ContentType.TEXTUAL ? "\n  <gs:decodedTerm>{xdmp:url-decode($term)}</gs:decodedTerm> " : "\n  <gs:decodedTerm>{$term}</gs:decodedTerm> ";

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
	} catch (UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Bad encoding {}", value, e);
	}
	return value;
    }

    /**
     * 
     * @param value
     * @return
     */
    private String cleanValue(String value) {

	if (value == null) {
	    return value;
	}

	if (value.startsWith("*")) {
	    value = value.substring(1, value.length());
	}

	if (value.endsWith("*")) {
	    value = value.substring(0, value.length() - 1);
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
	    GSLoggerFactory.getLogger(getClass()).warn("Can't compute mon or max", e);

	}

	return null;
    }
    
    private String createCTSIndexOrderOption(Queryable property, String direction) {
	
	return "cts:index-order(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', '"
		    + property.getName() + "')), '"+direction+"')";
    }

    private String createCTSMinMaxQuery(QualifiedName name, BondOperator operatorType, String sourceIdentifer) {

	String query = "()";
	if (sourceIdentifer != null) {

	    QualifiedName sourceIdName = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL, ResourceProperty.SOURCE_ID.getName());

	    query = buildCTSElementRangeQuery(sourceIdName, BondOperator.EQUAL.asMathOperator(), sourceIdentifer, true);
	}

	String ref = createElementReference(name);
	if (operatorType == BondOperator.MIN) {
	    return "cts:min(" + ref + ",()," + query + ")";

	}
	return "cts:max(" + ref + ",()," + query + ")";
    }

    private String createElementReference(QualifiedName name) {

	return createElementReference(name.getLocalName());
    }

    private String createElementReference(String name) {

	return "cts:element-reference(" + MarkLogicSearchBuilder.createFNQName(name) + ")";
    }

    private String createSSCScoreTermFrequencyQuery(String values) {

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
