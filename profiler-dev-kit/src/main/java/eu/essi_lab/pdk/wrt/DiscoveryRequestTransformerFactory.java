package eu.essi_lab.pdk.wrt;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.pluggable.Provider;

/**
 * A factory for {@link DiscoveryRequestTransformer}s.<br>
 * In order to be loaded, the {@link DiscoveryRequestTransformer}s plugins must be installed.
 * See {@link PluginsLoader} for more info
 *
 * @see Pluggable
 * @see PluginsLoader
 * @author Fabrizio
 */
public class DiscoveryRequestTransformerFactory {

    /**
     * @return
     */
    public static List<DiscoveryRequestTransformer> loadTransformers() {

	PluginsLoader<DiscoveryRequestTransformer> pluginsLoader = new PluginsLoader<>();
	return pluginsLoader.loadPlugins(DiscoveryRequestTransformer.class);
    }

    /**
     * @param provider
     * @return
     */
    public static List<DiscoveryRequestTransformer> loadTransformers(Provider provider) {

	PluginsLoader<DiscoveryRequestTransformer> pluginsLoader = new PluginsLoader<>();
	List<DiscoveryRequestTransformer> transformers = pluginsLoader.loadPlugins(DiscoveryRequestTransformer.class);

	return transformers.stream().//
		filter(transformer -> transformer.getProvider().equals(provider)).//
		collect(Collectors.toList());
    }

    /**
     * @param provider
     * @param profilerType
     * @return
     */
    public static List<DiscoveryRequestTransformer> loadTransformers(Provider provider, String profilerType) {

	PluginsLoader<DiscoveryRequestTransformer> pluginsLoader = new PluginsLoader<>();
	List<DiscoveryRequestTransformer> transformers = pluginsLoader.loadPlugins(DiscoveryRequestTransformer.class);

	return transformers.stream().//
		filter(transformer -> { //

		    Provider prov = transformer.getProvider();
		    String prof = transformer.getProfilerType();

		    return ((provider == null || (provider != null && prov.equals(provider))) && prof.equals(profilerType));

		}).collect(Collectors.toList());
    }

    /**
     * @param profilerType
     * @return
     */
    public static List<DiscoveryRequestTransformer> loadTransformers(String profilerType) {

	return loadTransformers(null, profilerType);
    }
}
