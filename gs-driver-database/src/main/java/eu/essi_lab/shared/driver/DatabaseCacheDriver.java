package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.serializer.SharedContentSerializer;
import eu.essi_lab.shared.serializer.SharedContentSerializers;

/**
 * @author ilsanto
 */
public class DatabaseCacheDriver implements ISharedRepositoryDriver<SharedCacheDriverSetting> {

    static final String DRIVER_TYPE = "DatabaseCache";

    private static final int TRHREAD_POOL_SIZE = 10;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(TRHREAD_POOL_SIZE);

    private static final String MLCACHE_READ_ERROR = "MLCACHE_READ_ERROR";

    private static final String MLCACHE_UNSUPPORTED_OPERATION_ERR = "MLCACHE_UNSUPPORTED_OPERATION_ERROR";

    private static final String MLCACHE_FOLDER_STORE_ERR = "MLCACHE_FOLDER_STORE_ERROR";

    private static final String MLCACHE_SHARED_CONTENT_SERIALIZER_NOT_FOUND = "MLCACHE_SHARED_CONTENT_SERIALIZER_NOT_FOUND";

    private SharedCacheDriverSetting setting;

    public DatabaseCacheDriver() {

	super();

	setting = new SharedCacheDriverSetting();
    }

    /**
     * @param setting
     */
    public DatabaseCacheDriver(SharedCacheDriverSetting setting) {

	super();

	configure(setting);
    }

    @Override
    public void configure(SharedCacheDriverSetting setting) {

	this.setting = setting;
    }

    @Override
    public SharedCacheDriverSetting getSetting() {

	return setting;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized SharedContent read(String identifier, SharedContentType type) throws GSException {

	SharedContent out = null;

	try {

	    Database database = getDatabase();

	    Optional<DatabaseFolder> optFolder = database.getFolder(Database.CACHE_FOLDER, true);

	    if (optFolder.isPresent()) {

		String id = contentIdentifier(identifier, type);

		InputStream binary = optFolder.get().getBinary(id);

		if (binary == null) {

		    return null;
		}

		SharedContentSerializer serializer = getSerializerOrThrowEx(type);

		out = serializer.fromStream(null, binary);

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("MarkLogic cache folder missing");
	    }
	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during MarkLogic shared content reading");

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MLCACHE_READ_ERROR, //
		    e);
	}

	return out;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized List<SharedContent> read(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(//
		getClass(), //
		"Operation not supported", //
		null, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_WARNING, //
		MLCACHE_UNSUPPORTED_OPERATION_ERR);

    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized void store(SharedContent sharedContent) throws GSException {

	SharedContentSerializer serializer = getSerializerOrThrowEx(sharedContent.getType());

	try {

	    Database database = getDatabase();

	    Optional<DatabaseFolder> optFolder = database.getFolder(Database.CACHE_FOLDER, true);

	    if (optFolder.isPresent()) {

		EXECUTOR.submit(() -> {

		    store(sharedContent, optFolder, serializer);
		});
	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("MarkLogic cache folder missing");
	    }
	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to create MarkLogicDatabase instance");

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MLCACHE_FOLDER_STORE_ERR, //
		    e);
	}
    }

    @SuppressWarnings("rawtypes")
    private synchronized void store(SharedContent sharedContent, Optional<DatabaseFolder> optFolder, SharedContentSerializer serializer) {

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Storing binary to folder {} STARTED", optFolder.get().getName());

	    String identifier = contentIdentifier(sharedContent.getIdentifier(), sharedContent.getType());

	    InputStream stream = serializer.toStream(sharedContent);

	    optFolder.get().store(identifier, FolderEntry.of(stream), EntryType.CACHE_ENTRY);

	    GSLoggerFactory.getLogger(getClass()).info("Storing binary to folder {} ENDED", optFolder.get().getName());

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    @Override
    public Long count(SharedContentType type) throws GSException {

	return null;
    }

    @Override
    public String getType() {

	return DRIVER_TYPE;
    }

    public String contentIdentifier(String identifier, SharedContentType sharedContentType) {

	StringBuilder builder = new StringBuilder(sharedContentType.toString());

	builder.append("_");

	builder.append(identifier);

	return builder.toString();
    }

    private Optional<SharedContentSerializer> findSerializer(SharedContentType type) {

	return Optional.ofNullable(SharedContentSerializers.getSerializer(type));
    }

    private Database getDatabase() throws GSException {

	StorageInfo uri = setting.getDatabaseCacheSetting().get().asStorageInfo();

	Database database = DatabaseFactory.get(uri);

	return database;
    }

    private SharedContentSerializer getSerializerOrThrowEx(SharedContentType type) throws GSException {

	Optional<SharedContentSerializer> optionalSerializer = findSerializer(type);

	return optionalSerializer.orElseThrow(() -> GSException.createException(//

		DatabaseCacheDriver.class, //
		"Unable to find serializer for shared content type " + type, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		MLCACHE_SHARED_CONTENT_SERIALIZER_NOT_FOUND));
    }

    @Override
    public SharedContentCategory getCategory() {

	return SharedContentCategory.DATABASE_CACHE;
    }
}
