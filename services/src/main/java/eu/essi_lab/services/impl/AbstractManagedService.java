package eu.essi_lab.services.impl;

import eu.essi_lab.services.*;

/**
 * @author Fabrizio
 */
public abstract class AbstractManagedService implements ManagedService {

    private String id;
    private ManagedServiceSetting setting;

    /**
     * @param id
     */
    @Override
    public void setId(String id) {

	this.id = id;
    }

    @Override
    public String getId() {

	return id;
    }


    @Override
    public void configure(ManagedServiceSetting setting) {

        this.setting = setting;
    }

    @Override
    public ManagedServiceSetting getSetting() {

        return setting;
    }
}
