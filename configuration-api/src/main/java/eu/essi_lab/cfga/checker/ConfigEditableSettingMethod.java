package eu.essi_lab.cfga.checker;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Collects all the {@link EditableSetting}s from the given <code>configuration</code> and executes the
 * {@link EditableSetting#test(Setting)} test
 * 
 * @author Fabrizio
 */
public class ConfigEditableSettingMethod implements CheckMethod {

    @Override
    public CheckResponse check(Configuration configuration) {

	GSLoggerFactory.getLogger(getClass()).info("Editable setting check from configuration STARTED");

	CheckResponse checkResponse = new CheckResponse(getName());

	List<Setting> matches = new ArrayList<>();

	ConfigurationUtils.deepFind(configuration, s -> {

	    try {
		return EditableSetting.class.isAssignableFrom(s.getSettingClass());

	    } catch (Throwable t) {

		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
		checkResponse.getMessages()
			.add("Editable setting isAssignableFrom check of configuration failed: " + ((Setting) s).getName());
	    }

	    return false;

	}, matches);

	matches.forEach(setting -> {

	    boolean test = EditableSetting.test((Setting) setting);

	    if (!test) {

		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
		checkResponse.getMessages().add("Editable setting check from configuration failed: " + ((Setting) setting).getName());
	    }
	});

	GSLoggerFactory.getLogger(getClass()).info("Editable setting check from configuration ENDED");

	return checkResponse;
    }
}
