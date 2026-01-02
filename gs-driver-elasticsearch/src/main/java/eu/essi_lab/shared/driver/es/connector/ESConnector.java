package eu.essi_lab.shared.driver.es.connector;

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

import static com.amazonaws.util.StringUtils.UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.es.connector.aws.AWSESConstants;

/**
 * @author ilsanto
 */
public class ESConnector implements IESConnector {

    private Logger logger = GSLoggerFactory.getLogger(ESConnector.class);

    private StorageInfo esStaorageUri;
    private static final String GS_SERVICE_INIT_INDEX = "gsserviceinitialized";

    private static final String INITIALIZE_JSON = "{\"initialized\":\"true\"}";
    private static final String ES_CONNECTOR_ERROR_EXECUTING = "ES_CONNECTOR_ERROR_EXECUTING";
    private static final String GS_SERVICE_INIT_TYPE = "initialize";
    private static final String SEARCH_PRETTY = "_search?pretty";
    private static final String SEARCH = "_search";
    private static final String INDEX_TYPE_SUFFIX = "estype";
    private static final String COUNT_SUFFIX = "_count";
    private static final String MGET_PATH = "_mget";

    /**
     * 
     */
    public StorageInfo getEsStorageUri() {
	return esStaorageUri;
    }

    /**
     * 
     */
    public void setEsStorageUri(StorageInfo uri) {

	this.esStaorageUri = new StorageInfo();

	String url = uri.getUri();

	if (!url.endsWith("/"))
	    url += "/";

	this.esStaorageUri.setUri(url);

	this.esStaorageUri.setUser(uri.getUser());
	this.esStaorageUri.setPassword(uri.getPassword());
	this.esStaorageUri.setName(uri.getName());
	this.esStaorageUri.setIdentifier(uri.getIdentifier());

    }

    protected ESRequestSubmitter getSubmitter() {

	ESRequestSubmitter submitter = new ESRequestSubmitter();

	submitter.setPwd(this.esStaorageUri.getPassword());

	submitter.setUser(this.esStaorageUri.getUser());

	return submitter;
    }

    @Override
    public boolean testConnection() {

	String url = getEsStorageUri().getUri() + "_cat/health?v";
	HttpGet get = new HttpGet(url);

	try {
	    HttpResponse r = getSubmitter().submit(get);

	    int code = r.getStatusLine().getStatusCode();

	    logger.trace("Received code {} from {}", code, url);

	    return validCode(code);

	} catch (IOException e) {

	    logger.warn("IOException executing {}", url, e);

	}

	return false;
    }

    private boolean validCode(int code) {
	logger.trace("Validating code [{}], valid code range is 200..399", code);

	return code >= 200 && code <= 399;
    }

    private String initIndex() {
	return GS_SERVICE_INIT_INDEX + getIndex(esStaorageUri);
    }

    private boolean isInitialized() {

	String geturl = getEsStorageUri().getUri() + initIndex() + "/" + GS_SERVICE_INIT_TYPE + "/" + SEARCH_PRETTY;

	HttpGet get = new HttpGet(geturl);

	logger.trace("Check if Elasticsearch is initialized {}", geturl);

	try {

	    HttpResponse response = getSubmitter().submit(get);

	    int code = response.getStatusLine().getStatusCode();

	    logger.trace("Received code {}", code);

	    if (code == 404) {
		return false;
	    }

	    String thestring = IOUtils.toString(response.getEntity().getContent(), UTF8);

	    if (!validCode(code)) {
		logger.trace("Received from Elasticsearch\n{}", thestring);
	    }

	    return new JSONObject(thestring).getJSONObject("hits").getInt("total") - 1 == 0;

	} catch (IOException e) {

	    logger.error("Error checking Elasticsearch initialization {}", get, e);

	}

	return false;

    }

    @Override
    public void initializePersistentStorage() throws GSException {

	if (isInitialized()) {
	    logger.trace("Elasticsearch already initialized {}", getEsStorageUri().getUri());

	    return;
	}

	String puturl = getEsStorageUri().getUri() + initIndex() + "/" + GS_SERVICE_INIT_TYPE + "/" + UUID.randomUUID().toString();

	logger.trace("Initialize PUT {} @ {}", INITIALIZE_JSON, puturl);

	HttpPut put = new HttpPut(puturl);

	HttpEntity entity = EntityBuilder.create().setText(INITIALIZE_JSON).setContentType(ContentType.APPLICATION_JSON).build();
	put.setEntity(entity);

	submitRequestWithRetry(put, JSONDocumentParser.class);

    }

    public void write(String identifier, String index, String type, JSONObject object) throws GSException {

	logger.debug("Requested to write {} of type {} to {}  {}", identifier, type, getEsStorageUri().getUri(), index);

	String puturl = getEsStorageUri().getUri() + index + "/" + type + "/" + identifier;

	logger.trace("Write PUT {}", puturl);

	HttpPut put = new HttpPut(puturl);

	String text = object.toString();
	// String trace = "JSON to write: <" + text + ">";
	// GSLoggerFactory.getLogger(getClass()).trace(trace);

	HttpEntity entity = EntityBuilder.create().setText(text).setContentType(ContentType.APPLICATION_JSON).build();

	put.setEntity(entity);

	submitRequestWithRetry(put, JSONDocumentParser.class);
    }

    @Override
    public void write(String identifier, SharedContentType type, InputStream stream) throws GSException {

	try {
	    String text = IOUtils.toString(stream, UTF8);
	    JSONObject object = new JSONObject(text);

	    write(identifier, getIndex(esStaorageUri), getIndexType(type), object);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, ES_CONNECTOR_ERROR_EXECUTING, e);
	}

    }

    @Override
    public Long count(SharedContentType type) throws GSException {

	logger.debug("Counting documents of type {} from {}", type, getEsStorageUri().getUri());

	String geturl = getEsStorageUri().getUri() + getIndex(esStaorageUri) + "/" + getIndexType(type) + "/" + COUNT_SUFFIX;

	logger.trace("Count GET {}", geturl);

	HttpGet get = new HttpGet(geturl);

	CountResponseParser response = submitRequestWithRetry(get, CountResponseParser.class);

	return response.getCount();
    }

    @Override
    public Optional<InputStream> get(String identifier, SharedContentType type) throws GSException {

	logger.debug("Getting document with id {} of type {} from {} STARTED", identifier, type, getEsStorageUri().getUri());

	String geturl = getEsStorageUri().getUri() + getIndex(esStaorageUri) + "/" + getIndexType(type) + "/" + identifier;

	logger.trace("Get document GET {}", geturl);

	HttpGet get = new HttpGet(geturl);

	GetDocumentParser response = submitRequestWithRetry(get, GetDocumentParser.class);

	Optional<InputStream> source = response.getSource();

	logger.debug("Getting document with id {} of type {} from {} ENDED", identifier, type, getEsStorageUri().getUri());

	return source;
    }

    @Override
    public List<InputStream> query(SharedContentType type, JSONObject query, boolean multiGet) throws GSException {

	logger.debug("Querying of type {} from {}", type, getEsStorageUri().getUri());
	
	String path = multiGet ? MGET_PATH : SEARCH;

	String posturl = getEsStorageUri().getUri() + getIndex(esStaorageUri) + "/" + getIndexType(type) + "/" + path;

	logger.trace("Query POST url {}", posturl);

	logger.trace("Query POST body {}", query);

	HttpPost post = new HttpPost(posturl);

	HttpEntity entity = EntityBuilder.create().setText(query.toString()).setContentType(ContentType.APPLICATION_JSON).build();

	post.setEntity(entity);

	QueryDocumentParser response = submitRequestWithRetry(post, QueryDocumentParser.class);

	return response.getSources(multiGet);
    }

    private <T extends JSONDocumentParser> T submitRequestWithRetry(HttpRequestBase request, Class<T> clazz) throws GSException {

	String msg = "Error executing Elasticsearch request at " + getEsStorageUri().getUri();
	try {

	    HttpResponse response = null;

	    long wait = 1000l;

	    while (true) {

		response = getSubmitter().submit(request);

		String type = request.getMethod();
		int code = response.getStatusLine().getStatusCode();
		logger.trace("Elasticsearch {} response code {}", type, code);

		InputStream stream = response.getEntity().getContent();
		String responseString = IOUtils.toString(stream, StandardCharsets.UTF_8);

		if (code == 400 && responseString.contains("Throttling") && wait < AWSESConstants.MAX_BACKOFF_TIMEOUT_MS) {

		    wait = wait * 2; // backoff wait
		    String info = "AWS Elasticsearch rate exceded exception identified during ESConnector execution. Trying to resend with backoff wait in "
			    + wait + "ms";
		    GSLoggerFactory.getLogger(getClass()).info(info);
		    sleep(wait);

		} else {

		    checkCodeAndThrowEx(code, msg, null, responseString);

		    Constructor<T> constructor = clazz.getConstructor(String.class);
		    return constructor.newInstance(responseString);
		}
	    }

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, ES_CONNECTOR_ERROR_EXECUTING, //
		    e);
	}
    }

    private void checkCodeAndThrowEx(int code, String errMsg, String userErrMsg, String response) throws GSException {

	if (!validCode(code)) {

	    if (logger.isWarnEnabled()) {
		logger.warn("Invalid code returned with response {}", response);
	    }

	    throw GSException.createException(//
		    getClass(), //
		    errMsg, //
		    userErrMsg, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ES_CONNECTOR_ERROR_EXECUTING);
	}

    }

    private void sleep(long wait) {
	try {
	    Thread.sleep(wait);
	} catch (InterruptedException e1) {
	    GSLoggerFactory.getLogger(getClass()).warn(e1.getMessage());
	    Thread.currentThread().interrupt();
	}

    }

    private String getIndexType(SharedContentType type) {
	return type.name().toLowerCase() + INDEX_TYPE_SUFFIX;
    }

    private String getIndex(StorageInfo storageUri) {
	return storageUri.getName().toLowerCase();
    }
}
