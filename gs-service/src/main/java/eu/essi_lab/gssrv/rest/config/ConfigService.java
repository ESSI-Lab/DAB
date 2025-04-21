/**
 * 
 */
package eu.essi_lab.gssrv.rest.config;

import javax.jws.WebService;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

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

/**
 * @author Fabrizio
 */
@WebService
@Path("/")
public class ConfigService {

    /**
     * Optional<br>
     * <ul>
     * <li>If missing, a new harvested source is created with a random id.<br>
     * - 201 Created is returned</li>
     * <li>If provided and a source with given id do not exists, it is created.<br>
     * - The id MUST contains only alphanumeric characters and the '_' symbol.<br>
     * - If the id is not regular, 404 Bad Request is returned, otherwise  
     *  201 Created is returned</li>
     * <li>If provided and a source with given id already exists, the related source is modified according to the other request parameters 
     * "label", "type", "repeatInterval", "repeatIntervalUnit", "startTime".<br>
     * - 204 No Content is returned</li>
     * </ul>
     */
    private static String ID_PARAM = "id";
   
    /**
     * Optional<br>
     * <ul>
     * <li>If missing, random label is created basing on the mandatory endpoint</li>
     * </ul>
     */
    private static String LABEL_PARAM = "label"; // optional
    private static String ENDPOINT_PARAM = "endpoint"; // mandatory
    private static String TYPE_PARAM = "type"; // mandatory

    private static String REPEAT_INTERVAL_PARAM = "repeatInterval"; // optional
    private static String REPEAT_INTERVAL_UNIT_PARAM = "repeatIntervalUnit"; // optional
    private static String START_TIME_PARAM = "startTime"; // optional

    @POST
    @Path("source")
    @Produces(MediaType.APPLICATION_JSON)
    public Response source(@Context UriInfo uriInfo) {

	MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

	if (params.isEmpty()) {

	    return Response.status(Status.BAD_REQUEST).build();
	}

	return Response.status(Status.OK).build();
    }
}
