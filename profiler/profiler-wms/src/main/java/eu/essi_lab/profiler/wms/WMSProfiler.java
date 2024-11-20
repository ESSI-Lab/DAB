package eu.essi_lab.profiler.wms;

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
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterInline;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.profiler.wms.capabilities.WMSCapabilitiesFilter;
import eu.essi_lab.profiler.wms.capabilities.WMSCapabilitiesHandler;
import eu.essi_lab.profiler.wms.demo.WMSDemoFilter;
import eu.essi_lab.profiler.wms.demo.WMSDemoHandler;
import eu.essi_lab.profiler.wms.map.WMSMapRequestFilter;
import eu.essi_lab.profiler.wms.map.WMSMapTransformer;

/**
 * Profiler implementing WMS protocol
 * 
 * @author boldrini
 */
public class WMSProfiler extends Profiler<WMSProfilerSetting> {

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
	AccessHandler<DataObject> getMapHandler = new AccessHandler<DataObject>();
	getMapHandler.setRequestTransformer(new WMSMapTransformer());
	getMapHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	getMapHandler.setMessageResponseFormatter(new AccessResultSetFormatterInline());
	selector.register(new WMSMapRequestFilter(), getMapHandler);

	// Get Capabilities
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

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity("").build();
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
    protected WMSProfilerSetting initSetting() {

	return new WMSProfilerSetting();
    }
}
