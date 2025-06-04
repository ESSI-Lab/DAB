package eu.essi_lab.gssrv.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.gssrv.starter.DABStarter;
import eu.essi_lab.lib.utils.GSLoggerFactory;

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
	setTitle("Save confirmation");
	setWidth(500, Unit.PIXELS);

	getFooterLayout().getStyle().set("border-top", "1px solid lightgray");

	VerticalLayout verticalLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	verticalLayout.getStyle().set("overflow-y", "auto");
	verticalLayout.getStyle().set("height", "400px");

	setContent(verticalLayout);

	setOnConfirmListener(new ButtonChangeListener() {

	    @Override
	    public void handleEvent(ClickEvent<Button> event) {

		onConfigurationFlushConfirmed();
	    }
	});

	if (!GSConfigurationView.putSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("New settings", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.putSettingList.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	if (!GSConfigurationView.editedSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("Edited settings", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.editedSettingList.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	if (!GSConfigurationView.removedSettingList.isEmpty()) {

	    {

		Label label = ComponentFactory.createLabel("Removed settings", 15);
		label.getStyle().set("font-weight", "bold");

		verticalLayout.add(label);

		GSConfigurationView.removedSettingList.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	    }

	    {
		if (!GSConfigurationView.additionalRemovalInfo.isEmpty()) {

		    Label label = ComponentFactory.createLabel("Deselected settings", 15);
		    label.getStyle().set("font-weight", "bold");

		    verticalLayout.add(label);

		    GSConfigurationView.additionalRemovalInfo.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s, 14)));
		}
	    }
	}

	if (!GSConfigurationView.newWorkerSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("New scheduled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.newWorkerSettingList.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	if (!GSConfigurationView.pausedWorkerSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("Paused/disabled scheduled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.pausedWorkerSettingList.forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	if (!GSConfigurationView.rescheduledWorkerSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("Rescheduled/enabled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.rescheduledWorkerSettingList
		    .forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	if (!GSConfigurationView.unscheduledWorkerSettingList.isEmpty()) {

	    Label label = ComponentFactory.createLabel("Unscheduled jobs", 15);
	    label.getStyle().set("font-weight", "bold");

	    verticalLayout.add(label);

	    GSConfigurationView.unscheduledWorkerSettingList
		    .forEach(s -> verticalLayout.add(ComponentFactory.createLabel(s.getName(), 14)));
	}

	Label label = ComponentFactory.createLabel("Click 'confirm' to save the configuration and apply all the changes'", 15);

	label.getStyle().set("margin-top", "20px");
	label.getStyle().set("margin-bottom", "5px");

	verticalLayout.add(label);
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
