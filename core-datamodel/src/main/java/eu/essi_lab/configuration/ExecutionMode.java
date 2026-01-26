package eu.essi_lab.configuration;

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

import java.util.Arrays;
import java.util.Optional;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum ExecutionMode implements LabeledEnum {

    /**
     * This mode disables execution of batch jobs and enables execution of incoming requests
     */
    FRONTEND("Frontend"),

    /**
     * This mode disables execution of incoming requests and enables execution of batch jobs of type harvesting
     */
    BATCH("Batch"),

    /**
     * This mode disables execution of incoming requests and enables execution of batch jobs of type bulk download and
     * type single download jobs
     */
    BULK("Bulk"),

    /**
     * This mode disables execution of incoming requests and enables execution of batch augmentation jobs
     */
    AUGMENTER("Augmenter"),
    /**
     * This mode disables execution of incoming requests, used for synch access cluster
     */
    ACCESS("Access"),

    /**
     * Reserved for very intensive requests usage
     */
    INTENSIVE("Intensive"),
    /**
     * 
     */
    CONFIGURATION("Configuration"),
    /**
     * 
     */
    LOCAL_PRODUCTION("Local Production"),

    /**
     * (default) This mode enables execution of batch jobs AND of incoming requests
     */
    MIXED("Mixed");

    /**
     * 
     */
    public static final String GIPROJECT_EXECUTION_MODE_KEY = "EU_FLORA_ESSI_GI_PROJECT_EXECUTION";

    /**
     * 
     */
    private String name;

    /**
     * @param name
     */
    private ExecutionMode(String name) {

	this.name = name;
    }

    @Override
    public String toString() {

	return getLabel();
    }

    @Override
    public String getLabel() {

	return name;
    }

    /**
     * @return
     */
    public static String readEnv() {

	String execMode = System.getenv(GIPROJECT_EXECUTION_MODE_KEY);

	if (execMode == null) {

	    execMode = System.getProperty(GIPROJECT_EXECUTION_MODE_KEY);
	}

	return execMode;
    }

    /**
     * @return
     */
    public static ExecutionMode get() {

	Optional<ExecutionMode> execmode = ExecutionMode.decode(readEnv());

	return execmode.orElse(ExecutionMode.MIXED);
    }

    /**
     * @param value
     * @return
     */
    public static Optional<ExecutionMode> decode(String value) {

	return Arrays.asList(values()).//
		stream().//
		filter(e -> e.name().equalsIgnoreCase(value)).//
		findFirst();
    }

}
