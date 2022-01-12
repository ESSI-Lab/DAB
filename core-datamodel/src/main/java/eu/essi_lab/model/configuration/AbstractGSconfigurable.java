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

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.GSException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractGSconfigurable extends GSJSONSerializable implements IGSConfigurable {

    private static final long serialVersionUID = -1631271676438729747L;
    private String key;
    private String label;

    private IGSConfigurationInstantiable instantiableType;

    public AbstractGSconfigurable() {
	setKey(UUID.randomUUID().toString());
    }

    @Override
    public GSConfOption<?> read(String key) {

	return getSupportedOptions().get(key);

    }

    @Override
    public void setSupportedOptions(Map<String, GSConfOption<?>> opts) {
	Map<String, GSConfOption<?>> supp = getSupportedOptions();

	opts.entrySet().stream().forEach(o -> {

	    if (supp == null)
		return;

	    if (supp.get(o.getKey()) == null)
		return;

	    if (!(supp.get(o.getKey()) instanceof GSConfOptionSubcomponent))
		return;

	    GSConfOptionSubcomponent oldopt = (GSConfOptionSubcomponent) o.getValue();

	    GSConfOptionSubcomponent newopt = (GSConfOptionSubcomponent) supp.get(o.getKey());

	    for (Subcomponent newsubcompnent : newopt.getAllowedValues()) {

		if (!oldopt.getAllowedValues().contains(newsubcompnent))
		    oldopt.getAllowedValues().add(newsubcompnent);

	    }

	});

	if (supp != null) {

	    supp.entrySet().stream().forEach(suppo -> {

		if (opts.get(suppo.getKey()) == null)
		    opts.put(suppo.getKey(), suppo.getValue());

	    });
	}

	getSupportedOptions().clear();

	opts.entrySet().stream().forEach(o -> getSupportedOptions().put(o.getKey(), o.getValue()));

    }

    @Override
    public boolean setOption(GSConfOption<?> opt) throws GSException {

	boolean ret = false;

	if (getSupportedOptions().containsKey(opt.getKey())) {

	    getSupportedOptions().put(opt.getKey(), opt);

	    ret = true;

	    onOptionSet(opt);
	}

	return ret;

    }

    @Override
    public String getLabel() {

	return label;
    }

    @Override
    public void setLabel(String l) {
	label = l;
    }

    @Override
    public String getKey() {

	return key;
    }

    @Override
    public void setKey(String k) {

	key = k;

    }
    @Override
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "instanceTypeConcrete")
    public IGSConfigurationInstantiable getInstantiableType() {
	return instantiableType;
    }

    @Override
    public void setInstantiableType(IGSConfigurationInstantiable instantiableType) {
	this.instantiableType = instantiableType;
    }

    @Override
    public void onStartUp() throws GSException {
	onFlush();
    }

    @Override
    public String toString() {
	String it = instantiableType == null ? "none" : instantiableType.getClass().getSimpleName() + " " + instantiableType.toString();
	return "key: " + key + " label: " + label + " instantiableType: " + it;
    }

}
