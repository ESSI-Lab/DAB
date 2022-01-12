package eu.essi_lab.request.executor.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class GWPSDownloader extends DirectDownloader {

    private long pollInterval = 10000;

    public long getPollInterval() {
	return pollInterval;
    }
    public void setPollInterval(long interval) {
	this.pollInterval = interval;
    }

    public GWPSDownloader(String linkage) {
	super(linkage);

    }

    public String download() {
	Downloader downloader = new Downloader();
	Optional<String> optionalString = downloader.downloadString(linkage);
	if (optionalString.isPresent()) {
	    String string = optionalString.get();
	    String statusLocation = extractStatusLocation(string);

	    long start = System.currentTimeMillis();

	    while (true) {

		long time = System.currentTimeMillis() - start;
		if (time > (60000l * 60l)) {
		    String error = "[BULK] Timeout for bulk download operation";
		    GSLoggerFactory.getLogger(getClass()).error(error);
		    throw new RuntimeException(error);
		}

		try {
		    Thread.sleep(pollInterval);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}

		Optional<String> optionalStatus = downloader.downloadString(statusLocation);
		if (optionalStatus.isPresent()) {
		    String status = optionalStatus.get();
		    System.out.println(status);
		    if (status.contains("COMPLETED")) {
			GSLoggerFactory.getLogger(getClass()).info("Successful download operation");
			String ret = extractCompletedLocation(status);
			return ret;
		    } else if (status.contains("FAILED")) {

			String msg = "Failed download operation";

			GSLoggerFactory.getLogger(getClass()).info(msg);

			throw new RuntimeException(msg);
		    } else {
			GSLoggerFactory.getLogger(getClass()).info("Download operation in progress");
		    }
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Failed status request");

		}

	    }

	} else {

	    String msg = "Failed download request";

	    GSLoggerFactory.getLogger(getClass()).info(msg);

	    throw new RuntimeException(msg);

	}

    }

    private String extractStatusLocation(String string) {
	string = string.substring(string.indexOf("statusLocation=\""));
	string = string.replace("statusLocation=\"", "");
	string = string.substring(0, string.indexOf('\"'));
	return string;

    }

    private String extractCompletedLocation(String string) {
	string = string.substring(string.indexOf("data\""));
	string = string.replace("data\"", "");
	string = string.substring(string.indexOf('\"') + 1);
	string = string.substring(0, string.indexOf('\"'));
	return string;

    }

}
