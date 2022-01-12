package eu.essi_lab.api.database.configuration;

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
import java.util.Iterator;
import java.util.Map;


import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import org.slf4j.Logger;
public class DataBaseConfiguration extends AbstractGSconfigurableComposed implements IGSMainConfigurable, IGSConfigurableComposed, Serializable {

    public static final String DATABASE_URI_OPTION_KEY = "DATABASE_URI_OPTION_KEY";
    public static final String GISUITE_ID_OPTION_KEY = "GISUITE_ID_OPTION_KEY";
    public static final String DATABASE_MAIN_COMPONENT_KEY = "DATABASE_MAIN_COMPONENT_KEY";
    private Map<String, GSConfOption<?>> supported = new HashMap<>();
    private DatabaseConsumerFactory factory = new DatabaseConsumerFactory();
    private DatabaseProviderFactory dataBaseProviderFactory = new DatabaseProviderFactory();
    private static final String DB_PROVIDER_CREATION_ERR_ID = "DB_PROVIDER_CREATION_ERR_ID";

    @JsonIgnore
    private  transient Logger logger = GSLoggerFactory.getLogger(DataBaseConfiguration.class);

    private Logger getLogger() {
	if (logger==null) {
	    logger = GSLoggerFactory.getLogger(DataBaseConfiguration.class);
	}
	return logger;
    }
    
    public DataBaseConfiguration() {

	super();

	setKey(DATABASE_MAIN_COMPONENT_KEY);

	setLabel("DataBase");

	GSConfOptionString gisuiteIdOpt = new GSConfOptionString();
	gisuiteIdOpt.setKey(GISUITE_ID_OPTION_KEY);
	gisuiteIdOpt.setLabel("GI-suite identifier");
	gisuiteIdOpt.setMandatory(true);

	getSupportedOptions().put(GISUITE_ID_OPTION_KEY, gisuiteIdOpt);
    }

    @JsonIgnore
    public void setDataBaseConsumerFactory(DatabaseConsumerFactory f) {
	factory = f;
    }

    @JsonIgnore
    public void setDataBaseProviderFactory(DatabaseProviderFactory f) {
	dataBaseProviderFactory = f;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (opt.getKey().equalsIgnoreCase(DATABASE_URI_OPTION_KEY) && opt instanceof GSConfOptionDBURI) {

	    StorageUri uri = ((GSConfOptionDBURI) opt).getValue();

	    InstantiableMetadataDBStorageURI iuri = new Deserializer().deserialize(uri.serialize(), InstantiableMetadataDBStorageURI.class);
	    DatabaseProvider provider;

	    try {

		provider = dataBaseProviderFactory.create(uri);

		provider.initialize(iuri, getGISuiteId());

		DatabaseReader reader = factory.createDataBaseReader(uri);

		reader.setDatabase(provider.getDatabase());

		getConfigurableComponents().put(reader.getKey(), reader);

		DatabaseWriter writer = factory.createDataBaseWriter(uri);

		writer.setDatabase(provider.getDatabase());

		getConfigurableComponents().put(writer.getKey(), writer);

		SourceStorage storage = factory.createSourceStorage(uri);

		storage.setDatabase(provider.getDatabase());

		getConfigurableComponents().put(storage.getKey(), storage);

	    } catch (GSException ex) {

		ErrorInfo ei = new ErrorInfo();

		ei.setContextId(this.getClass().getName());
		ei.setErrorId(DB_PROVIDER_CREATION_ERR_ID);

		ei.setUserErrorDescription("Can't connect to DB with url " + iuri.getUri());

		ex.addInfo(ei);

		throw ex;
	    }

	    iuri.setComponentId(this.getKey());

	    iuri.setComponent(this);

	    // TODO use the method provider.getconfigurable?

	    setInstantiableType(iuri);

	}

	if (opt.getKey().equalsIgnoreCase(GISUITE_ID_OPTION_KEY) && opt instanceof GSConfOptionString) {
	    GSConfOptionDBURI uriOption = new GSConfOptionDBURI();

	    uriOption.setLabel("Data Base Connection");

	    uriOption.setKey(DATABASE_URI_OPTION_KEY);

	    uriOption.setMandatory(true);

	    getSupportedOptions().put(DATABASE_URI_OPTION_KEY, uriOption);

	}
    }

    @Override
    public void onFlush() throws GSException {

	// Nothing to da on flush action

    }

    @JsonIgnore
    public DatabaseReader getReader() throws GSException {

	getLogger().trace("Getting DatabaseReader from configurables");
	Iterator<IGSConfigurable> it = getConfigurableComponents().values().iterator();

	while (it.hasNext()) {

	    getLogger().trace("Found configurable");

	    IGSConfigurable component = it.next();

	    if (component instanceof DatabaseReader) {

		getLogger().trace("Found DatabaseReader");

		DatabaseReader reader = (DatabaseReader) component;

		DatabaseProvider provider = getProvider();

		getLogger().trace("Found DatabaseProvider {} with database null? {}", provider.getClass().getCanonicalName(),
			(provider.getDatabase() == null));

		reader.setDatabase(provider.getDatabase());

		return reader;
	    }
	}

	return null;
    }

    @JsonIgnore
    public DatabaseWriter getWriter() throws GSException {

	getLogger().trace("Get DatabaseWriter");

	Iterator<IGSConfigurable> it = getConfigurableComponents().values().iterator();

	while (it.hasNext()) {

	    IGSConfigurable component = it.next();

	    if (component instanceof DatabaseWriter) {

		DatabaseWriter writer = (DatabaseWriter) component;

		writer.setDatabase(getProvider().getDatabase());

		return writer;
	    }
	}

	return null;
    }

    @JsonIgnore
    public SourceStorage getSourceStorage() throws GSException {

	getLogger().trace("Get SourceStorage");

	Iterator<IGSConfigurable> it = getConfigurableComponents().values().iterator();

	while (it.hasNext()) {

	    IGSConfigurable component = it.next();

	    if (component instanceof SourceStorage) {

		SourceStorage writer = (SourceStorage) component;

		writer.setDatabase(getProvider().getDatabase());

		return writer;
	    }
	}

	return null;
    }

    @JsonIgnore
    public DatabaseProvider getProvider() throws GSException {

	DatabaseProvider provider = dataBaseProviderFactory.create(getDBUri());

	getLogger().trace("Initializing Provider with gi-suite id: {}", getGISuiteId());

	provider.initialize(getDBUri(), getGISuiteId());

	getLogger().trace("Initializing Provider with gi-suite id: {} - Done: provider object is {}", getGISuiteId(), provider);

	return provider;
    }

    @JsonIgnore
    private String getGISuiteId() {

	return (String) getSupportedOptions().get(GISUITE_ID_OPTION_KEY).getValue();

    }

    @JsonIgnore
    StorageUri getDBUri() {

	return (StorageUri) getSupportedOptions().get(DATABASE_URI_OPTION_KEY).getValue();
    }

    @Override
    public IGSConfigurationInstantiable getInstantiableType() {

	InstantiableMetadataDBStorageURI info = new InstantiableMetadataDBStorageURI();

	try {
	    GSConfOption<?> option = getSupportedOptions().get(DATABASE_URI_OPTION_KEY);
	    if (option != null) {

		StorageUri storageUri = ((StorageUri) option.getValue());
		if (storageUri != null)
		    info = new Deserializer().deserialize(storageUri.serialize(), InstantiableMetadataDBStorageURI.class);

	    }
	    info.setComponent(this);

	} catch (GSException e) {
	    //TODO whatr should I do here?

	    getLogger().error("Error executing getInstantiableType", e);
	}

	return info;
    }

}
