package eu.essi_lab.profiler.om;

import java.io.File;
import java.io.FileOutputStream;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.om.OMRequest.APIParameters;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class DownloadsHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    private static DownloadSetting getDownloadSetting() {
	return ConfigurationWrapper.getDownloadSetting();
    }

    private static S3TransferWrapper s3wrapper = null;

    static {
	if (getDownloadSetting().getDownloadStorage() == DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	} else {
	    String accessKey = getDownloadSetting().getS3StorageSetting().getAccessKey().get();
	    String secretKey = getDownloadSetting().getS3StorageSetting().getSecretKey().get();

	    s3wrapper = new S3TransferWrapper();
	    s3wrapper.setAccessKey(accessKey);
	    s3wrapper.setSecretKey(secretKey);
	    s3wrapper.initialize();

	}
    }

    public DownloadsHandler() {

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return new StreamingOutput() {
	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		try {
		    handle(output, webRequest);
		} catch (Exception e) {
		    throw new WebApplicationException("Error handling status request", e);
		}
	    }
	};
    }

    protected void handle(OutputStream output, WebRequest webRequest) throws Exception {
	OMRequest request = new OMRequest(webRequest);
	String operationId = request.getParameterValue(APIParameters.ID);
	JSONObject ret = new JSONObject();
	JSONArray array = new JSONArray();

	String method = webRequest.getServletRequest().getMethod();

	GSUser user = UserFinder.create().findCurrentUser(webRequest.getServletRequest());

	GSProperty emailProperty = user.getProperty("email");

	String email = null;
	if (emailProperty != null) {
	    email = emailProperty.getValue().toString();
	}

	if (method.toLowerCase().equals("delete")) {

	    if (operationId != null) {
		List<JSONObject> statuses = getStatuses(email);
		for (JSONObject status : statuses) {
		    String id = status.optString("id");
		    if (id != null && id.equals(operationId)) {
			String stat = status.optString("status");
			if (stat != null && stat.equals("Completed")) {
			    // delete result
			    s3wrapper.deleteObject("his-central", "data-downloads/" + id + "-cancel");
			    s3wrapper.deleteObject("his-central", "data-downloads/" + id + ".zip");
			    status.put("locator", "");
			    status.put("status", "Removed");
			    OMHandler.status(s3wrapper, "his-central", id, status);
			} else {
			    // interrupt job
			    putCancelFlag(operationId);
			}
			break;
		    }
		}

	    }
	    return;

	}

	List<JSONObject> statuses = getStatuses(email);

	if (operationId == null) {
	    for (JSONObject jsonObject : statuses) {
		array.put(jsonObject);
	    }
	} else {
	    for (JSONObject jsonObject : statuses) {
		String id = jsonObject.optString("id");
		if (id != null && id.equals(operationId)) {
		    array.put(jsonObject);
		    break;
		}
	    }
	}

	// Create the response object
	JsonObject response = new JsonObject();
	response.addProperty("id", operationId);
	response.addProperty("status", "Not found");
	// response.addProperty("locator", "http://example.com/download.zip");

	// Write the response
	ret.put("results", array);
	try (OutputStreamWriter writer = new OutputStreamWriter(output)) {
	    writer.write(ret.toString());
	    writer.flush();
	}
    }

    private void putCancelFlag(String id) throws Exception {

	File tempFile = File.createTempFile(getClass().getSimpleName(), "cancel");
	FileOutputStream fos = new FileOutputStream(tempFile);
	fos.write("cancel".getBytes(StandardCharsets.UTF_8));
	fos.close();
	s3wrapper.uploadFile(tempFile.getAbsolutePath(), "his-central", "data-downloads/" + id + "-cancel");
	tempFile.delete();

    }

    private List<JSONObject> getStatuses(String email) throws Exception {
	ListObjectsV2Response objects = s3wrapper.listObjects("his-central", "data-downloads/" + email);
	List<JSONObject> ret = new ArrayList<JSONObject>();
	for (S3Object content : objects.contents()) {
	    String key = content.key();
	    if (key.endsWith("-status.json")) {
		File temp = File.createTempFile("status", ".json");
		boolean good = s3wrapper.download("his-central", key, temp);
		if (good) {
		    String s = Files.readString(temp.toPath());
		    JSONObject j = new JSONObject(s);
		    ret.add(j);
		    temp.delete();
		} else {
		    JSONObject s = new JSONObject();
		    s.put("status", "Error downloading status");
		    s.put("id", key.replace("data-downloads/", ""));
		    s.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
		    ret.add(s);
		}

	    }
	}
	return ret;
    }
}
