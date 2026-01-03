package eu.essi_lab.profiler.wof.discovery.sites;

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

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.validation.WebRequestValidator;

/**
 * @author boldrini
 */
public class GetSitesByBoxValidatorWithSeries implements WebRequestValidator {

    public GetSitesByBoxValidatorWithSeries() {
	// empty constructor for service loader
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	try {
	    GetSitesByBoxRequest bboxRequest = new GetSitesByBoxRequest(request);

	    ValidationMessage message = new ValidationMessage();
	    if (bboxRequest.isIncludeSeries()) {
		message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    } else {
		message.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	    return message;

	} catch (Exception e) {
	    return getErrorMessage(e.getMessage());
	}

    }

    private ValidationMessage getErrorMessage(String error) {
	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);
	message.setError(error);
	message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	return message;
    }

}
