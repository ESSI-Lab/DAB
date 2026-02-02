package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.orderedlayout.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;

/**
 * @author Fabrizio
 */
public class AsynchDownloadDescriptor extends TabDescriptor {

    /**
     *
     */
    public AsynchDownloadDescriptor() {

	setLabel("Downloads");

	VerticalLayout verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "-20px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withComponent(verticalLayout).//
		build();

	setIndex(GSTabIndex.ASYNC_DOWNLOADS.getIndex());
	addContentDescriptor(descriptor);
    }

}
