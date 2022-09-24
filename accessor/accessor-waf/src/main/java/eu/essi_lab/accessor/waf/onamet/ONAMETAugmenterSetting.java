package eu.essi_lab.accessor.waf.onamet;

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

import java.util.Arrays;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;

/**
 * @author Fabrizio
 */
public class ONAMETAugmenterSetting extends AugmenterSetting {

    /**
     * 
     */
    private static final String S3_BUCKET_SETTINGS_ID = "s3AugmenterBucketSetting";
    /**
     * 
    */
    private static final String DELETE_NC_FILES_OPTION_KEY = "deleteNcFiles";

    /**
     * 
     */
    public ONAMETAugmenterSetting() {

	S3StorageSetting s3Settings = new S3StorageSetting();
	s3Settings.setName("ONAMET Augmenter S3 settings");
	s3Settings.setIdentifier(S3_BUCKET_SETTINGS_ID);
	s3Settings.setEditable(false);
	s3Settings.setCanBeDisabled(false);

	Option<BooleanChoice> deleteOption = BooleanChoiceOptionBuilder.get().//
		withKey(DELETE_NC_FILES_OPTION_KEY).//
		withLabel("Delete extracted/downloaded NetCDF files from the local file system").//
		withSingleSelection().//
		withValues(Arrays.asList(BooleanChoice.TRUE,BooleanChoice.FALSE)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(deleteOption);

	addSetting(s3Settings);
    }

    /**
     * @param object
     */
    public ONAMETAugmenterSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ONAMETAugmenterSetting(String object) {

	super(object);
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
     * @return
     */
    public boolean deleteNCFiles() {

	return BooleanChoice.toBoolean(getOption(DELETE_NC_FILES_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     *  
     */
    public void setDeleteNCFiles(boolean delete) {

	getOption(DELETE_NC_FILES_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(delete));
    }
}
