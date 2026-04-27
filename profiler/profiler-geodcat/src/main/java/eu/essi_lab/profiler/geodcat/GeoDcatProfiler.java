package eu.essi_lab.profiler.geodcat;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * Profiler exposing DCAT (catalog) and GeoDCAT (dataset) JSON-LD endpoints backed by discovery.
 */
public class GeoDcatProfiler extends Profiler<GeoDcatProfilerSetting> {

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	DiscoveryHandler<GSResource> catalogHandler = new DiscoveryHandler<>();
	catalogHandler.setRequestTransformer(new GeoDcatCatalogRequestTransformer());
	catalogHandler.setMessageResponseMapper(new GeoDcatResultSetMapper());
	catalogHandler.setMessageResponseFormatter(new GeoDcatCatalogJsonLdFormatter());
	selector.register(new GETRequestFilter("geodcat/catalog"), catalogHandler);

	DiscoveryHandler<GSResource> datasetHandler = new DiscoveryHandler<>();
	datasetHandler.setRequestTransformer(new GeoDcatDatasetRequestTransformer());
	datasetHandler.setMessageResponseMapper(new GeoDcatResultSetMapper());
	datasetHandler.setMessageResponseFormatter(new GeoDcatDatasetJsonLdFormatter());
	selector.register(new GeoDcatDatasetRequestFilter(), datasetHandler);

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity("{}").build();
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return Response.status(Status.BAD_REQUEST).type(GeoDcatJsonLd.MEDIA_TYPE).entity("{}").build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	ValidationMessage message = new ValidationMessage();
	message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	message.setError("No handler for this path");

	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected GeoDcatProfilerSetting initSetting() {

	return new GeoDcatProfilerSetting();
    }
}
