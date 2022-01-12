package eu.essi_lab.configuration.reader;

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

import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.configuration.sync.IConfigurationSync;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import org.slf4j.Logger;

public class GSConfigurationReader implements IGSConfigurationReader {

    public static final String ERR_ID_COMPONENT_KEY_INVALID = "OPTION_KEY_INVALID";
    public static final String ERR_ID_NULL_CONF_SYNC = "ERR_ID_NULL_CONF_SYNC";

    private GSConfiguration conf;
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private static final String CANT_CREATE_GLOBAL_KEY_ERR_ID = "CANT_CREATE_GLOBAL_KEY_ERR_ID";
    private static final String CANT_CREATE_GLOBAL_KEY_NULL_CONFIGURABLE_ERR_ID = "CANT_CREATE_GLOBAL_KEY_NULL_CONFIGURABLE_ERR_ID";

    public GSConfigurationReader(IConfigurationSync s) throws GSException {

	if (s == null) {
	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("IConfigurationSync is null");
	    ei.setErrorId(ERR_ID_NULL_CONF_SYNC);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Provide a non-null IConfigurationSync");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    e.addInfo(ei);

	    throw e;
	}

	conf = s.getClonedConfiguration();
    }

    public GSConfigurationReader(GSConfiguration c) {

	conf = c;
    }

    @Override
    public long readTimeStamp() throws GSException {

	return conf.getTimeStamp();

    }

    @Override
    public GSConfiguration getConfiguration() throws GSException {

	return conf;

    }

    @Override
    public void update(Observable o, Object arg) {

	conf = (GSConfiguration) arg;

    }

    @Override
    public GSConfOption<?> readOption(IGSConfigurable component, String optionKey) throws GSException {

	return component.read(optionKey);
    }

    @Override
    public Source readSource(String uniqueIdentifier) {

	Iterator<IGSConfigurable> it = conf.getConfigurableComponents().values().iterator();

	return recursiveSourceRead(uniqueIdentifier, it);

    }

    private Source recursiveSourceRead(String uniqueIdentifier, Iterator<IGSConfigurable> it) {

	while (it.hasNext()) {
	    IGSConfigurable comp = it.next();

	    GSConfOption<?> opt = comp.getSupportedOptions().get(GSConfiguration.GS_SOURCE_OPTION_KEY);

	    if (opt != null && GSConfOptionSource.class.isAssignableFrom(opt.getClass())) {

		GSConfOptionSource sourceOption = ((GSConfOptionSource) opt);

		if (uniqueIdentifier.equalsIgnoreCase(sourceOption.getValue().getUniqueIdentifier()))
		    return sourceOption.getValue();

	    }

	    if (IGSConfigurableComposed.class.isAssignableFrom(comp.getClass())) {

		Source recursiveRead = recursiveSourceRead(uniqueIdentifier,
			((IGSConfigurableComposed) comp).getConfigurableComponents().values().iterator());

		if (recursiveRead != null)
		    return recursiveRead;

	    }
	}

	return null;
    }

    @Override
    public IGSConfigurable readComponent(ConfigurableKey componentKey) throws GSException {

//	logger.trace("Start reading {}", componentKey);

	if (componentKey == null)
	    return null;

	if (componentKey.isRoot()) {
//	    logger.trace("Component key is root");
	    return conf;
	}

	if (componentKey.oneLevelDown()) {

//	    logger.trace("Starting recursive procedure");

	    return recursivePathComponentsRead(componentKey, getConfiguration());

	}

	return null;
    }

    @Override
    public ConfigurableKey getConfigurableKey(IGSConfigurable configurable) throws GSException {

	if (configurable == null) {
	    logger.error("Can't create global key with null configurable");

	    throw GSException.createException(getClass(), "Can't create ConfigurableKey for null configurable", null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, CANT_CREATE_GLOBAL_KEY_NULL_CONFIGURABLE_ERR_ID);
	}
	Chronometer chronometer = new Chronometer();
	chronometer.start();

	ConfigurableKey key = new ConfigurableKey("root");

	if (!GSConfiguration.class.isAssignableFrom(configurable.getClass())) {

	    String serializedConfigurable = ((AbstractGSconfigurable) configurable).serialize();

	    boolean result = recursiveSearch(serializedConfigurable, key, conf);

	    if (!result)

		throw GSException.createException(getClass(), "Can't create ConfigurableKey for " + configurable.getKey(), null,
			ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, CANT_CREATE_GLOBAL_KEY_ERR_ID);
	}

	logger.debug("Completed global key generation in {} millis", chronometer.getElapsedTimeMillis());

	if (logger.isDebugEnabled())
	    logger.debug("Found key {} for configurable {}", key.keyString(), configurable.getKey());

	return key;

    }

    private boolean checkInstantiableType(Class c, String id, IGSConfigurationInstantiable type) {

	return type != null && (id == null || id.equalsIgnoreCase(type.getId())) && isAssignable(c, type);

    }
    boolean isAssignable(Class c, IGSConfigurationInstantiable type) {
	return c.isAssignableFrom(type.getClass());
    }

    private <T> T prepareInstantiable(IGSConfigurationInstantiable type, IGSConfigurable comp, Deserializer deserializer)
	    throws GSException {

	//
	// see GIP 272
	//
//	type.setComponent(comp);

	IGSConfigurationInstantiable clonedType = deserializer.deserialize(type.serializeTo(InputStream.class), type.getClass());

	IGSConfigurable clonedComp = deserializer.deserialize(((AbstractGSconfigurable) comp).serialize(), comp.getClass());

	clonedType.setComponent(clonedComp);

	return (T) clonedType;
    }

    @Override
    public <T> T readInstantiableType(Class<T> c, String id, Deserializer deserializer) throws GSException {

	return readInstantiableType(c, id, deserializer, true);
    }

    @Override
    public <T> List<T> readInstantiableType(Class<T> c, Deserializer deserializer) throws GSException {

	return readInstantiableType(c, deserializer, true);
    }

    public <T> List<T> readInstantiableType(Class<T> c, Deserializer deserializer, boolean cache) throws GSException {

	List<GSSource> cacheAllSources = ConfigurationSync.getInstance().getAllsources();

	if (cache && GSSource.class.isAssignableFrom(c) && cacheAllSources != null && !cacheAllSources.isEmpty()) {

	    return (List<T>) cacheAllSources;

	}

//	logger.trace("Start reading of instantiable {}", c.getName());

	List<T> list = new ArrayList<>();
	IGSConfigurationInstantiable ctype = conf.getInstantiableType();

	if (checkInstantiableType(c, null, ctype))
	    list.add(prepareInstantiable(ctype, conf, deserializer));

	list.addAll(recursiveReadInstantiableType(c, conf, deserializer));

//	logger.trace("Found {} instantiables", list.size());

	return list;
    }

    public <T> T readInstantiableType(Class<T> c, String id, Deserializer deserializer, boolean cache) throws GSException {

	GSSource cachesource = ConfigurationSync.getInstance().getSource(id);

	if (cache && GSSource.class.isAssignableFrom(c) && cachesource != null) {

	    return (T) cachesource;

	}

//	logger.trace("Start reading of instantiable {} with id {}", c.getName(), id);

	IGSConfigurationInstantiable type = conf.getInstantiableType();

	if (checkInstantiableType(c, id, type)) {

	    return prepareInstantiable(type, conf, deserializer);

	}

	return recursiveReadInstantiableType(c, id, conf, deserializer);
    }

    private <T> List<T> recursiveReadInstantiableType(Class<T> c, IGSConfigurableComposed comp, Deserializer deserializer)
	    throws GSException {

	Collection<? extends IGSConfigurable> vals = comp.getConfigurableComponents().values();
	List<T> list = new ArrayList<>();

	for (IGSConfigurable configurable : vals) {

	    IGSConfigurationInstantiable type = configurable.getInstantiableType();

	    boolean found = false;

	    if (checkInstantiableType(c, null, type)) {

		list.add(prepareInstantiable(type, configurable, deserializer));

		found = true;

	    }

	    //this should fix the issue of mixed accessor and multiple GSSources returned
	    // sonar marks the comparison below as a bug, we should mark as resolved since I can't import GSAccessor in this project
	    // TODO test if the new method really works, otherwise roll back this class to commit 17688e9ccea6cd7577b84ab301628d06cd0b3f17
	    if (c.equals(GSSource.class) && compareToGSAccessor(configurable) && found) {

		logger.debug("Found GSSource from a mixed accessor [label: {}, id: {}], skipping recursion", configurable.getLabel(),
			configurable.getKey());

		return list;
	    }

	    if (IGSConfigurableComposed.class.isAssignableFrom(configurable.getClass())) {

		list.addAll(recursiveReadInstantiableType(c, (IGSConfigurableComposed) configurable, deserializer));

	    }

	}

	return list;
    }

    private boolean compareToGSAccessor(IGSConfigurable configurable) {

	try {

	    Class<?> clazz = Class.forName("eu.essi_lab.adk.GSAccessor");

	    Constructor<?> cstr = clazz.getConstructor();

	    Object o = cstr.newInstance();

	    if (o.getClass().isAssignableFrom(configurable.getClass()))
		return true;

	} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {

	    logger.warn("Can't instantiate GSAccessor for class comparison", e);

	}

	return false;
    }

    private <T> T recursiveReadInstantiableType(Class<T> c, String id, IGSConfigurableComposed comp, Deserializer deserializer)
	    throws GSException {

	Collection<? extends IGSConfigurable> vals = comp.getConfigurableComponents().values();

	for (IGSConfigurable configurable : vals) {

	    IGSConfigurationInstantiable type = configurable.getInstantiableType();

	    if (checkInstantiableType(c, id, type)) {

		return prepareInstantiable(type, configurable, deserializer);

	    }

	    if (IGSConfigurableComposed.class.isAssignableFrom(configurable.getClass())) {

		T t = recursiveReadInstantiableType(c, id, (IGSConfigurableComposed) configurable, deserializer);

		if (t != null) {
		    return t;
		}

	    }
	}

	return null;
    }

    private boolean recursiveSearch(String configurable, ConfigurableKey key, IGSConfigurableComposed composed) throws GSException {

	Collection<? extends IGSConfigurable> components = composed.getConfigurableComponents().values();

	for (IGSConfigurable thisLevelComponent : components) {

	    key.addLevel(thisLevelComponent.getKey());

	    key.oneLevelDown();

	    if (sameComponent(configurable, thisLevelComponent)) {

		return true;

	    }

	    if (IGSConfigurableComposed.class.isAssignableFrom(thisLevelComponent.getClass())) {

		boolean recursiceResult = recursiveSearch(configurable, key, (IGSConfigurableComposed) thisLevelComponent);

		if (recursiceResult)
		    return true;
	    }

	    key.oneLevelUp(true);

	}

	return false;

    }

    private boolean sameComponent(String configurable, IGSConfigurable thisLevelComponent) throws GSException {

	String serializedConfigurable = ((AbstractGSconfigurable) thisLevelComponent).serialize();

	return serializedConfigurable.equals(configurable);

    }

    private IGSConfigurable recursivePathComponentsRead(ConfigurableKey componentKey, IGSConfigurableComposed comp) throws GSException {

	String cKey = componentKey.getCurrentComponentKey();

//	logger.trace("Current key component {}", cKey);

//	logger.trace("Component key level {}", componentKey);

	Collection<? extends IGSConfigurable> vals = comp.getConfigurableComponents().values();

	for (IGSConfigurable configurable : vals) {

//	    logger.trace("Comparing with {}", configurable.getKey());

	    if (componentKey.match(configurable)) {


		logger.trace("Match with {}, configurable instance is {}", configurable.getKey(), configurable.toString());

		return configurable;
	    }

//	    logger.trace("Mismatch with configurable key {} -- checking if path is correct against current path key {}",
//		    configurable.getKey(), cKey);

	    if (cKey.equals(configurable.getKey())) {
//		logger.trace("Path is correct");

		if (IGSConfigurableComposed.class.isAssignableFrom(configurable.getClass())) {

//		    logger.trace("{} component has children", configurable.getKey());

		    if (componentKey.oneLevelDown()) {
			//here the key is at configurable+1

//			logger.trace("One level down of component key goes to {}", componentKey);

			IGSConfigurable r = recursivePathComponentsRead(componentKey, (IGSConfigurableComposed) configurable);

			if (r != null)
			    return r;

			componentKey.oneLevelUp();

//			logger.trace("One level up of component key goes to {}", componentKey);
		    }

		}
	    }

//	    logger.trace("Path is not correct");

	}

	return null;
    }

    @Override
    public GSSource readGSSource(String uniqueIdentifier) throws GSException {

	return readInstantiableType(GSSource.class, uniqueIdentifier, new Deserializer());
    }

    @Override
    public List<GSSource> readGSSources() throws GSException {

	return readInstantiableType(GSSource.class, new Deserializer());

    }

}
