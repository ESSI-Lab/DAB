package eu.essi_lab.cdk;

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
import java.util.stream.Collectors;

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cdk.mixed.IMixedQueryConnector;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.pluggable.PluginsLoader;
public class ConnectorFactory {

    private static final ConnectorFactory INSTANCE = new ConnectorFactory();

    public static ConnectorFactory getInstance() {

	return INSTANCE;
    }

    private boolean disableSupportingTest = true;

    public void disableSupportingTest() {

	this.disableSupportingTest = true;
    }

    /**
     * @param source
     * @return
     */
    public List<IHarvestedQueryConnector> getHarvesterConnector(Source source) {

	PluginsLoader<IHarvestedQueryConnector> pluginsLoader = new PluginsLoader<>();
	List<IHarvestedQueryConnector> connectors = pluginsLoader.loadPlugins(IHarvestedQueryConnector.class);

	return connectors.stream().//
		filter(connector -> disableSupportingTest ? true : connector.supports(source)).//
		peek(connector -> {
		    connector.setSourceURL(source.getEndpoint());
		}).//
		collect(Collectors.toList());
    }

    /**
     * @param source
     * @return
     */
    public List<IDistributedQueryConnector> getDistributedConnector(Source source) {

	PluginsLoader<IDistributedQueryConnector> pluginsLoader = new PluginsLoader<>();
	List<IDistributedQueryConnector> connectors = pluginsLoader.loadPlugins(IDistributedQueryConnector.class);

	return connectors.stream().//
		filter(connector -> disableSupportingTest ? true : connector.supports(source)).//
		peek(connector -> connector.setSourceURL(source.getEndpoint())).//
		collect(Collectors.toList());
    }

    /**
     * @param source
     * @return
     */
    public List<IMixedQueryConnector> getMixedConnector(Source source) {

	PluginsLoader<IMixedQueryConnector> pluginsLoader = new PluginsLoader<>();
	List<IMixedQueryConnector> connectors = pluginsLoader.loadPlugins(IMixedQueryConnector.class);

	return connectors.stream().//
		filter(connector -> disableSupportingTest ? true : connector.supports(source)).//
		peek(connector -> connector.setSourceURL(source.getEndpoint())).//
		collect(Collectors.toList());
    }
}
