package eu.essi_lab.cfga.gs.setting.test;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.WMSCacheInvalidationSetting;
import eu.essi_lab.cfga.gs.setting.WMSCacheInvalidationSetting.TriggerCondition;
import eu.essi_lab.cfga.gs.setting.WMSCacheSetting;

/**
 * @author boldrini
 */
public class WMSCacheInvalidationSettingTest {

    @Test
    public void defaultTest() {

	WMSCacheSetting setting = new WMSCacheSetting();

	// disabled by default
	Assert.assertTrue(setting.getInvalidationSetting().isEmpty());

	test(new WMSCacheSetting(setting.getObject()), false);
	test(new WMSCacheSetting(setting.getObject().toString()), false);
    }

    @Test
    public void enabledTest() {

	WMSCacheSetting setting = new WMSCacheSetting();

	WMSCacheInvalidationSetting invalidation = setting.getSettings(WMSCacheInvalidationSetting.class).getFirst();
	invalidation.setEnabled(true);
	invalidation.setViews(" his-central , other-view,,his-central ");
	invalidation.setTriggerCondition(TriggerCondition.EVERY_COMPLETED_HARVESTING);

	test(setting, true);
	test(new WMSCacheSetting(setting.getObject()), true);
	test(new WMSCacheSetting(setting.getObject().toString()), true);
    }

    /**
     * @param setting
     * @param enabled
     */
    private void test(WMSCacheSetting setting, boolean enabled) {

	if (!enabled) {

	    Assert.assertTrue(setting.getInvalidationSetting().isEmpty());

	    WMSCacheInvalidationSetting invalidation = setting.getSettings(WMSCacheInvalidationSetting.class).getFirst();

	    Assert.assertEquals(List.of(), invalidation.getViews());
	    Assert.assertEquals(TriggerCondition.RECORDS_COUNT_CHANGED, invalidation.getTriggerCondition());

	    return;
	}

	WMSCacheInvalidationSetting invalidation = setting.getInvalidationSetting().get();

	Assert.assertEquals(List.of("his-central", "other-view"), invalidation.getViews());
	Assert.assertEquals(TriggerCondition.EVERY_COMPLETED_HARVESTING, invalidation.getTriggerCondition());
    }
}
