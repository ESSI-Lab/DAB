package eu.essi_lab.cfga.gs.setting.service;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
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

	GSLoggerFactory.getLogger(HarvestingSetting.class).debug("Updating managed service support STARTED");

	activeServices = MultiServiceManager.get().getActiveServices();

	GSLoggerFactory.getLogger(HarvestingSetting.class).debug("Updating managed service support ENDED");
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
		map(m -> m.getTimestamp()+", " + m.getLevel() + ": [ " + m.getMessage()+" ]").//
		collect(Collectors.joining("\n"));

	return joined;
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
