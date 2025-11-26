package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gui.extension.*;

/**
 * @author Fabrizio
 */
public class Sources extends TabPlaceholder {

    /**
     *
     */
    public Sources() {

	setLabel("Sources");

	setIndex(GSTabIndex.SOURCES.getIndex());

	addDescriptors(
		new SourcePrioritySetting.SourcePrioritySettingComponentInfo().getDescriptor(), //
		new SourceStorageSetting.SourceStorageSettingComponentInfo().getDescriptor(),//
		new GDCSourcesSetting.GDCSettingComponentInfo().getDescriptor()
	);
    }
}
