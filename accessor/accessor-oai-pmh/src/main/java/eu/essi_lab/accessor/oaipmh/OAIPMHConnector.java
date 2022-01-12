package eu.essi_lab.accessor.oaipmh;

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

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Node;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.cdk.harvest.AbstractHarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpRequestExecutor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
public class OAIPMHConnector extends AbstractHarvestedQueryConnector {

    public static final String PREFERRED_PREFIX_KEY = "PREFERRED_PREFIX_KEY";
    /**
     *
     */
    private static final long serialVersionUID = -161607157535399027L;
    private static final String OAIPMH_CONNECTOR_KEY = "OAI_PMH_CONNECTOR_KEY";
    public static final String SET_OPTION_KEY = "SET_OPTION_KEY";
    private static final String ADV_OPTIONS_KEY = "ADV_OPTIONS_KEY";

    private static final String OAI_PMH_CONNECTOR_LIST_MD_FORMATS_ERROR = "OAI_PMH_CONNECTOR_LIST_MD_FORMATS_ERROR";
    private static final String OAI_PMH_CONNECTOR_LIST_RECORDS_ERROR = "OAI_PMH_CONNECTOR_LIST_RECORDS_ERROR";
    private static final String OAI_PMH_CONNECTOR_GET_RESUMPTION_TOKEN_ERROR = "OAI_PMH_CONNECTOR_GET_RESUMPTION_TOKEN_ERROR";
    private static final String OAI_PMH_CONNECTOR_GET_LIST_RECORDS_METADATAS_ERROR = "OAI_PMH_CONNECTOR_GET_LIST_RECORDS_METADATAS_ERROR";
    private static final String OAI_PMH_CONNECTOR_LIST_METADATA_FORMATS_ERROR = "OAI_PMH_CONNECTOR_LIST_METADATA_FORMATS_ERROR";
    private static final String OAI_PMH_CONNECTOR_METADATA_NODE_AS_STRING_ERROR = "OAI_PMH_CONNECTOR_METADATA_NODE_AS_STRING_ERROR";
    private static final String OAI_PMH_CONNECTOR_IDENTIFY_ERROR = "OAI_PMH_CONNECTOR_IDENTIFY_ERROR";
    private static final String OAI_PMH_GET_GRANULARITY_ERROR = "OAI_PMH_GET_GRANULARITY_ERROR";
    private static final String OAI_PMH_CONNECTOR_LIST_SETS_ERROR = "OAI_PMH_CONNECTOR_LIST_SETS_ERROR";

    @JsonIgnore
    public static transient String staticSetName;
    @JsonIgnore
    private transient String preferredPrefix;
    @JsonIgnore
    private transient int count;
    @JsonIgnore
    private transient List<SimpleEntry<String, String>> metadataFormatsNS;
    @JsonIgnore
    private transient String setName;
    @JsonIgnore
    private transient boolean isFirstRequest;
    @JsonIgnore
    private int maximumAttemptsCount;
    @JsonIgnore
    private boolean essiClientId;
    /**
     * 
     */
    private static final int DEFAULT_MAX_ATTEMPTS_COUNT = 3;

    public OAIPMHConnector() {

	setKey(OAIPMH_CONNECTOR_KEY);

	GSConfOptionBoolean advOption = new GSConfOptionBoolean();
	advOption.setLabel("Advanced options");
	advOption.setKey(ADV_OPTIONS_KEY);
	advOption.setValue(false);
	getSupportedOptions().put(ADV_OPTIONS_KEY, advOption);

	setSet(OAIPMHConnector.staticSetName);

	setMaxAttemptsCount(DEFAULT_MAX_ATTEMPTS_COUNT);
    }

    public OAIPMHConnector(String setName) {

	this();

	setSet(setName);
    }

    /**
     * @param preferredPrefix
     */
    @JsonIgnore
    public void setPreferredPrefix(String preferredPrefix) {

	this.preferredPrefix = preferredPrefix;
    }

    @Override
    public String getLabel() {

	return "OAIPMH Connector";
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String token = request.getResumptionToken();
	//
	// most OAI-PMH services provide a temporary resumption token, so it cannot be reused
	//
	if (request.getRecovering()) {
	    token = null;
	}

	String from = null;
	String until = null;
	String set = null;

	Optional<Integer> mr = getMaxRecords();

	if (token == null) {

	    count = 0;
	    isFirstRequest = true;

	    /**
	     * FIX FOR GEOSS EUMETSAT DATA CATALOGUE
	     * It seems that TIME is not supported by EUMETSAT OAI-PMH catalogue
	     **/

	    if (!getSourceURL().contains("eoportal.eumetsat.int")) {

		from = request.getFromDateStamp();
		until = request.getUntilDateStamp();
		if (until == null) {
		    until = ISO8601DateTimeUtils.getISO8601DateTime();
		}

		//
		// removing milliseconds, if present (never supported)
		//
		if (from != null && from.contains(".")) {
		    from = from.substring(0, from.indexOf("."));
		}
		if (until != null && until.contains(".")) {
		    until = from.substring(0, until.indexOf("."));
		}

		//
		// granularity check
		//
		XMLDocumentReader identifyResponse = getIdentifyResponse(getSourceURL());
		String granularity = getGranularity(identifyResponse);

		if (granularity != null) {

		    if (!granularity.contains("T")) {
			if (from != null && from.contains("T")) {
			    from = from.substring(0, from.indexOf('T'));
			}
			if (until != null && until.contains("T")) {
			    until = until.substring(0, until.indexOf('T'));
			}
		    }

		    if (granularity.endsWith("Z")) {
			if (from != null && !from.endsWith("Z")) {
			    from = from + "Z";
			}
			if (until != null && !until.endsWith("Z")) {
			    until = until + "Z";
			}
		    } else {
			if (from != null && from.endsWith("Z")) {
			    from = from.substring(0, from.length() - 1);
			}
			if (until != null && until.endsWith("Z")) {
			    until = until.substring(0, until.length() - 1);
			}
		    }
		}
	    }

	    //
	    // set
	    //
	    GSConfOption<?> setOption = getSupportedOptions().get(SET_OPTION_KEY);
	    if (setOption != null) {
		Object value = getSupportedOptions().get(SET_OPTION_KEY).getValue();
		if (value != null) {
		    set = value.toString();
		}
	    }
	}

	if (preferredPrefix == null) {
	    metadataFormatsNS = listMetadataFormatsNS(getSourceURL());

	    preferredPrefix = getConfiguredPrefix();
	    if (preferredPrefix == null) {
		preferredPrefix = getPreferredPrefix();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Preferred prefix: {}", preferredPrefix);

	// exec the list records
	String listRecords = createListRecordsRequest(preferredPrefix, from, until, token, set);
	XMLDocumentReader reader = execListRecords(listRecords);

	// set the resumption token to the response
	setResumptionToken(reader, ret);

	// read the metadatas from the ListRecordsResponse
	List<Node> records = readRecords(reader);

	// try removing from and until to get some results
	if (isFirstRequest && records.isEmpty()) {
	    isFirstRequest = false;
	    String listRecordsNoTime = createListRecordsRequest(preferredPrefix, null, null, token, set);
	    reader = execListRecords(listRecordsNoTime);
	    // set the resumption token to the response
	    setResumptionToken(reader, ret);
	    records = readRecords(reader);

	}

	for (Node record : records) {

	    OriginalMetadata metadataRecord = new OriginalMetadata();

	    // ------------------------------------
	    //
	    // set the OAI ns URI
	    //
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.OAI_NS_URI);

	    // ------------------------------------
	    //
	    // set the metadata
	    //
	    String metadata = asString(record);

	    metadataRecord.setMetadata(metadata);

	    ret.addRecord(metadataRecord);

	    count++;

	    if (!isMaxRecordsUnlimited() && mr.isPresent() && count == mr.get()) {
		GSLoggerFactory.getLogger(getClass()).info("Reached max. records: {}", mr.get());
		GSLoggerFactory.getLogger(getClass()).info("Setting null resumption token to stop the harvesting");

		ret.setResumptionToken(null);
	    }
	}

	return ret;
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return true;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	try {
	    List<String> ret = new ArrayList<>();

	    List<SimpleEntry<String, String>> formatsNS = listMetadataFormatsNS(getSourceURL());
	    for (SimpleEntry<String, String> entry : formatsNS) {
		ret.add(entry.getKey());
	    }
	    return ret;
	} catch (Exception e) {
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_LIST_MD_FORMATS_ERROR, //
		    e);
	}
    }

    @Override
    public boolean supports(Source source) {

	String endpoint = source.getEndpoint();

	try {

	    if (!endpoint.endsWith("?")) {
		endpoint += "?";
	    }
	    XMLDocumentReader response = getIdentifyResponse(endpoint);
	    return response.evaluateBoolean("exists(//*:Identify)");

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).debug("Not supported endpoint {}", endpoint);
	}

	return false;
    }

    @Override
    public String getSourceURL() {

	return super.getSourceURL().endsWith("?") ? super.getSourceURL() : super.getSourceURL() + "?";
    }

    /**
     * @param setName
     */
    private void setSet(String setName) {

	this.setName = setName;

	GSConfOptionString setOption = new GSConfOptionString();
	setOption.setKey(SET_OPTION_KEY);
	setOption.setLabel("Select set");
	setOption.setValue(setName);
	getSupportedOptions().put(SET_OPTION_KEY, setOption);
    }

    /**
     * @return
     */
    public String getSetName() {

	return setName;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (opt instanceof GSConfOptionBoolean) {

	    GSConfOptionBoolean advanced = (GSConfOptionBoolean) opt;

	    if (advanced.getValue()) {

		GSConfOptionString prefKeyOptionString = new GSConfOptionString();
		prefKeyOptionString.setKey(PREFERRED_PREFIX_KEY);
		prefKeyOptionString.setLabel("Select prefix");
		getSupportedOptions().put(PREFERRED_PREFIX_KEY, prefKeyOptionString);

		GSConfOptionString setOption = new GSConfOptionString();
		setOption.setKey(SET_OPTION_KEY);
		setOption.setLabel("Select set");
		getSupportedOptions().put(SET_OPTION_KEY, setOption);

		try {

		    List<SimpleEntry<String, String>> nameSpaces = listMetadataFormatsNS(getSourceURL());

		    List<String> allowed = new ArrayList<>();

		    for (SimpleEntry<String, String> ns : nameSpaces) {

			allowed.add(ns.getKey());
		    }

		    ((GSConfOptionString) getSupportedOptions().get(PREFERRED_PREFIX_KEY)).setAllowedValues(allowed);

		    List<String> sets = getSets(getSourceURL());
		    ((GSConfOptionString) getSupportedOptions().get(SET_OPTION_KEY)).setAllowedValues(sets);

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		}

	    } else {

		getSupportedOptions().remove(PREFERRED_PREFIX_KEY);
		getSupportedOptions().remove(SET_OPTION_KEY);
	    }
	}

    }

    /**
     * 
     */
    @JsonIgnore
    public void setESSILabClientId() {

	this.essiClientId = true;
    }

    /**
     * Set the maximum number of connection attempts before throw exception. Default is 3
     * 
     * @param count
     */
    @JsonIgnore
    public void setMaxAttemptsCount(int count) {

	this.maximumAttemptsCount = count;
    }

    private String getConfiguredPrefix() {

	GSConfOption<?> gsConfOption = getSupportedOptions().get(PREFERRED_PREFIX_KEY);
	if (gsConfOption == null) {
	    return null;
	}

	return gsConfOption.getValue() != null ? gsConfOption.getValue().toString() : null;
    }

    private List<Node> readRecords(XMLDocumentReader reader) throws GSException {

	try {
	    return reader.evaluateOriginalNodesList("//*:record");

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_GET_LIST_RECORDS_METADATAS_ERROR, //
		    e);
	}
    }

    private void setResumptionToken(XMLDocumentReader reader, ListRecordsResponse<OriginalMetadata> ret) throws GSException {

	try {
	    // set the resumption token
	    Node tokenNode = reader.evaluateNode("//*:resumptionToken");
	    String resToken = null;
	    if (tokenNode != null) {

		if (GSLoggerFactory.getLogger(getClass()).isDebugEnabled())
		    GSLoggerFactory.getLogger(getClass()).debug("Resumption token element: {}",
			    XMLDocumentReader.asString(tokenNode).trim());

		resToken = reader.evaluateString(tokenNode, "text()");
	    } else {
		GSLoggerFactory.getLogger(getClass()).debug("No resumption token element found, connection closed");
	    }

	    if (resToken != null && !resToken.equals("")) {
		GSLoggerFactory.getLogger(getClass()).debug("Resumption token value: {}", resToken);
		ret.setResumptionToken(resToken);
	    } else {
		GSLoggerFactory.getLogger(getClass()).debug("No resumption token value found, connection closed");
		ret.setResumptionToken(null);
	    }

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_GET_RESUMPTION_TOKEN_ERROR, //
		    e);
	}

    }

    private XMLDocumentReader execListRecords(String listRecords) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Serving listRecords: {}", listRecords);

	HttpGet httpGet = new HttpGet(listRecords);

	if (essiClientId) {
	    httpGet.addHeader(WebRequest.CLIENT_IDENTIFIER_HEADER, WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);
	}

	int attempt = 0;
	while (attempt < maximumAttemptsCount) {

	    try {

		GSLoggerFactory.getLogger(getClass()).debug("Attempt #{} in progress", attempt + 1);

		HttpResponse response = new HttpRequestExecutor().execute(httpGet);

		InputStream content = response.getEntity().getContent();

		XMLDocumentReader reader = new XMLDocumentReader(content);

		if (reader.evaluateBoolean("exists(//*:ListRecords)")) {
		    GSLoggerFactory.getLogger(getClass()).debug("ListRecords successful");

		    return reader;
		}

		GSLoggerFactory.getLogger(getClass()).error("The service returned an error response: {}", reader.asString());

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Error occurred: " + e.getMessage(), e);
	    }

	    attempt++;

	    if (attempt < maximumAttemptsCount) {

		GSLoggerFactory.getLogger(getClass()).debug("Attempt #{} failed", attempt);
		GSLoggerFactory.getLogger(getClass()).debug("Failed request: {}", listRecords);

		try {

		    GSLoggerFactory.getLogger(getClass()).debug("Waiting  60 seconds for a new attempt...");
		    Thread.sleep(60000);

		} catch (InterruptedException ex) {
		    GSLoggerFactory.getLogger(getClass()).warn("Interrupted!", ex);
		    Thread.currentThread().interrupt();
		}
	    }
	}

	throw GSException.createException( //
		getClass(), //
		"Unable to execute listRecords after " + maximumAttemptsCount + " attempts", //
		"Unable to execute listRecords after " + maximumAttemptsCount + " attempts", //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		OAI_PMH_CONNECTOR_LIST_RECORDS_ERROR);
    }

    private String createListRecordsRequest(String preferredPrefix, String from, String until, String token, String set) {

	String listRecords = getSourceURL() + "verb=ListRecords";

	if (token != null) {
	    listRecords += "&resumptionToken=" + token;
	} else {
	    listRecords += "&metadataPrefix=" + preferredPrefix;

	    if (from != null) {
		listRecords += "&from=" + from;
	    }

	    if (until != null) {
		listRecords += "&until=" + until;
	    }
	}

	if (set != null) {
	    listRecords += "&set=" + set;
	}

	return listRecords;
    }

    private String getPreferredPrefix() {

	String gmdPrefix = null;
	String dcPrefix = null;

	for (SimpleEntry<String, String> entry : metadataFormatsNS) {

	    switch (entry.getValue()) {
	    case CommonNameSpaceContext.GMD_NS_URI:
	    case CommonNameSpaceContext.GMI_NS_URI:
		gmdPrefix = entry.getKey();
		break;
	    case CommonNameSpaceContext.OAI_DC_NS_URI:
	    default:
		dcPrefix = entry.getKey();
		break;
	    }
	}

	if (gmdPrefix != null) {
	    return gmdPrefix;
	}

	if (dcPrefix != null) {
	    return dcPrefix;
	}

	return null;
    }

    private List<String> getSets(String sourceURL) throws GSException {

	String listSets = sourceURL + "verb=ListSets";
	HttpGet httpGet = new HttpGet(listSets);

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Serving GET: {}", listSets);
	    HttpResponse response = new HttpRequestExecutor().execute(httpGet);

	    InputStream content = response.getEntity().getContent();
	    XMLDocumentReader reader = new XMLDocumentReader(content);

	    return Arrays.asList(reader.evaluateNodes("//*:setSpec")).//
		    stream().//
		    map(n -> ((org.w3c.dom.Element) n).getTextContent()).//
		    collect(Collectors.toList());

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_LIST_SETS_ERROR, //
		    e);
	}
    }

    private List<SimpleEntry<String, String>> listMetadataFormatsNS(String sourceURL) throws GSException {

	try {

	    List<SimpleEntry<String, String>> ret = new ArrayList<>();

	    String listMdFormats = sourceURL + "verb=ListMetadataFormats";
	    GSLoggerFactory.getLogger(OAIPMHConnector.class).trace("Requesting Metadata formats {}", listMdFormats);

	    HttpGet httpGet = new HttpGet(listMdFormats);
	    HttpResponse response = new HttpRequestExecutor().execute(httpGet);
	    InputStream content = response.getEntity().getContent();
	    XMLDocumentReader reader = new XMLDocumentReader(content);

	    Node[] nodes = reader.evaluateNodes("//*:metadataFormat");
	    for (Node nodeResult : nodes) {

		String prefix = reader.evaluateNode(nodeResult, "*:metadataPrefix").getTextContent();
		String ns = reader.evaluateNode(nodeResult, "*:metadataNamespace").getTextContent();

		SimpleEntry<String, String> entry = new SimpleEntry<>(prefix, ns);
		ret.add(entry);
	    }

	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_LIST_METADATA_FORMATS_ERROR, //
		    e);
	}
    }

    private String getGranularity(XMLDocumentReader reader) throws GSException {

	try {
	    return reader.evaluateString("//*:granularity");

	} catch (XPathExpressionException e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_GET_GRANULARITY_ERROR, //
		    e);
	}

    }

    private XMLDocumentReader getIdentifyResponse(String sourceURL) throws GSException {

	try {

	    String identify = sourceURL + "verb=Identify";
	    HttpGet httpGet = new HttpGet(identify);

	    GSLoggerFactory.getLogger(getClass()).debug("Serving GET: {}", identify);
	    HttpResponse response = new HttpRequestExecutor().execute(httpGet);

	    InputStream content = response.getEntity().getContent();
	    return new XMLDocumentReader(content);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_IDENTIFY_ERROR, //
		    e);
	}
    }

    private String asString(Node node) throws GSException {

	try {

	    return XMLDocumentReader.asString(node);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_CONNECTOR_METADATA_NODE_AS_STRING_ERROR, //
		    e);
	}
    }
}
