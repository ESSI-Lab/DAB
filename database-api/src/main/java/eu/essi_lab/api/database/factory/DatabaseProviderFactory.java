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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseConsumer;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This factory is the entry point of the "DataBase Manager" API<br>
 * <br>
 * <b>Usage notes</b><br>
 * <br>
 * Clients should create a suitable {@link DatabaseProvider} for a given {@link StorageUri} and initialize it with the
 * {@link DatabaseProvider#initialize(StorageUri, String)}
 * method. If the initialization is successful, the provided {@link Database} instance is shared between all the
 * suitable {@link DatabaseConsumer}s
 *
 * @author Fabrizio
 */
public class DatabaseProviderFactory {

    /**
     * 
     */
    static final Object PROVIDER_LOCK = new Object();

    private static final String NULL_DB_URI_ERR_ID = "NULL_DB_URI_ERR_ID";

    private static final String DB_PROVIDER_FACTORY_PROVIDER_NOT_FOUND_EXCEPTION = "DB_PROVIDER_FACTORY_PROVIDER_NOT_FOUND_EXCEPTION";

    private static Map<StorageUri, DatabaseProvider> providersMap = new HashMap<>();

    private DatabaseProviderFactory() {
    }

    public static void clearProviders() {
	providersMap.clear();
    }

    /**
     * Loads the available {@link DatabaseProvider}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param dbUri
     * @return the suitable {@link DatabaseProvider} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static DatabaseProvider create(StorageUri dbUri) throws GSException {

	synchronized (PROVIDER_LOCK) {

	    if (dbUri == null || dbUri.getUri() == null) {

		throw GSException.createException(//
			DatabaseProviderFactory.class, //
			"Missing provider uri", //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_CLIENT, //
			ErrorInfo.SEVERITY_ERROR, //
			NULL_DB_URI_ERR_ID, //
			null);
	    }

	    if (providersMap.containsKey(dbUri)) {

		return providersMap.get(dbUri);

	    } else {

		GSLoggerFactory.getLogger(DatabaseProviderFactory.class).trace("Initializing new provider with URI [" + dbUri + "]");
	    }

	    ServiceLoader<DatabaseProvider> inits = ServiceLoader.load(DatabaseProvider.class);

	    for (DatabaseProvider init : inits) {
		if (init.supports(dbUri)) {

		    providersMap.put(dbUri, init);

		    GSLoggerFactory.getLogger(DatabaseProviderFactory.class).trace("Providers map keys: [" + providersMap.keySet() + "]");

		    return init;
		}
	    }

	    throw GSException.createException(DatabaseConsumerFactory.class, //
		    "Suitable provider not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    DB_PROVIDER_FACTORY_PROVIDER_NOT_FOUND_EXCEPTION);

	}
    }
}
