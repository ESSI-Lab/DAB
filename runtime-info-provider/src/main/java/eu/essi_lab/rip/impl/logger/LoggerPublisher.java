/**
 * 
 */
package eu.essi_lab.rip.impl.logger;

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

import java.util.HashMap;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.rip.RuntimeInfoPublisher;

/**
 * fields @message
 * | parse @message "CONTEXT=* " as @context
 * | parse @message "PROVIDER=* " as @provider
 * | parse @message "RUNTIME_ID=* " as @runtimeId
 * | filter @message like "LoggerPublisher"
 * | sort @timestamp desc
 * | limit 600
 * -
 * -
 * -
 * parse @message "CONTEXT=* " as @context
 * | parse @message "PROVIDER=* " as @provider
 * | parse @message "RUNTIME_ID" as @msg
 * | sort @timestamp desc
 * | filter @message like "DISCOVERY_MESSAGE"
 * -
 * -
 * -
 * https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html?com/amazonaws/services/logs/model/StartQueryRequest.html
 * setQueryString
 * -
 * -
 * -
 * This implementation publishes runtime information to the log stream
 * 
 * @author Fabrizio
 */
public class LoggerPublisher extends RuntimeInfoPublisher {

    /**
     * @param runtimeId
     */
    public LoggerPublisher(String runtimeId, String context) {

	super(runtimeId, context);
    }

    private static final String CONTEXT = "CONTEXT";
    private static final String PROVIDER = "PROVIDER";
    private static final String RUNTIME_ID = "RUNTIME_ID";

    @Override
    public void publish(RuntimeInfoProvider provider) throws GSException {

	if (provider == null) {
	    return;
	}

	HashMap<String, List<String>> map = provider.provideInfo();

	map.forEach((key, list) -> {
	    list.forEach(val -> {

		final StringBuffer buffer = new StringBuffer(addItem(CONTEXT, getContext()));

		buffer.append(addItem(PROVIDER, provider.getName()));
		buffer.append(addItem(RUNTIME_ID, getRuntimeId()));

		if (StringUtils.isNotEmpty(val)) {
		    buffer.append(addItem(key, val));
		}

		GSLoggerFactory.getLogger(getClass()).info(buffer.toString());
	    });
	});
    }

    /**
     * @param tag
     * @param item
     * @return
     */
    private String addItem(String tag, String item) {

	return tag + "=" + StringUtils.encodeUTF8(item) + " ";
    }
}
