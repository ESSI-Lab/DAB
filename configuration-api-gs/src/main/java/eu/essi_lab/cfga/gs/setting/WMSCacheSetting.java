package eu.essi_lab.cfga.gs.setting;

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author boldrini
 */
public class WMSCacheSetting extends Setting {

    private static final String WMSCACHE_MODE_KEY = "wmsCacheMode";
    private static final String WMS_CACHE_REDIS_HOSTNAME_KEY = "wmsCacheRedisHostname";
    private static final String WMS_CACHE_REDIS_USERNAME_KEY = "wmsCacheRedisUsername";
    private static final String WMS_CACHE_REDIS_PASSWORD_KEY = "wmsCacheRedisPassword";
    private static final String WMS_CACHE_S3_HOSTNAME_KEY = "wmsCacheS3Hostname";
    private static final String WMS_CACHE_S3_USERNAME_KEY = "wmsCacheS3Username";
    private static final String WMS_CACHE_S3_PASSWORD_KEY = "wmsCacheS3Password";
    private static final String WMS_CACHE_S3_BUCKETNAME = "wmsCacheS3BucketName";
    private static final String WMS_CACHE_FOLDERNAME = "wmsCacheFolderName";

    public enum WMSCacheMode implements LabeledEnum {

	/**
	 * 
	 */
	DISABLED("Disabled"),
	/**
	 * 
	 */
	LOCAL_FILESYSTEM("Local filesystem"),
	/**
	 * 
	 */
	REDIS_S3("Redis plus S3");

	private final String name;

	/**
	 * @param name
	 */
	WMSCacheMode(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

    /**
     * 
     */
    public WMSCacheSetting() {

	setName("WMS Cache Settings");
	setDescription("Settings required by the DAB to enable and configure the WMS cache.");
	enableCompactMode(false);
	setEditable(false);
	setEnabled(false);

	Option<WMSCacheMode> compType = OptionBuilder.get(WMSCacheMode.class).//
		withKey(WMSCACHE_MODE_KEY).//
		withLabel("WMS cache mode").//
		withSingleSelection().//
		withValues(LabeledEnum.values(WMSCacheMode.class)).//
		withSelectedValue(LabeledEnum.values(WMSCacheMode.class).getFirst()).//
		cannotBeDisabled().//
		build();
	addOption(compType);

	Option<String> hostname = StringOptionBuilder.get().//
		withKey(WMS_CACHE_REDIS_HOSTNAME_KEY).//
		withLabel("Redis hostname").//
		cannotBeDisabled().//
		build();
	addOption(hostname);
	
	Option<String> username = StringOptionBuilder.get().//
		withKey(WMS_CACHE_REDIS_USERNAME_KEY).//
		withLabel("Redis username").//
		cannotBeDisabled().//
		build();
	addOption(username);

	Option<String> password = StringOptionBuilder.get().//
		withKey(WMS_CACHE_REDIS_PASSWORD_KEY).//
		withLabel("Redis password").//
		cannotBeDisabled().//
		build();
	addOption(password);
	
	addOption(StringOptionBuilder.get().//
		withKey(WMS_CACHE_S3_HOSTNAME_KEY).//
		withLabel("S3 hostname").//
		cannotBeDisabled().//
		build());
	
	addOption(StringOptionBuilder.get().//
		withKey(WMS_CACHE_S3_USERNAME_KEY).//
		withLabel("S3 username").//
		cannotBeDisabled().//
		build());
	
	addOption(StringOptionBuilder.get().//
		withKey(WMS_CACHE_S3_PASSWORD_KEY).//
		withLabel("S3 password").//
		cannotBeDisabled().//
		build());
	
	addOption(StringOptionBuilder.get().//
		withKey(WMS_CACHE_S3_BUCKETNAME).//
		withLabel("S3 bucket name").//
		cannotBeDisabled().//
		build());

	Option<String> folder = StringOptionBuilder.get().//
		withKey(WMS_CACHE_FOLDERNAME).//
		withLabel("Folder name").//
		cannotBeDisabled().//
		build();
	addOption(folder);
    }

    /**
     * @param object
     */
    public WMSCacheSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WMSCacheSetting(String object) {

	super(object);
    }

    /**
     *  
     */
    public void setMode(WMSCacheMode computationType) {

	getOption(WMSCACHE_MODE_KEY, WMSCacheMode.class).get().setValue(computationType);
    }

    /**
     * @return
     */
    public WMSCacheMode getMode() {

	return getOption(WMSCACHE_MODE_KEY, WMSCacheMode.class).get().getValue();
    }

    /**
     * @param user
     */
    public void setRedisHostname(String user) {

	getOption(WMS_CACHE_REDIS_HOSTNAME_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getRedisHostname() {

	return getOption(WMS_CACHE_REDIS_HOSTNAME_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param user
     */
    public void setRedisUser(String user) {

	getOption(WMS_CACHE_REDIS_USERNAME_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getRedisUser() {

	return getOption(WMS_CACHE_REDIS_USERNAME_KEY, String.class).get().getOptionalValue();
    }


    /**
     * @param user
     */
    public void setRedisPassword(String user) {

	getOption(WMS_CACHE_REDIS_PASSWORD_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getRedisPassword() {

	return getOption(WMS_CACHE_REDIS_PASSWORD_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setS3Hostname(String user) {

	getOption(WMS_CACHE_S3_HOSTNAME_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getS3Hostname() {

	return getOption(WMS_CACHE_S3_HOSTNAME_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param user
     */
    public void setS3User(String user) {

	getOption(WMS_CACHE_S3_USERNAME_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getS3User() {

	return getOption(WMS_CACHE_S3_USERNAME_KEY, String.class).get().getOptionalValue();
    }


    /**
     * @param user
     */
    public void setS3Password(String user) {

	getOption(WMS_CACHE_S3_PASSWORD_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getS3Password() {

	return getOption(WMS_CACHE_S3_PASSWORD_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param user
     */
    public void setS3Bucketname(String user) {

	getOption(WMS_CACHE_S3_BUCKETNAME, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getS3Bucketname() {

	return getOption(WMS_CACHE_S3_BUCKETNAME, String.class).get().getOptionalValue();
    }
    
    /**
     * @param user
     */
    public void setFoldername(String user) {

	getOption(WMS_CACHE_FOLDERNAME, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getFoldername() {

	return getOption(WMS_CACHE_FOLDERNAME, String.class).get().getOptionalValue();
    }

}
