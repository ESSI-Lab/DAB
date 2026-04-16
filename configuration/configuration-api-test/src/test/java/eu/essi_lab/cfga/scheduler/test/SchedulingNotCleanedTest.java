package eu.essi_lab.cfga.scheduler.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
 
/**
 * 
 * @author Fabrizio
 *
 */
public class SchedulingNotCleanedTest {

    /**
     * 
     */
    @Test
    public void test(){
	
	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();
	
	Assert.assertNotNull(setting.getScheduling());
	
	SelectionUtils.deepClean(setting);
	
	Assert.assertNotNull(setting.getScheduling());

    }
}
