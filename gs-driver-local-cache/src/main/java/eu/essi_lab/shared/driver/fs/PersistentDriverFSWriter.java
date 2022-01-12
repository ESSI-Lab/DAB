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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
public class PersistentDriverFSWriter {

    private final File persistentFilesDirectory;

    private transient Logger logger = GSLoggerFactory.getLogger(PersistentDriverFSWriter.class);
    private static final String LOCAL_PERSISTENT_DRIVER_FS_WRITER_WRITE_ERROR = "LOCAL_PERSISTENT_DRIVER_FS_WRITER_WRITE_ERROR";

    public PersistentDriverFSWriter(File rootDir) {
	persistentFilesDirectory = rootDir;
    }

    public Path createFilePath(String id) {

	return Paths.get(persistentFilesDirectory.toURI()).resolve(id);

    }

    Path createFile(Path path) throws IOException {

        if (Files.exists(path)){

            return path;


	}

	return Files.createFile(path);
    }

    /**
     * Writes the file with file name identifier, returns the written file. Throws a GSException if the write fails.
     *
     * @param identifier the file name
     * @param stream the content to be written
     * @return the written file
     * @throws eu.essi_lab.model.exceptions.GSException if the write fails
     */
    public File write(String identifier, InputStream stream) throws GSException {

	Path path = createFilePath(identifier);

	try (FileOutputStream os = new FileOutputStream(new File(createFile(path).toUri()))) {

	    IOUtils.copy(stream, os);

	} catch (IOException e) {

	    logger.error("Exception writing {}", path);

	    throw GSException.createException(PersistentDriverFSWriter.class, "Can't write file " + path, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, LOCAL_PERSISTENT_DRIVER_FS_WRITER_WRITE_ERROR, e);
	}

	logger.trace("Successfully written {}", path);

	return new File(path.toUri());

    }
}
