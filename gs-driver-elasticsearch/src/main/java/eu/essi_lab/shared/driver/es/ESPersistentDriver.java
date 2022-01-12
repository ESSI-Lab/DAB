package eu.essi_lab.shared.driver.es;

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

import com.google.common.net.MediaType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.d2k.serialization.J2RDFSerializer;
import eu.essi_lab.shared.driver.ISharedPersistentRepositoryDriver;
import eu.essi_lab.shared.driver.es.connector.ESConnectorFactory;
import eu.essi_lab.shared.driver.es.connector.IESConnector;
import eu.essi_lab.shared.driver.es.query.ESQueryMapper;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.serializer.GSSharedContentSerializers;
import eu.essi_lab.shared.serializer.IGSScharedContentSerializer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
public class ESPersistentDriver extends AbstractGSconfigurable implements ISharedPersistentRepositoryDriver {

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    private transient Logger logger = GSLoggerFactory.getLogger(ESPersistentDriver.class);
    private static final String ES_URI_OPTION_KEY = "ES_URI_OPTION_KEY";
    private static final String ESDRIVER_UNKNOWN_OPTION = "ESDRIVER_UNKNOWN_OPTION";
    private static final String ESDRIVER_BAD_STORAGEURI_OPTION = "ESDRIVER_BAD_STORAGEURI_OPTION";
    private static final String NO_ES_CONNECTOR_FOUND = "NO_ES_CONNECTOR_FOUND";

    private transient MediaType media = MediaType.JSON_UTF_8;
    private static final String NO_SHARED_CONTENT_SERIALIZER_FOUND = "NO_SHARED_CONTENT_SERIALIZER_FOUND";

    public ESPersistentDriver() {

	setLabel("Elastic Search");

	GSConfOptionDBURI uriOption = new GSConfOptionDBURI();

	uriOption.setLabel("Elastic Search URL");

	uriOption.setKey(ES_URI_OPTION_KEY);

	getSupportedOptions().put(ES_URI_OPTION_KEY, uriOption);

    }

    @Override
    public SharedContent readSharedContent(String identifier, SharedContentType type) throws GSException {
	IESConnector connector = getConnectorOrThrowEx();

	Optional<InputStream> optionalJson = connector.get(identifier, type);

	if (!optionalJson.isPresent())
	    return null;

	InputStream stream = optionalJson.get();

	IGSScharedContentSerializer serializer = getSerializerOrThrowEx(type);

	return serializer.fromStream(stream);
    }

    ESQueryMapper getMapper() {
	return new ESQueryMapper();
    }

    @Override
    public List<SharedContent> readSharedContent(SharedContentType type, SharedContentQuery query) throws GSException {

	IESConnector connector = getConnectorOrThrowEx();

	IGSScharedContentSerializer serializer = getSerializerOrThrowEx(type);

	JSONObject esquery = getMapper().mapToQuery(type, query);

	List<InputStream> list = connector.query(type, esquery);

	return list.stream().map(stream -> {
	    try {

		return serializer.fromStream(stream);

	    } catch (GSException e) {

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	    }

	    return null;
	}).filter(c -> c != null).collect(Collectors.toList());

    }

    Optional<IGSScharedContentSerializer> findSerializer(SharedContentType type) {
	return Optional.ofNullable(GSSharedContentSerializers.getSerializer(type, media));
    }

    private IESConnector getConnectorOrThrowEx() throws GSException {
	Optional<IESConnector> optional = getConnector((GSConfOptionDBURI) getSupportedOptions().get(ES_URI_OPTION_KEY));

	return optional.orElseThrow(() -> GSException
		.createException(ESPersistentDriver.class, "Can't find Elastic Search connector", null, ErrorInfo.ERRORTYPE_INTERNAL,
			ErrorInfo.SEVERITY_ERROR, NO_ES_CONNECTOR_FOUND));

    }

    private IGSScharedContentSerializer getSerializerOrThrowEx(SharedContentType type) throws GSException {
	Optional<IGSScharedContentSerializer> optionalSerializer = findSerializer(type);

	return optionalSerializer.orElseThrow(() -> GSException
		.createException(ESPersistentDriver.class, "Can't find serializer for shared content type " + type.getType(), null,
			ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, NO_SHARED_CONTENT_SERIALIZER_FOUND));
    }

    @Override
    public void store(SharedContent sharedContent) throws GSException {

	IESConnector connector = getConnectorOrThrowEx();

	IGSScharedContentSerializer serializer = getSerializerOrThrowEx(sharedContent.getType());

	connector.write(sharedContent.getIdentifier(), sharedContent.getType(), serializer.toStream(sharedContent));

    }

    @Override
    public Long count(SharedContentType type) throws GSException {

	IESConnector connector = getConnectorOrThrowEx();

	return connector.count(type);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return options;
    }

    Optional<IESConnector> getConnector(GSConfOptionDBURI o) {
	return Optional.ofNullable(ESConnectorFactory.getConnector(o.getValue()));
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	logger.trace("On option set {}", opt.getKey());

	if (opt.getKey().equals(ES_URI_OPTION_KEY)) {

	    if (GSConfOptionDBURI.class.isAssignableFrom(opt.getClass())) {

		GSConfOptionDBURI o = (GSConfOptionDBURI) opt;

		logger.trace("Looking for connector to {}", o.getValue().getUri());

		Optional<IESConnector> optional = getConnector(o);

		IESConnector connector = optional.orElseThrow(() -> GSException
			.createException(J2RDFSerializer.class, "No Elastic search connector found", null, ErrorInfo.ERRORTYPE_INTERNAL,
				ErrorInfo.SEVERITY_ERROR, NO_ES_CONNECTOR_FOUND));

		connector.initializePersistentStorage();

		logger.trace("Initialization complete");

		return;
	    }

	    throw GSException.createException(getClass(), "Provided option " + opt.getKey() + " can't be casted to GSConfOptionDBURI", null,
		    null, ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_WARNING, ESDRIVER_BAD_STORAGEURI_OPTION);
	}

	throw GSException.createException(getClass(), "Unknown option with key " + opt.getKey(), null, null, ErrorInfo.ERRORTYPE_CLIENT,
		ErrorInfo.SEVERITY_WARNING, ESDRIVER_UNKNOWN_OPTION);

    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here
    }
}
