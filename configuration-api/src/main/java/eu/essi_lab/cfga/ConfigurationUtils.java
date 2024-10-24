package eu.essi_lab.cfga;

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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ConfigurationUtils {

    /**
     * @param configuration
     * @param checkResponse
     * @throws GSException
     */
    public static void fix(Configuration configuration, CheckResponse checkResponse) throws GSException {

	fix(configuration, checkResponse, true);
    }

    /**
     * @param configuration
     * @param checkResponse
     * @param backup
     * @throws GSException
     */
    public static void fix(Configuration configuration, CheckResponse checkResponse, boolean backup) throws GSException {

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration fix STARTED");

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration backup STARTED");

	if (backup) {

	    try {
		ConfigurationSource backupSource = configuration.getSource().backup();

		boolean emptyOrMissing = backupSource.isEmptyOrMissing();

		if (emptyOrMissing) {

		    throw GSException.createException(//
			    ConfigurationUtils.class, //
			    "Unable to backup configuration, backup configuration is empty or missing", //
			    null, //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    "ConfigurationBackupEmptyOrMissingError");
		}

		GSLoggerFactory.getLogger(ConfigurationUtils.class).info("Backup to: {}", backupSource.getLocation());

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(ConfigurationUtils.class).error(e);

		throw GSException.createException(//
			ConfigurationUtils.class, //
			"Unable to backup configuration: " + e.getMessage(), //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			"ConfigurationBackupError");
	    }
	}

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration backup ENDED");

	List<Setting> settings = checkResponse.getSettings();

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("The following settings classes have issues: {} ",
		settings.stream().map(s -> s.getSettingClass().getSimpleName()).distinct().collect(Collectors.joining(", ")));

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("The following settings will be fixed: {} ",
		settings.stream().map(s -> s.getName()).collect(Collectors.joining(", ")));

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Settings fix STARTED");

	for (Setting setting : settings) {

	    Setting fixed = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(fixed);

	    SelectionUtils.deepAfterClean(fixed);

	    boolean replaced = configuration.replace(fixed);

	    if (!replaced) {

		throw GSException.createException(//
			ConfigurationUtils.class, //
			"Unable to replace fixed setting: " + fixed.getName(), //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			"ConfigurationFixReplaceError" //
		);
	    }
	}

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Settings fix ENDED");

	try {

	    GSLoggerFactory.getLogger(ConfigurationUtils.class).info("Configuration flush STARTED");

	    configuration.flush();

	    GSLoggerFactory.getLogger(ConfigurationUtils.class).info("Configuration flush ENDED");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(ConfigurationUtils.class).error(e);

	    throw GSException.createException(//
		    ConfigurationUtils.class, //
		    "Unable to flush configuration after fix:" + e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "ConfigurationFlushAfterFixError" //
	    );
	}

	GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration fix ENDED");
    }

    /**
     * @param configuration
     * @param mapper
     * @param mapped
     * @return
     */
    public static <T> Object deepMap(Configuration configuration, Function<Setting, T> mapper, List<T> mapped) {

	configuration.list().forEach(s -> SettingUtils.deepMap(s, mapper, mapped));

	return null;
    }

    /**
     * @param configuration
     * @param predicate
     * @param matches
     */
    public static void deepFind(Configuration configuration, Predicate<Setting> predicate, List<Setting> matches) {

	configuration.list().forEach(s -> SettingUtils.deepFind(s, predicate, matches));
    }

    /**
     * @param configuration
     * @param action
     */
    public static void deepPerform(Configuration configuration, Consumer<Setting> action) {

	configuration.list().forEach(s -> SettingUtils.deepPerform(s, action));
    }
}
