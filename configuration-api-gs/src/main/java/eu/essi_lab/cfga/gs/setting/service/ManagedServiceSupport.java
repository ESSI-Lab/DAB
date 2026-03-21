package eu.essi_lab.cfga.gs.setting.service;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gs.setting.sessioncoordinator.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.net.services.*;
import eu.essi_lab.lib.utils.*;
import redis.clients.jedis.*;

import java.util.*;

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

	SessionCoordinatorSetting.ServiceCoordinatorMode mode = ConfigurationWrapper.getSessionCoordinatorSetting()
		.getServiceCoordinatorMode();

	switch (mode) {

	case DISTRIBUTED -> {

	    String redisEndpoint = ConfigurationWrapper.getSessionCoordinatorSetting().getRedisEndpoint(false);

	    JedisPool jedis = new JedisPool(redisEndpoint, 6379);

	    List<ServiceDefinition> defs = ConfigurationWrapper.getServicesDefinition();

	    activeServices = MultiServiceManager.getDistributedActiveServices(defs, jedis);

	    jedis.close();
	}
	case LOCAL -> {

	    activeServices = MultiServiceManager.getLocalActiveServices();
	}
	}

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
