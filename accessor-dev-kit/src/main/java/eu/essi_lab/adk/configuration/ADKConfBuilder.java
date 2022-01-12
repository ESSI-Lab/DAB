package eu.essi_lab.adk.configuration;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class ADKConfBuilder implements Serializable {

    private static final String ERR_ID_CLASS_INTANTIATION_ERROR = "ERR_ID_CLASS_INTANTIATION_ERROR";
    private static final String ERR_ID_ADKBUILDER_SET_SUBCONPONENT_REFLECTION_EXCEPTION = "String ERR_ID_ADKBUILDER_SET_SUBCONPONENT_REFLECTION_EXCEPTION";

    @JsonIgnore
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public void addSubComponent(String subComponentOptionKey, String subComponentLabel, String subComponentClass,
	    IGSConfigurableComposed accessor) {

	((GSConfOptionSubcomponent) accessor.getSupportedOptions().get(subComponentOptionKey)).getAllowedValues().add(
		new Subcomponent(subComponentLabel, subComponentClass));

    }

    public Object getNewInstance(String clazz) throws GSException {
	try {

	    return Class.forName(clazz).newInstance();

	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

	    throw GSException.createException(this.getClass(), "Unable to instantiate class " + clazz, null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ERR_ID_CLASS_INTANTIATION_ERROR, e);
	}
    }

    public <T> T getNewInstance(String clazz, Class<T> valueType) throws GSException {

	return (T) getNewInstance(clazz);

    }

    public <T> T setSubComponent(IGSConfigurable configurable, IGSConfigurableComposed accessor) {

	//	configurable.setKey(accessor.getKey() + ":" + configurable.getKey());

	accessor.getConfigurableComponents().put(configurable.getKey(), configurable);

	return (T) configurable;

    }

    public <T> T onOptionSet(GSConfOptionSubcomponent co, IGSConfigurableComposed accessor, String setSubComponentMethodName,
	    Class<T> subComponentClass) throws GSException {

	String clazz = co.getValue().getValue();

	T inst = getNewInstance(clazz, subComponentClass);

	Object sub = setSubComponent((IGSConfigurable) inst, accessor);

	try {

	    Method method = accessor.getClass().getMethod(setSubComponentMethodName, subComponentClass);

	    method.invoke(accessor, sub);

	} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

	    logger.error(
		    "Error executing onOptionSet method for option " + co.getLabel() + " of configurable " + accessor.getLabel() + " " + "["
			    + accessor.getKey() + "]", e);

	    throw GSException.createException(this.getClass(), null, null, null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR,
		    ERR_ID_ADKBUILDER_SET_SUBCONPONENT_REFLECTION_EXCEPTION, e);

	}

	return (T) sub;

    }

    public <T> T readConfiguredComponent(IGSConfigurableComposed accessor, Class<T> clazz) {

	Map<String, IGSConfigurable> components = accessor.getConfigurableComponents();

	Iterator<IGSConfigurable> it = components.values().iterator();

	while (it.hasNext()) {

	    IGSConfigurable next = it.next();

	    if (clazz.isAssignableFrom(next.getClass()))
		return (T) next;

	}

	return null;
    }
}
