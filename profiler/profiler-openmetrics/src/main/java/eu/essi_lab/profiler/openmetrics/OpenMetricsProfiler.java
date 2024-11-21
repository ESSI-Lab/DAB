package eu.essi_lab.profiler.openmetrics;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * Profiler for reporting metrics on specific views
 * 
 * @author boldrini
 */
public class OpenMetricsProfiler extends Profiler {

    /**
     * The profiler type
     */
    private static final String OPEN_METRICS_PROFILER_TYPE = "OPEN_METRICS";

    public static final ProfilerSetting OPEN_METRICS_INFO = new ProfilerSetting();
    static {
	OPEN_METRICS_INFO.setServiceName("OpenMetrics");
	OPEN_METRICS_INFO.setServiceType(OPEN_METRICS_PROFILER_TYPE);
	OPEN_METRICS_INFO.setServicePath("metrics");
	OPEN_METRICS_INFO.setServiceVersion("1.0");
    }

    protected static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    protected static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    static {

	SUPPORTED_VERSIONS.add("1.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/json");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	////////////////////
	// FEATURES
	////////////////////
	selector.register(new OpenMetricsFilter(), new OpenMetricsHandler());

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

	String errorCode = validationMessage.getErrorCode();

	return error(errorCode);

    }

    private Response error(String message) {
	JSONObject json = new JSONObject();
	json.put("message", message);
	return Response.status(500).type(MediaType.APPLICATION_JSON).entity(json.toString()).build();
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
    protected ProfilerSetting initSetting() {

	return OPEN_METRICS_INFO;
    }

}
