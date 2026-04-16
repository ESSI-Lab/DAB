package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;

/**
 * @author Fabrizio
 */
public class RateLimiterSettingEditableTest {

    @Test
    public void test() {

	RateLimiterSetting setting = new RateLimiterSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
