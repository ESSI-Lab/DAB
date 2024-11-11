package eu.essi_lab.cfga;

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
