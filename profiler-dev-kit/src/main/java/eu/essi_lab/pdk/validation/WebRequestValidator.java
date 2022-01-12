package eu.essi_lab.pdk.validation;

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

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.Profiler;
public interface WebRequestValidator {

    /**
     * Validates the supplied <code>request</code> according to the {@link Profiler} service interface and
     * returns a {@link ValidationMessage}.<br>
     * In case of {@link ValidationResult#VALIDATION_FAILED}, the {@link ValidationMessage#getError()} returns
     * the error description.<br>
     * When a <code>RequestValidator</code> is dispatched to handle the {@link WebRequest}, the {@link Profiler} first
     * invokes
     * this method and only if the validation is successful, the query execution can proceeds
     */
    public ValidationMessage validate(WebRequest request) throws GSException;
}
