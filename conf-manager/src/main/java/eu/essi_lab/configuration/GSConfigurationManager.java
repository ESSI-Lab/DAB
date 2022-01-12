package eu.essi_lab.configuration;

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

import java.util.List;
import java.util.Observable;

import eu.essi_lab.configuration.reader.GSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.configuration.sync.IConfigurationSync;
import eu.essi_lab.configuration.writer.GSConfigurationWriter;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class GSConfigurationManager extends Observable implements IGSConfigurationReader, IGSConfigurationWriter {

    private static final String ERR_ID_INVALID_OPTION_VALUE = "ERR_ID_INVALID_OPTION_VALUE";

    private IGSConfigurationReader delegateReader;

    private GSConfigurationWriter delegateWriter;

    public GSConfigurationManager() throws GSException {
	this(ConfigurationSync.getInstance());
    }

    public GSConfigurationManager(IConfigurationSync s) throws GSException {

	delegateReader = new GSConfigurationReader(s);

	delegateWriter = new GSConfigurationWriter(s, delegateReader);

    }
    public GSConfigurationManager(GSConfiguration conf) throws GSException {

	delegateReader = new GSConfigurationReader(conf);

	delegateWriter = new GSConfigurationWriter(conf, delegateReader);

    }

    @Override
    public IGSConfigurable setOption(ConfigurableKey componentKey, GSConfOption<?> option) throws GSException {

	validateOption(option);

	IGSConfigurable component = delegateWriter.setOption(componentKey, option);

	flush();

	ComponentTreeFlusher flusher = new ComponentTreeFlusher(delegateReader.getConfiguration());

	flusher.flushPath(componentKey);

	return component;
    }

    @Override
    public List<GSSource> readGSSources() throws GSException {

	return delegateReader.readGSSources();

    }

    @Override
    public <T> T readInstantiableType(Class<T> clazz, String identifier, Deserializer deserializer) throws GSException {

	return delegateReader.readInstantiableType(clazz, identifier, deserializer);

    }

    @Override
    public <T> List<T> readInstantiableType(Class<T> c, Deserializer deserializer) throws GSException {
	return delegateReader.readInstantiableType(c, deserializer);
    }

    @Override
    public long readTimeStamp() throws GSException {

	return delegateReader.readTimeStamp();

    }

    @Override
    public ConfigurableKey getConfigurableKey(IGSConfigurable configurable) throws GSException {

	return delegateReader.getConfigurableKey(configurable);

    }

    @Override
    public GSConfiguration getConfiguration() throws GSException {

	return delegateReader.getConfiguration();

    }

    private void validateOption(GSConfOption option) throws GSException {

	try {

	    option.validate();

	} catch (GSException ex) {

	    throw ex;

	} catch (RuntimeException thr) {

	    GSException ex = GSException.createException(this.getClass(), "Tried to set an invalid option with key " + option.getKey(),
		    null, "The provided option value for option " + option.getLabel() + " is not valid.", ErrorInfo.ERRORTYPE_CLIENT,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_INVALID_OPTION_VALUE, thr);

	    ErrorInfo ei = new ErrorInfo();

	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    ex.addInfo(ei);

	    throw ex;

	}

    }

    @Override
    public void flush() throws GSException {

	this.addObserver(ConfigurationSync.getInstance());

	delegateWriter.flush();

	setChanged();

	notifyObservers(delegateReader.getConfiguration());

    }

    @Override
    public void flush(GSConfiguration c) throws GSException {

	this.addObserver(ConfigurationSync.getInstance());

	this.addObserver(delegateReader);

	delegateWriter.flush(c);

	setChanged();

	notifyObservers(c);

    }

    @Override
    public IGSConfigurable addSource(Source source) throws GSException {

	IGSConfigurable component = delegateWriter.addSource(source);

	flush();

	component.onFlush();

	return component;
    }

    @Override
    public void update(Observable o, Object arg) {
	// TODO Auto-generated method stub

    }

    @Override
    public GSConfOption<?> readOption(IGSConfigurable component, String optionKey) throws GSException {

	return delegateReader.readOption(component, optionKey);
    }

    @Override
    public IGSConfigurable readComponent(ConfigurableKey componentKey) throws GSException {

	return delegateReader.readComponent(componentKey);
    }

    @Override
    public Source readSource(String uniqueIdentifier) {
	return delegateReader.readSource(uniqueIdentifier);
    }

    @Override
    public GSSource readGSSource(String uniqueIdentifier) throws GSException {
	return delegateReader.readGSSource(uniqueIdentifier);
    }

}
