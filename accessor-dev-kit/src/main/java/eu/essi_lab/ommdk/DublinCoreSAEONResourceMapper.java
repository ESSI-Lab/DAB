package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;

/**
 * @author boldrini
 */
public class DublinCoreSAEONResourceMapper extends DublinCoreResourceMapper {

    private static final String SAEON_ACCESS_URL_CONVENTION = "DEFLAYER=";
    private static final String SAEON_APP = "http://app01.saeon.ac.za/PLATFORM_TEST/MAP/signature.asp?sources=";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.CSW_SAEON_NS_URI;
    }

    @Override
    protected List<Online> createOnlineResource(String link) {
	List<Online> ret = new ArrayList<>();

	Online online1 = new Online();

	if (link.toUpperCase().contains(SAEON_ACCESS_URL_CONVENTION)) {
	    String[] splitted = link.split(SAEON_ACCESS_URL_CONVENTION);

	    String baseURL = splitted[0];

	    if (baseURL.endsWith("&")) {

		baseURL = baseURL.substring(0, baseURL.lastIndexOf('&')) + "?";

	    }

	    online1.setLinkage(baseURL);
	    online1.setName(splitted[1]);

	    if (baseURL.toLowerCase().contains("wms")) {
		online1.setProtocol("WMS");
	    }

	    if (baseURL.toLowerCase().contains("wfs")) {
		online1.setProtocol("WFS");
	    }

	}

	ret.add(online1);

	Online online2 = new Online();

	online2.setLinkage(SAEON_APP + link);

	online2.setProtocol("geoss:helperapp:online");

	ret.add(online2);

	online1.setLinkage(link);

	return ret;
    }
}
