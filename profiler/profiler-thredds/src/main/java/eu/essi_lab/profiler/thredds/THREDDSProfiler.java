package eu.essi_lab.profiler.thredds;

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
import javax.xml.bind.JAXBElement;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterInline;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.profiler.thredds.catalog.THREDDSCatalogRequestFilter;
import eu.essi_lab.profiler.thredds.catalog.THREDDSCatalogRequestTransformer;
import eu.essi_lab.profiler.thredds.catalog.THREDDSCatalogResultSetFormatter;
import eu.essi_lab.profiler.thredds.catalog.THREDDSCatalogResultSetMapper;
import eu.essi_lab.profiler.thredds.das.THREDDSDASRequestFilter;
import eu.essi_lab.profiler.thredds.das.THREDDSDASTransformer;
import eu.essi_lab.profiler.thredds.dds.DDSAccessResultSetFormatterInline;
import eu.essi_lab.profiler.thredds.dds.THREDDSDDSRequestFilter;
import eu.essi_lab.profiler.thredds.dds.THREDDSDDSTransformer;
import eu.essi_lab.profiler.thredds.http.THREDDSHTTPRequestFilter;
import eu.essi_lab.profiler.thredds.http.THREDDSHTTPTransformer;
import eu.essi_lab.profiler.thredds.ncss.NCSSRequestFilter;
import eu.essi_lab.profiler.thredds.ncss.NCSSTransformer;
import eu.essi_lab.thredds._1_0_6.DatasetType;

public class THREDDSProfiler extends Profiler<THREDDSProfilerSetting> {

    /*
     * TODO
     * thredds/dodsC/testAll/Level3_Composite_N0R_20201004_0005.grib2.dds
     * /thredds/dodsC/testAll/Level3_Composite_N0R_20201004_0005.grib2.das
     * /thredds/dodsC/testAll/Level3_Composite_N0R_20201004_0005.grib2.dds
     * /thredds/dodsC/testAll/Level3_Composite_N0R_20201004_0005.grib2.dods?LambertConformal_Projection,y,reftime,time
     * /thredds/dodsC/testAll/Level3_Composite_N0R_20201004_0005.grib2.dods?x
     */

    /**
     * 
     */
    public static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    /**
     * 
     */
    public static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("1.0.6");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// CATALOG.XML / html
	DiscoveryHandler<JAXBElement<DatasetType>> handler = new DiscoveryHandler<>();
	handler.setRequestTransformer(new THREDDSCatalogRequestTransformer());
	handler.setMessageResponseMapper(new THREDDSCatalogResultSetMapper());
	handler.setMessageResponseFormatter(new THREDDSCatalogResultSetFormatter());
	selector.register(new THREDDSCatalogRequestFilter(), handler);

	// DDS
	AccessHandler<DataObject> ddsHandler = new AccessHandler<>();
	ddsHandler.setRequestTransformer(new THREDDSDDSTransformer());
	ddsHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	ddsHandler.setMessageResponseFormatter(new DDSAccessResultSetFormatterInline());
	selector.register(new THREDDSDDSRequestFilter(), ddsHandler);

	// DAS
	AccessHandler<DataObject> dasHandler = new AccessHandler<>();
	dasHandler.setRequestTransformer(new THREDDSDASTransformer());
	dasHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	dasHandler.setMessageResponseFormatter(new AccessResultSetFormatterInline());
	selector.register(new THREDDSDASRequestFilter(), dasHandler);

	// HTTP
	AccessHandler<DataObject> httpHandler = new AccessHandler<>();
	httpHandler.setRequestTransformer(new THREDDSHTTPTransformer());
	httpHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	httpHandler.setMessageResponseFormatter(new AccessResultSetFormatterInline());
	selector.register(new THREDDSHTTPRequestFilter(), httpHandler);

	// NCSS
	AccessHandler<DataObject> ncssHandler = new AccessHandler<>();
	ncssHandler.setRequestTransformer(new NCSSTransformer());
	ncssHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	ncssHandler.setMessageResponseFormatter(new AccessResultSetFormatterInline());
	selector.register(new NCSSRequestFilter(), ncssHandler);

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
    protected THREDDSProfilerSetting initSetting() {

	return new THREDDSProfilerSetting();
    }
}
