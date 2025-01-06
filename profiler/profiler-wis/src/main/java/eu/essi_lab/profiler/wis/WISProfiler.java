package eu.essi_lab.profiler.wis;

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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
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
import eu.essi_lab.profiler.wis.collections.WISCollectionsFilter;
import eu.essi_lab.profiler.wis.collections.WISCollectionsHandler;
import eu.essi_lab.profiler.wis.landing.WISLandingFilter;
import eu.essi_lab.profiler.wis.landing.WISLandingHandler;
import eu.essi_lab.profiler.wis.metadata.collection.WISDiscoveryMetadataFilter;
import eu.essi_lab.profiler.wis.metadata.collection.WISDiscoveryMetadataHandler;
import eu.essi_lab.profiler.wis.metadata.dataset.WISDatasetDiscoveryMetadataFilter;
import eu.essi_lab.profiler.wis.metadata.dataset.WISDatasetDiscoveryMetadataHandler;
import eu.essi_lab.profiler.wis.observations.WISObservationsFilter;
import eu.essi_lab.profiler.wis.observations.WISObservationsHandler;
import eu.essi_lab.profiler.wis.station.info.WISStationInfoFilter;
import eu.essi_lab.profiler.wis.station.info.WISStationInfoHandler;

/**
 * Profiler implementing WIS 2 in a box protocol
 * 
 * @author boldrini
 */
public class WISProfiler extends Profiler<WISProfilerSetting> {

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("2.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// Landing page
	WISLandingHandler landingHandler = new WISLandingHandler();
	selector.register(new WISLandingFilter(), landingHandler);

	// Collections
	WISCollectionsHandler collectionsHandler = new WISCollectionsHandler();
	selector.register(new WISCollectionsFilter(), collectionsHandler);

	// Discovery metadata (discovery items are sources)
	WISDiscoveryMetadataHandler discoveryMetadataHandler = new WISDiscoveryMetadataHandler();
	selector.register(new WISDiscoveryMetadataFilter(), discoveryMetadataHandler);

	// Station info
	WISStationInfoHandler stationInfoHandler = new WISStationInfoHandler();
	selector.register(new WISStationInfoFilter(), stationInfoHandler);

	// Observations
	WISObservationsHandler observationsHandler = new WISObservationsHandler();
	selector.register(new WISObservationsFilter(), observationsHandler);

	// Discovery metadata (discovery items are dataset)
	WISDatasetDiscoveryMetadataHandler datasetDiscoveryMetadataHandler = new WISDatasetDiscoveryMetadataHandler();
	selector.register(new WISDatasetDiscoveryMetadataFilter(), datasetDiscoveryMetadataHandler);

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
    protected WISProfilerSetting initSetting() {

	return new WISProfilerSetting();
    }
}
