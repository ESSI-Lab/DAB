package eu.essi_lab.cfga.gs.setting;

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

import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.driver.LocalFolderSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DownloadSetting extends Setting implements EditableSetting {

    private static final String LOCAL_DOWNLOAD_SETTING_ID = "localDownloadSetting";
    private static final String S3_DOWNLOAD_SETTING_ID = "s3DownloadSetting";

    /**
     * @author Fabrizio
     */
    public enum DownloadStorage implements LabeledEnum {

	/**
	 *
	 */
	LOCAL_DOWNLOAD_STORAGE("Local storage"),
	/**
	 *
	 */
	S3_DOWNLOAD_STORAGE("Amazon S3 storage");

	private final String label;

	/**
	 * @param label
	 */
	DownloadStorage(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}
    }

    public DownloadSetting() {

	super();

	setName("Download settings");
	setDescription("Downloaded items can be stored in the local file system (default) or with Amazon S3 storage");
	setSelectionMode(SelectionMode.SINGLE);
	setCanBeDisabled(false);

	setCanBeCleaned(false);

	//
	//
	//

	LocalFolderSetting localStorageSetting = new LocalFolderSetting();
	localStorageSetting.setIdentifier(LOCAL_DOWNLOAD_SETTING_ID);
	localStorageSetting.setName("Local");
	localStorageSetting.setDescription("Stores downloaded items in the local file system");
	localStorageSetting.setSelected(true);
	localStorageSetting.setCanBeDisabled(false);
	localStorageSetting.setEditable(false);

	addSetting(localStorageSetting);

	//
	//
	//

	S3StorageSetting s3StorageSetting = new S3StorageSetting();
	s3StorageSetting.setIdentifier(S3_DOWNLOAD_SETTING_ID);
	s3StorageSetting.setDescription("Stores downloaded items to an Amazon S3 storage");
	s3StorageSetting.setCanBeDisabled(false);
	s3StorageSetting.setEditable(false);

	addSetting(s3StorageSetting);
    }

    /**
     * @author Fabrizio
     */
    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	/**
	 *
	 */
	public DescriptorProvider() {

	    descriptor = TabContentDescriptorBuilder.get(DownloadSetting.class).//
		    withLabel("Download").//
		    build();
	}

	/**
	 * @return
	 */
	public TabContentDescriptor get() {

	    return descriptor;
	}
    }

    /**
     * @param object
     */
    public DownloadSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DownloadSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public DownloadStorage getDownloadStorage() {

	if (getSetting(LOCAL_DOWNLOAD_SETTING_ID).isPresent() && getSetting(LOCAL_DOWNLOAD_SETTING_ID).get().isSelected()) {

	    return DownloadStorage.LOCAL_DOWNLOAD_STORAGE;
	}

	return DownloadStorage.S3_DOWNLOAD_STORAGE;
    }

    /**
     * @param downloadStorage
     */
    public void setDownloadStorage(DownloadStorage downloadStorage) {

	switch (downloadStorage) {
	case LOCAL_DOWNLOAD_STORAGE -> {

	    if (getSetting(LOCAL_DOWNLOAD_SETTING_ID).isPresent()) {

		getSetting(LOCAL_DOWNLOAD_SETTING_ID).get().setSelected(true);
	    }

	    if (getSetting(S3_DOWNLOAD_SETTING_ID).isPresent()) {

		getSetting(S3_DOWNLOAD_SETTING_ID).get().setSelected(false);
	    }
	}

	case S3_DOWNLOAD_STORAGE -> {

	    if (getSetting(LOCAL_DOWNLOAD_SETTING_ID).isPresent()) {

		getSetting(LOCAL_DOWNLOAD_SETTING_ID).get().setSelected(false);
	    }

	    if (getSetting(S3_DOWNLOAD_SETTING_ID).isPresent()) {

		getSetting(S3_DOWNLOAD_SETTING_ID).get().setSelected(true);
	    }
	}
	}
    }

    /**
     * @return
     */
    public S3StorageSetting getS3StorageSetting() {

	return getSetting(S3_DOWNLOAD_SETTING_ID, S3StorageSetting.class).get();
    }

    /**
     * @return
     */
    public StorageInfo getStorageUri() {

	if (getSetting(S3_DOWNLOAD_SETTING_ID).get().isSelected()) {

	    return getSetting(S3_DOWNLOAD_SETTING_ID, S3StorageSetting.class).get().asStorageUri().get();
	}

	String folderPath = getSetting(LOCAL_DOWNLOAD_SETTING_ID, LocalFolderSetting.class).get().getFolderPath();

	folderPath = "file://" + folderPath;

	StorageInfo storageUri = new StorageInfo(folderPath);
	storageUri.setName("localFS");

	return storageUri;
    }
}
