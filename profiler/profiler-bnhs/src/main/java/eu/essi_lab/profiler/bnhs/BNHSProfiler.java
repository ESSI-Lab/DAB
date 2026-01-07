package eu.essi_lab.profiler.bnhs;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * @author boldrini
 */
public class BNHSProfiler extends Profiler<BNHSProfilerSetting> {

    public BNHSProfiler() {

    }

    /**
     * @param setting
     * @return
     */
    static String readViewId(WebRequest request) {
	
	return request.extractViewId().orElse("whos-arctic");	
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	//
	// the modified CSV list (adding the links to the DAB)
	//

	DiscoveryHandler<String> bnhsHandler = new DiscoveryHandler<>();
	bnhsHandler.setRequestTransformer(new BNHSRequestTransformer(request));
	bnhsHandler.setMessageResponseMapper(new BNHSResultSetMapper());
	bnhsHandler.setMessageResponseFormatter(new BNHSResultSetFormatter());
	selector.register(//
		createFiter("bnhs/csv"), //
		bnhsHandler);

	//
	// the station page
	//
	WebRequestHandler stationHandler = new BNHSStationHandler(request);
	selector.register(//
		createFiter("bnhs/station/*"), //
		stationHandler);

	//
	// the station timeseries page
	//
	WebRequestHandler stationTimeSeriesHandler = new BNHSStationHandler(request);
	selector.register(//
		createFiter("bnhs/station/*/timeseries"), //
		stationTimeSeriesHandler);

	//
	// base web page, with links (e.g. http://gs-service-production.geodab.eu/gs-service/services/bnhs)
	//
	WebRequestHandler infoHandler = new BNHSInfoHandler(request);
	selector.register(//
		createFiter("bnhs"), //
		infoHandler);

	return selector;
    }

    /**
     * @param path
     * @return
     */
    private GETRequestFilter createFiter(String path) {

	GETRequestFilter filter = new GETRequestFilter(path);
	filter.overrideServicesPath("/services/");

	return filter;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected BNHSProfilerSetting initSetting() {

	return new BNHSProfilerSetting();
    }

    @Override
    public Response createUncaughtError(WebRequest request, Status status, String message) {
	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return onValidationFailed(null, vm);
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {
	return Response.status(200).type(MediaType.TEXT_XML).entity(message.getError()).build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {
	ValidationMessage msg = new ValidationMessage();
	msg.setError("Handler not found");
	return onValidationFailed(request, msg);

    }
}
