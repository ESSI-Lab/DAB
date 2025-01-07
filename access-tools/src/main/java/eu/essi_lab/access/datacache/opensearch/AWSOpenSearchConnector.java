package eu.essi_lab.access.datacache.opensearch;

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

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;

public class AWSOpenSearchConnector extends OpenSearchConnector {

    @Override
    public boolean supports(DataConnectorType type) {
	switch (type) {
	case OPEN_SEARCH_AWS_1_3:
	    return true;
	default:
	    return false;
	}
    }

    protected RestHighLevelClient createClient(URL endpoint, String username, String password) {
	AWS4Signer signer = new AWS4Signer();
	signer.setServiceName("es");
	signer.setRegionName("us-east-1");
	AWSCredentials credentials = new BasicAWSCredentials(username, password);
	AWSCredentialsProvider credentialsProvier = new AWSStaticCredentialsProvider(credentials);
	HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor("es", signer, credentialsProvier);
	return new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint.toString()))
		.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }
}
