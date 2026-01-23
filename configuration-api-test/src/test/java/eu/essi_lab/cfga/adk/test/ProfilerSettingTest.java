/**
 *
 */
package eu.essi_lab.cfga.adk.test;

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.profiler.os.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class ProfilerSettingTest {

    /**
     *
     */
    @Test
    public void test() {

	OSProfilerSetting setting = new OSProfilerSetting();

	//
	//
	//

	Assert.assertTrue(setting.isOnline());

	setting.setOnline(false);

	Assert.assertFalse(setting.isOnline());

	setting.setOnline(true);

	Assert.assertTrue(setting.isOnline());

	Assert.assertFalse(setting.getConsumer().isPresent());

	Assert.assertEquals(ProfilerSetting.DEFAULT_RSM_THREADS_COUNT, setting.getResultSetMapperThreadsCount().get().toString());

	//
	//
	//

	String serviceType = setting.getServiceType();
	String configurableType = setting.getConfigurableType();

	Assert.assertEquals(serviceType, configurableType);

	setting.setServiceType("type");

	// the "Profiler" postfix is added to be sure
	// is different from accessors type
	Assert.assertEquals("typeProfiler", setting.getServiceType());
	Assert.assertEquals("typeProfiler", setting.getConfigurableType());

	//
	//
	//

	setting.setServicePath("path");
	setting.setServiceName("name");
	setting.setServiceVersion("version");

	Assert.assertEquals("path", setting.getServicePath());
	Assert.assertEquals("name", setting.getServiceName());
	Assert.assertEquals("version", setting.getServiceVersion());

	//
	//
	//

	setting.putKeyValue(ProfilerSetting.KeyValueOptionKeys.RSM_THREADS_COUNT_PROPERTY.getLabel(), "-1");

	Assert.assertFalse(setting.getResultSetMapperThreadsCount().isPresent());

	setting.putKeyValue(ProfilerSetting.KeyValueOptionKeys.RSM_THREADS_COUNT_PROPERTY.getLabel(), "11");

	Assert.assertEquals(Integer.valueOf(11), setting.getResultSetMapperThreadsCount().get());

    }
}
