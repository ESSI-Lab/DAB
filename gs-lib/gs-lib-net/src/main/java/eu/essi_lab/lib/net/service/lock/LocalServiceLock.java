package eu.essi_lab.lib.net.service.lock;

import java.util.*;

/**
 * @author Fabrizio
 */
public class LocalServiceLock implements ServiceLock {

    private final String value;
    private final String hostName;
    private final String serviceId;

    public static final Set<Map.Entry<String, String>> ACTIVE_SERVICES = new LinkedHashSet<>();

    /**
     *
     * @param serviceId
     * @param hostName
     */
    public LocalServiceLock(String serviceId, String hostName) {

	this.value = hostName + ":" + serviceId;
        this.hostName = hostName;
        this.serviceId = serviceId;
    }

    /**
     * @return
     */
    @Override
    public boolean tryAcquire() {

        ACTIVE_SERVICES.add(Map.entry(hostName, serviceId));

	return true;
    }

    /**
     * @return
     */
    @Override
    public boolean renew() {

	return true;
    }

    /**
     *
     */
    @Override
    public void release() {

        ACTIVE_SERVICES.remove(Map.entry(hostName, serviceId));
    }

    /**
     * @return
     */
    @Override
    public String getValue() {

	return value;
    }
}
