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

import eu.essi_lab.configuration.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public enum JVMOption {

    /**
     * - String
     */
    CLUSTER(ClusterType.CLUSTER_TYPE_KEY, "Cluster: ", ClusterType.LOCAL.getLabel()),

    /**
     * - String
     */
    EXECUTION_MODE(ExecutionMode.EXECUTION_MODE_KEY, "Execution mode: ", ExecutionMode.MIXED.getLabel()),

    /**
     * - Boolean
     */
    FORCE_VOLATILE_DB("forceVolatileDB", "Forced usage of volatile DB", "Usage of volatile DB not forced", false),

    /**
     * - Boolean
     */
    INIT_CACHES("initCaches", "Caches initialization enabled", "Caches initialization disabled", false),
    /**
     * - Boolean
     */
    CHECK_CONFIG("checkConfig", "Configuration check enabled", "Configuration check disabled", true),
    /**
     * - Boolean
     */
    SKIP_GDAL_TEST("skipGDALTest", "GDAL ping method disabled", "GDAL ping method enabled", false),
    /**
     * - Boolean
     */
    SKIP_HEALTH_CHECK("skipHealthcheck", "Health check at startup disabled", "Health check at startup enabled", false),
    /**
     * - String
     */
    CONFIGURATION_URL("configuration.url", "Configuration URL: "),

    /**
     * - String
     */
    LOCAL_PROD_CONFIG_PATH("localProdConfigPath", "Local production config. path: "),

    /**
     * - String
     */
    S3_ENDPOINT("s3Endpoint", "S3 endpoint: "),

    /**
     * - Boolean
     */
    SKIP_CONFIG_AUTHORIZATION("skipConfigAuthorization", "Configuration authorization disabled", "Configuration authorization enabled",
	    false),

    /**
     * - Boolean
     */
    SKIP_REQUESTS_AUTHORIZATION("skipReqAuthorization", "Requests authorization disabled", "Requests authorization enabled", false),

    /**
     * - Boolean
     */
    DEBUG_OPENSEARCH_QUERIES("debugOpenSearchQueries", "OpenSearch queries debugging enabled", "OpenSearch queries debugging disabled",
	    false),

    /**
     * - Boolean
     */
    INIT_OPENSEARCH_INDEXES("initIndexes", "OpenSearch indexes init enabled", "OpenSearch indexes init disabled", false),

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
    ANONYMOUS_OFFSET_LIMIT("offsetLimit", "Maximum request offset granted to the anonymous user (-1 = unlimited): ", Integer.MAX_VALUE,
	    v -> v == -1 ? Integer.MAX_VALUE : v),

    /**
     * - Integer
     */
    ANONYMOUS_PAGE_SIZE_LIMIT("pageSizeLimit", "Maximum page size granted to the anonymous user: ", 200);

    /**
     *
     */
    public static boolean printMessages;

    private final String option;
    private String infoMessage;
    private String enabledMessage;
    private String disabledMessage;
    private Boolean defaultBooleanValue;
    private Integer defaultIntValue;
    private Function<Integer, Integer> intMapper;
    private String defaultStringValue;

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     * @param defaultValue
     */
    JVMOption(String option, String enabledMessage, String disabledMessage, boolean defaultValue) {

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
    JVMOption(String option, String infoMessage, int defaultValue) {

	this.option = option;
	this.infoMessage = infoMessage;
	this.defaultIntValue = defaultValue;
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     * @param defaultValue
     */
    JVMOption(String option, String infoMessage, int defaultValue, Function<Integer, Integer> intMapper) {

	this.option = option;
	this.infoMessage = infoMessage;
	this.defaultIntValue = defaultValue;
	this.intMapper = intMapper;
    }

    /**
     * @param option
     * @param enabledMessage
     * @param disabledMessage
     * @param defaultValue
     */
    JVMOption(String option, String infoMessage, String defaultValue) {

	this.option = option;
	this.infoMessage = infoMessage;
	this.defaultStringValue = defaultValue;
    }

    /**
     * @param option
     * @param infoMessage
     */
    JVMOption(String option, String infoMessage) {

	this.option = option;
	this.infoMessage = infoMessage;
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
    public Optional<String> getDefaultStringValue() {

	return Optional.ofNullable(defaultStringValue);
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
    public Optional<Integer> getDefaultIntValue() {

	return Optional.ofNullable(defaultIntValue);
    }

    /**
     * @param javaOpt
     * @return
     */
    public static Optional<String> getStringValue(JVMOption javaOpt) {

	String ret = System.getProperty(javaOpt.option);

	if (ret == null) {

	    ret = System.getenv(javaOpt.option);
	}

	if (javaOpt.infoMessage != null && ret != null && printMessages) {

	    GSLoggerFactory.getLogger(JVMOption.class).debug(javaOpt.infoMessage + ret);
	}

	return Optional.ofNullable(ret);
    }

    public static void main(String[] args) {

	log();
    }

    /**
     * @param javaOpt
     * @return
     */
    public static Optional<Integer> getIntValue(JVMOption javaOpt) {

	Optional<Integer> opt = getStringValue(javaOpt).map(Integer::valueOf);

	if (opt.isEmpty()) {

	    //
	    // not declared option: returns default value (if specified) or an empty Optional (only possible case)
	    //

	    return javaOpt.getDefaultIntValue();
	}

	//
	// declared option: if a mapper is present mapping is applied
	//

	return getIntMapper(javaOpt).map(mapper -> mapper.apply(opt.get())).or(() -> opt);
    }

    /**
     * @param javaOpt
     * @return
     */
    public static boolean isEnabled(JVMOption javaOpt) {

	boolean out = getStringValue(javaOpt).map(Boolean::valueOf).orElse(javaOpt.defaultBooleanValue);

	if (printMessages) {

	    GSLoggerFactory.getLogger(JVMOption.class).debug(out ? javaOpt.enabledMessage : javaOpt.disabledMessage);
	}

	return out;
    }

    /**
     * @param javaOpt
     * @return
     */
    private static Optional<Function<Integer, Integer>> getIntMapper(JVMOption javaOpt) {

	return Optional.ofNullable(javaOpt.intMapper);
    }

    /**
     *
     */
    public static void log() {

	StringBuilder builder = new StringBuilder();

	int maxValueLength = getMaxValueLength();

	int trailing = getTableLength(maxValueLength);

	builder.append("\n");
	builder.append("-".repeat(Math.max(0, trailing))).append("\n");
	builder.append("-  Option").append(getSpaces("Option", 24)).append("|").//
		append("  Declared").append(getSpaces("Declared", 10)).append("|").//
		append("  Value").append(getSpaces("Valu", maxValueLength)).append("|").append("\n");//
	builder.append("-".repeat(Math.max(0, trailing))).append("\n");

	Stream.of(values()).sorted(Comparator.comparing(JVMOption::getOption)).forEach(option -> {

	    Optional<String> value = JVMOption.getStringValue(option).map(v -> v.equals("-1") ? "Unlimited" : v);

	    String defValue = readDefValue(option);

	    if (value.isPresent()) {

		String row = buildRow(option.getOption(), value.get(), true, maxValueLength);

		builder.append(row);//

	    } else {

		String row = buildRow(option.getOption(), defValue, false, maxValueLength);

		builder.append(row);//
	    }

	    builder.append("-".repeat(Math.max(0, trailing))).append("\n");
	});

	GSLoggerFactory.getLogger(JVMOption.class).info(builder.toString());
    }

    /**
     * @param option
     * @return
     */
    private static String readDefValue(JVMOption option) {

	Optional<String> defString = option.getDefaultStringValue();

	Optional<Integer> defInt = option.getDefaultIntValue();

	Optional<Boolean> defBoolean = option.getDefaultBooleanValue();

	String defValue = defString.orElseGet(
		() -> defInt.isPresent() ? String.valueOf(defInt.get()) : defBoolean.isPresent() ? String.valueOf(defBoolean.get()) : "-");

	if (defValue.equals(String.valueOf(Integer.MAX_VALUE))) {

	    defValue = "Unlimited";
	}

	return defValue;
    }

    /**
     * @param maxValueLength
     * @return
     */
    private static int getTableLength(int maxValueLength) {

	AtomicInteger length = new AtomicInteger(0);

	List.of(values()).forEach(option -> {

	    Optional<String> value = JVMOption.getStringValue(option);

	    String defValue = readDefValue(option);

	    if (value.isPresent()) {

		String row = buildRow(option.getOption(), value.get(), true, maxValueLength);

		if (row.length() > length.intValue()) {

		    length.set(row.length());
		}

	    } else {

		String row = buildRow(option.getOption(), defValue, false, maxValueLength);

		if (row.length() > length.intValue()) {

		    length.set(row.length());
		}
	    }
	});

	return length.intValue() - 1;
    }

    /**
     * @return
     */
    private static int getMaxValueLength() {

	return Stream.of(values()).map(option -> {

	    Optional<String> value = JVMOption.getStringValue(option);

	    String defValue = readDefValue(option);

	    return value.map(String::length).orElseGet(defValue::length);

	}).max(Integer::compareTo).get();
    }

    /**
     * @param option
     * @param value
     * @param present
     * @param maxValueLength
     * @return
     */
    private static String buildRow(String option, String value, boolean present, int maxValueLength) {

	return "-  " + option + getSpaces(option, 24) + "|" +//
		"  " + Optional.of(present).map(v -> v ? "yes" : "no").get() + getSpaces(
		Optional.of(present).map(v -> v ? "yes" : "no").get(), 10) + "|  " + value +//
		getSpaces(value, maxValueLength) + " |" + "\n";
    }

    /**
     * @param str
     * @param len
     * @return
     */
    private static String getSpaces(String str, int len) {

	return " ".repeat(Math.max(0, len - str.length()));
    }

}
