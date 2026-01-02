package eu.essi_lab.shared.driver;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.serializer.SharedContentSerializer;
import eu.essi_lab.shared.serializer.SharedContentSerializers;

/**
 * This implements the driver for using local fs as the shared repository of category persistent. Objects are stored
 * into a directory. This
 * implementation of the interface {@link ISharedPersistentRepositoryDriver} does not provide the implementation of the
 * method {@link
 * ISharedPersistentRepositoryDriver#read(SharedContentType, SharedContentQuery)}
 *
 * @author ilsanto
 */
public class LocalPersistentDriver implements ISharedRepositoryDriver<SharedPersistentDriverSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "LocalPersistentDriver";

    private static final String METHOD_NOT_IMPLEMENTED_ERR_ID = "METHOD_NOT_IMPLEMENTED_ERR_ID";
    private static final String SERIALIZER_NOT_FOUND = "SERIALIZER_NOT_FOUND";

    private static final String LOCAL_PERSISTENT_DRIVER_WRITE_ERROR = "LOCAL_PERSISTENT_DRIVER_WRITE_ERROR";

    private static final String LOCAL_PERSISTENT_DRIVER_READ_ERROR = "LOCAL_PERSISTENT_DRIVER_READ_ERROR";

    private SharedPersistentDriverSetting setting;

    /**
     * 
     */
    public LocalPersistentDriver() {

	this.setting = new SharedPersistentDriverSetting();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized void store(SharedContent sharedContent) throws GSException {

	SharedContentSerializer serializer = SharedContentSerializers.getSerializer(sharedContent.getType());

	if (serializer == null) {

	    throw GSException.createException(//
		    getClass(), //
		    null, //
		    "Serializer not found for: " + sharedContent.getType(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SERIALIZER_NOT_FOUND);
	}

	GSLoggerFactory.getLogger(getClass()).trace("Using serializer {}", serializer.getClass());

	InputStream stream = serializer.toStream(sharedContent);

	String path = getSetting().getLocalPersistentSetting().get().getFolderPath();

	path = path + File.separator + sharedContent.getType();

	File folder = new File(path);

	if (!folder.exists()) {

	    boolean mkdirs = folder.mkdirs();

	    if (!mkdirs) {

		throw GSException.createException(//
			getClass(), //
			null, "Unable to create path: " + folder, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SERIALIZER_NOT_FOUND);
	    }
	}

	path = path + File.separator + sharedContent.getIdentifier();

	File file = new File(path);

	GSLoggerFactory.getLogger(getClass()).trace("Writing content to {} STARTED", path);

	try (FileOutputStream os = new FileOutputStream(file)) {

	    IOUtils.copy(stream, os);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LOCAL_PERSISTENT_DRIVER_WRITE_ERROR, //
		    e);
	} finally {

	    try {
		stream.close();
	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Writing content to {} ENDED", path);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public synchronized SharedContent read(String identifier, SharedContentType type) throws GSException {

	String path = getSetting().getLocalPersistentSetting().get().getFolderPath();

	path = path + File.separator + type;

	File folder = new File(path);

	if (!folder.exists()) {

	    boolean mkdirs = folder.mkdirs();

	    if (!mkdirs) {

		throw GSException.createException(//
			getClass(), //
			null, "Unable to create path: " + folder, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SERIALIZER_NOT_FOUND);
	    }
	}

	path = path + File.separator + identifier;

	File file = new File(path);

	if (!file.exists()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Resource {} not found", file);

	    return null;
	}

	SharedContent content = null;
	SharedContentSerializer serializer = SharedContentSerializers.getSerializer(type);

	if (serializer == null) {

	    throw GSException.createException(//
		    getClass(), //
		    null, "Serializer not found for: " + type, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SERIALIZER_NOT_FOUND);
	}

	GSLoggerFactory.getLogger(getClass()).trace("Using serializer {}", serializer.getClass());

	try {

	    GSLoggerFactory.getLogger(getClass()).trace("Reading content from {} STARTED", path);

	    InputStream stream = new FileInputStream(file);

	    content = serializer.fromStream(identifier, stream);

	    GSLoggerFactory.getLogger(getClass()).trace("Reading content from {} ENDED", path);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    LOCAL_PERSISTENT_DRIVER_READ_ERROR, //
		    e);

	}

	return content;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized List<SharedContent> read(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(//
		this.getClass(), //
		"Method not implemented", //
		null, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_WARNING, //
		METHOD_NOT_IMPLEMENTED_ERR_ID);
    }

    @Override
    public synchronized Long count(SharedContentType type) throws GSException {

	String path = getSetting().getLocalPersistentSetting().get().getFolderPath();

	path = path + File.separator + type;

	File file = new File(path);

	return (long) file.listFiles().length;
    }

    @Override
    public void configure(SharedPersistentDriverSetting setting) {

	this.setting = setting;
    }

    @Override
    public SharedPersistentDriverSetting getSetting() {

	return this.setting;
    }

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }

    @Override
    public SharedContentCategory getCategory() {

	return SharedContentCategory.LOCAL_PERSISTENT;
    }
}
