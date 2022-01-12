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
import eu.essi_lab.shared.model.SharedContentType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
public class PersistentDriverFSInit {

    private static PersistentDriverFSInit instance;
    private static final String PREFIX = "PersistentDriverDriectory-";

    private final File rootDirectory;
    private final Map<String, File> typeDirectoryMap = new HashMap<>();

    private static final String LOCAL_PERSISTENT_DRIVER_DIR_INIT_ERROR = "LOCAL_PERSISTENT_DRIVER_DIR_INIT_ERROR" + "";

    private PersistentDriverFSInit() throws IOException {

	rootDirectory = initialize();

	GSLoggerFactory.getLogger(PersistentDriverFSInit.class).info("Persistent Local FS Driver initialized with directory {}",
		rootDirectory.getAbsolutePath());
    }

    public static PersistentDriverFSInit getInstance() throws GSException {
	if (instance == null) {
	    try {
		instance = new PersistentDriverFSInit();
	    } catch (IOException e) {

		GSLoggerFactory.getLogger(PersistentDriverFSInit.class).error("Error initializing directory", e);

		throw GSException.createException(PersistentDriverFSInit.class, "Can't initilize directory", null,
			ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, LOCAL_PERSISTENT_DRIVER_DIR_INIT_ERROR, e);
	    }
	}

	return instance;
    }

    public File getDirectory(SharedContentType type) {

	if (typeDirectoryMap.get(type.getType()) == null)
	    typeDirectoryMap.put(type.getType(), initDir(type.getType()));

	return typeDirectoryMap.get(type.getType());
    }

    private File initDir(String dir) {

	try {
	    return new File(Files.createDirectory(Paths.get(rootDirectory.toURI()).resolve(dir)).toString());
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(PersistentDriverFSInit.class).error("Can't init directory for type {}", dir);
	}

	return null;
    }

    private File initialize() throws IOException {

	return new File(Files.createTempDirectory(PREFIX, new FileAttribute[] {}).toString());
    }

}
