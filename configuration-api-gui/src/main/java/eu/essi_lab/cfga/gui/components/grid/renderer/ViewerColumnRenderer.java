package eu.essi_lab.cfga.gui.components.grid.renderer;

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

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dialog.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.textfield.*;
import eu.essi_lab.cfga.gui.dialog.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class ViewerColumnRenderer extends GridColumnRenderer<Button> {

    private String tooltip;
    private String columnName;
    private String windowTitle;

    /**
     * @param columnName
     * @param windowTitle
     * @param tooltip
     * @return
     */
    public static ViewerColumnRenderer create(String columnName, String windowTitle, String tooltip) {

	ViewerColumnRenderer renderer = new ViewerColumnRenderer();
	renderer.columnName = columnName;
	renderer.windowTitle = windowTitle;
	renderer.tooltip = tooltip;

	return renderer;
    }

    /**
     * @param columnName
     * @param windowTitle
     * @return
     */
    public static ViewerColumnRenderer create(String columnName, String windowTitle) {

	return create(columnName, windowTitle, null);
    }

    /**
     *
     */
    public ViewerColumnRenderer() {

    }

    /**
     * @param item
     * @return
     */
    @Override
    public Button createComponent(HashMap<String, String> item) {

	Dialog dialog = createDialog(windowTitle, item.get(columnName));

	Button button = createButton(tooltip);

	button.addClickListener(evt -> dialog.open());

	return button;
    }

    /**
     * @param windowTitle
     * @param text
     * @return
     */
    protected Dialog createDialog(String windowTitle, String text) {

	int areaWidth = 700;
	int areaHeight = 600;
	
	TextArea textArea = new TextArea();
	textArea.getStyle().set("font-size", "14px");
	textArea.getStyle().set("padding", "0px");
	textArea.setWidthFull();
	textArea.setHeight(areaHeight+"px");
	textArea.setReadOnly(true);
	textArea.setValue(text);
	textArea.addClassName("no-wrap");
	textArea.addClassName("text-area-readonly");

	NotificationDialog dialog = NotificationDialog.getNotificationDialog(windowTitle, "");
	dialog.setResizable(true);
	dialog.setWidth(areaWidth + 30 + "px");
	dialog.setMinWidth(areaWidth + 30 + "px");

	dialog.setMinHeight(areaHeight + 160 + "px");
	dialog.setMaxHeight(areaHeight + 160 + "px");

	dialog.getContentLayout().getStyle().set("padding-left", "12px");
	dialog.setContent(textArea);

	return dialog;
    }

    /**
     * @return
     */
    protected Button createButton(String tooltip) {

	Button button = new Button(VaadinIcon.OPEN_BOOK.create());
	button.addThemeVariants(ButtonVariant.LUMO_ICON);
	button.getStyle().set("background-color", "transparent");
	button.setHeight("20px");
	button.setId("viewerButton");

	if (tooltip != null) {

	    button.setTooltipText(tooltip);
	}

	return button;
    }
}
