/**
 * 
 */
package eu.essi_lab.profiler.rest.handler.info;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsMessage.GroupByPeriod;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchClient;

/**
 * @author Fabrizio
 */
public class GWPStatisticsHandler extends DefaultRequestHandler {

    private static final String ORIGIN = "origin";
    private static final String MAX_RESULTS = "maxResults";
    private static final String PERIOD = "period";
    private static final String PERIOD_RANGE_START = "periodRangeStart";
    private static final String PERIOD_RANGE_END = "periodRangeEnd";
    private static final String CATALOG = "catalog";
    private static final String INTERVAL = "interval";
    private static final String INTERVAL_SIZE = "intervalSize";
    private static final String STATISTIC = "statistic";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);

	KeyValueParser parser = new KeyValueParser(StringUtils.decodeUTF8(request.getQueryString()));

	//
	// statistic
	//
	String statParam = parser.getValue(STATISTIC);

	if (!parser.isValid(STATISTIC)) {

	    message.setError("Missing mandatory parameter '" + STATISTIC + "'");
	    return message;

	} else {
	    try {
		Statistic stat = Statistic.fromName(statParam);

		//
		// catalog
		//
		if (parser.isValid(CATALOG)) {

		    switch (stat) {
		    case MOST_POPULAR_AREAS:
		    case MOST_POPULAR_KEYWORDS:
		    case NUMBER_OF_SEARCHES:
			break;
		    default:
			message.setError("The statistic '" + statParam + "' can not be applied to a single catalog");
			return message;

		    }
		} else if (stat == Statistic.NUMBER_OF_SEARCHES || stat == Statistic.NUMBER_OF_ACCESS_REQUESTS) {

		    String interval = parser.getValue(INTERVAL);
		    try {
			Interval.fromName(interval);
		    } catch (IllegalArgumentException ex) {
			message.setError(ex.getMessage());
			return message;
		    }

		    String intervalSize = parser.getValue(INTERVAL_SIZE);
		    try {
			Integer value = Integer.valueOf(intervalSize);
			if (value <= 0) {
			    message.setError("Value  of '" + INTERVAL_SIZE + "' parameter must be an integer >= 0");
			    return message;
			}
		    } catch (NumberFormatException ex) {
			message.setError("Value  of '" + INTERVAL_SIZE + "' parameter must be an integer >= 0");
			return message;
		    }
		}

	    } catch (IllegalArgumentException ex) {
		message.setError(ex.getMessage());
		return message;
	    }
	}

	//
	// maxResults
	//
	String maxResults = parser.getValue(MAX_RESULTS);

	if (parser.isValid(MAX_RESULTS) && Statistic.fromName(statParam) != Statistic.NUMBER_OF_SEARCHES) {
	    try {
		Integer value = Integer.valueOf(maxResults);
		if (value <= 0) {
		    message.setError("Value  of '" + MAX_RESULTS + "' parameter must be an integer >= 0");
		    return message;
		}
	    } catch (NumberFormatException ex) {
		message.setError("Value  of '" + MAX_RESULTS + "' parameter must be an integer >= 0");
		return message;
	    }
	}

	//
	// period, periodRangeStart, periodRangeEnd
	//
	String period = parser.getValue(PERIOD);
	String periodRangeStart = parser.getValue(PERIOD_RANGE_START);
	String periodRangeEnd = parser.getValue(PERIOD_RANGE_END);

	if (!parser.isValid(PERIOD) && !parser.isValid(PERIOD_RANGE_START)) {

	    message.setError("One of '" + PERIOD + "' or '" + PERIOD_RANGE_START + "' parameter must be set");
	    return message;
	}

	if (parser.isValid(PERIOD) && parser.isValid(PERIOD_RANGE_START)) {

	    message.setError("Only least one of '" + PERIOD_RANGE_START + "' or '" + PERIOD_RANGE_END + "' parameter must be set");
	    return message;
	}

	if (parser.isValid(PERIOD)) {

	    try {
		Period.fromName(period);
	    } catch (IllegalArgumentException ex) {

		message.setError(ex.getMessage());
		return message;
	    }
	}

	if (parser.isValid(PERIOD_RANGE_START)) {

	    if (!ISO8601DateTimeUtils.parseISO8601ToDate(periodRangeStart).isPresent()) {
		message.setError("Value of '" + PERIOD_RANGE_START + "' parameter must be ISO8601 compliant");
		return message;
	    }
	}

	if (parser.isValid(PERIOD_RANGE_END)) {

	    if (!ISO8601DateTimeUtils.parseISO8601ToDate(periodRangeEnd).isPresent()) {
		message.setError("Value of '" + PERIOD_RANGE_END + "' parameter must be ISO8601 compliant");
		return message;
	    }
	}

	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	boolean es = true;

	StatisticsResponse response = null;

	StatisticsMessage message = createMessage(webRequest);

	if (es) {
	    // elastic search implementation

	    Optional<DatabaseSetting> setting = ConfigurationWrapper.getSystemSettings().getStatisticsSetting();

	    //
	    // it should be present if stats gathering is enabled!
	    //
	    if (setting.isPresent()) {

		DatabaseSetting databaseSetting = setting.get();
		String databaseName = databaseSetting.getDatabaseName();
		String databasePassword = databaseSetting.getDatabasePassword();
		String databaseUri = databaseSetting.getDatabaseUri();
		String databaseUser = databaseSetting.getDatabaseUser();

		ElasticsearchClient client = new ElasticsearchClient(databaseUri, databaseUser, databasePassword);

		client.setDbName(databaseName);
		response = client.compute(message);
	    }

	} else {
	    // marklogic implementation

	    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	    GSLoggerFactory.getLogger(FullStatisticsHandler.class).debug("Storage uri: {}", uri);

	    DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(uri);

	    response = executor.compute(message);
	}

	response.convertGroupedByMillisToISODataTime();

	KeyValueParser parser = new KeyValueParser(StringUtils.decodeUTF8(webRequest.getQueryString()));

	int maxResults = Integer.valueOf(parser.getValue(MAX_RESULTS, "10"));

	response.adjustBboxFrequencyResult(maxResults);

	JSONObject jsonResponse = mapResponse(response, parser);

	return jsonResponse.toString();
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }

    /**
     * @author Fabrizio
     */
    public enum Period {

	LAST_WEEK("lastWeek"), //
	LAST_MONTH("lastMonth"), //
	LAST_YEAR("lastYear"); //

	private String name;

	private Period(String name) {

	    this.name = name;
	}

	/**
	 * @return
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @param name
	 * @return
	 */
	public static Period fromName(String name) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(s -> s.getName().equals(name)).//
		    findFirst().//
		    orElseThrow(() -> new IllegalArgumentException("Invalid period name: " + name));
	}
    }

    /**
     * @author Fabrizio
     */
    public enum Interval {

	MINUTE("minute", "m"), //
	HOUR("hour", "h"), //
	DAY("day", "d"), //
	WEEK("week", "w"), //
	MONTH("month", "M"), //
	QUARTER("quarter", "1"), //
	YEAR("year", "y")//
	;

	private String name;
	private String abbreviation;

	private Interval(String name, String abbreviation) {

	    this.name = name;
	    this.abbreviation = abbreviation;
	}

	/**
	 * @return
	 */
	public String getName() {

	    return name;
	}

	/**
	 * @return
	 */
	public String getAbbreviation() {

	    return abbreviation;
	}

	/**
	 * @param name
	 * @return
	 */
	public static Interval fromName(String name) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(s -> s.getName().equals(name)).//
		    findFirst().//
		    orElseThrow(() -> new IllegalArgumentException("Invalid interval name: " + name));
	}
    }

    /**
     * @author Fabrizio
     */
    private enum StatisticType {

	ALL_CATALOGS, //
	GENERAL//
    }

    /**
     * @author Fabrizio
     */
    public enum Statistic {

	//
	// discovery statistics
	//
	MOST_POPULAR_RESOURCES("mostPopularResources", StatisticType.ALL_CATALOGS), //
	MOST_POPULAR_CATALOGS("mostPopularCatalogs", StatisticType.ALL_CATALOGS), //
	MOST_POPULAR_ORG("mostPopularOrg", StatisticType.ALL_CATALOGS), //

	NUMBER_OF_SEARCHES("numberOfSearches", StatisticType.GENERAL), //
	MOST_POPULAR_KEYWORDS("mostPopularKeywords", StatisticType.GENERAL), //
	MOST_POPULAR_AREAS("mostPopularAreas", StatisticType.GENERAL),

	// not used (to be refined in the mapResponse method)
	MOST_POPULAR_DISCOVERY_SERVICES("mostPopularDiscoveryServices"), //
	// not used
	MOST_POPULAR_DISCOVERY_SOURCES("mostPopularDiscoverySources"), //
	// not used
	MOST_POPULAR_DISCOVERY_VIEW("mostPopularDiscoveryView"), //

	//
	// access statistics
	//
	NUMBER_OF_ACCESS_REQUESTS("numberOfAccessRequests"), //
	MOST_POPULAR_DATA_TYPES("mostPopularDataTypes"), //
	MOST_POPULAR_DATA_FORMAT("mostPopularDataFormat"), //
	MOST_POPULAR_DATA_CRS("mostPopularDataCRS"), //

	MOST_POPULAR_ACCESS_SOURCES("mostPopularAccessSources"), //
	MOST_POPULAR_ACCESS_VIEW("mostPopularAccessView"),

	//
	// !!! to be refined in the mapResponse method
	//
	MOST_POPULAR_ACCESS_SERVICES("mostPopularAccessServices");

	private String name;
	private StatisticType type;

	/**
	 * @param name
	 */
	private Statistic(String name) {
	    this(name, null);
	}

	/**
	 * @param name
	 * @param type
	 */
	private Statistic(String name, StatisticType type) {
	    this.name = name;
	    this.type = type;
	}

	/**
	 * @return
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @return
	 */
	public StatisticType getType() {
	    return type;
	}

	/**
	 * @param name
	 * @return
	 */
	public static Statistic fromName(String name) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(s -> s.getName().equals(name)).//
		    findFirst().//
		    orElseThrow(() -> new IllegalArgumentException("Invalid statistic name: " + name));
	}

    }

    private StatisticsMessage createMessage(WebRequest webRequest) {

	StatisticsMessage message = new StatisticsMessage();

	KeyValueParser parser = new KeyValueParser(StringUtils.decodeUTF8(webRequest.getQueryString()));

	String maxResults = parser.getValue(MAX_RESULTS, "10");

	LogicalBond andBond = BondFactory.createAndBond();

	//
	// origin
	//
	if (parser.isValid(ORIGIN)) {

	    String[] originArray = parser.getValue(ORIGIN).split(",");

	    if (originArray.length == 1) {

		andBond.getOperands().add(//
			BondFactory.createRuntimeInfoElementBond(//
				BondOperator.EQUAL, //
				RuntimeInfoElement.RUNTIME_CONTEXT, //
				originArray[0]));
	    } else {

		LogicalBond orBond = BondFactory.createOrBond();

		Arrays.stream(parser.getValue(ORIGIN).split(",")).//

			forEach(hostName ->

			orBond.getOperands().add(//
				BondFactory.createRuntimeInfoElementBond(//
					BondOperator.EQUAL, //
					RuntimeInfoElement.RUNTIME_CONTEXT, //
					hostName)));

		andBond.getOperands().add(orBond);
	    }
	}

	Statistic statistic = Statistic.fromName(parser.getValue(STATISTIC));

	//
	// these constraints can be applied with all the stats. in particular the start time
	// of the analyzed period is always required; it is directly set by the 'periodRangeStart' param
	// or it is computed from the value of the 'period' param.
	// the method returns
	// the period of milliseconds and the start time of the period.
	// the time constraint queryable is DISCOVERY_MESSAGE_TIME_STAMP_MILLIS for all the
	// statistics except 'mostPopularResources' which is captured from the result set and thus requires
	// the RESULT_SET_TIME_STAMP_MILLIS
	//
	long[] periodStartTime = handlePeriodConstraints(parser, message, andBond, statistic);

	long period = periodStartTime[0];
	long startTime = periodStartTime[1];

	//
	// this constraint is applied with numberOfSearches, mostPopularKeywords and mostPopularAreas stats
	//
	handleCatalogConstraint(parser, andBond);

	if (!andBond.getOperands().isEmpty()) {

	    message.setUserBond(andBond.getOperands().size() == 1 ? andBond.getFirstOperand() : andBond);
	    message.setNormalizedBond(andBond.getOperands().size() == 1 ? andBond.getFirstOperand() : andBond);
	    message.setPermittedBond(andBond.getOperands().size() == 1 ? andBond.getFirstOperand() : andBond);
	}

	switch (statistic) {
	case MOST_POPULAR_RESOURCES:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.RESULT_SET_RESOURCE_TITLE), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_CATALOGS:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_ORG:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.DISCOVERY_MESSAGE_ORGANISATION_NAME), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_AREAS:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_KEYWORDS:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.DISCOVERY_MESSAGE_TITLE), //
		    Integer.valueOf(maxResults));

	    break;

	case NUMBER_OF_SEARCHES:
	case NUMBER_OF_ACCESS_REQUESTS:

	    RuntimeInfoElement target = statistic == Statistic.NUMBER_OF_SEARCHES ? //
		    RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP_MILLIS : //
		    RuntimeInfoElement.ACCESS_MESSAGE_TIME_STAMP_MILLIS;

	    GroupByPeriod groupByPeriod = new GroupByPeriod();

	    Interval interval = Interval.fromName(parser.getValue(INTERVAL, Interval.DAY.getName()));
	    String intervalSize = parser.getValue(INTERVAL_SIZE, "1");

	    groupByPeriod.setPeriod(period);
	    groupByPeriod.setStartTime(startTime);
	    groupByPeriod.setInterval(intervalSize + interval.getAbbreviation()); // e.g. 2d

	    double intervalSizeDouble = Double.parseDouble(intervalSize);
	    double fraction = 0;

	    groupByPeriod.setTarget(target);
	    switch (interval) {
	    case MINUTE:
		fraction = (period / (intervalSizeDouble * 1000 * 60));
		break;
	    case HOUR:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60));
		break;
	    case DAY:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60 * 24));
		break;
	    case WEEK:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60 * 24 * 7));
		break;
	    case MONTH:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60 * 24 * 30));
		break;
	    case QUARTER:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60 * 24 * 90));
		break;
	    case YEAR:
		fraction = (period / (intervalSizeDouble * 1000 * 60 * 60 * 24 * 365));
		break;
	    }

	    groupByPeriod.setFraction(fraction);

	    message.groupBy(groupByPeriod);

	    GSLoggerFactory.getLogger(getClass()).debug("Number of searches/requests grouped by: {} ", interval);
	    GSLoggerFactory.getLogger(getClass()).debug("Number of searches/requests grouped by period (in millis): {} ", period);
	    GSLoggerFactory.getLogger(getClass()).debug("Number of searches/requests grouped by fraction: {} ", fraction);

	    message.countDistinct(Arrays.asList(target));

	    message.setPage(new Page(1, (int) fraction));

	    break;

	case MOST_POPULAR_DISCOVERY_SERVICES:
	case MOST_POPULAR_ACCESS_SERVICES:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.PROFILER_NAME), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_DATA_CRS:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.ACCESS_MESSAGE_CRS), //
		    Integer.valueOf(maxResults));

	    break;
	case MOST_POPULAR_DATA_FORMAT:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.ACCESS_MESSAGE_DATA_FORMAT), //
		    Integer.valueOf(maxResults));

	    break;
	case MOST_POPULAR_DATA_TYPES:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.ACCESS_MESSAGE_DATA_TYPE), //
		    Integer.valueOf(maxResults));
	    break;

	case MOST_POPULAR_ACCESS_SOURCES:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_ID), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_DISCOVERY_SOURCES:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID), //
		    Integer.valueOf(maxResults));

	    break;

	case MOST_POPULAR_ACCESS_VIEW:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.ACCESS_MESSAGE_VIEW_ID), //
		    Integer.valueOf(maxResults));

	    break;
	case MOST_POPULAR_DISCOVERY_VIEW:

	    message.computeFrequency(//
		    Arrays.asList(RuntimeInfoElement.DISCOVERY_MESSAGE_VIEW_ID), //
		    Integer.valueOf(maxResults));

	    break;
	}

	return message;
    }

    /**
     * @param parser
     * @param message
     * @param andBond
     * @param statistic
     * @return
     */
    private long[] handlePeriodConstraints(//
	    KeyValueParser parser, //
	    StatisticsMessage message, //
	    LogicalBond andBond, //
	    Statistic statistic) {

	long period = 0;
	long startTime = 0;
	Optional<Long> endTime = Optional.empty();

	if (parser.isValid(PERIOD)) {

	    switch (Period.fromName(parser.getValue(PERIOD))) {
	    case LAST_WEEK:
		period = 1000l * 60 * 60 * 24 * 7;

		break;
	    case LAST_MONTH:

		period = 1000l * 60 * 60 * 24 * 30;
		break;

	    case LAST_YEAR:

		period = 1000l * 60 * 60 * 24 * 365;
		break;
	    }

	    startTime = System.currentTimeMillis() - period;

	} else {

	    String start = parser.getDecodedValue(PERIOD_RANGE_START);
	    Date startDate = ISO8601DateTimeUtils.parseISO8601ToDate(start).get();

	    startTime = startDate.getTime();

	    period = System.currentTimeMillis() - startTime;

	    if (parser.isValid(PERIOD_RANGE_END)) {

		String end = parser.getDecodedValue(PERIOD_RANGE_END);
		Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();

		endTime = Optional.of(endDate.getTime());

		period = endDate.getTime() - startTime;
	    }
	}

	RuntimeInfoElement timeConstraint = null;

	switch (statistic) {
	case MOST_POPULAR_ACCESS_VIEW:
	case MOST_POPULAR_DATA_CRS:
	case MOST_POPULAR_DATA_FORMAT:
	case MOST_POPULAR_DATA_TYPES:
	case NUMBER_OF_ACCESS_REQUESTS:
	    timeConstraint = RuntimeInfoElement.ACCESS_MESSAGE_TIME_STAMP_MILLIS;
	    break;
	case MOST_POPULAR_RESOURCES:
	case MOST_POPULAR_ACCESS_SOURCES:

	    timeConstraint = RuntimeInfoElement.RESULT_SET_TIME_STAMP_MILLIS;
	    break;

	case MOST_POPULAR_DISCOVERY_SERVICES:
	case MOST_POPULAR_ACCESS_SERVICES:

	    timeConstraint = RuntimeInfoElement.PROFILER_TIME_STAMP_MILLIS;
	    break;
	default:
	    timeConstraint = RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP_MILLIS;
	}

	andBond.getOperands().add(BondFactory.createRuntimeInfoElementBond(//
		BondOperator.GREATER_OR_EQUAL, //
		timeConstraint, //
		startTime));

	if (endTime.isPresent()) {
	    andBond.getOperands()

		    .add(BondFactory.createRuntimeInfoElementBond(//
			    BondOperator.LESS_OR_EQUAL, //
			    timeConstraint, //
			    endTime.get()));
	}

	return new long[] { period, startTime };
    }

    /**
     * @param parser
     * @param andBond
     */
    private void handleCatalogConstraint(KeyValueParser parser, LogicalBond andBond) {

	Optional<String> catalog = Optional.empty();
	if (parser.isValid(CATALOG)) {

	    catalog = Optional.of(parser.getDecodedValue(CATALOG));

	    //
	    // the following includes ONLY the supplied catalog
	    //
	    LogicalBond bond = BondFactory.createNotBond(//
		    BondFactory.createRuntimeInfoElementBond(//
			    BondOperator.NOT_EQUAL, //
			    RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL, //
			    catalog.get()));
	    //
	    // the following includes ALSO but not ONLY the supplied catalog
	    //
	    // RuntimeInfoElementBond bond = BondFactory.createRuntimeInfoElementBond(//
	    // BondOperator.EQUAL, //
	    // RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL, //
	    // catalog.get());

	    andBond.getOperands().add(bond);
	}
    }

    /**
     * @param response
     * @param parser
     * @return
     */
    private JSONObject mapResponse(StatisticsResponse response, KeyValueParser parser) {

	//
	// -----
	//
	Statistic statistic = Statistic.fromName(parser.getValue(STATISTIC));

	JSONObject jsonResponse = new JSONObject();
	jsonResponse.put("statistic", statistic.getName());

	final JSONArray results = new JSONArray();
	jsonResponse.put("results", results);

	switch (statistic) {
	case NUMBER_OF_SEARCHES:
	case NUMBER_OF_ACCESS_REQUESTS:

	    int itemsCount = response.getItemsCount();
	    jsonResponse.put("size", itemsCount);

	    response.getItems().forEach(item -> {

		ComputationResult result = item.getCountDistinct().get(0);
		String value = result.getValue();
		String startPeriod = item.getGroupedBy().get().split("#")[0];

		JSONObject resultObject = new JSONObject();
		resultObject.put("target", startPeriod);
		resultObject.put("value", value);

		results.put(resultObject);
	    });

	    break;
	default:

	    List<ComputationResult> frequency = response.getItems().get(0).getFrequency();

	    String value = frequency.get(0).getValue();
	    if (StringUtils.isNotEmpty(value)) {

		List<String> list = Arrays.asList(value.split(" "));

		list.forEach(v -> {

		    String[] split = v.split(ComputationResult.FREQUENCY_ITEM_SEP);

		    String target = split[0];
		    String val = split[1];

		    JSONObject resultObject = null;
		    switch (statistic) {
		    case MOST_POPULAR_ACCESS_SOURCES:

			resultObject = new JSONObject();
			resultObject.put("label", getSourceLabel(target));
			break;

		    case MOST_POPULAR_DISCOVERY_SERVICES:

			switch (target) {
			case "WMSProfiler":
			    break;
			default:
			    resultObject = new JSONObject();
			}

			break;
		    case MOST_POPULAR_ACCESS_SERVICES:

			switch (target) {
			case "HydroServerProfiler":
			case "RestProfiler":
			case "HydroCSVProfiler":
			case "SOSProfiler":
			case "GWISProfiler":
			case "GWPSProfiler":
			case "WMSProfiler":
			    resultObject = new JSONObject();
			    break;
			}

			break;
		    default:
			resultObject = new JSONObject();
		    }

		    if (Objects.nonNull(resultObject)) {

			resultObject.put("target", target);
			resultObject.put("value", val);

			results.put(resultObject);
		    }
		});
	    } else {
		jsonResponse.put("size", "0");
	    }

	    break;
	}

	jsonResponse.put("size", results.length());

	return jsonResponse;
    }

    /**
     * @param id
     * @return
     */
    private String getSourceLabel(String id) {

	return ConfigurationWrapper.getAllSources().//
		stream().//
		filter(s -> s.getUniqueIdentifier().equals(id)).//
		map(s -> s.getLabel()).//
		findFirst().//
		get();
    }
}
