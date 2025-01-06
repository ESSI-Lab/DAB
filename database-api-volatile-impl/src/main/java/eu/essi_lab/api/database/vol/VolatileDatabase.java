/**
 * 
 */
package eu.essi_lab.api.database.vol;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class VolatileDatabase extends Database {

    private StorageInfo dbInfo;
    private String dbIdentifier;
    private DatabaseSetting setting;
    private List<GSResource> resourcesList;
    private List<GSUser> usersList;
    private List<View> viewsList;
    private List<VolatileFolder> foldersList;
    private HashMap<String, Document> documentsMap;
    private HashMap<GSSource, HarvestingProperties> harvPropMap;
    private boolean initialized;

    public VolatileDatabase() {

	resourcesList = new ArrayList<GSResource>();
	usersList = new ArrayList<GSUser>();

	viewsList = new ArrayList<View>();
	foldersList = new ArrayList<VolatileFolder>();
	documentsMap = new HashMap<>();
	harvPropMap = new HashMap<>();
    }

    /**
     * @return
     */
    public List<GSResource> getResourcesList() {

	synchronized (resourcesList) {

	    return resourcesList;
	}
    }

    /**
     * @return
     */
    public List<GSUser> getUsersList() {

	synchronized (usersList) {

	    return usersList;
	}
    }

    /**
     * @return
     */
    public List<View> getViewsList() {

	synchronized (viewsList) {

	    return viewsList;
	}
    }

    /**
     * @return
     */
    public List<VolatileFolder> getFodersList() {

	synchronized (foldersList) {

	    return foldersList;
	}
    }

    /**
     * 
     */
    public void removeFolders() {

	synchronized (getFodersList()) {

	    this.foldersList = new ArrayList<>();
	}
    }

    /**
     * @return
     */
    public HashMap<String, Document> getDocumentsMap() {

	synchronized (documentsMap) {

	    return documentsMap;
	}
    }

    /**
     * @return
     */
    public HashMap<GSSource, HarvestingProperties> getHarvesingPropertiesMap() {

	synchronized (harvPropMap) {

	    return harvPropMap;
	}
    }

    @Override
    public boolean supports(StorageInfo dbInfo) {

	this.dbInfo = dbInfo;
	this.dbIdentifier = dbInfo.getIdentifier();

	return dbInfo.getName().equals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
    }

    @Override
    public StorageInfo getStorageInfo() {

	return this.dbInfo;
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

	return "VolatileDatabase";
    }

    @Override
    public void initialize(StorageInfo dbUri) throws GSException {

	if (!initialized) {

	    this.dbInfo = dbUri;
	    this.dbIdentifier = dbUri.getIdentifier();
	    this.initialized = true;
	}
    }

    @Override
    public void release() throws GSException {

    }

    /**
     * 
     */
    public void clear() {

	resourcesList = new ArrayList<GSResource>();
	usersList = new ArrayList<GSUser>();
	viewsList = new ArrayList<View>();
	foldersList = new ArrayList<VolatileFolder>();
	documentsMap = new HashMap<>();
	harvPropMap = new HashMap<>();
    }

    /**
     * @return
     */
    public String getIdentifier() {

	return dbIdentifier;
    }

    @Override
    public Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException {

	Optional<VolatileFolder> opt = getFodersList().stream().filter(f -> f.getSimpleName().equals(folderName)).findFirst();

	if (opt.isPresent()) {
	    return Optional.of(opt.get());
	}

	if (createIfNotExist) {

	    VolatileFolder folder = new VolatileFolder(folderName);

	    getFodersList().add(folder);

	    return Optional.of(folder);
	}

	return Optional.empty();
    }

    @Override
    public SourceStorageWorker getWorker(String sourceId) throws GSException {
	//
	return null;
    }

    @Override
    public DatabaseFolder getFolder(String folderName) throws GSException {
	//
	return null;
    }

    @Override
    public boolean existsFolder(String folderName) throws GSException {
	//
	return false;
    }

    @Override
    public DatabaseFolder[] getFolders() throws GSException {
	//
	return null;
    }

    @Override
    public boolean removeFolder(String folderName) throws GSException {
	//
	return false;
    }

    @Override
    public boolean addFolder(String folderName) throws GSException {
	//
	return false;
    }

    @Override
    public DatabaseFolder findWritingFolder(SourceStorageWorker worker) throws GSException {
	//
	return null;
    }

    @Override
    public List<String> getIdentifiers(IdentifierType type, String folderName, boolean excludDeleted) throws GSException {
	//
	return null;
    }
}
