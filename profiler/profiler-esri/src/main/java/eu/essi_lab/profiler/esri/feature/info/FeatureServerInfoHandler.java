package eu.essi_lab.profiler.esri.feature.info;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;

/**
 * @author boldrini
 */
public class FeatureServerInfoHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest request) throws GSException {

	InputStream stream = FeatureServerInfoHandler.class.getClassLoader().getResourceAsStream("esri/feature-server-info-template.json");

	try {
	    String str = IOUtils.toString(stream, StandardCharsets.UTF_8);
	    String http =  request.getUriInfo().getAbsolutePath().toString().replace("rest/info", "services");
	    str = str.replace("$HTTP_URL", http);
	    str = str.replace("$HTTPS_URL", http.replace("http", "https"));
	    stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();

    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
