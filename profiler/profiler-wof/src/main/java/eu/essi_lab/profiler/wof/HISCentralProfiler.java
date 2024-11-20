package eu.essi_lab.profiler.wof;

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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.wof.discovery.series.GetSeriesCatalogForBoxRequestFilter;
import eu.essi_lab.profiler.wof.discovery.series.GetSeriesCatalogForBoxResultSetFormatter;
import eu.essi_lab.profiler.wof.discovery.series.GetSeriesCatalogForBoxResultSetMapper;
import eu.essi_lab.profiler.wof.discovery.series.GetSeriesCatalogForBoxSemanticTransformer;
import eu.essi_lab.profiler.wof.discovery.series.GetSeriesCatalogForBoxTransformer;
import eu.essi_lab.profiler.wof.discovery.sites.GetSitesFormatter;
import eu.essi_lab.profiler.wof.discovery.sites.GetSitesMapper;
import eu.essi_lab.profiler.wof.discovery.sites.GetSitesRequest;
import eu.essi_lab.profiler.wof.discovery.sites.GetSitesTransformer;
import eu.essi_lab.profiler.wof.info.GetWaterOneFlowServiceInfoFilter;
import eu.essi_lab.profiler.wof.info.GetWaterOneFlowServiceInfoHandler;
import eu.essi_lab.profiler.wof.welcome.HISCentralWelcomeHandler;
import eu.essi_lab.profiler.wof.welcome.WelcomeRequestFilter;
import eu.essi_lab.profiler.wof.wsdl.HISCentralWSDLHandler;
import eu.essi_lab.profiler.wof.wsdl.WSDLRequestFilter;

/**
 * Profiler implementing CUAHSI HIS Central protocol
 * 
 * @author boldrini
 */
public class HISCentralProfiler extends Profiler<HISCentralProfilerSetting> {

    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    protected static final String HIS_CENTRAL_PROFILER_ON_DISCOVER_REQUEST_VALIDATED_ERROR = "HIS_CENTRAL_PROFILER_ON_DISCOVER_REQUEST_VALIDATED_ERROR";

    static {

	// as from https://hiscentral.cuahsi.org/
	SUPPORTED_VERSIONS.add("2.6.2");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    // Missing operations:
    // GetControlledVocabulary
    // GetSearchableConcepts
    // GetSeriesCatalogForBox2
    // GetSeriesCatalogForBox3
    // GetSeriesMetadataCountOrData
    // GetServicesInBox2
    // GetSitesInBox2
    // GetVariables
    // getOntologyTree
    // getOntologywithOption

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// WSDL
	HISCentralWSDLHandler hisCentralWSDLHandler = new HISCentralWSDLHandler();
	selector.register(new WSDLRequestFilter(), hisCentralWSDLHandler);

	DiscoveryHandler<String> getSeriesCatalogForBoxHandler = new DiscoveryHandler<>();
	getSeriesCatalogForBoxHandler.setMessageResponseMapper(new GetSeriesCatalogForBoxResultSetMapper());
	// get series
	if (request.hasSemanticPath()) {
	    getSeriesCatalogForBoxHandler.setRequestTransformer(new GetSeriesCatalogForBoxSemanticTransformer());
	} else {
	    getSeriesCatalogForBoxHandler.setRequestTransformer(new GetSeriesCatalogForBoxTransformer());
	}

	getSeriesCatalogForBoxHandler.setMessageResponseFormatter(new GetSeriesCatalogForBoxResultSetFormatter());
	selector.register(new GetSeriesCatalogForBoxRequestFilter(), getSeriesCatalogForBoxHandler);

	// get water one flow service info

	GetWaterOneFlowServiceInfoHandler getWaterOneFlowServiceInfoHandler = new GetWaterOneFlowServiceInfoHandler();
	selector.register(new GetWaterOneFlowServiceInfoFilter(), getWaterOneFlowServiceInfoHandler);

	// get sites

	DiscoveryHandler<String> getSitesHandler = new DiscoveryHandler<>();
	getSitesHandler.setRequestTransformer(new GetSitesTransformer());
	getSitesHandler.setMessageResponseMapper(new GetSitesMapper());
	getSitesHandler.setMessageResponseFormatter(new GetSitesFormatter());
	selector.register(new WOFRequestFilter() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return new GetSitesRequest(request);
	    }
	}, getSitesHandler);

	// HTML welcome page
	HISCentralWelcomeHandler hisCentralWelcomeHandler = new HISCentralWelcomeHandler();
	selector.register(new WelcomeRequestFilter(), hisCentralWelcomeHandler);

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return onValidationFailed(null, vm);
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity("").build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
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
    protected HISCentralProfilerSetting initSetting() {

	return new HISCentralProfilerSetting();
    }

    public static String generateUniqueIdFromString(String uniqueIdentifier) {
	String ret = "" + Math.abs(uniqueIdentifier.hashCode());
	return ret;
    }
}
