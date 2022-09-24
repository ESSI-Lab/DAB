package eu.essi_lab.pdk.rsm;

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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * A factory for {@link DiscoveryResultSetMapper}s.<br>
 * In order to be loaded, the {@link DiscoveryRequestTransformer}s plugins must be installed.
 * See {@link PluginsLoader} for more info
 *
 * @see Pluggable
 * @see PluginsLoader
 * @author Fabrizio
 */
public class DiscoveryResultSetMapperFactory {

    /**
     * @param targetClass
     * @return
     */
    public static <T> List<DiscoveryResultSetMapper<T>> loadMappers(Class<T> targetClass) {

	return loadMappers(null, null, targetClass);
    }

    /**
     * @param provider
     * @param targetClass
     * @return
     */
    public static <T> List<DiscoveryResultSetMapper<T>> loadMappers(Provider provider, Class<T> targetClass) {

	return loadMappers(provider, null, targetClass);
    }

    /**
     * @param encoding
     * @param targetClass
     * @return
     */
    public static <T> List<DiscoveryResultSetMapper<T>> loadMappers(MappingSchema encoding, Class<T> targetClass) {

	return loadMappers(null, encoding, targetClass);
    }

    /**
     * @param provider
     * @param encoding
     * @param targetClass
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<DiscoveryResultSetMapper<T>> loadMappers(Provider provider, MappingSchema encoding, Class<T> targetClass) {

	PluginsLoader<DiscoveryResultSetMapper> pluginsLoader = new PluginsLoader<>();
	List<DiscoveryResultSetMapper> mappers = pluginsLoader.loadPlugins(DiscoveryResultSetMapper.class);

	ArrayList<DiscoveryResultSetMapper<T>> out = Lists.newArrayList();

	mappers.forEach(mapper -> {

	    Type type = getActualParameterizedType(mapper);

	    if (type.equals(targetClass)) {

		if ((provider == null || (provider != null && mapper.getProvider().equals(provider))) && //
		(encoding == null || mapper.getMappingSchema().equals(encoding))) {
		    out.add(mapper);
		}
	    }
	});

	return out;
    }

    @SuppressWarnings("rawtypes")
    private static Type getActualParameterizedType(DiscoveryResultSetMapper mapper) {

	Class current = mapper.getClass();
	Type genericSuperclass = current.getGenericSuperclass();

	while (!(genericSuperclass instanceof ParameterizedType)) {
	    current = current.getSuperclass();
	    genericSuperclass = current.getGenericSuperclass();
	}

	ParameterizedType eventHandlerInterface = (ParameterizedType) genericSuperclass;
	Type type = eventHandlerInterface.getActualTypeArguments()[0];
	return type;
    }
}
