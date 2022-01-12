package eu.essi_lab.gssrv.starter;

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

import java.util.HashMap;
import java.util.Map;

import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSource;
import eu.essi_lab.model.exceptions.GSException;

public class BrokeredSourceConfigurableComposed extends AbstractGSconfigurableComposed implements IGSMainConfigurable {
    private static final long serialVersionUID = 344198339061522792L;

    private Map<String, GSConfOption<?>> supported = new HashMap<>();

    public BrokeredSourceConfigurableComposed() {

	setLabel("Brokered Sources");
	setKey(GSConfiguration.BROKERED_SOURCES_KEY);
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

	if (option instanceof GSConfOptionSource) {

	    Source source = ((GSConfOptionSource) option).getValue();

	    BrokeredSourceConfigurable acc = createBrokeredSourceConfigurable(source);

	    executeOnOptionSet(option,  acc);
	}
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here
    }

    BrokeredSourceConfigurable createBrokeredSourceConfigurable(Source s) {

	BrokeredSourceConfigurable acc = new BrokeredSourceConfigurable();

	acc.setKey(s.getUniqueIdentifier() + ":" + acc.getKey());

	return acc;
    }

    void executeOnOptionSet(GSConfOption<?> option,  BrokeredSourceConfigurable acc) throws GSException {

	acc.setOption(option);

	getConfigurableComponents().put(acc.getKey(), acc);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return supported;
    }

}
