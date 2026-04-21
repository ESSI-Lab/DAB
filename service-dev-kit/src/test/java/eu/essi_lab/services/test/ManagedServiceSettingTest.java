package eu.essi_lab.services.test;

import eu.essi_lab.cfga.*;
import eu.essi_lab.services.*;
import eu.essi_lab.services.impl.*;
import org.junit.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ManagedServiceSettingTest {

    /**
     *
     */
    @Test
    public void initTest() {

	ManagedServiceSetting setting = new ManagedServiceSetting();

	String serviceId = setting.getServiceId();
	String selectedImpl = setting.getSelectedImpl();
	Class<? extends ManagedService> selectedImplClass = setting.getSelectedImplClass();
	String serviceDescription = setting.getServiceDescription();
	Optional<String> serviceOptions = setting.getServiceOptions();

	Assert.assertNull(serviceId);
	Assert.assertNull(selectedImpl);
	Assert.assertNull(selectedImplClass);
	Assert.assertEquals("No description provided", serviceDescription);
	Assert.assertTrue(serviceOptions.isEmpty());
    }

    @Test
    public void setTest() {

	ManagedServiceSetting setting = new ManagedServiceSetting();
	setting.loadServiceImpl();

	setting.setServiceId("test");
	setting.setServiceDescription("desc");
	setting.setServiceOptions("options");
	setting.selectImpl(TestService.class.getName());

	SelectionUtils.deepClean(setting);

	Assert.assertEquals("test", setting.getServiceId());
	Assert.assertEquals("desc", setting.getServiceDescription());
	Assert.assertEquals("options", setting.getServiceOptions().get());
	Assert.assertEquals(TestService.class.getName(), setting.getSelectedImpl());
    }

    @Test
    public void editableTest() {

	ManagedServiceSetting setting = new ManagedServiceSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
