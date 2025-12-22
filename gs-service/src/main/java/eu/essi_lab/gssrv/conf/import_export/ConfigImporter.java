/**
 *
 */
package eu.essi_lab.gssrv.conf.import_export;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.radiobutton.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.upload.*;
import com.vaadin.flow.component.upload.receivers.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.DatabaseFolder.*;
import eu.essi_lab.api.database.cfg.*;
import eu.essi_lab.api.database.opensearch.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.setting.group.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.lib.net.s3.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class ConfigImporter extends VerticalLayout {

    /**
     *
     */
    public ConfigImporter() {

	getStyle().set("padding", "0px");

	setWidthFull();
	setHeightFull();

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
	upload.setDropLabel(new Label("Import configuration file"));
	upload.setAcceptedFileTypes("application/json", ".json");
	upload.setDropLabel(new Label("Drop file here"));

	Button localUploadButton = new Button("Import from local configuration file");
	localUploadButton.getStyle().set("font-size", "14px");
	localUploadButton.setWidth("330px");
	localUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	upload.setUploadButton(localUploadButton);

	//
	//
	//

	HorizontalLayout remoteUploadLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	remoteUploadLayout.setWidthFull();

	Button remoteUploadButton = new Button("Import from remote configuration file");
	remoteUploadButton.getStyle().set("font-size", "14px");
	remoteUploadButton.setWidth("400px");
	remoteUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	TextField dbSourceTextField = new TextField();
	dbSourceTextField.getStyle().set("font-size", "14px");
	dbSourceTextField.setValue("osl://user:password@http:localhost:9200/test/testConfig");
	dbSourceTextField.setWidthFull();
	dbSourceTextField.getStyle().set("margin-left", "10px");

	remoteUploadButton.addClickListener(event -> {

	    try {

		DatabaseSource source = DatabaseSource.of(dbSourceTextField.getValue());

		try {

		    ClonableInputStream configStream = new ClonableInputStream(source.getStream());

		    switch (setting.getSelectedSource()) {
		    case OPENSEARCH -> handleOpenSearchUpload(setting, configStream, upload, msgDiv);
		    case S3 -> handleS3Upload(setting, configStream, upload, msgDiv);
		    }

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);

		    NotificationDialog.getErrorDialog("Error occurred, unable to import configuration: " + ex.getMessage()).open();

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
	msgDiv.getStyle().set("padding-top", "5px");
	msgDiv.getStyle().set("border", "1px solid lightgray");
	msgDiv.getStyle().set("margin-left", "3px");

	HorizontalLayout uploadLayout = new HorizontalLayout();
	uploadLayout.getStyle().set("margin-top", "-15px");
	uploadLayout.setWidthFull();
	uploadLayout.add(upload);
	uploadLayout.add(msgDiv);

	//
	//
	//

	SettingComponent settingComponent = SettingComponentFactory.createSettingComponent(//
		ConfigurationWrapper.getConfiguration().get(),//
		setting, //
		false, // forceReadOnly
		false,// forceHideHeader
		null); // tabContent

	settingComponent.getOptionTextFields("OpenSearch")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting)));

	settingComponent.getOptionTextFields("S3")
		.forEach(tf -> tf.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting)));

	RadioComponentsHandler radioComponentsHandler = settingComponent.getRadioHandler().get();
	RadioButtonGroup<String> radioGroup = radioComponentsHandler.getGroupComponent();
	radioGroup.addValueChangeListener(event -> checkFields(msgDiv, upload, remoteUploadButton, setting));

	settingComponent.getStyle().set("margin-top", "-10px");

	//
	//
	//

	Div infoDiv = new Div();
	infoDiv.setWidthFull();
	infoDiv.getStyle().set("padding-top", "5px");
	infoDiv.getStyle().set("font-size", "14px");

	String info = "Configure the storage ('OpenSearch' or 'S3') where to import the configuration, then click the 'Import from local "
		+ "configuration file' or 'Import from remote configuration file' button to start the "
		+ "import into the configured storage. "
		+ "When the import is done, you can read in the panel above, the "
		+ "Java option required to start the DAB with the imported configuration";

	infoDiv.getElement().setProperty("innerHTML", info);

	add(infoDiv);
	add(uploadLayout);
	add(remoteUploadLayout);
	add(settingComponent);

	checkFields(msgDiv, upload, remoteUploadButton, setting);
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

	HeadObjectResponse objectMetadata = wrapper.getObjectMetadata(info.getName(), ConfigImportExportDescriptor.DEFAULT_CONFIG_NAME);

	if (objectMetadata != null) {

	    ConfirmationDialog confirmationDialog = new ConfirmationDialog("A configuration is already stored in the given bucket.\n\n"
		    + " Click 'Confirm' to overwrite the existing configuration, or click 'Cancel' to abort the upload", evt -> {

		try {

		    ConfigImportExportDescriptor.uploadS3Config(wrapper, info, configStream.clone());

		    NotificationDialog.getInfoDialog("Configuration correctly overwritten").open();

		    upload.clearFileList();

		    setMessage(messageLabel,
			    "<span style='font-size: 14px;'>Use the following Java option: </span><br><div style='font-size: 14px; font-weight: bold'>-Dconfiguration.url="
				    + buildS3StartupURL(setting.getSelectedStorageInfo().get()) + "</div>");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);

		    NotificationDialog.getErrorDialog("Error occurred, unable to overwrite configuration: " + e.getMessage(), 700).open();

		    upload.interruptUpload();
		}
	    });

	    confirmationDialog.setOnCancelListener(evt -> upload.clearFileList());
	    confirmationDialog.setWidth(670, Unit.PIXELS);

	    confirmationDialog.setTitle("Configuration overwriting");
	    confirmationDialog.getContentLayout().getStyle().set("font-size", "14px");
	    confirmationDialog.open();

	} else {

	    try {

		ConfigImportExportDescriptor.uploadS3Config(wrapper, setting.getSelectedStorageInfo().get(), configStream.clone());

		NotificationDialog.getInfoDialog("Configuration correctly imported").open();

		upload.clearFileList();

		setMessage(messageLabel,
			"<span style='font-size: 14px;'>Use the following Java option: </span><br><div style='font-size: 14px; font-weight: bold'>-Dconfiguration.url="
				+ buildS3StartupURL(setting.getSelectedStorageInfo().get()) + "</div>");

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
		+ ConfigImportExportDescriptor.DEFAULT_CONFIG_NAME;
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
			    + " Click 'Confirm' to overwrite the existing configuration, or click 'Cancel' to abort the upload", evt -> {

		try {

		    uploadOpenSearchConfig(setting.getSelectedStorageInfo().get(), configStream.clone(), true);

		    NotificationDialog.getInfoDialog("Configuration correctly overwritten").open();

		    upload.clearFileList();

		    setMessage(messageDiv,
			    "<span style='font-size: 16px;'>Use the following Java option: </span><span style='font-size: 16px; font-weight: bold'>-Dconfiguration.url="
				    + buildOpenSearchStartupURL(setting.getSelectedStorageInfo().get()) + "</span>");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);

		    NotificationDialog.getErrorDialog("Error occurred, unable to overwrite configuration: " + e.getMessage()).open();

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

		NotificationDialog.getInfoDialog("Configuration correctly imported").open();

		upload.clearFileList();

		setMessage(messageDiv,
			"<span style='font-size: 16px;'>Use the following Java option: </span><span style='font-size: 14px; font-weight: bold'>-Dconfiguration.url="
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
     * osl://awsaccesskey:awssecretkey@https:localhost:9200/test/testConfig2 osm://awsaccesskey:awssecretkey@https:awshost/prod/testConfig3
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
    private void uploadOpenSearchConfig(StorageInfo info, InputStream configStream, boolean replace) throws Exception {

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(info);

	OpenSearchFolder folder = new OpenSearchFolder(database, Database.CONFIGURATION_FOLDER);

	String configName = info.getName() + ".json";

	if (replace) {

	    folder.replace(//
		    configName, //
		    FolderEntry.of(configStream), //
		    EntryType.CONFIGURATION);
	} else {

	    folder.store(//
		    configName, //
		    FolderEntry.of(configStream), //
		    EntryType.CONFIGURATION);
	}
    }

    /**
     * @param div
     * @param message
     */
    private void setMessage(Div div, String message) {

	div.getElement().setProperty("innerHTML", message);
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

	    setMessage(label, "<span style='font-size: 14px; color:red;'>Please, fill all the required fields to proceed</span>");

	} else {

	    setMessage(label,
		    "<span style='font-size: 14px;color:black;'>Click one of the button on the left to upload the selected configuration file</span>");
	}
    }
}
