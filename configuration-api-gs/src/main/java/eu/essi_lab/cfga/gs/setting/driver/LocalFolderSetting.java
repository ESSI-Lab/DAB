package eu.essi_lab.cfga.gs.setting.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import org.json.JSONObject;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class LocalFolderSetting extends Setting {

    /**
     * 
     */
    private static final String PATH_OPTION_ID = "localFolderPath";
    /**
     * 
     */
    private static final String USER_TEMP_FOLDER = "file://user.temp";

    /**
     * 
     */
    public LocalFolderSetting() {

	setName("Local folder");
	enableCompactMode(false);

	setCanBeDisabled(false);
	setEditable(false);

	Option<String> pathOption = StringOptionBuilder.get().//
		withLabel("Path to a folder in which to store resources").//
		withDescription(
			"If the given folder do not exists, an attempt to create it will be done. The given path must begin with 'file://'. Put 'file://user.temp' for the local temporary user folder")
		.//
		withValue(USER_TEMP_FOLDER).//
		withKey(PATH_OPTION_ID).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(pathOption);
    }

    /**
     * @param object
     */
    public LocalFolderSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public LocalFolderSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public String getFolderPath() {

	String value = getOption(PATH_OPTION_ID, String.class).get().getValue();

	if (value.equals(USER_TEMP_FOLDER)) {

	    value = System.getProperty("java.io.tmpdir");
	}
	
	value = value.replace("\\", "/");

	return value.replace("file://", "");
    }

    /**
     * @param path
     */
    public void setFolderPath(String path) {

	getOption(PATH_OPTION_ID, String.class).get().setValue(path);
    }
}
