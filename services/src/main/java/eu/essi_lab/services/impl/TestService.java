package eu.essi_lab.services.impl;

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.message.*;

import java.time.*;
import java.time.temporal.*;

/**
 * @author Fabrizio
 */
public class TestService extends AbstractManagedService {

    private boolean running;

    /**
     *
     */
    public TestService() {
    }

    @Override
    public void start() {

	running = true;

	publish(MessageChannel.MessageLevel.INFO, "Started service: " + getId());

	new Thread(() -> {

	    while (running) {

		GSLoggerFactory.getLogger(getClass()).info("*** [ Running service: {} ] ***", getId());

		getSetting().getServiceOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Options: {}", o));
		getSetting().getKeyValueOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Key-value options: {}", o));

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

}
