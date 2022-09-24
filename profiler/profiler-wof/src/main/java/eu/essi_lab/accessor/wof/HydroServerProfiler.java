package eu.essi_lab.accessor.wof;

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
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;

import org.cuahsi.waterml._1.NoteType;
import org.cuahsi.waterml._1.QueryInfoType;
import org.cuahsi.waterml._1.QueryInfoType.Criteria;
import org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter;
import org.cuahsi.waterml._1.SiteInfoResponseType.Site;
import org.cuahsi.waterml._1.VariableInfoType;

import eu.essi_lab.accessor.wof.access.GetValuesObjectRequestFilter;
import eu.essi_lab.accessor.wof.access.GetValuesObjectResultSetMapper;
import eu.essi_lab.accessor.wof.access.GetValuesObjectTransformer;
import eu.essi_lab.accessor.wof.access.GetValuesRequestFilter;
import eu.essi_lab.accessor.wof.access.GetValuesResultSetFormatter;
import eu.essi_lab.accessor.wof.access.GetValuesResultSetMapper;
import eu.essi_lab.accessor.wof.access.GetValuesTransformer;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoObjectRequestFilter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoObjectResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoObjectTransformer;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoRequestFilter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoResultSetMapper;
import eu.essi_lab.accessor.wof.discovery.sites.GetSiteInfoTransformer;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxObjectRequestFilterNoSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxObjectRequestFilterWithSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxObjectResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxRequestFilterNoSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxRequestFilterWithSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxTransformerNoSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesByBoxTransformerWithSeries;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesObjectHandler;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesObjectRequest;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesObjectResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesObjectTransformer;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesRequest;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesResultSetMapper;
import eu.essi_lab.accessor.wof.discovery.sites.GetSitesTransformer;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoObjectRequest;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoObjectResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoObjectTransformer;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoRequest;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariableInfoTransformer;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesObjectRequest;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesObjectResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesObjectTransformer;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesRequest;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesResultSetFormatter;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesResultSetMapper;
import eu.essi_lab.accessor.wof.discovery.variables.GetVariablesTransformer;
import eu.essi_lab.accessor.wof.wsdl.HydroServerWSDLHandler;
import eu.essi_lab.accessor.wof.wsdl.WSDLRequestFilter;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * Profiler implementing CUAHSI HIS Server protocol
 * 
 * @author boldrini
 */
public class HydroServerProfiler extends Profiler {

    /**
     * The profiler type
     */
    private static final String HYDRO_SERVER_PROFILER_TYPE = "HYDRO-SERVER";

    public static final ProfilerSetting HYDRO_SERVER_INFO = new ProfilerSetting();
    static {
	HYDRO_SERVER_INFO.setServiceName("CUAHSI Hydro Server");
	HYDRO_SERVER_INFO.setServiceType(HYDRO_SERVER_PROFILER_TYPE);
	HYDRO_SERVER_INFO.setServicePath("cuahsi_1_1.asmx");
	HYDRO_SERVER_INFO.setServiceVersion("1.1");
    }

    protected static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    protected static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    private static final String KEY_REQUEST = "request";

    static {

	SUPPORTED_VERSIONS.add("1.1");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	HydroServerWSDLHandler hydroServerWSDLHandler = new HydroServerWSDLHandler();
	selector.register(new WSDLRequestFilter(), hydroServerWSDLHandler);

	////////////////////
	// VARIABLES
	////////////////////

	DiscoveryHandler<VariableInfoType> getVariablesHandler = new DiscoveryHandler<>();
	getVariablesHandler.setRequestTransformer(new GetVariablesTransformer());
	getVariablesHandler.setMessageResponseMapper(new GetVariablesResultSetMapper());
	getVariablesHandler.setMessageResponseFormatter(new GetVariablesResultSetFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetVariablesRequest(request);
	    }
	}, getVariablesHandler); // GetVariables

	DiscoveryHandler<VariableInfoType> getVariablesObjectHandler = new DiscoveryHandler<>();
	getVariablesObjectHandler.setRequestTransformer(new GetVariablesObjectTransformer());
	getVariablesObjectHandler.setMessageResponseMapper(new GetVariablesResultSetMapper());
	getVariablesObjectHandler.setMessageResponseFormatter(new GetVariablesObjectResultSetFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetVariablesObjectRequest(request);
	    }
	}, getVariablesObjectHandler); // GetVariablesObject

	DiscoveryHandler<VariableInfoType> getVariableInfoHandler = new DiscoveryHandler<>();
	getVariableInfoHandler.setRequestTransformer(new GetVariableInfoTransformer());
	getVariableInfoHandler.setMessageResponseMapper(new GetVariablesResultSetMapper());
	getVariableInfoHandler.setMessageResponseFormatter(new GetVariableInfoResultSetFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetVariableInfoRequest(request);
	    }
	}, getVariableInfoHandler); // GetVariableInfo

	DiscoveryHandler<VariableInfoType> getVariableInfoObjectHandler = new DiscoveryHandler<>();
	getVariableInfoObjectHandler.setRequestTransformer(new GetVariableInfoObjectTransformer());
	getVariableInfoObjectHandler.setMessageResponseMapper(new GetVariablesResultSetMapper());
	getVariableInfoObjectHandler.setMessageResponseFormatter(new GetVariableInfoObjectResultSetFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetVariableInfoObjectRequest(request);
	    }
	}, getVariableInfoObjectHandler); // GetVariableInfoObject

	////////////////////
	// SITES
	////////////////////
	DiscoveryHandler<Site> getSitesHandler = new DiscoveryHandler<>();
	getSitesHandler.setRequestTransformer(new GetSitesTransformer());
	getSitesHandler.setMessageResponseMapper(new GetSitesResultSetMapper());
	getSitesHandler.setMessageResponseFormatter(new GetSitesResultSetFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetSitesRequest(request);
	    }
	}, getSitesHandler); // GetSites

	boolean useObjects = false;
	WebRequestHandler getSitesObjectHandler;
	if (useObjects) {
	    DiscoveryHandler<Site> sitesObjectHandler = new DiscoveryHandler<>();
	    sitesObjectHandler.setRequestTransformer(new GetSitesObjectTransformer());
	    sitesObjectHandler.setMessageResponseMapper(new GetSitesResultSetMapper());
	    sitesObjectHandler.setMessageResponseFormatter(new GetSitesObjectResultSetFormatter());
	    getSitesObjectHandler = sitesObjectHandler;
	} else {
	    getSitesObjectHandler = new GetSitesObjectHandler();
	}

	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetSitesObjectRequest(request);
	    }
	}, getSitesObjectHandler); // GetSitesObject

	////////////////////
	// SITES BY BOX
	////////////////////

	// with IncludeSeries OFF
	DiscoveryHandler<Site> getSitesByBoxNoSeriesObjectHandler = new DiscoveryHandler<>();
	getSitesByBoxNoSeriesObjectHandler.setRequestTransformer(new GetSitesByBoxTransformerNoSeries(true));
	getSitesByBoxNoSeriesObjectHandler.setMessageResponseMapper(new GetSitesResultSetMapper());
	getSitesByBoxNoSeriesObjectHandler.setMessageResponseFormatter(new GetSitesByBoxObjectResultSetFormatter());
	selector.register(new GetSitesByBoxObjectRequestFilterNoSeries(), getSitesByBoxNoSeriesObjectHandler); // GetSitesByBoxObject

	// with IncludeSeries ON
	DiscoveryHandler<Site> getSitesByBoxWithSeriesObjectHandler = new DiscoveryHandler<>();
	getSitesByBoxWithSeriesObjectHandler.setRequestTransformer(new GetSitesByBoxTransformerWithSeries(true));
	getSitesByBoxWithSeriesObjectHandler.setMessageResponseMapper(new GetSiteInfoResultSetMapper());
	getSitesByBoxWithSeriesObjectHandler.setMessageResponseFormatter(new GetSitesByBoxObjectResultSetFormatter());
	selector.register(new GetSitesByBoxObjectRequestFilterWithSeries(), getSitesByBoxWithSeriesObjectHandler); // GetSitesByBoxObject

	// with IncludeSeries OFF
	DiscoveryHandler<Site> getSitesByBoxNoSeriesHandler = new DiscoveryHandler<>();
	getSitesByBoxNoSeriesHandler.setRequestTransformer(new GetSitesByBoxTransformerNoSeries(false));
	getSitesByBoxNoSeriesHandler.setMessageResponseMapper(new GetSitesResultSetMapper());
	getSitesByBoxNoSeriesHandler.setMessageResponseFormatter(new GetSitesByBoxResultSetFormatter());
	selector.register(new GetSitesByBoxRequestFilterNoSeries(), getSitesByBoxNoSeriesHandler); // GetSitesByBox

	// with IncludeSeries ON
	DiscoveryHandler<Site> getSitesByBoxWithSeriesHandler = new DiscoveryHandler<>();
	getSitesByBoxWithSeriesHandler.setRequestTransformer(new GetSitesByBoxTransformerWithSeries(false));
	getSitesByBoxWithSeriesHandler.setMessageResponseMapper(new GetSiteInfoResultSetMapper());
	getSitesByBoxWithSeriesHandler.setMessageResponseFormatter(new GetSitesByBoxResultSetFormatter());
	selector.register(new GetSitesByBoxRequestFilterWithSeries(), getSitesByBoxWithSeriesHandler); // GetSitesByBox

	////////////////////
	// SITE INFO
	////////////////////

	DiscoveryHandler<Site> getSiteInfoObjectHandler = new DiscoveryHandler<>();
	getSiteInfoObjectHandler.setRequestTransformer(new GetSiteInfoObjectTransformer());
	getSiteInfoObjectHandler.setMessageResponseMapper(new GetSiteInfoResultSetMapper());
	getSiteInfoObjectHandler.setMessageResponseFormatter(new GetSiteInfoObjectResultSetFormatter());
	selector.register(new GetSiteInfoObjectRequestFilter(), getSiteInfoObjectHandler); // GetSiteInfoObject

	DiscoveryHandler<Site> getSiteInfoHandler = new DiscoveryHandler<>();
	getSiteInfoHandler.setRequestTransformer(new GetSiteInfoTransformer());
	getSiteInfoHandler.setMessageResponseMapper(new GetSiteInfoResultSetMapper());
	getSiteInfoHandler.setMessageResponseFormatter(new GetSiteInfoResultSetFormatter());
	selector.register(new GetSiteInfoRequestFilter(), getSiteInfoHandler); // GetSiteInfo

	////////////////////
	// get values
	////////////////////

	AccessHandler<DataObject> getValuesHandler = new AccessHandler<>();
	getValuesHandler.setRequestTransformer(new GetValuesTransformer());
	getValuesHandler.setMessageResponseMapper(new GetValuesResultSetMapper());
	getValuesHandler.setMessageResponseFormatter(new GetValuesResultSetFormatter());
	selector.register(new GetValuesRequestFilter(), getValuesHandler); // GetValues

	AccessHandler<DataObject> getValuesObjectHandler = new AccessHandler<>();
	getValuesObjectHandler.setRequestTransformer(new GetValuesObjectTransformer());
	getValuesObjectHandler.setMessageResponseMapper(new GetValuesObjectResultSetMapper());
	getValuesObjectHandler.setMessageResponseFormatter(new GetValuesResultSetFormatter());
	selector.register(new GetValuesObjectRequestFilter(), getValuesObjectHandler); // GetValuesObject

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return onValidationFailed(null, vm);
    }

    public static final String ERROR_GET_VALUES_VARIABLE_NOT_FOUND_IN_SITE = "ERROR_GET_VALUES_VARIABLE_NOT_FOUND_IN_SITE";
    public static final String ERROR_GET_VALUES_OBJECT_VARIABLE_NOT_FOUND_IN_SITE = "ERROR_GET_VALUES_OBJECT_VARIABLE_NOT_FOUND_IN_SITE";
    public static final String ERROR_VARIABLE_NOT_FOUND = "ERROR_VARIABLE_NOT_FOUND";
    public static final String ERROR_SITE_NOT_FOUND = "ERROR_SITE_NOT_FOUND";

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage validationMessage) {

	String errorCode = validationMessage.getErrorCode();

	switch (errorCode) {
	case ERROR_GET_VALUES_VARIABLE_NOT_FOUND_IN_SITE:
	case ERROR_GET_VALUES_OBJECT_VARIABLE_NOT_FOUND_IN_SITE:
	    try {
		return Response.status(200).type(MediaType.TEXT_XML).entity(validationMessage.getError()).build();
	    } catch (Exception e) {
		e.printStackTrace();
		return error(e.getMessage());
	    }
	case ERROR_VARIABLE_NOT_FOUND:
	case ERROR_SITE_NOT_FOUND:
	default:
	    String message = validationMessage.getError();
	    return error(message);
	}

    }

    private Response error(String message) {
	return Response.status(500).type(MediaType.TEXT_XML).entity(
		"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><soap:Fault><faultcode>soap:Client</faultcode><faultstring>"
			+ message + "</faultstring><detail /></soap:Fault></soap:Body></soap:Envelope>")
		.build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String req = parser.getValue(KEY_REQUEST, true);
	ValidationMessage message = new ValidationMessage();

	if (req == null) {

	    message.setError("Missing mandatory request parameter");
	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.toString());
	    message.setLocator(KEY_REQUEST);
	} else {
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Invalid request parameter");
	    message.setLocator(KEY_REQUEST);
	}

	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected ProfilerSetting initSetting() {

	return HYDRO_SERVER_INFO;
    }

    public static QueryInfoType getQueryInfo(String url, WOFRequest request) {
	String methodCalled = request.getActualRequestName();
	Set<Entry<String, String>> parameters = request.getActualParameters();
	List<String> pars = new ArrayList<>();
	for (Entry<String, String> entry : parameters) {
	    if (!entry.getKey().toLowerCase().equals("request")) {
		pars.add(entry.getKey());
		pars.add(entry.getValue());
	    }
	}
	return getQueryInfo(url, methodCalled, "Discovery and Access Broker", pars.toArray(new String[] {}));
    }

    public static QueryInfoType getQueryInfo(String url, String methodCalled, String notes, String[] parameters) {
	QueryInfoType ret = new QueryInfoType();
	try {
	    ret.setCreationTime(ISO8601DateTimeUtils.getXMLGregorianCalendar(new Date()));
	} catch (DatatypeConfigurationException e) {
	    e.printStackTrace();
	}
	Criteria criteria = new Criteria();
	criteria.setMethodCalled(methodCalled);
	ret.setCriteria(criteria);
	if (notes != null) {
	    NoteType note = new NoteType();
	    note.setValue(notes);
	    ret.getNote().add(note);
	}
	if (parameters != null) {
	    for (int i = 0; i < parameters.length - 1; i += 2) {
		String name = parameters[i];
		String value = parameters[i + 1];
		Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setValue(value);
		criteria.getParameter().add(parameter);
	    }
	}
	ret.setQueryURL(url);
	return ret;
    }

}
