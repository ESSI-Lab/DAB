package eu.essi_lab.model.pluggable;

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
public class PluginsLoader<T extends Pluggable> {

    private static File pluginsFolder;

    /**
     * Creates a new plugins loader with the new plugins folder "GS-Service classpath/plugins"
     */
    public PluginsLoader() {

	URL pluginUrl = getClass().getClassLoader().getResource("plugins/plugin");
	if (pluginUrl == null) {
	    // this happens during the tests, when the GS-service plugins/plugin folder is not visible
	    return;
	}
	String pluginPath = null;
	try {
	    pluginPath = URLDecoder.decode(pluginUrl.getPath(), "UTF-8");
	} catch (UnsupportedEncodingException e) {
	}

	File pluginFile = new File(pluginPath);
	pluginsFolder = new File(pluginFile.getParent());
    }

    /**
     * Set the folder for the new plugins, that is plugin installed after the JVM bootstrap
     * 
     * @param pluginsFolder
     */
    public static void setNewPluginsFolder(File folder) {

	pluginsFolder = folder;
	GSLoggerFactory.getLogger(PluginsLoader.class).info("New plugins folder set: " + pluginsFolder.getAbsolutePath());
    }

    /**
     * Loads preinstalled plugins and new plugins of the supplied <code>type</code>
     * 
     * @param type the plugin type
     * @return a {@link List} of plugins of the supplied <code>type</code>
     */
    public List<T> loadPlugins(Class<T> type) {

	List<URL> newPlugins = new ArrayList<>();

	if (pluginsFolder != null) {
	    File[] listFiles = pluginsFolder.listFiles();
	    if (listFiles != null) {
		newPlugins = Arrays.asList(pluginsFolder.listFiles()).//
			stream().//
			filter(file -> file.getPath().toLowerCase().endsWith(".jar")).//
			map(file -> toURL(file)).//
			filter(Objects::nonNull).//
			collect(Collectors.toList());
	    }
	}

	URLClassLoader ucl = new URLClassLoader(//
		newPlugins.toArray(new URL[] {}), //
		Thread.currentThread().getContextClassLoader());
	ServiceLoader<T> loader = ServiceLoader.load(type, ucl);

	ArrayList<T> out = new ArrayList<T>();
	loader.forEach(out::add);

	// GSLoggerFactory.getLogger(getClass()).info("Loaded plugins of type: " + type.getCanonicalName());
	// GSLoggerFactory.getLogger(getClass()).info("New: " + newPlugins.size());
	// GSLoggerFactory.getLogger(getClass()).info("Pre installed: " + (out.size() - newPlugins.size()));

	return out;
    }

    private URL toURL(File file) {
	try {
	    return file.toURI().toURL();
	} catch (MalformedURLException e) {
	}
	return null;
    }
}
