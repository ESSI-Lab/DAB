package eu.essi_lab.profiler.om;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.google.gson.JsonObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.om.OMRequest.APIParameters;

public class StatusHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
        ValidationMessage ret = new ValidationMessage();
        ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
        return ret;
    }

    public StatusHandler() {
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    handle(output, webRequest);
                } catch (Exception e) {
                    throw new WebApplicationException("Error handling status request", e);
                }
            }
        };
    }

    protected void handle(OutputStream output, WebRequest webRequest) throws Exception {
        OMRequest request = new OMRequest(webRequest);
        String operationId = request.getParameterValue(APIParameters.OPERATION_ID);

        // Create the response object
        JsonObject response = new JsonObject();
        response.addProperty("operationId", operationId);        
        response.addProperty("status", "Not found");
//        response.addProperty("locator", "http://example.com/download.zip");

        // Write the response
        try (OutputStreamWriter writer = new OutputStreamWriter(output)) {
            writer.write(response.toString());
            writer.flush();
        }
    }
} 
