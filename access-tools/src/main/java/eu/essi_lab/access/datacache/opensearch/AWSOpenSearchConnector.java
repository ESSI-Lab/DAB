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
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

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

    protected OpenSearchClient createClient(URL endpoint, String accessKey, String secretKey) {

	AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

	AwsSdk2TransportOptions awsSdk2TransportOptions = AwsSdk2TransportOptions.builder().setCredentials(credentialsProvider).//

		build();

	SdkHttpClient httpClient = ApacheHttpClient.builder().build();

	HttpHost httpHost = HttpHost.create(endpoint.toString());

	AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(//
		httpClient, //
		httpHost.getHostName(), //
		"es", //
		Region.US_EAST_1, //
		awsSdk2TransportOptions);

	OpenSearchClient ret = new OpenSearchClient(awsSdk2Transport);

	return ret;

    }
    // protected RestHighLevelClient createClient(URL endpoint, String username, String password) {
    // AWS4Signer signer = new AWS4Signer();
    // signer.setServiceName("es");
    // signer.setRegionName("us-east-1");
    // AWSCredentials credentials = new BasicAWSCredentials(username, password);
    // AWSCredentialsProvider credentialsProvier = new AWSStaticCredentialsProvider(credentials);
    // HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor("es", signer, credentialsProvier);
    // return new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint.toString()))
    // .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    // }

}
