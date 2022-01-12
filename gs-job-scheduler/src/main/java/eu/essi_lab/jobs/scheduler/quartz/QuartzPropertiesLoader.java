package eu.essi_lab.jobs.scheduler.quartz;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.jobs.configuration.scheduler.InstantiableSchedulerInfo;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class QuartzPropertiesLoader extends AbstractGSconfigurable implements IGSConfigurable, IGSMainConfigurable {

    public static final String QUARTZ_MAIN_COMPONENT_KEY = "QUARTZ_MAIN_COMPONENT_KEY";
    private static final java.lang.String ERR_ID_QUARTZ_PROPERTIES_NOT_FOUND = "ERR_ID_QUARTZ_PROPERTIES_NOT_FOUND";
    private Map<String, GSConfOption<?>> supported = new HashMap<>();
    private static final String SQL_DB_OPTION_KEY = "SQL_DB_OPTION_KEY";
    @JsonIgnore
    private  transient Logger logger = GSLoggerFactory.getLogger(QuartzPropertiesLoader.class);

    @JsonIgnore
    private  transient String propsFile = "quartz.properties";

    public QuartzPropertiesLoader() {

	super();
	setKey(QUARTZ_MAIN_COMPONENT_KEY);
	setLabel("Quartz");

	GSConfOptionDBURI option = new GSConfOptionDBURI();
	option.setKey(SQL_DB_OPTION_KEY);
	option.setLabel("MySQL DataBase");

	getSupportedOptions().put(SQL_DB_OPTION_KEY, option);

    }

    public Properties loadDefaultProperties() throws GSException {

	InputStream stream = this.getClass().getClassLoader().getResourceAsStream(propsFile);

	Properties properties = new Properties();

	try {
	    properties.load(stream);
	} catch (IOException e) {

	    logger.error("Can't find {}", propsFile, e);

	    throw GSException.createException(this.getClass(), "Error thrwon by Quartz Properties Loader", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_PROPERTIES_NOT_FOUND, e);
	}

	return properties;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	//Nothing to do on option set action

    }

    @Override
    public void onFlush() throws GSException {

	QuartzSchedulerConfiguration.getInstance().update((GSConfOptionDBURI) getSupportedOptions().get(SQL_DB_OPTION_KEY));

    }

    @Override
    public IGSConfigurationInstantiable getInstantiableType() {

	InstantiableSchedulerInfo info = new InstantiableSchedulerInfo();

	try {

	    GSConfOption<?> option = getSupportedOptions().get(SQL_DB_OPTION_KEY);

	    if (option != null) {

		StorageUri storageUri = ((StorageUri) option.getValue());

		if (storageUri != null)
		    info = new Deserializer().deserialize(storageUri.serialize(), InstantiableSchedulerInfo.class);
	    }

	    info.setComponent(this);

	} catch (GSException e) {
	    //TODO whatr should I do here?

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

	return info;
    }

}
