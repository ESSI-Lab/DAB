/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.endpoints.BooleanResponse;

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

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabase extends Database {

    private OpenSearchClient client;
    private String identifier;
    private DatabaseSetting setting;

    public static void main(String[] args) throws OpenSearchException, IOException {

	AwsSdk2TransportOptions awsSdk2TransportOptions = AwsSdk2TransportOptions.builder().//
		build();

	SdkHttpClient httpClient = ApacheHttpClient.builder().build();

	AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(//
		httpClient, //
		"w6iz16h1nis29el1etjd.us-east-1.aoss.amazonaws.com", //
		"aoss", // Amazon OpenSearch Serverless
		Region.US_EAST_1, // signing service region
		awsSdk2TransportOptions);

	OpenSearchClient client = new OpenSearchClient(awsSdk2Transport);

	// InfoResponse info = client.info();

	// create the index
	ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index("movies-index").build();

	BooleanResponse exists = client.indices().exists(existsIndexRequest);

	System.out.println(exists);

	// String index = "movies";
	//
	// CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(index).build();
	//
	// try {
	// client.indices().create(createIndexRequest);
	//
	// // add settings to the index
	// IndexSettings indexSettings = new IndexSettings.Builder().build();
	// PutIndicesSettingsRequest putSettingsRequest = new
	// PutIndicesSettingsRequest.Builder().index(index).settings(indexSettings)
	// .build();
	//
	// client.indices().putSettings(putSettingsRequest);
	//
	// System.out.println(client.indices());
	//
	// } catch (OpenSearchException ex) {
	// final String errorType = Objects.requireNonNull(ex.response().error().type());
	// if (!errorType.equals("resource_already_exists_exception")) {
	// throw ex;
	// }
	// }

	//
	//
	//

	httpClient.close();
    }

    @Override
    public void initialize(StorageInfo storageInfo) throws GSException {

	System.setProperty("aws.accessKeyId", storageInfo.getUser());
	System.setProperty("aws.secretAccessKey", storageInfo.getPassword());

	identifier = storageInfo.getIdentifier() == null ? UUID.randomUUID().toString() : storageInfo.getIdentifier();

	AwsSdk2TransportOptions awsSdk2TransportOptions = AwsSdk2TransportOptions.builder().//
		build();

	SdkHttpClient httpClient = ApacheHttpClient.builder().build();

	String serviceName = storageInfo.getType().get().equals("OpenSearch") ? "es" : "aoss";

	AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(//
		httpClient, //
		HttpHost.create(storageInfo.getUri()).getHostName(), //
		serviceName, //
		Region.US_EAST_1, //
		awsSdk2TransportOptions);

	client = new OpenSearchClient(awsSdk2Transport);

	//
	//
	//
	 
	checkIndex("resources-index");

	checkIndex("users-index");

	checkIndex("views-index");
	
    }
    
    /**
     * 
     * @param indexName
     * @return
     * @throws IOException 
     * @throws OpenSearchException 
     */
    private boolean checkIndex(String indexName) throws OpenSearchException, IOException {
	
	ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(indexName).build();

	return client.indices().exists(existsIndexRequest).value();
    }

    @Override
    public SourceStorageWorker getWorker(String sourceId) throws GSException {

	return null;
    }

    @Override
    public DatabaseFolder getFolder(String folderName) throws GSException {

	return null;
    }

    @Override
    public Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException {

	return Optional.empty();
    }

    @Override
    public boolean existsFolder(String folderName) throws GSException {

	return false;
    }

    @Override
    public DatabaseFolder[] getFolders() throws GSException {

	return null;
    }

    @Override
    public boolean removeFolder(String folderName) throws GSException {

	return false;
    }

    @Override
    public boolean addFolder(String folderName) throws GSException {

	return false;
    }

    @Override
    public DatabaseFolder findWritingFolder(SourceStorageWorker worker) throws GSException {

	return null;
    }

    @Override
    public List<String> getIdentifiers(IdentifierType type, String folderName, boolean excludDeleted) throws GSException {

	return null;
    }

    @Override
    public StorageInfo getStorageInfo() {

	return null;
    }

    @Override
    public void release() throws GSException {

    }

    @Override
    public String getIdentifier() {

	return identifier;
    }

    @Override
    public boolean supports(StorageInfo dbInfo) {

	return dbInfo.getType().isPresent()
		&& (dbInfo.getType().get().equals("OpenSearch") || dbInfo.getType().get().equals("OpenSearchServerless"));
    }

    @Override
    public void configure(DatabaseSetting setting) {

	this.setting = setting;
    }

    @Override
    public DatabaseSetting getSetting() {

	return this.setting;
    }

    @Override
    public String getType() {

	return "OpenSearch";
    }
}
