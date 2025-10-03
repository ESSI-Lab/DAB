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

import java.util.ServiceLoader;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * Loads all the registered {@link EditableSetting} and executes the {@link EditableSetting#test(Setting)} test
 * 
 * @author Fabrizio
 */
public class RegisteredEditableMethod implements CheckMethod {
    
   

    @Override
    public CheckResponse check(Configuration configuration) {

	CheckResponse checkResponse = new CheckResponse(getName());

	GSLoggerFactory.getLogger(getClass()).info("Editable registered settings check STARTED");

	ServiceLoader<EditableSetting> loader = ServiceLoader.load(EditableSetting.class);

	StreamUtils.iteratorToStream(loader.iterator()).forEach(setting -> {

	    boolean test = EditableSetting.test((Setting) setting);

	    if (!test) {

		checkResponse.getMessages().add("Editable registered setting check failed:" + ((Setting) setting).getName());
		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).info("Editable registered settings check ENDED");

	return checkResponse;
    }
}
