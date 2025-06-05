package eu.essi_lab.profiler.os.handler.discover.covering;

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

import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.profiler.os.OSProfiler.KeyValueOptionKeys;

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
     * @param setting
     * @return
     */
    public static boolean isCoveringModeEnabled(ProfilerSetting setting) {

	Optional<String> property = getProperty(SystemSetting.KeyValueOptionKeys.COVERING_MODE.getLabel(), setting);

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @param setting
     * @return
     */
    static int getMaxIterations(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_MAX_ITERATIONS.getLabel(), setting);

	return property.isPresent() ? Integer.valueOf(property.get()) : DEFAULT_MAX_ITERATIONS;
    }

    /**
     * @param setting
     * @return
     */
    static double getPartitionSize(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_PARTITION_SIZE.getLabel(), setting);

	return property.isPresent() ? Double.valueOf(property.get()) : DEFAULT_PARTITION_SIZE;
    }

    /**
     * @param setting
     * @return
     */
    static Optional<Integer> getPageSize(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_PAGE_SIZE.getLabel(), setting);

	return property.isPresent() ? Optional.of(Integer.valueOf(property.get())) : Optional.empty();
    }

    /**
     * @param setting
     * @return
     */
    static int getCoveringThreshold(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_COVERING_TRESHOLD.getLabel(), setting);

	return property.isPresent() ? Integer.valueOf(property.get()) : DEFAULT_COVERING_TRESHOLD;
    }

    /**
     * @param setting
     * @return
     */
    static Optional<String> getProductType(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_PRODUCT_TYPE.getLabel(), setting);

	return property.isPresent() && !property.get().equals("none") ? property : Optional.empty();
    }

    /**
     * @param setting
     * @return
     */
    static boolean isViewOnlyEnabled(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_VIEW_ONLY.getLabel(), setting);

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @param setting
     * @return
     */
    static boolean isTemporalConstraintEnabled(ProfilerSetting setting) {

	Optional<String> property = getProperty(KeyValueOptionKeys.COVERING_MODE_TEMPORAL_CONSTRAINT.getLabel(), setting);

	return property.isPresent() && property.get().equals("enabled");
    }

    /**
     * @param propertyName
     * @param setting
     * @return
     */
    static Optional<String> getProperty(String propertyName, ProfilerSetting setting) {

	Optional<Properties> keyValueOption = setting.getKeyValueOptions();
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
