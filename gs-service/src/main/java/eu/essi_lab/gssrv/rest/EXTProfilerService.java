package eu.essi_lab.gssrv.rest;

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

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class EXTProfilerService extends AbstractProfilerService {

    private class EXTProfilerFilter implements ProfilerSettingFilter {

	public EXTProfilerFilter() {
	}

	@Override
	public boolean accept(ProfilerSetting setting) {

	    @SuppressWarnings("rawtypes")
	    Profiler profiler = (Profiler) setting.createConfigurableOrNull();

	    if (profiler != null) {

		Provider provider = profiler.getProvider();
		
		if (provider != null) {
		    String organization = provider.getOrganization();
		    return organization != null && !organization.equals(ESSILabProvider.ESSI_LAB_ORGANIZATION);
		}
	    }

	    return false;
	}
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Path("view/{viewId:[^/]+}/{customPath}")
    public Response externalGetWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("customPath") String customPath) {

	return serve(new EXTProfilerFilter(), hsr, uriInfo);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Path("{customPath}")
    public Response externalGet(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("customPath") String customPath) {

	return serve(new EXTProfilerFilter(), hsr, uriInfo);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Path("view/{viewId:[^/]+}/{customPath}")
    public Response externalPostWithView(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("viewId") String viewId,
	    @PathParam("customPath") String customPath) {

	return serve(new EXTProfilerFilter(), hsr, uriInfo);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    @Path("{customPath}")
    public Response externalPost(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @PathParam("customPath") String customPath) {

	return serve(new EXTProfilerFilter(), hsr, uriInfo);
    }
}
