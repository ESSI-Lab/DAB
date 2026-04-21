package eu.essi_lab.api.database.factory;

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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This factory is the entry point of the "DataBase Manager" API<br>
 * <br>
 * <b>Usage notes</b><br>
 * <br>
 * Clients should create a suitable {@link Database} for a given {@link StorageInfo} and initialize it with the
 * {@link Database#initialize(StorageInfo, String)}
 * method. If the initialization is successful, the provided {@link Database} instance is shared between all the
 * suitable {@link DatabaseConsumer}s
 *
 * @author Fabrizio
 */
public class DatabaseFactory {

    /**
     * 
     */
    static final Object PROVIDER_LOCK = new Object();

    private static final Map<StorageInfo, Database> PROVIDERS_MAP = new HashMap<>();

    private DatabaseFactory() {
    }

    /**
     * Loads the available {@link Database}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param info
     * @return the suitable {@link Database} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static Database get(StorageInfo info) throws GSException {

	if (info == null || info.getUri() == null) {

	    throw GSException.createException(//
		    DatabaseFactory.class, //
		    "Missing provider info", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "DatabaseFactoryMissingInfoError", //
		    null);
	}

	synchronized (PROVIDER_LOCK) {

	    if (PROVIDERS_MAP.containsKey(info)) {

		return PROVIDERS_MAP.get(info);
	    }

	    for (Database database : ServiceLoader.load(Database.class)) {

		if (database.supports(info)) {

		    GSLoggerFactory.getLogger(DatabaseProviderFactory.class)
			    .debug("Initialization of database provider for info {} STARTED", info.getUri());

		    database.initialize(info);

		    PROVIDERS_MAP.put(info, database);

		    GSLoggerFactory.getLogger(DatabaseProviderFactory.class).debug("Initialization of database provider for info {} ENDED",
			    info.getUri());

		    GSLoggerFactory.getLogger(DatabaseFactory.class).trace("Providers:\n\n{}",

			    PROVIDERS_MAP.keySet().stream().map(i -> i.toString()).collect(Collectors.joining(",\n\n")));

		    return database;
		}
	    }

	    throw GSException.createException(DatabaseProviderFactory.class, //
		    "Suitable provider not found: " + info, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "DatabaseProviderFactorySuitableProviderNotFoundError");

	}
    }
}
