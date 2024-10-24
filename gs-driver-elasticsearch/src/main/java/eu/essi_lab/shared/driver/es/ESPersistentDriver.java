package eu.essi_lab.shared.driver.es;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
import eu.essi_lab.shared.driver.es.connector.ESConnectorFactory;
import eu.essi_lab.shared.driver.es.connector.IESConnector;
import eu.essi_lab.shared.driver.es.query.ESQueryMapper;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.serializer.SharedContentSerializer;
import eu.essi_lab.shared.serializer.SharedContentSerializers;

/**
 * @author ilsanto
 */
public class ESPersistentDriver implements ISharedRepositoryDriver<SharedPersistentDriverSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "ESPersistentDriver";

    private static final String NO_ES_CONNECTOR_FOUND = "NO_ES_CONNECTOR_FOUND";
    private static final String NO_SHARED_CONTENT_SERIALIZER_FOUND = "NO_SHARED_CONTENT_SERIALIZER_FOUND_ERROR";

    private static final String SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR = "SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR";

    private SharedPersistentDriverSetting setting;

    /**
     * 
     */
    public ESPersistentDriver() {

	setting = new SharedPersistentDriverSetting();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized SharedContent read(String identifier, SharedContentType type) throws GSException {

	if (type != SharedContentType.JSON_TYPE) {

	    throw GSException.createException(//

		    ESPersistentDriver.class, //
		    "Shared content type " + type + " not supported", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR);
	}

	GSLoggerFactory.getLogger(getClass()).trace("Getting connector STARTED");

	IESConnector connector = getConnectorOrThrowEx();

	GSLoggerFactory.getLogger(getClass()).trace("Getting connector {} ENDED", connector.getClass().getSimpleName());

	GSLoggerFactory.getLogger(getClass()).trace("Performing connector request STARTED");

	Optional<InputStream> optionalJson = Optional.empty();

	try {

	    optionalJson = connector.get(identifier, type);

	} catch (GSException ex) {

	    ex.log();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Performing connector request ENDED");

	if (!optionalJson.isPresent()) {

	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).trace("Serialization STARTED");

	InputStream stream = optionalJson.get();

	SharedContentSerializer serializer = getSerializerOrThrowEx(type);

	SharedContent content = serializer.fromStream(identifier, stream);

	GSLoggerFactory.getLogger(getClass()).trace("Serialization ENDED");

	return content;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized List<SharedContent> read(SharedContentType type, SharedContentQuery query) throws GSException {

	if (type != SharedContentType.JSON_TYPE) {

	    throw GSException.createException(//

		    ESPersistentDriver.class, //
		    "Shared content type " + type + " not supported", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR);
	}

	IESConnector connector = getConnectorOrThrowEx();

	SharedContentSerializer serializer = getSerializerOrThrowEx(type);

	JSONObject esquery = getMapper().mapToQuery(query);

	List<InputStream> list = connector.query(type, esquery, !query.getIdsList().isEmpty());

	return list.stream().//
		map(stream -> {
		    try {

			return serializer.fromStream(null, stream);

		    } catch (GSException e) {

			e.log();
		    }

		    return null;
		}).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized void store(SharedContent sharedContent) throws GSException {

	if (sharedContent.getType() != SharedContentType.JSON_TYPE) {

	    throw GSException.createException(//

		    ESPersistentDriver.class, //
		    "Shared content type " + sharedContent.getType() + " not supported", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR);
	}

	IESConnector connector = getConnectorOrThrowEx();

	SharedContentSerializer serializer = getSerializerOrThrowEx(sharedContent.getType());

	connector.write(sharedContent.getIdentifier(), sharedContent.getType(), serializer.toStream(sharedContent));
    }

    @Override
    public synchronized Long count(SharedContentType type) throws GSException {

	if (type != SharedContentType.JSON_TYPE) {

	    throw GSException.createException(//

		    ESPersistentDriver.class, //
		    "Shared content type " + type + " not supported", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SHARED_CONTENT_TYPE_NOT_SUPPORTED_ERROR);
	}

	IESConnector connector = getConnectorOrThrowEx();

	try {

	    return connector.count(type);

	} catch (GSException ex) {

	    ex.log();
	}

	return 0l;
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

    ESQueryMapper getMapper() {

	return new ESQueryMapper();
    }

    Optional<SharedContentSerializer> findSerializer(SharedContentType type) {

	return Optional.ofNullable(SharedContentSerializers.getSerializer(type));
    }

    Optional<IESConnector> getConnector(StorageInfo uri) {

	return Optional.ofNullable(ESConnectorFactory.getConnector(uri));
    }

    private IESConnector getConnectorOrThrowEx() throws GSException {

	Optional<IESConnector> optional = getConnector(setting.getElasticSearchSetting().get().asStorageUri());

	return optional.orElseThrow(() -> GSException.createException(//

		ESPersistentDriver.class, //
		"Can't find Elastic Search connector", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		NO_ES_CONNECTOR_FOUND));

    }

    private SharedContentSerializer getSerializerOrThrowEx(SharedContentType type) throws GSException {

	Optional<SharedContentSerializer> optionalSerializer = findSerializer(type);

	return optionalSerializer.orElseThrow(() -> GSException.createException(//

		ESPersistentDriver.class, //
		"Can't find serializer for shared content type " + type, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		NO_SHARED_CONTENT_SERIALIZER_FOUND));
    }

    @Override
    public SharedContentCategory getCategory() {

	return SharedContentCategory.ELASTIC_SEARCH_PERSISTENT;
    }
}
