package eu.essi_lab.cfga.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;

import eu.essi_lab.cfga.gui.components.grid.ContextMenuItem;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public abstract class TaskStarter implements ContextMenuItem, ButtonChangeListener {

    protected TextArea textArea;
    protected Checkbox forceVolatileScheduler;
    protected VaadinSession session;
    protected ConfirmationDialog dialog;

    /**
     * 
     */
    public TaskStarter() {
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	dialog.getConfirmButton().setEnabled(false);
	dialog.getConfirmButton().getStyle().set("color", "lightgray");
	dialog.getConfirmButton().getStyle().set("background-color", "white");

	SchedulerWorkerSetting setting = getSetting();

	setting.getScheduling().setEnabled(true);
	setting.getScheduling().setRunOnce();

	Boolean force = forceVolatileScheduler.getValue();

	Scheduler scheduler = force ? SchedulerFactory.getVolatileScheduler()
		: SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	try {

	    scheduler.addJobEventListener((e, c, ex) -> {

		onJobExecuted(e, c, ex);

	    }, JobEvent.JOB_EXECUTED, //
		    UUID.randomUUID().toString(), //
		    true); //

	    updateText(getStartTaskText());

	    scheduler.schedule(setting);

	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    NotificationDialog.getErrorDialog("Error occurred: " + e.getMessage()).open();
	}
    }

    /**
     * @param event
     * @param context
     * @param ex
     */
    protected void onJobExecuted(JobEvent event, JobExecutionContext context, JobExecutionException ex) {

	String text = getJobExecutedMessage(event, context, ex);

	updateText(text); //
    }

    /**
     * @param event
     * @param context
     * @param ex
     * @return
     */
    protected String getJobExecutedMessage(JobEvent event, JobExecutionContext context, JobExecutionException ex) {

	Optional<SchedulerJobStatus> status = SchedulerUtils.readStatus(context);

	String text = getEndTaskText();

	if (status.isPresent()) {

	    text += "\n\n" + formatStatus(status.get());
	}

	return text;
    }

    @Override
    public void onClick(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	session = VaadinSession.getCurrent();

	//
	//
	//

	dialog = new ConfirmationDialog();
	dialog.setTitle(getDialogTitle(event));
	dialog.setHeight(500, Unit.PIXELS);
	dialog.setWidth(650, Unit.PIXELS);
	dialog.setConfirmText("Start");
	dialog.setCancelText("Close");
	dialog.setCloseOnConfirm(false);
	dialog.setOnConfirmListener(this);

	dialog.getFooterLayout().getStyle().remove("height");
	dialog.getContentLayout().getStyle().set("padding", "0px");

	textArea = new TextArea();
	textArea.setValue(getTextAreaText(event));
	textArea.setSizeFull();
	textArea.setWidth(635, Unit.PIXELS);
	textArea.setHeight(350, Unit.PIXELS);
	textArea.getStyle().set("font-size", "14px");

	textArea.setReadOnly(true);

	forceVolatileScheduler = new Checkbox("Force usage of volatile scheduler", true);
	forceVolatileScheduler.getStyle().set("font-size", "13px");

	Component buttons = dialog.getFooterLayout().getComponentAt(0);

	dialog.getFooterLayout().removeAll();
	dialog.getFooterLayout().add(forceVolatileScheduler);
	dialog.getFooterLayout().add(buttons);

	dialog.setContent(textArea);
	dialog.open();
    }

    /**
     * @return
     */
    protected abstract SchedulerWorkerSetting getSetting();

    /**
     * @param event
     * @return
     */
    protected abstract String getDialogTitle(GridContextMenuItemClickEvent<HashMap<String, String>> event);

    /**
     * @param event
     * @return
     */
    protected abstract String getTextAreaText(GridContextMenuItemClickEvent<HashMap<String, String>> event);

    /**
     * @return
     */
    protected abstract String getStartTaskText();

    /**
     * @return
     */
    protected abstract String getEndTaskText();

    /**
     * @param status
     * @return
     */
    public static String formatStatus(SchedulerJobStatus status) {

	final StringBuilder builder = new StringBuilder();

	builder.append("- Status: " + status.getPhase().getLabel() + "\n");
	builder.append("- Start time: " + status.getStartTime().get() + "\n");
	builder.append("- End time: " + status.getEndTime().orElse(ISO8601DateTimeUtils.getISO8601DateTime()) + "\n\n");

	List<String> messagesList = status.getMessagesList(false);

	messagesList.forEach(m -> {

	    builder.append(m + "\n");
	});

	return builder.toString();
    }

    /**
     * @param text
     */
    protected void updateText(final String text) {

	session.access(() -> {

	    Optional<UI> ui = textArea.getUI();
	    try {
		if (ui.isPresent()) {

		    textArea.setValue(textArea.getValue() + "\n" + text);
		    ui.get().push();
		} // otherwise the dialog is closed
	    } catch (com.vaadin.flow.component.UIDetachedException ex) {
		// the tab is closed
	    }
	});

    }
}
