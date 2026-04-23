package eu.essi_lab.request.executor.impl.access;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.lib.net.downloader.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.JobStatus.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import org.json.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

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

	    @SuppressWarnings("rawtypes")
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

		JSONObject content = null;

		try {

		    InputStream binary = getCacheFolder().getBinary(jobId);

		    if (binary != null) {

			content = new JSONObject(new String(binary.readAllBytes()));
		    }

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

		if (content != null) {

		    SchedulerJobStatus jobStatus = new SchedulerJobStatus(content);
		    JobPhase phase = jobStatus.getPhase();

		    switch (phase) {
		    case COMPLETED:

			GSLoggerFactory.getLogger(getClass()).info("Successful download operation");
			return jobStatus.getDataUri().orElse("missing");

		    case ERROR:

			String msg = String.join(",", jobStatus.getErrorMessages());

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
     * @return
     * @throws GSException
     */
    private DatabaseFolder getCacheFolder() throws GSException {

	StorageInfo storageInfo = ConfigurationWrapper.getStorageInfo();

	return DatabaseFactory.get(storageInfo).getCacheFolder();
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
