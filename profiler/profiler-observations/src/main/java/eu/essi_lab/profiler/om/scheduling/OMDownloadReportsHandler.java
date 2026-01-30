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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfiguredSMTPClient;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
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
     * @param locator
     * @param errorMessage optional message (e.g. when max download size reached)
     * @param partDetails  optional part info (part number, size, final part, downloaded files)
     * @param userMail
     */
    public static void sendEmail(//
	    DownloadStatus status, //
	    OMSchedulerSetting setting, //
	    Optional<String> locator, //
	    Optional<String> errorMessage, //
	    Optional<String> partDetails, //
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

	    Optional<String> provider = parser.getOptionalValue("provider");
	    if (provider.isPresent()) {

		parameters+="Provider/Source: " + provider.get()+"\n\n";
	    }

	    Optional<String> featureName = parser.getOptionalValue("featureName");
	    if (featureName.isPresent()) {

		parameters+="Station/Platform name: " + featureName.get()+"\n\n";
	    }

	    if (obsProperty.isPresent()) {
		String uri = URLDecoder.decode(obsProperty.get(), StandardCharsets.UTF_8);
		if (uri.contains(HISCentralOntology.HIS_CENTRAL_BASE_URI)){
		    HISCentralOntology ontology = new HISCentralOntology();
		    SKOSConcept concept = ontology.getConcept(uri);
		    if (concept!=null){
			uri = uri+" ("+concept.getPreferredLabel("en")+")";
		    }
		}
		parameters+= "Observed property: " + uri +"\n\n";
	    }

	    Optional<String> west = parser.getOptionalValue("west");
	    Optional<String> south = parser.getOptionalValue("south");
	    Optional<String> east = parser.getOptionalValue("east");
	    Optional<String> north = parser.getOptionalValue("north");

	    Optional<String> predefinedLayer = parser.getOptionalValue("predefinedLayer");
	    if (predefinedLayer.isPresent()) {

		parameters+="Predefined spatial extent: " + predefinedLayer.get()+"\n\n";
	    }

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

	    Optional<String> aggregationDuration = parser.getOptionalValue("aggregationDuration");
	    if (aggregationDuration.isPresent()) {

		parameters+="Aggregation duration: " + aggregationDuration.get()+"\n\n";
	    }

	    Optional<String> intendedObservationSpacing = parser.getOptionalValue("intendedObservationSpacing");
	    if (intendedObservationSpacing.isPresent()) {

		parameters+="Intended observation spacing: " + intendedObservationSpacing.get()+"\n\n";
	    }

	    Optional<String> timeInterpolation = parser.getOptionalValue("timeInterpolation");
	    if (timeInterpolation.isPresent()) {

		parameters+="Time interpolation: " + timeInterpolation.get()+"\n\n";
	    }

	    Optional<String> ontology = parser.getOptionalValue("ontology");
	    if (ontology.isPresent()) {

		parameters+="Ontology: " + ontology.get()+"\n\n";
	    }

	    Optional<String> format = parser.getOptionalValue("format");
	    if (format.isPresent()) {

		parameters+="Format: " + format.get()+"\n\n";
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

	if (errorMessage.isPresent()) {
	    builder.append("Message: " + errorMessage.get() + "\n\n");
	}

	if (partDetails.isPresent()) {
	    builder.append(partDetails.get());
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
