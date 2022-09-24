package eu.essi_lab.api.database.factory;

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

import java.util.ServiceLoader;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class DatabaseConsumerFactory {

    private static final String DB_READER_CREATION_FAILED_DB_NULL_EXCEPTION = "DB_READER_CREATION_FAILED_DB_NULL_EXCEPTION";
    private static final String DB_READER_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION = "DB_READER_CREATION_FAILED_PROVIDER_NOT_FOUND";

    private static final String DB_WRITER_CREATION_FAILED_DB_NULL_EXCEPTION = "DB_WRITER_CREATION_FAILED_DB_NULL_EXCEPTION";
    private static final String DB_WRITER_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION = "DB_WRITER_CREATION_FAILED_PROVIDER_NOT_FOUND";

    private static final String SOURCE_STORAGE_CREATION_FAILED_DB_NULL_EXCEPTION = "SOURCE_STORAGE_CREATION_FAILED_DB_NULL_EXCEPTION";
    private static final String SOURCE_STORAGE_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION = "SOURCE_STORAGE_CREATION_FAILED_PROVIDER_NOT_FOUND";

    private DatabaseConsumerFactory() {
    }

    /**
     * Loads the available {@link DatabaseReader}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseReader} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    @SuppressWarnings("rawtypes")
    public static DatabaseReader createDataBaseReader(StorageUri dbUri) throws GSException {

	synchronized (DatabaseProviderFactory.PROVIDER_LOCK) {

	    ServiceLoader<DatabaseReader> readers = ServiceLoader.load(DatabaseReader.class);

	    for (DatabaseReader reader : readers) {

		if (reader.supports(dbUri)) {

		    DatabaseProvider provider = DatabaseProviderFactory.create(dbUri);
		    Database dataBase = provider.getDatabase();

		    if (dataBase == null) {

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class)
				.debug("Initialization of database reader for uri {} STARTED", dbUri.getUri());

			provider.initialize(dbUri, dbUri.getConfigFolder());

			dataBase = provider.getDatabase();

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Initialization of database reader for uri {} ENDED",
				dbUri.getUri());

		    }

		    if (dataBase != null) {
			reader.setDatabase(dataBase);
			return reader;
		    }

		    throw GSException.createException(DatabaseConsumerFactory.class, //
			    "Provider for DB reader found but DB instance is null", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    DB_READER_CREATION_FAILED_DB_NULL_EXCEPTION);
		}
	    }

	    throw GSException.createException(DatabaseConsumerFactory.class, //
		    "Suitable provider not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    DB_READER_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION);
	}
    }

    /**
     * Loads the available {@link DatabaseWriter}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseWriter} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    @SuppressWarnings("rawtypes")
    public static DatabaseWriter createDataBaseWriter(StorageUri dbUri) throws GSException {

	synchronized (DatabaseProviderFactory.PROVIDER_LOCK) {

	    ServiceLoader<DatabaseWriter> writers = ServiceLoader.load(DatabaseWriter.class);

	    for (DatabaseWriter writer : writers) {

		if (writer.supports(dbUri)) {

		    DatabaseProvider provider = DatabaseProviderFactory.create(dbUri);
		    Database dataBase = provider.getDatabase();

		    if (dataBase == null) {

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Initializing of database writer for uri {} STARTED",
				dbUri.getUri());

			provider.initialize(dbUri, dbUri.getConfigFolder());

			dataBase = provider.getDatabase();

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Initializing of database writer for uri {} ENDED",
				dbUri.getUri());

		    }

		    if (dataBase != null) {
			writer.setDatabase(dataBase);
			return writer;
		    }

		    throw GSException.createException(DatabaseConsumerFactory.class, //
			    "Provider for DB reader found but DB instance is null", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    DB_WRITER_CREATION_FAILED_DB_NULL_EXCEPTION);
		}
	    }

	    throw GSException.createException(DatabaseConsumerFactory.class, //
		    "Suitable provider not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    DB_WRITER_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION);

	}
    }

    /**
     * Loads the available {@link SourceStorage}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link SourceStorage} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    @SuppressWarnings("rawtypes")
    public static SourceStorage createSourceStorage(StorageUri dbUri) throws GSException {

	synchronized (DatabaseProviderFactory.PROVIDER_LOCK) {

	    ServiceLoader<SourceStorage> storages = ServiceLoader.load(SourceStorage.class);

	    for (SourceStorage storage : storages) {

		if (storage.supports(dbUri)) {

		    DatabaseProvider provider = DatabaseProviderFactory.create(dbUri);
		    Database dataBase = provider.getDatabase();

		    if (dataBase == null) {

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Initializing of source storage for uri {} STARTED",
				dbUri.getUri());

			provider.initialize(dbUri, dbUri.getConfigFolder());

			dataBase = provider.getDatabase();

			GSLoggerFactory.getLogger(DatabaseConsumerFactory.class).debug("Initializing of source storage for uri {} ENDED",
				dbUri.getUri());

		    }

		    if (dataBase != null) {
			storage.setDatabase(dataBase);
			return storage;
		    }

		    throw GSException.createException(DatabaseConsumerFactory.class, //
			    "Provider for DB reader found but DB instance is null", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    SOURCE_STORAGE_CREATION_FAILED_DB_NULL_EXCEPTION);

		}
	    }

	    throw GSException.createException(DatabaseConsumerFactory.class, //
		    "Suitable provider not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    SOURCE_STORAGE_CREATION_FAILED_PROVIDER_NOT_FOUND_EXCEPTION);
	}
    }
}
