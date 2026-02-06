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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.setting.SystemSetting;
import org.json.JSONArray;
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
import eu.essi_lab.profiler.om.DownloadPartResult;
import eu.essi_lab.profiler.om.OMHandler;

/**
 * @author Fabrizio
 */
public class OMSchedulerWorker extends SchedulerWorker<OMSchedulerSetting> {

    static final String CONFIGURABLE_TYPE = "OMSchedulerWorker";

    public enum DownloadStatus {
	STARTED, CANCELED, PART_ENDED, ENDED
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	String requestURL = getSetting().getRequestURL();
	String operationId = getSetting().getOperationId();
	String asynchDownloadName = getSetting().getAsynchDownloadName();
	String bucket = getSetting().getBucket();
	String publicURL = getSetting().getPublicURL();
	Integer maxDownloadSizeMB = getSetting().getMaxDownloadSizeMB();
	Integer maxDownloadPartSizeMB = getSetting().getMaxDownloadPartSizeMB();

	String fname = asynchDownloadName + ".zip";

	DataDownloaderTool downloader = new DataDownloaderTool();

	S3TransferWrapper s3wrapper = null;

	if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() != DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	    s3wrapper = OMHandler.getS3TransferWrapper();
	}

	if (maxDownloadSizeMB == null) {
	    maxDownloadSizeMB = ConfigurationWrapper.getDefaultMaxDownloadSizeMB();
	    if (maxDownloadSizeMB == null) {
		maxDownloadSizeMB = 10;
	    }
	}
	if (maxDownloadPartSizeMB == null) {
	    maxDownloadPartSizeMB = ConfigurationWrapper.getDefaultMaxDownloadPartSizeMB();
	    if (maxDownloadPartSizeMB == null) {
		maxDownloadPartSizeMB = 1;
	    }
	}

	String resumptionToken = null;
	BigDecimal totalDownloadedSoFarMB = BigDecimal.ZERO;
	int partNumber = 0;
	boolean done = false;
	List<String> partLocators = new ArrayList<>();
	boolean resuming = false;

	if (s3wrapper != null) {
	    File statusFile = null;
	    try {
		statusFile = File.createTempFile("om-status-", ".json");
		if (s3wrapper.download(bucket, "data-downloads/" + operationId + "-status.json", statusFile)) {
		    String content = new String(Files.readAllBytes(statusFile.toPath()), StandardCharsets.UTF_8);
		    JSONObject saved = new JSONObject(content);
		    String savedStatus = saved.optString("status", "");
		    if ("PartCompleted".equals(savedStatus)) {
			String token = saved.optString("resumptionToken", null);
			if (token != null && !token.isEmpty()) {
			    resuming = true;
			    partNumber = saved.optInt("partNumber", 0);
			    resumptionToken = token;
			    if (saved.has("totalUncompressedSizeInMB")) {
				Object o = saved.get("totalUncompressedSizeInMB");
				if (o instanceof Number) {
				    totalDownloadedSoFarMB = BigDecimal.valueOf(((Number) o).doubleValue());
				} else if (o != null) {
				    totalDownloadedSoFarMB = new BigDecimal(o.toString());
				}
			    }
			    JSONArray locatorsArr = saved.optJSONArray("locators");
			    if (locatorsArr != null) {
				for (int i = 0; i < locatorsArr.length(); i++) {
				    partLocators.add(locatorsArr.getString(i));
				}
			    }
			    GSLoggerFactory.getLogger(getClass())
				    .info("OMSchedulerWorker RESUMING from part {} for operation id {}", partNumber + 1, operationId);
			}
		    }
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Could not load status for resume check: {}", e.getMessage());
	    } finally {
		if (statusFile != null && statusFile.exists()) {
		    statusFile.delete();
		}
	    }
	}

	String emailNotifications = getSetting().getEmailNotifications();
	String email = null;
	if (!resuming) {
	    GSLoggerFactory.getLogger(getClass()).info("OMSchedulerWorker STARTED, operation id {}", operationId);
	    if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
		email = getSetting().getEmail();
		OMDownloadReportsHandler.sendEmail(DownloadStatus.STARTED, setting, Optional.empty(), Optional.empty(), Optional.empty(),
			Optional.of(email));
	    }
	    OMDownloadReportsHandler.sendEmail(DownloadStatus.STARTED, setting, Optional.empty(), Optional.empty(), Optional.empty(),
		    Optional.empty());
	}

	while (!done) {

	    partNumber++;
	    String partFname = partNumber == 1 ? fname : asynchDownloadName + "_part" + partNumber + ".zip";

	    DownloadPartResult result = downloader.downloadPart(s3wrapper, bucket, requestURL, operationId, asynchDownloadName,
		    maxDownloadSizeMB, maxDownloadPartSizeMB, resumptionToken, totalDownloadedSoFarMB, partNumber, partLocators,status);

	    if (result == null || result.getPartFile() == null) {

		if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() != DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {
		    JSONObject msg = new JSONObject();
		    msg.put("id", operationId);
		    msg.put("status", "Canceled");
		    msg.put("downloadName", asynchDownloadName);
		    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

		    OMHandler.status(s3wrapper, bucket, operationId, msg);

		    if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
			email = getSetting().getEmail();
			OMDownloadReportsHandler.sendEmail(DownloadStatus.CANCELED, setting, Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.of(email));
		    }

		    OMDownloadReportsHandler.sendEmail(DownloadStatus.CANCELED, setting, Optional.empty(), Optional.empty(),
			    Optional.empty(), Optional.empty());
		}
		done = true;

	    } else {

		File partFile = result.getPartFile();
		BigDecimal sizeInMB = result.getSizeInMB() != null ? result.getSizeInMB() : BigDecimal.ZERO;
		BigDecimal partUncompressedMB = result.getUncompressedSizeInMB() != null ? result.getUncompressedSizeInMB() : sizeInMB;
		totalDownloadedSoFarMB = totalDownloadedSoFarMB.add(partUncompressedMB);

		if (ConfigurationWrapper.getDownloadSetting().getDownloadStorage() != DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

		    s3wrapper.uploadFile(partFile.getAbsolutePath(), bucket, "data-downloads/" + partFname, "application/zip");

		    String locator = publicURL + "/data-downloads/" + partFname;
		    JSONObject msg = new JSONObject();
		    msg.put("id", operationId);
		    if (result.isFinalPart()) {
			msg.put("status", result.isMaxSizeReached() ? "CompletedWithLimit" : "Completed");
			if (result.isMaxSizeReached() && result.getErrorMessage() != null) {
			    msg.put("message", result.getErrorMessage());
			}
		    } else {
			int fileCount = result.getDownloadedFileNames() != null ? result.getDownloadedFileNames().size() : 0;
			String partSizeStr = result.getUncompressedSizeInMB() != null ? result.getUncompressedSizeInMB().toString() : "?";
			msg.put("status", "PartCompleted");
			msg.put("statusMessage",
				"Part " + partNumber + " ready: " + fileCount + " files, " + partSizeStr + " MB (more parts in progress)");
			msg.put("statusMessageKey", "status_message_part_ready_more_in_progress");
			JSONObject statusParams = new JSONObject();
			statusParams.put("part", partNumber);
			statusParams.put("fileCount", fileCount);
			statusParams.put("sizeMb", partSizeStr);
			msg.put("statusMessageParams", statusParams);
			if (result.getResumptionToken() != null && !result.getResumptionToken().isEmpty()) {
			    msg.put("resumptionToken", result.getResumptionToken());
			}
		    }
		    msg.put("downloadName", asynchDownloadName);
		    partLocators.add(locator);
		    msg.put("locators", new JSONArray(partLocators));
		    msg.put("sizeInMB", sizeInMB);
		    msg.put("totalUncompressedSizeInMB", totalDownloadedSoFarMB);
		    msg.put("partNumber", partNumber);
		    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

		    OMHandler.status(s3wrapper, bucket, operationId, msg);

		    status.clearMessages();
		    status.addInfoMessage(totalDownloadedSoFarMB + " MB downloaded");

		    Optional<String> errorMsg = result.isMaxSizeReached() && result.getErrorMessage() != null ? Optional.of(
			    result.getErrorMessage()) : Optional.empty();
		    StringBuilder partDetailsBuilder = new StringBuilder();
		    partDetailsBuilder.append("Part number: ").append(partNumber).append("\n");
		    partDetailsBuilder.append("Part total size: ").append(result.getUncompressedSizeInMB()).append(" MB");
		    if (result.getUncompressedSizeInMB() != null) {
			partDetailsBuilder.append(" (compressed to ").append(sizeInMB).append(" MB)");
		    }
		    partDetailsBuilder.append("\n");
		    partDetailsBuilder.append("Part is final: ").append(result.isFinalPart() ? "yes" : "no").append("\n");
		    if (result.getDownloadedFileNames() != null && !result.getDownloadedFileNames().isEmpty()) {
			partDetailsBuilder.append("Downloaded files:\n");
			for (String fileName : result.getDownloadedFileNames()) {
			    partDetailsBuilder.append("  ").append(fileName).append("\n");
			}
		    }
		    partDetailsBuilder.append("\n");
		    Optional<String> partDetails = Optional.of(partDetailsBuilder.toString());
		    DownloadStatus stat = result.isFinalPart() ? DownloadStatus.ENDED : DownloadStatus.PART_ENDED;
		    if (emailNotifications != null && emailNotifications.toLowerCase().equals("true")) {
			email = getSetting().getEmail();
			OMDownloadReportsHandler.sendEmail(stat, setting, Optional.of(locator), errorMsg, partDetails, Optional.of(email));
		    }

		    OMDownloadReportsHandler.sendEmail(stat, setting, Optional.of(locator), errorMsg, partDetails, Optional.empty());
		}

		partFile.delete();

		resumptionToken = result.getResumptionToken();
		if (resumptionToken == null) {
		    done = true;
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("OMSchedulerWorker ENDED, operation id {}", operationId);
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
