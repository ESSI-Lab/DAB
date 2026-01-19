/**
 *
 */
package eu.essi_lab.messages;

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

import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public enum JavaOptions {

    /**
     * - Boolean
     */
    FORCE_VOLATILE_DB("forceVolatileDB", "Forced usage of volatile DB", "Usage of volatile DB not forced"),

    /**
     * - Boolean
     */
    INIT_CACHES("initCaches", "Caches initialization enabled", "Caches initialization disabled"),
    /**
     * - Boolean
     */
    CHECK_CONFIG("checkConfig", "Configuration check enabled", "Configuration check disabled", true),
    /**
     * - Boolean
     */
    SKIP_GDAL_TEST("skipGDALTest", "GDAL ping method disabled", "GDAL ping method enabled"),
    /**
     * - Boolean
     */
    SKIP_HEALTH_CHECK("skip.healthcheck", "Health check disabled", "Health check enabled"),
    /**
     * - String
     */
    CONFIGURATION_URL("configuration.url", "Configuration URL: "),

    /**
     *
     */
    LOCAL_PROD_CONFIG_PATH("localProdConfigPath", "Local production config. path: "),

    /**
     * - String
     */
    S3_ENDPOINT("s3Endpoint", "S3 endpoint: "),

    /**
     * - Boolean
     */
    SKIP_CONFIG_AUTHORIZATION("skipConfigAuthorization", "Configuration authorization disabled", "Configuration authorization enabled"),

    /**
     * - Boolean
     */
    SKIP_REQUESTS_AUTHORIZATION("skipReqAuthorization", "Requests authorization disabled", "Requests authorization enabled"),

    /**
     * - Boolean
     */
    DEBUG_OPENSEARCH_QUERIES("debugOpenSearchQueries", "OpenSearch queries debugging enabled", "OpenSearch queries debugging disabled"),

    /**
     * - Boolean
     */
    INIT_OPENSEARCH_INDEXES("initIndexes", "OpenSearch indexes init enabled", "OpenSearch indexes init disabled"),

    /**
     * - Boolean
     */
    UPDATE_DATA_FOLDER_INDEX("updateDataFolderIndex", "Updating of data-folder index enabled", "Updating of data-folder index disabled",
	    true),

    /**
     * - Integer
     */
    NUMBER_OF_DATA_FOLDER_INDEX_SHARDS("numShards", "Number of data-folder index shards: "),

    /**
     * - Integer
     */
    ANONYMOUS_OFFSET_LIMIT("offsetLimit", "Maximum request offset granted to the anonymous user: "),

    /**
     * - Integer
     */
    ANONYMOUS_PAGE_SIZE_LIMIT("pageSizeLimit", "Maximum page size granted to the anonymous user: ");

    private final String option;
    private String infoMessage;
    private String enabledMessage;
    private String disabledMessage;
    private Boolean defaultBooleanValue;
    private Integer defaultIntValue;

    /**
     * @param option
     * @param infoMessage
     */
    JavaOptions(String option, String infoMessage) {

	this.option = option;
	this.infoMessage = infoMessage;
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     */
    JavaOptions(String option, String enabledMessage, String disabledMessage) {

	this(option, enabledMessage, disabledMessage, false);
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     * @param defaultValue
     */
    JavaOptions(String option, String enabledMessage, String disabledMessage, boolean defaultValue) {

	this.option = option;
	this.enabledMessage = enabledMessage;
	this.disabledMessage = disabledMessage;
	this.defaultBooleanValue = defaultValue;
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     * @param defaultValue
     */
    JavaOptions(String option, String enabledMessage, String disabledMessage, int defaultValue) {

	this.option = option;
	this.enabledMessage = enabledMessage;
	this.disabledMessage = disabledMessage;
	this.defaultIntValue = defaultValue;
    }

    /**
     * @return
     */
    public String getOption() {

	return option;
    }

    /**
     * @return
     */
    public Optional<Boolean> getDefaultBooleanValue() {

	return Optional.ofNullable(defaultBooleanValue);
    }

    /**
     * @return
     */
    public Optional<Integer> getDefaultIntegerValue() {

	return Optional.ofNullable(defaultIntValue);
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

	Optional<Integer> opt = getValue(javaOpt).map(Integer::valueOf);

	if (opt.isEmpty()) {

	    return javaOpt.getDefaultIntegerValue();
	}

	return opt;
    }

    /**
     * @param javaOpt
     * @return
     */
    public static boolean isEnabled(JavaOptions javaOpt) {

	boolean out = getValue(javaOpt).map(Boolean::valueOf).orElse(javaOpt.defaultBooleanValue);

	GSLoggerFactory.getLogger(JavaOptions.class).debug(out ? javaOpt.enabledMessage : javaOpt.disabledMessage);

	return out;
    }
}
