/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.database;

/**
 * @author Fabrizio
 */
public class UsersDatabaseSetting extends DatabaseSetting {

    /**
     * 
     */
    public UsersDatabaseSetting() {

	setName("Users database settings");
	setDescription("If enabled and configured, this setting allows to retrieve users information from a specific database");
    }

    protected String getDbSettingId() {

	return "usersDbSetting";
    }

    protected String getVolatileDbSettingId() {

	return "usersVolFDbSetting";
    }

}
