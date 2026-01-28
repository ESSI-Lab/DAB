package eu.essi_lab.cfga;

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

import eu.essi_lab.cfga.setting.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public interface ConfigurationChangeListener {

    /**
     * @author Fabrizio
     */
    enum EventType {

	/**
	 *
	 */
	SETTING_PUT,
	/**
	 *
	 */
	SETTING_REMOVED,
	/**
	 *
	 */
	SETTING_REPLACED,
	/**
	 *
	 */
	CONFIGURATION_CLEARED,
	/**
	 *
	 */
	CONFIGURATION_FLUSHED,
	/**
	 *
	 */
	CONFIGURATION_AUTO_RELOADED;
    }

    /**
     * @author Fabrizio
     */
    class ConfigurationChangeEvent {

	private final EventType eventType;
	private final Configuration configuration;
	private final List<Setting> settings;

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, EventType eventType) {

	    this(configuration, List.of(), eventType);
	}

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, Setting setting, EventType eventType) {

	    this(configuration, Collections.singletonList(setting), eventType);
	}

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, List<Setting> settings, EventType eventType) {

	    this.configuration = configuration;
	    this.settings = settings;
	    this.eventType = eventType;
	}

	/**
	 * @return
	 */
	public Configuration getConfiguration() {

	    return configuration;
	}

	/**
	 * @return
	 */
	public List<Setting> getSettings() {

	    return settings;
	}

	/**
	 * @return
	 */
	public EventType getEventType() {

	    return eventType;
	}
    }

    /**
     * @param eventType
     */
    void configurationChanged(ConfigurationChangeEvent event);
}
