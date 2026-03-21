package eu.essi_lab.gssrv.conf.import_export;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.*;

/**
 * @author Fabrizio
 */
public class ConfigViewer extends VerticalLayout {

    /**
     *
     */
    public ConfigViewer() {

	getStyle().set("padding", "0px");

	setWidthFull();
	setHeightFull();

	//
	//
	//

	HorizontalLayout layout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();

	CopyToClipboardButton copyButton = ComponentFactory.createCopyToClipboardButton();

	copyButton.getStyle().set("margin-right", "10px");

	layout.add(copyButton);

	Span infoDiv = new Span();
	infoDiv.setWidthFull();
	infoDiv.getStyle().set("padding-top", "14px");
	infoDiv.getStyle().set("font-size", "14px");

	String infoMsg = "Click the button on the left to copy the configuration to the clipboard";

	infoDiv.getElement().setProperty("innerHTML", infoMsg);

	layout.add(infoDiv);

	add(layout);

	//
	//
	//

	TextArea area = new TextArea();
	area.getStyle().set("font-size", "13px");
	area.getStyle().set("vertical-overflow", "auto");
	area.getStyle().set("margin-top", "-15px");
	area.addClassName("text-area-readonly");
	area.setHeightFull();
	area.setWidthFull();
	area.setReadOnly(true);

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    area.setMaxHeight(screenHeight - 450, Unit.PIXELS);
	});

	area.setValue(ConfigurationWrapper.getConfiguration().get().toString());

	copyButton.setSupplier(area::getValue);

	add(area);

    }
}
