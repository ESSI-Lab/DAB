package eu.essi_lab.cfga.gui.components.grid.renderer;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dialog.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.textfield.*;
import eu.essi_lab.cfga.gui.dialog.*;

import java.util.*;

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

	TextArea textArea = new TextArea();
	textArea.getStyle().set("font-size", "14px");
	textArea.getStyle().set("padding", "0px");
	textArea.setWidth(areaWidth + "px");
	textArea.setHeight("600px");
	textArea.setReadOnly(true);
	textArea.setValue(text);
	textArea.addClassName("no-wrap");
	textArea.addClassName("text-area-readonly");

	NotificationDialog dialog = NotificationDialog.getNotificationDialog(windowTitle, "");
	dialog.setWidth(areaWidth + 30 + "px");
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
