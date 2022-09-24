package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.StorageUri;

/**
 * @author Fabrizio
 */
public class S3StorageSetting extends Setting implements EditableSetting {

    private static String STORAGE_URI_OPTION_KEY = "s3StorageUri";
    private static String STORAGE_USER_OPTION_KEY = "s3StorageUser";
    private static String STORAGE_PWD_OPTION_KEY = "s3StoragePassword";
    private static String BUCKET_NAME_OPTION_KEY = "s3BucketName";
    
    public static void main(String[] args) {
	
	System.out.println(new S3StorageSetting());
    }

    /**
     * 
     */
    public S3StorageSetting() {

	setName("Amazon S3 storage settings");
	setDescription("Storage based on Amazon S3");
	enableCompactMode(false);

	Option<String> endpointOption = StringOptionBuilder.get().//
		withLabel("S3 endpoint").//
		withDescription(
			"Overrides the default endpoint to send requests to the specified AWS region")
		.//
		withKey(STORAGE_URI_OPTION_KEY).//
		required().//
		withValue("https://s3.amazonaws.com/").//
		cannotBeDisabled().//
		build();

	addOption(endpointOption);

	Option<String> accessKeyOption = StringOptionBuilder.get().//
		withLabel("Access key").//
		withKey(STORAGE_USER_OPTION_KEY).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(accessKeyOption);

	Option<String> secretKeyOption = StringOptionBuilder.get().//
		withLabel("Secret key").//
		withKey(STORAGE_PWD_OPTION_KEY).//
		cannotBeDisabled().//
		build();

	addOption(secretKeyOption);

	Option<String> bucketNameOption = StringOptionBuilder.get().//
		withLabel("Bucket name").//
		withKey(BUCKET_NAME_OPTION_KEY).//
		cannotBeDisabled().//
		build();

	addOption(bucketNameOption);
    }

    /**
     * @param object
     */
    public S3StorageSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public S3StorageSetting(String object) {

	super(object);
    }

    /**
     * @return 
     */
    public Optional<String> getEndpoint() {

	return getOption(STORAGE_URI_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param storageUri
     */
    public void setEndpoint(String storageUri) {

	getOption(STORAGE_URI_OPTION_KEY, String.class).get().setValue(storageUri);
    }

    /**
     * @return 
     */
    public Optional<String> getAccessKey() {

	return getOption(STORAGE_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param accessKey
     */
    public void setAccessKey(String accessKey) {

	getOption(STORAGE_USER_OPTION_KEY, String.class).get().setValue(accessKey);
    }

    /**
     * @return
     */
    public Optional<String> getSecretKey() {

	return getOption(STORAGE_PWD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param secretKey
     */
    public void setSecretKey(String secretKey) {

	getOption(STORAGE_PWD_OPTION_KEY, String.class).get().setValue(secretKey);
    }

    /**
     * @return
     */
    public Optional<String> getBucketName() {

	return getOption(BUCKET_NAME_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param bucketName
     */
    public void setBucketName(String bucketName) {

	getOption(BUCKET_NAME_OPTION_KEY, String.class).get().setValue(bucketName);
    }

    /**
     * @param storageUri
     */
    public void setStorageUri(StorageUri uri) {

	setEndpoint(uri.getUri());
	setBucketName(uri.getStorageName());
	setSecretKey(uri.getPassword());
	setAccessKey(uri.getUser());
    }

    /***
     * @return
     */
    public Optional<StorageUri> asStorageUri() {

	if (getEndpoint().isPresent() && getBucketName().isPresent() && getAccessKey().isPresent()
		&& getSecretKey().isPresent()) {

	    StorageUri storageUri = new StorageUri();

	    storageUri.setUri(getEndpoint().get());
	    storageUri.setStorageName(getBucketName().get());
	    storageUri.setUser(getAccessKey().get());
	    storageUri.setPassword(getSecretKey().get());

	    return Optional.of(storageUri);
	}

	return Optional.empty();
    }
}
