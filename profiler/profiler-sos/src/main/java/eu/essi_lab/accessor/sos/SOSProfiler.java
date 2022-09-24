package eu.essi_lab.accessor.sos;

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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.accessor.sos.availability.GetDataAvailabilityFilter;
import eu.essi_lab.accessor.sos.availability.GetDataAvailabilityFormatter;
import eu.essi_lab.accessor.sos.availability.GetDataAvailabilityMapper;
import eu.essi_lab.accessor.sos.availability.GetDataAvailabilityTransformer;
import eu.essi_lab.accessor.sos.capabilities.SOSCapabilitiesFilter;
import eu.essi_lab.accessor.sos.capabilities.SOSCapabilitiesHandler;
import eu.essi_lab.accessor.sos.foi.GetFeatureOfInterestFilter;
import eu.essi_lab.accessor.sos.foi.GetFeatureOfInterestFormatter;
import eu.essi_lab.accessor.sos.foi.GetFeatureOfInterestMapper;
import eu.essi_lab.accessor.sos.foi.GetFeatureOfInterestTransformer;
import eu.essi_lab.accessor.sos.observation.GetObservationRequestFilter;
import eu.essi_lab.accessor.sos.observation.GetObservationResultSetMapper;
import eu.essi_lab.accessor.sos.observation.GetObservationTransformer;
import eu.essi_lab.accessor.sos.sensor.DescribeSensorFilter;
import eu.essi_lab.accessor.sos.sensor.DescribeSensorFormatter;
import eu.essi_lab.accessor.sos.sensor.DescribeSensorMapper;
import eu.essi_lab.accessor.sos.sensor.DescribeSensorTransformer;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterInline;

/**
 * Profiler implementing SOS v.2.0.0 protocol
 * 
 * @author boldrini
 */
public class SOSProfiler extends Profiler {

    /**
     * The profiler type
     */
    private static final String SOS_PROFILER_TYPE = "SOS";

    public static final ProfilerSetting SOS_SERVICE_INFO = new ProfilerSetting();
    static {
	SOS_SERVICE_INFO.setServiceName("Sensor Observation Service");
	SOS_SERVICE_INFO.setServiceType(SOS_PROFILER_TYPE);
	SOS_SERVICE_INFO.setServicePath("sos");
	SOS_SERVICE_INFO.setServiceVersion("2.0");
    }

    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("2.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	SOSCapabilitiesHandler capabilitiesHandler = new SOSCapabilitiesHandler();
	selector.register(new SOSCapabilitiesFilter(), capabilitiesHandler);

	DiscoveryHandler<String> getFeatureOfInterestHandler = new DiscoveryHandler<>();
	getFeatureOfInterestHandler.setMessageResponseMapper(new GetFeatureOfInterestMapper());
	getFeatureOfInterestHandler.setRequestTransformer(new GetFeatureOfInterestTransformer());
	getFeatureOfInterestHandler.setMessageResponseFormatter(new GetFeatureOfInterestFormatter());
	selector.register(new GetFeatureOfInterestFilter(), getFeatureOfInterestHandler);
	
	
	DiscoveryHandler<String> getDescribeSensorHandler = new DiscoveryHandler<>();
	getDescribeSensorHandler.setMessageResponseMapper(new DescribeSensorMapper());
	getDescribeSensorHandler.setRequestTransformer(new DescribeSensorTransformer());
	getDescribeSensorHandler.setMessageResponseFormatter(new DescribeSensorFormatter());
	selector.register(new DescribeSensorFilter(), getDescribeSensorHandler);

	DiscoveryHandler<String> getDataAvailabilityHandler = new DiscoveryHandler<>();
	getDataAvailabilityHandler.setRequestTransformer(new GetDataAvailabilityTransformer());
	getDataAvailabilityHandler.setMessageResponseFormatter(new GetDataAvailabilityFormatter());
	getDataAvailabilityHandler.setMessageResponseMapper(new GetDataAvailabilityMapper());
	selector.register(new GetDataAvailabilityFilter(), getDataAvailabilityHandler);

	AccessHandler<DataObject> getObservationHandler = new AccessHandler<>();
	getObservationHandler.setRequestTransformer(new GetObservationTransformer());
	getObservationHandler.setMessageResponseMapper(new GetObservationResultSetMapper());
	getObservationHandler.setMessageResponseFormatter(new AccessResultSetFormatterInline());
	selector.register(new GetObservationRequestFilter(), getObservationHandler);

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());

	return onValidationFailed(null, vm);
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage validationMessage) {

	String message = validationMessage.getError();

	String errorBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		+ "<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.0.0\" xsi:schemaLocation=\"http://www.opengis.net/ows/1.1 http://schemas.opengis.net/ows/1.1.0/owsAll.xsd\">\n"
		+ "  <ows:Exception exceptionCode=\"" + validationMessage.getErrorCode() + "\" locator=\"" + validationMessage.getLocator()
		+ "\">\n" + "    <ows:ExceptionText>" + message + "</ows:ExceptionText>\n" + "  </ows:Exception>\n"
		+ "</ows:ExceptionReport>";

	return Response.status(200).type(MediaType.APPLICATION_XML).entity(errorBody).build();
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
    protected ProfilerSetting initSetting() {

	return SOS_SERVICE_INFO;
    }

}
