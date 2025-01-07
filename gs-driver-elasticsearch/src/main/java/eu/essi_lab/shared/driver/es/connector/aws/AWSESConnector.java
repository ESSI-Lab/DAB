package eu.essi_lab.shared.driver.es.connector.aws;

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

import java.util.Optional;

import org.slf4j.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticsearch.AWSElasticsearch;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClientBuilder;
import com.amazonaws.services.elasticsearch.model.DomainInfo;
import com.amazonaws.services.elasticsearch.model.ListDomainNamesRequest;
import com.amazonaws.services.elasticsearch.model.ListDomainNamesResult;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.shared.driver.es.connector.ESConnector;
import eu.essi_lab.shared.driver.es.connector.ESRequestSubmitter;

/**
 * @author ilsanto
 */
public class AWSESConnector extends ESConnector {

    private Logger logger = GSLoggerFactory.getLogger(AWSESConnector.class);

    BasicAWSCredentials getAWSCredentials() {
	return new BasicAWSCredentials(getEsStorageUri().getUser(), getEsStorageUri().getPassword());
    }

    AWSElasticsearch getAWSClient() {

	return AWSElasticsearchClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
		.withRegion(new AWSESUrlParser(getEsStorageUri().getUri()).getRegion()).build();
    }

    @Override
    public boolean testConnection() {

	if (!awsElasticSearch())
	    return false;

	AWSElasticsearch client = getAWSClient();

	ListDomainNamesRequest listRequest = new ListDomainNamesRequest();

	ListDomainNamesResult response = doRequestWithRetry(client, listRequest);

	AWSESUrlParser parser = new AWSESUrlParser(getEsStorageUri().getUri());

	Optional<DomainInfo> found = response.getDomainNames().stream()
		.filter(domainInfo -> domainInfo.getDomainName().equals(parser.getDomainName())).findFirst();

	if (!found.isPresent()) {
	    logger.warn("The provided url is aws, but the domain identification failed. Url is: {} Expected domain was {}",
		    getEsStorageUri().getUri(), parser.getDomainName());

	    return false;
	}

	return true;
    }

    private ListDomainNamesResult doRequestWithRetry(AWSElasticsearch client, ListDomainNamesRequest listRequest) {

	long wait = 1000l;

	while (true) {

	    try {

		return client.listDomainNames(listRequest);

	    } catch (Exception e) {

		String msg = e.getMessage();

		if (msg.contains("ThrottlingException") && wait < AWSESConstants.MAX_BACKOFF_TIMEOUT_MS) {
		    wait = wait * 2; // backoff wait
		    String info = "AWS ES rate exceded exception identified during AWSESConnector TEST. Trying to resend with backoff wait in " + wait + "ms";
		    GSLoggerFactory.getLogger(getClass()).info(info);
		    try {
			Thread.sleep(wait);
		    } catch (InterruptedException e1) {
			GSLoggerFactory.getLogger(getClass()).warn(e1.getMessage());
			Thread.currentThread().interrupt();
		    }
		} else {
		    throw e;
		}
	    }

	}

    }

    @Override
    protected ESRequestSubmitter getSubmitter() {

	AWSESRequestSubmitter submitter = new AWSESRequestSubmitter();

	submitter.setPwd(getEsStorageUri().getPassword());

	submitter.setUser(getEsStorageUri().getUser());

	submitter.setRegion(new AWSESUrlParser(getESUrl()).getRegion());

	return submitter;
    }

    boolean awsElasticSearch() {

	return new AWSESUrlParser(getEsStorageUri().getUri()).isAWSESEndpoint();
    }

    String getESUrl() {
	return getEsStorageUri().getUri();
    }

}
