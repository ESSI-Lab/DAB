package eu.essi_lab.cfga.test;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Configuration.State;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * This test simulates a use case with 3 configurations (2 readonly) sharing the same file source and
 * with an autoreload time of 15 seconds. <br>
 * After 5 seconds, the RW config changes a setting and flushes. At this time, the other configs are expected to be
 * out of synch. <br>
 * After 17 seconds, all the configs are expected to be synchronized
 * 
 * @author Fabrizio
 */
public class MultipleConfigurationsTest {

    private boolean testPassed = true;
    private boolean testEnded = false;

    @Test
    public void test() throws Exception {

	FileSource filesSource = new FileSource();

	//
	// Creates the read/write configuration with one setting and
	// a reload time of 15 seconds
	//

	Configuration rwConfig = new Configuration(filesSource, TimeUnit.SECONDS, 15);
	rwConfig.clear();

	Assert.assertEquals(new Integer(15), rwConfig.getAutoreloadInterval().get());
	Assert.assertEquals(TimeUnit.SECONDS, rwConfig.getAutoreloadTimeUnit().get());

	Setting setting = new Setting();

	setting.setDescription("Description");
	setting.setName("Name");

	rwConfig.put(setting);
	rwConfig.flush();

	List<Setting> list = rwConfig.list();
	Assert.assertEquals(1, list.size());

	//
	// Creates read configurations 1 and 2 with reload time of 15 seconds
	//
	// Now, all configuration are equals
	//

	Configuration rConfig1 = new Configuration(filesSource, TimeUnit.SECONDS, 15);
	Configuration rConfig2 = new Configuration(filesSource, TimeUnit.SECONDS, 15);

	Assert.assertEquals(rConfig1.toString(), rwConfig.toString());
	Assert.assertEquals(rConfig2.toString(), rwConfig.toString());
	Assert.assertEquals(rConfig1.toString(), rConfig2.toString());

	//
	// After 5 seconds, the RW config changes name and description of the item
	//
	TimerTask timerTask1 = new TimerTask() {

	    @Override
	    public void run() {

		Setting setting = rwConfig.list().get(0);

		setting.setDescription("New description");
		setting.setName("New name");

		rwConfig.replace(setting);

		testPassed &= "New description".equals(rwConfig.list().get(0).getDescription().get());
		testPassed &= ("New name".equals(rwConfig.list().get(0).getName()));

		//
		// now the RW config is dirty
		//
		Assert.assertEquals(State.DIRTY, rwConfig.getState());

		try {
		    rwConfig.flush();

		    Assert.assertEquals(State.SYNCH, rwConfig.getState());

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		    testPassed = false;
		}

		//
		// now, since the configs autoreload time is 15 seconds, the read-only configs
		// should be out of synch
		//

		testPassed &= "Description".equals(rConfig1.list().get(0).getDescription().get());
		testPassed &= ("Name".equals(rConfig1.list().get(0).getName()));

		testPassed &= "Description".equals(rConfig2.list().get(0).getDescription().get());
		testPassed &= ("Name".equals(rConfig2.list().get(0).getName()));

		//
		// the readonly configs state is always synch, since a config is dirty only when its
		// write methods are successfully invoked
		//
		Assert.assertEquals(State.SYNCH, rConfig1.getState());
		Assert.assertEquals(State.SYNCH, rConfig2.getState());

		testPassed &= !rwConfig.toString().equals(rConfig1.toString());
		testPassed &= !rwConfig.toString().equals(rConfig2.toString());
		testPassed &= rConfig1.toString().equals(rConfig2.toString());
	    }
	};

	Timer timer1 = new Timer();
	timer1.schedule(timerTask1, TimeUnit.SECONDS.toMillis(5));

	//
	// After 17 seconds, all the configs should now be synch
	//
	TimerTask timerTask2 = new TimerTask() {

	    @Override
	    public void run() {

		testPassed &= "New description".equals(rConfig1.list().get(0).getDescription().get());
		testPassed &= ("New name".equals(rConfig1.list().get(0).getName()));

		testPassed &= "New description".equals(rConfig2.list().get(0).getDescription().get());
		testPassed &= ("New name".equals(rConfig2.list().get(0).getName()));

		testPassed &= rwConfig.toString().equals(rConfig1.toString());
		testPassed &= rwConfig.toString().equals(rConfig2.toString());
		testPassed &= rConfig1.toString().equals(rConfig2.toString());

		testEnded = true;
	    }
	};

	Timer timer2 = new Timer();
	timer2.schedule(timerTask2, TimeUnit.SECONDS.toMillis(17));

	while (!testEnded) {
	    Thread.sleep(1000);
	}

	Assert.assertTrue(testPassed);
    }
}
