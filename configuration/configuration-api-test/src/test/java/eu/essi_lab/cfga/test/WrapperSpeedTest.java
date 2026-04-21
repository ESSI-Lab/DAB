package eu.essi_lab.cfga.test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public class WrapperSpeedTest {

    private static final int TEST_COUNT = 10;

    private static final int HARVESTED_SOURCES_COUNT = 300;
    private static final int DISTRIBUTED_SOURCES_COUNT = 100;

    //
    //
    //

    public enum Method {

	GET_ACCESSORS_SETTINGS_TIME, //

	GET_DISTRIBUTED_ACCESSORS_SETTINGS_TIME, //

	GET_HARVESTED_ACCESSORS_SETTINGS_TIME, //

	GET_MIXED_ACCESSORS_SETTINGS_TIME, //

	//
	//
	//

	GET_ALL_SOURCES_TIME, //

	GET_HARVESTED_SOURCES_TIME, //

	GET_DISTRIBUTED_SOURCES_TIME, //

	GET_HARVESTED_AND_MIXED_SOURCES_TIME, //

	GET_MIXED_SOURCES_TIME, //

	//
	//
	//

	GET_AUGMENTER_WORKER_SETTINGS_TIME, //

	GET_DISTRIBUTION_SETTINGS_TIME, //

	GET_HARVESTING_SETTINGS, //

	GET_PROFILER_SETTINGS, //

	//
	//
	//

	TO_JSON_ARRAY_TIME, //

	LIST_TIME;
    }

    //
    //
    //

    private HashMap<Method, Long> timeMap;

    private Chronometer chronometer;

    private HarvestingSetting harvesterWorkerSetting;

    private DefaultConfiguration configuration;

    private Long totalTime;

    /**
     * @throws Exception
     */
    public WrapperSpeedTest() throws Exception {

	timeMap = new LinkedHashMap<>();

	configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	harvesterWorkerSetting = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		findFirst().//
		get();

	AccessorSetting accessorSetting = ConfigurationWrapper.getDistributedAccessorSettings().get(0);

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	for (int i = 0; i < HARVESTED_SOURCES_COUNT; i++) {
	    
	    Setting clone = harvesterWorkerSetting.clone();

	    clone.setIdentifier(UUID.randomUUID().toString());

	    configuration.put(clone);
	}

	for (int i = 0; i < DISTRIBUTED_SOURCES_COUNT; i++) {
	    
	    Setting clone = accessorSetting.clone();

	    clone.setIdentifier(UUID.randomUUID().toString());

	    configuration.put(clone);
	}

	System.out.println("Put time: " + chronometer.formatElapsedTime());
    }

    @Test
    public void test() throws Exception {

	for (int i = 0; i < TEST_COUNT; i++) {

	    System.out.println("Test " + i + " STARTED");

	    _test();

	    System.out.println("Test " + i + " ENDED");
	}

	totalTime = 0l;

	timeMap.keySet().forEach(key -> {

	    Long time = timeMap.get(key) / TEST_COUNT;

	    totalTime += time;

	    System.out.println("[" + Chronometer.formatElapsedTime(time, TimeFormat.SEC_MLS) + "] " + key);
	});

	System.out.println("---");
	System.out
		.println("[" + Chronometer.formatElapsedTime(totalTime / Method.values().length, TimeFormat.SEC_MLS) + "] Total mean time");
    }

//    @Test
    public void harvestingSettingTest() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	harvesterWorkerSetting = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		findFirst().//
		get();

	//
	//
	//

	for (int i = 0; i < HARVESTED_SOURCES_COUNT; i++) {

	    harvesterWorkerSetting.setIdentifier(UUID.randomUUID().toString());

	    configuration.put(harvesterWorkerSetting);
	}
	Chronometer chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestingSettings();

	System.out.println("TIME1: " + chronometer.formatElapsedTime());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestingSettings();
	System.out.println("TIME2: " + chronometer.formatElapsedTime());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestingSettings();
	System.out.println("TIME3: " + chronometer.formatElapsedTime());
    }

    /**
     * 
     */
    private void _test() throws Exception {

	// System.out.println(configuration.toJSONArray());

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getAccessorSettings();

	timeMap.compute(Method.GET_ACCESSORS_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getDistributedAccessorSettings();

	timeMap.compute(Method.GET_DISTRIBUTED_ACCESSORS_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestedAccessorSettings();

	timeMap.compute(Method.GET_HARVESTED_ACCESSORS_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getMixedAccessorSettings();

	timeMap.compute(Method.GET_MIXED_ACCESSORS_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getAllSources();

	timeMap.compute(Method.GET_ALL_SOURCES_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestedSources();

	timeMap.compute(Method.GET_HARVESTED_SOURCES_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getDistributedSources();

	timeMap.compute(Method.GET_DISTRIBUTED_SOURCES_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getMixedSources();

	timeMap.compute(Method.GET_MIXED_SOURCES_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestedAndMixedSources();

	timeMap.compute(Method.GET_HARVESTED_AND_MIXED_SOURCES_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getAugmenterWorkerSettings();

	timeMap.compute(Method.GET_AUGMENTER_WORKER_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getDistributonSettings();

	timeMap.compute(Method.GET_DISTRIBUTION_SETTINGS_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getHarvestingSettings();

	timeMap.compute(Method.GET_HARVESTING_SETTINGS,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());
	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	ConfigurationWrapper.getProfilerSettings();

	timeMap.compute(Method.GET_PROFILER_SETTINGS,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	//
	//
	//

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	configuration.toJSONArray();

	timeMap.compute(Method.TO_JSON_ARRAY_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());

	chronometer = new Chronometer(TimeFormat.SEC_MLS);
	chronometer.start();

	configuration.list();

	timeMap.compute(Method.LIST_TIME,
		(k, v) -> (v == null) ? chronometer.getElapsedTimeMillis() : v + chronometer.getElapsedTimeMillis());
    }

}
