/**
 * 
 */
package eu.essi_lab.profiler.om.scheduling;

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

import java.io.File;
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

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	OMDownloadReportsHandler.sendEmail("STARTED", setting, Optional.empty());

	String requestURL = getSetting().getRequestURL();
	String operationId = getSetting().getOperationId();

	String fname = operationId + ".zip";

	DataDownloaderTool downloader = new DataDownloaderTool();

	File downloaded = downloader.download(requestURL, operationId);

	String locator = null;

	if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() == DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	} else {

	    S3TransferWrapper s3wrapper = OMHandler.getS3TransferWrapper();

	    s3wrapper.uploadFile(downloaded.getAbsolutePath(), "his-central", "data-downloads/" + fname, "application/zip");

	    downloaded.delete();

	    locator = "https://his-central.s3.us-east-1.amazonaws.com/data-downloads/" + fname;

	    JSONObject msg = new JSONObject();
	    msg.put("operationId", operationId);
	    msg.put("status", "Completed");
	    msg.put("locator", locator);
	    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

	    OMHandler.status(s3wrapper, operationId, msg);
	}

	OMDownloadReportsHandler.sendEmail("ENDED", setting, Optional.of(locator));
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
