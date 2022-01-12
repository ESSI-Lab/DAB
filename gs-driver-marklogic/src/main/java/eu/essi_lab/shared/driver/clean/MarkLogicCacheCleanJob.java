package eu.essi_lab.shared.driver.clean;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
public class MarkLogicCacheCleanJob implements Runnable {

    private final Folder folder;
    private transient Logger logger = GSLoggerFactory.getLogger(getClass());

    private final Long retentionTimeInSeconds;

    public MarkLogicCacheCleanJob(Folder f, Long retentionInSeconds) {

	folder = f;

	retentionTimeInSeconds = retentionInSeconds;
    }

    @Override
    public void run() {

	logger.info("Start cleaning of MarkLogic Cache folder {} with retention time in seconds {}", folder.getURI(),
		retentionTimeInSeconds);

	int[] deleted = new int[] { 0 };

	try {

	    List<String> list = Arrays.asList(folder.listKeys());

	    int total = list.size();

	    list.forEach(key -> {

		try {

		    Date lastUpdated = folder.getBinaryLastUpdate(key);

		    logger.trace("{} was last updated at {}", key, lastUpdated);

		    if (new Date().getTime() - lastUpdated.getTime() > retentionTimeInSeconds * 1000L) {

			boolean removed = doRemove(key);

			if (removed)
			    deleted[0]++;
		    }
		} catch (Exception e) {

		    logger.warn("Can't read last modified of binary with key {}, trying to delete", key, e);

		    boolean removed = doRemove(key);

		    if (removed)
			deleted[0]++;

		}

	    });

	    logger.info("Completed cleaning of MarkLogic Cache folder {} with {}/{} deleted binaries", folder.getURI(), deleted[0], total);

	} catch (Exception e) {
	    logger.warn("Can't get keys of folder {}", folder.getURI(), e);
	}

    }

    private boolean doRemove(String key) {
	try {

	    logger.trace("Trying to remove {}", key);

	    folder.remove(key);

	    logger.trace("Successfully removed {}", key);

	    return true;

	} catch (Exception e1) {
	    logger.warn("Can't remove binary with key {}, trying to delete", key, e1);
	}

	return false;
    }

    public Folder getFolder() {
	return folder;
    }
}
