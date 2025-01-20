package eu.essi_lab.api.database;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.marklogic.xcc.exceptions.RequestException;

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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class Database implements DatabaseCompliant, Configurable<DatabaseSetting> {

    /**
     * @author Fabrizio
     */
    public enum IdentifierType {

	/**
	 * 
	 */
	PUBLIC,
	/**
	 * 
	 */
	PRIVATE,
	/**
	 * 
	 */
	ORIGINAL,
	/**
	 * 
	 */
	OAI_HEADER
    }

    /**
     * @author Fabrizio
     */
    public enum DatabaseImpl {

	/**
	 * 
	 */
	MARK_LOGIC("MarkLogic"),
	/**
	 * 
	 */
	OPENSEARCH("OpenSearch");

	private String name;

	private DatabaseImpl(String name) {

	    this.name = name;
	}

	public String getName() {

	    return name;
	}

	public String toString() {

	    return getName();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum OpenSearchServiceType {

	/**
	 * es -> internally used to create the AwsSdk2Transport
	 * osm -> (OpenSearch Managed) protocol used as database type in the {@link StorageInfo} and
	 * in the GSService VM argument "configuration.url". E.g: osm://localhost/prod/prodConfig
	 */
	OPEN_SEARCH_MANAGED("es", "osm"),

	/**
	 * es -> internally used to create the AwsSdk2Transport
	 * osm -> (OpenSearch Serverless) protocol used as database type in the {@link StorageInfo} and
	 * in the GSService VM argument "configuration.url". E.g: oss://localhost/preprod/preprodConfig
	 */
	OPEN_SEARCH_SERVERLESS("aoss", "oss");

	private String protocol;
	private String serviceName;

	/**
	 * @param serviceName
	 * @param protocol
	 */
	private OpenSearchServiceType(String serviceName, String protocol) {

	    this.serviceName = serviceName;
	    this.protocol = protocol;
	}

	/**
	 * @return
	 */
	public String getProtocol() {

	    return protocol;
	}

	/**
	 * @return
	 */
	public String getServiceName() {

	    return serviceName;
	}

	/**
	 * @param protocol
	 * @return
	 */
	public static OpenSearchServiceType decode(String protocol) {

	    return protocol.equals(OPEN_SEARCH_MANAGED.getProtocol()) ? OPEN_SEARCH_MANAGED : OPEN_SEARCH_SERVERLESS;
	}

	/**
	 * @return
	 */
	public static List<String> protocols() {

	    return Arrays.asList(OpenSearchServiceType.values()).//
		    stream().//
		    map(p -> p.getProtocol()).//
		    collect(Collectors.toList());
	}
    }

    /**
     * 
     */
    public static final String USERS_FOLDER = "users";
    /**
     * 
     */
    public static final String VIEWS_FOLDER = "views";
    /*
     * 
     */
    public static final String AUGMENTERS_FOLDER = "augmenters";
    /*
     * 
     */
    public static final String CONFIGURATION_FOLDER = "configuration";
    /*
     * 
     */
    public static final String CACHE_FOLDER = "cache";

    /**
     * @param startupUri
     * @return
     */
    public static DatabaseImpl getImpl(String startupUri) {

	if (startupUri.startsWith("xdbc")) {

	    return DatabaseImpl.MARK_LOGIC;
	}

	if (startupUri.startsWith(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol())
		|| startupUri.startsWith(OpenSearchServiceType.OPEN_SEARCH_SERVERLESS.getProtocol())) {

	    return DatabaseImpl.OPENSEARCH;
	}

	return null;
    }

    /**
     * @param uri
     * @return
     */
    public static boolean isStartupUri(String uri) {

	return uri.startsWith("xdbc") || //
		uri.startsWith(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol())
		|| uri.startsWith(OpenSearchServiceType.OPEN_SEARCH_SERVERLESS.getProtocol());
    }

    /**
     * E.g: "xdbc://user:password@hostname:8000,8004/dbName/folder/"
     * E.g: "osm://productionhost/prod/prodConfig"
     * E.g: "oss://productionhost/prod/prodConfig"
     *
     * @param startupUri
     * @return
     * @throws URISyntaxException
     */
    public static StorageInfo getInfo(String startupUri) throws URISyntaxException {

	StorageInfo storageInfo = new StorageInfo();

	if (startupUri.startsWith("xdbc")) {

	    String xdbc = startupUri.replace("xdbc://", "xdbc_");

	    // uri --> xdbc://hostname:8000,8004
	    String uri = "xdbc://" + xdbc.substring(xdbc.indexOf("@") + 1, xdbc.indexOf("/"));
	    String user = xdbc.substring(xdbc.indexOf("_") + 1, xdbc.indexOf(":"));
	    String password = xdbc.substring(xdbc.indexOf(":") + 1, xdbc.indexOf("@"));

	    xdbc = xdbc.substring(xdbc.indexOf("/") + 1); // dnName/defaultConf/
	    String dbName = xdbc.substring(0, xdbc.indexOf("/"));
	    String folder = xdbc.substring(xdbc.indexOf("/") + 1, xdbc.lastIndexOf("/"));

	    storageInfo.setUri(uri);
	    storageInfo.setUser(user);
	    storageInfo.setPassword(password);
	    storageInfo.setIdentifier(folder);
	    storageInfo.setName(dbName);

	} else {

	    URI uri = new URI(startupUri);

	    String env = uri.getPath().split("/+")[1];
	    String configName = uri.getPath().split("/+")[2];

	    OpenSearchServiceType serviceType = OpenSearchServiceType.decode(uri.getScheme());

	    storageInfo = new StorageInfo();
	    storageInfo.setIdentifier(env);
	    storageInfo.setName(Database.CONFIGURATION_FOLDER);
	    storageInfo.setType(serviceType.getProtocol());
	    storageInfo.setUser(configName); // config name encoded ad user name
	    storageInfo.setUri("https://" + uri.getAuthority());
	}

	return storageInfo;
    }

    /**
     * Initializes a data base instance with the given <code>storageInfo</code>
     *
     * @param storageInfo
     * @throws GSException if the initialization fails
     */
    public abstract void initialize(StorageInfo storageInfo) throws GSException;

    /**
     * @param sourceId
     * @return
     * @throws GSException
     */
    public abstract SourceStorageWorker getWorker(String sourceId) throws GSException;

    /**
     * @param folderName
     * @return
     */
    public abstract DatabaseFolder getFolder(String folderName) throws GSException;

    /**
     * @param folderName
     * @param createIfNotExist
     * @return
     * @throws GSException
     */
    public abstract Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException;

    /**
     * @param folderName
     * @return
     */
    public abstract boolean existsFolder(String folderName) throws GSException;

    /**
     * @return
     */
    public abstract DatabaseFolder[] getFolders() throws GSException;

    /**
     * @param folderName
     */
    public abstract boolean removeFolder(String folderName) throws GSException;

    /**
     * @param folderName
     */
    public abstract boolean addFolder(String folderName) throws GSException;

    /**
     * The folder tagged ad writing folder exists only during harvesting.
     * in this case no harvesting is in progress, so we need to find the
     * current data folder with max. 2 attempts
     * If both folder exist or none, there is some kind of issue and an exception is thrown
     * since the resource can not be stored/updated.
     * See GIP-288
     * 
     * @param worker
     * @return
     * @throws GSException
     * @throws RequestException
     */
    public abstract DatabaseFolder findWritingFolder(SourceStorageWorker worker) throws GSException;

    /**
     * @param type
     * @param folderName
     * @param excludDeleted
     * @return
     * @throws GSException
     */
    public abstract List<String> getIdentifiers(IdentifierType type, String folderName, boolean excludDeleted) throws GSException;

    /**
     * Return the checked {@link StorageInfo}
     * 
     * @return
     */
    public abstract StorageInfo getStorageInfo();

    /**
     * A possible implementation can execute some code which release some resources.<br>
     * After this method calling, the {@link Database} provided by this provider is no longer usable
     * 
     * @throws GSException
     */
    public abstract void release() throws GSException;

    /**
     * @return
     */
    public abstract String getIdentifier();

    /**
     * @return
     * @throws GSException
     */
    public DatabaseFolder getViewFolder(boolean createIfNotExist) throws GSException {

	return getProtectedFolder(VIEWS_FOLDER, createIfNotExist);
    }

    /**
     * @return
     * @throws GSException
     */
    public DatabaseFolder getUsersFolder() throws GSException {

	return getProtectedFolder(USERS_FOLDER, true);
    }

    /**
     * @return
     * @throws GSException
     */
    public DatabaseFolder getAugmentersFolder() throws GSException {

	return getProtectedFolder(AUGMENTERS_FOLDER, true);
    }

    /**
     * @param dirURI
     * @return
     * @throws GSException
     */
    protected DatabaseFolder getProtectedFolder(String dirURI, boolean createIfNotExist) throws GSException {

	return getFolder(dirURI, createIfNotExist).orElse(null);
    }

}
