package eu.essi_lab.lib.net.service;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ServiceDefinition {

    public final String id;
    public final Class<? extends ManagedService> clazz;

    /**
     * @param id
     * @param clazz
     */
    private ServiceDefinition(String id, Class<? extends ManagedService> clazz) {

	this.id = id;
	this.clazz = clazz;
    }

    /**
     * @param id
     * @param clazz
     * @return
     */
    public static ServiceDefinition of(String id, Class<? extends ManagedService> clazz) {

	return new ServiceDefinition(id, clazz);
    }

    /**
     * @return
     */
    public String getId() {

	return id;
    }

    /**
     * @return
     */
    public Class<? extends ManagedService> getServiceClass() {

	return clazz;
    }

    /**
     * @return
     */
    public ManagedService create() {

	try {

	    ManagedService service = clazz.getDeclaredConstructor().newInstance();
	    service.setId(id);

	    return service;

	} catch (Exception e) {

	    throw new RuntimeException(e);
	}
    }

    @Override
    public boolean equals(Object o) {

	return o instanceof ServiceDefinition def && //
		Objects.equals(id, def.id) && Objects.equals(clazz, def.clazz);
    }
}
