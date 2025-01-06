package eu.essi_lab.pdk.rsf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
 * A factory for {@link MessageResponseFormatter}s.<br>
 * In order to be loaded, the {@link DiscoveryRequestTransformer}s plugins must be installed.
 * See {@link PluginsLoader} for more info
 *
 * @see Pluggable
 * @see PluginsLoader
 * @author Fabrizio
 */
public class DiscoveryResultSetFormatterFactory {

    /**
     * @param targetClass
     * @return
     */
    public static <T> List<DiscoveryResultSetFormatter<T>> loadFormatters(Class<T> targetClass) {

	return loadFormatters(null, null, targetClass);

    }

    /**
     * @param encoding
     * @param targetClass
     * @return
     */
    public static <T> List<DiscoveryResultSetFormatter<T>> loadFormatters(FormattingEncoding encoding, Class<T> targetClass) {

	return loadFormatters(null, encoding, targetClass);
    }

    /**
     * @param provider
     * @param encoding
     * @param targetClass
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> List<DiscoveryResultSetFormatter<T>> loadFormatters(Provider provider, FormattingEncoding encoding,
	    Class<T> targetClass) {

	PluginsLoader<DiscoveryResultSetFormatter> pluginsLoader = new PluginsLoader<>();
	List<DiscoveryResultSetFormatter> formatters = pluginsLoader.loadPlugins(DiscoveryResultSetFormatter.class);

	ArrayList<DiscoveryResultSetFormatter<T>> out = Lists.newArrayList();

	formatters.forEach(formatter -> {

	    Type type = getActualParameterizedType(formatter);

	    if (type.equals(targetClass)) {

		Provider prov = formatter.getProvider();
		FormattingEncoding enc = formatter.getEncoding();

		if ((provider == null || (provider != null && prov.equals(provider))) //
			&& (enc == null || enc.equals(encoding))) {

		    out.add(formatter);
		}
	    }
	});

	return out;
    }

    @SuppressWarnings("rawtypes")
    private static Type getActualParameterizedType(MessageResponseFormatter formatter) {

	Class current = formatter.getClass();
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
