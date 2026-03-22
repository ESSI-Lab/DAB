package eu.essi_lab.gssrv.services;

import eu.essi_lab.lib.net.service.*;
import eu.essi_lab.lib.utils.*;

import java.time.*;
import java.time.temporal.*;

/**
 * @author Fabrizio
 */
public class TestService implements ManagedService {

    private volatile boolean running = false;
    private String id;
    private boolean enabled;

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

	new Thread(() -> {
	    while (running) {

		GSLoggerFactory.getLogger(getClass()).info("*** [ Running service: {} ] ***", getId());
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
    }
}
