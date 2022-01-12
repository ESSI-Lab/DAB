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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.MediaType;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.AbstractGSConfigurableJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SHARED_CONTENT_TYPES;
import eu.essi_lab.shared.driver.clean.MarkLogicCacheCleanJob;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.serializer.GSSharedContentSerializers;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
public class MarkLogicCacheDriver extends AbstractGSConfigurableJob implements ISharedCacheRepositoryDriver {

    @JsonIgnore
    private static final String MLCACHE_COMPONENT_KEY = "MLCACHE_COMPONENT_KEY";

    @JsonIgnore
    private static final String MLCACHE_NO_SHARED_CONTENT_SERIALIZER_FOUND = "MLCACHE_NO_SHARED_CONTENT_SERIALIZER_FOUND";

    @JsonIgnore
    private static final String CACHE_FOLDER_PREFIX = "driverCache";

    @JsonIgnore
    private static final String MLCACHE_UNSUPPORTED_READ_QUERY_ERR = "MLCACHE_UNSUPPORTED_READ_QUERY_ERR";

    @JsonIgnore
    private static final String MLCACHE_FOLDER_CREATION_ERR = "MLCACHE_FOLDER_CREATION_ERR";

    @JsonIgnore
    private transient CacheStoreQueue queue;

    @JsonIgnore
    private transient ConfigurationUtils utils;

    @JsonIgnore
    private static final String CANT_CREATE_FOLDER_LOG = "Can't create folder {}";

    @JsonIgnore
    private static final String UNABLE_TO_CREATE_FOLDER_MSG = "Unable to create folder ";

    @JsonIgnore
    private static final String ML_CACHE_RETENTION_TIME_KEY = "ML_CACHE_RETENTION_TIME_KEY";

    public MarkLogicCacheDriver() {

	super();

	setLabel("MarkLogic Cache");

	setKey(MLCACHE_COMPONENT_KEY);

	GSConfOptionInteger cacheRetentionTimeOpt = new GSConfOptionInteger();

	cacheRetentionTimeOpt.setKey(ML_CACHE_RETENTION_TIME_KEY);

	cacheRetentionTimeOpt.setLabel("Max time (in minutes) resources are kept in cache");

	cacheRetentionTimeOpt.setValue(60);

	getSupportedOptions().put(ML_CACHE_RETENTION_TIME_KEY, cacheRetentionTimeOpt);

    }

    public String cacheFolderName() {

	StringBuilder builder = new StringBuilder(CACHE_FOLDER_PREFIX);

	return builder.toString();

    }

    public String contentIdentifier(String identifier, SharedContentType sharedContentType) {

	StringBuilder builder = new StringBuilder(sharedContentType.getType());

	builder.append(identifier);

	return builder.toString();

    }

    @Override
    public SharedContent readSharedContent(String identifier, SharedContentType type) throws GSException {

	IGSScharedContentSerializer serializer = getSerializerOrThrowEx(type);

	String folderName = cacheFolderName();

	SharedContent[] c = new SharedContent[] { null };

	try {

	    MarkLogicDatabase mldb = getMarkLogicDatabase();

	    mldb.getFolder(folderName, true).ifPresent(folder -> {

		try {
		    InputStream binary = folder.getBinary(contentIdentifier(identifier, type));

		    c[0] = serializer.fromStream(binary);

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(MarkLogicCacheDriver.class).error("Can't read binary with id {} from folder {}", identifier,
			    folderName, e);
		}

	    });

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't obtain database in MarkLogic Shared repository cache");

	    throw e;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(CANT_CREATE_FOLDER_LOG, folderName, e);

	    throw GSException.createException(getClass(), UNABLE_TO_CREATE_FOLDER_MSG + folderName, null, null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, MLCACHE_FOLDER_CREATION_ERR, e);
	}

	return c[0];
    }

    @Override
    public List<SharedContent> readSharedContent(SharedContentType type, SharedContentQuery query) throws GSException {

	throw GSException.createException(getClass(),
		"readSharedContent(SharedContentType type, SharedContentQuery query) is not supported in " + getClass().getName(), null,
		null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, MLCACHE_UNSUPPORTED_READ_QUERY_ERR);

    }

    Optional<IGSScharedContentSerializer> findSerializer(SharedContentType type) {

	Optional<MediaType> media = selectMedia(type);

	if (!media.isPresent())
	    return Optional.empty();

	return Optional.ofNullable(GSSharedContentSerializers.getSerializer(type, media.get()));
    }

    Optional<MediaType> selectMedia(SharedContentType type) {

	String t = type.getType();

	switch (t) {
	case SHARED_CONTENT_TYPES.GS_YELLOW_PAGE_TYPE:
	    return Optional.of(MediaType.XML_UTF_8);
	default:
	    return Optional.empty();
	}

    }

    private IGSScharedContentSerializer getSerializerOrThrowEx(SharedContentType type) throws GSException {

	Optional<IGSScharedContentSerializer> optionalSerializer = findSerializer(type);

	return optionalSerializer.orElseThrow(() -> GSException.createException(MarkLogicCacheDriver.class,
		"Can't find serializer for shared content type " + type.getType(), null, ErrorInfo.ERRORTYPE_INTERNAL,
		ErrorInfo.SEVERITY_ERROR, MLCACHE_NO_SHARED_CONTENT_SERIALIZER_FOUND));
    }

    MarkLogicDatabase getMarkLogicDatabase() throws GSException {

	StorageUri suri = ConfigurationUtils.getStorageURI();

	MarkLogicDatabase db = (MarkLogicDatabase) new DatabaseConsumerFactory().createDataBaseReader(suri).getDatabase();

	return db;
    }

    @Override
    public void store(SharedContent sharedContent) throws GSException {

	IGSScharedContentSerializer serializer = getSerializerOrThrowEx(sharedContent.getType());

	String folderName = cacheFolderName();

	try {

	    MarkLogicDatabase mldb = getMarkLogicDatabase();

	    mldb.getFolder(folderName, true).ifPresent(folder ->

	    getQueue().toCache(contentIdentifier(sharedContent.getIdentifier(), sharedContent.getType()), sharedContent, serializer, folder)

	    );

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't obtain database in MarkLogic Shared repository cache");

	    throw e;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(CANT_CREATE_FOLDER_LOG, folderName, e);

	    throw GSException.createException(getClass(), UNABLE_TO_CREATE_FOLDER_MSG + folderName, null, null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, MLCACHE_FOLDER_CREATION_ERR, e);
	}

    }

    @Override
    public Long count(SharedContentType type) throws GSException {
	return null;
    }

    @JsonIgnore
    public CacheStoreQueue getQueue() {

	if (queue == null)
	    queue = new CacheStoreQueue();

	return queue;
    }

    @Override
    public void run(Map<String, Object> jobDataMap, Boolean isRecovering, Optional<GSJobStatus> jobStatus) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Starting {} job", getClass().getName());

	String folderName = cacheFolderName();

	MarkLogicDatabase mldb = getMarkLogicDatabase();

	try {

	    mldb.getFolder(folderName, true).ifPresent(folder -> {

		MarkLogicCacheCleanJob job = new MarkLogicCacheCleanJob(folder,
			((GSConfOptionInteger) getSupportedOptions().get(ML_CACHE_RETENTION_TIME_KEY)).getValue() * 60L);

		job.run();

	    });

	} catch (RequestException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_CREATE_FOLDER_LOG, folderName, e);

	    throw GSException.createException(getClass(), UNABLE_TO_CREATE_FOLDER_MSG + folderName, null, null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, MLCACHE_FOLDER_CREATION_ERR, e);
	}

    }

    @Override
    public GSJobValidationResult isValid(Map<String, Object> jobDataMap) {

	// TODO
	GSLoggerFactory.getLogger(getClass()).debug("Validating {} with key {}", getClass().getName(), getKey());

	GSJobValidationResult result = new GSJobValidationResult();

	GSLoggerFactory.getLogger(getClass()).info("{} with key {} is valid", getClass().getName(), getKey());

	result.setValid(true);

	return result;
    }
}
