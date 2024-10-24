package eu.essi_lab.profiler.arpa;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.Profiler;

public class HydroCSVUtils {
    private static String wpsPath = null;

    public static String getWPSPath() {
	if (wpsPath == null) {
	    PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	    List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	    for (Profiler profiler : profilers) {
		String className = profiler.getClass().getSimpleName();
		if (className != null && className.equals("GWPSProfiler")) {
		    wpsPath = profiler.getSetting().getServicePath();
		}
	    }
	}
	return wpsPath;

    }

    private static String hydroServerPath = null;

    public static String getHydroServerPath() {
	if (hydroServerPath == null) {
	    PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	    List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	    for (Profiler profiler : profilers) {
		String className = profiler.getClass().getSimpleName();
		if (className != null && className.equals("HydroServerProfiler")) {
		    hydroServerPath = profiler.getSetting().getServicePath();
		}
	    }
	}
	return hydroServerPath;

    }

    private static String hisCentralPath = null;

    public static String getHISCentralPath() {
	if (hisCentralPath == null) {
	    PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	    List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	    for (Profiler profiler : profilers) {
		String className = profiler.getClass().getSimpleName();
		if (className != null && className.equals("HISCentralProfiler")) {
		    hisCentralPath = profiler.getSetting().getServicePath();
		}
	    }
	}
	return hisCentralPath;

    }

    private static String hydroCSVPath = null;

    public static String getHydroCSVPath() {
	if (hydroCSVPath == null) {
	    PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	    List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	    for (Profiler profiler : profilers) {
		String className = profiler.getClass().getSimpleName();
		if (className != null && className.equals("HydroCSVProfiler")) {
		    hydroCSVPath = profiler.getSetting().getServicePath();
		}
	    }
	}
	return hydroCSVPath;

    }
    
    private static String sosPath = null;

    public static String getSOSPath() {
	if (sosPath == null) {
	    PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	    List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	    for (Profiler profiler : profilers) {
		String className = profiler.getClass().getSimpleName();
		if (className != null && className.equals("SOSProfiler")) {
		    sosPath = profiler.getSetting().getServicePath();
		}
	    }
	}
	return sosPath;

    }
}
