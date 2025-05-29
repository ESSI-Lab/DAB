package eu.essi_lab.profiler.wps.status;

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

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.shared.driver.DriverFactory;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;

/**
 * @author Fabrizio
 */
public class GWPSStatusHandler extends DefaultRequestHandler {

    /**
     * @param webRequest
     * @return
     */
    private String getId(WebRequest webRequest) {

	if (webRequest.isGetRequest()) {

	    String servletRequest = webRequest.getUriInfo().getAbsolutePath().toString();

	    String[] split = servletRequest.split("/");

	    boolean status = false;
	    String statusId = null;
	    for (String s : split) {
		if (status) {
		    statusId = s;
		    break;
		}
		if (s.equals("status")) {
		    status = true;
		} else {
		    status = false;
		}
	    }

	    return statusId;
	}

	return null;
    }

    @Override
    public ValidationMessage validate(WebRequest webRequest) throws GSException {

	String id = getId(webRequest);

	ValidationMessage ret = new ValidationMessage();
	if (id == null) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	} else {
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	}

	return ret;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	SharedPersistentDriverSetting driverSetting = ConfigurationWrapper.getSharedPersistentDriverSetting();

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getConfiguredDriver(driverSetting, true);

	String id = getId(webRequest);

	SharedContent<JSONObject> response = getResponse(driver, id);

	JSONObject output = new JSONObject();

	if (response == null) {

	    output.put("status", "DOWNLOADING DATA");
	    output.put("message", "Transformation request has started");
	} else {

	    JSONObject content = response.getContent();

	    SchedulerJobStatus status = new SchedulerJobStatus(content);
	    JobPhase phase = status.getPhase();

	    switch (phase) {
	    case COMPLETED:

		output.put("status", "COMPLETED");
		output.put("message", phase.getLabel());
		output.put("data", status.getDataUri().orElse("missing"));

		break;

	    case ERROR:
	    case CANCELED:

		output.put("status", "ERROR");
		List<String> errorMessages = status.getErrorMessages();
		if (!errorMessages.isEmpty()) {
		    output.put("message", errorMessages.stream().collect(Collectors.joining(",")));
		}

		break;

	    case RESCHEDULED:
	    case RUNNING:
		output.put("status", "DOWNLOADING DATA");
		output.put("message", "Transformation request has started");

		break;
	    }
	}

	return output.toString();
    }

    @SuppressWarnings("unchecked")
    private SharedContent<JSONObject> getResponse(@SuppressWarnings("rawtypes") ISharedRepositoryDriver driver, String id) {

	try {
	    return driver.read(id, SharedContentType.JSON_TYPE);
	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}

	return null;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
