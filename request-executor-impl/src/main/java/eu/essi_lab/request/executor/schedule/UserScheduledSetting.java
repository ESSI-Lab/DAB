/**
 * 
 */
package eu.essi_lab.request.executor.schedule;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.messages.RequestMessage;

/**
 * @author Fabrizio
 */
public class UserScheduledSetting extends SchedulerWorkerSetting {

    private static final String MESSAGE = "message";
    private static final String HANDLER = "handler";
    private static final String MAPPER = "mapper";
    private static final String FORMATTER = "formatter";

    public UserScheduledSetting() {

	//
	// the class to configure
	//
	setConfigurableType(UserSchedulerWorker.CONFIGURABLE_TYPE);
	//
	// the scheduling group
	//
	setGroup(SchedulingGroup.ASYNCH_ACCESS);
	
	{
	    Option<RequestMessage> option = OptionBuilder.get(RequestMessage.class).//
		    base64Encoded().//
		    withKey(MESSAGE).//
		    build();
	    addOption(option);
	}
	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(HANDLER).//
		    build();
	    addOption(option);
	}
	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(MAPPER).//
		    build();
	    addOption(option);
	}
	{
	    Option<String> option = StringOptionBuilder.get().//
		    withKey(FORMATTER).//
		    build();
	    addOption(option);
	}
    }

    /**
     * @param object
     */
    public UserScheduledSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public UserScheduledSetting(String object) {

	super(object);
    }

    /**
     * @return the message
     */
    public RequestMessage getRequestMessage() {

	return getOption(MESSAGE, RequestMessage.class).get().getValue();
    }

    /**
     * @param message the message to set
     */
    public void setRequestMessage(RequestMessage message) {

	getOption(MESSAGE, RequestMessage.class).get().setValue(message);
    }

    /**
     * @return the handler
     */
    public String getHandler() {
	
	return getOption(HANDLER, String.class).get().getValue();
    }

    /**
     * @param handler the handler to set
     */
    public void setHandler(String handler) {
	
	getOption(HANDLER, String.class).get().setValue(handler);
    }

    /**
     * @return the mapper
     */
    public String getMapper() {

	return getOption(MAPPER, String.class).get().getValue();

    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(String mapper) {
	
	getOption(MAPPER, String.class).get().setValue(mapper);
    }

    /**
     * @return the formatter
     */
    public String getFormatter() {
	
	return getOption(FORMATTER, String.class).get().getValue();
    }

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(String formatter) {
	
	getOption(FORMATTER, String.class).get().setValue(formatter);
    }
}
