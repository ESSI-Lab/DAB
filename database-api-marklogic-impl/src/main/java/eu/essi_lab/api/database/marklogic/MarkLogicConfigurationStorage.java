package eu.essi_lab.api.database.marklogic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.internal.Folder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class MarkLogicConfigurationStorage implements IGSConfigurationStorage {

    public static final String ERR_DB_INSTANTIATION = "ERR_DB_INSTANTIATION";
    private StorageUri storageURI;
    public static final String CONFIG_KEY = "config.json";
    private static final String WRITE_NULL_CONFIGURATION_ERR_ID = "WRITE_NULL_CONFIGURATION_ERR_ID";
    private static final String WRITE_CONFIGURATION_ERR_ID = "WRITE_CONFIGURATION_ERR_ID";

    @Override
    public void transactionUpdate(GSConfiguration conf) throws GSException {

	if (conf == null) {
	    throw GSException.createException(//
		    MarkLogicConfigurationStorage.class, //
		    "Can't write null configuration", //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WRITE_NULL_CONFIGURATION_ERR_ID);
	}

	try {

	    DatabaseProviderFactory factory = new DatabaseProviderFactory();
	    DatabaseProvider provider = factory.create(storageURI);

	    provider.initialize(storageURI, storageURI.getConfigFolder());

	    DatabaseConsumerFactory consumerFactory = new DatabaseConsumerFactory();
	    DatabaseWriter writer = consumerFactory.createDataBaseWriter(storageURI);

	    Database db = writer.getDatabase();
	    if (db instanceof MarkLogicDatabase) {
		MarkLogicDatabase marklogicDB = (MarkLogicDatabase) db;
		String folderName = storageURI.getConfigFolder();
		if (!marklogicDB.existsFolder(folderName)) {
		    marklogicDB.addFolder(folderName);
		}

		Folder folder = marklogicDB.getFolder(folderName);

		InputStream is = conf.serializeToInputStream();

		if (!folder.storeBinary(CONFIG_KEY, is) && !folder.replaceBinary(CONFIG_KEY, is)) {

		    throw GSException.createException(//
			    MarkLogicConfigurationStorage.class, //
			    "Can't write configuration to db", //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    WRITE_CONFIGURATION_ERR_ID);
		}

		return;
	    }

	    throw new ClassCastException("Not a MarkLogicDatabase: " + db.getClass().getSimpleName());

	} catch (Exception e) {

	    throw GSException.createException(//
		    MarkLogicConfigurationStorage.class, //
		    "Can not write configuration file: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ERR_DB_INSTANTIATION, //
		    e);

	}
    }

    @Override
    public GSConfiguration read() throws GSException {

	try {
	    DatabaseProviderFactory factory = new DatabaseProviderFactory();
	    DatabaseProvider provider = factory.create(storageURI);

	    provider.initialize(storageURI, storageURI.getConfigFolder());

	    DatabaseConsumerFactory consumerFactory = new DatabaseConsumerFactory();
	    DatabaseReader reader = consumerFactory.createDataBaseReader(storageURI);

	    Database db = reader.getDatabase();
	    if (db instanceof MarkLogicDatabase) {
		MarkLogicDatabase marklogicDB = (MarkLogicDatabase) db;
		String folderName = storageURI.getConfigFolder();
		if (!marklogicDB.existsFolder(folderName)) {
		    return null;
		}
		Folder folder = marklogicDB.getFolder(folderName);
		InputStream is = folder.getBinary(CONFIG_KEY);

		if (is == null) {
		    return null;
		}

		GSConfiguration deserialized = new Deserializer().deserialize(is, GSConfiguration.class);

		is.close();

		return deserialized;
	    }

	    throw new ClassCastException("Not a MarkLogicDatabase: " + db.getClass().getSimpleName());

	} catch (Exception e) {

	    throw GSException.createException(//
		    MarkLogicConfigurationStorage.class, //
		    "Can not read configuration file: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ERR_DB_INSTANTIATION, //
		    e);

	}
    }

    @Override
    public boolean supports(StorageUri dbUri) {

	if (dbUri != null && dbUri.getUri() != null && dbUri.getUri().startsWith("xdbc")) {
	    return true;
	}

	return false;
    }

    @Override
    public boolean validate(StorageUri storageURI) {

	try {
	    DatabaseProviderFactory factory = new DatabaseProviderFactory();
	    DatabaseProvider provider = factory.create(storageURI);

	    provider.initialize(storageURI, storageURI.getConfigFolder());

	    DatabaseConsumerFactory consumerFactory = new DatabaseConsumerFactory();
	    consumerFactory.createDataBaseReader(storageURI);
	    consumerFactory.createDataBaseWriter(storageURI);

	    return true;

	} catch (GSException ex) {
	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during MarkLogic initialization: " + ex.getMessage());
	    return false;
	}
    }

    @Override
    public void setStorageUri(StorageUri storageURI) {

	this.storageURI = storageURI;
    }

    @Override
    public StorageUri getStorageUri() {

	return this.storageURI;
    }
}
