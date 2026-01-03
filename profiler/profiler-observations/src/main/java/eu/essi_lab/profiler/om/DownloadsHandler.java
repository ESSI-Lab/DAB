package eu.essi_lab.profiler.om;

import java.io.File;
import java.io.FileOutputStream;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import eu.essi_lab.lib.utils.GSLoggerFactory;
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

	GSUser user = UserFinder.findCurrentUser(webRequest.getServletRequest());

	// check permissions
	Optional<String> optView = webRequest.extractViewId();
	if (optView.isPresent() && optView.get().equals("his-central")) {
	    if (!user.hasPermission("downloads") || !user.hasPermission("api")) {
		printErrorMessage(output, "The user has not correct permissions");
		return;
	    }
	}

	//

	Optional<String> emailProperty = user.getStringPropertyValue("email");

	String email = null;
	if (emailProperty.isPresent()) {
	    email = emailProperty.get();
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

    /**
     * @param email
     * @return
     * @throws Exception
     */
    private List<JSONObject> getStatuses(String email) throws Exception {

	ListObjectsV2Response objects = s3wrapper.listObjects("his-central", "data-downloads/" + email);

	return objects.contents().//
		parallelStream().//
		filter(c -> c.key().endsWith("-status.json")).//
		map(c -> {

		    try {

			JSONObject out = null;

			File temp = File.createTempFile("status", ".json");

			if (s3wrapper.download("his-central", c.key(), temp)) {

			    String s = Files.readString(temp.toPath());
			    out = new JSONObject(s);

			} else {

			    out = new JSONObject();
			    out.put("status", "Error downloading status");
			    out.put("id", c.key().replace("data-downloads/", ""));
			    out.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
			}

			temp.delete();

			return out;

		    } catch (Exception ex) {

			GSLoggerFactory.getLogger(getClass()).error(ex);
		    }

		    return null;

		}).filter(Objects::nonNull).//

		collect(Collectors.toList());
    }

    protected void printErrorMessage(OutputStream output, String message) throws IOException {

	OutputStreamWriter writer = new OutputStreamWriter(output);

	JSONObject error = new JSONObject();
	error.put("message", message);

	writer.write(error.toString());
	writer.close();
    }
}
