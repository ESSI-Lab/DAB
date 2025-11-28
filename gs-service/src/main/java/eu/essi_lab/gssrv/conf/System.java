package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.driver.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;

/**
 *
 * @author Fabrizio
 *
 */
public class System extends TabDescriptor {

    /**
     *
     */
    public System() {

	setLabel("System");

	setIndex(GSTabIndex.SYSTEM.getIndex());

	addContentDescriptors(
		new SystemSetting.DescriptorProvider().get(), //
		new DatabaseSetting.DescriptorProvider().get(),//
		new DriverSetting.DescriptorProvider().get(),//
		new SchedulerViewSetting.DescriptorProvider().get(),//
		new DownloadSetting.DescriptorProvider().get(),//
		new RateLimiterSetting.DescriptorProvider().get(),//
		new ConfigHandler().get()
	);
    }
}
