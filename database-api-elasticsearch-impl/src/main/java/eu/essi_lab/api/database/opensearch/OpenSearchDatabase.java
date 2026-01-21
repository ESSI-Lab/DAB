/**
 *
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.configuration.ExecutionMode;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import com.fasterxml.jackson.core.StreamReadConstraints;

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

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JavaOptions;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabase extends Database {

    public static boolean debugQueries = false;


    /**
     *
     */
    public static final Integer MAX_RESULT_WINDOW_SIZE = 10000;

    static {

	debugQueries = JavaOptions.isEnabled(JavaOptions.DEBUG_OPENSEARCH_QUERIES);

	//
	// set the Jackson StreamReadConstraints maxStringLength to 50 MB instead of the
	// default 20 MB
	//
	StreamReadConstraints.overrideDefaultStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(50000000).build());
    }

    private OpenSearchClient client;
    private String identifier;
    private DatabaseSetting setting;

    private SdkHttpClient httpClient;
    private boolean initialized;
    private StorageInfo storageInfo;

    /**
     * @return
     */
    public static StorageInfo createLocalServiceInfo() {

	StorageInfo info = new StorageInfo();

	info.setUser("test");// ignored
	info.setPassword("test");// ignored

	// mandatory
	info.setType(OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol());

	// the identifier is set same as name in the db initialization
	info.setName("test");
	info.setUri("http://localhost:9200");

	return info;
    }

    /**
     * @return
     * @throws GSException
     */
    public static OpenSearchDatabase createLocalService() throws GSException {

	StorageInfo info = createLocalServiceInfo();

	OpenSearchDatabase database = new OpenSearchDatabase();

	database.initialize(info);

	return database;
    }

    @Override
    public void initialize(StorageInfo storageInfo) throws GSException {

	this.storageInfo = storageInfo;
	if (!initialized) {

	    // both ignored for local protocol
	    System.setProperty("aws.accessKeyId", storageInfo.getUser());
	    System.setProperty("aws.secretAccessKey", storageInfo.getPassword());

	    //
	    // if missing, the identifier is same as name
	    // the identifier is missing when the storage info comes from the
	    // configuration, where the identifier field is missing
	    //
	    identifier = storageInfo.getIdentifier() != null ? storageInfo.getIdentifier() : storageInfo.getName();

	    AwsSdk2TransportOptions awsSdk2TransportOptions = AwsSdk2TransportOptions.builder().//

		    build();

	    httpClient = ApacheHttpClient.builder().build();

	    OpenSearchServiceType serviceType = OpenSearchServiceType.decode(storageInfo.getType().get());

	    String serviceName = serviceType.getServiceName();

	    HttpHost httpHost = HttpHost.create(storageInfo.getUri());

	    String schemeName = httpHost.getSchemeName();

	    if (schemeName.equals("http") || serviceType == OpenSearchServiceType.OPEN_SEARCH_LOCAL) {

		client = createClient(storageInfo);

	    } else {

		AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(//
			httpClient, //
			httpHost.getHostName(), //
			serviceName, //
			Region.US_EAST_1, //
			awsSdk2TransportOptions);

		client = new OpenSearchClient(awsSdk2Transport);
	    }

	    switch (ExecutionMode.get()) {
	    case CONFIGURATION:
	    case LOCAL_PRODUCTION:
	    case MIXED:

		//
		//
		//

		if (JavaOptions.isEnabled(JavaOptions.INIT_OPENSEARCH_INDEXES)) {

		    IndexMapping.initializeIndexes(client);
		}

		//
		//
		//

		if (JavaOptions.isEnabled(JavaOptions.UPDATE_DATA_FOLDER_INDEX)) {

		    DataFolderMapping mapping = DataFolderMapping.get();

		    try {

			mapping.checkAndUpdate(client);

		    } catch (IOException e) {

			throw GSException.createException(getClass(), "OpenSearchDataFolderIndexUpdatingError", e);
		    }
		}
	    }

	    initialized = true;
	}
    }

    /**
     * @param storageInfo
     * @return
     * @throws URISyntaxException
     */
    public static OpenSearchClient createClient(StorageInfo storageInfo) throws GSException {

	URI uri;
	try {
	    uri = new URI(storageInfo.getUri());

	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(e);
	    throw GSException.createException(OpenSearchDatabase.class, e.getMessage(), e);
	}

	HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

	RestClientBuilder builder = RestClient.builder(httpHost);

	if (storageInfo.getUser() != null && !storageInfo.getUser().isEmpty() && //
		storageInfo.getPassword() != null && !storageInfo.getPassword().isEmpty()) {

	    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	    credentialsProvider.setCredentials(AuthScope.ANY,
		    new UsernamePasswordCredentials(storageInfo.getUser(), storageInfo.getPassword()));

	    builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

	}

	if (storageInfo.getPath().isPresent()) {

	    builder.setPathPrefix(storageInfo.getPath().get());
	}

	RestClient restClient = builder.build();

	OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

	return new OpenSearchClient(transport);
    }

    @Override
    public DatabaseFolder getFolder(String folderName) throws GSException {

	if (existsFolder(folderName)) {

	    return new OpenSearchFolder(this, folderName);
	}

	return null;
    }

    @Override
    public Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException {

	OpenSearchFolder folder = new OpenSearchFolder(this, folderName);

	if (existsFolder(folderName)) {

	    return Optional.of(folder);
	}

	if (!createIfNotExist) {

	    return Optional.empty();
	}

	boolean created = addFolder(folderName);

	if (created) {

	    return Optional.ofNullable(folder);
	}

	return Optional.empty();
    }

    @Override
    public boolean existsFolder(String folderName) throws GSException {

	try {

	    return FolderRegistry.get(this).isRegistered(new OpenSearchFolder(this, folderName));

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(OpenSearchDatabase.class, "OpenSearchDatabaseExistsFolderError", ex);
	}
    }

    @Override
    public DatabaseFolder[] getFolders() throws GSException {

	try {

	    List<OpenSearchFolder> folders = FolderRegistry.get(this).getRegisteredFolders();

	    return folders.toArray(new OpenSearchFolder[] {});

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(OpenSearchDatabase.class, "OpenSearchDatabaseGetFoldersError", ex);
	}
    }

    @Override
    public boolean removeFolder(String folderName) throws GSException {

	try {

	    OpenSearchFolder folder = new OpenSearchFolder(this, folderName);
	    folder.clear();

	    return FolderRegistry.get(this).deregister(folder);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(OpenSearchDatabase.class, "OpenSearchDatabaseRemoceFolderError", ex);
	}
    }

    @Override
    public boolean addFolder(String folderName) throws GSException {

	if (!existsFolder(folderName)) {

	    OpenSearchFolder folder = new OpenSearchFolder(this, folderName);

	    try {
		return FolderRegistry.get(this).register(folder);

	    } catch (IOException ex) {

		GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

		throw GSException.createException(getClass(), "OpenSearchDatabaseAddFolderError", ex);
	    }
	}

	return false;
    }

    //
    // NOT IMPLEMENTED AT THE MOMENT. Used in deprecated SourceStorageWorker
    // testISOCompliance, recoverTags and
    // testISCompliance methods and also in markDeletedRecords (which could be used)
    //
    @Override
    public List<String> getIdentifiers(IdentifierType type, String folderName, boolean excludDeleted) throws GSException {

	throw new NotImplementedException();
    }

    @Override
    public StorageInfo getStorageInfo() {

	return storageInfo;
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

    /**
     * @param info
     * @return
     */
    public static boolean isSupported(StorageInfo info) {

	return info.getType().isPresent() && //
		OpenSearchServiceType.protocols().contains(info.getType().get());
    }

    @Override
    public boolean supports(StorageInfo info) {

	return isSupported(info);
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

	return DatabaseImpl.OPENSEARCH.getName();
    }

    /**
     * @return
     */
    public OpenSearchClient getClient() {

	return client;
    }

    @Override
    public DatabaseImpl getImplementation() {

	return DatabaseImpl.OPENSEARCH;
    }
}
