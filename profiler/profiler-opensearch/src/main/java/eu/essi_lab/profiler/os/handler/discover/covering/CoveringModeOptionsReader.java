package eu.essi_lab.profiler.os.handler.discover.covering;

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

import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;

/**
 * @author Fabrizio
 */
public class CoveringModeOptionsReader {

    /**
     * 
     */
    private static final Integer DEFAULT_COVERING_TRESHOLD = 80;

    /**
     * 
     */
    private static final Integer DEFAULT_PARTITION_SIZE = 1;

    /**
     * 
     */
    private static final Integer DEFAULT_MAX_ITERATIONS = 4;

    /**
     * @return
     */
    public static boolean isCoveringModeEnabled() {

	Optional<String> property = getProperty("coveringMode");

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @return
     */
    static int getMaxIterations() {

	Optional<String> property = getProperty("coveringModeMaxIterations");

	return property.isPresent() ? Integer.valueOf(property.get()) : DEFAULT_MAX_ITERATIONS;
    }

    /**
     * @return
     */
    static double getPartitionSize() {

	Optional<String> property = getProperty("coveringModePartitionSize");

	return property.isPresent() ? Double.valueOf(property.get()) : DEFAULT_PARTITION_SIZE;
    }

    /**
     * @return
     */
    static Optional<Integer> getPageSize() {

	Optional<String> property = getProperty("coveringModePageSize");

	return property.isPresent() ? Optional.of(Integer.valueOf(property.get())) : Optional.empty();
    }

    /**
     * @return
     */
    static int getCoveringThreshold() {

	Optional<String> property = getProperty("coveringModeCoveringTreshold");

	return property.isPresent() ? Integer.valueOf(property.get()) : DEFAULT_COVERING_TRESHOLD;
    }

    /**
     * @return
     */
    static Optional<String> getProductType() {

	Optional<String> property = getProperty("coveringModeProductType");

	return property.isPresent() && !property.get().equals("none") ? property : Optional.empty();
    }

    /**
     * @return
     */
    static boolean isViewOnlyEnabled() {

	Optional<String> property = getProperty("coveringModeViewOnly");

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @return
     */
    static boolean isTemporalConstraintEnabled() {

	Optional<String> property = getProperty("coveringModeTemporalConstraint");

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @param propertyName
     * @return
     */
    static Optional<String> getProperty(String propertyName) {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();
	    String option = properties.getProperty(propertyName);

	    if (option != null && !option.isEmpty()) {

		return Optional.of(option);
	    }
	}

	return Optional.empty();
    }
}
