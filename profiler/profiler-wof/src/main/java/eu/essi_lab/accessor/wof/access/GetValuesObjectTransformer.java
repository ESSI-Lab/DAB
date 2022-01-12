package eu.essi_lab.accessor.wof.access;

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

import eu.essi_lab.accessor.wof.HydroServerProfiler;
import eu.essi_lab.accessor.wof.WOFRequest;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
public class GetValuesObjectTransformer extends GetValuesTransformer {
    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	GetValuesObjectValidator validator = new GetValuesObjectValidator();
	ValidationMessage ret = validator.validate(request);
	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}
	return extendedValidation(request);

    }

    public WOFRequest getValuesRequest(WebRequest request) {
	return new GetValuesObjectRequest(request);
    }

    public String getVariableNotFoundInSiteErrorCode() {
	return HydroServerProfiler.ERROR_GET_VALUES_OBJECT_VARIABLE_NOT_FOUND_IN_SITE;
    }

    @Override
    public DefaultAccessResultSetMapper getMapper() {
	return new GetValuesObjectResultSetMapper();

    }

}
