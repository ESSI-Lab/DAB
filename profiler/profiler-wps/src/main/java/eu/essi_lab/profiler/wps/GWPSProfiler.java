package eu.essi_lab.profiler.wps;

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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationException;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.BulkDownloadHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.ScheduleAccessHandler;
import eu.essi_lab.pdk.handler.ScheduleBulkDownloadHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterAttachment;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.pdk.wrt.ScheduleRequestTransformer;
import eu.essi_lab.profiler.wps.bulk.GWPSBulkDownloadHandler;
import eu.essi_lab.profiler.wps.bulk.GWPSBulkDownloadRequestFilter;
import eu.essi_lab.profiler.wps.bulk.GWPSBulkDownloadRequestFilterSynch;
import eu.essi_lab.profiler.wps.capabilities.GWPSCapabilitiesRequestFilter;
import eu.essi_lab.profiler.wps.capabilities.GWPSCapabilitiesResultSetFormatter;
import eu.essi_lab.profiler.wps.capabilities.GWPSCapabilitiesResultSetMapper;
import eu.essi_lab.profiler.wps.capabilities.GWPSCapabilitiesTransformer;
import eu.essi_lab.profiler.wps.capabilities.GWPS_GP_CapabilitiesResultSetFormatter;
import eu.essi_lab.profiler.wps.executor.GWPSExecuteTransformer;
import eu.essi_lab.profiler.wps.executor.asynch.GWPSExecuteFormatter;
import eu.essi_lab.profiler.wps.executor.asynch.GWPSExecuteResultMapper;
import eu.essi_lab.profiler.wps.executor.asynch.GWPS_GP_ExecuteFormatter;
import eu.essi_lab.profiler.wps.status.GWPSStatusHandler;
import eu.essi_lab.profiler.wps.status.GWPSStatusRequestFilter;

/**
 * Profiler implementing WPS protocol as requested by legacy GEOSS Web Portal
 * 
 * @author boldrini
 */
public class GWPSProfiler extends Profiler {

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	// as from https://hiscentral.cuahsi.org/
	SUPPORTED_VERSIONS.add("1.0.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// Get Capabilities
	DiscoveryHandler<String> getCapabilitiesExecutorHandler = new DiscoveryHandler<String>();
	getCapabilitiesExecutorHandler.setMessageResponseMapper(new GWPSCapabilitiesResultSetMapper());
	getCapabilitiesExecutorHandler.setRequestTransformer(new GWPSCapabilitiesTransformer());

	if (isRequestFromGP(request)) {
	    GSLoggerFactory.getLogger(getClass()).trace("Using GP capabilities result set formatter");
	    getCapabilitiesExecutorHandler.setMessageResponseFormatter(new GWPS_GP_CapabilitiesResultSetFormatter());
	} else {
	    getCapabilitiesExecutorHandler.setMessageResponseFormatter(new GWPSCapabilitiesResultSetFormatter());
	}

	selector.register(new GWPSCapabilitiesRequestFilter(), getCapabilitiesExecutorHandler);

	// Execute (synchronous request)
	AccessHandler<DataObject> executeSynchHandler = new AccessHandler<DataObject>();
	executeSynchHandler.setRequestTransformer(new GWPSExecuteTransformer());
	executeSynchHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	executeSynchHandler.setMessageResponseFormatter(new AccessResultSetFormatterAttachment());
	selector.register(new eu.essi_lab.profiler.wps.executor.synch.GWPSExecuteRequestFilter(), executeSynchHandler);

	// Execute (asynchronous request)
	ScheduleAccessHandler<String> executeAsynchHandler = new ScheduleAccessHandler<String>(executeSynchHandler);
	ScheduleRequestTransformer sart = new ScheduleRequestTransformer();
	sart.setTransformer(new GWPSExecuteTransformer());
	executeAsynchHandler.setRequestTransformer(sart);

	if (isRequestFromGP(request)) {
	    GSLoggerFactory.getLogger(getClass()).trace("Using GP execute formatter");
	    executeAsynchHandler.setMessageResponseFormatter(new GWPS_GP_ExecuteFormatter());
	} else {
	    executeAsynchHandler.setMessageResponseFormatter(new GWPSExecuteFormatter());
	}

	executeAsynchHandler.setMessageResponseMapper(new GWPSExecuteResultMapper());
	selector.register(new eu.essi_lab.profiler.wps.executor.asynch.GWPSExecuteRequestFilter(), executeAsynchHandler);

	// Status handler
	GWPSStatusHandler statusHandler = new GWPSStatusHandler();
	selector.register(new GWPSStatusRequestFilter(), statusHandler);

	// Bulk download (synchronous request)
	BulkDownloadHandler<DataObject> bulkDownloadHandler = new GWPSBulkDownloadHandler();
	bulkDownloadHandler.setRequestTransformer(new GWPSBulkDownloadRequestTransformer());
	bulkDownloadHandler.setMessageResponseMapper(new GWPSBulkDownloadResultSetMapper());
	bulkDownloadHandler.setMessageResponseFormatter(new GWPSBulkDownloadResponseFormatter());
	selector.register(new GWPSBulkDownloadRequestFilterSynch(), bulkDownloadHandler);

	// Bulk download (asynchronous request)

	ScheduleBulkDownloadHandler<String> bulkDownloadAsynchHandler = new ScheduleBulkDownloadHandler<String>(bulkDownloadHandler);
	ScheduleRequestTransformer sart2 = new ScheduleRequestTransformer();
	sart2.setTransformer(new GWPSBulkDownloadRequestTransformer());
	bulkDownloadAsynchHandler.setRequestTransformer(sart2);

	if (isRequestFromGP(request)) {
	    GSLoggerFactory.getLogger(getClass()).trace("Using GP execute formatter");
	    bulkDownloadAsynchHandler.setMessageResponseFormatter(new GWPS_GP_ExecuteFormatter());
	} else {
	    bulkDownloadAsynchHandler.setMessageResponseFormatter(new GWPSExecuteFormatter());
	}

	bulkDownloadAsynchHandler.setMessageResponseMapper(new GWPSExecuteResultMapper());
	selector.register(new GWPSBulkDownloadRequestFilter(), bulkDownloadAsynchHandler);

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

	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
		"<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/ows/1.1 http://schemas.opengis.net/ows/1.1.0/owsAll.xsd\" version=\"1.0.0\" xml:lang=\"en-CA\">\n";

	List<ValidationException> exceptions = message.getExceptions();
	for (ValidationException exception : exceptions) {

	    String locator = exception.getLocator();
	    String errorCode = exception.getCode();
	    String msg = exception.getMessage();

	    xml += "	<ows:Exception locator=\"" + locator + "\"  exceptionCode=\"" + errorCode + "\">\n" + "		<ows:ExceptionText>"
		    + msg + "</ows:ExceptionText>\n" + "	</ows:Exception>\n";
	}

	xml += "</ows:ExceptionReport>";

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(xml).build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	ValidationMessage message = new ValidationMessage();
	message.setError("Handler not found");
	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected GWPSProfilerSetting initSetting() {

	return new GWPSProfilerSetting();
    }

    public boolean isRequestFromGP(WebRequest request) {
	return request.getUriInfo().getBaseUri().toString().contains("gs-service-production.geodab.eu")
		|| request.getUriInfo().getBaseUri().toString().contains("gs-service-preproduction.geodab.eu");
    }
}
