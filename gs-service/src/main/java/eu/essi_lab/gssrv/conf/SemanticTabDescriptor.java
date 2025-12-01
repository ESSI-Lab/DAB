package eu.essi_lab.gssrv.conf;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.distribution.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gs.setting.ontology.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;

/**
 * @author Fabrizio
 */
public class SemanticTabDescriptor extends TabDescriptor {

    /**
     *
     */
    public SemanticTabDescriptor() {

	setLabel("Semantic");

	setIndex(GSTabIndex.SEMANTICS.getIndex());

	addContentDescriptors(//
		new OntologySetting.DescriptorProvider().get(), //
		new DefaultSemanticSearchSetting.DescriptorProvider().get()//
	);
    }
}
