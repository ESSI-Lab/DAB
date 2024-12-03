package eu.essi_lab.profiler.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter.InspectionStrategy;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestTransformer;
import eu.essi_lab.profiler.csw.handler.discover.CSWResultSetFormatter;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWDescribeRecordHandler;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWGetCapabilitiesHandler;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWValidateGetRecordsHandler;
import eu.essi_lab.profiler.csw.profile.CSWProfile;

/**
 * @author Fabrizio
 */
public class CSWProfiler<CSWPS extends CSWProfilerSetting> extends Profiler<CSWProfilerSetting> {

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    protected static final String CSW_PROFILER_ON_DISCOVER_REQUEST_VALIDATED_ERROR = "CSW_PROFILER_ON_DISCOVER_REQUEST_VALIDATED_ERROR";

    static {

	SUPPORTED_VERSIONS.add("2.0.2");

	// as from 10.6.4.3
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    private DiscoveryHandler<Element> discoveryHandler;

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	discoveryHandler = new DiscoveryHandler<Element>();
	discoveryHandler.setRequestTransformer(new CSWRequestTransformer());
	discoveryHandler.setMessageResponseFormatter(new CSWResultSetFormatter());

	//
	// GetRecord (GET and POST) and GetRecordById (GET)
	//

	CSWRequestFilter getRecordFilter = new CSWRequestFilter();

	getRecordFilter.addQueryCondition("GetRecord", InspectionStrategy.IGNORE_CASE_LIKE_MATCH);
	getRecordFilter.addQueryCondition("GetRecordById", InspectionStrategy.IGNORE_CASE_LIKE_MATCH);

	getRecordFilter.addResultTypeCondition(ResultType.RESULTS);
	getRecordFilter.addResultTypeCondition(ResultType.HITS);

	selector.register(getRecordFilter, discoveryHandler);

	//
	// DescribeRecord
	//

	selector.register(//
		new CSWRequestFilter("DescribeRecord", InspectionStrategy.IGNORE_CASE_LIKE_MATCH), //
		new CSWDescribeRecordHandler());

	//
	// GetCapabilities
	//

	selector.register(//
		new CSWRequestFilter("GetCapabilities", InspectionStrategy.IGNORE_CASE_LIKE_MATCH), //
		new CSWGetCapabilitiesHandler());

	//
	// Validate
	//

	selector.register(//
		new CSWRequestFilter(ResultType.VALIDATE), //
		new CSWValidateGetRecordsHandler()); //

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return onValidationFailed(null, vm);
    }

    /**
     * @param message
     * @return
     */
    public static ExceptionReport createExceptionReport(ValidationMessage message) {

	ExceptionReport report = new ExceptionReport();
	report.setVersion("1.2.0");
	ExceptionType exceptionType = new ExceptionType();
	exceptionType.setExceptionCode(message.getErrorCode());
	exceptionType.getExceptionText().add(message.getError());
	if (message.getLocator() != null) {
	    exceptionType.setLocator(message.getLocator());
	}
	report.getException().add(exceptionType);
	return report;
    }

    @Override
    protected void onRequestValidated(WebRequest webRequest, WebRequestValidator validator, RequestType type) throws GSException {

	if (type != RequestType.DISCOVERY) {
	    return;
	}

	String outputSchema = null;
	ElementSetType setType = null;
	List<QName> elementNames = null;

	try {

	    GetRecords getRecords = CSWRequestUtils.getGetRecordFromPOST(webRequest);

	    // --------------------------------------------------------------------------
	    //
	    // in case of HITS there is nothing to map, so the mapping process is skipped
	    //
	    if (getRecords != null && getRecords.getResultType() == ResultType.HITS) {
		discoveryHandler.setMessageResponseMapper(createDummyResultSetMapper());
		return;
	    }

	    // --------------------------------------------------------------------------
	    //
	    // GetRecordById
	    //
	    if (getRecords == null) {
		if (webRequest.isGetRequest()) {

		    KeyValueParser parser = new KeyValueParser(webRequest.getURLDecodedQueryString());
		    outputSchema = parser.getValue("outputSchema", true);
		    String set = parser.getValue("ElementSetName", true);
		    if (set != null) {
			setType = ElementSetType.fromValue(set);
		    }
		} else {

		    GetRecordById grBydId = CommonContext.unmarshal(webRequest.getBodyStream().clone(), GetRecordById.class);
		    outputSchema = grBydId.getOutputSchema();
		    ElementSetName elementSetName = grBydId.getElementSetName();
		    if (elementSetName != null) {
			setType = elementSetName.getValue();
		    }
		}

		if (setType == null) {
		    setType = ElementSetType.SUMMARY;
		}

	    } else {
		// --------------------------------------------------------------------------
		//
		// GetRecords
		//
		outputSchema = getRecords.getOutputSchema();

		QueryType queryType = (QueryType) getRecords.getAbstractQuery().getValue();
		ElementSetName setName = queryType.getElementSetName();

		if (setName != null) {
		    setType = setName.getValue();
		} else {
		    elementNames = queryType.getElementNames();
		}
	    }

	    // default output schema
	    if (outputSchema == null) {
		outputSchema = CommonNameSpaceContext.CSW_NS_URI;
	    }
	} catch (Exception ex) {

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_PROFILER_ON_DISCOVER_REQUEST_VALIDATED_ERROR);
	}

	// -------------------------------------------------------------------------------
	//
	// get the mapper from a profile selected according to the requested output schema
	//
	List<CSWProfile> profiles = CSWProfile.getAvailableProfiles();
	for (CSWProfile cswProfile : profiles) {
	    if (cswProfile.supportsOutputSchema(outputSchema)) {

		DiscoveryResultSetMapper<Element> mapper = cswProfile.getResultSetMapper(outputSchema, setType, elementNames);
		discoveryHandler.setMessageResponseMapper(mapper);

		GSLoggerFactory.getLogger(getClass()).info("Selected profile: " + cswProfile.getClass().getName());
		GSLoggerFactory.getLogger(getClass()).info("Selected mapper: " + mapper.getClass().getName());

		return;
	    }
	}
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	ExceptionReport report = createExceptionReport(message);

	String string = "";
	try {

	    Marshaller marshaller = CommonContext.createMarshaller(false);
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
		    "http://www.opengis.net/ows http://schemas.opengis.net/ows/1.0.0/owsAll.xsd");

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    marshaller.marshal(report, outputStream);

	    string = outputStream.toString("UTF-8");

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(string).build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getURLDecodedQueryString());
	String req = parser.getValue("request", true);
	ValidationMessage message = new ValidationMessage();

	if (req == null) {

	    message.setError("Missing mandatory request parameter");
	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.toString());
	    message.setLocator("request");
	} else {
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Invalid request parameter");
	    message.setLocator("request");
	}

	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected CSWProfilerSetting initSetting() {

	return new CSWProfilerSetting();
    }

    private DiscoveryResultSetMapper<Element> createDummyResultSetMapper() {

	return new DiscoveryResultSetMapper<Element>() {

	    @Override
	    public ResultSet<Element> map(DiscoveryMessage message, ResultSet<GSResource> resultSet) throws GSException {

		ResultSet<Element> mappedResSet = new ResultSet<Element>(resultSet);
		mappedResSet.setResultsList(new ArrayList<Element>());

		return mappedResSet;
	    }

	    @Override
	    public MappingSchema getMappingSchema() {
		return null;
	    }

	    @Override
	    public Provider getProvider() {
		return null;
	    }

	    @Override
	    public Element map(DiscoveryMessage message, GSResource resource) throws GSException {
		return null;
	    }
	};
    }
}
