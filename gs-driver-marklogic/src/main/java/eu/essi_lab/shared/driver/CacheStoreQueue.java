package eu.essi_lab.shared.driver;

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

import eu.essi_lab.api.database.internal.Folder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CacheStoreQueue {

    private static final int TRHREAD_POOL_SIZE = 10;
    private final ExecutorService executor;

    public CacheStoreQueue() {

	executor = Executors.newFixedThreadPool(TRHREAD_POOL_SIZE);

    }

    public Boolean toCache(String identifier, SharedContent sharedContent, IGSScharedContentSerializer serializer, Folder folder) {

	executor.submit(() -> {
	    try {

		folder.storeBinary(identifier, serializer.toStream(sharedContent), new Date());

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(CacheStoreQueue.class).error("Can't store binary to folder {}", folder.getURI(), e);
	    }

	});

	return true;
    }

}
