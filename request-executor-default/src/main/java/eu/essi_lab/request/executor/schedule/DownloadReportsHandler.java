package eu.essi_lab.request.executor.schedule;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.model.resource.data.DataReferences;

/**
 * @author Fabrizio
 */
public class DownloadReportsHandler {

    private static boolean enabled;

    /**
     * 
     */
    public DownloadReportsHandler() {
    }

    /**
     * 
     */
    public static void enable() {

	enabled = true;
    }

    /**
     * @param status
     * @param setting
     * @param resultStorageURI
     */
    public synchronized static void sendBulkDownloadEmail(//
	    String status, //
	    UserScheduledSetting setting, //
	    Optional<String> resultStorageURI) {

	if (!enabled) {

	    return;
	}

	SchedulingGroup group = setting.getGroup();

	String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + "[" + group.name() + "]" + "[" + status + "]";
	//
	StringBuilder builder = new StringBuilder();

	builder.append(group == SchedulingGroup.ASYNCH_ACCESS ? "Asynch access " : "Bulk download ");
	builder.append(status.toLowerCase() + ".\n\n");

	builder.append("References: \n\n");

	BulkDownloadMessage requestMessage = (BulkDownloadMessage) setting.getRequestMessage();

	DataReferences dataReferences = requestMessage.getDataReferences();

	dataReferences.getReferences().forEach(ref -> {

	    builder.append("Title: " + ref.getTitle() + "\n");
	    builder.append("Link: " + ref.getLinkage() + "\n");
	    builder.append("\n");
	});

	resultStorageURI.ifPresent(uri -> builder.append("Storage uri: " + uri + "\n"));

	ConfiguredGmailClient.sendEmail(subject, builder.toString());
    }
}
