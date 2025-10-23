package eu.essi_lab.cfga.scheduler;

import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleInstanceIdGenerator;
import org.quartz.spi.InstanceIdGenerator;

/**
 * @author Mattia Santoro
 */
public class ESSIQuartzInstanceIdGenerator extends SimpleInstanceIdGenerator implements InstanceIdGenerator {

    @Override
    public String generateInstanceId() {
	try {
	    return super.generateInstanceId();
	} catch (SchedulerException e) {

	    return HostNamePropertyUtils.getHostNameProperty() + System.currentTimeMillis() + "-essi";
	}
    }
}
