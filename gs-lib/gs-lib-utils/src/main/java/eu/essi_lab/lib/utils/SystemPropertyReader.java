package eu.essi_lab.lib.utils;

import java.util.*;

/**
 * @author Fabrizio
 */
public class SystemPropertyReader {

    /**
     * @return
     */
    public static Optional<String> read(String key) {

	String value = System.getenv(key);

	if (value == null) {

	    value = System.getProperty(key);
	}

	return Optional.ofNullable(value);
    }
}
