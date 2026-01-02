package eu.essi_lab.cfga.gui.dialog;

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

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Label;

import eu.essi_lab.cfga.gui.components.ComponentFactory;

@SuppressWarnings("serial")
public class NotificationDialog extends ConfirmationDialog {

    /**
     * @param text
     * @return
     */
    public static NotificationDialog getInfoDialog(String text) {

	return new NotificationDialog("Info", text, 500);
    }

    /**
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getInfoDialog(String text, int width) {

	return new NotificationDialog("Info", text, width);

    }

    /**
     * @param text
     * @return
     */
    public static NotificationDialog getWarningDialog(String text) {

	return new NotificationDialog("Warning", text, 500);

    }

    /**
     * @param text
     * @return
     */
    public static NotificationDialog getWarningDialog(String text, Throwable throwable) {

	return new NotificationDialog("Warning", text, 500, throwable);

    }

    /**
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getWarningDialog(String text, int width) {

	return new NotificationDialog("Warning", text, width);

    }

    /**
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getWarningDialog(String text, int width, Throwable throwable) {

	return new NotificationDialog("Warning", text, width, throwable);

    }

    /**
     * @param text
     * @return
     */
    public static NotificationDialog getErrorDialog(String text) {

	return new NotificationDialog("Error", text, 500);
    }

    /**
     * @param text
     * @return
     */
    public static NotificationDialog getErrorDialog(String text, Throwable throwable) {

	return new NotificationDialog("Error", text, 500, throwable);
    }

    /**
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getErrorDialog(String text, int width) {

	return new NotificationDialog("Error", text, width);
    }

    /**
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getErrorDialog(String text, int width, Throwable throwable) {

	return new NotificationDialog("Error", text, width, throwable);
    }

    /**
     * @param title
     * @param text
     * @param width
     * @param throwable
     * @return
     */
    public static NotificationDialog getNotificationDialog(String title, String text, int width, Throwable throwable) {

	return new NotificationDialog(title, text, width, throwable);
    }

    /**
     * @param title
     * @param text
     * @param throwable
     * @return
     */
    public static NotificationDialog getNotificationDialog(String title, String text, Throwable throwable) {

	return new NotificationDialog(title, text, 500, throwable);
    }

    /**
     * @param title
     * @param text
     * @return
     */
    public static NotificationDialog getNotificationDialog(String title, String text) {

	return new NotificationDialog(title, text, 500);
    }

    /**
     * @param title
     * @param text
     * @param width
     * @return
     */
    public static NotificationDialog getNotificationDialog(String title, String text, int width) {

	return new NotificationDialog(title, text, width);
    }

    /**
     * @param title
     * @param text
     * @param width
     */
    private NotificationDialog(String title, String text, int width) {

	this(title, text, width, null);
    }

    /**
     * @param title
     * @param text
     * @param width
     */
    private NotificationDialog(String title, String text, int width, Throwable throwable) {

	setTitle(title);

	if (text == null || text.trim().isEmpty()) {

	    text = "No message available";
	}

	Label label = ComponentFactory.createLabel(text);
	// label.getStyle().set("color", "red");
	label.getStyle().set("font-size","14px");

	setContent(label);
	getConfirmButton().setVisible(false);
	setCancelText("Close");

	setWidth(width, Unit.PIXELS);

	getFooterLayout().getStyle().set("border-top", "1px solid lightgray");
    }
}
