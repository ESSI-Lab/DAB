package eu.essi_lab.profiler.wfs.description;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ServiceLoader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wfs.WFSProfiler;
import eu.essi_lab.profiler.wfs.WFSQueryHandler;
import eu.essi_lab.profiler.wfs.WFSRequest.Parameter;
import eu.essi_lab.profiler.wfs.feature.FeatureType;
import eu.essi_lab.profiler.wfs.feature.WFSGetFeatureRequest;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;

public class DescribeFeatureQueryHandler extends WFSQueryHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WFSDescribeFeatureRequest wfsRequest = new WFSDescribeFeatureRequest(request);

	String type = wfsRequest.getParameterValue(Parameter.TYPE_NAME);

	FeatureType featureType = findFeatureType(type);

	if (featureType == null) {
	    ValidationMessage ret = new ValidationMessage();
	    ret.setError("Feature type " + type + " unknown");
	    ret.setErrorCode(WFSProfiler.ERROR_CODE_INVALID_PARAMETER_VALUE);
	    ret.setLocator(Parameter.TYPE_NAME.getKeys()[0]);
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	    return ret;
	}

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

   

    @Override
    public Response handle(WebRequest request) throws GSException {

	WFSDescribeFeatureRequest wfsRequest = new WFSDescribeFeatureRequest(request);

	String type = wfsRequest.getParameterValue(Parameter.TYPE_NAME);

	FeatureType featureType = findFeatureType(type);

	String schema = featureType.getSchema();

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(schema);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();

    }

   
}
