/**
 * 
 */
package eu.essi_lab.profiler.rest.handler.info;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsMessage.GroupByPeriod;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * @author Fabrizio
 */
public class FullStatisticsHandler extends DefaultRequestHandler {

    private static final String GROUP_BY_TARGET = "groupByTarget";
    private static final String GROUP_BY_PERIOD_TARGET = "groupByPeriodTarget";
    private static final String GROUP_BY_PERIOD_PERIOD = "groupByPeriodPeriod";
    private static final String GROUP_BY_PERIOD_FRACTION = "groupByPeriodFraction";
    private static final String MAX_FREQUENCY_ITEMS = "maxFrequencyItems";
    private static final String MAX_TARGETS = "maxTargets";
    private static final String MIN_TARGETS = "minTargets";
    private static final String SUM_TARGETS = "sumTargets";
    private static final String AVG_TARGETS = "avgTargets";
    private static final String COUNT_DISTINCT_TARGETS = "countDistinctTargets";
    private static final String FREQUENCY_TARGETS = "frequencyTargets";
    private static final String BBOX_UNION = "bboxUnion";
    private static final String TEMP_EXTENT_UNION = "tempExtentUnion";
    private static final String QUERY_BBOX_UNION = "queryBboxUnion";
    private static final String QUERY_TEMP_EXTENT_UNION = "queryTempExtentUnion";
    private static final String START = "start";
    private static final String MAX_RESULTS = "maxResults";
    private static final String CONSTRAINTS = "constraints";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(FullStatisticsHandler.class).debug("Storage uri: {}", uri);

	DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(uri);

	StatisticsMessage message = createMessage(webRequest);

	StatisticsResponse response = executor.compute(message);

	String out = "<out></out>";

	try {
	    out = response.asString(true);

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return out;
    }

    /**
     * @param webRequest
     * @return
     * @throws GSException
     */
    private StatisticsMessage createMessage(WebRequest webRequest) throws GSException {

	StatisticsMessage message = new StatisticsMessage();

	KeyValueParser parser = new KeyValueParser(StringUtils.decodeUTF8(webRequest.getQueryString()));

	String start = parser.getValue(START, "1");
	String maxResults = parser.getValue(MAX_RESULTS, "100");

	if (parser.isValid(GROUP_BY_TARGET)) {

	    String target = parser.getDecodedValue(GROUP_BY_TARGET);

	    Optional<Queryable> queryable = findQueryable(target);
	    message.groupBy(queryable.get());

	    message.setPage(new Page(//
		    Integer.valueOf(start), //
		    Integer.valueOf(maxResults)));

	} else if (parser.isValid(GROUP_BY_PERIOD_TARGET)) {

	    String target = parser.getDecodedValue(GROUP_BY_PERIOD_TARGET);
	    Optional<Queryable> queryable = findQueryable(target);

	    GroupByPeriod groupByPeriod = new GroupByPeriod();
	    groupByPeriod.setTarget(queryable.get());

	    message.groupBy(groupByPeriod);

	    String period = parser.getDecodedValue(GROUP_BY_PERIOD_PERIOD);

	    groupByPeriod.setPeriod(Long.valueOf(period));

	    String fraction = parser.getDecodedValue(GROUP_BY_PERIOD_FRACTION);

	    groupByPeriod.setFraction(Integer.valueOf(fraction));

	    message.setPage(new Page(//
		    Integer.valueOf(start), //
		    Integer.valueOf(maxResults)));
	}

	if (parser.isValid(MAX_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, MAX_TARGETS);
	    if (!targets.isEmpty()) {

		message.computeMax(targets);
	    }
	}

	if (parser.isValid(MIN_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, MIN_TARGETS);
	    if (!targets.isEmpty()) {

		message.computeMin(targets);
	    }
	}

	if (parser.isValid(SUM_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, SUM_TARGETS);
	    if (!targets.isEmpty()) {

		message.computeSum(targets);
	    }
	}

	if (parser.isValid(AVG_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, AVG_TARGETS);
	    if (!targets.isEmpty()) {

		message.computeAvg(targets);
	    }
	}

	if (parser.isValid(COUNT_DISTINCT_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, COUNT_DISTINCT_TARGETS);
	    if (!targets.isEmpty()) {

		message.countDistinct(targets);
	    }
	}

	if (parser.isValid(FREQUENCY_TARGETS)) {

	    List<Queryable> targets = getTargets(parser, FREQUENCY_TARGETS);
	    if (!targets.isEmpty()) {

		String maxItems = parser.getValue(MAX_FREQUENCY_ITEMS, "10");

		message.computeFrequency(targets, Integer.valueOf(maxItems));
	    }
	}

	if (parser.isValid(BBOX_UNION)) {

	    String decodedValue = parser.getDecodedValue(BBOX_UNION);
	    if (decodedValue.equals("true")) {

		message.computeBboxUnion();
	    }
	}

	if (parser.isValid(TEMP_EXTENT_UNION)) {

	    String decodedValue = parser.getDecodedValue(TEMP_EXTENT_UNION);
	    if (decodedValue.equals("true")) {

		message.computeTempExtentUnion();
	    }
	}

	if (parser.isValid(QUERY_BBOX_UNION)) {

	    String decodedValue = parser.getDecodedValue(QUERY_BBOX_UNION);
	    if (decodedValue.equals("true")) {

		message.computeQueryBboxUnion();
	    }
	}

	if (parser.isValid(QUERY_TEMP_EXTENT_UNION)) {

	    String decodedValue = parser.getDecodedValue(QUERY_TEMP_EXTENT_UNION);
	    if (decodedValue.equals("true")) {

		message.computeQueryTempExtentUnion();
	    }
	}

	List<Bond> operands = new ArrayList<Bond>();

	StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();

	Optional<String> viewId = webRequest.extractViewId();

	if (viewId.isPresent()) {

	    Optional<View> optView = WebRequestTransformer.findView(storageUri, viewId.get());

	    if (optView.isPresent()) {

		View view = optView.get();

		Optional<Bond> optionalBond = message.getUserBond();

		if (!optionalBond.isPresent()) {

		    operands.add(view.getBond());

		} else {

		    operands.add(BondFactory.createAndBond(optionalBond.get(), view.getBond()));
		}
	    }
	}

	if (parser.isValid(CONSTRAINTS)) {

	    String decodedValue = parser.getDecodedValue(CONSTRAINTS);
	    List<String> constraints = Arrays.asList(decodedValue.split("\\|"));

	    if (constraints.size() == 1) {

		operands.add(createBond(constraints.get(0)));

	    } else {

		List<Bond> constOperands = new ArrayList<Bond>();
		boolean logicalAnd = false;

		for (String c : constraints) {

		    if (!c.equals("and") && !c.equalsIgnoreCase("or")) {

			constOperands.add(createBond(c));
		    } else {
			logicalAnd = c.equals("and");
		    }
		}

		operands.add(logicalAnd ? //
			BondFactory.createAndBond(constOperands) : //
			BondFactory.createOrBond(constOperands));
	    }
	}

	Bond bond = null;

	if (operands.size() == 1) {

	    bond = operands.get(0);
	} else if (operands.size() > 1) {

	    bond = BondFactory.createAndBond(operands);
	}

	message.setUserBond(bond);
	message.setNormalizedBond(bond);
	message.setPermittedBond(bond);

	return message;
    }

    /**
     * @param constraint
     * @return
     */
    private Bond createBond(String constraint) {

	//
	// isExecutable,true,boolean,=
	//

	String name = constraint.split(",")[0];
	String val = constraint.split(",")[1];
	if (val.equals("none")) {
	    val = "";
	}
	String type = constraint.split(",")[2];
	String op = constraint.split(",")[3];
	boolean not = false;
	if (constraint.split(",").length == 5) {
	    not = true;
	}

	Bond bond = null;

	Optional<Queryable> queryable = findQueryable(name);
	if (queryable.isPresent()) {

	    Queryable element = queryable.get();

	    String className = element.getClass().getName();

	    BondOperator bondOperator = Operator.fromOp(op);

	    if (className.equals(MetadataElement.class.getName())) {

		bond = BondFactory.createSimpleValueBond(bondOperator, (MetadataElement) element, val);

		// switch (type) {
		// case "int":
		// BondFactory.createSimpleValueBond(bondOperator, (MetadataElement) element,
		// Integer.valueOf(val));
		// break;
		// case "long":
		// BondFactory.createSimpleValueBond(bondOperator, (MetadataElement) element,
		// Long.valueOf(val));
		// break;
		// case "double":
		// BondFactory.createSimpleValueBond(bondOperator, (MetadataElement) element,
		// Double.valueOf(val));
		// break;
		// case "text":
		// BondFactory.createSimpleValueBond(bondOperator, (MetadataElement) element, val);
		// break;
		// }

	    } else if (className.equals(ResourceProperty.class.getName())) {

		bond = BondFactory.createResourcePropertyBond(bondOperator, (ResourceProperty) element, val);

	    } else if (className.equals(RuntimeInfoElement.class.getName())) {

		bond = BondFactory.createRuntimeInfoElementBond(bondOperator, (RuntimeInfoElement) element, val);
	    }
	}

	if (not) {
	    bond = BondFactory.createNotBond(bond);
	}

	return bond;
    }

    private enum Operator {

	LIKE("like"), //
	EQUAL("equal"), //
	NOT_EQUAL("notEqual"), //
	GREATER("gt"), //
	LESS("lt"), //
	GREATER_OR_EQUAL("gte"), //
	LESS_OR_EQUAL("lte");

	private String op;

	private Operator(String op) {

	    this.op = op;
	}

	/**
	 * @return
	 */
	public String getOp() {

	    return op;
	}

	public static BondOperator fromOp(String op) {

	    Operator operator = Arrays.asList(values()).//
		    stream().//
		    filter(e -> e.op.equals(op)).//
		    findFirst().//
		    get();

	    switch (operator) {
	    case EQUAL:
		return BondOperator.EQUAL;
	    case GREATER:
		return BondOperator.GREATER;
	    case GREATER_OR_EQUAL:
		return BondOperator.GREATER_OR_EQUAL;
	    case LESS:
		return BondOperator.LESS;
	    case LESS_OR_EQUAL:
		return BondOperator.LESS_OR_EQUAL;
	    case LIKE:
		return BondOperator.TEXT_SEARCH;
	    case NOT_EQUAL:
		return BondOperator.NOT_EQUAL;
	    default:
		return null;
	    }
	}

    }

    /**
     * @param parser
     * @param param
     * @return
     */
    private List<Queryable> getTargets(KeyValueParser parser, String param) {

	return Arrays.asList(//
		parser.getDecodedValue(param).//
			split(","))
		.//
		stream().//
		map(name -> findQueryable(name)).//
		filter(q -> q.isPresent()).//
		map(q -> q.get()).//
		collect(Collectors.toList());
    }

    /**
     * @param name
     * @return
     */
    private Optional<Queryable> findQueryable(String name) {

	Optional<Queryable> out = Optional.empty();

	try {
	    return Optional.of(MetadataElement.fromName(name));
	} catch (Exception ex) {
	}

	try {
	    return Optional.of(ResourceProperty.fromName(name));
	} catch (Exception ex) {
	}

	try {
	    return Optional.of(RuntimeInfoElement.fromName(name));
	} catch (Exception ex) {
	}

	return out;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
