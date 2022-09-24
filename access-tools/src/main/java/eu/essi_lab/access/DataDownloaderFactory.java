package eu.essi_lab.access;

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

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.resource.GSResource;

public class DataDownloaderFactory {

    private DataDownloaderFactory() {
	// private constructor to hide implicit public one
    }

    public static DataDownloader getDataDownloader(GSResource resource, String onlineResourceId) {

	PluginsLoader<DataDownloader> pluginsLoader = new PluginsLoader<>();
	List<DataDownloader> downloaders = pluginsLoader.loadPlugins(DataDownloader.class);

	for (DataDownloader downloader : downloaders) {
	    try {
		downloader.setOnlineResource(resource, onlineResourceId);

		if (downloader.canDownload()) {
		    return downloader;
		}

	    } catch (GSException e) {

		e.log();

	    }
	}

	return null;

    }

}
