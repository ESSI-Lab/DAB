/**
 *
 */
package eu.essi_lab.gssrv.conf.import_export;

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

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.lib.net.s3.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class ConfigExporter extends VerticalLayout {

    private ConfigurationSourceSetting setting;

    /**
     *
     */
    public ConfigExporter() {

	getStyle().set("padding", "0px");

	setWidthFull();
	setHeightFull();

	//
	//
	//

	Div msgDiv = new Div();
	msgDiv.setWidthFull();
	msgDiv.getStyle().set("padding-top", "5px");
	msgDiv.getStyle().set("padding-bottom", "5px");
	msgDiv.getStyle().set("font-size", "14px");

	add(msgDiv);

	Button localExportButton = new Button("Export to local file system");
	localExportButton.getStyle().set("font-size", "14px");
	localExportButton.setWidth("400px");
	localExportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	localExportButton.setEnabled(false);

	TextField localExportField = new TextField();
	localExportField.setPlaceholder("Folder path (e.g.: c:\\config)");
	localExportField.setWidthFull();
	localExportField.getStyle().set("margin-left", "10px");
	localExportField.setValueChangeMode(ValueChangeMode.EAGER);
	localExportField.addValueChangeListener(evt -> {

	    File file = new File(localExportField.getValue());

	    localExportButton.setEnabled(file.exists() && file.isDirectory());

	    if (localExportButton.isEnabled()) {

		setMessage(msgDiv, "Click the 'Export to local file system' button to proceed'", "black");

	    } else {

		setMessage(msgDiv, "Invalid folder path", "red");
	    }
	});

	localExportButton.addClickListener(event -> {

	    try {

		try {

		    InputStream stream = ConfigurationWrapper.getConfiguration().get().getSource().getStream();

		    ClonableInputStream configStream = new ClonableInputStream(stream);

		    File out = new File(localExportField.getValue(), ConfigImportExportDescriptor.DEFAULT_CONFIG_NAME);

		    FileUtils.copyInputStreamToFile(configStream.clone(), out);

		    setMessage(msgDiv, "Configuration correctly exported to: " + out.getAbsolutePath(), "green");

		} catch (Exception ex) {

		    setMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage(), "red");

		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}

	    } catch (Exception ex) {

		setMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage(), "red");

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	});

	//
	//
	//

	HorizontalLayout localExportLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	localExportLayout.setWidthFull();

	localExportLayout.add(localExportButton);
	localExportLayout.add(localExportField);

	add(localExportLayout);

	//
	//
	//


	setMessage(msgDiv, "To export the configuration to a local folder, enter the folder path"
			+ " and click the 'Export to local file system' button. "
			+ "To export the configuration to an S3 bucket, fill all the related fields and click " + " 'Export to S3 bucket' button",
		"black");

	//
	//
	//

	HorizontalLayout s3ExportLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	s3ExportLayout.setWidthFull();

	Button s3ExportButton = new Button("Export to S3 bucket");
	s3ExportButton.getStyle().set("font-size", "14px");
	s3ExportButton.setWidth("400px");
	s3ExportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	s3ExportButton.getStyle().set("margin-right", "10px");

	s3ExportButton.addClickListener(event -> {

	    try {

		try {

		    InputStream stream = ConfigurationWrapper.getConfiguration().get().getSource().getStream();

		    StorageInfo info = setting.getSelectedStorageInfo().get();

		    S3TransferWrapper wrapper = new S3TransferWrapper();
		    wrapper.setAccessKey(info.getUser());
		    wrapper.setSecretKey(info.getPassword());
		    wrapper.setEndpoint(info.getUri());

		    ConfigImportExportDescriptor.uploadS3Config(wrapper, info, stream);

		    setMessage(msgDiv, "Configuration correctly exported", "green");

		} catch (Exception ex) {

		    setMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage(), "red");

		    GSLoggerFactory.getLogger(getClass()).error(ex);

		}

	    } catch (Exception ex) {

		setMessage(msgDiv, "Error occurred. Unable to export configuration: " + ex.getMessage(), "red");

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	});

	setting = new ConfigurationSourceSetting();
	setting.removeOpenSearchSetting();

	SettingComponent settingComponent = SettingComponentFactory.createSettingComponent(//
		ConfigurationWrapper.getConfiguration().get(),//
		setting, //
		false, // forceReadOnly
		true,// forceHideHeader
		null); // tabContent

	settingComponent.getOptionTextFields("S3")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, s3ExportButton, setting)));

	s3ExportLayout.add(s3ExportButton);
	s3ExportLayout.add(settingComponent);

	add(s3ExportLayout);

	checkFields(msgDiv, s3ExportButton, setting);
    }

    /**
     * @param label
     * @param upload
     * @param remoteUploadButton
     * @param setting
     */
    private void checkFields(Div label, Button s3ExportButton, ConfigurationSourceSetting setting) {

	Optional<StorageInfo> info = setting.getSelectedStorageInfo();

	s3ExportButton.setEnabled(info.isPresent());

	if (info.isEmpty()) {

	    setMessage(label, "<span style='font-size: 14px; color:red;'>Please, " + "fill all the required fields to proceed</span>",
		    "red");

	} else {

	    setMessage(label, "<span style='font-size: 14px;color:black;'>Click the 'Export to S3 bucket' " + "button to proceed</span>",
		    "black");
	}
    }

    /**
     * @param div
     * @param message
     */
    private void setMessage(Div div, String message, String color) {

	div.getStyle().set("color", color);

	div.getElement().setProperty("innerHTML", message);
    }
}
