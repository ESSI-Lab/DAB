/**
 * 
 */
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

import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public enum JavaOptions {

    /**
     * - Boolean
     */
    INIT_CACHES("initCaches", "Caches initialization enabled", "Caches initialization disabled"),
    /**
     * - Boolean
     */
    CHECK_CONFIG("checkConfig", "Configuration check enabled", "Configuration check disabled"),
    /**
     * - Boolean
     */
    SKIP_GDAL_TEST("skipGDALTest", null),
    /**
     * - Boolean
     */
    SKIP_HEALTH_CHECK("skip.healthcheck", "Health check disabled", "Health check enabled"),
    /**
     * - String
     */
    CONFIGURATION_URL("configuration.url", "Configuration URL: "),
    /**
     * - String
     */
    S3_ENDPOINT("s3Endpoint", "S3 endpoint: "),

    /**
     * 
     */
    SKIP_AUTHORIZATION("DAB_SKIP_AUTHORIZATION", "Authorization disabled", "Authorization enabled"),

    /**
     * - Boolean
     */
    DEBUG_OPENSEARCH_QUERIES("debugOpenSearchQueries", "OpenSearch queries debugging enabled", "OpenSearch queries debugging disabled"),

    /**
     * - Boolean
     */
    INIT_OPENSEARCH_INDEXES("initIndexes", "OpenSearch indexes init enabled", "OpenSearch indexes init disabled"),

    /**
     * - Integer
     */
    NUMBER_OF_DATA_FOLDER_INDEX_SHARDS("numShards", "Number of data-folder index shards: ");

    private String option;
    private String infoMessage;
    private String enabledMessage;
    private String disabledMessage;

    /**
     * @param option
     * @param infoMessage
     */
    private JavaOptions(String option, String infoMessage) {

	this.option = option;
	this.infoMessage = infoMessage;
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     */
    private JavaOptions(String option, String enabledMessage, String disabledMessage) {

	this.option = option;
	this.enabledMessage = enabledMessage;
	this.disabledMessage = disabledMessage;
    }

    /**
     * @param javaOpt
     * @return
     */
    public static Optional<String> getValue(JavaOptions javaOpt) {

	String ret = System.getProperty(javaOpt.option);

	if (ret == null) {

	    ret = System.getenv(javaOpt.option);
	}

	if (javaOpt.infoMessage != null && ret != null) {

	    GSLoggerFactory.getLogger(JavaOptions.class).debug(javaOpt.infoMessage + ret);
	}

	return Optional.ofNullable(ret);
    }

    /**
     * @param javaOpt
     * @return
     */
    public static Optional<Integer> getIntValue(JavaOptions javaOpt) {

	return getValue(javaOpt).map(v -> Integer.valueOf(v));
    }

    /**
     * @param javaOpt
     * @return
     */
    public static boolean isEnabled(JavaOptions javaOpt) {

	Optional<String> check = getValue(javaOpt);

	boolean out = check.isEmpty() ? false : Boolean.valueOf(check.get());

	GSLoggerFactory.getLogger(JavaOptions.class).debug(out ? javaOpt.enabledMessage : javaOpt.disabledMessage);

	return out;
    }
}
