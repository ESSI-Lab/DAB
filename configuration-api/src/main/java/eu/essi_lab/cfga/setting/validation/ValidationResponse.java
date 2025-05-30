package eu.essi_lab.cfga.setting.validation;

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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrizio
 */
public class ValidationResponse {

    /**
     * @author Fabrizio
     */
    public enum ValidationResult {
	/**
	 * 
	 */
	VALIDATION_FAILED,
	/**
	 * 
	 */
	VALIDATION_SUCCESSFUL
    }

    private ValidationResult result;
    private List<String> errors;
    private List<String> warnings;

    /**
     * 
     */
    public ValidationResponse() {

	errors = new ArrayList<String>();
	warnings = new ArrayList<String>();

	setResult(ValidationResult.VALIDATION_SUCCESSFUL);
    }

    /**
     * @param result
     */
    public void setResult(ValidationResult result) {

	this.result = result;
    }

    /**
     * @return
     */
    public ValidationResult getResult() {

	return result;
    }

    /**
     * @return
     */
    public List<String> getErrors() {

	return errors;
    }

    /**
     * @return
     */
    public List<String> getWarnings() {

	return warnings;
    }

    /**
     * 
     * @return
     */
    public boolean isEmpty() {

	return getErrors().isEmpty() && getWarnings().isEmpty();
    }

}
