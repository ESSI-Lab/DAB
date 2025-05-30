package eu.essi_lab.profiler.wof.access;

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

import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;

/**
 * @author boldrini
 */
public class GetValuesRequestFilter extends GETRequestFilter {

    private static final String HYDRO_SERVER_REQUEST_FILTER_ERROR = "HYDRO_SERVER_REQUEST_FILTER_ERROR";
    private List<ResultType> types;

    public GetValuesRequestFilter() {
	types = new ArrayList<>();
    }

    public GetValuesRequestFilter(ResultType type) {
	types = new ArrayList<>();
	types.add(type);
    }

    public GetValuesRequestFilter(String queryString, InspectionStrategy strategy) {
	super(queryString, strategy);
	types = new ArrayList<>();
    }

    public void addResultTypeCondition(ResultType type) {

	types.add(type);
    }

    @Override
    public boolean accept(WebRequest request) throws GSException {

	GetValuesValidator validator = new GetValuesValidator();
	ValidationMessage validationMessage = validator.validate(request);
	if (validationMessage.getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)) {
	    return true;
	}
	return false;

    }
}
