package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.TabContentDescriptor;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.TabContentDescriptorBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.GSSource;

/**
 * Options of the publication to the WMO Information System (WIS2): the identity of the WIS2 centre, the public base URL
 * and view of the published records and links, and the sources whose data are published with the <code>core</code> WMO
 * data policy (the other sources are published with the <code>recommended</code> data policy, with data available
 * only through the authenticated APIs).<br>
 * The WIS2 MQTT broker is configured in the system settings.<br>
 * Empty options fall back to the corresponding legacy <code>wis*</code> key-value options of the system settings, and
 * then to their default value.
 *
 * @author boldrini
 */
public class WIS2Setting extends Setting implements EditableSetting {

    private static final String CENTRE_ID_OPTION_KEY = "wis2CentreId";
    private static final String PUBLISHER_OPTION_KEY = "wis2Publisher";
    private static final String GENERATED_BY_OPTION_KEY = "wis2GeneratedBy";
    private static final String HOSTNAME_OPTION_KEY = "wis2Hostname";
    private static final String VIEW_ID_OPTION_KEY = "wis2ViewId";
    private static final String TOKEN_CONTACT_OPTION_KEY = "wis2TokenContact";
    private static final String DATA_NOTIFICATIONS_MODE_OPTION_KEY = "wis2DataNotificationsMode";
    private static final String PRIVATE_TOPIC_PREFIX_OPTION_KEY = "wis2PrivateTopicPrefix";

    /**
     * Default prefix of the private topics, not relayed by the WIS2 Global Brokers (which subscribe to
     * <code>origin/a/wis2/#</code>)
     */
    public static final String DEFAULT_PRIVATE_TOPIC_PREFIX = "test/a/wis2";

    /**
     * How the data of the core sources are published, from the safest to the operational mode
     *
     * @author boldrini
     */
    public enum DataNotificationsMode implements LabeledEnum {

	/**
	 * Data notifications are not published, the records of the core sources do not advertise data topics and the
	 * core data are not served
	 */
	DISABLED("Disabled"),

	/**
	 * Data notifications are built and logged, but not published. The core data are served, the records are
	 * unchanged
	 */
	DRY_RUN("Dry run"),

	/**
	 * Data notifications are published under the private topic prefix, not relayed by the WIS2 Global Brokers. The
	 * core data are served, the records are unchanged
	 */
	PRIVATE("Private"),

	/**
	 * Data notifications are published under the experimental topics of the hydrology discipline, advertised by the
	 * records of the core sources
	 */
	EXPERIMENTAL("Experimental"),

	/**
	 * Data notifications are published under the official topics, advertised by the records of the core sources
	 */
	OPERATIONAL("Operational");

	private final String label;

	/**
	 * @param label
	 */
	DataNotificationsMode(String label) {

	    this.label = label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return label;
	}

	/**
	 * @return true if the records of the core sources advertise the data topics, with the core data policy
	 */
	public boolean isAdvertised() {

	    return this == EXPERIMENTAL || this == OPERATIONAL;
	}
    }

    private static final String CORE_SOURCES_SETTING_IDENTIFIER = "wis2CoreSources";

    /**
     *
     */
    public WIS2Setting() {

	setName("WIS2");
	setCanBeDisabled(false);
	enableCompactMode(false);

	addOption(buildOption(CENTRE_ID_OPTION_KEY, "Centre id",
		"WIS2 centre id, used in the topics and in the metadata identifiers (e.g. it-cnr-itiam-whos-dab). A centre id ending with '-test' can be used for testing"));

	addOption(buildOption(PUBLISHER_OPTION_KEY, "Publisher", "Organization added as publisher to the WIS2 metadata records"));

	addOption(buildOption(GENERATED_BY_OPTION_KEY, "Generated by", "The 'generated_by' value of the WIS2 notification messages"));

	addOption(buildOption(HOSTNAME_OPTION_KEY, "Hostname",
		"Public base URL of the links in the WIS2 metadata records and notification messages (e.g. https://wis2.whos-dab.eu)"));

	addOption(buildOption(VIEW_ID_OPTION_KEY, "View id", "Identifier of the view through which the WIS2 records and data are served"));

	addOption(buildOption(TOKEN_CONTACT_OPTION_KEY, "Token request contact",
		"E-mail address to contact in order to request the token needed by the authenticated APIs"));

	Option<DataNotificationsMode> mode = OptionBuilder.get(DataNotificationsMode.class).//
		withKey(DATA_NOTIFICATIONS_MODE_OPTION_KEY).//
		withLabel("Data notifications").//
		withDescription(
			"How the data of the core sources are published: disabled; dry run (notifications logged only); private (notifications published under the private topic prefix, not relayed by WIS2); experimental (experimental topics, advertised in the records); operational (official topics, advertised in the records)")
		.//
		withSingleSelection().//
		withValues(LabeledEnum.values(DataNotificationsMode.class)).//
		withSelectedValue(DataNotificationsMode.DISABLED).//
		cannotBeDisabled().//
		build();

	addOption(mode);

	addOption(buildOption(PRIVATE_TOPIC_PREFIX_OPTION_KEY, "Private topic prefix",
		"Prefix of the topics of the private data notifications, replacing origin/a/wis2. Default: " + DEFAULT_PRIVATE_TOPIC_PREFIX));

	//
	// sources published with the core data policy
	//

	Setting coreSources = new Setting();
	coreSources.setCanBeDisabled(false);
	coreSources.setEditable(false);
	coreSources.setName("Core sources");
	coreSources.setDescription(
		"Sources whose data are published with the 'core' WMO data policy. Data of the other sources are published with the 'recommended' data policy");
	coreSources.setIdentifier(CORE_SOURCES_SETTING_IDENTIFIER);
	coreSources.setSelectionMode(SelectionMode.MULTI);
	addSetting(coreSources);

	if (ConfigurationWrapper.getConfiguration().isPresent()) {

	    ConfigurationWrapper.getAllSources().forEach(s -> {

		if (s.getUniqueIdentifier() != null && !s.getUniqueIdentifier().isEmpty()) {

		    Setting setting = new Setting();
		    setting.setCanBeDisabled(false);
		    setting.setEditable(false);
		    setting.setName(s.getLabel());
		    setting.setIdentifier(s.getUniqueIdentifier());
		    coreSources.addSetting(setting);
		}
	    });
	}
    }

    /**
     * @param object
     */
    public WIS2Setting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WIS2Setting(String object) {

	super(object);
    }

    /**
     * @author boldrini
     */
    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	/**
	 *
	 */
	public DescriptorProvider() {

	    descriptor = TabContentDescriptorBuilder.get(WIS2Setting.class).//
		    withLabel("WIS2").//
		    withEditDirective("EDIT", "Edit WIS2 settings").//
		    build();
	}

	/**
	 * @return
	 */
	public TabContentDescriptor get() {

	    return descriptor;
	}
    }

    /**
     * @param key
     * @param label
     * @param description
     * @return
     */
    private static Option<String> buildOption(String key, String label, String description) {

	return StringOptionBuilder.get().//
		withKey(key).//
		withLabel(label).//
		withDescription(description).//
		cannotBeDisabled().//
		build();
    }

    /**
     * @param key
     * @return
     */
    private Optional<String> readOption(String key) {

	return getOption(key, String.class).//
		flatMap(Option::getOptionalValue).//
		map(String::trim).//
		filter(value -> !value.isEmpty());
    }

    /**
     * @param key
     * @param value
     */
    private void writeOption(String key, String value) {

	getOption(key, String.class).get().setValue(value);
    }

    /**
     * @return
     */
    public Optional<String> getCentreId() {

	return readOption(CENTRE_ID_OPTION_KEY);
    }

    /**
     * @param centreId
     */
    public void setCentreId(String centreId) {

	writeOption(CENTRE_ID_OPTION_KEY, centreId);
    }

    /**
     * @return
     */
    public Optional<String> getPublisher() {

	return readOption(PUBLISHER_OPTION_KEY);
    }

    /**
     * @param publisher
     */
    public void setPublisher(String publisher) {

	writeOption(PUBLISHER_OPTION_KEY, publisher);
    }

    /**
     * @return
     */
    public Optional<String> getGeneratedBy() {

	return readOption(GENERATED_BY_OPTION_KEY);
    }

    /**
     * @param generatedBy
     */
    public void setGeneratedBy(String generatedBy) {

	writeOption(GENERATED_BY_OPTION_KEY, generatedBy);
    }

    /**
     * @return
     */
    public Optional<String> getHostname() {

	return readOption(HOSTNAME_OPTION_KEY);
    }

    /**
     * @param hostname
     */
    public void setHostname(String hostname) {

	writeOption(HOSTNAME_OPTION_KEY, hostname);
    }

    /**
     * @return
     */
    public Optional<String> getViewId() {

	return readOption(VIEW_ID_OPTION_KEY);
    }

    /**
     * @param viewId
     */
    public void setViewId(String viewId) {

	writeOption(VIEW_ID_OPTION_KEY, viewId);
    }

    /**
     * @return
     */
    public Optional<String> getTokenRequestContact() {

	return readOption(TOKEN_CONTACT_OPTION_KEY);
    }

    /**
     * @param contact
     */
    public void setTokenRequestContact(String contact) {

	writeOption(TOKEN_CONTACT_OPTION_KEY, contact);
    }

    /**
     * @return
     */
    public DataNotificationsMode getDataNotificationsMode() {

	return getOption(DATA_NOTIFICATIONS_MODE_OPTION_KEY, DataNotificationsMode.class).//
		map(Option::getValue).//
		orElse(DataNotificationsMode.DISABLED);
    }

    /**
     * @param mode
     */
    public void setDataNotificationsMode(DataNotificationsMode mode) {

	getOption(DATA_NOTIFICATIONS_MODE_OPTION_KEY, DataNotificationsMode.class).get().setValue(mode);
    }

    /**
     * @return the prefix of the private topics, replacing <code>origin/a/wis2</code>
     */
    public String getPrivateTopicPrefix() {

	return readOption(PRIVATE_TOPIC_PREFIX_OPTION_KEY).orElse(DEFAULT_PRIVATE_TOPIC_PREFIX);
    }

    /**
     * @param prefix
     */
    public void setPrivateTopicPrefix(String prefix) {

	writeOption(PRIVATE_TOPIC_PREFIX_OPTION_KEY, prefix);
    }

    /**
     * @param sourceIdentifiers
     */
    public void selectCoreSources(List<String> sourceIdentifiers) {

	getSetting(CORE_SOURCES_SETTING_IDENTIFIER).get().select(s -> sourceIdentifiers.contains(s.getIdentifier()));
    }

    /**
     * @param sourceIdentifier
     * @return true if the given source was selected and has been removed
     */
    public boolean deselectSource(String sourceIdentifier) {

	Setting coreSources = getSetting(CORE_SOURCES_SETTING_IDENTIFIER).get();

	Optional<Setting> optSetting = coreSources.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected() && s.getIdentifier().equals(sourceIdentifier)).//
		findFirst();

	return optSetting.filter(coreSources::removeSetting).isPresent();
    }

    /**
     * @param sourceIdentifier
     * @return true if the data of the given source are published with the <code>core</code> data policy
     */
    public boolean isCoreSource(String sourceIdentifier) {

	return getCoreSourcesIds().contains(sourceIdentifier);
    }

    /**
     * @param source
     * @return
     */
    public boolean isCoreSource(GSSource source) {

	return isCoreSource(source.getUniqueIdentifier());
    }

    /**
     * @return
     */
    public List<String> getCoreSourcesIds() {

	return getSetting(CORE_SOURCES_SETTING_IDENTIFIER).//
		get().//
		getSettings().//
		stream().//
		filter(Setting::isSelected).//
		map(Setting::getIdentifier).//
		collect(Collectors.toList());
    }
}
