/**
 * 
 */
package eu.essi_lab.configuration;

import java.util.Arrays;
import java.util.Optional;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum ClusterType implements LabeledEnum {

    /**
     * 
     */
    PRODUCTION("Production"),
    /**
     * 
     */
    PRE_PRODUCTION("Preproduction"),
    /**
     * 
     */
    TEST("Test"),
    /**
     * 
     */
    LOCAL("Local");

    /**
     * 
     */
    private String name;

    /**
     * @param name
     */
    private ClusterType(String name) {

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

	String execMode = System.getenv("cluster");

	if (execMode == null) {

	    execMode = System.getProperty("cluster");
	}

	return execMode;
    }

    /**
     * @return
     */
    public static ClusterType get() {

	Optional<ClusterType> execmode = ClusterType.decode(readEnv());

	return execmode.orElse(ClusterType.LOCAL);
    }

    /**
     * @param value
     * @return
     */
    public static Optional<ClusterType> decode(String value) {

	return Arrays.asList(values()).//
		stream().//
		filter(e -> e.getLabel().equals(value)).//
		findFirst();
    }
}
