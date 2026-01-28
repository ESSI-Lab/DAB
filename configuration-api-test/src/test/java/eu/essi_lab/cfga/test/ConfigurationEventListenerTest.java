package eu.essi_lab.cfga.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationChangeListener;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationEventListenerTest {

    private int eventsCount;

    /**
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

	Configuration configuration = new Configuration(new FileSource());

	configuration.addChangeEventListener(new ConfigurationChangeListener() {

	    @Override
	    public void configurationChanged(ConfigurationChangeEvent event) {

		switch (event.getEventType()) {
		case EventType.SETTING_PUT:
		    Assert.assertEquals("settingId", event.getSettings().getFirst().getIdentifier());
		    eventsCount++;
		    break;
		case EventType.SETTING_REMOVED:
		    Assert.assertEquals("settingId", event.getSettings().getFirst().getIdentifier());
		    eventsCount++;
		    break;
		case EventType.SETTING_REPLACED:
		    Assert.assertEquals("settingId", event.getSettings().getFirst().getIdentifier());
		    eventsCount++;
		    break;
		case EventType.CONFIGURATION_CLEARED:
		    eventsCount++;
		    break;
		case EventType.CONFIGURATION_FLUSHED:
		    eventsCount++;
		    break;
		}
	    }
	});

	Setting setting = new Setting();
	setting.setIdentifier("settingId");

	//
	//
	//
	configuration.put(setting);

	//
	//
	//
	setting.setCanBeDisabled(false);
	configuration.replace(setting);

	//
	//
	//
	configuration.remove(setting.getIdentifier());

	//
	//
	//

	configuration.clear();

	//
	//
	//

	configuration.flush();

	//
	//
	//

	Assert.assertEquals(5, eventsCount);

    }
}
