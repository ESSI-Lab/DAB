package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.utils.PropertiesUtils;

/**
 * @author Fabrizio
 */
public class AugmenterProperties extends Properties {

    private static final long serialVersionUID = -3182036351371322455L;
    private static final String START = "start";
    private static final String BEGIN_TIME_STAMP = "beginTimestamp";
    private static final String TIME_STAMP = "timeStamp";
    private static final String PROGRESS = "progress";
    private static final String ITERATIONS_COUNT = "iterationsCount";

    /**
     * 
     */
    public AugmenterProperties() {

	setBeginTimestamp();
	setStart(1);
	setProperty(ITERATIONS_COUNT, "1");
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     */
    public static AugmenterProperties fromStream(InputStream stream) throws IOException {

	return PropertiesUtils.fromStream(stream, AugmenterProperties.class);
    }

    /**
     * @return
     * @throws IOException
     */
    public InputStream asStream() throws IOException {

	return PropertiesUtils.asStream(this);
    }

    /**
     * @param start
     */
    public void setStart(int start) {

	setProperty(START, String.valueOf(start));
    }

    /**
     * @return
     */
    public int getStart() {

	String property = getProperty(START);

	return Integer.valueOf(property);
    }

    /**
     * 
     */
    public void setBeginTimestamp() {

	setProperty(BEGIN_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
    }

    /**
     * 
     */
    public void setTimestamp() {

	setProperty(TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
    }

    /**
     * @param iterationsCount
     * @param targetIterationsCount
     * @param progress
     */
    public void setProgress(IterationLogger logger) {

	String value = "[" + logger.getIterationsCount() + "/" + logger.getTargetIterations() + "] --> " + logger.getProgress() + "%";

	setProperty(ITERATIONS_COUNT, String.valueOf(logger.getIterationsCount()));

	setProperty(PROGRESS, value);
    }

    /**
     * @return
     */
    public Integer getIterationsCount() {

	String property = getProperty(ITERATIONS_COUNT, "1");

	return Integer.valueOf(property);
    }
}
