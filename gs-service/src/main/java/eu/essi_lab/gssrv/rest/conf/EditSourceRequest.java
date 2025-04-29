/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class EditSourceRequest extends PutSourceRequest {

    /**
     * 
     */
    public EditSourceRequest() {
    }

    /**
     * @param object
     */
    public EditSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	List<Parameter> supportedParameters = super.getSupportedParameters();

	return supportedParameters.//
		stream().// sourceId is mandatory in this request
		map(p -> p.getName().equals(SOURCE_ID) ? Parameter.of(p.getName(), p.getContentType(), p.getInputPattern().get(), true)
			: p)
		.//
		collect(Collectors.toList());
    }
}
