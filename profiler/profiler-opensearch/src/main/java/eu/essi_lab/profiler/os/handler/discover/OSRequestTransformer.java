package eu.essi_lab.profiler.os.handler.discover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import com.google.common.collect.Lists;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.odip.rosetta.RosettaStone;
import eu.essi_lab.lib.odip.rosetta.RosettaStoneConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.DiscoveryMessage.EiffelAPIDiscoveryOption;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.BondUtils;
import eu.essi_lab.pdk.SemanticSearchSupport;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSProfilerSetting;
import eu.essi_lab.profiler.os.OSRequestParser;
import eu.essi_lab.profiler.os.handler.discover.covering.CoveringModeDiscoveryHandler;
import eu.essi_lab.profiler.os.handler.discover.covering.CoveringModeOptionsReader;
import eu.essi_lab.profiler.os.handler.discover.eiffel.EiffelDiscoveryHelper;
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

    protected ProfilerSetting setting;

    //
    //
    // ---------------------------

    static {
	SUPPORTED_OUTPUT_FORMATS.add(MediaType.APPLICATION_JSON);
	SUPPORTED_OUTPUT_FORMATS.add(MediaType.APPLICATION_ATOM_XML);
	SUPPORTED_OUTPUT_FORMATS.add(NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE);
    }

    /**
     * @param setting
     */
    public OSRequestTransformer() {

	this(new OSProfilerSetting());
    }

    /**
     * @param setting
     */
    public OSRequestTransformer(ProfilerSetting setting) {

	this.setting = setting;
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	message = super.refineMessage(message);

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	// including weighted queries to improve ranking
	message.setIncludeWeightedQueries(true);

	//
	// covering mode: overrides the satellites sources results priority,
	// set the covering mode view and adjusts the ranking to give a weight only to
	// bbox
	//
	if (CoveringModeOptionsReader.isCoveringModeEnabled(setting)) {

	    message.setResultsPriority(ResultsPriority.DATASET);

	    setView(CoveringModeDiscoveryHandler.COVERING_MODE_VIEW_ID, databaseURI, message);

	    RankingStrategy strategy = new RankingStrategy();
	    strategy.setAbstractWeight("0");
	    strategy.setAccessQualityWeight("0");
	    strategy.setAnyTextWeight("0");
	    strategy.setEssentialVariablesWeight("0");
	    strategy.setIsGDCWeight("0");
	    strategy.setMetadataQualityWeight("0");
	    strategy.setSubjectWeight("0");
	    strategy.setTitleWeight("0");
	    strategy.setBoundingBoxWeight("100");

	    message.setRankingStrategy(strategy);
	}

	//
	// -----------------------------------------------------------------------------------
	//

	if (message.getWebRequest().getFormData().isPresent()) {

	    //
	    // Get Sources query
	    //

	    if (OSGetSourcesFilter.isGetSourcesQuery(message.getWebRequest())) {

		message.setRequestTimeout(30);
		message.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
		message.setOutputSources(true);
	    }

	    //
	    //
	    //

	    KeyValueParser keyValueParser = new KeyValueParser(message.getWebRequest().getFormData().get());
	    OSRequestParser parser = new OSRequestParser(keyValueParser);

	    OSParameter evtOrder = WebRequestParameter.findParameter(OSParameters.EVENT_ORDER.getName(), OSParameters.class);
	    String evtOrderValue = parser.parse(evtOrder);

	    if (evtOrderValue != null && !evtOrderValue.equals("")) {

		message.setQuakeMLEventOrder(evtOrderValue);
	    }

	    OSParameter viewIdParam = WebRequestParameter.findParameter(OSParameters.VIEW_ID.getName(), OSParameters.class);

	    //
	    // Set the view from the param if present
	    //

	    String viewIdValue = parser.parse(viewIdParam);

	    if (viewIdValue != null && !viewIdValue.equals("")) {

		setView(viewIdValue, databaseURI, message);
	    }

	    //
	    // Term frequency
	    //

	    handleTermFrequency(parser, message);
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

	    // checks result window size
	    Page page = getPage(request);
	    int winSize = page.getStart() + page.getSize();
	    if (winSize > Database.MAX_RESULT_WINDOW_SIZE) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Result window is too large, start index + count must be less than or equal to: "
			+ Database.MAX_RESULT_WINDOW_SIZE + " but was " + winSize);

		return message;
	    }

	    // checks the sources param
	    OSParameter sourceParam = WebRequestParameter.findParameter(OSParameters.SOURCES.getName(), OSParameters.class);

	    String value = parser.parse(sourceParam);

	    if (value != null) {

		GSLoggerFactory.getLogger(getClass()).trace("Found {} parameter, starting sources check", OSParameters.SOURCES.getName());

		String[] split = value.split(",");

		List<String> checkSources = ConfigurationWrapper.checkSources(Arrays.asList(split));

		if (!checkSources.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).error("Validation failed, the supplied sources does not exist: " + checkSources);

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("The supplied source does not exist: " + checkSources);
		    return message;
		}

		GSLoggerFactory.getLogger(getClass()).trace("Completed sources check");
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
		if (outputFormat.equals("application/atom xml")) {
		    outputFormat = MediaType.APPLICATION_ATOM_XML;
		}
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

	    String eiffelDiscoveryOption = parser.parse(OSParameters.EIFFEL_DISCOVERY);

	    if (eiffelDiscoveryOption != null && !eiffelDiscoveryOption.equals("")) {

		boolean supported = eiffelDiscoveryOption.equals(//

			DiscoveryMessage.EiffelAPIDiscoveryOption.FILTER_AND_SORT.name())
			|| eiffelDiscoveryOption.equals(//
				DiscoveryMessage.EiffelAPIDiscoveryOption.SORT_AND_FILTER.name());

		if (!supported) {

		    GSLoggerFactory.getLogger(getClass())
			    .error("Validation failed, unsupported eiffel API discoery option: " + eiffelDiscoveryOption);

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("Validation failed, unsupported eiffel API discoery option: " + eiffelDiscoveryOption);

		    return message;
		}
	    }

	    // String parsedParents = parser.parse(OSParameters.PARENTS);
	    //
	    // if (parsedParents != null && !parsedParents.isEmpty() &&
	    // parsedParents.split(",").length > 1) {
	    // message.setResult(ValidationResult.VALIDATION_FAILED);
	    // message.setError("Only one parent value is allowed, found: " +
	    // parsedParents.split(",").length);
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

	// creates the bond list
	ArrayList<Bond> bondList = new ArrayList<>();

	//
	// search terms are NOT included in case of Eiffel SORT_AND_FILTER
	//
	Optional<EiffelAPIDiscoveryOption> eiffelOption = EiffelDiscoveryHelper.readEiffelOption(request, setting);

	if (!eiffelOption.isPresent() || eiffelOption.get() == EiffelAPIDiscoveryOption.FILTER_AND_SORT) {

	    Optional<String> searchTerms = parser.optParse(OSParameters.SEARCH_TERMS);

	    String searchFields = parser.parse(OSParameters.SEARCH_FIELDS);

	    //
	    // search terms
	    //

	    if (searchTerms.isPresent()) {

		Optional<Bond> searchTermsBond = createSearchTermsBond(parser, searchTerms.get(), searchFields, eiffelOption.isPresent());

		if (searchTermsBond.isPresent()) {

		    bondList.add(searchTermsBond.get());
		}
	    }

	    //
	    // attribute title
	    //

	    Optional<String> attrTitle = parser.optParse(OSParameters.ATTRIBUTE_TITLE);

	    if (attrTitle.isPresent()) {

		Optional<Bond> bond = createSearchTermsBond(//
			parser, //
			attrTitle.get(), //
			MetadataElement.ATTRIBUTE_TITLE_EL_NAME, //
			eiffelOption.isPresent());

		if (bond.isPresent()) {

		    bondList.add(bond.get());
		}
	    }

	    //
	    // concept URI
	    //

	    Optional<String> conceptUri = parser.optParse(OSParameters.SEMANTIC_CONCEPT_URI);

	    if (conceptUri.isPresent()) {

		Optional<Bond> conceptURIBond = createSearchTermsBond(parser, conceptUri.get(), searchFields, eiffelOption.isPresent());

		if (conceptURIBond.isPresent()) {

		    bondList.add(conceptURIBond.get());
		}
	    }
	}

	//
	// the portal set no sources if all are selected, here we add them in order to be in synch with
	// the message.getSources (see DiscoveryRequestTransformer)
	//

	// adds the bonds created from the the available OS params
	List<OSParameter> parameters = WebRequestParameter.findParameters(OSParameters.class);

	String rosetta = parser.parse(OSParameters.ROSETTA);

	for (OSParameter osParameter : parameters) {

	    Optional<Bond> bond = Optional.empty();

	    try {

		String value = parser.parse(osParameter);

		//
		// sources
		//

		if (osParameter.getName().equals(OSParameters.SOURCES.getName())) {

		    bond = handleSources(parser, request, osParameter, value);
		}

		//
		// already handled separately
		//
		if (osParameter.getName().equals(OSParameters.SEARCH_TERMS.getName())
			|| osParameter.getName().equals(OSParameters.SEARCH_FIELDS.getName())) {

		    continue;
		}

		//
		// rosetta
		//
		if ((osParameter.equals(OSParameters.INSTRUMENT_IDENTIFIER) || //
			osParameter.equals(OSParameters.PLATFORM_IDENTIFIER) || //
			osParameter.equals(OSParameters.ORIGINATOR_ORGANISATION_IDENTIFIER) || //
			osParameter.equals(OSParameters.ATTRIBUTE_IDENTIFIER)) && ( //
		rosetta != null && !rosetta.equals("false") && value != null)) {

		    value = handleRosetta(rosetta, value);
		}

		//
		// spatial
		//
		if (osParameter == OSParameters.BBOX || osParameter == OSParameters.W3W || osParameter == OSParameters.WKT) {

		    String spatialRelation = parser.parse(OSParameters.SPATIAL_RELATION);
		    bond = osParameter.asBond(value, spatialRelation);

		    //
		    // temporal
		    //
		} else if (osParameter == OSParameters.TIME_START || osParameter == OSParameters.TIME_END) {

		    String timeRelation = parser.parse(OSParameters.TIME_RELATION);
		    bond = osParameter.asBond(value, timeRelation);

		    //
		    // others
		    //
		} else if (osParameter != OSParameters.VIEW_ID && //
			osParameter != OSParameters.ATTRIBUTE_TITLE && //
			osParameter != OSParameters.SOURCES) {

		    String spatialRelation = parser.parse(OSParameters.SPATIAL_RELATION);
		    bond = osParameter.asBond(value, spatialRelation);
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

	return BondFactory.aggregate(bondList, LogicalOperator.AND).orElse(null);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return OSProfilerSetting.OPEN_SEARCH_PROFILER_TYPE;
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
	    // the ResourceSubset is ignored when
	    // message.setDistinctValuesElement(ResourceProperty.SOURCE_ID)
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

    /**
     * @param parser
     * @param request
     * @param osParameter
     * @param value
     * @return
     * @throws Exception
     */
    private Optional<Bond> handleSources(//
	    OSRequestParser parser, //
	    WebRequest request, //
	    OSParameter osParameter, //
	    String value) throws Exception {

	Optional<Bond> out = Optional.empty();

	String sources = parser.parse(OSParameters.SOURCES);

	String viewId = request.extractViewId().orElse(parser.parse(OSParameters.VIEW_ID));

	Optional<View> view = Optional.empty();

	if (viewId != null && !viewId.equals(KeyValueParser.UNDEFINED)) {

	    view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	}

	if (sources == null || sources.equals(KeyValueParser.UNDEFINED)) {

	    Stream<GSSource> stream = null;

	    if (view.isPresent()) {

		stream = ConfigurationWrapper.getViewSources(view.get()).stream();

	    } else {

		stream = ConfigurationWrapper.getAllSources().stream();
	    }

	    value = stream.map(s -> s.getUniqueIdentifier()).collect(Collectors.joining(","));

	    out = osParameter.asBond(value);

	    // } else if (view.isPresent() && view.get().getSourceDeployment() != null) {
	    //
	    // String sourceDeployment = view.get().getSourceDeployment();
	    //
	    // List<String> sourceIdsByDeployment = ConfigurationWrapper.getAllSources().//
	    // stream().filter(s -> s.getDeployment().contains(sourceDeployment)).//
	    // map(s -> s.getUniqueIdentifier()).//
	    // collect(Collectors.toList());//
	    //
	    // List<String> selected = Arrays.asList(sources.split(","));
	    // List<String> unselected = sourceIdsByDeployment.stream().//
	    // filter(id -> !selected.contains(id)).//
	    // collect(Collectors.toList());
	    //
	    // int selectedCount = selected.size();
	    // int unSelectedCount = unselected.size();
	    //
	    // if (selectedCount <= unSelectedCount) {
	    //
	    // out = osParameter.asBond(value);
	    //
	    // } else {
	    //
	    // LogicalBond orBond = BondFactory.createOrBond();
	    // unselected.forEach(id ->
	    // orBond.getOperands().add(BondFactory.createNotBond(BondFactory.createSourceIdentifierBond(id))));
	    //
	    // out = Optional.of(orBond);
	    // }

	} else {

	    out = osParameter.asBond(value);
	}

	return out;
    }

    /**
     * @param parser
     * @param message
     */
    private void handleTermFrequency(OSRequestParser parser, DiscoveryMessage message) {

	String termFrequency = parser.parse(OSParameters.TERM_FREQUENCY);

	List<String> values = Arrays.asList(termFrequency.split(","));

	ArrayList<Queryable> list = Lists.newArrayList();

	for (String v : values) {

	    switch (v) {
	    case "instrumentDesc" -> list.add(MetadataElement.INSTRUMENT_DESCRIPTION);
	    case "instrumentTitle" -> list.add(MetadataElement.INSTRUMENT_TITLE);
	    case "platformDesc" -> list.add(MetadataElement.PLATFORM_DESCRIPTION);
	    case "platformTitle" -> list.add(MetadataElement.PLATFORM_TITLE);
	    case "intendedObservationSpacing" -> list.add(MetadataElement.TIME_RESOLUTION_DURATION_8601);
	    case "aggregationDuration" -> list.add(MetadataElement.TIME_AGGREGATION_DURATION_8601);
	    case "timeInterpolation" -> list.add(MetadataElement.TIME_INTERPOLATION);
	    case "orgName" -> list.add(MetadataElement.ORGANISATION_NAME);
	    case "origOrgDesc" -> list.add(MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION);
	    case "themeCategory" -> list.add(MetadataElement.THEME_CATEGORY);
	    case "attributeTitle" -> list.add(MetadataElement.ATTRIBUTE_TITLE);
	    case "attributeURI", "observedPropertyURI" -> list.add(MetadataElement.OBSERVED_PROPERTY_URI);
	    case "instrumentId" -> list.add(MetadataElement.INSTRUMENT_IDENTIFIER);
	    case "platformId" -> list.add(MetadataElement.PLATFORM_IDENTIFIER);
	    case "origOrgId" -> list.add(MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER);
	    case "attributeId" -> list.add(MetadataElement.ATTRIBUTE_IDENTIFIER);
	    case "keyword" -> list.add(MetadataElement.KEYWORD);
	    case "format" -> list.add(MetadataElement.DISTRIBUTION_FORMAT);
	    case "protocol" -> list.add(MetadataElement.ONLINE_PROTOCOL);
	    case "providerID" -> list.add(ResourceProperty.SOURCE_ID);
	    case "organisationName" -> list.add(MetadataElement.ORGANISATION_NAME);
	    case "prodType" -> list.add(MetadataElement.PRODUCT_TYPE);
	    case "sensorOpMode" -> list.add(MetadataElement.SENSOR_OP_MODE);
	    case "sensorSwath" -> list.add(MetadataElement.SENSOR_SWATH);
	    case "sarPolCh" -> list.add(MetadataElement.SAR_POL_CH);
	    case "sscScore" -> list.add(ResourceProperty.SSC_SCORE);
	    case "s3InstrumentIdx" -> list.add(MetadataElement.S3_INSTRUMENT_IDX);
	    case "s3ProductLevel" -> list.add(MetadataElement.S3_PRODUCT_LEVEL);
	    case "s3Timeliness" -> list.add(MetadataElement.S3_TIMELINESS);
	    }
	}

	message.setTermFrequencyTargets(list);
    }

    /**
     * <html> <head> <style> table { font-family: arial, sans-serif;
     * border-collapse: collapse; width: 100%; } td, th { border: 1px solid #dddddd; text-align: left; padding: 8px; }
     * </style> </head>
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
     * <td style="background-color: white">( title ^ A )
     * <span style="background: #00ff00">OR</span> ( title ^ B )</td>
     * </tr>
     * <tr >
     * <td style="background-color: #ffd699">3</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ac00">AND</td>
     * <td style="background: #ffd24d">title / kwd / abstract / anyText</td>
     * <td style="background-color: white">( title ^ A )
     * <span style="background: #00ac00">AND</span> ( title ^ B )</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">4</td>
     * <td style="background: #ccf2ff">A</td>
     * <td style="background: #bfff00">-</td>
     * <td style="background: #ffff99">( title + subject ) / title * kwd * abstract
     * * anyText</td>
     * <td style="background-color: white">( title ^ A )
     * <span style="background: #ffff99">OR</span> ( subject ^ A)</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">5</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ff00">OR</td>
     * <td style="background:#ffff99">( title + subject ) / title * kwd * abstract *
     * anyText</td>
     * <td style="background-color: white">[( title ^ A )
     * <span style="background: #00ff00">OR</span> ( subject ^ A)]
     * <span style="background: #ffff99">OR</span> [( title ^ B )
     * <span style="background: #00ff00">OR</span> ( subject ^ B)]</td>
     * </tr>
     * <tr>
     * <td style="background-color: #ffd699">6</td>
     * <td style="background:#33ccff">A, B, ...</td>
     * <td style="background:#00ac00">AND</td>
     * <td style="background:#ffff99">( title + subject ) / title * kwd * abstract *
     * anyText</td>
     * <td style="background-color: white">[( title ^ A )
     * <span style="background: #00ac00">AND</span> ( subject ^ A)]
     * <span style="background: #ffff99">OR</span> [( title ^ B )
     * <span style="background: #00ac00">AND</span> ( subject ^ B)]</td>
     * </tr>
     * </table>
     * </body> </html>
     * 
     * @param keyValueParser
     * @param request
     * @param eiffelOption
     */
    private Optional<Bond> createSearchTermsBond(//
	    OSRequestParser parser, //
	    String searchTerms, //
	    String searchFields, //
	    boolean eiffelOption //
    ) {

	Optional<Integer> filterAndSortSplitTreshold = EiffelDiscoveryHelper.getFilterAndSortSplitTreshold(setting);

	if (eiffelOption && filterAndSortSplitTreshold.isPresent()) {

	    searchFields = "title,keyword,description";

	    String[] split = searchTerms.split(" ");
	    boolean notOr = searchTerms.split(" OR ").length == 1;
	    boolean notAnd = searchTerms.split(" AND ").length == 1;

	    if (split.length >= filterAndSortSplitTreshold.get() && //
		    notOr && //
		    notAnd) {

		searchTerms = searchTerms.replaceAll(" ", " OR ");
	    }

	} else if (searchFields == null) {

	    searchFields = "title,keyword";
	}

	LogicalOperator operator = null;

	if (searchTerms.startsWith("\"") && searchTerms.endsWith("\"")) {

	    searchTerms = searchTerms.substring(1, searchTerms.length());
	    searchTerms = searchTerms.substring(0, searchTerms.length() - 1);
	}

	searchTerms = searchTerms.trim();

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

	ArrayList<Bond> bonds = new ArrayList<Bond>();

	for (String searchTerm : terms) {

	    createFieldsBond(parser, searchFields, searchTerm).ifPresent(bond -> bonds.add(bond));
	}

	return BondFactory.aggregate(bonds, operator);
    }

    /**
     * @param parser
     * @param innerBonds
     * @param searchFields
     * @param searchValue
     */
    private Optional<Bond> createFieldsBond(//
	    OSRequestParser parser, //
	    String searchFields, //
	    String searchValue //
    ) {

	boolean semanticSearch = parser.optParse(OSParameters.SEMANTIC_SEARCH_ENABLED).map(v -> Boolean.valueOf(v)).orElse(false);
	String ontologyIds = parser.optParse(OSParameters.SEMANTIC_ONTOLOGY_IDS).orElse("");
	boolean withObsPropURIs = parser.optParse(OSParameters.SEMANTIC_WITH_OBSERVED_PROPERTIES_URIS).map(v -> Boolean.valueOf(v))
		.orElse(false);

	if (semanticSearch && !ontologyIds.isEmpty()) {

	    SemanticSearchSupport support = new SemanticSearchSupport();
	    support.setExpansionLevelParam(OSParameters.SEMANTIC_EXPANSION_LEVEL.getName());
	    support.setExpansionLimitParam(OSParameters.SEMANTIC_EXPANSION_LIMIT.getName());
	    support.setRelationsParam(OSParameters.SEMANTIC_RELATIONS.getName());
	    support.setSearchLangsParam(OSParameters.SEMANTIC_SEARCH_LANGS.getName());
	    support.setSourceLangsParam(OSParameters.SEMANTIC_SOURCE_LANGS.getName());

	    return support.getSemanticBond(parser.getParser(), searchValue, ontologyIds, searchFields, withObsPropURIs);
	}

	return BondUtils.createFieldsBond(searchFields, searchValue);
    }

    /**
     * @param rosetta
     * @param value
     * @return
     */
    private String handleRosetta(String rosetta, String value) {

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

	return value;
    }

    /**
     * @param terms
     * @return
     */
    private String createOrSearch(Set<String> terms) {

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

}
