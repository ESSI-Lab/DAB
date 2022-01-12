package eu.essi_lab.model.configuration;

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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public abstract class AbstractGSconfigurableComposed extends AbstractGSconfigurable implements IGSConfigurableComposed {

    private static final long serialVersionUID = -8578392127275245524L;
    private Map<String, IGSConfigurable> configurables = new LinkedHashMap<>();

    public AbstractGSconfigurableComposed() {
	super();
    }

    @Override
    @JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "configurableComponentConcrete")
    public Map<String, IGSConfigurable> getConfigurableComponents() {

	return configurables;
    }

    @Override
    public String toString() {
	String gsConfigurable = super.toString();

	String configurableStr = configurables == null ? "none" : configurables.toString();
	String ret = gsConfigurable + " Configurables: " + configurableStr;

	return ret;
    }

}
