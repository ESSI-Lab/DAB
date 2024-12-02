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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class ESSIProfilerService extends AbstractProfilerService {

    private class ESSIProfilerFilter implements ProfilerSettingFilter {

	private String requestPath;

	public ESSIProfilerFilter(String requestPath) {

	    if (requestPath.contains("/")) {
		int index = requestPath.indexOf("/");
		requestPath = requestPath.substring(0, index);
	    }

	    this.requestPath = requestPath;
	}

	@Override
	public boolean accept(ProfilerSetting setting) {

	    String profilerPath = setting.getServicePath();

	    return profilerPath.equals(requestPath);
	}
    }

    /*
     * common requests
     */
    @GET
    @Path("{path:.*}")
    public Response getRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("{path:.*}")
    public Response postRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("{path:.*}")
    public Response putRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("{path:.*}")
    public Response deleteRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("{path:.*}")
    public Response optionsRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    /*
     * requests with view
     */
    @GET
    @Path("view/{viewId:[^/]+}/{path:.*}")
    public Response getRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("view/{viewId:[^/]+}/{path:.*}")
    public Response postRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("view/{viewId:[^/]+}/{path:.*}")
    public Response putRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("view/{viewId:[^/]+}/{path:.*}")
    public Response deleteRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("view/{viewId:[^/]+}/{path:.*}")
    public Response optionsRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    /*
     * requests with token and view
     */
    @GET
    @Path("token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response getRequestWithTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response postRequestWithTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response putRequestWithTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response deleteRequestWithTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response optionsRequestWithTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    /*
     * semantic requests
     */
    @GET
    @Path("semantic/{path:.*}")
    public Response semanticGetRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("semantic/{path:.*}")
    public Response semanticPostRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("semantic/{path:.*}")
    public Response semanticPutRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("semantic/{path:.*}")
    public Response semanticDeleteRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {

	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("semantic/{path:.*}")
    public Response semanticOptionsRequest(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("path") String path) {

	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    /*
     * semantic requests with view
     */
    @GET
    @Path("semantic/view/{viewId:[^/]+}/{path:.*}")
    public Response semanticGetRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("semantic/view/{viewId:[^/]+}/{path:.*}")
    public Response semanticPostRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("semantic/view/{viewId:[^/]+}/{path:.*}")
    public Response semanticPutRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("semantic/view/{viewId:[^/]+}/{path:.*}")
    public Response semanticDeleteRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("semantic/view/{viewId:[^/]+}/{path:.*}")
    public Response semanticOptionsRequestWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @GET
    @Path("semantic/token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response getRequestWithSemanticTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @POST
    @Path("semantic/token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response postRequestWithSemanticTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @PUT
    @Path("semantic/token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response putRequestWithSemanticTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

    @DELETE
    @Path("semantic/token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response deleteRequestWithSemanticTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }
    
    @OPTIONS
    @Path("semantic/token/{tokenId:[^/]+}/view/{viewId:[^/]+}/{path:.*}")
    public Response optionsRequestWithSemanticTokenAndView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @PathParam("tokenId") String tokenId, @PathParam("viewId") String viewId, @PathParam("path") String path) {
	return serve(new ESSIProfilerFilter(path), hsr, uriInfo);
    }

}
