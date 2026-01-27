package eu.essi_lab.profiler.om.scheduling;

import java.net.URI;

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

import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfiguredSMTPClient;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.profiler.om.scheduling.OMSchedulerWorker.DownloadStatus;

/**
 * @author Fabrizio
 */
public class OMDownloadReportsHandler {

    /**
     * 
     */
    public OMDownloadReportsHandler() {
    }

    /**
     * @param status
     * @param setting
     */
    public static void sendEmail(//
	    DownloadStatus status, //
	    OMSchedulerSetting setting, //
	    Optional<String> locator, //
	    Optional<String> userMail) {

	boolean toUser = userMail.isPresent();

	SchedulingGroup group = setting.getGroup();

	String subject;
	if (toUser) {
	    subject = "[HIS-Central][Download " + status.name().toLowerCase() + "] " + setting.getAsynchDownloadName();
	} else {
	    subject = ConfiguredSMTPClient.MAIL_REPORT_SUBJECT + "[" + group.name() + "]" + "[" + status + "] "
		    + setting.getAsynchDownloadName();
	}

	StringBuilder builder = new StringBuilder();

	builder.append("Bulk download ");
	builder.append(status.name().toLowerCase() + "\n\n");

	builder.append("Download name: " + setting.getAsynchDownloadName() + "\n\n");

	builder.append("Operation ID: " + setting.getOperationId()+"\n\n");

	try {
	    String requestURL = setting.getRequestURL();

	    KeyValueParser parser = new KeyValueParser(new URI(requestURL).toURL().getQuery());

	    Optional<String> obsProperty = parser.getOptionalValue("observedProperty");

	    String parameters = "";

	    if (obsProperty.isPresent()) {

		parameters+= "Observed property: " + obsProperty.get()+"\n\n";
	    }

	    Optional<String> west = parser.getOptionalValue("west");
	    Optional<String> south = parser.getOptionalValue("south");
	    Optional<String> east = parser.getOptionalValue("east");
	    Optional<String> north = parser.getOptionalValue("north");

	    if (west.isPresent() && south.isPresent() && east.isPresent() && north.isPresent()) {

		parameters+="Spatial extent (south, west, north, east): \n";
		parameters+=south.get() + ", \n";
		parameters+=west.get() + ", \n";
		parameters+=north.get() + ", \n";
		parameters+=east.get()+"\n\n";
	    }

	    Optional<String> begin = parser.getOptionalValue("beginPosition");
	    Optional<String> end = parser.getOptionalValue("endPosition");

	    if (begin.isPresent()) {

		parameters+="Begin time: " + begin.get()+"\n\n";
	    }

	    if (end.isPresent()) {

		parameters+="End time: " + end.get()+"\n\n";
	    }

	    if (parameters.isEmpty()) {
		parameters = "No parameters!\n\n";
	    }else{
		parameters = "Parameters:\n\n"+parameters;
	    }

	    builder.append(parameters);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(OMDownloadReportsHandler.class).error(e);
	}



	if (locator.isPresent()) {

	    builder.append("ZIP file: " + locator.get()+"\n\n");
	}

	if (toUser) {

	    ConfiguredSMTPClient.sendEmail(subject, builder.toString(), userMail.get());

	} else {

	    builder.append(
		    "Request parameters: " + setting.getRequestURL().substring(setting.getRequestURL().indexOf("?") + 1) + "\n\n");

	    ConfiguredSMTPClient.sendEmail(subject, builder.toString());
	}
    }
}
