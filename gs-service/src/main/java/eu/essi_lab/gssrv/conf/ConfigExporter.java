/**
 *
 */
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.TabDescriptorBuilder;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * @author Fabrizio
 */
public class ConfigExporter {

    /**
     *
     */
    private static final String DEFAULT_CONFIG_NAME = "gs-configuration.json";
    private final VerticalLayout mainLayout;

    /**
     *
     */
    public ConfigExporter() {

	mainLayout = new VerticalLayout();
	mainLayout.getStyle().set("margin-top", "5px");
	mainLayout.setWidthFull();
	mainLayout.setHeightFull();

	//
	//
	//

	Div msgDiv = new Div();

	Button exportButton = new Button("Export configuration");
	exportButton.setWidth("400px");
	exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	exportButton.setEnabled(false);

	TextField exportFolderField = new TextField();
	exportFolderField.setPlaceholder("Folder path (e.g.: c:\\config)");
	exportFolderField.setWidthFull();
	exportFolderField.getStyle().set("margin-left", "10px");
	exportFolderField.setValueChangeMode(ValueChangeMode.EAGER);
	exportFolderField.addValueChangeListener(evt -> {

	    File file = new File(exportFolderField.getValue());

	    exportButton.setEnabled(file.exists() && file.isDirectory());

	    if (exportButton.isEnabled()) {

		setInfoMessage(msgDiv, "Click the 'Export configuration button'");

	    } else {

		setErrorMessage(msgDiv, "Invalid folder path");
	    }
	});

	exportButton.addClickListener(event -> {

	    try {

		try {

		    InputStream stream = ConfigurationWrapper.getConfiguration().get().getSource().getStream();

		    ClonableInputStream configStream = new ClonableInputStream(stream);

		    File out = new File(exportFolderField.getValue(), DEFAULT_CONFIG_NAME);

		    FileUtils.copyInputStreamToFile(configStream.clone(), out);

		    setInfoMessage(msgDiv, "Configuration correctly exported: " + out.getAbsolutePath());

		} catch (Exception ex) {

		    setErrorMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage());

		    GSLoggerFactory.getLogger(getClass()).error(ex);

		    NotificationDialog.getErrorDialog("Error occurred, unable to export configuration: " + ex.getMessage()).open();
		}

	    } catch (Exception ex) {

		setErrorMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage());

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }

	});

	HorizontalLayout exportLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	exportLayout.setWidthFull();

	exportLayout.add(exportButton);
	exportLayout.add(exportFolderField);

	//
	//
	//

	msgDiv.setWidthFull();
	msgDiv.getStyle().set("padding-top", "10px");
	msgDiv.getStyle().set("font-size", "17px");

	setInfoMessage(msgDiv, "Enter the folder path where to export the configuration and click the 'Export configuration button'");

	//
	//
	//

	mainLayout.add(msgDiv);
	mainLayout.add(exportLayout);
    }

    /**
     * @return
     */
    public VerticalLayout getMainLayout() {

	return mainLayout;
    }

    /**
     * @param div
     * @param message
     */
    private void setInfoMessage(Div div, String message) {

	div.getStyle().set("color", "black");

	div.getElement().setProperty("innerHTML", message);
    }

    /**
     * @param div
     * @param message
     */
    private void setErrorMessage(Div div, String message) {

	div.getStyle().set("color", "red");

	div.getElement().setProperty("innerHTML", message);
    }

}
