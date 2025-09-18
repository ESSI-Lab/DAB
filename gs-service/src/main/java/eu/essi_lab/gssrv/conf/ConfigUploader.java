/**
 * 
 */
package eu.essi_lab.gssrv.conf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.cfg.DatabaseSource;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.TabIndex;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.components.setting.group.RadioComponentsHandler;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * @author Fabrizio
 */
public class ConfigUploader extends ComponentInfo {

    /**
     * 
     */
    private static final String DEFAULT_CONFIG_NAME = "gs-configuration.json";

    /**
     * 
     */
    public ConfigUploader() {

	setComponentName("Configuration upload");

	VerticalLayout verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "15px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	ConfigurationSourceSetting setting = new ConfigurationSourceSetting();

	Div msgDiv = new Div();

	//
	//
	//

	MemoryBuffer memoryBuffer = new MemoryBuffer();

	Upload upload = new Upload(memoryBuffer);
	upload.getElement().setEnabled(false);

	upload.addFinishedListener(event -> {

	    try {

		ClonableInputStream configStream = new ClonableInputStream(memoryBuffer.getInputStream());

		switch (setting.getSelectedSource()) {
		case MARK_LOGIC -> handleMarkLogicUpload(setting, configStream, upload, msgDiv);
		case OPENSEARCH -> handleOpenSearchUpload(setting, configStream, upload, msgDiv);
		case S3 -> handleS3Upload(setting, configStream, upload, msgDiv);
		}

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

		NotificationDialog.getErrorDialog("Error occurred, unable to upload configuration: " + ex.getMessage()).open();

		upload.interruptUpload();
	    }
	});

	upload.setMaxFiles(1);
	upload.setDropAllowed(true);
	upload.setDropLabel(new Label("Upload configuration file"));
	upload.setAcceptedFileTypes("application/json", ".json");
	upload.setDropLabel(new Label("Drop file here"));

	Button localUploadButton = new Button("Upload local configuration file");
	localUploadButton.setWidth("400px");
	localUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	upload.setUploadButton(localUploadButton);

	//
	//
	//

	HorizontalLayout remoteUploadLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	remoteUploadLayout.setWidthFull();

	Button remoteUploadButton = new Button("Upload remote configuration file");
	remoteUploadButton.setWidth("400px");
	remoteUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	TextField dbSourceTextField = new TextField();
	dbSourceTextField.setValue("xdbc://user:password@hostname:8000,8004/dbName/folder/");
	dbSourceTextField.setWidthFull();
	dbSourceTextField.getStyle().set("margin-left", "10px");

	remoteUploadButton.addClickListener(event -> {

	    try {

		DatabaseSource source = DatabaseSource.of(dbSourceTextField.getValue());

		try {

		    ClonableInputStream configStream = new ClonableInputStream(source.getStream());

		    switch (setting.getSelectedSource()) {
		    case MARK_LOGIC -> handleMarkLogicUpload(setting, configStream, upload, msgDiv);
		    case OPENSEARCH -> handleOpenSearchUpload(setting, configStream, upload, msgDiv);
		    case S3 -> handleS3Upload(setting, configStream, upload, msgDiv);
		    }

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);

		    NotificationDialog.getErrorDialog("Error occurred, unable to upload configuration: " + ex.getMessage()).open();

		    upload.interruptUpload();
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	});

	remoteUploadLayout.add(remoteUploadButton);
	remoteUploadLayout.add(dbSourceTextField);

	//
	//
	//

	msgDiv.setWidthFull();
	msgDiv.getStyle().set("padding", "10px");
	msgDiv.getStyle().set("border", "1px solid lightgray");
	msgDiv.getStyle().set("margin-left", "3px");

	HorizontalLayout uploadLayout = new HorizontalLayout();
	uploadLayout.setWidthFull();
	uploadLayout.add(upload);
	uploadLayout.add(msgDiv);

	//
	//
	//

	SettingComponent settingComponent = SettingComponentFactory.createSettingComponent(//
		ConfigurationWrapper.getConfiguration().get(), setting, false);

	settingComponent.getOptionTextFields("MarkLogic")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting)));

	settingComponent.getOptionTextFields("OpenSearch")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting)));

	settingComponent.getOptionTextFields("S3")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting)));

	RadioComponentsHandler radioComponentsHandler = settingComponent.getRadioHandler().get();
	RadioButtonGroup<String> radioGroup = radioComponentsHandler.getGroupComponent();
	radioGroup.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting));

	//
	//
	//

	verticalLayout.add(uploadLayout);
	verticalLayout.add(remoteUploadLayout);

	verticalLayout.add(settingComponent);

	checkFields(msgDiv, upload, remoteUploadButton, setting);

	//
	//
	//

	TabInfo tabInfo = TabInfoBuilder.get().//
		withIndex(TabIndex.CONFIG_UPLOADER.getIndex()).//
		withShowDirective(getComponentName()).//
		withComponent(verticalLayout).//
		build();

	setTabInfo(tabInfo);
    }

    /**
     * @param setting
     * @param configStream
     * @param upload
     * @param messageLabel
     * @return
     */
    private void handleS3Upload(ConfigurationSourceSetting setting, ClonableInputStream configStream, Upload upload, Div messageLabel) {

	StorageInfo info = setting.getSelectedStorageInfo().get();

	S3TransferWrapper wrapper = new S3TransferWrapper();
	wrapper.setAccessKey(info.getUser());
	wrapper.setSecretKey(info.getPassword());
	wrapper.setEndpoint(info.getUri());

	HeadObjectResponse objectMetadata = wrapper.getObjectMetadata(info.getName(), DEFAULT_CONFIG_NAME);

	if (objectMetadata != null) {

	    ConfirmationDialog confirmationDialog = new ConfirmationDialog("A configuration is already stored in the given bucket.\n\n"
		    + " Click 'Confirm' to overwrite the existing configuration, or click 'Cancel' to abort the upload", evt -> {

			try {

			    uploadS3Config(wrapper, info, configStream.clone(), true);

			    NotificationDialog.getInfoDialog("Configuration correctly overwritten").open();

			    upload.clearFileList();

			    setMessage(messageLabel,
				    "<span style='font-size: 17px;'>Use the following Java option: </span><span style='font-size: 17px; font-weight: bold'>-Dconfiguration.url="
					    + buildS3StartupURL(setting.getSelectedStorageInfo().get()) + "</span>");

			} catch (Exception e) {

			    GSLoggerFactory.getLogger(getClass()).error(e);

			    NotificationDialog.getErrorDialog("Error occurred, unable to overwrite configuration: " + e.getMessage(), 700)
				    .open();

			    upload.interruptUpload();
			}
		    });

	    confirmationDialog.setOnCancelListener(evt -> upload.clearFileList());
	    confirmationDialog.setWidth(670, Unit.PIXELS);

	    confirmationDialog.setTitle("Configuration overwriting");
	    confirmationDialog.open();

	} else {

	    try {

		uploadS3Config(wrapper, setting.getSelectedStorageInfo().get(), configStream.clone(), false);

		NotificationDialog.getInfoDialog("Configuration correctly uploaded").open();

		upload.clearFileList();

		setMessage(messageLabel,
			"<span style='font-size: 17px;'>Use the following Java option: </span><span style='font-size: 17px; font-weight: bold'>-Dconfiguration.url="
				+ buildS3StartupURL(setting.getSelectedStorageInfo().get()) + "</span>");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		NotificationDialog.getErrorDialog("Error occurred, unable to upload configuration:\n " + e.getMessage(), 700).open();

		upload.interruptUpload();
		upload.clearFileList();
	    }
	}
    }

    /**
     * s3://awsUser:awsPassword@https:s3.amazonaws.com/bucket/config.json
     * 
     * @param storageInfo
     * @return
     */
    private String buildS3StartupURL(StorageInfo info) {

	return "s3://" + //
		info.getUser() + ":" //
		+ info.getPassword() + "@" //
		+ info.getUri().replace("://", ":").replace("/", "") + "/" //
		+ info.getName() + "/"//
		+ DEFAULT_CONFIG_NAME;
    }

    /**
     * @param wrapper
     * @param info
     * @param configStream
     * @param replace
     * @throws IOException
     */
    private void uploadS3Config(S3TransferWrapper wrapper, StorageInfo info, InputStream configStream, boolean replace) throws IOException {

	File tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".json").toFile();

	FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

	IOUtils.copy(configStream, fileOutputStream);

	wrapper.uploadFile(tempFile.getAbsolutePath(), info.getName(), DEFAULT_CONFIG_NAME);

	tempFile.delete();
    }

    /**
     * @param setting
     * @param configStream
     * @param upload
     * @param messageDiv
     * @return
     */
    private void handleMarkLogicUpload(ConfigurationSourceSetting setting, ClonableInputStream configStream, Upload upload,
	    Div messageDiv) {
    }

    /**
     * @param setting
     * @param configStream
     * @param upload
     * @param messageDiv
     * @throws Exception
     */
    private void handleOpenSearchUpload(//
	    ConfigurationSourceSetting setting, //
	    ClonableInputStream configStream, //
	    Upload upload, //
	    Div messageDiv) throws Exception {

	if (openSearchConfigExists(setting.getSelectedStorageInfo().get(), configStream.clone())) {

	    ConfirmationDialog confirmationDialog = new ConfirmationDialog(
		    "A configuration with the given name is already stored in the database.\n\n"
			    + " Click 'Confirm' to overwrite the existing configuration, or click 'Cancel' to abort the upload",
		    evt -> {

			try {

			    uploadOpenSearchConfig(setting.getSelectedStorageInfo().get(), configStream.clone(), true);

			    NotificationDialog.getInfoDialog("Configuration correctly overwritten").open();

			    upload.clearFileList();

			    setMessage(messageDiv,
				    "<span style='font-size: 17px;'>Use the following Java option: </span><span style='font-size: 17px; font-weight: bold'>-Dconfiguration.url="
					    + buildOpenSearchStartupURL(setting.getSelectedStorageInfo().get()) + "</span>");

			} catch (Exception e) {

			    GSLoggerFactory.getLogger(getClass()).error(e);

			    NotificationDialog.getErrorDialog("Error occurred, unable to overwrite configuration: " + e.getMessage())
				    .open();

			    upload.interruptUpload();
			}
		    });

	    confirmationDialog.setOnCancelListener(evt -> upload.clearFileList());
	    confirmationDialog.setWidth(670, Unit.PIXELS);

	    confirmationDialog.setTitle("Configuration overwriting");
	    confirmationDialog.open();

	} else {

	    try {

		uploadOpenSearchConfig(setting.getSelectedStorageInfo().get(), configStream.clone(), false);

		NotificationDialog.getInfoDialog("Configuration correctly uploaded").open();

		upload.clearFileList();

		setMessage(messageDiv,
			"<span style='font-size: 17px;'>Use the following Java option: </span><span style='font-size: 17px; font-weight: bold'>-Dconfiguration.url="
				+ buildOpenSearchStartupURL(setting.getSelectedStorageInfo().get()) + "</span>");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		NotificationDialog.getErrorDialog("Error occurred, unable to upload configuration: " + e.getMessage()).open();

		upload.interruptUpload();
		upload.clearFileList();
	    }
	}
    }

    /**
     * osl://awsaccesskey:awssecretkey@http:localhost:9200/test/testConfig1
     * osl://awsaccesskey:awssecretkey@https:localhost:9200/test/testConfig2
     * osm://awsaccesskey:awssecretkey@https:awshost/prod/testConfig3
     * 
     * @param info
     * @return
     */
    private String buildOpenSearchStartupURL(StorageInfo info) {

	return info.getType().get() + "://" + //
		info.getUser() + ":" + //
		info.getPassword() + "@" + //
		info.getUri().replace("://", ":") + "/" + //
		info.getIdentifier() + "/" + //
		info.getName();//
    }

    /**
     * @param info
     * @param configStream
     * @throws Exception
     */
    private boolean openSearchConfigExists(StorageInfo info, InputStream configStream) throws Exception {

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(info);

	OpenSearchFolder folder = new OpenSearchFolder(database, Database.CONFIGURATION_FOLDER);

	String configName = info.getName() + ".json";

	return folder.exists(configName);
    }

    /**
     * @param info
     * @param configStream
     * @param replace
     * @return
     * @throws Exception
     */
    private boolean uploadOpenSearchConfig(StorageInfo info, InputStream configStream, boolean replace) throws Exception {

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(info);

	OpenSearchFolder folder = new OpenSearchFolder(database, Database.CONFIGURATION_FOLDER);

	String configName = info.getName() + ".json";

	if (replace) {

	    return folder.replace(//
		    configName, //
		    FolderEntry.of(configStream), //
		    EntryType.CONFIGURATION);
	}

	return folder.store(//
		configName, //
		FolderEntry.of(configStream), //
		EntryType.CONFIGURATION);
    }

    /**
     * @param label
     * @param message
     */
    private void setMessage(Div label, String message) {

	label.getElement().setProperty("innerHTML", message);
    }

    /**
     * @param label
     * @param upload
     * @param remoteUploadButton
     * @param setting
     */
    private void checkFields(Div label, Upload upload, Button remoteUploadButton, ConfigurationSourceSetting setting) {

	Optional<StorageInfo> info = setting.getSelectedStorageInfo();

	upload.getElement().setEnabled(info.isPresent());
	remoteUploadButton.setEnabled(info.isPresent());

	if (info.isEmpty()) {

	    setMessage(label, "<span style='font-size: 17px; color:red;'>Please, fill all the required fields to proceed</span>");

	} else {

	    setMessage(label,
		    "<span style='font-size: 17px;color:black;'>Click the button on the left to upload the selected configuration file</span>");
	}
    }
}
