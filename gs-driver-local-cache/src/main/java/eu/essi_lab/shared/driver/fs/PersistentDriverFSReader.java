package eu.essi_lab.shared.driver.fs;

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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
public class PersistentDriverFSReader {

    private final File persistentFilesDirectory;

    private transient Logger logger = GSLoggerFactory.getLogger(PersistentDriverFSReader.class);

    public PersistentDriverFSReader(File rootDir) {
	persistentFilesDirectory = rootDir;
    }

    /**
     * Reads the file with file name identifier, returns null if the file is not found.
     *
     * @param identifier
     * @return
     */
    public File read(String identifier) {

	File[] files = new File[] { null };

	Optional.of(persistentFilesDirectory.list((dir, name) -> name.equals(identifier))).ifPresent(list -> {
	    if (list.length > 0)
		files[0] = new File(Paths.get(persistentFilesDirectory.toURI()).resolve(list[0]).toUri());
	});

	if (logger.isTraceEnabled())
	    logger.trace("Requested file {} was{} found", identifier, (files[0] != null ? "" : " not"));

	return files[0];

    }

}
