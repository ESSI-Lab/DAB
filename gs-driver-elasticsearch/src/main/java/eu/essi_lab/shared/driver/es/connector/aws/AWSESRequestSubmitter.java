package eu.essi_lab.shared.driver.es.connector.aws;

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

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import eu.essi_lab.shared.driver.es.connector.ESRequestSubmitter;

/**
 * @author ilsanto
 */
public class AWSESRequestSubmitter extends ESRequestSubmitter {

    private static final String ES_SERVICE_NAME = "es";
    private String region;

    @Override
    protected HttpClient authenticatedClient() {

	AWS4Signer signer = new AWS4Signer();
	signer.setServiceName(ES_SERVICE_NAME);
	signer.setRegionName(region);

	BasicAWSCredentials credentials = new BasicAWSCredentials(getUser(), getPwd());

	HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(ES_SERVICE_NAME, signer,
		new AWSStaticCredentialsProvider(credentials));

	return HttpClientBuilder.create().addInterceptorLast(interceptor).build();

    }

    public void setRegion(String region) {
	this.region = region;
    }
}
