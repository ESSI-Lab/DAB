package eu.essi_lab.cfga.setting;

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
import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public interface KeyValueOptionDecorator {

    /**
     * 
     */
    static final String KEY_VALUE_OPTION_KEY = "keyValue";

    /**
     * @return
     */
    public default Setting getDecoratedSetting() {

	return (Setting) this;
    }

    /**
     * 
     */
    public default void addKeyValueOption() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(KEY_VALUE_OPTION_KEY).//
		withLabel("Key-value options").//
		withDescription("Set of key-value options. They must be compliant with Java Properties").//
		withTextArea().//
		cannotBeDisabled().//
		build();

	getDecoratedSetting().addOption(option);
    }

    /**
     * @param key
     * @param value
     */
    public default boolean putKeyValue(String key, String value) {

	String currentValue = getDecoratedSetting().//
		getOption(KEY_VALUE_OPTION_KEY, String.class).//
		get().//
		getValue();

	String keyValue = key + "=" + value;

	boolean replaced = false;

	if (currentValue != null) {

	    String matchKey = key + "=";

	    if (currentValue.contains(matchKey)) {

		int startIndex = currentValue.indexOf(matchKey) + matchKey.length();
		int endIndex = currentValue.indexOf("\n", startIndex);
		if (endIndex == -1) {
		    endIndex = currentValue.length();
		}

		String currentKeyValue = currentValue.substring(startIndex, endIndex);

		currentValue = currentValue.replace(matchKey + currentKeyValue, keyValue);

		replaced = true;

	    } else {

		currentValue += "\n" + keyValue;
	    }

	} else {

	    currentValue = keyValue;
	}

	getDecoratedSetting().//
		getOption(KEY_VALUE_OPTION_KEY, String.class).//
		get().//
		setValue(currentValue);

	return replaced;
    }

    /***
     * @return
     */
    public default Optional<Properties> getKeyValueOptions() {

	Optional<Option<String>> option = getDecoratedSetting().//
		getOption(KEY_VALUE_OPTION_KEY, String.class);

	if (option.isEmpty()) {

	    return Optional.empty();
	}

	String value = option.//
		get().//
		getValue();

	if (value == null || value.isEmpty()) {

	    return Optional.empty();
	}

	Properties properties = new Properties();

	try {
	    properties.load(IOStreamUtils.asStream(value.trim()));

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    return Optional.empty();
	}

	return Optional.of(properties);
    }
}
