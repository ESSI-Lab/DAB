package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.distribution.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.extension.*;

/**
 * @author Fabrizio
 */
public class Sources extends ComponentInfo {

    /**
     *
     */
    public Sources() {

	setName("Sources");

	setPlaceholder(TabPlaceholder.of(GSTabIndex.SOURCES.getIndex(),//

		new SourcePrioritySetting.SourcePrioritySettingComponentInfo().getDescriptor(), //
		new SourceStorageSetting.SourceStorageSettingComponentInfo().getDescriptor(),//
		new GDCSourcesSetting.GDCSettingComponentInfo().getDescriptor()));
    }
}
