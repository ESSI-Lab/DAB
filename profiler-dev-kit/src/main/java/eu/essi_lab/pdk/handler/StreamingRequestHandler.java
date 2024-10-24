package eu.essi_lab.pdk.handler;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.json.JSONObject;

import eu.essi_lab.authorization.xacml.XACMLAuthorizer;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

/**
 * @author boldrini
 */
public abstract class StreamingRequestHandler extends DefaultRequestHandler {

    private static final String STREAMING_REQUEST_HANDLER_AUTHORIZATION_ERROR = "STREAMING_REQUEST_HANDLER_AUTHORIZATION_ERROR";

    /**
     * 
     */
    private static final Object LOCK = new Object();

    private static IDiscoveryStringExecutor discoveryExecutor;
    private static IAccessExecutor accessExecutor;
    private static XACMLAuthorizer authorizer;

    static {
	ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
	discoveryExecutor = loader.iterator().next();

	ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	accessExecutor = accessLoader.iterator().next();

	try {
	    authorizer = new XACMLAuthorizer();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(StreamingRequestHandler.class).error(e);
	}
    }

    /**
     * 
     */
    public StreamingRequestHandler() {

    }

    /**
     * 
     */
    protected Object getEntity(WebRequest request) throws GSException {

	boolean authorized = false;

	try {

	    RequestMessage message = RequestMessage.create();

	    message.setRequestId(request.getRequestId());

	    message.setWebRequest(request);

	    message.setCurrentUser(request.getCurrentUser());

	    message.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	    message.setPage(new Page());

	    Optional<String> viewId = request.extractViewId();

	    if (viewId.isPresent()) {

		WebRequestTransformer.setView(viewId.get(), message.getDataBaseURI(), message);
	    }

	    synchronized (LOCK) {

		authorized = authorizer.isAuthorized(message);
	    }

	    // authorizer.close();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    throw GSException.createException(this.getClass(), STREAMING_REQUEST_HANDLER_AUTHORIZATION_ERROR, ex);
	}

	if (authorized) {

	    return getStreamingResponse(request);
	}

	builder = builder.status(Status.FORBIDDEN);

	return getStringResponse(request);
    }

    /**
     * Returns the response as a stream
     * 
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     * @return a non <code>null</code> string
     * @throws GSException if errors occurred during the response creation
     */
    public abstract StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException;

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	return "";
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    protected ResultSet<String> exec(DiscoveryMessage message) throws GSException {

	return discoveryExecutor.retrieveStrings(message);
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    protected ResultSet<DataObject> exec(AccessMessage message) throws GSException {

	return accessExecutor.retrieve(message);
    }

    /**
     * @param output
     * @param message
     * @throws IOException
     */
    protected void printErrorMessage(OutputStream output, String message) throws IOException {

	OutputStreamWriter writer = new OutputStreamWriter(output);

	JSONObject error = new JSONObject();
	error.put("message", message);

	writer.write(error.toString());
	writer.close();
    }
}
