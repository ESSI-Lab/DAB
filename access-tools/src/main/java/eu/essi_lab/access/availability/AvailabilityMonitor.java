package eu.essi_lab.access.availability;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class AvailabilityMonitor {

    private static AvailabilityMonitor instance = null;
    private static Optional<S3TransferWrapper> manager = null;

    private AvailabilityMonitor() {

    }

    public static AvailabilityMonitor getInstance() {
	if (instance == null) {
	    instance = new AvailabilityMonitor();
	}
	return instance;
    }

    public synchronized Optional<S3TransferWrapper> getS3TransferManager() {

	if (this.manager == null || this.manager.isEmpty()) {

	    this.manager = ConfigurationWrapper.getS3TransferManager();
	}

	return this.manager;
    }

    public DownloadInformation getLastDownloadDate(boolean good, String sourceId) {
	String status = "good";
	if (!good) {
	    status = "bad";
	}
	GSLoggerFactory.getLogger(getClass()).info("asked for last download platform for source {}", sourceId);
	getS3TransferManager();
	if (manager.isPresent()) {
	    try {
		Date downloaded = manager.get().getObjectDate("dabreporting", "download-availability/" + sourceId + "-" + status + ".txt");
		if (downloaded != null) {
		    return new DownloadInformation(true, downloaded, null);
		} else {
		    return null;
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("S3 manager is needed");
	}
	return null;
    }

    public DownloadInformation getLastDownloadDate(String sourceId) {
	return getLastDownloadDate(true, sourceId);
    }

    public DownloadInformation getLastFailedDownloadDate(String sourceId) {
	return getLastDownloadDate(false, sourceId);
    }

    public DownloadInformation getLastDownloadInformation(boolean good, String sourceId) {
	String status = "good";
	if (!good) {
	    status = "bad";
	}
	GSLoggerFactory.getLogger(getClass()).info("asked for last download platform for source {}", sourceId);
	getS3TransferManager();
	if (manager.isPresent()) {
	    try {
		File tmp = File.createTempFile(getClass().getSimpleName(), ".txt");

		boolean downloaded = manager.get().download("dabreporting", "download-availability/" + sourceId + "-" + status + ".txt",
			tmp);
		if (downloaded) {
		    try (BufferedReader br = new BufferedReader(new FileReader(tmp))) {
			String date = br.readLine();
			String platformId = br.readLine();
			tmp.delete();
			return new DownloadInformation(true, ISO8601DateTimeUtils.parseISO8601ToDate(date).get(), platformId);
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		} else {
		    if (tmp.exists()) {
			tmp.delete();
		    }
		    return null;
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("S3 manager is needed");
	}
	return null;
    }

    public DownloadInformation getLastDownloadInformation(String sourceId) {
	return getLastDownloadInformation(true, sourceId);
    }

    public DownloadInformation getLastFailedDownloadInformation(String sourceId) {
	return getLastDownloadInformation(false, sourceId);
    }

}
