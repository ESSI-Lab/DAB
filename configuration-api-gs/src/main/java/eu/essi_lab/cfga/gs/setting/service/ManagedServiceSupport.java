package eu.essi_lab.cfga.gs.setting.service;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.*;
import eu.essi_lab.services.message.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class ManagedServiceSupport {

    /**
     *
     */
    private static final ManagedServiceSupport INSTANCE = new ManagedServiceSupport();

    private List<Map.Entry<String, String>> activeServices;

    /**
     * @return
     */
    public static ManagedServiceSupport getInstance() {

	return INSTANCE;
    }

    /**
     *
     */
    private ManagedServiceSupport() {

	activeServices = new ArrayList<>();
    }

    /**
     *
     */
    public synchronized void update() {

	GSLoggerFactory.getLogger(ManagedServiceSupport.class).debug("Updating managed service support STARTED");

	activeServices = MultiServiceManager.get().getActiveServices();

	GSLoggerFactory.getLogger(ManagedServiceSupport.class).debug("Updating managed service support ENDED");
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getServiceStatus(Setting setting) {

	String serviceId = setting.getOption(ManagedServiceSetting.SERVICE_ID_OPTION_KEY, String.class).get().getValue();

	if (getActiveServices().contains(serviceId)) {

	    return "Active";
	}

	if (getQueuedServices().contains(serviceId)) {

	    return "Queued";
	}

	return "Disabled";
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getServiceMessages(Setting setting) {

	String serviceId = setting.getOption(ManagedServiceSetting.SERVICE_ID_OPTION_KEY, String.class).get().getValue();

	List<MessageChannel.Message> messages = MessageChannels.get().read(serviceId);

	String joined = messages.stream().//
		map(m -> m.getTimestamp() + ", " + m.getLevel() + ": [ " + m.getMessage() + " ]").//
		collect(Collectors.joining("\n"));

	String collect = KeyValueStoreProvider.get().get(serviceId).stream().map(e -> "Key: " + e.getKey() + " - Value: " + e.getValue())
		.collect(Collectors.joining("\n"));

	return joined + "\n" + collect;
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getServiceHost(Setting setting) {

	String serviceId = setting.getOption(ManagedServiceSetting.SERVICE_ID_OPTION_KEY, String.class).get().getValue();

	return getActiveServicesWithHost().//
		stream().//
		filter(entry -> entry.getValue().equals(serviceId)).//
		map(Map.Entry::getKey).//
		findFirst().//
		orElse("");
    }

    /**
     * @return
     */
    public synchronized List<Map.Entry<String, String>> getActiveServicesWithHost() {

	return activeServices;
    }

    /**
     * @return
     */
    public synchronized List<String> getActiveServices() {

	return activeServices.stream().map(Map.Entry::getValue).toList();
    }

    /**
     * @return
     */
    public synchronized List<String> getQueuedServices() {

	return ConfigurationWrapper.getManagedServiceSettings().stream().//
		filter(Setting::isEnabled). //
		map(ManagedServiceSetting::getServiceId).//
		filter(id -> !getActiveServices().contains(id)).//
		toList();//
    }

    /**
     * @return
     */
    public synchronized List<String> getDisabledServices() {

	return ConfigurationWrapper.getManagedServiceSettings(). //
		stream().//
		filter(s -> !s.isEnabled()). //
		map(ManagedServiceSetting::getServiceId).//
		toList();//
    }

}
