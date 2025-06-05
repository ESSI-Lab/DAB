package eu.essi_lab.profiler.om.scheduling;

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
	    OMSchedulerSetting setting,//
	    Optional<String> locator) {

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

	ConfiguredGmailClient.sendEmail(subject, builder.toString());
    }
}
