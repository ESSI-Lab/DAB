package eu.essi_lab.cfga.gs.setting.ratelimiter.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.ratelimiter.ExecutionModeSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.ComputationType;
import eu.essi_lab.configuration.ExecutionMode;

/**
 * @author Fabrizio
 */
public class RateLimiterSettingTest {

    @Test
    public void test() {

	RateLimiterSetting setting = new RateLimiterSetting();
	initTest(setting);
	initTest(new RateLimiterSetting(setting.getObject()));
	initTest(new RateLimiterSetting(setting.getObject().toString()));
	
	setting.setComputationType(ComputationType.DISTRIBUTED);
	
	ComputationType computationType = setting.getComputationType();
	Assert.assertEquals(ComputationType.DISTRIBUTED, computationType);
    }

    /**
     * @param
     */
    private void initTest(RateLimiterSetting setting) {

	ComputationType computationType = setting.getComputationType();
	Assert.assertEquals(ComputationType.DISABLED, computationType);
	
	//
	// Default settings
	//

	String defaultDB = setting.getDefaultDB();
	Assert.assertEquals("default", defaultDB);

	Integer defaultMaxRequestsPerIP = setting.getDefaultMaxRequestsPerIP();
	Assert.assertEquals(new Integer(10), defaultMaxRequestsPerIP);

	Integer defaultMaxConcurrentRequests = setting.getDefaultMaxConcurrentRequests();
	Assert.assertEquals(new Integer(5), defaultMaxConcurrentRequests);

	Integer defaultMaxConcurrentRequestsPerIP = setting.getDefaultMaxConcurrenRequestsPerIP();
	Assert.assertEquals(new Integer(1), defaultMaxConcurrentRequestsPerIP);

	String hostName = setting.getHostName();
	Assert.assertEquals("localhost", hostName);
	
	//
	// Access settings
	//

	Optional<ExecutionModeSetting> accessSetting = setting.getExecutionModeSetting(ExecutionMode.ACCESS);
	Assert.assertEquals("default", accessSetting.get().getDB());
	Assert.assertEquals(new Integer(10), accessSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), accessSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), accessSetting.get().getMaxConcurrentRequestsPerIP());

	//
	// Augmenter settings
	//

	Optional<ExecutionModeSetting> augmenterSetting = setting.getExecutionModeSetting(ExecutionMode.AUGMENTER);
	Assert.assertEquals("default", augmenterSetting.get().getDB());
	Assert.assertEquals(new Integer(10), augmenterSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), augmenterSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), augmenterSetting.get().getMaxConcurrentRequestsPerIP());

	//
	// Frontend settings
	//
	
	Optional<ExecutionModeSetting> frontendSetting = setting.getExecutionModeSetting(ExecutionMode.FRONTEND);
	Assert.assertEquals("default", frontendSetting.get().getDB());
	Assert.assertEquals(new Integer(10), frontendSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), frontendSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), frontendSetting.get().getMaxConcurrentRequestsPerIP());

	//
	// Intensive settings
	//
	
	Optional<ExecutionModeSetting> intensiveSetting = setting.getExecutionModeSetting(ExecutionMode.INTENSIVE);
	Assert.assertEquals("default", intensiveSetting.get().getDB());
	Assert.assertEquals(new Integer(10), intensiveSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), intensiveSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), intensiveSetting.get().getMaxConcurrentRequestsPerIP());

	//
	// Local production settings
	//
	
	Optional<ExecutionModeSetting> localProdSetting = setting.getExecutionModeSetting(ExecutionMode.LOCAL_PRODUCTION);
	Assert.assertEquals("default", localProdSetting.get().getDB());
	Assert.assertEquals(new Integer(10), localProdSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), localProdSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), localProdSetting.get().getMaxConcurrentRequestsPerIP());

	//
	// Mixed settings
	//
	
	Optional<ExecutionModeSetting> mixedSetting = setting.getExecutionModeSetting(ExecutionMode.MIXED);
	Assert.assertEquals("default", mixedSetting.get().getDB());
	Assert.assertEquals(new Integer(10), mixedSetting.get().getMaxRequestsPerIP());
	Assert.assertEquals(new Integer(5), mixedSetting.get().getMaxConcurrentRequests());
	Assert.assertEquals(new Integer(1), mixedSetting.get().getMaxConcurrentRequestsPerIP());
	
	//
	// Unsupported execution modes
	//
	
	Assert.assertFalse(setting.getExecutionModeSetting(ExecutionMode.BATCH).isPresent());
	Assert.assertFalse(setting.getExecutionModeSetting(ExecutionMode.BULK).isPresent());
	Assert.assertFalse(setting.getExecutionModeSetting(ExecutionMode.CONFIGURATION).isPresent());
    }
}
