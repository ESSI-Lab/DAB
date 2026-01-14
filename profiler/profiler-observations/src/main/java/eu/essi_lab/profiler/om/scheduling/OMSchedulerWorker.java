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

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.om.DataDownloaderTool;
import eu.essi_lab.profiler.om.OMHandler;

/**
 * @author Fabrizio
 */
public class OMSchedulerWorker extends SchedulerWorker<OMSchedulerSetting> {

    static final String CONFIGURABLE_TYPE = "OMSchedulerWorker";

    public enum DownloadStatus {
	STARTED, CANCELED, ENDED
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	String emailNotifications = getSetting().getEmailNotifications();
	String email = null;
	if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
	    email = getSetting().getEmail();
	    OMDownloadReportsHandler.sendEmail(DownloadStatus.STARTED, setting, Optional.empty(), Optional.of(email));
	}

	OMDownloadReportsHandler.sendEmail(DownloadStatus.STARTED, setting, Optional.empty(), Optional.empty());

	String requestURL = getSetting().getRequestURL();
	String operationId = getSetting().getOperationId();
	String asynchDownloadName = getSetting().getAsynchDownloadName();
	String bucket = getSetting().getBucket();
	String publicURL = getSetting().getPublicURL();

	String fname = asynchDownloadName + ".zip";

	DataDownloaderTool downloader = new DataDownloaderTool();

	S3TransferWrapper s3wrapper = null;

	if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() != DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	    s3wrapper = OMHandler.getS3TransferWrapper();
	}

	File downloaded = downloader.download(s3wrapper, bucket, requestURL, operationId, asynchDownloadName);

	String locator = null;

	if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() == DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	} else {

	    if (downloaded == null) {

		JSONObject msg = new JSONObject();
		msg.put("id", operationId);
		msg.put("status", "Canceled");
		msg.put("downloadName", asynchDownloadName);
		msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

		OMHandler.status(s3wrapper, bucket, operationId, msg);

		if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
		    email = getSetting().getEmail();
		    OMDownloadReportsHandler.sendEmail(DownloadStatus.CANCELED, setting, Optional.empty(), Optional.of(email));
		}

		OMDownloadReportsHandler.sendEmail(DownloadStatus.CANCELED, setting, Optional.empty(), Optional.empty());

	    } else {

		s3wrapper.uploadFile(downloaded.getAbsolutePath(), bucket, "data-downloads/" + fname, "application/zip");

		long sizeInBytes = downloaded.length();
		BigDecimal sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.UP);

		downloaded.delete();

		locator = publicURL + "/data-downloads/" + fname;
		JSONObject msg = new JSONObject();
		msg.put("id", operationId);
		msg.put("status", "Completed");
		msg.put("downloadName", asynchDownloadName);
		msg.put("locator", locator);
		msg.put("sizeInMB", sizeInMB);
		msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

		OMHandler.status(s3wrapper, bucket, operationId, msg);

		if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
		    email = getSetting().getEmail();
		    OMDownloadReportsHandler.sendEmail(DownloadStatus.ENDED, setting, Optional.of(locator), Optional.of(email));
		}

		OMDownloadReportsHandler.sendEmail(DownloadStatus.ENDED, setting, Optional.of(locator), Optional.empty());
	    }
	}
    }

    @Override
    protected OMSchedulerSetting initSetting() {

	return new OMSchedulerSetting();
    }

    @Override
    protected void storeJobStatus(SchedulerJobStatus status) throws GSException {

	SchedulerViewSetting setting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(setting);

	try {
	    scheduler.setJobStatus(status);

	} catch (SQLException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store status");
	    GSLoggerFactory.getLogger(getClass()).error(status.toString());
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }
}
