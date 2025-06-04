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

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Provides utility return message constructors
 * 
 * @author boldrini
 */
public abstract class AbstractValidator implements WebRequestValidator {

    public AbstractValidator() {
    }

    @Override
    public abstract ValidationMessage validate(WebRequest request) throws GSException;

    protected ValidationMessage getErrorMessage(String error) {
	return getErrorMessage(error, ExceptionCode.NO_APPLICABLE_CODE);
    }

    protected ValidationMessage getErrorMessage(String error, ExceptionCode code) {
	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);
	message.setError(error);
	message.setErrorCode(code.getCode());
	return message;
    }

    protected ValidationMessage getSuccessMessage() {
	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return message;
    }

}
