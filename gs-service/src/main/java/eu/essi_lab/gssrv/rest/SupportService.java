package eu.essi_lab.gssrv.rest;

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

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import eu.essi_lab.lib.odip.ODIPVocabularyHandler;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.OutputFormat;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Profile;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Target;
import eu.essi_lab.lib.utils.GSLoggerFactory;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class SupportService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/odip")
    public Response odip(//
	    @QueryParam("target") String target, //
	    @QueryParam("term") String term, //
	    @QueryParam("profile") String profile, //
	    @QueryParam("label") String label, //
	    @QueryParam("suggestion") String suggestion, //
	    @QueryParam("callback") String callback) { //

	ODIPVocabularyHandler handler = new ODIPVocabularyHandler();
	handler.setOutputFormat(OutputFormat.JSON);

	String output = null;
	try {
	    if (label != null) {
		output = handler.getTerm(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target), label);
	    } else if (term != null) {
		output = handler.getLabel(term);
	    } else {
		output = handler.listLabels(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target),suggestion);
	    }
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    JSONObject object = new JSONObject();
	    object.put("error", ex.getMessage());
	    output = object.toString();
	}

	output = callback + "(" + output + ")";

	return Response.ok(output, MediaType.APPLICATION_JSON).build();
    }
}
