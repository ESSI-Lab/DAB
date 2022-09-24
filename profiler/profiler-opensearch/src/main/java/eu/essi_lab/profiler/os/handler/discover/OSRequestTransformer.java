package eu.essi_lab.profiler.os.handler.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.odip.rosetta.RosettaStone;
import eu.essi_lab.lib.odip.rosetta.RosettaStoneConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSProfiler;
import eu.essi_lab.profiler.os.OSRequestParser;
import eu.essi_lab.profiler.os.handler.srvinfo.OSGetSourcesFilter;

/**
 * @author Fabrizio
 */
public class OSRequestTransformer extends DiscoveryRequestTransformer {

    /**
     * List of available supported formats
     */
    private static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();
    private static final String OS_PARAM_PARSING_ERROR = "OS_PARAM_PARSING_ERROR";
    private Logger logger = GSLoggerFactory.getLogger(OSRequestTransformer.class);

    // ---------------------------
    //
    // Use of this cached values is to improve discovery response time
    // when the requested view is geoss. we assume that in prod. env. such
    // variables will never change
    //
    private static StorageUri storageUri;
    private static StorageUri userJobStorageUri;
    //
    //
    // ---------------------------

    static {
	SUPPORTED_OUTPUT_FORMATS.add(MediaType.APPLICATION_JSON);
	SUPPORTED_OUTPUT_FORMATS.add(MediaType.APPLICATION_ATOM_XML);
	SUPPORTED_OUTPUT_FORMATS.add(NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE);
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	//
	// -----------------------------------------------------------------------------------
	//

	DiscoveryMessage message = createMessage();

	message.setRequestId(request.getRequestId());

	message.setRequestTimeout(10);

	message.setWebRequest(request);

	message.setPage(getPage(request));

	message.setCurrentUser(request.getCurrentUser());

	Optional<String> viewId = request.extractViewId();

	//
	// user job storage URI
	//
	if (viewId.orElse("").equals("geoss")) {

	    if (Objects.isNull(userJobStorageUri)) {

		userJobStorageUri = ConfigurationWrapper.getDownloadSetting().getStorageUri();
	    }

	    message.setUserJobStorageURI(userJobStorageUri);

	} else {

	    message.setUserJobStorageURI(ConfigurationWrapper.getDownloadSetting().getStorageUri());
	}

	//
	// DB URI
	//
	StorageUri uri = null;

	if (viewId.orElse("").equals("geoss")) {

	    if (Objects.isNull(storageUri)) {

		storageUri = ConfigurationWrapper.getDatabaseURI();
	    }

	    uri = storageUri;

	} else {

	    uri = ConfigurationWrapper.getDatabaseURI();
	}

	if (Objects.isNull(uri)) {

	    GSException exception = GSException.createException(getClass(), //
		    "Data Base storage URI not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_WARNING, //
		    DB_STORAGE_URI_NOT_FOUND);

	    message.getException().getErrorInfoList().add(exception.getErrorInfoList().get(0));

	    GSLoggerFactory.getLogger(this.getClass()).warn("Data Base storage URI not found");
	}

	message.setDataBaseURI(uri);

	//
	// shared repo info
	//
	if (viewId.orElse("").equals("geoss")) {

	} else {

	}

	message = refineMessage(message);

	//
	// set the view from the path if present
	//
	if (viewId.isPresent()) {

	    setView(viewId.get(), uri, message);
	}

	//
	// -----------------------------------------------------------------------------------
	//

	if (request.getFormData().isPresent()) {

	    KeyValueParser keyValueParser = new KeyValueParser(request.getFormData().get());
	    OSRequestParser parser = new OSRequestParser(keyValueParser);

	    if (OSGetSourcesFilter.isGetSourcesQuery(request)) {

		message.setRequestTimeout(30);
		message.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
		message.setOutputSources(true);
	    }

	    OSParameter evtOrder = WebRequestParameter.findParameter(OSParameters.EVENT_ORDER.getName(), OSParameters.class);
	    String evtOrderValue = parser.parse(evtOrder);

	    if (evtOrderValue != null && !evtOrderValue.equals("")) {

		message.setQuakeMLEventOrder(evtOrderValue);
	    }

	    OSParameter viewIdParam = WebRequestParameter.findParameter(OSParameters.VIEW_ID.getName(), OSParameters.class);

	    //
	    // set the view from the param if present
	    //
	    String viewIdValue = parser.parse(viewIdParam);

	    if (viewIdValue != null && !viewIdValue.equals("")) {

		setView(viewIdValue, uri, message);
	    }

	    //
	    // term frequency
	    //
	    String termFrequency = parser.parse(OSParameters.TERM_FREQUENCY);

	    List<String> values = Arrays.asList(termFrequency.split(","));

	    ArrayList<Queryable> list = Lists.newArrayList();

	    for (String v : values) {

		switch (v) {
		case "instrumentDesc":
		    list.add(MetadataElement.INSTRUMENT_DESCRIPTION);
		    break;
		case "instrumentTitle":
		    list.add(MetadataElement.INSTRUMENT_TITLE);
		    break;
		case "platformDesc":
		    list.add(MetadataElement.PLATFORM_DESCRIPTION);
		    break;
		case "platformTitle":
		    list.add(MetadataElement.PLATFORM_TITLE);
		    break;
		case "orgName":
		    list.add(MetadataElement.ORGANISATION_NAME);
		    break;
		case "origOrgDesc":
		    list.add(MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION);
		case "themeCategory":
		    list.add(MetadataElement.THEME_CATEGORY);
		    break;
		case "attributeTitle":
		    list.add(MetadataElement.ATTRIBUTE_TITLE);
		    break;
		case "instrumentId":
		    list.add(MetadataElement.INSTRUMENT_IDENTIFIER);
		    break;
		case "platformId":
		    list.add(MetadataElement.PLATFORM_IDENTIFIER);
		    break;
		case "origOrgId":
		    list.add(MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER);
		    break;
		case "attributeId":
		    list.add(MetadataElement.ATTRIBUTE_IDENTIFIER);
		    break;
		case "keyword":
		    list.add(MetadataElement.KEYWORD);
		    break;
		case "format":
		    list.add(MetadataElement.DISTRIBUTION_FORMAT);
		    break;
		case "protocol":
		    list.add(MetadataElement.ONLINE_PROTOCOL);
		    break;
		case "providerID":
		    list.add(ResourceProperty.SOURCE_ID);
		    break;
		case "organisationName":
		    list.add(MetadataElement.ORGANISATION_NAME);
		    break;
		case "prodType":
		    list.add(MetadataElement.PRODUCT_TYPE);
		    break;
		case "sensorOpMode":
		    list.add(MetadataElement.SENSOR_OP_MODE);
		    break;
		case "sensorSwath":
		    list.add(MetadataElement.SENSOR_SWATH);
		    break;
		case "sarPolCh":
		    list.add(MetadataElement.SAR_POL_CH);
		    break;
		case "sscScore":
		    list.add(ResourceProperty.SSC_SCORE);
		    break;
		case "s3InstrumentIdx":
		    list.add(MetadataElement.S3_INSTRUMENT_IDX);
		    break;
		case "s3ProductLevel":
		    list.add(MetadataElement.S3_PRODUCT_LEVEL);
		    break;
		case "s3Timeliness":
		    list.add(MetadataElement.S3_TIMELINESS);
		    break;
		}
	    }

	    message.setTermFrequencyTargets(list);
	}

	return message;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	Optional<String> queryString = request.getFormData();

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	if (request.getRequestPath() != null && request.getRequestPath().contains("description")) {

	    return message;
	}

	if (!queryString.isPresent() || queryString.get().isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).error("Validation failed, query part missing");

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Query part missing");
	    return message;
	}

	KeyValueParser keyValueParser = new KeyValueParser(queryString.get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	try {
	    // checks the sources param
	    OSParameter sourceParam = WebRequestParameter.findParameter(OSParameters.SOURCES.getName(), OSParameters.class);

	    String value = parser.parse(sourceParam);
	    if (value != null) {

		logger.trace("Found {} parameter, starting sources check", OSParameters.SOURCES.getName());

		String[] split = value.split(",");

		List<String> checkSources = ConfigurationWrapper.checkSources(Arrays.asList(split));

		if (!checkSources.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).error("Validation failed, the supplied sources does not exist: " + checkSources);

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("The supplied source does not exist: " + checkSources);
		    return message;
		}

		logger.trace("Completed sources check");
	    }

	    List<OSParameter> parameters = WebRequestParameter.findParameters(OSParameters.class);

	    // it can throws an IllegalArgumentException
	    boolean paramFound = false;
	    for (OSParameter osParameter : parameters) {
		String parse = parser.parse(osParameter);
		paramFound |= parse != null && !parse.equals("");
	    }

	    if (!paramFound) {

		GSLoggerFactory.getLogger(getClass())
			.error("Validation failed, invalid OpenSearch request: none of the supported search parameters is set");

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Invalid OpenSearch request: none of the supported search parameters is set");

		return message;
	    }

	    String outputFormat = parser.parse(OSParameters.OUTPUT_FORMAT);

	    if (outputFormat != null && !outputFormat.equals("")) {
		boolean supported = false;
		for (String format : SUPPORTED_OUTPUT_FORMATS) {
		    supported |= outputFormat.equals(format);
		}

		if (!supported) {

		    GSLoggerFactory.getLogger(getClass()).error("Validation failed, unsupported outputFormat: " + outputFormat);

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("Unsupported outputFormat: " + outputFormat);

		    return message;
		}
	    }

	    // String parsedParents = parser.parse(OSParameters.PARENTS);
	    //
	    // if (parsedParents != null && !parsedParents.isEmpty() && parsedParents.split(",").length > 1) {
	    // message.setResult(ValidationResult.VALIDATION_FAILED);
	    // message.setError("Only one parent value is allowed, found: " + parsedParents.split(",").length);
	    //
	    // return message;
	    // }

	} catch (IllegalArgumentException ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Validation failed: " + ex.getMessage(), ex);

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError(ex.getMessage());

	    return message;
	}

	return message;
    }

    @Override
    public Bond getUserBond(WebRequest request) throws GSException {

	if (!request.getFormData().isPresent()) {
	    return null;
	}

	KeyValueParser keyValueParser = new KeyValueParser(request.getFormData().get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	OSParameter sources = WebRequestParameter.findParameter(OSParameters.PARENTS.getName(), OSParameters.class);
	OSParameter id = WebRequestParameter.findParameter(OSParameters.ID.getName(), OSParameters.class);
	String sourcesValue = parser.parse(sources);
	String idValue = parser.parse(id);
	if ((sourcesValue != null && sourcesValue.equals("ROOT"))
		|| (request.getQueryString().toLowerCase().contains("getcontent") && idValue.equals("ROOT"))) {
	    // get sources request
	    return null;
	}

	// creates the bond list
	ArrayList<Bond> bondList = new ArrayList<>();

	// creates the search terms bond
	Bond searchTermsBond = createSearchTermsBond(parser);
	if (searchTermsBond != null) {
	    bondList.add(searchTermsBond);
	}

	String rosetta = parser.parse(OSParameters.ROSETTA);
	// adds the bonds created from the the available OS params
	List<OSParameter> parameters = WebRequestParameter.findParameters(OSParameters.class);
	for (OSParameter osParameter : parameters) {

	    if (osParameter.getName().equals(OSParameters.SEARCH_TERMS.getName())
		    || osParameter.getName().equals(OSParameters.SEARCH_FIELDS.getName())) {
		continue;
	    }

	    String value = parser.parse(osParameter);

	    if (osParameter.equals(OSParameters.INSTRUMENT_IDENTIFIER) || //
		    osParameter.equals(OSParameters.PLATFORM_IDENTIFIER) || //
		    osParameter.equals(OSParameters.ORIGINATOR_ORGANISATION_IDENTIFIER) || //
		    osParameter.equals(OSParameters.ATTRIBUTE_IDENTIFIER)) {
		if (rosetta != null && !rosetta.equals("false") && value != null) {
		    RosettaStone rs = new RosettaStoneConnector();
		    Set<String> terms = new TreeSet<>();
		    terms.add(value);
		    terms.addAll(rs.getTranslations(value));
		    switch (rosetta) {
		    case "narrow":
			Set<String> narrowerTerms = rs.getNarrower(value);
			if (narrowerTerms != null) {
			    terms.addAll(narrowerTerms);
			}
			break;
		    case "broad":
			Set<String> broaderTerms = rs.getBroader(value);
			if (broaderTerms != null) {
			    terms.addAll(broaderTerms);
			}
			break;

		    case "true":
		    default:
			// do nothing, because translations are always added
			break;
		    }
		    if (terms != null && !terms.isEmpty()) {
			value = createOrSearch(terms);
		    } else {
			value = null;
		    }
		}
	    }

	    Optional<Bond> bond = Optional.empty();

	    try {

		if (osParameter == OSParameters.BBOX || osParameter == OSParameters.W3W) {

		    String spatialRelation = parser.parse(OSParameters.SPATIAL_RELATION);
		    bond = osParameter.asBond(value, spatialRelation);

		} else if (osParameter == OSParameters.TIME_START || osParameter == OSParameters.TIME_END) {

		    String spatialRelation = parser.parse(OSParameters.TIME_RELATION);
		    bond = osParameter.asBond(value, spatialRelation);

		} else if (osParameter != OSParameters.VIEW_ID) {

		    bond = osParameter.asBond(value);
		}

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		throw GSException.createException(//
			getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			OS_PARAM_PARSING_ERROR, ex);
	    }

	    if (bond.isPresent()) {
		bondList.add(bond.get());
	    }
	}

	Bond bond = null;
	if (bondList.size() > 1) {
	    bond = BondFactory.createAndBond(bondList.toArray(new Bond[] {}));
	} else if (bondList.size() == 1) {
	    bond = bondList.get(0);
	}

	return bond;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return OSProfiler.OPEN_SEARCH_PROFILER_TYPE;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	if (OSGetSourcesFilter.isGetSourcesQuery(request)) {
	    //
	    // since in case of sources query
	    // the message.setDistinctValuesElement(ResourceProperty.SOURCE_ID) is set,
	    // at the moment this setting is ignored because in the current implementation
	    // the ResourceSubset is ignored when message.setDistinctValuesElement(ResourceProperty.SOURCE_ID)
	    // is set
	    //
	    // see GIP-287
	    //
	    selector.setSubset(ResourceSubset.SOURCE);
	} else {
	    selector.setSubset(ResourceSubset.FULL);
	}

	selector.setIncludeOriginal(false);

	return selector;
    }

    @Override
    protected Page getPage(WebRequest request) {

	if (!request.getFormData().isPresent()) {
	    return new Page(1, 10);
	}

	KeyValueParser keyValueParser = new KeyValueParser(request.getFormData().get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	int startIndex = Integer.parseInt(parser.parse(OSParameters.START_INDEX));
	int count = Integer.parseInt(parser.parse(OSParameters.COUNT));

	return new Page(startIndex, count);
    }

    private static String createOrSearch(Set<String> terms) {
	if (terms == null || terms.isEmpty()) {
	    return null;
	}
	ArrayList<String> list = Lists.newArrayList(terms);
	if (list.size() == 1) {
	    return list.get(0);
	}

	StringBuilder sb = new StringBuilder();

	for (int i = 0; i < list.size() - 1; i++) {

	    sb.append(list.get(i));

	    sb.append(" OR ");

	}

	sb.append(list.get(list.size() - 1));

	return sb.toString();

    }

    /**
     * <html>
     * <head>
     * <style>
     * table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; } td, th { border: 1px solid
     * #dddddd; text-align:
     * left; padding: 8px; }
     * </style>
     * </head>
     * <body>
     * <table>
     * <tr>
     * <td style="background-color: #ffd699"><b>#</b></td>
     * <td style="background-color: #ffd699"><b>Search terms</b></td>
     * <td style="background-color: #ffd699"><b>Search operator</b></td>
     * <td style="background-color: #ffd699"><b>Search fields</b></td>
     * <td style="background-color: #ffd699"><b>Example</b></td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">1</td>
     * <td style="background: #ccf2ff">A</td>
     * <td style="background: #bfff00">-</td>
     * <td style="background: #ffd24d">title / kwd / abstract / anyText</td>
     * <td style="background-color: white">( title ^ A )</td>
     * </tr>
     * <tr >
     * <td style="background-color: #ffd699">2</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ff00">OR</td>
     * <td style="background: #ffd24d">title / kwd / abstract / anyText</td>
     * <td style="background-color: white">( title ^ A ) <span style="background: #00ff00">OR</span> ( title ^ B )</td>
     * </tr>
     * <tr >
     * <td style="background-color: #ffd699">3</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ac00">AND</td>
     * <td style="background: #ffd24d">title / kwd / abstract / anyText</td>
     * <td style="background-color: white">( title ^ A ) <span style="background: #00ac00">AND</span> ( title ^ B )</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">4</td>
     * <td style="background: #ccf2ff">A</td>
     * <td style="background: #bfff00">-</td>
     * <td style="background: #ffff99">( title + subject ) / title * kwd * abstract * anyText</td>
     * <td style="background-color: white">( title ^ A ) <span style="background: #ffff99">OR</span> ( subject ^ A)</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">5</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ff00">OR</td>
     * <td style="background:#ffff99">( title + subject ) / title * kwd * abstract * anyText</td>
     * <td style="background-color: white">[( title ^ A ) <span style="background: #00ff00">OR</span> ( subject ^ A)]
     * <span style="background: #ffff99">OR</span> [( title ^ B ) <span style="background: #00ff00">OR</span> (
     * subject ^ B)]</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">6</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ac00">AND</td>
     * <td style="background:#ffff99">( title + subject ) / title * kwd * abstract * anyText</td>
     * <td style="background-color: white">[( title ^ A ) <span style="background: #00ac00">AND</span> ( subject ^ A)]
     * <span style="background: #ffff99">OR</span> [( title ^ B ) <span style="background: #00ac00">AND</span> (
     * subject ^ B)]</td>
     * </tr>
     * </table>
     * </body>
     * </html>
     */
    private Bond createSearchTermsBond(OSRequestParser parser) {

	String searchTerms = parser.parse(WebRequestParameter.findParameter(OSParameters.SEARCH_TERMS.getName(), OSParameters.class));
	String searchFields = parser.parse(WebRequestParameter.findParameter(OSParameters.SEARCH_FIELDS.getName(), OSParameters.class));
	if (searchFields == null) {
	    searchFields = "title,keyword";
	}

	if (Objects.nonNull(searchTerms) && !searchTerms.isEmpty()) {

	    if (searchTerms.startsWith("\"") && searchTerms.endsWith("\"")) {
		searchTerms = searchTerms.substring(1, searchTerms.length());
		searchTerms = searchTerms.substring(0, searchTerms.length() - 1);
	    }

	    searchTerms = searchTerms.trim();

	    LogicalOperator operator = null;

	    String[] terms;
	    String[] orTerms = searchTerms.split(" OR ");

	    if (orTerms.length > 1) {

		operator = LogicalOperator.OR;
		terms = orTerms;

	    } else {

		String[] andTerms = searchTerms.split(" AND ");

		if (andTerms.length > 1) {

		    operator = LogicalOperator.AND;
		    terms = andTerms;

		} else {
		    terms = new String[] { searchTerms };
		}
	    }

	    List<Bond> innerBonds = new ArrayList<>();

	    for (String searchTerm : terms) {

		ArrayList<Bond> operands = new ArrayList<>();

		if (searchFields.toLowerCase().contains("anytext")) {
		    operands.add(BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.ANY_TEXT, searchTerm));
		}

		if (searchFields.toLowerCase().contains("title")) {
		    operands.add(BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, searchTerm));
		}

		if (searchFields.toLowerCase().contains("subject")) {
		    operands.add(BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.SUBJECT, searchTerm));
		}

		if (searchFields.toLowerCase().contains("abstract") || searchFields.toLowerCase().contains("description")) {
		    operands.add(BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.ABSTRACT, searchTerm));
		}

		if (searchFields.toLowerCase().contains("keyword")) {
		    operands.add(BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, searchTerm));
		}

		switch (operands.size()) {
		case 0:
		    break;
		case 1:
		    innerBonds.add(operands.get(0));
		    break;
		default:
		    innerBonds.add(BondFactory.createOrBond(operands));
		    break;
		}

	    }

	    switch (innerBonds.size()) {
	    case 0:
		return null;
	    case 1:
		return innerBonds.get(0);
	    default:
		return BondFactory.createLogicalBond(operator, innerBonds);
	    }
	}

	return null;
    }

}
