package eu.essi_lab.lib.net.service;

/**
 * @author Fabrizio
 */
public interface ManagedService {

    /**
     * @param id
     */
    public void setId(String id);

    /**
     *
     * @return
     */
    String getId();

    /**
     *
     */
    void start();

    /**
     *
     */
    void stop();

    /**
     *
     */
    default String getName(){

        return getClass().getSimpleName();
    }
}
