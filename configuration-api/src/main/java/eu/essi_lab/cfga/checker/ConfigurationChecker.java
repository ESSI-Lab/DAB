package eu.essi_lab.cfga.checker;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.ObjectExtension;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class ConfigurationChecker implements Consumer<Setting> {

    /**
     * 
     */
    private Set<String> errorsSet;

    /**
     * 
     */
    public ConfigurationChecker() {

	errorsSet = new HashSet<>();
    }

    /**
     * @param configuration
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<String> check(Configuration configuration) {

	checkClasses(configuration);

	editableSettingCheck(configuration);

	editableSettingCheck();

	return new ArrayList(errorsSet);
    }

    /**
     * @param configuration
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<String> checkClasses(Configuration configuration) {

	ConfigurationUtils.deepPerform(configuration, this);

	return new ArrayList(errorsSet);
    }

    /**
     * @return
     */
    public List<String> editableSettingCheck() {

	ServiceLoader<EditableSetting> loader = ServiceLoader.load(EditableSetting.class);
	StreamUtils.iteratorToStream(loader.iterator()).forEach(setting -> {

	    boolean test = EditableSetting.test((Setting) setting);

	    if (!test) {

		errorsSet.add("Editable setting check failed: " + ((Setting) setting).getName());
	    }
	});

	return Arrays.asList(errorsSet.toArray(new String[] {}));
    }

    /**
     * @param configuration
     * @return
     */
    public List<String> editableSettingCheck(Configuration configuration) {

	List<Setting> matches = new ArrayList<>();

	ConfigurationUtils.deepFind(configuration, s -> {

	    try {
		return EditableSetting.class.isAssignableFrom(s.getSettingClass());
	   
	    } catch (Throwable t) {
		
		errorsSet.add("Editable setting check failed: " + ((Setting) s).getName());
	    }
	    
	    return false;

	}, matches);

	matches.forEach(setting -> {

	    boolean test = EditableSetting.test((Setting) setting);

	    if (!test) {

		errorsSet.add("Editable setting check failed: " + ((Setting) setting).getName());
	    }
	});

	return Arrays.asList(errorsSet.toArray(new String[] {}));
    }

    @Override
    public void accept(Setting setting) {

	try {

	    //
	    // 1) Setting class
	    //
	    setting.getSettingClass();

	} catch (Throwable ex) {

	    errorsSet.add("Setting class not found: " + setting.getObject().getString("settingClass"));
	}

	//
	// 2) Setting extension class
	//
	Optional<Class<? extends ObjectExtension>> optionalExtensionClass = setting.getOptionalExtensionClass();

	if (!optionalExtensionClass.isPresent() && setting.getObject().has("extensionClass")) {

	    errorsSet.add("Setting ObjectExtension class not found: " + setting.getObject().getString("extensionClass"));
	}

	//
	// 3) Setting after clean function
	//
	Optional<Class<? extends AfterCleanFunction>> optionalAfterCleanFunctionClass = setting.getOptionalAfterCleanFunctionClass();

	if (!optionalAfterCleanFunctionClass.isPresent() && setting.getObject().has("afterCleanFunction")) {

	    errorsSet.add("Setting AfterCleanFunction class not found: " + setting.getObject().getString("afterCleanFunction"));
	}

	//
	// 4) Setting validator class
	//
	Optional<Class<? extends Validator>> optionalValidatorClass = setting.getOptionalValidatorClass();

	if (!optionalValidatorClass.isPresent() && setting.getObject().has("validatorClass")) {

	    errorsSet.add("Setting Validator class not found: " + setting.getObject().getString("validatorClass"));
	}

	setting.getOptions().forEach(option -> {

	    //
	    // 5) Option value class
	    //
	    Class<?> valueClass = option.getValueClass();

	    if (valueClass == null) {

		errorsSet.add("Option value class not found: " + option.getObject().getString("valueClass"));
	    }

	    //
	    // 6) Option values loader
	    //
	    @SuppressWarnings("rawtypes")
	    Optional<Class<? extends ValuesLoader>> optionalLoaderClass = option.getOptionalLoaderClass();

	    if (!optionalLoaderClass.isPresent() && option.getObject().has("valuesLoaderClass")) {

		errorsSet.add("Option ValuesLoader class not found: " + option.getObject().getString("valuesLoaderClass"));
	    }
	});
    }
}
