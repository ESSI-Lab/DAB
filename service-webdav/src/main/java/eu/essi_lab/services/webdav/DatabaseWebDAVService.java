/**
 *
 */
package eu.essi_lab.services.webdav;

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
    private static final String PORT_KEY = "";

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

	StorageInfo osStorageInfo = ConfigurationWrapper.getStorageInfo();

	OpenSearchDatabase database = new OpenSearchDatabase();
	try {
	    database.initialize(osStorageInfo);

	} catch (GSException e) {
	    throw new RuntimeException(e);
	}

	DatabaseResourceFactory factory = new DatabaseResourceFactory(database);

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
