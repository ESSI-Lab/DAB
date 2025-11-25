package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.distribution.*;
import eu.essi_lab.cfga.gs.setting.driver.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.*;
import eu.essi_lab.cfga.gui.extension.*;

/**
 *
 * @author Fabrizio
 *
 */
public class System extends ComponentInfo {

    /**
     *
     */
    public System() {

	setName("System");

	setPlaceholder(TabPlaceholder.of(GSTabIndex.SYSTEM.getIndex(),//

		new SystemSetting.SystemSettingComponentInfo().getDescriptor(), //
		new DatabaseSetting.DatabaseComponentInfo().getDescriptor(),//
		new DriverSetting.DriverComponentInfo().getDescriptor(),//
		new SchedulerViewSetting.SchedulerSettingComponentInfo().getDescriptor(),//
		new DownloadSetting.DownloadSettingComponentInfo().getDescriptor(),//
		new RateLimiterSetting.RateLimiterSettingComponentInfo().getDescriptor(),//
		new ConfigHandler().getDescriptor()

	));
    }
}
