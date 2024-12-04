package eu.essi_lab.profiler.wof;

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

import java.util.Optional;

import javax.ws.rs.core.UriInfo;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.messages.bond.DynamicViewAnd;
import eu.essi_lab.messages.bond.DynamicViewCountry;
import eu.essi_lab.messages.bond.DynamicViewSource;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Country;

/**
 * @author boldrini
 */
public class WOFMapperUtils {

    /**
     * If true, the identifier is generated from the source id and not incrementally assigned. This is preferred to be
     * true.
     */
    public static final boolean IDENTIFIER_FROM_SOURCE = true;

    public static String getServiceUrl(DiscoveryMessage message, GSSource source) {

	DynamicViewSource dynamicView = new DynamicViewSource();
	dynamicView.setPostfix(source.getUniqueIdentifier());

	return getServiceUrl(message, dynamicView);
    }

    public static String getServiceUrl(DiscoveryMessage message, Country country) {

	DynamicViewCountry dynamicView = new DynamicViewCountry();
	dynamicView.setPostfix(country.getISO3());

	return getServiceUrl(message, dynamicView);
    }

    public static String getServiceUrl(DiscoveryMessage message, View myView) {
	String ret = "http://localhost"; //

	try {
	    UriInfo uri = message.getWebRequest().getUriInfo();

	    String viewId = null;

	    if (message.getView().isPresent()) {

		viewId = message.getView().get().getId();
	    }

	    String sourceViewId = myView.getId();

	    String finalViewId = "";

	    if (viewId == null || viewId.equals("") || viewId.equals(sourceViewId)) {
		finalViewId = sourceViewId;
	    } else {
		DynamicViewAnd andView = new DynamicViewAnd();
		andView.setPostfix(viewId + DynamicView.ARGUMENT_SEPARATOR + sourceViewId);
		finalViewId = andView.getId();
	    }

	    // encoding seems not be needed
	    // finalViewId = URLEncoder.encode(finalViewId, "UTF-8");

	    String baseURI = uri.getBaseUri().toString();
	    String forwardedProto = message.getWebRequest().getServletRequest().getHeader("x-forwarded-proto");
	    if (forwardedProto == null) {
		forwardedProto = message.getWebRequest().getServletRequest().getHeader("x-forwarded-protocol");
	    }
	    if (baseURI.startsWith("http://") && forwardedProto != null && forwardedProto.equals("https")) {
		baseURI = baseURI.replace("http://", "https://");
	    }
	    Optional<String> token = message.getWebRequest().extractTokenId();
	    String tokenPart = "";
	    if (token.isPresent()) {
		tokenPart = "/" + WebRequest.TOKEN_PATH + "/" + token.get();
	    }
	    ret = baseURI + tokenPart + "/" + WebRequest.VIEW_PATH + "/" + finalViewId + "/"
		    + new HydroServerProfilerSetting().getServicePath();
	} catch (Exception e) {
	}

	return ret;
    }

}
