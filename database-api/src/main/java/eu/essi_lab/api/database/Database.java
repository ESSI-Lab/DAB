package eu.essi_lab.api.database;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
