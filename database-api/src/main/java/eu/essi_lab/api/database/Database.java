package eu.essi_lab.api.database;

import java.util.Optional;

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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public interface Database extends DatabaseCompliant, Configurable<DatabaseSetting> {

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
	EXIST_EMBEDDED("eXistEmbedded");

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
    public void initialize(StorageInfo storageInfo) throws GSException;

    /**
     * @param folderName
     * @param createIfNotExist
     * @return
     * @throws GSException
     */
    public Optional<DatabaseFolder> getFolder(String folderName, boolean createIfNotExist) throws GSException;

    /**
     * Return the checked {@link StorageInfo}
     * 
     * @return
     */
    public StorageInfo getStorageInfo();

    /**
     * A possible implementation can execute some code which release some resources.<br>
     * After this method calling, the {@link Database} provided by this provider is no longer usable
     * 
     * @throws GSException
     */
    public void release() throws GSException;

    /**
     * @return
     */
    public String getIdentifier();

}
