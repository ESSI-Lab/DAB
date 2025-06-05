/**
 * 
 */
package eu.essi_lab.profiler.om.scheduling;

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

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class OMSchedulerSetting extends SchedulerWorkerSetting {

    private static final String REQUEST_URL_OPTION_KEY = "requestURL";
    private static final String OPERATION_ID_OPTION_KEY = "operationId";

    /**
     * 
     */
    public OMSchedulerSetting() {

	setConfigurableType(OMSchedulerWorker.CONFIGURABLE_TYPE);

	setGroup(SchedulingGroup.ASYNCH_ACCESS);

	Option<String> requestUrlOption = StringOptionBuilder.get().//
		withKey(REQUEST_URL_OPTION_KEY).//
		build();
	addOption(requestUrlOption);

	Option<String> operationIdOption = StringOptionBuilder.get().//
		withKey(OPERATION_ID_OPTION_KEY).//
		build();
	addOption(operationIdOption);
    }

    /**
     * @param url
     */
    public void setRequestURL(String url) {

	getOption(REQUEST_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @param id
     */
    public void setOperationId(String id) {

	getOption(OPERATION_ID_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @return
     */
    public String getRequestURL() {

	return getOption(REQUEST_URL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public String getOperationId() {

	return getOption(OPERATION_ID_OPTION_KEY, String.class).get().getValue();
    }
}
