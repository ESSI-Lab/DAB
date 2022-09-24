package eu.essi_lab.gssrv.starter;

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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.essi_lab.api.configuration.storage.GSConfigurationStorageFactory;
import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.gssrv.servlet.ServletListener;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
public class ConfigurationLookup {

    private static final String CONFIGURATION_URI = "conf/configurationURL.json";
    private static final String CONFIGURATION_LOOKUP_ALIEN_ERROR = "CONFIGURATION_LOOKUP_ALIEN_ERROR";
    private static final String CONFIGURATION_STORAGE_INIT_ERROR = "CONFIGURATION_STORAGE_INIT_ERROR";
    private static final String COUNFIGURATION_RELATIVE_DIR = "conf/";

    /**
     * This method looks for configuration URLs in configuration file(s) and instantiates proper objects implementing
     * the interface {@link
     * IGSConfigurationStorage}. Configuration URLs are searched in the following order: 1) file
     * conf/configurationURL.json 2) retrieve from
     * other nodes in the cloud (if the underlying cloud infrastructure is supported). This second option is not
     * implemented yet.
     *
     * @return the {@link IGSConfigurationStorage} implementation object or null if no configuration URL is found either
     *         in step 1 or 2.
     * @throws {@link GSException} if a configuration URL is found but it refers to a storage type which is not
     *         supported.
     */
    public IGSConfigurationStorage getDBGIsuiteConfiguration() throws GSException {

	try {

	    InputStream is = GISuiteStarter.class.getClassLoader().getResourceAsStream(CONFIGURATION_URI);

	    ConfigurationFile obj;

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(is, baos);
	    is.close();
	    baos.close();
	    
//	    String content = new String(baos.toByteArray());
//	    GSLoggerFactory.getLogger(getClass()).info(content);
	    
	    try (JsonParser jp = new JsonFactory().createParser(baos.toByteArray())) {
		obj = new ObjectMapper().reader().readValue(jp, ConfigurationFile.class);
	    }

	    String url = obj.getUrl();
	    String confPrint = "Using configuration.url=" + url;
	    GSLoggerFactory.getLogger(getClass()).info(confPrint);
	    IGSConfigurationStorage storage = getDBGIsuiteConfiguration(new StorageUri(url));

	    //
	    // see GIP-323
	    //
	    //
	    // the configuration storage is supposed to be null only in case of a fresh suite start,
	    // that is, without configuration file.
	    // in the other 2 cases, via profile or shared remote configuration, it has to be non null
	    //
	    // - obj.getUrl():
	    // empty -> "". Only in this case a null storage is admitted
	    // profile -> "full-prod-db-no-quartz.json"
	    // remote ->
	    // "xdbc://$DBPRODUCTIONU:$DBPRODUCTIONP@marklogic.host.production/PRODUCTION-DB/preprodenvconf/"
	    //
	    if (storage == null && !obj.getUrl().isEmpty()) {

		throw GSException.createException(//
			getClass(), //
			"Unable to initialize the configuration storage", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			CONFIGURATION_STORAGE_INIT_ERROR);
	    }

	    return storage;

	} catch (GSException ex) {

	    throw ex;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    CONFIGURATION_LOOKUP_ALIEN_ERROR, //
		    e);
	}
    }

    /**
     * This method tries to instantiate proper objects implementing the interface {@link IGSConfigurationStorage}.
     * Configuration URL is
     * passed as argument.
     *
     * @param url of configuration file
     * @return the {@link IGSConfigurationStorage} implementation object.
     * @throws {@link GSException} if the configuration URL refers to a storage type which is not supported.
     */
    public IGSConfigurationStorage getDBGIsuiteConfiguration(StorageUri url) throws GSException {

	return GSConfigurationStorageFactory.createConfigurationStorage(toAbsolutePath(url));

    }

    /**
     * This method transforms a {@link StorageUri} object into an object which provides an absolute path when invoking
     * {@link
     * StorageUri#getUri()}.
     *
     * @param url of configuration file
     * @return the absolutre path {@link StorageUri} object.
     */
    public static StorageUri toAbsolutePath(StorageUri url) {

	if (!isLocalRelative(url))
	    return url;

	URL relPath = GISuiteStarter.class.getClassLoader().getResource(COUNFIGURATION_RELATIVE_DIR);

	return new StorageUri(Paths.get(URI.create(relPath.toString() + url.getUri())).toUri().toString());

    }

    public static boolean isLocalRelative(StorageUri url) {

	String lc = url.getUri().toLowerCase();

	return !lc.contains("://");

    }
}
