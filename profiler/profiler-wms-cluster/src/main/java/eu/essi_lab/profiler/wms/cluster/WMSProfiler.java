package eu.essi_lab.profiler.wms.cluster;

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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.wms.exceptions._1_3_0.ServiceExceptionReport;
import eu.essi_lab.jaxb.wms.exceptions._1_3_0.ServiceExceptionType;
import eu.essi_lab.jaxb.wms.extension.JAXBWMS;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.wms.cluster.capabilities.WMSCapabilitiesFilter;
import eu.essi_lab.profiler.wms.cluster.capabilities.WMSCapabilitiesHandler;
import eu.essi_lab.profiler.wms.cluster.demo.WMSDemoFilter;
import eu.essi_lab.profiler.wms.cluster.demo.WMSDemoHandler;
import eu.essi_lab.profiler.wms.cluster.feature.info.WMSGetFeatureInfoHandler;
import eu.essi_lab.profiler.wms.cluster.feature.info.WMSGetFeatureInfoRequestFilter;
import eu.essi_lab.profiler.wms.cluster.legend.WMSGetLegendHandler;
import eu.essi_lab.profiler.wms.cluster.legend.WMSLegendRequestFilter;
import eu.essi_lab.profiler.wms.cluster.map.WMSGetMapHandler2;
import eu.essi_lab.profiler.wms.cluster.map.WMSMapRequestFilter;

/**
 * Profiler implementing WMS protocol providing a map of cluster points
 * 
 * @author boldrini
 */
public class WMSProfiler extends Profiler<WMSClusterProfilerSetting> {

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("1.3.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// Get Capabilities
	WMSCapabilitiesHandler capabilitiesHandler = new WMSCapabilitiesHandler();
	selector.register(new WMSCapabilitiesFilter(), capabilitiesHandler);

	// Get Map
	WMSGetMapHandler2 getMapHandler = new WMSGetMapHandler2();
	selector.register(new WMSMapRequestFilter(), getMapHandler);

	// Get Legend
	WMSGetLegendHandler getLegendHandler = new WMSGetLegendHandler(getSetting());
	selector.register(new WMSLegendRequestFilter(), getLegendHandler);

	// Get Feature Info
	WMSGetFeatureInfoHandler getFeatureInfoHandler = new WMSGetFeatureInfoHandler();
	selector.register(new WMSGetFeatureInfoRequestFilter(), getFeatureInfoHandler);

	// Demo
	WMSDemoHandler demoHandler = new WMSDemoHandler();
	selector.register(new WMSDemoFilter(), demoHandler);

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
	ServiceExceptionReport ser = new ServiceExceptionReport();
	ServiceExceptionType set = new ServiceExceptionType();
	set.setLocator(message.getLocator());
	set.setCode(message.getErrorCode());
	set.setValue(message.getError());
	ser.getServiceExceptions().add(set);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
	    JAXBWMS.getInstance().getMarshaller().marshal(ser, baos);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	String xml = new String(baos.toByteArray());
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
    protected WMSClusterProfilerSetting initSetting() {

	return new WMSClusterProfilerSetting();
    }
}
