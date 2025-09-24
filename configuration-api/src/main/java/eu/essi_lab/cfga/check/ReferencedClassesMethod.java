package eu.essi_lab.cfga.check;

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

import java.util.Optional;
import java.util.function.Consumer;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.ObjectExtension;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Verifies that all the classes referenced by the settings and the options of the given <code>configuration</code>
 * can be instantiated.<br>
 * The following referenced classes are tested:<br>
 * <ul>
 * <li>{@link Setting#getSettingClass()}</li>
 * <li>{@link Setting#getOptionalExtensionClass()}</li>
 * <li>{@link Setting#getOptionalAfterCleanFunctionClass()}</li>
 * <li>{@link Setting#getOptionalValidatorClass()}</li>
 * <li>{@link Option#getValueClass()}</li>
 * <li>{@link Option#getOptionalLoaderClass()}</li>
 * </ul>
 * 
 * @author Fabrizio
 */
public class ReferencedClassesMethod implements CheckMethod, Consumer<Setting> {

    /**
     * 
     */
    private CheckResponse checkResponse;

    /**
     * 
     */
    public ReferencedClassesMethod() {

	checkResponse = new CheckResponse(getName());
    }

    @Override
    public CheckResponse check(Configuration configuration) {

	GSLoggerFactory.getLogger(getClass()).info("Referenced classes check STARTED");

	ConfigurationUtils.deepPerform(configuration, this);

	GSLoggerFactory.getLogger(getClass()).info("Referenced classes check ENDED");

	return checkResponse;
    }

    /**
     * 
     */
    @Override
    public void accept(Setting setting) {

	try {

	    //
	    // 1) Setting class
	    //
	    setting.getSettingClass();

	} catch (Throwable ex) {

	    checkResponse.getMessages().add("Setting class not found: " + setting.getObject().getString("settingClass"));
	    checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
	}

	//
	// 2) Setting extension class
	//
	Optional<Class<? extends ObjectExtension>> optionalExtensionClass = setting.getOptionalExtensionClass();

	if (!optionalExtensionClass.isPresent() && setting.getObject().has("extensionClass")) {

	    checkResponse.getMessages().add("Setting ObjectExtension class not found: " + setting.getObject().getString("extensionClass"));
	    checkResponse.setCheckResult(CheckResult.CHECK_FAILED);

	}

	//
	// 3) Setting after clean function
	//
	Optional<Class<? extends AfterCleanFunction>> optionalAfterCleanFunctionClass = setting.getOptionalAfterCleanFunctionClass();

	if (!optionalAfterCleanFunctionClass.isPresent() && setting.getObject().has("afterCleanFunction")) {

	    checkResponse.getMessages()
		    .add("Setting AfterCleanFunction class not found: " + setting.getObject().getString("afterCleanFunction"));
	    checkResponse.setCheckResult(CheckResult.CHECK_FAILED);

	}

	//
	// 4) Setting validator class
	//
	Optional<Class<? extends Validator>> optionalValidatorClass = setting.getOptionalValidatorClass();

	if (!optionalValidatorClass.isPresent() && setting.getObject().has("validatorClass")) {

	    checkResponse.getMessages().add("Setting Validator class not found: " + setting.getObject().getString("validatorClass"));
	    checkResponse.setCheckResult(CheckResult.CHECK_FAILED);

	}

	setting.getOptions().forEach(option -> {

	    //
	    // 5) Option value class
	    //
	    Class<?> valueClass = option.getValueClass();

	    if (valueClass == null) {

		checkResponse.getMessages().add("Option value class not found: " + option.getObject().getString("valueClass"));
		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);

	    }

	    //
	    // 6) Option values loader
	    //
	    @SuppressWarnings("rawtypes")
	    Optional<Class<? extends ValuesLoader>> optionalLoaderClass = option.getOptionalLoaderClass();

	    if (!optionalLoaderClass.isPresent() && option.getObject().has("valuesLoaderClass")) {

		checkResponse.getMessages()
			.add("Option ValuesLoader class not found: " + option.getObject().getString("valuesLoaderClass"));
		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
	    }
	});
    }
}
