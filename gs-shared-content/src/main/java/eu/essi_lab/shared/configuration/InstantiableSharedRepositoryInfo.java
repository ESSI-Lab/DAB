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

import java.io.InputStream;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.exceptions.GSException;
public class InstantiableSharedRepositoryInfo extends SharedRepositoryInfo implements IGSConfigurationInstantiable {

    @XmlTransient
    @JsonIgnore
    private  transient IGSConfigurableComposed sharedRepoConfigurable;
    private String componentId;

    public InstantiableSharedRepositoryInfo() {
	super();
    }

    @XmlTransient
    @JsonIgnore
    public IGSConfigurableComposed getSharedContentConfiguration() {
	return sharedRepoConfigurable;
    }

    @JsonIgnore
    public void setSharedContentConfiguration(IGSConfigurableComposed conf) {
	this.sharedRepoConfigurable = conf;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public String getId() {
	//TODO
	return null;
    }

    @Override
    public String getComponentId() {

	if (sharedRepoConfigurable != null)

	    return sharedRepoConfigurable.getKey();

	return componentId;
    }

    @Override
    public void setComponentId(String key) {
	componentId = key;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public void setComponent(IGSConfigurable configurable) {
	sharedRepoConfigurable = (IGSConfigurableComposed) configurable;
    }

    @Override
    public <T> T serializeTo(Class<T> clazz) throws GSException {

	if (String.class.isAssignableFrom(clazz))
	    return (T) this.serialize();

	if (InputStream.class.isAssignableFrom(clazz))
	    return (T) this.serializeToInputStream();

	return null;
    }
}
