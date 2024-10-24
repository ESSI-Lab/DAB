package eu.essi_lab.profiler.rest.handler.token;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Generates tokens with hard-coded role "geoss-private-write" and requires the header "mirrorsiteclient" with
 * hard-coded value "GWP-748f4cca-bed0-4d1f-93f0-31eae3cf7c61"
 * 
 * @author Fabrizio
 */
public class MirrorSiteTokenGeneratorHandler extends TokenGeneratorHandler {

    /**
     * 
     */
    private static final String MIRROR_SITE_HEADER_NAME_PREFIX = "mirrorsiteclient";
    /**
     * 
     */
    private static final String MIRROR_SITE_CLIENT_1 = "GWP-748f4cca-bed0-4d1f-93f0-31eae3cf7c61";

    /**
    * 
    */
    @Override
    public String generateTokenAndUser(String role, String tokenPostfix) throws GSException {

	return super.generateTokenAndUser(GEOSSPrivateWriteRolePolicySet.ROLE, "_msc");
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	message.setResponseEncoding(MediaType.APPLICATION_JSON);

	if (request.isOptionsRequest()) {

	    return message;
	}

	String headerValue = request.getServletRequest().getHeader(MIRROR_SITE_HEADER_NAME_PREFIX);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();
	Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	List<String> supportedClients = new ArrayList<>();

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();
	    List<String> keys = properties.keySet().//
		    stream().//
		    filter(k -> k.toString().startsWith(MIRROR_SITE_HEADER_NAME_PREFIX)).//
		    map(k -> k.toString()).//
		    collect(Collectors.toList());

	    keys.forEach(k -> supportedClients.add(properties.get(k).toString()));

	    GSLoggerFactory.getLogger(getClass()).debug("Supported GWP clients:\n " + supportedClients);
	}

	if (supportedClients.isEmpty()) {

	    message.setError("Missing list of supported clients");
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setLocator("request");
	}

	//
	//
	//

	else if (headerValue == null) {

	    message.setError("Missing authorization header");
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setLocator("request");

	} else if (!supportedClients.contains(headerValue)) {

	    message.setError("Client not authorized");
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setLocator("request");
	}

	return message;
    }
}
