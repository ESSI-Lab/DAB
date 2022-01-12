package eu.essi_lab.shared.configuration;

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

import eu.essi_lab.model.configuration.option.GSConfOptionString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
import eu.essi_lab.shared.model.IGSSharedContentCategory;
public class SharedContentConfiguration extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    public static final String CATEGORY_COMPONENT_KEY_SUFFIX = "_catComponentKey";
    public static final String SHARED_REPOSITORY_CONFIRATION_KEY = "SHARED_REPOSITORY_CONFIRATION_KEY";
    private static final String CATEGORY_OPTION_KEY_SUFFIX = "_catOptionKey";
    private static final String ERR_ID_CLASS_INTANTIATION_ERROR = "ERR_ID_CLASS_INTANTIATION_ERROR";
    private Map<String, GSConfOption<?>> supported;
    private static final String ERR_ID_CATEGORY_NOT_FOUND_ERROR = "ERR_ID_CATEGORY_NOT_FOUND_ERROR";

    public SharedContentConfiguration() {

	setKey(SHARED_REPOSITORY_CONFIRATION_KEY);
	setLabel("Shared Repository Configuration");

	supported = new HashMap<>();

	ServiceLoader<IGSSharedContentCategory> ll = ServiceLoader.load(IGSSharedContentCategory.class);

	Iterator<IGSSharedContentCategory> it = ll.iterator();

	while (it.hasNext()) {

	    IGSSharedContentCategory category = it.next();

	    GSConfOptionSubcomponent opt = createCategoryOption(category);

	    getSupportedOptions().put(opt.getKey(), opt);

	}

    }

    private String createCategoryOptionKey(IGSSharedContentCategory category) {
	return category.getType() + CATEGORY_OPTION_KEY_SUFFIX;
    }

    private String createCategoryComponentKey(IGSSharedContentCategory category) {
	return category.getType() + CATEGORY_COMPONENT_KEY_SUFFIX;
    }

    private GSConfOptionSubcomponent createCategoryOption(IGSSharedContentCategory category) {

	GSConfOptionSubcomponent o = new GSConfOptionSubcomponent();

	o.setKey(createCategoryOptionKey(category));

	o.setLabel(category.getName());

	List<ISharedRepositoryDriver> drivers = category.getAvailableDrivers();

	List<Subcomponent> alowed = new ArrayList<>();

	for (ISharedRepositoryDriver driver : drivers) {

	    String driverLabel = driver.getClass().getName();
	    String clazz = driver.getClass().getName();

	    if (driver.getLabel() != null && !"".equalsIgnoreCase(driver.getLabel()))
		driverLabel = driver.getLabel();

	    Subcomponent subcomponent = new Subcomponent(driverLabel, clazz);

	    alowed.add(subcomponent);

	}

	o.setAllowedValues(alowed);
	return o;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	IGSSharedContentCategory category = getCategory(opt);

	if (category == null)
	    throw GSException.createException(this.getClass(), "Category not found", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_CATEGORY_NOT_FOUND_ERROR);

	Subcomponent subcomponent = (Subcomponent) opt.getValue();

	String clazz = subcomponent.getValue();

	try {

	    ISharedRepositoryDriver driver = instantiateDriverByClass(clazz);

	    String compKey = createCategoryComponentKey(category);

	    getConfigurableComponents().remove(compKey);

	    getConfigurableComponents().put(compKey, driver);

	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

	    throw GSException.createException(this.getClass(), "Unable to instantiate class " + clazz, null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, ERR_ID_CLASS_INTANTIATION_ERROR, e);
	}
    }

    ISharedRepositoryDriver instantiateDriverByClass(String clazz)
	    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
	return (ISharedRepositoryDriver) Class.forName(clazz).newInstance();
    }

    private IGSSharedContentCategory getCategory(GSConfOption<?> opt) {

	String optKey = opt.getKey();

	String type = optKey.replace(CATEGORY_OPTION_KEY_SUFFIX, "");

	ServiceLoader<IGSSharedContentCategory> categories = ServiceLoader.load(IGSSharedContentCategory.class);

	for (IGSSharedContentCategory category : categories) {

	    if (type.equals(category.getType()))
		return category;

	}

	return null;
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here

    }

    public ISharedRepositoryDriver getDriver(IGSSharedContentCategory contentCategory) {

	return (ISharedRepositoryDriver) getConfigurableComponents().get(createCategoryComponentKey(contentCategory));

    }

    @Override
    public IGSConfigurationInstantiable getInstantiableType() {

	InstantiableSharedRepositoryInfo info = new InstantiableSharedRepositoryInfo();

	info.setSharedContentConfiguration(this);

	return info;
    }
}
