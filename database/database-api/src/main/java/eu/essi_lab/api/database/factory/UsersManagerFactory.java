/**
 * 
 */
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

import eu.essi_lab.api.database.UsersManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class UsersManagerFactory {

    /**
     * 
     */
    static final Object PROVIDER_LOCK = new Object();

    private static final Map<StorageInfo, UsersManager> PROVIDERS_MAP = new HashMap<>();

    private UsersManagerFactory() {
    }

    /**
     * Loads the available {@link UsersManager}s using the {@link ServiceLoader} API and
     * selects the one suitable for the given <code>dbUri</code>
     *
     * @param info
     * @return the suitable {@link UsersManager} or <code>null</code> if none is found
     * @throws GSException if dbUri is <code>null</code> or dbUri.getUri is <code>null</code>
     */
    public static UsersManager get(StorageInfo info) throws GSException {

	if (info == null || info.getUri() == null) {

	    throw GSException.createException(//
		    DatabaseFactory.class, //
		    "Missing provider info", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "UsersManagerFactoryMissingInfoError", //
		    null);
	}

	synchronized (PROVIDER_LOCK) {

	    if (PROVIDERS_MAP.containsKey(info)) {

		return PROVIDERS_MAP.get(info);
	    }

	    for (UsersManager manager : ServiceLoader.load(UsersManager.class)) {

		if (manager.supports(info)) {

		    GSLoggerFactory.getLogger(DatabaseProviderFactory.class).debug("Initialization of users manager for info {} STARTED",
			    info.getUri());

		    manager.initialize(info);

		    GSLoggerFactory.getLogger(DatabaseProviderFactory.class).debug("Initialization of users manager for info {} ENDED",
			    info.getUri());

		    PROVIDERS_MAP.put(info, manager);

		    GSLoggerFactory.getLogger(DatabaseFactory.class).trace("Providers:\n\n{}",

			    PROVIDERS_MAP.keySet().stream().map(i -> i.toString()).collect(Collectors.joining(",\n\n")));

		    return manager;
		}
	    }

	    throw GSException.createException(DatabaseProviderFactory.class, //
		    "Suitable provider not found: " + info, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "UsersManagerFactorySuitableProviderNotFoundError");

	}
    }
}
