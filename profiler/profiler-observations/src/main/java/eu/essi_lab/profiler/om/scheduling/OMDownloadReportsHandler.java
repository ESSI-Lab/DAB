package eu.essi_lab.profiler.om.scheduling;

import java.net.URI;

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

import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.KeyValueParser;

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
	    String status, //
	    OMSchedulerSetting setting, //
	    Optional<String> locator, //
	    Optional<String> userMail) {

	SchedulingGroup group = setting.getGroup();

	String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + "[" + group.name() + "]" + "[" + status + "]";

	StringBuilder builder = new StringBuilder();

	builder.append("Bulk download ");
	builder.append(status.toLowerCase() + "\n\n");

	builder.append("Download name: " + setting.getAsynchDownloadName() + "\n\n");

	builder.append("Operation ID: " + setting.getOperationId());

	try {
	    String requestURL = setting.getRequestURL();

	    KeyValueParser parser = new KeyValueParser(new URI(requestURL).toURL().getQuery());

	    Optional<String> obsProperty = parser.getOptionalValue("observedProperty");
	    if (obsProperty.isPresent()) {

		builder.append("\n\nObserved property: " + obsProperty.get());
	    }

	    Optional<String> west = parser.getOptionalValue("west");
	    Optional<String> south = parser.getOptionalValue("south");
	    Optional<String> east = parser.getOptionalValue("east");
	    Optional<String> north = parser.getOptionalValue("north");

	    if (west.isPresent() && south.isPresent() && east.isPresent() && north.isPresent()) {

		builder.append("\n\nSpatial extent (south, west, north, east): ");
		builder.append(south.get() + ", ");
		builder.append(west.get() + ", ");
		builder.append(north.get() + ", ");
		builder.append(east.get());
	    }

	    Optional<String> begin = parser.getOptionalValue("beginPosition");
	    Optional<String> end = parser.getOptionalValue("endPosition");

	    if (begin.isPresent()) {

		builder.append("\n\nBegin time: " + begin.get());
	    }

	    if (end.isPresent()) {

		builder.append("\n\nEnd time: " + end.get());
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(OMDownloadReportsHandler.class).error(e);
	}

	if (locator.isPresent()) {

	    builder.append("\n\nZIP file: " + locator.get());
	}

	if (userMail.isPresent()) {

	    ConfiguredGmailClient.sendEmail(subject, builder.toString(), userMail.get());

	} else {

	    builder.append("\n\nRequest URL: " + setting.getRequestURL().substring(0, setting.getRequestURL().indexOf("?") + 1) + "\n\n");

	    ConfiguredGmailClient.sendEmail(subject, builder.toString());
	}
    }
}
