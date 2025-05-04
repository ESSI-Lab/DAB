package eu.essi_lab.lib.utils;

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

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * boldrini
 */
public class FileTrash {
    private static final Queue<File> fileQueue = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // Start the deletion task on class load
    static {
	executor.scheduleAtFixedRate(() -> {
	    File file;
	    while ((file = fileQueue.poll()) != null) {
		if (file.exists()) {
		    boolean deleted = file.delete();
		    if (!deleted) {
			GSLoggerFactory.getLogger(FileTrash.class).error("Warning: Failed to delete " + file.getAbsolutePath());
		    }
		}
	    }
	}, 0, 5, TimeUnit.SECONDS); // Check every 5 seconds
    }

    /**
     * Adds a file to the asynchronous delete queue.
     *
     * @param file File to delete
     */
    public static void deleteLater(File file) {
	if (file != null) {
	    fileQueue.add(file);
	}
    }

    /**
     * Shuts down the background thread gracefully.
     */
    public static void shutdown() {
	executor.shutdown();
    }
}
