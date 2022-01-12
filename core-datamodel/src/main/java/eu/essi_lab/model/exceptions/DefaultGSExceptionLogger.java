package eu.essi_lab.model.exceptions;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class DefaultGSExceptionLogger {

    private static final String DEFAULT_LOGGING_TEMPLATE = "{} --> {}";

    private DefaultGSExceptionLogger() {
	//private constructor to hide public one
    }

    public static synchronized void log(DefaultGSExceptionHandler handler) {

	long ts = System.currentTimeMillis();

	DefaultGSExceptionReader reader = handler.getReader();

	int severity = reader.getSeverity();

	String tolog = "";

	Logger logger = GSLoggerFactory.getLogger(DefaultGSExceptionLogger.class);

	Throwable alien = reader.getAlienException();

	if (alien != null) {

	    tolog = "Found Alien Exception (" + alien.getClass().getCanonicalName() + ") with message --> " + alien.getMessage();

	    write(logger, tolog, severity, ts, alien);

	}

	List<String> messages = reader.getErrorMessages();

	for (String m : messages) {
	    tolog = "Error Message: " + m;
	    write(logger, tolog, severity, ts, null);

	}

	tolog = "Error CODE: " + handler.createGSErrorCode();
	write(logger, tolog, severity, ts, null);
    }

    public static synchronized String printToString(DefaultGSExceptionHandler handler) {

	long ts = System.currentTimeMillis();

	String ls = System.getProperty("line.separator");

	DefaultGSExceptionReader reader = handler.getReader();

	int severity = reader.getSeverity();

	StringBuilder sb = new StringBuilder();

	Throwable alien = reader.getAlienException();

	if (alien != null) {

	    sb.append("Found Alien Exception (" + alien.getClass().getCanonicalName() + ") with message " + alien.getMessage());

	    sb.append(ls);

	    StringWriter errors = new StringWriter();
	    alien.printStackTrace(new PrintWriter(errors));
	    sb.append(errors.toString());

	}

	List<String> messages = reader.getErrorMessages();

	for (String m : messages) {

	    sb.append("Error Message: " + m);

	}

	sb.append("Error CODE: " + handler.createGSErrorCode());

	return sb.toString();
    }

    private static void write(Logger logger, String tolog, int severity, long ts, Throwable th) {

	if (severity == ErrorInfo.SEVERITY_ERROR) {

	    if (th == null)
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog);
	    else
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog, th);

	} else if (severity == ErrorInfo.SEVERITY_FATAL) {

	    if (th == null)
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog);
	    else
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog, th);

	    //TODO trigger shutdown procedure?

	} else if (severity == ErrorInfo.SEVERITY_WARNING) {

	    if (th == null)
		logger.warn(DEFAULT_LOGGING_TEMPLATE, ts, tolog);
	    else
		logger.warn(DEFAULT_LOGGING_TEMPLATE, ts, tolog, th);
	} else {
	    if (th == null)
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog);
	    else
		logger.error(DEFAULT_LOGGING_TEMPLATE, ts, tolog, th);
	}
    }

}
