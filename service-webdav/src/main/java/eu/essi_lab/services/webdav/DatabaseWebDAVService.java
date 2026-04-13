/**
 *
 */
package eu.essi_lab.services.webdav;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.opensearch.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.services.impl.*;
import io.milton.config.*;
import io.milton.http.*;
import io.milton.simpleton.*;

/**
 * @author Fabrizio
 */
public class DatabaseWebDAVService extends AbstractManagedService {

    /**
     *
     */
    private static final int DEFAULT_PORT = 8083;


    /**
     *
     */
    private static final int DEFAULT_MAX_FILES = 1000;

    /**
     *
     */
    private static final String PORT_KEY = "port";
    private static final String MAX_FILES_KEY = "maxFiles";

    /**
     *
     */
    private SimpletonServer server;

    /**
     *
     */
    @Override
    public void start() {

	int port = getSetting(). //
		readKeyValue(PORT_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_PORT);//

	int maxFiles = getSetting(). //
		readKeyValue(MAX_FILES_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_MAX_FILES);//

	StorageInfo osStorageInfo = ConfigurationWrapper.getStorageInfo();

	OpenSearchDatabase database = new OpenSearchDatabase();
	try {
	    database.initialize(osStorageInfo);

	} catch (GSException e) {
	    throw new RuntimeException(e);
	}

	DatabaseResourceFactory factory = new DatabaseResourceFactory(database, maxFiles);

	HttpManagerBuilder builder = new HttpManagerBuilder();

	builder.setEnableFormAuth(false);
	builder.setResourceFactory(factory);

	HttpManager httpManager = builder.buildHttpManager();

	server = new SimpletonServer(httpManager, builder.getOuterWebdavResponseHandler(), 100, 1);
	server.setHttpPort(port);

	server.start();
    }

    /**
     *
     */
    @Override
    public void stop() {

	server.stop();
    }
}
