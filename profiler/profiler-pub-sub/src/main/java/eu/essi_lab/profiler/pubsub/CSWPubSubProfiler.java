package eu.essi_lab.profiler.pubsub;

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

import java.io.ByteArrayOutputStream;

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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.DELETERequestFilter;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;
import eu.essi_lab.profiler.pubsub.handler.csw.CSWPubSubGetCapabilitiesHandler;
import eu.essi_lab.profiler.pubsub.handler.csw.CSWSubscribeHandler;
import eu.essi_lab.profiler.pubsub.handler.csw.CSWSubscriptionsHandler;
import eu.essi_lab.profiler.pubsub.handler.csw.CSWUnsubscribeHandler;

/**
 * http://docs.opengeospatial.org/is/13-131r1/13-131r1.html
 * 
 * @author Fabrizio
 */
public class CSWPubSubProfiler extends Profiler {

    static final String CSW_PUB_SUB_PROFILER_TYPE = "CSW-PUB-SUB";

    public static final ProfilerSetting PUB_SUB_SERVICE_INFO = new ProfilerSetting();
    static {
	PUB_SUB_SERVICE_INFO.setServiceName("CSWPubSub");
	PUB_SUB_SERVICE_INFO.setServiceType(CSW_PUB_SUB_PROFILER_TYPE);
	PUB_SUB_SERVICE_INFO.setServicePath("cswpubsub");
	PUB_SUB_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// GET capabilities
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath()), new CSWPubSubGetCapabilitiesHandler());

	// GET subscription
	// GET subscription/id
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/subscription"), new CSWSubscriptionsHandler());
	selector.register(new GETRequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/subscription/*"), new CSWSubscriptionsHandler());
	// DELETE subscription/id
	selector.register(new DELETERequestFilter(PUB_SUB_SERVICE_INFO.getServicePath() + "/subscription/*"), new CSWUnsubscribeHandler());
	// POST
	selector.register(new WebRequestFilter() {
	    @Override
	    public boolean accept(WebRequest request) throws GSException {
		return request.isPostRequest();
	    }
	}, new CSWSubscribeHandler());

	return selector;
    }

    @Override
    protected ProfilerSetting initSetting() {

	return PUB_SUB_SERVICE_INFO;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
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
    private ExceptionReport createExceptionReport(ValidationMessage message) {

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

	return null;
    }
}
