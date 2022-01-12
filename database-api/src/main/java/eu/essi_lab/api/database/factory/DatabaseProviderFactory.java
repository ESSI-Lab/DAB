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
public class DatabaseProviderFactory implements Serializable {

    private static final String NULL_DB_URI_ERR_ID = "NULL_DB_URI_ERR_ID";

    private static Map<StorageUri, DatabaseProvider> providersMap = new HashMap<>();

    public DatabaseProviderFactory() {
	// nothing to do here
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
    public DatabaseProvider create(StorageUri dbUri) throws GSException {

	if (dbUri == null || dbUri.getUri() == null) {
	    throw GSException.createException(DatabaseProviderFactory.class, "Missing provider uri", null, null, ErrorInfo.ERRORTYPE_CLIENT,
		    ErrorInfo.SEVERITY_ERROR, NULL_DB_URI_ERR_ID, null);
	}

	GSLoggerFactory.getLogger(getClass()).trace("Storage URI [" + dbUri + "]");
	GSLoggerFactory.getLogger(getClass()).trace("Providers map keys: [" + providersMap.keySet() + "]");

	if (providersMap.containsKey(dbUri)) {

	    GSLoggerFactory.getLogger(getClass()).trace("Reusing provider with URI [" + dbUri + "]");
	    return providersMap.get(dbUri);
	}

	ServiceLoader<DatabaseProvider> inits = ServiceLoader.load(DatabaseProvider.class);

	for (DatabaseProvider init : inits) {
	    if (init.supports(dbUri)) {

		providersMap.put(dbUri, init);
		return init;
	    }
	}

	return null;
    }
}
