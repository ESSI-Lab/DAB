package eu.essi_lab.request.executor.storage;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.shared.SharedContent.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * @author Fabrizio
 */
public class LocalResultStorage extends ResultStorage {

    /**
     *
     */
    public LocalResultStorage() {
    }

    /**
     * @param resultStorageURI
     */
    public LocalResultStorage(DownloadSetting setting) {

	super(setting);
    }

    @Override
    public void store(String objectName, File file) throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Storing file {} STARTED", file.getAbsolutePath());

	String location = getResultStorageURI().getUri();

	File targetDir = new File(location + File.separator + SharedContentType.FILE_TYPE);

	if (!targetDir.exists()) {

	    boolean mkdirs = targetDir.mkdirs();

	    if (!mkdirs) {

		throw GSException.createException(//
			getClass(), //
			null, //
			"Unable to create path: " + targetDir, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			"DirectoryCreationError");
	    }
	}

	File targetFile = new File(location + File.separator + StringUtils.URLEncodeUTF8(objectName));

	GSLoggerFactory.getLogger(getClass()).trace("Writing content to: {}", targetFile);

	FileInputStream inputStream = new FileInputStream(file);

	try (FileOutputStream os = new FileOutputStream(targetFile)) {

	    IOUtils.copy(inputStream, os);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "LocalResultStorageError", //
		    e);
	} finally {

	    try {
		inputStream.close();
	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Storing file {} ENDED", file.getAbsolutePath());
    }

    @Override
    public String getStorageLocation(String objectName) {

	String location = getResultStorageURI().getUri();

	location = location + (location.endsWith(File.separator) || location.endsWith("/") ? "" : File.separator) + objectName;

	return location;
    }
}
