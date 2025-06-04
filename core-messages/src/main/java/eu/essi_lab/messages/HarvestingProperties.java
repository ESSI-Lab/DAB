package eu.essi_lab.messages;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.PropertiesUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * Provides info about the harvesting performed on a {@link GSSource}
 *
 * @author Fabrizio
 */
public class HarvestingProperties extends Properties {

    /**
    * 
    */
    private static final long serialVersionUID = 8995192992040560943L;
    private static final String RESOURCES_COUNT_KEY = "resourcesCount";
    private static final String HARVESTING_COUNT_KEY = "harvestingCount";
    private static final String END_TIME_STAMP_KEY = "endTimeStamp";
    private static final String START_TIME_STAMP_KEY = "startTimeStamp";
    private static final String RECOVERY_RESUMPTION_TOKEN_KEY = "recoveryResumptionToken";
    private static final String COMPLETED_KEY = "completed";

    /**
     * 
     */
    public static final String FILE_NAME = "harvesting.properties";

    /**
     * 
     */
    public HarvestingProperties() {

	setHarvestingCount(0);
    }

    /**
     * @return
     */
    public static String getFileName() {

	return FILE_NAME;
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     */
    public static HarvestingProperties fromStream(InputStream stream) throws IOException {

	return PropertiesUtils.fromStream(stream, HarvestingProperties.class);
    }

    /**
     * @return
     * @throws IOException
     */
    public InputStream asStream() throws IOException {

	return PropertiesUtils.asStream(this);
    }

    /**
     * Set the number of resources harvested by the related {@link GSSource}
     */
    public void setResourcesCount(int count) {

	setProperty(RESOURCES_COUNT_KEY, String.valueOf(count));
    }

    /**
     * Get the number of resources harvested by the related {@link GSSource}
     */
    public int getResourcesCount() {

	String property = getProperty(RESOURCES_COUNT_KEY);
	if (property == null) {
	    return -1;
	}

	return Integer.valueOf(property);
    }

    /**
     * Set how many times the related {@link GSSource} has been harvested
     */
    public void setHarvestingCount(int count) {

	setProperty(HARVESTING_COUNT_KEY, String.valueOf(count));
    }

    /**
     *  
     */
    public void incrementHarvestingCount() {

	int count = getHarvestingCount();

	setProperty(HARVESTING_COUNT_KEY, String.valueOf(count + 1));
    }

    /**
     * Get how many times the related {@link GSSource} has been harvested
     */
    public int getHarvestingCount() {

	return Integer.valueOf(getProperty(HARVESTING_COUNT_KEY));
    }

    /**
     * Set <code>timeStamp</code> as time stamp of the last harvesting start time
     *
     * @param timeStamp
     */
    public void setStartHarvestingTimestamp(String timeStamp) {

	setProperty(START_TIME_STAMP_KEY, timeStamp);
    }

    /**
     * Get the time stamp (expressed in ISO8601 format) of the last harvesting start time
     */
    public String getStartHarvestingTimestamp() {

	return getProperty(START_TIME_STAMP_KEY);
    }

    /**
     * Set this moment as time stamp of the last harvesting end time
     */
    public void setEndHarvestingTimestamp() {

	String dateTime = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
	setProperty(END_TIME_STAMP_KEY, dateTime);
    }

    /**
     * Get the time stamp (expressed in ISO8601 format) of the last harvesting end time
     */
    public String getEndHarvestingTimestamp() {

	return getProperty(END_TIME_STAMP_KEY);
    }

    /**
     * @param token
     */
    public void setRecoveryResumptionToken(String token) {

	setProperty(RECOVERY_RESUMPTION_TOKEN_KEY, token);
    }

    /**
     * @return
     */
    public String getRecoveryResumptionToken() {

	return getProperty(RECOVERY_RESUMPTION_TOKEN_KEY);
    }

    /**
     * @param token
     */
    public void setRecoveryRemovalToken(String token) {

	setProperty(ResourceProperty.RECOVERY_REMOVAL_TOKEN.getName(), token);
    }

    /**
     * @return
     */
    public String getRecoveryRemovalToken() {

	return getProperty(ResourceProperty.RECOVERY_REMOVAL_TOKEN.getName());
    }

    /**
     * @param completed
     */
    public void setCompleted(boolean completed) {

	setProperty(COMPLETED_KEY, String.valueOf(completed));
    }

    /**
     * @param isRecovering
     * @return
     */
    public boolean isResumed(boolean isRecovering) {

	return !isCompleted().orElse(true) && !isRecovering;
    }

    /**
     * @return
     */
    public Optional<Boolean> isCompleted() {

	String property = getProperty(COMPLETED_KEY);
	if (property == null) {
	    return Optional.empty();
	}

	return Optional.of(Boolean.valueOf(property));
    }

}
