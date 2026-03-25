package eu.essi_lab.services.impl;

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.*;
import eu.essi_lab.services.message.*;

import java.time.*;
import java.time.temporal.*;

/**
 * @author Fabrizio
 */
public class TestService implements ManagedService {

    private volatile boolean running = false;
    private String id;
    private boolean enabled;
    private ManagedServiceSetting setting;

    /**
     *
     */
    public TestService() {
    }

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
    public void start() {

	running = true;

	publish(MessageChannel.MessageLevel.INFO, "Started service: " + getId());

	new Thread(() -> {

	    while (running) {

		GSLoggerFactory.getLogger(getClass()).info("*** [ Running service: {} ] ***", getId());

		setting.getServiceOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Options: {}", o));
		setting.getKeyValueOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Key-value options: {}", o));

		publish(MessageChannel.MessageLevel.INFO, "Running service: " + getId());

		try {
		    Thread.sleep(Duration.of(5, ChronoUnit.SECONDS));
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}).start();
    }

    @Override
    public void stop() {

	running = false;

	publish(MessageChannel.MessageLevel.INFO, "Stopped service: " + getId());

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
