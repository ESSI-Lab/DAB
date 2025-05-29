package eu.essi_lab.pdk.validation;

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

import java.util.Map;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

public class GETRequestValidator extends AbstractValidator {

    private String[] mandatoryKeys = new String[] {};

    /**
     * Construct with a set of mandatory parameters
     * 
     * @param keys a set of keys that must be present in the request in order to validate
     */
    public GETRequestValidator(String... keys) {
	this.mandatoryKeys = keys;
    }

    /**
     * Validates HTTP GET requests
     */
    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	// first, it must be a valid HTTP GET
	if (!request.isGetRequest()) {

	    return getErrorMessage("Expected HTTP GET request (Not a HTTP-GET)");

	}

	// then, we check the HTTP GET mandatory keys to be present
	String query = request.getQueryString();

	KeyValueParser parser = new KeyValueParser(query);

	Map<String, String> actualParameters = parser.getParametersMap();

	for (String mandatoryKey : mandatoryKeys) {
	    boolean found = false;
	    for (String actualKey : actualParameters.keySet()) {
		if (actualKey != null && actualKey.toLowerCase().equals(mandatoryKey.toLowerCase())) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		return getErrorMessage("Mandatory HTTP GET parameter not found: " + mandatoryKey, ExceptionCode.MISSING_PARAMETER);
	    }
	}

	return getSuccessMessage();

    }

}
