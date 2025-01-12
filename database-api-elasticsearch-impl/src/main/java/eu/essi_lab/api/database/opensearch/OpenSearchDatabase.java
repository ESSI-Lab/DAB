/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.rest_client.RestClientTransport;

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
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabase extends Database {

    /**
     * @author Fabrizio
     */
    public enum OpenSearchServiceType {

	OPEN_SEARCH_MANAGED("es"),

	OPEN_SEARCH_SERVERLESS("aoss");

	private String type;

	/**
	 * 
	 */
	private OpenSearchServiceType(String type) {

	    this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {

	    return type;
	}

	/**
	 * @param type
	 * @return
	 */
	public static OpenSearchServiceType decode(String type) {

	    return type.equals(OPEN_SEARCH_MANAGED.getType()) ? OPEN_SEARCH_MANAGED : OPEN_SEARCH_SERVERLESS;
	}

	/**
	 * S@return
	 */
	public static List<String> types() {

	    return Arrays.asList(OpenSearchServiceType.values()).stream().map(t -> t.getType()).collect(Collectors.toList());
	}
    }

    private OpenSearchClient client;
    private String identifier;
    private DatabaseSetting setting;

    private SdkHttpClient httpClient;
    private boolean initialized;

    @Override
    public void initialize(StorageInfo storageInfo) throws GSException {

	if (!initialized) {

	    System.setProperty("aws.accessKeyId", storageInfo.getUser());
	    System.setProperty("aws.secretAccessKey", storageInfo.getPassword());

	    identifier = storageInfo.getIdentifier() == null ? UUID.randomUUID().toString() : storageInfo.getIdentifier();

	    AwsSdk2TransportOptions awsSdk2TransportOptions = AwsSdk2TransportOptions.builder().//

		    build();

	    httpClient = ApacheHttpClient.builder().build();

	    String serviceName = OpenSearchServiceType.decode(storageInfo.getType().get()).getType();

	    HttpHost httpHost = HttpHost.create(storageInfo.getUri());

	    String schemeName = httpHost.getSchemeName();

	    if (schemeName.equals("http")) {

		client = createNoSSLContextClient(storageInfo);

	    } else {

		AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(//
			httpClient, //
			httpHost.getHostName(), //
			serviceName, //
			Region.US_EAST_1, //
			awsSdk2TransportOptions);

		client = new OpenSearchClient(awsSdk2Transport);
	    }

	    //
	    //
	    //

	    initializeIndexes();

	    initialized = true;
	}
    }

    /**
     * @throws GSException
     */
    public void initializeIndexes() throws GSException {

	for (IndexMapping mapping : IndexMapping.MAPPINGS) {

	    boolean exists = checkIndex(mapping.getIndex());

	    if (!exists) {

		createIndex(mapping);
	    }
	}
    }

    /**
     * @param storageInfo
     * @return
     */
    public static OpenSearchClient createNoSSLContextClient(StorageInfo storageInfo) {

	HttpHost httpHost = HttpHost.create(storageInfo.getUri());

	RestClient restClient = RestClient.builder(httpHost).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
	    @Override
	    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {

		return httpClientBuilder.setSSLContext(null);
	    }
	}).build();

	OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

	return new OpenSearchClient(transport);
    }

    /**
     * @throws GSException
     */
    private void createIndex(IndexMapping mapping) throws GSException {

	TypeMapping typeMapping = new TypeMapping.Builder().//
		withJson(mapping.getMappingStream()).//
		build();

	CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().//
		index(mapping.getIndex()).//
		mappings(typeMapping).//
		build();

	try {

	    CreateIndexResponse response = client.indices().create(createIndexRequest);

	    if (!response.acknowledged()) {

		throw GSException.createException(//
			getClass(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_FATAL, //
			"OpenSearchDatabaseCreate" + mapping.getIndex() + "NotAcknowledgedError");
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchDatabaseCreate" + mapping.getIndex() + "Error", ex);
	}
    }

    /**
     * @param indexName
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    private boolean checkIndex(String indexName) throws GSException {

	ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(indexName).build();

	try {

	    return client.indices().exists(existsIndexRequest).value();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchDatabaseCheckIndexError", ex);
	}
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

	if (httpClient != null) {

	    httpClient.close();
	}
    }

    @Override
    public String getIdentifier() {

	return identifier;
    }

    @Override
    public boolean supports(StorageInfo dbInfo) {

	return dbInfo.getType().isPresent() && OpenSearchServiceType.types().contains(dbInfo.getType().get());
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

    /**
     * @return
     */
    public OpenSearchClient getClient() {

	return client;
    }

    /**
     * @param name
     * @return
     */
    private String normalizeName(String name) {

	if (!getIdentifier().equals("ROOT") && !name.contains(getIdentifier())) {
	    return "/" + getIdentifier() + "_" + name + "/";
	}

	return "/" + name + "/";
    }

    /**
     * @param name
     * @throws IllegalArgumentException
     */
    private void checkName(String name) throws IllegalArgumentException {

	if (name == null)
	    throw new IllegalArgumentException("Argument cannot be null");

	if (name.startsWith("/") || name.contains("\\") || name.endsWith("/")) {
	    throw new IllegalArgumentException("Argument cannot start with or end with slashes and it can not contain back slashes");
	}
    }

}
