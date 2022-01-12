package eu.essi_lab.demo.extensions.profiler;

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

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.demo.extensions.DemoProvider;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
public class DemoInfoHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	JSONObject info = new JSONObject();

	JSONObject service = new JSONObject();
	service.put("name", "demo-service");
	service.put("version", "1.0.0");
	info.put("service", service);

	JSONObject provider = new JSONObject();
	provider.put("name", DemoProvider.getInstance().getOrganization());
	provider.put("email", DemoProvider.getInstance().getEmail());
	info.put("provider", provider);

	return info.toString(3);
    }

    @Override
    public MediaType getMediaType(WebRequest request) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
