package eu.essi_lab.cfga;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public interface ConfigurationChangeListener {

    /**
     * @author Fabrizio
     */
    public class ConfigurationChangeEvent {
	/**
	 * 
	 */
	public static final int SETTING_PUT = 0;
	/**
	 * 
	 */
	public static final int SETTING_REMOVED = 1;

	/**
	 * 
	 */
	public static final int SETTING_REPLACED = 2;

	/**
	 * 
	 */
	public static final int CONFIGURATION_CLEARED = 3;

	/**
	 * 
	 */
	public static final int CONFIGURATION_FLUSHED = 4;

	/**
	 * 
	 */
	public static final int CONFIGURATION_AUTO_RELOADED = 5;

	private int eventType;
	private Configuration configuration;
	private List<Setting> settings;

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, int eventType) {

	    this(configuration, Arrays.asList(), eventType);
	}

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, Setting setting, int eventType) {

	    this(configuration, Arrays.asList(setting), eventType);
	}

	/**
	 * @param configuration
	 * @param setting
	 * @param eventType
	 */
	public ConfigurationChangeEvent(Configuration configuration, List<Setting> settings, int eventType) {
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
	public int getEventType() {

	    return eventType;
	}
    }

    /**
     * @param eventType
     */
    void configurationChanged(ConfigurationChangeEvent event);

}
