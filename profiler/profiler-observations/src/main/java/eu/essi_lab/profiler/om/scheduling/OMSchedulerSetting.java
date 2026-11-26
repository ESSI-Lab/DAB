/**
 * 
 */
package eu.essi_lab.profiler.om.scheduling;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;

import org.json.JSONObject;

import java.util.Optional;

import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class OMSchedulerSetting extends SchedulerWorkerSetting {

    private static final String REQUEST_URL_OPTION_KEY = "requestURL";
    private static final String OPERATION_ID_OPTION_KEY = "operationId";
    private static final String E_MAIL_OPTION_KEY = "eMail";
    private static final String E_MAIL_NOTIFICATIONS_OPTION_KEY = "eMailNotifications";
    private static final String ASYNCH_DOWNLOAD_NAME_OPTION_KEY = "asynchDownloadName";
    private static final String BUCKET_OPTION_KEY = "bucket";
    private static final String PUBLIC_URL_OPTION_KEY = "publicURL";
    private static final String MAX_DOWNLOAD_SIZE_MB_OPTION_KEY = "maxDownloadSizeMB";
    private static final String MAX_DOWNLOAD_PART_SIZE_MB_OPTION_KEY = "maxDownloadPartSizeMB";
    private static final String METADATA_ONLY_OPTION_KEY = "metadataOnly";
    private static final String USER_OPTION_KEY = "user";

    /**
     * 
     */
    public OMSchedulerSetting() {

	setConfigurableType(OMSchedulerWorker.CONFIGURABLE_TYPE);

	getScheduling().setRunIndefinitely();
	getScheduling().setRepeatInterval(Integer.MAX_VALUE, TimeUnit.DAYS);

	setGroup(SchedulingGroup.ASYNCH_ACCESS);

	Option<String> requestUrlOption = StringOptionBuilder.get().//
		withKey(REQUEST_URL_OPTION_KEY).//
		build();
	addOption(requestUrlOption);

	Option<String> operationIdOption = StringOptionBuilder.get().//
		withKey(OPERATION_ID_OPTION_KEY).//
		build();
	addOption(operationIdOption);

	Option<String> eMailOption = StringOptionBuilder.get().//
		withKey(E_MAIL_OPTION_KEY).//
		build();
	addOption(eMailOption);

	Option<String> notificationOption = StringOptionBuilder.get().//
		withKey(E_MAIL_NOTIFICATIONS_OPTION_KEY).//
		build();
	addOption(notificationOption);

	Option<String> asynchDownloadNameOption = StringOptionBuilder.get().//
		withKey(ASYNCH_DOWNLOAD_NAME_OPTION_KEY).//
		build();
	addOption(asynchDownloadNameOption);

	Option<Integer> maxDownloadSizeMBOption = IntegerOptionBuilder.get().//
		withKey(MAX_DOWNLOAD_SIZE_MB_OPTION_KEY).//
		build();
	addOption(maxDownloadSizeMBOption);

	Option<Integer> maxDownloadPartSizeMBOption = IntegerOptionBuilder.get().//
		withKey(MAX_DOWNLOAD_PART_SIZE_MB_OPTION_KEY).//
		build();
	addOption(maxDownloadPartSizeMBOption);

	Option<String> bucketOption = StringOptionBuilder.get().//
		withKey(BUCKET_OPTION_KEY).//
		build();
	addOption(bucketOption);
	
	Option<String> publicURLOption = StringOptionBuilder.get().//
		withKey(PUBLIC_URL_OPTION_KEY).//
		build();
	addOption(publicURLOption);

	Option<String> metadataOnlyOption = StringOptionBuilder.get().//
		withKey(METADATA_ONLY_OPTION_KEY).//
		build();
	addOption(metadataOnlyOption);

	Option<String> userOption = StringOptionBuilder.get().//
		withKey(USER_OPTION_KEY).//
		build();
	addOption(userOption);
    }

    /**
     * @param url
     */
    public void setRequestURL(String url) {

	getOption(REQUEST_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @param id
     */
    public void setOperationId(String id) {

	getOption(OPERATION_ID_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @return
     */
    public String getRequestURL() {

	return getOption(REQUEST_URL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public String getOperationId() {

	return getOption(OPERATION_ID_OPTION_KEY, String.class).get().getValue();
    }

    public String getEmail() {

	return getOption(E_MAIL_OPTION_KEY, String.class).get().getValue();
    }

    public void setEmail(String email) {

	getOption(E_MAIL_OPTION_KEY, String.class).get().setValue(email);
    }

    public String getEmailNotifications() {

	return getOption(E_MAIL_NOTIFICATIONS_OPTION_KEY, String.class).get().getValue();
    }

    public void setEmailNotifications(String notifications) {

	getOption(E_MAIL_NOTIFICATIONS_OPTION_KEY, String.class).get().setValue(notifications);
    }

    public String getAsynchDownloadName() {

	return getOption(ASYNCH_DOWNLOAD_NAME_OPTION_KEY, String.class).get().getValue();
    }

    public void setAsynchDownloadName(String name) {

	getOption(ASYNCH_DOWNLOAD_NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    public String getBucket() {

	return getOption(BUCKET_OPTION_KEY, String.class).get().getValue();
    }

    public void setBucket(String name) {

	getOption(BUCKET_OPTION_KEY, String.class).get().setValue(name);
    }
    
    public String getPublicURL() {

	return getOption(PUBLIC_URL_OPTION_KEY, String.class).get().getValue();
    }

    public void setPublicURL(String name) {

	getOption(PUBLIC_URL_OPTION_KEY, String.class).get().setValue(name);
    }

    public Integer getMaxDownloadSizeMB() {

	return getOption(MAX_DOWNLOAD_SIZE_MB_OPTION_KEY, Integer.class).get().getValue();
    }

    public void setMaxDownloadSizeMB(Integer mb) {

	getOption(MAX_DOWNLOAD_SIZE_MB_OPTION_KEY, Integer.class).get().setValue(mb);
    }

    public Integer getMaxDownloadPartSizeMB() {

	return getOption(MAX_DOWNLOAD_PART_SIZE_MB_OPTION_KEY, Integer.class).get().getValue();
    }

    public void setMaxDownloadPartSizeMB(Integer mb) {

	getOption(MAX_DOWNLOAD_PART_SIZE_MB_OPTION_KEY, Integer.class).get().setValue(mb);
    }

    public boolean getMetadataOnly() {

	String value = getOption(METADATA_ONLY_OPTION_KEY, String.class).get().getValue();
	return value != null && value.equalsIgnoreCase("true");
    }

    public void setMetadataOnly(boolean metadataOnly) {

	getOption(METADATA_ONLY_OPTION_KEY, String.class).get().setValue(metadataOnly ? "true" : "false");
    }

    /**
     * Saves the user who requested the download, for the users who can not be found again from the request URL (users authenticated
     * with a bearer token, which is not stored). The job then runs with the rights the user had when the download was requested.<br>
     * Only the user identity and its properties are saved, never a token.
     *
     * @param user
     */
    @SuppressWarnings("rawtypes")
    public void setUser(GSUser user) {

	JSONObject json = new JSONObject();

	json.put("identifier", user.getIdentifier());
	json.put("enabled", Boolean.TRUE.equals(user.isEnabled()));

	if (user.getRole() != null) {
	    json.put("role", user.getRole());
	}
	if (user.getAuthProvider() != null) {
	    json.put("authProvider", user.getAuthProvider());
	}

	JSONObject properties = new JSONObject();

	for (GSProperty property : user.getProperties()) {
	    if (property.getValue() instanceof String value) {
		properties.put(property.getName(), value);
	    }
	}

	json.put("properties", properties);

	getOption(USER_OPTION_KEY, String.class).get().setValue(json.toString());
    }

    /**
     * @return the user saved with {@link #setUser(GSUser)}; empty for the users found from the request URL, and for the jobs created
     *         before the user was saved
     */
    public Optional<GSUser> getUser() {

	Optional<String> value = getOption(USER_OPTION_KEY, String.class).flatMap(Option::getOptionalValue).filter(v -> !v.isBlank());

	if (value.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject json = new JSONObject(value.get());

	GSUser user = new GSUser();
	user.setIdentifier(json.optString("identifier", null));
	user.setEnabled(json.optBoolean("enabled", true));
	user.setRole(json.optString("role", null));
	user.setAuthProvider(json.optString("authProvider", null));

	JSONObject properties = json.optJSONObject("properties");

	if (properties != null) {
	    properties.keySet().forEach(name -> user.setPropertyValue(name, properties.getString(name)));
	}

	return Optional.of(user);
    }
}
