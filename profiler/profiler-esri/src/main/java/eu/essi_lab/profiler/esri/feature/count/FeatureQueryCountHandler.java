package eu.essi_lab.profiler.esri.feature.count;

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

import java.io.ByteArrayInputStream;
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.esri.feature.query.ESRIRequest;
import eu.essi_lab.profiler.esri.feature.query.FeatureQueryRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * @author boldrini
 */
public class FeatureQueryCountHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest request) throws GSException {

	ESRIRequest esriRequest = new ESRIRequest(request);
	
	String callback = esriRequest.getParameter("callback");

	JSONObject ret = new JSONObject();

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	FeatureQueryRequestTransformer transformer = new FeatureQueryRequestTransformer();
	DiscoveryMessage discoveryMessage = transformer.transform(request);
	discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

	CountSet count = executor.count(discoveryMessage);

	ret.put("count", count.getCount());

	if (callback == null) {
	    callback = "";
	} else {
	    callback = callback + "(";
	}

	String str = callback + ret.toString();

	if (!callback.isEmpty()) {
	    str += ")";
	}

	ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
	return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();

    }
    

    protected StorageInfo getStorageURI(DiscoveryMessage message) throws GSException {
	StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();
	if (storageUri != null) {

	    message.setDataBaseURI(storageUri);

	} else {

	    GSException exception = GSException.createException(getClass(), //
		    "Data Base storage URI not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_WARNING, //
		    "DB_STORAGE_URI_NOT_FOUND");

	    message.getException().getErrorInfoList().add(exception.getErrorInfoList().get(0));

	    GSLoggerFactory.getLogger(this.getClass()).warn("Data Base storage URI not found");
	}
	return storageUri;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
