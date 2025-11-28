package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;

/**
 * @author Fabrizio
 */
public class SourcesTabDescriptor extends TabDescriptor {

    /**
     *
     */
    public SourcesTabDescriptor() {

	setLabel("Sources");

	setIndex(GSTabIndex.SOURCES.getIndex());

	addContentDescriptors(
		new SourceStorageSetting.DescriptorProvider().get(),//
		new GDCSourcesSetting.DescriptorProvider().get()
	);
    }
}
