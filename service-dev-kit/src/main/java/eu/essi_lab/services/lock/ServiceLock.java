package eu.essi_lab.services.lock;

/**
 * @author Fabrizio
 */
public interface ServiceLock {

    /**
     * @param serviceId
     * @return
     */
    static String getKey(String serviceId) {

	return "service:" + serviceId + ":lock";
    }

    /**
     * @param key
     * @return
     */
    static String getServiceId(String key) {

	return key.replace("service:", "").replace(":lock", "");
    }

    /**
     * @return
     */
    boolean tryAcquire();

    /**
     * @return
     */
    boolean renew();

    /**
     *
     */
    void release();

    /**
     * @return
     */
    String getValue();
}
