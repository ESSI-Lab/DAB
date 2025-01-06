package eu.essi_lab.profiler.csw.handler.discover;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordByIdResponse;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordsResponse;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.RequestStatusType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.csw._2_0_2.SearchResultsType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.jaxb.filter._1_1_0.SortByType;
import eu.essi_lab.jaxb.filter._1_1_0.SortOrderType;
import eu.essi_lab.jaxb.filter._1_1_0.SortPropertyType;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.csw.CSWProfiler;
import eu.essi_lab.profiler.csw.CSWRequestConverter;
import eu.essi_lab.profiler.csw.CSWRequestUtils;

/**
 * @author Fabrizio
 */
public class CSWResultSetFormatter extends DiscoveryResultSetFormatter<Element> {

    /**
     * The encoding name of {@link #CSW_FORMATTING_ENCODING}
     */
    public static final String CSW_FORMATTING_ENCODING_NAME = "csw-frm-encoding";
    /**
     * The encoding version of {@link #CSW_FORMATTING_ENCODING}
     */
    public static final String CSW_FORMATTING_ENCODING_VERSION = "2.0.2";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding CSW_FORMATTING_ENCODING = new FormattingEncoding();
    private static final String CSW_RESULT_SET_FORMATTER_ERROR = "CSW_RESULT_SET_FORMATTER_ERROR";

    /**
     * 
     */
    private static final String CSS_VIEW_IDENTIFIER = "emod-pace";

    static {
	CSW_FORMATTING_ENCODING.setEncoding(CSW_FORMATTING_ENCODING_NAME);
	CSW_FORMATTING_ENCODING.setEncodingVersion(CSW_FORMATTING_ENCODING_VERSION);
	CSW_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    /**
     * http://reference1.mapinfo.com/software/spatial_server/english/1_0/csw/postget/postgetgetrecords.html Ignored
     * parameters: HOPCOUNT
     * RESPONSEHANDLER DISTRIBUTEDSEARCH
     */
    @Override
    public Response format(DiscoveryMessage message, ResultSet<Element> mappedResultSet) throws GSException {

	WebRequest webRequest = message.getWebRequest();

	try {

	    GetRecords getRecords = null;

	    boolean getRecordsFromGET = CSWRequestUtils.isGetRecordsFromGET(webRequest);
	    if (getRecordsFromGET) {

		CSWRequestConverter converter = new CSWRequestConverter();
		getRecords = converter.convert(webRequest);

	    } else if (CSWRequestUtils.isGetRecordsFromPOST(webRequest)) {

		getRecords = CSWRequestUtils.getGetRecordFromPOST(webRequest);
	    }

	    List<Element> resultsList = mappedResultSet.getResultsList();

	    // ---------------------
	    //
	    // GetRecordById
	    //
	    if (getRecords == null) {

		return formatGetRecordById(resultsList, message.getView());
	    }

	    // ---------------------
	    //
	    // GetRecords
	    //

	    DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    GetRecordsResponse recordsResponse = new GetRecordsResponse();

	    // set the status
	    RequestStatusType statusType = new RequestStatusType();
	    statusType.setTimestamp(XMLGregorianCalendarUtils.createGregorianCalendar());
	    recordsResponse.setSearchStatus(statusType);

	    // creates the search results
	    SearchResultsType searchResultsType = new SearchResultsType();

	    // in case of errors, the error elements are added to the response
	    List<Element> errorElements = null;

	    if (!mappedResultSet.getException().getErrorInfoList().isEmpty() || //
		    !message.getException().getErrorInfoList().isEmpty()) { //

		errorElements = handleErrorResponse(//
			message, //
			mappedResultSet, //
			builder);
	    }

	    int matched = mappedResultSet.getCountResponse().getCount();

	    searchResultsType.setNumberOfRecordsMatched(new BigInteger(String.valueOf(matched)));
	    searchResultsType.setRecordSchema(getRecords.getOutputSchema());

	    QueryType type = (QueryType) (getRecords.getAbstractQuery().getValue());

	    // element names or element set name
	    ElementSetType setType = null;

	    ElementSetName elementSetName = type.getElementSetName();
	    if (elementSetName != null) {
		setType = elementSetName.getValue();
	    }

	    // set the search results
	    searchResultsType.setElementSet(setType);
	    recordsResponse.setSearchResults(searchResultsType);

	    // ---------------------
	    //
	    // GetRecords HITS
	    //
	    if (getRecords.getResultType() == ResultType.HITS) {

		// set the search results attributes
		searchResultsType.setNextRecord(new BigInteger("0"));
		searchResultsType.setNumberOfRecordsReturned(new BigInteger("0"));

	    } else {
		// ---------------------
		//
		// GetRecords RESULTS
		//
		int start = getRecords.getStartPosition().intValue();
		int returned = mappedResultSet.getResultsList().size();
		int max = getRecords.getMaxRecords().intValue();
		int next = start + max > matched ? 0 : (start + returned);

		// set the search results attributes
		searchResultsType.setNextRecord(new BigInteger(String.valueOf(next)));
		searchResultsType.setNumberOfRecordsReturned(new BigInteger(String.valueOf(returned)));

		// ---------------------
		//
		// Sorting
		//
		SortByType sortBy = type.getSortBy();
		if (sortBy != null) {
		    sort(sortBy, resultsList);
		}

		// ---------------------
		//
		// Adding results
		//
		addResults(resultsList, errorElements, searchResultsType);
	    }

	    return buildResponse(recordsResponse);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Error formatting result in CSW", ex);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_RESULT_SET_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return CSW_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private Response formatGetRecordById(List<Element> resultsList, Optional<View> view) throws Exception {

	GetRecordByIdResponse response = new GetRecordByIdResponse();

	for (Element result : resultsList) {

	    response.getAnies().add(result);
	}

	boolean insertXSLT = view.isPresent() && view.get().getId().equals(CSS_VIEW_IDENTIFIER);

	return buildResponse(response, insertXSLT);
    }

    /**
     * @param searchResultsType
     * @param message
     * @param mappedResultSet
     * @param builder
     * @param recordsResponse
     * @return
     * @throws Exception
     */
    private List<Element> handleErrorResponse(//
	    RequestMessage message, //
	    MessageResponse<Element, CountSet> mappedResultSet, //
	    DocumentBuilder builder) throws Exception {

	ArrayList<Element> out = new ArrayList<>();

	mappedResultSet.getException().getErrorInfoList().addAll(message.getException().getErrorInfoList());

	List<ErrorInfo> errorInfoList = mappedResultSet.getException().getErrorInfoList();

	for (ErrorInfo errorInfo : errorInfoList) {

	    ValidationMessage validationMessage = new ValidationMessage();
	    validationMessage.setError(errorInfo.getErrorDescription());
	    validationMessage.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());

	    ExceptionReport report = CSWProfiler.createExceptionReport(validationMessage);

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    CommonContext.createMarshaller(false).marshal(report, outputStream);

	    Document doc = builder.parse(new ByteArrayInputStream(outputStream.toString("UTF-8").getBytes(StandardCharsets.UTF_8)));

	    out.add(doc.getDocumentElement());
	}

	return out;
    }

    private void sort(SortByType sortBy, List<Element> resultsList) {

	List<SortPropertyType> sortProperty = sortBy.getSortProperty();
	if (!sortProperty.isEmpty()) {

	    SortPropertyType sortPropertyType = sortProperty.get(0);

	    PropertyNameType propertyName = sortPropertyType.getPropertyName();

	    String name = propertyName.getContent().get(0).toString();
	    SortOrderType sortOrder = sortPropertyType.getSortOrder();

	    resultsList.sort(

		    (resource1, resource2) -> {

			try {
			    XMLDocumentReader reader1 = new XMLDocumentReader(resource1.getOwnerDocument());
			    reader1.setNamespaceContext(new CommonNameSpaceContext());
			    XMLDocumentReader reader2 = new XMLDocumentReader(resource2.getOwnerDocument());
			    reader2.setNamespaceContext(new CommonNameSpaceContext());

			    Node[] nodes1 = reader1.evaluateNodes("//" + name);
			    Node[] nodes2 = reader2.evaluateNodes("//" + name);

			    if (nodes1.length == 0 && nodes2.length == 0) {
				return 0;
			    }

			    switch (sortOrder) {

			    case ASC:
			    case DESC:
			    default:
				if (nodes1.length == 0) {
				    return 1;
				}
				if (nodes2.length == 0) {
				    return -1;
				}
				break;
			    }

			    String c1 = nodes1[0].getTextContent().trim();
			    String c2 = nodes2[0].getTextContent().trim();

			    switch (sortOrder) {
			    case ASC:
				return c1.compareTo(c2);
			    case DESC:
				return c2.compareTo(c1);
			    }

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
			}

			return 0;
		    });
	}
    }

    /**
     * @param resultsList
     * @param errorElements
     * @param searchResultsType
     */
    private void addResults(//
	    List<Element> resultsList, //
	    List<Element> errorElements, //
	    SearchResultsType searchResultsType) {

	for (Element result : resultsList) {

	    searchResultsType.getAnies().add(result);
	}

	if (errorElements != null) {
	    
	    errorElements.forEach(el -> searchResultsType.getAnies().add(el));
	}
    }

    private Response buildResponse(Object response) throws Exception {

	return buildResponse(response, false);
    }

    private Response buildResponse(Object response, boolean insertXSLtag) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	String stringValue = CommonContext.asString(response, false);

	String type = MediaType.APPLICATION_XML;

	if (insertXSLtag) {

	    stringValue = "<?xml-stylesheet type=\"text/xsl\" href=\"/gs-service/iso-xslt/style.xsl\">\n" + stringValue;
	}

	builder = builder.entity(stringValue);
	builder = builder.type(type);

	return builder.build();
    }
}
