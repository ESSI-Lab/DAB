package eu.essi_lab.profiler.administration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.VolatileOptionSet;
import eu.essi_lab.model.configuration.option.VolatileSourceAdd;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class AdminPostHandler implements WebRequestHandler {

    public static final String CONFIGURATION_NOT_FOUND_USER_ERROR = "Configuration file was not found";
    public static final String CONFIGURATION_NOT_FOUND_CORRECTION_SUGGESTION = "You might need to initialize GI-suite first";
    private static final String UNKNOWN_POST_ACTION_ERR_ID = "UNKNOWN_POST_ACTION_ERR_ID";

    private String resource;

    public AdminPostHandler(String requestedResource) {

	resource = requestedResource;
    }

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	ClonableInputStream stream = webRequest.getBodyStream();

	if (resource.equalsIgnoreCase("option")) {

	    VolatileOptionSet parsed = new Deserializer().deserialize(stream.clone(), VolatileOptionSet.class);

	    GSConfigurationManager manager = new GSConfigurationManager();

	    execSetOption(manager, parsed.getOptions());

	    return Response.status(Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(manager.getConfiguration().serialize()).build();

	}

	if (resource.equalsIgnoreCase("source")) {

	    VolatileSourceAdd parsed = new Deserializer().deserialize(stream.clone(), VolatileSourceAdd.class);

	    GSConfigurationManager manager = new GSConfigurationManager();

	    execAddSource(manager, parsed.getSource());

	    return Response.status(Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(manager.getConfiguration().serialize()).build();

	}

	if (resource.equalsIgnoreCase("configuration")) {

	    GSConfiguration configuration = new Deserializer().deserialize(stream.clone(), GSConfiguration.class);

	    GSConfigurationManager manager = new GSConfigurationManager();

	    manager.flush(configuration);

	    return Response.status(Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(manager.getConfiguration().serialize()).build();

	}

	throw GSException.createException(this.getClass(), "Unknown Post action", null, null, ErrorInfo.ERRORTYPE_CLIENT,
		ErrorInfo.SEVERITY_ERROR, UNKNOWN_POST_ACTION_ERR_ID);

    }

    private void execAddSource(GSConfigurationManager manager, Source source) throws GSException {

	manager.addSource(source);

    }

    public void execSetOption(GSConfigurationManager manager, Map<String, GSConfOption<?>> map) throws GSException {

	Iterator<String> it = map.keySet().iterator();

	while (it.hasNext()) {
	    String key = it.next();

	    ConfigurableKey configurableKey = new ConfigurableKey(key);

	    manager.setOption(configurableKey, map.get(key));
	}

    }

}
