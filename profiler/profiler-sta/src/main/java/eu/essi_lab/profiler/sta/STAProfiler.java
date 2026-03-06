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

package eu.essi_lab.profiler.sta;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.sta.filter.DatastreamsFilter;
import eu.essi_lab.profiler.sta.filter.DatastreamsObservationsFilter;
import eu.essi_lab.profiler.sta.filter.FeaturesOfInterestFilter;
import eu.essi_lab.profiler.sta.filter.LocationsFilter;
import eu.essi_lab.profiler.sta.filter.ObservationsFilter;
import eu.essi_lab.profiler.sta.filter.RootFilter;
import eu.essi_lab.profiler.sta.filter.ThingsFilter;
import eu.essi_lab.profiler.sta.filter.ThingsLocationsFilter;
import eu.essi_lab.profiler.sta.handler.DatastreamsHandler;
import eu.essi_lab.profiler.sta.handler.DatastreamsObservationsHandler;
import eu.essi_lab.profiler.sta.handler.FeaturesOfInterestHandler;
import eu.essi_lab.profiler.sta.handler.LocationsHandler;
import eu.essi_lab.profiler.sta.handler.ObservationsHandler;
import eu.essi_lab.profiler.sta.handler.RootHandler;
import eu.essi_lab.profiler.sta.handler.ThingsHandler;
import eu.essi_lab.profiler.sta.handler.ThingsLocationsHandler;

/**
 * Profiler for OGC SensorThings API (STA) Part 1: Sensing.
 *
 * @author boldrini
 */
public class STAProfiler extends Profiler<STAProfilerSetting> {

    @Override
    public HandlerSelector getSelector(WebRequest request) {
	HandlerSelector selector = new HandlerSelector();

	selector.register(new RootFilter(), new RootHandler());
	selector.register(new DatastreamsObservationsFilter(), new DatastreamsObservationsHandler());
	selector.register(new DatastreamsFilter(), new DatastreamsHandler());
	selector.register(new ThingsLocationsFilter(), new ThingsLocationsHandler());
	selector.register(new ThingsFilter(), new ThingsHandler());
	selector.register(new LocationsFilter(), new LocationsHandler());
	selector.register(new ObservationsFilter(), new ObservationsHandler());
	selector.register(new FeaturesOfInterestFilter(), new FeaturesOfInterestHandler());

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
    protected Response onValidationFailed(WebRequest request, ValidationMessage validationMessage) {
	JSONObject json = new JSONObject();
	json.put("message", validationMessage.getError());
	json.put("error", validationMessage.getErrorCode());
	return Response.status(Integer.valueOf(validationMessage.getErrorCode()))
		.type(MediaType.APPLICATION_JSON)
		.entity(json.toString()).build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {
	ValidationMessage message = new ValidationMessage();
	message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	message.setError("Invalid request parameter");
	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected STAProfilerSetting initSetting() {
	return new STAProfilerSetting();
    }
}
