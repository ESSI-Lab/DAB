/**
 * 
 */
package eu.essi_lab.messages;

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

	if (javaOpt.infoMessage != null) {

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
