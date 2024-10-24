/**
 * 
 */
package eu.essi_lab.harvester;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class HarvestingReportsHandler {

    private static boolean enabled;

    private SourceStorage sourceStorage;
    private GSSource gsSource;

    /**
     * @param gsSource
     * @param sourceStorage
     */
    public HarvestingReportsHandler(GSSource gsSource, SourceStorage sourceStorage) {

	this.gsSource = gsSource;
	this.sourceStorage = sourceStorage;
    }

    /**
    * 
    */
    void sendErrorAndWarnMessageEmail() {

	if (!enabled) {

	    return;
	}

	List<String> errorsReport;
	List<String> warnReport;

	try {
	    errorsReport = sourceStorage.retrieveErrorsReport(gsSource);
	    warnReport = sourceStorage.retrieveWarnReport(gsSource);

	    StringBuilder builder = new StringBuilder();

	    builder.append("Problems occurred during harvesting of source: " + gsSource.getLabel());
	    builder.append("\n");
	    builder.append("Source endpoint: " + gsSource.getEndpoint());
	    builder.append("\n");
	    builder.append("Source id: " + gsSource.getUniqueIdentifier());
	    builder.append("\n\n");

	    if (!errorsReport.isEmpty()) {

		builder.append(errorsReport.stream().collect(Collectors.joining("\n")));

		String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + ConfiguredGmailClient.MAIL_HARVESTING_SUBJECT
			+ ConfiguredGmailClient.MAIL_ERROR_SUBJECT;

		ConfiguredGmailClient.sendEmail(subject, builder.toString());
	    }

	    if (!warnReport.isEmpty()) {

		builder.append(warnReport.stream().collect(Collectors.joining("\n")));
		String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + ConfiguredGmailClient.MAIL_HARVESTING_SUBJECT
			+ ConfiguredGmailClient.MAIL_WARNING_SUBJECT;

		ConfiguredGmailClient.sendEmail(subject, builder.toString());
	    }

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve the errors/warn report");
	}
    }

    /**
     * @param start
     * @param source
     * @param isRecovering
     * @param resumed
     * @param harvestingProperties
     * @throws GSException
     */
    void sendHarvestingEmail(//
	    boolean start, //
	    GSSource source, //
	    Boolean isRecovering, //
	    boolean resumed, //
	    List<String> report, //
	    HarvestingProperties harvestingProperties) {

	if (!enabled) {

	    return;
	}

	String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + ConfiguredGmailClient.MAIL_HARVESTING_SUBJECT
		+ (start ? "[STARTED]" : "[ENDED]");

	String message = "Label: " + source.getLabel() + "\n";
	message += "Endpoint: " + source.getEndpoint() + "\n";
	message += "Id: " + source.getUniqueIdentifier() + "\n";
	message += "Recovering: " + isRecovering + "\n";
	message += "Resumed: " + resumed + "\n";
	message += "Host: " + HostNamePropertyUtils.getHostNameProperty() + "\n";

	if (Objects.nonNull(harvestingProperties)) {

	    String startTime = harvestingProperties.getStartHarvestingTimestamp();
	    String endTime = harvestingProperties.getEndHarvestingTimestamp();
	    int harvCount = harvestingProperties.getHarvestingCount();
	    int resourcesCount = harvestingProperties.getResourcesCount();

	    if (harvCount > 0) {
		message += "Start time: " + startTime + "\n";
		message += "End time: " + endTime + "\n";
		message += "Harvesting #: " + harvCount + "\n";
		message += "Resources #: " + resourcesCount + "\n";
	    }
	}

	if (report != null) {
	    message += "-- Source storage report STARTED ---\n";
	    for (String r : report) {

		message += r + "\n";
	    }
	    message += "-- Source storage report ENDED ---\n";
	}

	message += "--- \n";

	ConfiguredGmailClient.sendEmail(subject, message);
    }

    /**
     * @param ex
     */
    void gatherConflictingResourceException(ConflictingResourceException ex) {

	if (!enabled) {

	    return;
	}

	List<String> originalIds = ex.getOriginalIds();
	List<GSSource> currentSources = ex.getIncomingSources();
	List<GSSource> existingSources = ex.getExistingSources();

	StringBuilder builder = new StringBuilder();

	for (int i = 0; i < originalIds.size(); i++) {

	    builder.append("--- CONFLICTING RESOURCE FOUND ---");
	    builder.append("\n");

	    builder.append("Original id: " + originalIds.get(i));
	    builder.append("\n");

	    builder.append("Incoming source: " + currentSources.get(i));
	    builder.append("\n");

	    builder.append("Existing source: " + existingSources.get(i));
	    builder.append("\n");
	}

	update(builder.toString());

	GSLoggerFactory.getLogger(getClass()).error(builder.toString());
    }

    /**
     * @param hce
     */
    void gatherHarvesterComponentException(HarvestingComponentException hce) {

	if (!enabled) {

	    return;
	}

	gatherGSException(hce.getException());
    }

    /**
     * @param exception
     */
    void gatherGSException(GSException exception) {

	if (!enabled) {

	    return;
	}

	List<ErrorInfo> list = exception.getErrorInfoList();

	if (Objects.isNull(list)) {
	    return;
	}

	list.forEach(info -> {

	    Throwable cause = info.getCause();

	    StringBuilder builder = new StringBuilder();
	    builder.append("Error id: " + info.getErrorId());
	    builder.append("\n");

	    String errorDescription = info.getErrorDescription();
	    if (Objects.nonNull(errorDescription) && !errorDescription.isEmpty()) {
		builder.append("Error msg: " + info.getErrorDescription());
		builder.append("\n");
	    }

	    if (Objects.nonNull(cause)) {

		if (cause instanceof GSException) {

		    GSException internalEx = (GSException) cause;
		    gatherGSException(internalEx);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		cause.printStackTrace(new PrintStream(outputStream));
		try {

		    builder.append("\n");
		    builder.append(outputStream.toString("UTF-8"));

		} catch (UnsupportedEncodingException e) {
		}
	    }

	    update(builder.toString());
	});
    }

    /**
     * @param rex
     * @param firstHarvesting
     */
    void gatherDuplicatedResourceException(DuplicatedResourceException rex) {

	if (!enabled) {

	    return;
	}

	String originalId = rex.getOriginalId();
	GSResource incomingResource = rex.getIncomingResource();
	GSResource existingResource = rex.getExistingResource();

	String incomingOriginal = incomingResource.getOriginalMetadata().getMetadata();
	String existingOriginal = existingResource.getOriginalMetadata().getMetadata();

	GSSource source = incomingResource.getSource();
	int duplicationCase = rex.getDuplicationCase();

	StringBuilder builder = new StringBuilder();

	builder.append("--- DUPLICATED RESOURCE FOUND ---");
	builder.append("\n");

	builder.append("Duplication case: " + duplicationCase);
	builder.append("\n");

	builder.append("Original metadata equals: " + incomingOriginal.equals(existingOriginal));
	builder.append("\n");

	builder.append("Original id: " + originalId);
	builder.append("\n");

	builder.append("Source: " + source);
	builder.append("\n");

	update(builder.toString());

	GSLoggerFactory.getLogger(getClass()).warn("Skipping duplicated resource [" + originalId + "] from source [" + source + "]");
    }

    /**
     * @param message
     */
    private void update(String message) {

	try {
	    sourceStorage.updateErrorsAndWarnReport(gsSource, message);

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to update the error harvesting report");

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * 
     */
    public static void enable() {

	enabled = true;
    }

}
