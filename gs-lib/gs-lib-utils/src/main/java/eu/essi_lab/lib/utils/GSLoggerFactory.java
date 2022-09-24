package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.PrintStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.base.Charsets;

/**
 * @author Fabrizio
 */
public class GSLoggerFactory {

    static {
	HostNamePropertyUtils.setHostNameProperty();
    }

    /**
     * @author Fabrizio
     */
    public static class GSLogger implements Logger {

	private Logger logger;

	/**
	 * @param clazz
	 */
	public GSLogger(Class<?> clazz) {

	    logger = LoggerFactory.getLogger(clazz);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {

	    logger.warn(marker, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {

	    logger.warn(marker, msg, t);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {

	    logger.warn(marker, format, arguments);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {

	    logger.warn(marker, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {

	    logger.warn(format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String msg) {

	    logger.warn(marker, msg);
	}

	@Override
	public void warn(String msg, Throwable t) {

	    logger.warn(msg, t);
	}

	@Override
	public void warn(String format, Object... arguments) {

	    logger.warn(format, arguments);
	}

	@Override
	public void warn(String format, Object arg) {

	    logger.warn(format, arg);
	}

	@Override
	public void warn(String msg) {

	    logger.warn(msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {

	    logger.trace(marker, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {

	    logger.trace(marker, msg, t);
	}

	@Override
	public void trace(Marker marker, String format, Object... arguments) {

	    logger.trace(marker, format, arguments);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {

	    logger.trace(marker, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {

	    logger.trace(format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String msg) {

	    logger.trace(marker, msg);
	}

	@Override
	public void trace(String msg, Throwable t) {

	    logger.trace(msg, t);
	}

	@Override
	public void trace(String format, Object... arguments) {

	    logger.trace(format, arguments);
	}

	@Override
	public void trace(String format, Object arg) {

	    logger.trace(format, arg);
	}

	@Override
	public void trace(String msg) {

	    logger.trace(msg);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {

	    return logger.isWarnEnabled(marker);
	}

	@Override
	public boolean isWarnEnabled() {

	    return logger.isWarnEnabled();
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {

	    return logger.isTraceEnabled(marker);
	}

	@Override
	public boolean isTraceEnabled() {

	    return logger.isTraceEnabled();
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {

	    return logger.isInfoEnabled(marker);
	}

	@Override
	public boolean isInfoEnabled() {

	    return logger.isInfoEnabled();
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {

	    return logger.isErrorEnabled(marker);
	}

	@Override
	public boolean isErrorEnabled() {

	    return logger.isErrorEnabled();
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {

	    return logger.isDebugEnabled(marker);
	}

	@Override
	public boolean isDebugEnabled() {

	    return logger.isDebugEnabled();
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {

	    logger.info(marker, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {

	    logger.info(marker, msg, t);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {

	    logger.info(marker, format, arguments);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {

	    logger.info(marker, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {

	    logger.info(format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String msg) {

	    logger.info(marker, msg);
	}

	@Override
	public void info(String msg, Throwable t) {

	    logger.info(msg, t);
	}

	@Override
	public void info(String format, Object... arguments) {

	    logger.info(format, arguments);
	}

	@Override
	public void info(String format, Object arg) {

	    logger.info(format, arg);
	}

	@Override
	public void info(String msg) {

	    logger.info(msg);
	}

	@Override
	public String getName() {

	    return logger.getName();
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {

	    logger.error(marker, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {

	    logger.error(marker, msg, t);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {

	    logger.error(marker, format, arguments);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {

	    logger.error(marker, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {

	    logger.error(format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String msg) {

	    logger.error(marker, msg);
	}

	@Override
	public void error(String msg, Throwable t) {

	    logger.error(msg, t);
	}

	/**
	 * @param t
	 */
	public void error(Throwable t) {

	    logger.error(t.getMessage(), t);
	}

	@Override
	public void error(String format, Object... arguments) {

	    logger.error(format, arguments);
	}

	@Override
	public void error(String format, Object arg) {

	    logger.error(format, arg);
	}

	@Override
	public void error(String msg) {

	    logger.error(msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {

	    logger.debug(marker, format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {

	    logger.debug(marker, msg, t);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {

	    logger.debug(marker, format, arguments);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {

	    logger.debug(marker, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {

	    logger.debug(format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String msg) {

	    logger.debug(marker, msg);
	}

	@Override
	public void debug(String msg, Throwable t) {

	    logger.debug(msg, t);
	}

	@Override
	public void debug(String format, Object... arguments) {

	    logger.debug(format, arguments);
	}

	@Override
	public void debug(String format, Object arg) {

	    logger.debug(format, arg);
	}

	@Override
	public void debug(String msg) {

	    logger.debug(msg);
	}

	/**
	 * 
	 */
	public void traceFreeMemory(String message) {

	    logger.trace(message + "[" + Runtime.getRuntime().freeMemory() / 1000000 + "] MB");
	}

	/**
	 * 
	 */
	public void traceFreeMemory() {

	    traceFreeMemory("FHSM: ");
	}
    }

    /**
     * @param clazz
     * @return
     */
    public static GSLogger getLogger(Class<?> clazz) {

	return new GSLogger(clazz);
    }
}
