package eu.essi_lab.accessor.csw;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.w3c.dom.Node;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.accessor.csw.parser.CSWOperationParser;
import eu.essi_lab.cdk.harvest.AbstractHarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordsResponse;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.RecordType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.csw._2_0_2.SearchResultsType;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.jaxb.ows._1_0_0.OperationsMetadata;
import eu.essi_lab.lib.net.utils.HttpRequestExecutor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
public class CSWConnector extends AbstractHarvestedQueryConnector {

    public static final String CSW_EXCEPTIO_REPORT_ERROR = "CSW_EXCEPTIO_REPORT_ERROR";
    public static final String CSW_GETRECORDS_UNMARSHALL_ERROR = "CSW_GETRECORDS_UNMARSHALL_ERROR";
    String selectedSchema;
    // originally set equals to sourceURL, this URL may change after reading the capabilities document
    String getRecordsURLGET;
    String getRecordsURLPOSTXML;
    String getRecordsURLPOSTSOAP;
    protected transient Map<String, QName> supportedTypesBySchema = new HashMap<>();
    @JsonIgnore
    protected transient List<String> supportedOutputSchemas = new ArrayList<>();
    protected static final String CSW_CONNECTOR_PAGESIZE_OPTION_KEY = "CSW_CONNECTOR_PAGESIZE_OPTION_KEY";
    /**
     *
     */
    private static final long serialVersionUID = -3882045719832526204L;
    private static final String CSW_CONNECTOR_CAPABILITIES_EXTRACTION_ERROR = "CSW_CONNECTOR_CAPABILITIES_EXTRACTION_ERROR";
    private static final String CSW_CONNECTOR_METADATA_NODE_AS_STRING_ERROR = "CSW_CONNECTOR_METADATA_NODE_AS_STRING_ERROR";
    // the source URL as input by the user
    private String sourceURL;
    private Binding getRecordsBinding = Binding.POST_XML;
    private int startIndex;
    // by default request is set to 10
    private int requestSizeDefault = 100;
    private static final String GET_RECORDS_OPERATION_NAME = "GetRecords";
    @JsonIgnore
    private transient Capabilities capabilitiesDocument;
    private static final String PREFERRED_VERSION = "2.0.2";
    private static final String ACCEPTED_VERSION = "2.0.2";
    private static final String OUTPUT_FORMAT = "application/xml";
    private static final String CONSTRAINT_LAGUAGEPARAMETER = "&CONSTRAINTLANGUAGE=CQL_TEXT";

    public enum Binding {
	GET, POST_XML, POST_SOAP
    }

    public CSWConnector() {

	startIndex = 1;

	GSConfOption<Integer> pageSizeOption = new GSConfOptionInteger();
	pageSizeOption.setLabel("GetRecords page size");
	pageSizeOption.setKey(CSW_CONNECTOR_PAGESIZE_OPTION_KEY);
	pageSizeOption.setValue(requestSizeDefault);
	getSupportedOptions().put(pageSizeOption.getKey(), pageSizeOption);
    }

    /**
     * @return
     */
    protected GSConfOptionInteger getPageSizeOption() {

	return (GSConfOptionInteger) getSupportedOptions().get(CSW_CONNECTOR_PAGESIZE_OPTION_KEY);
    }

    @Override
    public String getLabel() {

	return "CSW Connector";
    }

    public Binding getGetRecordsBinding() {
	return getRecordsBinding;
    }

    /**
     * Sets the binding to be used by this connector to perform the GetRecords request (POST_XML by default if not set)
     *
     * @param getRecordsBinding
     */
    public void setGetRecordsBinding(Binding getRecordsBinding) {
	this.getRecordsBinding = getRecordsBinding;
    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {
	return new CSWHttpGetRecordsRequestCreator(getRecordsBinding, this, getRecords);
    }

    SimpleEntry<Integer, File> doExecHttpRequest(HttpRequestBase baseRequest) throws GSException {

	Integer code = null;
	try {

	    HttpResponse httpResponse = new HttpRequestExecutor().execute(baseRequest);

	    code = httpResponse.getStatusLine().getStatusCode();

	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Returnded http code {}", code);

	    InputStream content = httpResponse.getEntity().getContent();

	    File tmpFile = File.createTempFile("CSWConnector", ".xml");
	    tmpFile.deleteOnExit();

	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(content, fos);
	    content.close();
	    fos.close();

	    SimpleEntry<Integer, File> ret = new SimpleEntry<>(code, tmpFile);

	    return ret;

	} catch (IOException e1) {

	    GSLoggerFactory.getLogger(CSWConnector.class).error("Error executing Http request to {}", baseRequest.getURI().toString(), e1);

	    throw GSException.createException( //
		    getClass(), //
		    "IOException connecting to " + baseRequest.getURI().toString() + " -- http code: " + code, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_EXCEPTIO_REPORT_ERROR, e1);
	}
    }

    private int calculateStart(String token) {
	// the first request start position is "1"
	int startPosition = this.startIndex;
	if (token != null) {
	    startPosition = Integer.parseInt(token);
	}

	return startPosition;
    }

    private int getRecordsLimit() {
	Optional<Integer> op = getMaxRecords();

	return op.isPresent() ? op.get() : -100;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	readCapabilitiesDocument();

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int startPosition = calculateStart(request.getResumptionToken());

	int requestSize = getRequestSize();

	int recordsLimit = getRecordsLimit();

	if (!isMaxRecordsUnlimited() && startPosition >= recordsLimit) {
	    // max records reached - collection of new records is stopped
	    ret.setResumptionToken(null);

	    GSLoggerFactory.getLogger(CSWConnector.class)
		    .debug("Reached max records limit of {} records. Skipping collection of additional records!", recordsLimit);

	    return ret;
	}

	Integer code = null;
	File file = null;

	int tries = 3;

	while ((code == null || code > 400) && tries > 0) {

	    GetRecords getRecords = createGetRecords(startPosition, requestSize);

	    CSWHttpGetRecordsRequestCreator creator = getCreator(getRecords);

	    String getRecordsURL = creator.getGetRecordsUrl();

	    HttpRequestBase baseRequest = creator.getHttpRequest();

	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Executing GetRecords request ({}-{}) to URL: {}",
		    getRecords.getStartPosition(), getRecords.getStartPosition().add(getRecords.getMaxRecords()).subtract(BigInteger.ONE),
		    getRecordsURL);

	    SimpleEntry<Integer, File> codeFile = doExecHttpRequest(baseRequest);

	    code = codeFile.getKey();
	    file = codeFile.getValue();
	    if (code > 400) {
		// try to request one record only, for some services such as NODC it will work
		requestSize = requestSize / 2;
		if (requestSize == 0) {
		    requestSize = 1;
		}
		GSLoggerFactory.getLogger(getClass()).info("Retrying CSW GetRecords with smaller size (" + requestSize + ")");
		if (requestSize == 1) {
		    tries--;
		    GSLoggerFactory.getLogger(getClass()).info("Retrying CSW GetRecords with minimal size. " + tries + " tries left.");
		}
		// delete the error response
		file.delete();
	    }
	}

	SearchResultsType searchResults = toSearchResults(file);

	if (file != null && file.exists())
	    file.delete();

	GSLoggerFactory.getLogger(CSWConnector.class).debug("GetRecords response obtained (" + searchResults.getNumberOfRecordsReturned()
		+ " returned / " + searchResults.getNumberOfRecordsMatched() + " matched)");

	ret = toOriginalMetadata(searchResults);

	filterResults(ret);

	BigInteger nextRecordField = searchResults.getNextRecord();
	Integer nextRecord;
	if (nextRecordField != null) {
	    nextRecord = nextRecordField.intValue();
	} else {
	    // very few servers (such as SAEON CSW http://app01.saeon.ac.za/PLATFORM_TEST/MAP/csw.asp? don't implement
	    // nextRecord)
	    // this is a general fall back
	    nextRecord = startPosition + requestSize;
	}
	if (nextRecord == 0 || nextRecord == startPosition) {
	    ret.setResumptionToken(null);
	} else {
	    ret.setResumptionToken(String.valueOf(nextRecord));
	}
	// additional check if all records have been returned
	int currentPosition = startPosition + requestSize;
	if (nextRecord > searchResults.getNumberOfRecordsMatched().intValue()
		|| currentPosition > searchResults.getNumberOfRecordsMatched().intValue()) {
	    // all records have been returned
	    ret.setResumptionToken(null);
	}
	if (!isMaxRecordsUnlimited() && currentPosition > recordsLimit) {
	    ret.setResumptionToken(null);
	}

	return ret;
    }

    /**
     * Sub connectors might implement this method to reduce (e.g. filtering out some results from the returned list)
     * 
     * @param ret
     */
    public void filterResults(ListRecordsResponse<OriginalMetadata> ret) {

    }

    OriginalMetadata objectToOriginalMetadata(Object object) {

	Node node;
	try {
	    if (object instanceof Node) {
		// it may be already a DOM node
		node = (Node) object;
	    } else {
		// it may be a JAXB object
		node = CommonContext.asDocument(object, true);
	    }

	    return createMetadata(node);

	} catch (Exception e) {
	    if (GSLoggerFactory.getLogger(CSWConnector.class).isWarnEnabled()) {
		GSLoggerFactory.getLogger(CSWConnector.class).warn("Can't add element to results", e);
	    }
	}

	return null;

    }

    void toOriginalMetadataFromAnies(ListRecordsResponse<OriginalMetadata> ret, Optional<List<Object>> optional) {

	optional.ifPresent(
		anies -> anies.stream().map(object -> objectToOriginalMetadata(object)).filter(Objects::nonNull).forEach(ret::addRecord));

    }

    OriginalMetadata recordToOriginalMetadata(RecordType record) {
	try {

	    Node node = CommonContext.asDocument(record, true);

	    return createMetadata(node);

	} catch (Exception e) {
	    if (GSLoggerFactory.getLogger(CSWConnector.class).isWarnEnabled())
		GSLoggerFactory.getLogger(CSWConnector.class).warn("Can't add element to results");
	}

	return null;
    }

    void toOriginalMetadataFromAbstractRecords(ListRecordsResponse<OriginalMetadata> ret,
	    Optional<List<JAXBElement<? extends AbstractRecordType>>> optional) {

	optional.ifPresent(abstractRecords -> abstractRecords.stream().map(elem ->

	recordToOriginalMetadata((RecordType) elem.getValue())

	).filter(Objects::nonNull).forEach(ret::addRecord));
    }

    ListRecordsResponse<OriginalMetadata> toOriginalMetadata(SearchResultsType searchResults) {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	toOriginalMetadataFromAbstractRecords(ret, Optional.of(searchResults.getAbstractRecords()));

	toOriginalMetadataFromAnies(ret, Optional.of(searchResults.getAnies()));

	return ret;
    }

    SearchResultsType toSearchResults(File file) throws GSException {
	Exception e1 = null;

	File tmpFile = null;
	try {

	    tmpFile = fixGetRecordsResponse(file);

	    GetRecordsResponse getRecordsResponse = unmarshal(tmpFile);

	    return getRecordsResponse.getSearchResults();

	} catch (UnmarshalException e) {

	    handleGetRecordsUnmarshallingException(tmpFile, e);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(CSWConnector.class).error("Error handling GetRecords response stream", e);
	    e1 = e;
	} finally {
	    if (file != null && file.exists()) {
		file.delete();
	    }
	    if (tmpFile != null && tmpFile.exists()) {
		tmpFile.delete();
	    }
	}

	throw GSException.createException( //
		getClass(), //
		"Error handling GetRecords response stream", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		CSW_EXCEPTIO_REPORT_ERROR, e1);
    }

    void handleGetRecordsUnmarshallingException(File file, UnmarshalException originalException) throws GSException {

	if (GSLoggerFactory.getLogger(CSWConnector.class).isDebugEnabled())
	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Looking for exception report");

	try {
	    ExceptionReport exceptionReport = CommonContext.unmarshal(file, ExceptionReport.class);
	    String exceptionCode = exceptionReport.getException().get(0).getExceptionCode();
	    String exceptionMessage = exceptionReport.getException().get(0).getExceptionText().get(0);

	    if (GSLoggerFactory.getLogger(CSWConnector.class).isDebugEnabled()) {
		GSLoggerFactory.getLogger(CSWConnector.class).debug("Found Exception report - code: {} - message {}", exceptionCode,
			exceptionMessage);
	    }

	} catch (Exception e) {
	    if (GSLoggerFactory.getLogger(CSWConnector.class).isDebugEnabled()) {
		GSLoggerFactory.getLogger(CSWConnector.class).debug("No exception report found, original exception following",
			originalException);
	    }

	    throw GSException.createException( //
		    getClass(), //
		    "Exception unmarshalling GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GETRECORDS_UNMARSHALL_ERROR, originalException);
	}

	throw GSException.createException( //
		getClass(), //
		"Exception report from server", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		CSW_EXCEPTIO_REPORT_ERROR);
    }

    protected GetRecordsResponse unmarshal(File file) throws JAXBException {
	return CommonContext.unmarshal(file, GetRecordsResponse.class);
    }

    /**
     * It does a simple copy without fixing anything. This method can be overridden by sub connectors needing to fix the
     * CSW service response
     *
     * @param baos
     * @return
     */
    protected File fixGetRecordsResponse(File file1) throws GSException {
	try {
	    File file2 = File.createTempFile("CSWConnectorFix", ".xml");
	    FileInputStream fis = new FileInputStream(file1);
	    FileOutputStream fos = new FileOutputStream(file2);
	    IOUtils.copy(fis, fos);
	    fis.close();
	    fos.close();
	    file1.delete();
	    return file2;
	} catch (IOException e) {
	    throw new GSException();
	}
    }

    protected GetRecords createGetRecords(Integer startPosition, Integer requestSize) throws GSException {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(OUTPUT_FORMAT);
	getRecords.setResultType(ResultType.RESULTS);
	if (requestSize == null) {
	    requestSize = getRequestSize();
	}
	getRecords.setMaxRecords(new BigInteger("" + requestSize));

	getRecords.setOutputSchema(getRequestedMetadataSchema());

	ElementSetName elementSetName = new ElementSetName();
	elementSetName.setValue(ElementSetType.FULL);

	QueryType queryType = new QueryType();
	queryType.setElementSetName(elementSetName);

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);
	getRecords.setAbstractQuery(query);

	QName requestedType = supportedTypesBySchema.get(getSelectedSchema());
	if (requestedType != null) {
	    queryType.getTypeNames().add(requestedType);
	}

	getRecords.setStartPosition(new BigInteger("" + startPosition));

	return getRecords;
    }

    protected String getConstraintLanguageParameter() {
	return CONSTRAINT_LAGUAGEPARAMETER;
    }

    private Optional<Operation> findGetRecordsOperation(Capabilities capabilities) {

	OperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();
	List<Operation> operations = operationsMetadata.getOperation();

	if (GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled())
	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Found {} operations", operations.size());

	return operations.stream().filter(operation -> {
	    String name = operation.getName();

	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Operation name: {}", name);

	    return GET_RECORDS_OPERATION_NAME.equals(name);
	}).findFirst();

    }

    CSWOperationParser getCSWOperationParser(Operation o) {
	return new CSWOperationParser(o, this);
    }

    void readCapabilitiesDocument() throws GSException {

	if (supportedOutputSchemas != null && !supportedOutputSchemas.isEmpty()) {

	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Capabilities already parsed, returning");

	    return;

	}

	GSLoggerFactory.getLogger(CSWConnector.class).trace("SupportedSchemas is null, getting capabilities document");

	Capabilities cap = getCapabilities(getSourceURL(), false);

	GSLoggerFactory.getLogger(CSWConnector.class).trace("Completed GetCapabilities");

	GSLoggerFactory.getLogger(CSWConnector.class).trace("Found not null capabilities, looking for {} operation now",
		GET_RECORDS_OPERATION_NAME);

	Optional<Operation> optional = findGetRecordsOperation(cap);

	optional.ifPresent(operation -> {
	    CSWOperationParser operationParser = getCSWOperationParser(operation);

	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Parsing operation");
	    operationParser.parse();
	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Completed parsing operation");

	    getRecordsURLPOSTSOAP = operationParser.getRecordsURLPOSTSOAP();
	    getRecordsURLPOSTXML = operationParser.getRecordsURLPOSTXML();
	    getRecordsURLGET = operationParser.getRecordsURLGET();
	    supportedOutputSchemas = operationParser.getSupportedOutputSchemas();
	    supportedTypesBySchema = operationParser.getSupportedTypesBySchema();

	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Operation successfully parsed");

	});

	if (supportedOutputSchemas == null || supportedOutputSchemas.isEmpty()) {

	    GSLoggerFactory.getLogger(CSWConnector.class).error("No Supported Schemas where found in {} with url {}", getLabel(),
		    sourceURL);

	    throw GSException.createException( //
		    getClass(), //
		    "Unable to extract information from CSW GetCapabilities", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_CONNECTOR_CAPABILITIES_EXTRACTION_ERROR);
	}

    }

    protected OriginalMetadata createMetadata(Node node) throws GSException {

	OriginalMetadata metadataRecord = new OriginalMetadata();

	// ------------------------------------
	//
	// set the scheme URI
	//
	metadataRecord.setSchemeURI(getReturnedMetadataSchema());

	// ------------------------------------
	//
	// set the metadata
	//
	String metadata = asString(node);

	metadataRecord.setMetadata(metadata);

	return metadataRecord;
    }

    public void setSelectedSchema(String selectedSchema) {
	this.selectedSchema = selectedSchema;
    }

    /**
     * Returns the selected schema if previously specified by the user, or a schema from the supported output schemata,
     * in this order: ISO
     * 19115-2, ISO 19115-1, Dublin Core. Other schemata should be selected by the client.
     *
     * @return
     * @throws GSException
     */
    String getSelectedSchema() throws GSException {

	if (selectedSchema != null) {
	    return selectedSchema;
	}

	readCapabilitiesDocument();

	String output = null;
	if (supportedOutputSchemas.contains(CommonNameSpaceContext.GMI_NS_URI)) {
	    output = CommonNameSpaceContext.GMI_NS_URI;
	} else if (supportedOutputSchemas.contains(CommonNameSpaceContext.GMD_NS_URI)) {
	    output = CommonNameSpaceContext.GMD_NS_URI;
	} else {
	    output = CommonNameSpaceContext.CSW_NS_URI;
	}

	return output;
    }

    /**
     * Returns the schema used in requests to the remote CSW. Subclasses may change this.
     *
     * @return
     * @throws GSException
     */
    protected String getRequestedMetadataSchema() throws GSException {
	return getSelectedSchema();
    }

    /**
     * Returns the schema of resources returned by the remote CSW. Subclasses may change this.
     *
     * @return
     * @throws GSException
     */
    protected String getReturnedMetadataSchema() throws GSException {
	return getSelectedSchema();
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
		    CSW_CONNECTOR_METADATA_NODE_AS_STRING_ERROR, //
		    e);
	}
    }

    /**
     * Returns the default Get Records request size (100)
     *
     * @return
     */
    @JsonIgnore
    public int getRequestSize() {

	return getPageSizeOption().getValue();

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	readCapabilitiesDocument();

	return supportedOutputSchemas;
    }

    InputStream doExecGetRequest(URI uri, boolean silent) throws IOException {

	HttpGet httpGet = new HttpGet(uri);

	HttpResponse response = new HttpRequestExecutor().execute(httpGet);

	if (!silent && GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled()) {

	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Response code {}", response.getStatusLine().getStatusCode());
	}

	InputStream content = response.getEntity().getContent();

	if (!silent && GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled()) {

	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Contant loaded");
	}

	return content;
    }

    Capabilities doUnmarshallCapabiliesStream(InputStream content, boolean silent) throws JAXBException {

	if (!silent && GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled())
	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Unmarshalling");

	return CommonContext.unmarshal(content, Capabilities.class);

    }

    @JsonIgnore
    public Capabilities getCapabilities(String sourceURL, boolean silent) throws GSException {

	GSLoggerFactory.getLogger(CSWConnector.class).trace("Retrieving Capabilities Document");

	if (capabilitiesDocument != null) {
	    GSLoggerFactory.getLogger(CSWConnector.class).trace("Returning cached capabilities document");
	    return capabilitiesDocument;
	}

	List<URI> urlsList = getCapabilitiesURLs(sourceURL);

	for (URI uri : urlsList) {

	    if (!silent && GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled()) {
		GSLoggerFactory.getLogger(CSWConnector.class).trace("Getting capabilities with URI: {}", uri);
	    }

	    try {

		InputStream content = doExecGetRequest(uri, silent);

		capabilitiesDocument = doUnmarshallCapabiliesStream(content, silent);

		return capabilitiesDocument;

	    } catch (IOException | JAXBException e) {
		if (!silent && GSLoggerFactory.getLogger(CSWConnector.class).isWarnEnabled())
		    GSLoggerFactory.getLogger(CSWConnector.class).warn("Can't use {} to download capabilities document", uri, e);
	    }

	}

	GSLoggerFactory.getLogger(CSWConnector.class).error("Unable to get Capabilities document");

	throw GSException.createException( //
		getClass(), //
		"Unable to download Capabilities Document", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		CSW_CONNECTOR_CAPABILITIES_EXTRACTION_ERROR);
    }

    private List<URI> getCapabilitiesURLs(String sourceURL) {

	List<URI> ret = new ArrayList<>();

	sourceURL = normalizeURL(sourceURL); // to prepare the URL to accept additional parameters
	try {
	    ret.add(new URL(sourceURL + "SERVICE=CSW&REQUEST=GetCapabilities&ACCEPTVERSIONS=" + getAcceptVersions()).toURI());
	    ret.add(new URL(sourceURL + "SERVICE=CSW&REQUEST=GetCapabilities&ACCEPTVERSION=" + getAcceptVersions()).toURI());
	    ret.add(new URL(sourceURL + "service=CSW&request=GetCapabilities&acceptversions=" + getAcceptVersions()).toURI());
	    ret.add(new URL(sourceURL + "SERVICE=CSW&REQUEST=GetCapabilities&VERSION=" + getPreferredVersion()).toURI());
	    ret.add(new URL(sourceURL + "service=CSW&request=GetCapabilities&version=" + getPreferredVersion()).toURI());

	} catch (MalformedURLException | URISyntaxException e) {

	    GSLoggerFactory.getLogger(CSWConnector.class).debug("Can't create capabilities url with exception", e);
	}

	if (ret.isEmpty())
	    GSLoggerFactory.getLogger(CSWConnector.class).warn("Empty list of capabilities url with source url {}", sourceURL);

	return ret;
    }

    private String getPreferredVersion() {

	return PREFERRED_VERSION;
    }

    private String getAcceptVersions() {

	return ACCEPTED_VERSION;
    }

    @Override
    public boolean supports(Source source) {
	try {
	    Capabilities capabilities = getCapabilities(source.getEndpoint(), true);
	    if (capabilities != null) {
		return true;
	    }
	} catch (GSException e) {
	    if (GSLoggerFactory.getLogger(CSWConnector.class).isTraceEnabled())
		GSLoggerFactory.getLogger(CSWConnector.class).trace("Exception testing support of {} with endpoint {}", source.getLabel(),
			source.getEndpoint());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

	return false;
    }

    public String normalizeURL(String url) {
	if (url.endsWith("?") || url.endsWith("&")) {
	    return url;
	}

	return url.contains("?") ? url + "&" : url + "?";
    }

    /**
     * @param startIndex
     */
    public void setStartIndex(Integer startIndex) {

	this.startIndex = startIndex;
    }

}
