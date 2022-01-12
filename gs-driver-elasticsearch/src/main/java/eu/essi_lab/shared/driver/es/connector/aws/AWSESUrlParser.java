package eu.essi_lab.shared.driver.es.connector.aws;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
public class AWSESUrlParser {

    private transient Logger logger = GSLoggerFactory.getLogger(AWSESUrlParser.class);
    private final String endpoint;
    private static final String AWS_DOMAIN_HOST_SUFFIX_2 = ".es.amazonaws.com";
    private static final String AWS_DOMAIN_HOST_SUFFIX_1 = ".es.amazonaws.com/";
    private static final String HTTPS_PREFIX = "https://";

    public AWSESUrlParser(String url) {

	this.endpoint = url;
    }

    public boolean isAWSESEndpoint() {

	try {

	    URL url = new URL(this.endpoint);

	    String host = url.getHost();

	    if (host.toLowerCase().contains(AWS_DOMAIN_HOST_SUFFIX_2))
		return true;

	} catch (MalformedURLException e) {
	    logger.warn("Provided url {} is not well formed, returning false", endpoint, e);
	}

	return false;
    }

    private String removeUnneeded() {
	return this.endpoint.replace(AWS_DOMAIN_HOST_SUFFIX_1, "").replace(AWS_DOMAIN_HOST_SUFFIX_2, "").replace(HTTPS_PREFIX, "");
    }

    public String getDomainName() {

	String part = removeUnneeded().split("\\.")[0];

	return part.split("-")[1];

    }

    public String getRegion() {

	return removeUnneeded().split("\\.")[1];

    }

}
