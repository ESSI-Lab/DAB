package eu.essi_lab.gssrv.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.gssrv.starter.*;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
class SaveConfirmationDialog extends ConfirmationDialog {

    private GSConfigurationView view;

    /**
     *
     */
    public SaveConfirmationDialog(GSConfigurationView view) {

	addToCloseAll();

	this.view = view;
	setHeader("Save confirmation");
	setWidth(500, Unit.PIXELS);

	getContentLayout().getStyle().set("padding", "0px");
	getContentLayout().getStyle().set("padding-top", "20px");

	VerticalLayout mainLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	mainLayout.getStyle().set("padding", "0px");
	mainLayout.getStyle().set("padding-right", "15px");
	mainLayout.getStyle().set("height", "400px");

	setContent(mainLayout);

	VerticalLayout contentLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	contentLayout.getStyle().set("padding", "0px");
	contentLayout.getStyle().set("overflow-y", "auto");
	contentLayout.getStyle().set("height", "360px");

	mainLayout.add(contentLayout);

	setOnConfirmListener(new ButtonChangeListener() {

	    @Override
	    public void handleEvent(ClickEvent<Button> event) {

		onConfigurationFlushConfirmed();
	    }
	});

	if (!GSConfigurationView.putSettingList.isEmpty()) {

	    Span label = ComponentFactory.createSpan("New settings", 15);
	    label.getStyle().set("font-weight", "bold");

	    contentLayout.add(label);

	    GSConfigurationView.putSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	}

	if (!GSConfigurationView.editedSettingList.isEmpty()) {

	    Span label = ComponentFactory.createSpan("Edited settings", 15);
	    label.getStyle().set("font-weight", "bold");

	    contentLayout.add(label);

	    GSConfigurationView.editedSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	}

	if (!GSConfigurationView.removedSettingList.isEmpty()) {

	    {

		Span label = ComponentFactory.createSpan("Removed settings", 15);
		label.getStyle().set("font-weight", "bold");

		contentLayout.add(label);

		GSConfigurationView.removedSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	    }

	    {
		if (!GSConfigurationView.additionalRemovalInfo.isEmpty()) {

		    Span label = ComponentFactory.createSpan("Deselected settings", 15);
		    label.getStyle().set("font-weight", "bold");

		    contentLayout.add(label);

		    GSConfigurationView.additionalRemovalInfo.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s, 14)));
		}
	    }
	}

	if (!GSConfigurationView.newWorkerSettingList.isEmpty()) {

	    Span label = ComponentFactory.createSpan("New scheduled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    contentLayout.add(label);

	    GSConfigurationView.newWorkerSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	}

	if (!GSConfigurationView.rescheduledWorkerSettingList.isEmpty()) {

	    Span label = ComponentFactory.createSpan("Rescheduled/enabled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    contentLayout.add(label);

	    GSConfigurationView.rescheduledWorkerSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	}

	if (!GSConfigurationView.unscheduledWorkerSettingList.isEmpty()) {

	    Span label = ComponentFactory.createSpan("Unscheduled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    contentLayout.add(label);

	    GSConfigurationView.unscheduledWorkerSettingList.forEach(s -> contentLayout.add(ComponentFactory.createSpan(s.getName(), 14)));
	}

	//
	//
	//

	Span label = ComponentFactory.createSpan("Click 'confirm' to save the configuration and apply all the changes'", 15);

	label.getStyle().set("margin-top", "20px");
	label.getStyle().set("margin-bottom", "5px");

	mainLayout.add(label);
    }

    /**
     *
     */
    private void onConfigurationFlushConfirmed() {

	// if (!LockManager.getInstance().isAcquired()) {
	//
	// NotificationDialog.getWarningDialog("Save permission not available").open();
	// return;
	// }

	try {

	    view.getConfiguration().flush();

	    //
	    // forces the reload to the wrapper configuration so in
	    // this node, the changes are already available without waiting for the autoreload
	    // useful for testing purpose
	    //
	    DABStarter.configuration.reload();

	    view.updateScheduler();

	    view.initLists();

	    view.getSaveButton().setEnabled(false);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    NotificationDialog.getErrorDialog(e.getMessage(), e).open();
	}
    }
}
