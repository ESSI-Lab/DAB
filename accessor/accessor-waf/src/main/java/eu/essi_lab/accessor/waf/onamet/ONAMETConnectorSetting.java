package eu.essi_lab.accessor.waf.onamet;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class ONAMETConnectorSetting extends HarvestedConnectorSetting {

    private static final String MAX_ENTRIES_OPTION_KEY = "maxEntries";
    private static final String EXTRACTION_TIMEOUT_OPTION_KEY = "extractionTimeout";
    private static final String START_FOLDER_URL_OPTION_KEY = "startFolderURLOption";
    private static final String THREDDS_URL_OPTION_KEY = "threddsURL";
    private static final String THREDDS_DATA_SUB_FOLDER_OPTION_KEY = "onametConnectorThreddsDataSubFolder";
    private static final String EXTRACTION_PATH_OPTION_KEY = "extractionPath";
    private static final String S3_BUCKET_SETTINGS_ID = "s3BucketSetting";

    /**
     * 
     */
    public ONAMETConnectorSetting() {

	Option<Integer> maxEntriesOption = IntegerOptionBuilder.get().//
		withKey(MAX_ENTRIES_OPTION_KEY).//
		withLabel("Maximum number of NetCDF files to extract from the compressed files, or to process from a 'd0x' folder").//
		withDescription("Leave unset to extract/process all the NetCDF files").//
		withMinValue(1).//
		cannotBeDisabled().//
		build();

	addOption(maxEntriesOption);

	Option<Integer> extractionTimeoutOption = IntegerOptionBuilder.get().//
		withKey(EXTRACTION_TIMEOUT_OPTION_KEY).//
		withLabel("Extraction timeout expressed in minutes (default 6 hours)").//
		withDescription("Leave unset for no timeout").//
		withMinValue(1).//
		withValue(360).//
		cannotBeDisabled().//
		build();

	addOption(extractionTimeoutOption);

	Option<String> extractionPathOption = StringOptionBuilder.get().//
		withKey(EXTRACTION_PATH_OPTION_KEY).//
		withLabel("Local file system path to store the generated NetCDF files").//
		withDescription("Leave unset to extract the NetCDF files in the folder 'onamet-nc' located in the user temp directory").//
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

	Option<String> startFolderURLOption = StringOptionBuilder.get().//
		withKey(START_FOLDER_URL_OPTION_KEY).//
		withLabel("Complete URL of the 'd0x' folder by which starts the processing").//
		withDescription("If set, it will overwrite the computed start folder").//
		cannotBeDisabled().//
		build();

	addOption(startFolderURLOption);

	//
	//
	//

	S3StorageSetting s3Settings = new S3StorageSetting();
	s3Settings.setName("ONAMET Connector S3 settings");
	s3Settings.setIdentifier(S3_BUCKET_SETTINGS_ID);
	s3Settings.setEditable(false);
	s3Settings.setCanBeDisabled(false);

	addSetting(s3Settings);
    }

    /**
     * @param object
     */
    public ONAMETConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param string
     */
    public ONAMETConnectorSetting(String string) {

	super(string);
    }

    /**
     * @param max
     */
    public void setExtractionTimeout(int max) {

	getOption(EXTRACTION_TIMEOUT_OPTION_KEY, Integer.class).get().setValue(max);
    }

    /**
     * @return
     */
    public Optional<Integer> getExtractionTimeout() {

	return getOption(EXTRACTION_TIMEOUT_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @param max
     */
    public void setMaxProcessedEntries(int max) {

	getOption(MAX_ENTRIES_OPTION_KEY, Integer.class).get().setValue(max);
    }

    /**
     * @return
     */
    public Optional<Integer> getMaxProcessedEntries() {

	return getOption(MAX_ENTRIES_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @param url
     */
    public void setStartFolderUrl(String url) {

	getOption(START_FOLDER_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @return
     */
    public Optional<String> getStartFolderUrl() {

	return getOption(START_FOLDER_URL_OPTION_KEY, String.class).get().getOptionalValue();
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

    /**
     * @return
     */
    public Optional<String> getExtractionPath() {

	return getOption(EXTRACTION_PATH_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param string
     */
    public void setExtractionPath(String path) {

	getOption(EXTRACTION_PATH_OPTION_KEY, String.class).get().setValue(path);
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

    @Override
    protected String initConnectorType() {

	return ONAMETConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ONAMET Connector settings";
    }

}
