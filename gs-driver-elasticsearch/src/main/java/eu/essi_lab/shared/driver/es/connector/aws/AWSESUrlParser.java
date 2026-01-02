package eu.essi_lab.shared.driver.es.connector.aws;

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

import java.net.MalformedURLException;
import java.net.URL;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author ilsanto
 */
public class AWSESUrlParser {

    /**
     * 
     */
    private static final String AWS_DOMAIN_HOST_SUFFIX = ".es.amazonaws.com";

    /**
     * 
     */
    private String endpoint;

    /**
     * @param url
     */
    public AWSESUrlParser(String url) {

	this.endpoint = url;
	if (endpoint.endsWith("/")) {
	    endpoint = endpoint.substring(0, endpoint.length() - 1);
	}
    }

    /**
     * @return
     */
    public boolean isAWSESEndpoint() {

	try {

	    URL url = new URL(this.endpoint);

	    String host = url.getHost();

	    if (host.toLowerCase().contains(AWS_DOMAIN_HOST_SUFFIX)) {
		return true;
	    }

	} catch (MalformedURLException e) {

	    GSLoggerFactory.getLogger(AWSESUrlParser.class).warn("Provided url {} is not well formed, returning false", endpoint, e);
	}

	return false;
    }

    /**
     * @return
     */
    public String getDomainName() {

	String end = endpoint.replace("." + getRegion() + AWS_DOMAIN_HOST_SUFFIX, "");
	end = end.replace("https://search-", "");

	String name = end.substring(0, end.lastIndexOf("-"));

	return name;
    }

    /**
     * @return
     */
    public String getRegion() {

	return endpoint.substring(endpoint.indexOf(".") + 1, endpoint.indexOf("es", endpoint.indexOf(".")) - 1);
    }
}
