package eu.essi_lab.api.database.factory;

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

import java.io.Serializable;
import java.util.ServiceLoader;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.configuration.DataBaseConfiguration;
import eu.essi_lab.api.database.configuration.InstantiableMetadataDBStorageURI;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
public class DatabaseConsumerFactory implements Serializable {

    public DatabaseConsumerFactory() {
	//Nothing to init
    }

    /**
     * Loads the available {@link DatabaseReader}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseReader} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public DatabaseReader createDataBaseReader(StorageUri dbUri) throws GSException {

	GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Creating database reader for uri {}", dbUri.getUri());

	if (dbUri instanceof InstantiableMetadataDBStorageURI) {
	    GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("StorageURI is instantiable");
	    return createDataBaseReader((InstantiableMetadataDBStorageURI) dbUri);
	}

	GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("StorageURI is not instantiable");
	ServiceLoader<DatabaseReader> readers = ServiceLoader.load(DatabaseReader.class);

	for (DatabaseReader reader : readers) {

	    if (reader.supports(dbUri)) {

		DatabaseProvider provider = new DatabaseProviderFactory().create(dbUri);
		Database dataBase = provider.getDatabase();

		if (dataBase != null) {
		    reader.setDatabase(dataBase);
		    return reader;
		}
	    }
	}

	return null;
    }

    /**
     * Loads the available {@link DatabaseWriter}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseWriter} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public DatabaseWriter createDataBaseWriter(StorageUri dbUri) throws GSException {

	if (dbUri instanceof InstantiableMetadataDBStorageURI) {
	    return createDataBaseWriter((InstantiableMetadataDBStorageURI) dbUri);
	}

	ServiceLoader<DatabaseWriter> writers = ServiceLoader.load(DatabaseWriter.class);

	for (DatabaseWriter writer : writers) {

	    if (writer.supports(dbUri)) {

		DatabaseProvider provider = new DatabaseProviderFactory().create(dbUri);
		Database dataBase = provider.getDatabase();

		if (dataBase != null) {
		    writer.setDatabase(dataBase);
		    return writer;
		}
	    }
	}

	return null;
    }

    /**
     * Loads the available {@link SourceStorage}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link SourceStorage} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public SourceStorage createSourceStorage(StorageUri dbUri) throws GSException {

	if (dbUri instanceof InstantiableMetadataDBStorageURI) {
	    return createSourceStorage((InstantiableMetadataDBStorageURI) dbUri);
	}

	ServiceLoader<SourceStorage> storages = ServiceLoader.load(SourceStorage.class);

	for (SourceStorage storage : storages) {

	    if (storage.supports(dbUri)) {

		DatabaseProvider provider = new DatabaseProviderFactory().create(dbUri);
		Database dataBase = provider.getDatabase();

		if (dataBase != null) {
		    storage.setDatabase(dataBase);
		    return storage;
		}
	    }
	}

	return null;
    }

    /**
     * Loads a preconfigured {@link DatabaseReader}s using an {@link InstantiableMetadataDBStorageURI} object. This method must
     * be invoked at query time
     *
     * @param dbUri
     * @return
     * @throws GSException
     */
    public DatabaseReader createDataBaseReader(InstantiableMetadataDBStorageURI dbUri) throws GSException {

	DataBaseConfiguration dbConf = (DataBaseConfiguration) dbUri.getDataBaseConfiguraiton();

	return dbConf.getReader();
    }

    /**
     * Loads a preconfigured {@link DatabaseWriter}s using an {@link InstantiableMetadataDBStorageURI} object. This method must
     * be invoked at query time
     *
     * @param dbUri
     * @return
     * @throws GSException
     */
    public DatabaseWriter createDataBaseWriter(InstantiableMetadataDBStorageURI dbUri) throws GSException {

	DataBaseConfiguration dbConf = (DataBaseConfiguration) dbUri.getDataBaseConfiguraiton();

	return dbConf.getWriter();
    }

    /**
     * Loads a preconfigured {@link SourceStorage}s using an {@link InstantiableMetadataDBStorageURI} object. This method must
     * be invoked at query time
     *
     * @param dbUri
     * @return
     * @throws GSException
     */
    private SourceStorage createSourceStorage(InstantiableMetadataDBStorageURI dbUri) throws GSException {

	DataBaseConfiguration dbConf = (DataBaseConfiguration) dbUri.getDataBaseConfiguraiton();

	return dbConf.getSourceStorage();
    }
}
