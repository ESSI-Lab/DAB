package eu.essi_lab.api.configuration.storage;

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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.util.ServiceLoader;

import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import org.slf4j.Logger;

public class GSConfigurationStorageFactory {

    public static final String ERR_ID_NULL_DB_URL = "ERR_ID_NULL_DB_URL";

    private static Logger logger = GSLoggerFactory.getLogger(GSConfigurationStorageFactory.class);
    public static IGSConfigurationStorage createConfigurationStorage(StorageUri url) throws GSException {

	validateURL(url);

	ServiceLoader<IGSConfigurationStorage> templates = ServiceLoader.load(IGSConfigurationStorage.class);

	for (IGSConfigurationStorage c : templates) {

	    Boolean ok = tryTemplate(c, url);

	    if (ok)
		return c;

	}

	return null;
    }

    public static Boolean tryTemplate(IGSConfigurationStorage c, StorageUri url) {

	if (!c.supports(url))
	    return false;

	if (!c.validate(url))
	    return false;

	c.setStorageUri(url);

	return true;
    }

    /**
     * This method initializes the remote storage referenced by the provided {@link StorageUri} with the {@link GSConfiguration} which is
     * provided.
     *
     * @param url: the {@link StorageUri} of the remote storage
     * @param conf: the {@link GSConfiguration} to write
     * @return the actual {@link StorageUri} where the configuration was stored or null if the remote storage type is not supported.
     * @throws {@link GSException} if the configuration URL is null or empty or if the write operation fails.
     */
    public static StorageUri storeConfigurationToRemote(StorageUri url, GSConfiguration conf) throws GSException {

	logger.info("Initializing Configuration url {}", url.getUri());

	validateURL(url);

	ServiceLoader<IGSConfigurationStorage> templates = ServiceLoader.load(IGSConfigurationStorage.class);

	for (IGSConfigurationStorage c : templates) {

	    StorageUri turl = tryInitializeTemplate(c, url, conf);

	    if (turl != null)
		return turl;

	}

	return null;
    }

    private static void validateURL(StorageUri url) throws GSException {

	if (url == null || url.getUri() == null || url.getUri() == "") {

	    GSException ex = new GSException();
	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(GSConfigurationStorageFactory.class.getName());
	    ei.setErrorId(ERR_ID_NULL_DB_URL);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    ei.setUserErrorDescription("Please provide a not-empty DB url");

	    ex.addInfo(ei);

	    throw ex;
	}

    }

    public static StorageUri tryInitializeTemplate(IGSConfigurationStorage c, StorageUri url, GSConfiguration conf) throws GSException {

	if (!c.supports(url))
	    return null;

	c.setStorageUri(url);

	c.transactionUpdate(conf);

	return url;
    }
}
