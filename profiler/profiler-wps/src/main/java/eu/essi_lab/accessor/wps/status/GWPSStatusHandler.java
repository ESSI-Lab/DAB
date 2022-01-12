package eu.essi_lab.accessor.wps.status;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.report.GSJobEventCollectorFactory;
import eu.essi_lab.jobs.report.GSJobStatusCollector;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.model.SharedContent;

public class GWPSStatusHandler extends DefaultRequestHandler {

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

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

 	SharedRepositoryInfo repo = ConfigurationUtils.getSharedRepositoryInfo();
	GSJobStatusCollector collector = GSJobEventCollectorFactory.getGSJobStatusCollector(repo);

	StorageUri storageURI = ConfigurationUtils.getUserJobStorageURI();

	String id = getId(webRequest);

	SharedContentReadResponse<GSJobStatus> response;
	try {
	    response = collector.read(id);
	} catch (Exception e) {
	    return "{\"status\":\"ERROR\",\"message\":\"Error retrieving status of id: " + id + "\"}";
	}

	List<SharedContent<GSJobStatus>> contents = response.getContents();
	if (!contents.isEmpty()) {
	    SharedContent<GSJobStatus> content = contents.get(0);
	    GSJobStatus status = content.getContent();	    
	    String result = status.getResult();
	    if (result == null) {
		return "{\"status\":\"DOWNLOADING DATA\",\"message\":\"Transformation request has started\"}";
	    } else if (result.equals("PASS")) {
		Date endDate = status.getEndDate();
		if (storageURI != null && //
			storageURI.getUri() != null && storageURI.getUri().length() > 0 && //
			storageURI.getStorageName() != null && storageURI.getStorageName().length() > 0) {
//		    String url = storageURI.getUri() + storageURI.getStorageName() + "/" + objectName;
		    String url = status.getResultStorage();;
		    return " {\"status\":\"COMPLETED\",\"message\":\"Completed\",\"data\":\"" + url + "\"}";
		} else {
		    return "{\"status\":\"ERROR\",\"message\":\"Processing is completed, however no info on which storage to use to retrieve the saved result\"}";
		}
	    } else {
		return "{\"status\":\"ERROR\",\"message\":\"" + result + "\"}";
	    }
	}

	return "{\"status\":\"ERROR\",\"message\":\"Error retrieving status of id: " + id + "\"}";

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

}
