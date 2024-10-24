package eu.essi_lab.cfga.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Configuration.State;
import eu.essi_lab.cfga.ConfigurationChangeListener;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;

/*
 * 
 */
public class ConfigurationCloneTest {

    /**
     * @throws Exception
     */
    @Test
    public void clonedListTest() throws Exception {

	Configuration original = new Configuration();

	//
	//
	//

	Configuration clone = original.clone();

	//
	//
	//

	original.put(new Setting());

	//
	//
	//

	Assert.assertEquals(1, original.list().size());

	Assert.assertEquals(0, clone.list().size());

	//
	//
	//

	clone.put(new Setting());

	//
	//
	//

	Assert.assertEquals(1, original.list().size());

	Assert.assertEquals(1, clone.list().size());

	//
	//
	//

	original.clear();

	Assert.assertEquals(0, original.list().size());

	Assert.assertEquals(1, clone.list().size());

    }

    /**
     * @throws Exception
     */
    @Test
    public void dirtyFlagTest() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration original = new Configuration(filesSource);

	List<Setting> list = Arrays.asList(new Setting(), new Setting(), new Setting());

	filesSource.flush(list);

	original.reload();

	//
	//
	//

	original.put(new Setting());

	//
	//
	//

	Configuration clone = original.clone();

	//
	//
	//

	Assert.assertEquals(State.DIRTY, original.getState());
	Assert.assertEquals(State.DIRTY, clone.getState());

	//
	//
	//

	Assert.assertEquals(4, original.list().size());
	Assert.assertEquals(4, clone.list().size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void sourceTest() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration original = new Configuration(filesSource);

	List<Setting> list = Arrays.asList(new Setting(), new Setting(), new Setting());

	filesSource.flush(list);

	original.reload();

	//
	//
	//

	Configuration clone = original.clone();

	//
	//
	//

	Assert.assertEquals(3, original.getSource().list().size());
	Assert.assertEquals(3, clone.getSource().list().size());

	//
	//
	//

	Assert.assertEquals(original.getSource().list(), clone.getSource().list());
    }

    /**
     * @throws Exception
     */
    @Test
    public void listenerListTest() throws Exception {

	Configuration original = new Configuration();

	original.addChangeEventListener(new ConfigurationChangeListener() {
	    @Override
	    public void configurationChanged(ConfigurationChangeEvent event) {
	    }
	});

	//
	//
	//

	Configuration clone = original.clone();

	//
	//
	//

	Assert.assertEquals(1, original.getListenerList().size());
	Assert.assertEquals(0, clone.getListenerList().size());
    }

    /**
     * @throws Exception
     */
    @Test
    public void autoreloadTest() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration original = new Configuration(filesSource);

	Optional<Integer> autoreloadInterval = original.getAutoreloadInterval();
	Optional<TimeUnit> autoreloadTimeUnit = original.getAutoreloadTimeUnit();

	Assert.assertFalse(autoreloadInterval.isPresent());
	Assert.assertFalse(autoreloadTimeUnit.isPresent());

	original.autoreload(TimeUnit.DAYS, 5);

	//
	//
	//

	Configuration clone = original.clone();

	//
	//
	//

	Assert.assertEquals(TimeUnit.DAYS, original.getAutoreloadTimeUnit().get());
	Assert.assertEquals(new Integer(5), original.getAutoreloadInterval().get());

	Assert.assertFalse(clone.getAutoreloadInterval().isPresent());
	Assert.assertFalse(clone.getAutoreloadTimeUnit().isPresent());
    }
}
