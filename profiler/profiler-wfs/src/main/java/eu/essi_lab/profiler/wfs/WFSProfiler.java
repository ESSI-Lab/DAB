package eu.essi_lab.profiler.wfs;

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

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.wfs.capabilities.WFSCapabilitiesFilter;
import eu.essi_lab.profiler.wfs.capabilities.WFSCapabilitiesHandler;
import eu.essi_lab.profiler.wfs.description.DescribeFeatureQueryHandler;
import eu.essi_lab.profiler.wfs.description.DescribeFeatureRequestFilter;
import eu.essi_lab.profiler.wfs.feature.GetFeatureQueryHandler;
import eu.essi_lab.profiler.wfs.feature.GetFeatureRequestFilter;

public class WFSProfiler extends Profiler {

    /**
     * The profiler type
     */
    private static final String WFS_PROFILER_TYPE = "WFS";

    public static final ProfilerSetting WFS_SERVICE_INFO = new ProfilerSetting();
    static {
	WFS_SERVICE_INFO.setServiceName("WFS Profiler");
	WFS_SERVICE_INFO.setServiceType(WFS_PROFILER_TYPE);
	WFS_SERVICE_INFO.setServicePath("wfs");
	WFS_SERVICE_INFO.setServiceVersion("1.1.0");
    }

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    public static final String ERROR_CODE_INVALID_PARAMETER_VALUE = "InvalidParameterValue";

    static {

	SUPPORTED_VERSIONS.add("1.1.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// Get Capabilities
	WFSCapabilitiesHandler capabilitiesHandler = new WFSCapabilitiesHandler();
	selector.register(new WFSCapabilitiesFilter(), capabilitiesHandler);

	// Describe Feature
	selector.register(new DescribeFeatureRequestFilter(), new DescribeFeatureQueryHandler());

	// Get Feature
	selector.register(new GetFeatureRequestFilter(), new GetFeatureQueryHandler());

	//
	// // Demo
	// WMSDemoHandler demoHandler = new WMSDemoHandler();
	// selector.register(new WMSDemoFilter(), demoHandler );

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

    /**
     * @param message
     * @return
     */
    public static ExceptionReport createExceptionReport(ValidationMessage message) {

	ExceptionReport report = new ExceptionReport();
	report.setVersion("1.0.0");
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
    protected ProfilerSetting initSetting() {

	return WFS_SERVICE_INFO;
    }
}
