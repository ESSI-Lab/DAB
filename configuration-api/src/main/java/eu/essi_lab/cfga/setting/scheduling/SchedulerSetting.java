/**
 * 
 */
package eu.essi_lab.cfga.setting.scheduling;

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

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SchedulerSetting extends Setting implements EditableSetting {

    /**
     * 
     */
    private static final String USER_DATE_TIME_OPTION_KEY = "userDateTime";

    private static final String PERSISTENT_SCHEDULER_SETTING_ID = "persistentScheduler";
    private static final String VOLATILE_SCHEDULER_SETTING_ID = "volatileScheduler";

    private static final String SQL_DATABASE_URI_KEY = "sqlDatabaseURL";
    private static final String SQL_DATABASE_USER_OPTION_KEY = "sqlDatabaseUser";
    private static final String SQL_DATABASE_PWD_OPTION_KEY = "sqlDatabasePassword";
    private static final String SQL_DATABASE_NAME_OPTION_KEY = "sqlDatabaseName";

    /**
     * @author Fabrizio
     */
    public enum JobStoreType {

	/**
	 * 
	 */
	VOLATILE,
	/**
	 * 
	 */
	PERSISTENT;
    }

    /**
     * 
     */
    public SchedulerSetting() {

	setName("Scheduler");
	setCanBeDisabled(false);

	setCanBeCleaned(false);
	enableCompactMode(false);

	// only one scheduler type can be selected
	setSelectionMode(SelectionMode.SINGLE);

	//
	// User date time zone
	//

	Option<String> userDateTimeOption = StringOptionBuilder.get().//
		withKey(USER_DATE_TIME_OPTION_KEY).//
		withLabel("The date time by which the user sets the scheduling").//
		withSingleSelection().//
		withValues(Arrays.asList(DateTimeZone.getAvailableIDs().toArray(new String[] {}))).//
		withSelectedValue("Europe/Berlin").//
		cannotBeDisabled().//
		build();

	addOption(userDateTimeOption);

	//
	//
	//

	Setting volatileSchedulerSetting = new Setting();
	volatileSchedulerSetting.setName("Volatile scheduler");
	volatileSchedulerSetting.setDescription("This scheduler stores jobs and triggers in memory");
	volatileSchedulerSetting.setIdentifier(VOLATILE_SCHEDULER_SETTING_ID);
	volatileSchedulerSetting.setSelected(true);
	volatileSchedulerSetting.setEditable(false);
	volatileSchedulerSetting.setCanBeDisabled(false);
	volatileSchedulerSetting.enableCompactMode(false);

	addSetting(volatileSchedulerSetting);

	//
	// ---
	//

	Setting persistentSchedulerSetting = new Setting();
	persistentSchedulerSetting.setIdentifier(PERSISTENT_SCHEDULER_SETTING_ID);
	persistentSchedulerSetting.setName("Persistent scheduler");
	persistentSchedulerSetting.setDescription("This scheduler stores jobs and triggers in a SQL database");
	persistentSchedulerSetting.setCanBeDisabled(false);
	persistentSchedulerSetting.enableCompactMode(false);
	persistentSchedulerSetting.setEditable(false);

	Option<String> sqlUrlOption = StringOptionBuilder.get().//
		withLabel("SQL Database URI (e.g: jdbc:mysql://localhost:3306)").//
		withKey(SQL_DATABASE_URI_KEY).//
		cannotBeDisabled().//
		required().//
		build();

	persistentSchedulerSetting.addOption(sqlUrlOption);

	Option<String> userOption = StringOptionBuilder.get().//
		withLabel("SQL Database User").//
		withKey(SQL_DATABASE_USER_OPTION_KEY).//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		cannotBeDisabled().//
		required().//
		build();

	persistentSchedulerSetting.addOption(userOption);

	Option<String> pwdOption = StringOptionBuilder.get().//
		withLabel("SQL Database Password").//
		withKey(SQL_DATABASE_PWD_OPTION_KEY).//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		cannotBeDisabled().//
		required().//
		build();

	persistentSchedulerSetting.addOption(pwdOption);

	Option<String> dbNameOption = StringOptionBuilder.get().//
		withLabel("SQL Database name").//
		withKey(SQL_DATABASE_NAME_OPTION_KEY).//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		cannotBeDisabled().//
		required().//
		build();

	persistentSchedulerSetting.addOption(dbNameOption);

	addSetting(persistentSchedulerSetting);
    }

    /**
     * @param object
     */
    public SchedulerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SchedulerSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public JobStoreType getJobStoreType() {

	Optional<Setting> optional = getPersistentSchedulerSetting();

	if (optional.isPresent() && optional.get().isSelected()) {

	    return JobStoreType.PERSISTENT;
	}

	return JobStoreType.VOLATILE;
    }

    /**
     * @param jobStoreType
     */
    public void setJobStoreType(JobStoreType jobStoreType) {

	switch (jobStoreType) {
	case VOLATILE:
	    getPersistentSchedulerSetting().ifPresent(s -> s.setSelected(false));
	    getVolatileSchedulerSetting().ifPresent(s -> s.setSelected(true));
	    break;
	case PERSISTENT:
	    getPersistentSchedulerSetting().ifPresent(s -> s.setSelected(true));
	    getVolatileSchedulerSetting().ifPresent(s -> s.setSelected(false));
	}
    }

    /**
     * @param name
     */
    public void setSQLDatabaseName(String name) throws UnsupportedOperationException {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile scheduler setting");
	}

	getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public String getSQLDatabaseName() {

	if (isVolatile()) {

	    return null;
	}

	return getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_NAME_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param uri
     */
    public void setSQLDatabaseUri(String uri) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile scheduler setting");
	}

	getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_URI_KEY, String.class).get().setValue(uri);
    }

    /**
     * @return
     */
    public String getSQLDatabaseUri() {

	if (isVolatile()) {

	    return null;
	}

	return getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_URI_KEY, String.class).get().getValue();
    }

    /**
     * @param user
     */
    public void setSQLDatabaseUser(String user) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile scheduler setting");
	}

	getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public String getSQLDatabaseUser() {

	if (isVolatile()) {

	    return null;
	}

	return getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_USER_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param password
     */
    public void setSQLDatabasePassword(String password) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile scheduler setting");
	}

	getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_PWD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public String getSQLDatabasePassword() {

	if (isVolatile()) {

	    return null;
	}

	return getPersistentSchedulerSetting().get().getOption(SQL_DATABASE_PWD_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param dateTimeZone
     */
    public void setUserDateTimeZone(String dateTimeZone) {

	getOption(USER_DATE_TIME_OPTION_KEY, String.class).get().select(v -> v.equals(dateTimeZone));
    }

    /**
     * @return
     */
    public DateTimeZone getUserDateTimeZone() {

	return DateTimeZone.forID(getOption(USER_DATE_TIME_OPTION_KEY, String.class).get().getSelectedValue());
    }

    /**
     * 
     */
    public void debugSQLSettings() {

	GSLoggerFactory.getLogger(getClass()).debug("DB URI: " + getSQLDatabaseUri());
	GSLoggerFactory.getLogger(getClass()).debug("DB name: " + getSQLDatabaseName());
	GSLoggerFactory.getLogger(getClass()).debug("DB pwd: " + getSQLDatabasePassword());
	GSLoggerFactory.getLogger(getClass()).debug("DB user: " + getSQLDatabaseUser());
    }

    /**
     * @return
     */
    private Optional<Setting> getPersistentSchedulerSetting() {

	return getSetting(PERSISTENT_SCHEDULER_SETTING_ID);
    }

    /**
     * @return
     */
    private Optional<Setting> getVolatileSchedulerSetting() {

	return getSetting(VOLATILE_SCHEDULER_SETTING_ID);
    }

    /**
     * @return
     */
    private boolean isVolatile() {

	return getJobStoreType() == JobStoreType.VOLATILE;
    }
}
