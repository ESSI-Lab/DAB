package eu.essi_lab.accessor.waf.onamet_stations;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class ONAMETStationsAugmenterSetting extends AugmenterSetting {

    private static final String S3_BUCKET_SETTINGS_ID = "s3OnametStationsBucketSetting";
    private static final String THREDDS_URL_OPTION_KEY = "onametStationsThreddsURL";
    private static final String THREDDS_DATA_SUB_FOLDER_OPTION_KEY = "onametStationsThreddsDataSubFolder";
    private static final String NC_PATH_OPTION_KEY = "ncPath";

    /**
     * 
     */
    public ONAMETStationsAugmenterSetting() {

	Option<String> extractionPathOption = StringOptionBuilder.get().//
		withKey(NC_PATH_OPTION_KEY).//
		withLabel("Local file system path to store the generated NetCDF files").//
		withDescription(
			"Leave unset to extract the NetCDF files in the folder 'onamet-stations-nc' located in the user temp directory")
		.//
		cannotBeDisabled().//
		build();

	addOption(extractionPathOption);

	Option<String> threddsURLOption = StringOptionBuilder.get().//
		withKey(THREDDS_URL_OPTION_KEY).//
		withLabel("URL of the THREDDS service which publishes the NetCDF files").//
		withValue("http://localhost/thredds/").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(threddsURLOption);

	Option<String> threddsDataFolder = StringOptionBuilder.get().//
		withKey(THREDDS_DATA_SUB_FOLDER_OPTION_KEY).//
		withLabel("Optional data sub-folder (e.g: 'grid','timeseries')").//
		withDescription("A sub-folder of the 'data' folder where the NC files are located").//
		cannotBeDisabled().//
		build();

	addOption(threddsDataFolder);

	S3StorageSetting s3Settings = new S3StorageSetting();
	s3Settings.setName("ONAMET Stations Augmenter-harvesting S3 settings");
	s3Settings.setIdentifier(S3_BUCKET_SETTINGS_ID);
	s3Settings.setEditable(false);
	s3Settings.setCanBeDisabled(false);

	addSetting(s3Settings);
    }

    /**
     * @param object
     */
    public ONAMETStationsAugmenterSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ONAMETStationsAugmenterSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public Optional<String> getNCPath() {

	return getOption(NC_PATH_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param string
     */
    public void setNCPath(String path) {

	getOption(NC_PATH_OPTION_KEY, String.class).get().setValue(path);
    }

    /**
     * @param bucketName
     */
    public void setS3BucketName(String bucketName) {

	getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().setBucketName(bucketName);
    }

    /**
     * @return
     */
    public Optional<String> getS3BucketName() {

	return getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().getBucketName();
    }

    /**
     * @param accessKey
     */
    public void setS3AccessKey(String accessKey) {

	getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().setAccessKey(accessKey);
    }

    /**
     * @return
     */
    public Optional<String> getS3AccessKey() {

	return getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().getAccessKey();
    }

    /**
     * @param secretKey
     */
    public void setS3SecretKey(String secretKey) {

	getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().setSecretKey(secretKey);
    }

    /**
     * @return
     */
    public Optional<String> getS3SecretKey() {

	return getSetting(S3_BUCKET_SETTINGS_ID, S3StorageSetting.class).get().getSecretKey();
    }

    /**
     * @param string
     */
    public void setTHREDDSUrl(String url) {

	getOption(THREDDS_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @return
     */
    public String getTHREDDSUrl() {

	return getOption(THREDDS_URL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param dataSubFolder
     */
    public void setTHREDDSDataSubFolder(String dataSubFolder) {

	getOption(THREDDS_DATA_SUB_FOLDER_OPTION_KEY, String.class).get().setValue(dataSubFolder);
    }

    /**
     * @return
     */
    public Optional<String> getTHREDDSDataSubFolder() {

	return getOption(THREDDS_DATA_SUB_FOLDER_OPTION_KEY, String.class).get().getOptionalValue();
    }

}
