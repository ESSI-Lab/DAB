package eu.essi_lab.pdk.rsf;

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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;

/**
 * @author Fabrizio
 */
public class JSONSemanticResponseFormatter extends SemanticResultSetFormatter<JSONObject> {

    @Override
    public Response format(SemanticMessage message, SemanticResponse<JSONObject> response) throws GSException {

	UriInfo uriInfo = message.getWebRequest().getUriInfo();

	List<PathSegment> pathSegments = uriInfo.getPathSegments();

	PathSegment lastSegment = pathSegments.get(pathSegments.size() - 1);

	Page page = message.getPage();

	SemanticCountResponse countResponse = response.getCountResponse();

	List<JSONObject> resultsList = response.getResultsList();

	JSONObject out = new JSONObject();

	out.put("start", page.getStart());
	out.put("total", countResponse.getCount());
	out.put("count", resultsList.size());

	JSONArray array = new JSONArray();

	for (JSONObject object : resultsList) {

	    array.put(object);
	}

	//
	// browsing request with subject id or search request
	//
	if ((message.getBrowsingOperation().isPresent() && message.getBrowsingOperation().get().getSubjectId().isPresent()) ||

		!message.getBrowsingOperation().isPresent()) {

	    out.put("results", array);

	    Optional<JSONObject> parent = response.getParentObject();
	    if (parent.isPresent()) {

		JSONObject parentConcept = parent.get();
		out.put("parentConcept", parentConcept);
	    }

	} else {

	    //
	    // no subject specified, the root is returned
	    //
	    out.put("entryPoints", array);
	}

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(out.toString());
	builder.type(MediaType.APPLICATION_JSON_TYPE);

	return builder.build();
    }

    @Override
    public FormattingEncoding getEncoding() {

	return null;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
