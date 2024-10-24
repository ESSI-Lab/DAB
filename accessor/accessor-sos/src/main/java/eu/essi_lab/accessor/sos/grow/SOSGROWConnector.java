package eu.essi_lab.accessor.sos.grow;

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

import eu.essi_lab.accessor.sos.SOSRequestBuilder;
import eu.essi_lab.accessor.sos.tahmo.SOSTAHMOConnector;

/**
 * @author Fabrizio
 */
public class SOSGROWConnector extends SOSTAHMOConnector {

    /**
     * 
     */

    private static final String HTTP = "http://";

    /**
     * 
     */
    private static final String GROW_CREDENTIALS = ".%5CGROW_HL:321Demo";

    /**
     * 
     */
    public static final String TYPE = "SOS GROW Connector";

    /**
     * @author Fabrizio
     */
    public class SOSGROWRequestBuilder extends SOSTTAHMORequestBuilder {

	/**
	 * @param serviceUrl
	 * @param version
	 */
	public SOSGROWRequestBuilder(String serviceUrl, String version) {
	    super(serviceUrl, version);
	}

	@Override
	public String addCredentialsInRequests(String url) {
	    return url.replace(HTTP, HTTP + GROW_CREDENTIALS + "@");
	}

	@Override
	public String removeCredentialsInRequests(String url) {
	    if (url.contains(GROW_CREDENTIALS)) {
		String[] splittedString = url.split("@");
		if (splittedString.length > 1) {
		    url = HTTP + splittedString[1];
		}
	    }
	    return url;
	}
    }

    /**
     * @return
     */
    public SOSRequestBuilder createRequestBuilder() {

	return new SOSGROWRequestBuilder(getSourceURL(), "2.0.0");
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
