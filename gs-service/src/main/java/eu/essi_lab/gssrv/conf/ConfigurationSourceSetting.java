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

import java.util.Arrays;
import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.S3StorageSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ConfigurationSourceSetting extends Setting implements EditableSetting {

    private static final String OS_SETTING_ID = "osSetting";
    private static final String OS_URI_OPTION_KEY = "osUri";
    private static final String OS_NAME_OPTION_KEY = "osName";
    private static final String OS_CONFIG_NAME_OPTION_KEY = "osConfigName";
    private static final String OS_TYPE_OPTION_KEY = "osType";
    private static final String OS_USER_OPTION_KEY = "osUser";
    private static final String OS_PWD_OPTION_KEY = "osPwd";

    private static final String S3_SETTING_ID = "s3SettingId";
    private static final String S3_URI_OPTION_KEY = "s3Uri";
    private static final String S3_CONFIG_BUCKET_OPTION_KEY = "s3Bucket";
    private static final String S3_ACCESS_KEY_OPTION_KEY = "s3AccessKey";
    private static final String S3_SECRET_KEY_OPTION_KEY = "s3SecretKey";

    /**
     * @author Fabrizio
     */
    enum ConfigurationSource {

	/**
	 * 
	 */
	MARK_LOGIC,
	/**
	 * 
	 */
	OPENSEARCH,
	/**
	 * 
	 */
	S3
    }

    /**
     * 
     */
    public ConfigurationSourceSetting() {

	setCanBeDisabled(false);
	setEditable(false);
	setShowHeader(false);
	setCanBeCleaned(false);
	setSelectionMode(SelectionMode.SINGLE);

	DatabaseSetting dbSetting = ConfigurationWrapper.getDatabaseSetting();

	DatabaseImpl impl = getDatabaseImpl(dbSetting);

	{

	    S3StorageSetting s3 = ConfigurationWrapper.getDownloadSetting().getS3StorageSetting();

	    Setting s3SourceSetting = new Setting();
	    s3SourceSetting.setName("S3");
	    s3SourceSetting.setIdentifier(S3_SETTING_ID);
	    s3SourceSetting.setCanBeDisabled(false);
	    s3SourceSetting.enableCompactMode(false);
	    s3SourceSetting.setEditable(false);
	    s3SourceSetting.setSelected(true);

	    Option<String> uriOption = StringOptionBuilder.get().//
		    withLabel("S3 URI").//
		    withKey(S3_URI_OPTION_KEY).//
		    withConditionalValue(s3.getEndpoint().isPresent(), s3.getEndpoint().orElse(null)).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    s3SourceSetting.addOption(uriOption);

	    Option<String> bucketOption = StringOptionBuilder.get().//
		    withLabel("Configuration bucket").//
		    withKey(S3_CONFIG_BUCKET_OPTION_KEY).//
		    withConditionalValue(s3.getBucketName().isPresent(), s3.getBucketName().orElse(null)).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    s3SourceSetting.addOption(bucketOption);

	    Option<String> userOption = StringOptionBuilder.get().//
		    withLabel("Access key").//
		    withKey(S3_ACCESS_KEY_OPTION_KEY).//
		    withConditionalValue(s3.getAccessKey().isPresent(), s3.getAccessKey().orElse(null)).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    s3SourceSetting.addOption(userOption);

	    Option<String> pwdOption = StringOptionBuilder.get().//
		    withLabel("Secret key").//
		    withKey(S3_SECRET_KEY_OPTION_KEY).//
		    withConditionalValue(s3.getSecretKey().isPresent(), s3.getSecretKey().orElse(null)).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    s3SourceSetting.addOption(pwdOption);

	    addSetting(s3SourceSetting);
	}

	{
	    Setting osSourceSetting = new Setting();
	    osSourceSetting.setName("OpenSearch");
	    osSourceSetting.setIdentifier(OS_SETTING_ID);
	    osSourceSetting.setCanBeDisabled(false);
	    osSourceSetting.enableCompactMode(false);
	    osSourceSetting.setEditable(false);

	    Option<String> uriOption = StringOptionBuilder.get().//
		    withLabel("Database URI").//
		    withDescription("E.g.: 'http://localhost:9200'").//
		    withKey(OS_URI_OPTION_KEY).//
		    withConditionalValue(impl == DatabaseImpl.OPENSEARCH, dbSetting.getDatabaseUri()).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    osSourceSetting.addOption(uriOption);

	    Option<String> osType = StringOptionBuilder.get().//
		    withLabel("Database type").//
		    withKey(OS_TYPE_OPTION_KEY).//
		    withSingleSelection().//
		    withValues(Arrays.asList("OpenSearch", "OpenSearch managed (AWS)")).//
		    withSelectedValue(impl == DatabaseImpl.OPENSEARCH
			    ? dbSetting.getDatabaseType().map(v -> v.equals("osl") ? "OpenSearch" : "OpenSearch managed (AWS)").get()
			    : "OpenSearch")
		    .//
		    cannotBeDisabled().//
		    required().//
		    build();

	    osSourceSetting.addOption(osType);

	    Option<String> osName = StringOptionBuilder.get().//
		    withLabel("Database name").//
		    withDescription("E.g.: 'test', 'preprod', 'prod'").//
		    withKey(OS_NAME_OPTION_KEY).//
		    withConditionalValue(impl == DatabaseImpl.OPENSEARCH, dbSetting.getDatabaseName()).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    osSourceSetting.addOption(osName);

	    Option<String> osConfigName = StringOptionBuilder.get().//
		    withLabel("Configuration name").//
		    withDescription("E.g.: 'testConfig', 'preprodConfig', 'prodConfig'").//
		    withKey(OS_CONFIG_NAME_OPTION_KEY).//
		    cannotBeDisabled().//
		    required().//
		    build();

	    osSourceSetting.addOption(osConfigName);

	    Option<String> userOption = StringOptionBuilder.get().//
		    withLabel("Database user/access key").//
		    withKey(OS_USER_OPTION_KEY).//
		    withValue(impl == DatabaseImpl.OPENSEARCH ? dbSetting.getDatabaseUser() : "").//
		    cannotBeDisabled().//
		    build();

	    osSourceSetting.addOption(userOption);

	    Option<String> pwdOption = StringOptionBuilder.get().//
		    withLabel("Database password/secret key").//
		    withKey(OS_PWD_OPTION_KEY).//
		    withConditionalValue(impl == DatabaseImpl.OPENSEARCH, dbSetting.getDatabasePassword()).//
		    cannotBeDisabled().//
		    build();

	    osSourceSetting.addOption(pwdOption);

	    addSetting(osSourceSetting);
	}
    }

    /**
     * @return
     */
    public ConfigurationSource getSelectedSource() {

	// Setting mlSetting = getSetting(MARK_LOGIC_SETTING_ID, Setting.class).get();
	//
	// if (mlSetting.isSelected()) {
	//
	// return ConfigurationSource.MARK_LOGIC;
	// }

	Setting osSetting = getSetting(OS_SETTING_ID, Setting.class).get();

	if (osSetting.isSelected()) {

	    return ConfigurationSource.OPENSEARCH;
	}

	return ConfigurationSource.S3;
    }

    /**
     * @return
     */
    public Optional<StorageInfo> getSelectedStorageInfo() {

	// Setting mlSetting = getSetting(MARK_LOGIC_SETTING_ID, Setting.class).get();
	//
	// if (mlSetting.isSelected()) {
	//
	// Optional<String> mlName = mlSetting.getOption(MARK_LOGIC_NAME_OPTION_KEY,
	// String.class).get().getOptionalValue();
	// Optional<String> mlPwd = mlSetting.getOption(MARK_LOGIC_PWD_OPTION_KEY,
	// String.class).get().getOptionalValue();
	// Optional<String> mlUser = mlSetting.getOption(MARK_LOGIC_USER_OPTION_KEY,
	// String.class).get().getOptionalValue();
	// Optional<String> mlUri = mlSetting.getOption(MARK_LOGIC_URI_OPTION_KEY,
	// String.class).get().getOptionalValue();
	// Optional<String> mlFolder = mlSetting.getOption(MARK_LOGIC_FOLDER_OPTION_KEY,
	// String.class).get().getOptionalValue();
	//
	// if (mlName.isEmpty() || mlPwd.isEmpty() || mlUser.isEmpty() || mlUri.isEmpty() || mlFolder.isEmpty()) {
	//
	// return Optional.empty();
	// }
	//
	// }

	Setting osSetting = getSetting(OS_SETTING_ID, Setting.class).get();

	if (osSetting.isSelected()) {

	    Optional<String> osName = osSetting.getOption(OS_NAME_OPTION_KEY, String.class).get().getOptionalValue();
	    Optional<String> osConfigName = osSetting.getOption(OS_CONFIG_NAME_OPTION_KEY, String.class).get().getOptionalValue();

	    Optional<String> osUri = osSetting.getOption(OS_URI_OPTION_KEY, String.class).get().getOptionalValue();
	    Optional<String> osPwd = osSetting.getOption(OS_PWD_OPTION_KEY, String.class).get().getOptionalValue();
	    Optional<String> osUser = osSetting.getOption(OS_USER_OPTION_KEY, String.class).get().getOptionalValue();
	    Optional<String> osType = osSetting.getOption(OS_TYPE_OPTION_KEY, String.class).get().getOptionalValue();

	    if (osName.isEmpty() || osUri.isEmpty() || osPwd.isEmpty() || osUser.isEmpty() || osConfigName.isEmpty()) {

		return Optional.empty();
	    }

	    StorageInfo info = new StorageInfo(osUri.get());
	    info.setUser(osUser.get());
	    info.setPassword(osPwd.get());

	    info.setType(osType.get().equals("OpenSearch") ? "osl" : "osm");

	    info.setName(osConfigName.get());
	    info.setIdentifier(osName.get());

	    return Optional.of(info);
	}

	Setting s3Setting = getSetting(S3_SETTING_ID, Setting.class).get();

	Optional<String> s3Uri = s3Setting.getOption(S3_URI_OPTION_KEY, String.class).get().getOptionalValue();
	Optional<String> s3Bucket = s3Setting.getOption(S3_CONFIG_BUCKET_OPTION_KEY, String.class).get().getOptionalValue();
	Optional<String> s3AccessKey = s3Setting.getOption(S3_ACCESS_KEY_OPTION_KEY, String.class).get().getOptionalValue();
	Optional<String> s3SecretKey = s3Setting.getOption(S3_SECRET_KEY_OPTION_KEY, String.class).get().getOptionalValue();

	if (s3Uri.isEmpty() || s3Bucket.isEmpty() || s3AccessKey.isEmpty() || s3SecretKey.isEmpty()) {

	    return Optional.empty();
	}

	StorageInfo info = new StorageInfo(s3Uri.get());
	info.setUser(s3AccessKey.get());
	info.setPassword(s3SecretKey.get());
	info.setName(s3Bucket.get());

	return Optional.of(info);
    }

    /**
     * @param databaseSetting
     * @return
     */
    private DatabaseImpl getDatabaseImpl(DatabaseSetting databaseSetting) {

	try {
	    Database database = DatabaseFactory.get(databaseSetting.asStorageInfo());
	    return database.getImplementation();

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }
}
