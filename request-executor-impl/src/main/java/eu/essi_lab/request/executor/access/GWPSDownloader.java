package eu.essi_lab.request.executor.access;

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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.DriverFactory;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;

public class GWPSDownloader extends DirectDownloader {

    /**
     * 
     */
    private static final long POLL_INTERVAL = 10000;
    /**
     * 
     */
    private static final long TIMEOUT = TimeUnit.HOURS.toMillis(1);

    /**
     * @param linkage
     */
    public GWPSDownloader(String linkage) {

	super(linkage);
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    public String download() {

	Downloader downloader = new Downloader();

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from {} STARTED", linkage);

	Optional<String> optionalString = downloader.downloadOptionalString(linkage);

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from {} ENDED", linkage);

	if (optionalString.isPresent()) {

	    String string = optionalString.get();
	    // String statusLocation = extractStatusLocation(string);

	    GSLoggerFactory.getLogger(getClass()).trace("Extracting job ID from {} STARTED", string);

	    String jobId = extractJobId(string);

	    GSLoggerFactory.getLogger(getClass()).trace("Extracting job ID ENDED", string);

	    GSLoggerFactory.getLogger(getClass()).trace("Job ID: {}", jobId);

	    SharedPersistentDriverSetting driverSetting = ConfigurationWrapper.getSharedPersistentDriverSetting();
	    @SuppressWarnings("rawtypes")
	    ISharedRepositoryDriver driver = DriverFactory.getConfiguredDriver(driverSetting, true);

	    long start = System.currentTimeMillis();

	    while (true) {

		long time = System.currentTimeMillis() - start;

		if (time > TIMEOUT) {

		    String error = "[BULK] 1 hour timeout for bulk download operation";
		    GSLoggerFactory.getLogger(getClass()).error(error);

		    throw new RuntimeException(error);
		}

		try {
		    Thread.sleep(POLL_INTERVAL);
		} catch (InterruptedException e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
		}

		SharedContent<JSONObject> sharedContent = null;

		try {
		    sharedContent = driver.read(jobId, SharedContentType.JSON_TYPE);

		} catch (GSException e) {

		    String errors = e.getErrorInfoList().stream().map(i -> i.getErrorDescription()).filter(Objects::nonNull)
			    .collect(Collectors.joining(","));

		    if (!errors.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).error("Following errors detected: {}", errors);
		    }

		    //
		    // since valid codes are until 399, ES driver throws an exception if a resource is not found, error code 404
		    // but here is normal when a job status is not found, since the job is still running and its status is
		    // not yet stored in the ES, so there is no reason to throw an exception
		    // simply another attempt will be done, until the status is ready
		    //
		    // throw new RuntimeException(errors);
		}

		if (sharedContent != null) {

		    SchedulerJobStatus jobStatus = new SchedulerJobStatus(sharedContent.getContent());
		    JobPhase phase = jobStatus.getPhase();
		    switch (phase) {
		    case COMPLETED:
			GSLoggerFactory.getLogger(getClass()).info("Successful download operation");
			String ret = jobStatus.getDataUri().orElse("missing");
			return ret;
		    case ERROR:
			String msg = jobStatus.getErrorMessages().stream().collect(Collectors.joining(","));
			if (msg.isEmpty()) {
			    msg = "Failed download operation";
			}

			GSLoggerFactory.getLogger(getClass()).info(msg);

			throw new RuntimeException(msg);
		    }
		} else {

		    GSLoggerFactory.getLogger(getClass()).info("Download operation in progress");
		}
	    }
	} else {

	    String msg = "Failed download request";

	    GSLoggerFactory.getLogger(getClass()).info(msg);

	    throw new RuntimeException(msg);
	}
    }

    /**
     * @param string
     * @return
     */
    private String extractJobId(String string) {

	String statusLocation = extractStatusLocation(string);

	return statusLocation.substring(statusLocation.lastIndexOf("/") + 1, statusLocation.length());
    }

    /**
     * @param string
     * @return
     */
    private String extractStatusLocation(String string) {
	string = string.substring(string.indexOf("statusLocation=\""));
	string = string.replace("statusLocation=\"", "");
	string = string.substring(0, string.indexOf('\"'));
	return string;
    }
}
