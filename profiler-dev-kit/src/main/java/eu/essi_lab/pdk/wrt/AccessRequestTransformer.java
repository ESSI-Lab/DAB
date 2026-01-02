package eu.essi_lab.pdk.wrt;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.request.executor.IAccessExecutor;

/**
 * Validates and transforms an "access query" in the correspondent {@link AccessMessage}. The access query is
 * represented by a {@link WebRequest} which encapsulates request parameters and headers.<br>
 * This component is part of the {@link AccessHandler} composition and the calling of {@link #transform(WebRequest)}
 * method is the first step of the workflow
 * 
 * @author Fabrizio
 */
public abstract class AccessRequestTransformer extends WebRequestTransformer<AccessMessage> {

    /**
     * 
     */
    private static final String ACCESS_REQUEST_TRANSFORMER_NULL_ONLINE_ID = "ACCESS_REQUEST_TRANSFORMER_NULL_ONLINE_ID";

    /**
     * Refines the <code>message</code> by setting the {@link DataDescriptor} the online identifier and the
     * {@link GSSource}s
     * 
     * @see AccessMessage#setOnlineId(String)
     * @see AccessMessage#setTargetDataDescriptor(DataDescriptor)
     * @see RequestMessage#setSources(List)
     * @see #getTargetDescriptor(WebRequest)
     * @see #getSources(WebRequest)
     */
    public AccessMessage refineMessage(AccessMessage message) throws GSException {

	String onlineId = getOnlineId(message.getWebRequest());

	if (onlineId == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Null online id from: " + message.getWebRequest().getUriInfo().getRequestUri(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_REQUEST_TRANSFORMER_NULL_ONLINE_ID);
	}

	message.setOnlineId(onlineId);

	Optional<DataDescriptor> descriptor = getTargetDescriptor(message.getWebRequest());
	if (descriptor.isPresent()) {
	    message.setTargetDataDescriptor(descriptor.get());

	    DataFormat format = descriptor.get().getDataFormat();
	    String extension = ".dat";
	    if (format != null) {
		if (format.equals(DataFormat.NETCDF()) || format.isSubTypeOf(DataFormat.NETCDF())) {
		    extension = ".nc";
		}
		if (format.equals(DataFormat.IMAGE_PNG())) {
		    extension = ".png";
		}
		if (format.equals(DataFormat.IMAGE_JPG())) {
		    extension = ".jpg";
		}
		if (format.equals(DataFormat.IMAGE_GEOTIFF())) {
		    extension = ".tiff";
		}
		if (format.equals(DataFormat.DDS())) {
		    extension = ".dds";
		}
		if (format.equals(DataFormat.DAS())) {
		    extension = ".das";
		}
	    }
	    message.setUserJobResultId("user-request-result-" + System.currentTimeMillis() + extension);

	}

	List<GSSource> sources = getSources(message.getWebRequest());
	message.setSources(sources);

	return message;
    }

    /**
     * Creates an instance of {@link AccessMessage}
     */
    protected AccessMessage createMessage() {

	return new AccessMessage();
    }

    /**
     * Returns an {@link Optional} possible empty which describes a {@link DataDescriptor} representing the supplied
     * <code>request</code> parameters.<br>
     * 
     * @implNote
     * 	  An empty optional means that no transformation is required, so the data will be downloaded and returned
     *           as it is.<br>
     *           Otherwise, the returned {@link DataDescriptor} should has only the properties that require a
     *           transformation. The missing properties are added by the {@link IAccessExecutor} implementation using
     *           the {@link DataDescriptor#fillMissingInformationOf(DataDescriptor)} method
     * @param request the {@link WebRequest} triggered by the {@link AccessProfiler} supported client
     * @return {@link Optional} possible empty which describes a {@link DataDescriptor} representing the supplied
     *         <code>request</code> parameters
     */
    protected abstract Optional<DataDescriptor> getTargetDescriptor(WebRequest request) throws GSException;

    /**
     * @param request
     * @return
     * @throws GSException
     */
    protected abstract String getOnlineId(WebRequest request) throws GSException;

    /**
     * This default implementation invokes the {@link #ConfigurationWrapper.getAllSources()} method
     */
    protected List<GSSource> getSources(WebRequest request) throws GSException {

	return ConfigurationWrapper.getAllSources();
    }

}
