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

import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;

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
	    String userMail) {

	SchedulingGroup group = setting.getGroup();

	String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + "[" + group.name() + "]" + "[" + status + "]";

	StringBuilder builder = new StringBuilder();

	builder.append("OM asynch download ");
	builder.append(status.toLowerCase() + "\n\n");

	builder.append("Request URL: " + setting.getRequestURL() + "\n\n");
	builder.append("Operation ID: " + setting.getOperationId());

	if (locator.isPresent()) {

	    builder.append("\n\nZIP file: " + locator.get());
	}
	// send to default recipients
	ConfiguredGmailClient.sendEmail(subject, builder.toString());
	// send as well to the user if needed
	if (userMail != null && userMail.contains("@")) {
	    ConfiguredGmailClient.sendEmail(subject, builder.toString(), userMail);
	}
    }
}
