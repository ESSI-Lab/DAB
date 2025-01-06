package eu.essi_lab.api.database.factory;

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

import java.util.HashMap;
import java.util.ServiceLoader;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseSemanticsExecutor;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class DatabaseProviderFactory {

    private static final HashMap<StorageInfo, DatabaseWriter> WRITERS_MAP = new HashMap<>();
    private static final HashMap<StorageInfo, DatabaseReader> READERS_MAP = new HashMap<>();
    private static final HashMap<StorageInfo, DatabaseFinder> FINDERS_MAP = new HashMap<>();
    private static final HashMap<StorageInfo, DatabaseExecutor> EXECUTORS_MAP = new HashMap<>();
    private static final HashMap<StorageInfo, DatabaseSemanticsExecutor> SEMANTICS_MAP = new HashMap<>();

    private DatabaseProviderFactory() {
    }
    
    /**
     * Loads the available {@link DatabaseFinder}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseFinder} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseFinder getFinder(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    DatabaseFinder mapped = FINDERS_MAP.get(dbUri);
	    if (mapped != null) {

		return mapped;
	    }

	    Database database = DatabaseFactory.get(dbUri);

	    for (DatabaseFinder finder : ServiceLoader.load(DatabaseFinder.class)) {

		if (finder.supports(dbUri)) {

		    finder.setDatabase(database);

		    FINDERS_MAP.put(dbUri, finder);

		    return finder;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"DatabaseFinderCreationError");

    }

    /**
     * Loads the available {@link DatabaseReader}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseReader} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseReader getReader(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    DatabaseReader mapped = READERS_MAP.get(dbUri);
	    if (mapped != null) {

		return mapped;
	    }

	    Database database = DatabaseFactory.get(dbUri);

	    for (DatabaseReader reader : ServiceLoader.load(DatabaseReader.class)) {

		if (reader.supports(dbUri)) {

		    reader.setDatabase(database);

		    READERS_MAP.put(dbUri, reader);

		    return reader;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"DatabaseReaderCreationError");

    }

    /**
     * Loads the available {@link DatabaseWriter}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseWriter} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseWriter getWriter(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    DatabaseWriter mapped = WRITERS_MAP.get(dbUri);
	    if (mapped != null) {

		return mapped;
	    }

	    Database database = DatabaseFactory.get(dbUri);

	    for (DatabaseWriter writer : ServiceLoader.load(DatabaseWriter.class)) {

		if (writer.supports(dbUri)) {

		    writer.setDatabase(database);

		    WRITERS_MAP.put(dbUri, writer);

		    return writer;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"DatabaseWriterCreationError");

    }

    /**
     * Loads the available {@link DatabaseExecutor}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseWriter} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseExecutor getExecutor(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    DatabaseExecutor mapped = EXECUTORS_MAP.get(dbUri);
	    if (mapped != null) {

		return mapped;
	    }

	    Database database = DatabaseFactory.get(dbUri);

	    for (DatabaseExecutor executor : ServiceLoader.load(DatabaseExecutor.class)) {

		if (executor.supports(dbUri)) {

		    executor.setDatabase(database);

		    EXECUTORS_MAP.put(dbUri, executor);

		    return executor;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"DatabaseExecutorCreationError");

    }

    /**
     * Loads the available {@link DatabaseExecutor}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseWriter} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseSemanticsExecutor getSemanticsExecutor(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    DatabaseSemanticsExecutor mapped = SEMANTICS_MAP.get(dbUri);
	    if (mapped != null) {

		return mapped;
	    }

	    Database database = DatabaseFactory.get(dbUri);

	    for (DatabaseSemanticsExecutor executor : ServiceLoader.load(DatabaseSemanticsExecutor.class)) {

		if (executor.supports(dbUri)) {

		    executor.setDatabase(database);

		    SEMANTICS_MAP.put(dbUri, executor);

		    return executor;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"DatabaseSemanticsExecutorCreationError");

    }

    /**
     * Loads the available {@link SourceStorage}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link SourceStorage} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static SourceStorage getSourceStorage(StorageInfo dbUri) throws GSException {

	synchronized (DatabaseFactory.PROVIDER_LOCK) {

	    Database database = DatabaseFactory.get(dbUri);

	    for (SourceStorage storage : ServiceLoader.load(SourceStorage.class)) {

		if (storage.supports(dbUri)) {

		    storage.setDatabase(database);

		    return storage;
		}
	    }
	}

	throw GSException.createException(DatabaseProviderFactory.class, //
		"Suitable provider not found: " + dbUri, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_FATAL, //
		"SourceStorageCreationError");

    }
}
