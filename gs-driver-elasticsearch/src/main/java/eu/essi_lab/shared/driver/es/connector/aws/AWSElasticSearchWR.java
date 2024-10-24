package eu.essi_lab.shared.driver.es.connector.aws;

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

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author ilsanto
 */
public class AWSElasticSearchWR {

    private Logger logger = GSLoggerFactory.getLogger(AWSElasticSearchWR.class);
    private AWSCredentials awsCreds;

    public void write(String esURL, String index, InputStream stream) {



	Request<?> request = new DefaultRequest<Void>("es");
	request.setContent(stream);
	URI finalURL = URI.create(esURL + index + "/test/" + UUID.randomUUID().toString());

	logger.trace("Final url to write {}", finalURL);
	request.setEndpoint(finalURL);
	request.setHttpMethod(HttpMethodName.PUT);

	AWS4Signer signer = new AWS4Signer();
	signer.setRegionName("us-east-1");
	signer.setServiceName(request.getServiceName());
	signer.sign(request, awsCreds);

	AmazonHttpClient client = new AmazonHttpClient(new ClientConfiguration());

	client.execute(request, new DummyHandler<>(new AmazonWebServiceResponse<Void>()),
		new DummyHandler<>(new AmazonServiceException("Colud not write to index " + index)), new ExecutionContext(true));

    }

    public void setAwsCreds(AWSCredentials awsCreds) {
	this.awsCreds = awsCreds;
    }
}
