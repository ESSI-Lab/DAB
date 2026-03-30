package eu.essi_lab.gssrv.conf.import_export;

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
