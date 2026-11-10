package eu.essi_lab.gssrv.conf.task.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Summary of a COG task, written in the task status and so in the summary e-mail of the task. The processors keep logging the details
 * on the GS log, and report here only the outcome (run, published files, errors).<br>
 * The e-mail does not show the message type, so warnings and errors are prefixed with WARNING: and ERROR:
 */
public interface TaskReport {

    /**
     * Maximum length of an exception message in the report (GDAL errors include the whole command output)
     */
    int MAX_MESSAGE_LENGTH = 300;

    /**
     * Report that discards the messages, used when the processors are executed outside a task
     */
    TaskReport NONE = new TaskReport() {

	@Override
	public void info(String message) {
	}

	@Override
	public void warn(String message) {
	}

	@Override
	public void error(String message) {
	}
    };

    void info(String message);

    void warn(String message);

    void error(String message);

    /**
     * @return a report that writes the messages in the given task status
     */
    static TaskReport of(SchedulerJobStatus status) {

	return new TaskReport() {

	    @Override
	    public void info(String message) {
		status.addInfoMessage(message);
	    }

	    @Override
	    public void warn(String message) {
		status.addWarningMessage("WARNING: " + message);
	    }

	    @Override
	    public void error(String message) {
		status.addErrorMessage("ERROR: " + message);
	    }
	};
    }

    /**
     * @return the first line of the exception message, truncated to {@link #MAX_MESSAGE_LENGTH} characters
     */
    static String message(Throwable e) {

	String message = e.getMessage() != null ? e.getMessage().strip() : e.getClass().getSimpleName();
	int newLine = message.indexOf('\n');
	if (newLine > 0) {
	    message = message.substring(0, newLine).strip();
	}
	return message.length() > MAX_MESSAGE_LENGTH ? message.substring(0, MAX_MESSAGE_LENGTH) + "..." : message;
    }

    /**
     * @return the time elapsed since the given start, e.g. 6m 36s
     */
    static String elapsed(Instant start) {

	Duration duration = Duration.between(start, Instant.now());
	long hours = duration.toHours();
	return (hours > 0 ? hours + "h " : "") + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s";
    }
}
