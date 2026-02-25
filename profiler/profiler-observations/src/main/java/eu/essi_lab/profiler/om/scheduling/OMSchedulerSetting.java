/**
 * 
 */
package eu.essi_lab.profiler.om.scheduling;

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

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

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

}
