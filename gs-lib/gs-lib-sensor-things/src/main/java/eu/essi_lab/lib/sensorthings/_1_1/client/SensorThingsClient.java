package eu.essi_lab.lib.sensorthings._1_1.client;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.sensorthings._1_1.client.request.AddressableEntity;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AssociationLinkResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.DataArrayFormatResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.PropertyResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.PropertyValueResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.SensorThingsResponse;
import eu.essi_lab.lib.sensorthings._1_1.client.response.SensorThingsResponseImpl;
import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResult;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.FeatureOfInterest;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.HistoricalLocation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Observation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.ObservedProperty;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * https://docs.ogc.org/is/18-088/18-088.html#sensorthings-serviceinterface
 * 
 * @author Fabrizio
 */
/**
 * @author Fabrizio
 */
/**
 * @author Fabrizio
 */
public class SensorThingsClient {

    /**
     * 
     */
    private Downloader downloader;
    /**
     * 
     */
    private URL serviceRootUrl;
    /**
     * 
     */
    private SimpleEntry<Integer, Integer> retry;

    /**
     * e.g.: https://beta.hydroserver2.org/api/sensorthings/v1.1/
     * 
     * @param serviceRootUrl
     * @throws MalformedURLException
     */
    public SensorThingsClient(String serviceRootUrl) throws MalformedURLException {

	this(new URL(serviceRootUrl));
    }

    /**
     * e.g.: https://beta.hydroserver2.org/api/sensorthings/v1.1/
     * 
     * @param serviceRootUrl
     */
    public SensorThingsClient(URL serviceRootUrl) {

	this.serviceRootUrl = serviceRootUrl;
	this.downloader = new Downloader();

	withRetryPolicy(10, 1);
    }

    /**
     * @param timeout request timeout in seconds
     */
    public SensorThingsClient withTimeout(int timeout) {

	this.downloader.setConnectionTimeout(TimeUnit.SECONDS,timeout);
	this.downloader.setResponseTimeout(TimeUnit.SECONDS,timeout);
	return this;
    }

    /**
     * @param attempts maximum number of attempts (inclusive the first)
     * @param delay delay to occur between retries, in seconds
     */
    public SensorThingsClient withRetryPolicy(int attempts, int delay) {

	this.retry = new SimpleEntry<Integer, Integer>(attempts, delay);
	return this;
    }

    /**
     * @param request
     * @return
     * @throws GSException
     * @throws IOException
     */
    public SensorThingsResponse execute(SensorThingsRequest request) throws GSException {

	request.setServiceRootUrl(getServiceRootUrl());

	RetryPolicy<HttpResponseWrapper> retryPolicy = RetryPolicy.<HttpResponseWrapper> //
		builder().//
		handleResultIf(r -> r.getException().isPresent()).//
		withDelay(Duration.ofSeconds(retry.getValue())).//
		withMaxAttempts(retry.getKey()).//
		onRetry(e -> GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount())).//
		onRetriesExceeded(e -> GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded")).//
		build();

	HttpResponseWrapper wrapper = Failsafe.with(retryPolicy).get(() -> downloadReponse(request));

	if (wrapper.getException().isPresent()) {

	    throw wrapper.getException().get();
	}

	String responseString = getResponseString(wrapper.getResponse().body());

	SensorThingsResponseImpl sensorThingsResponse = new SensorThingsResponseImpl();

	//
	// DataArrayFormatResult
	//

	if (request.isDataArrayResponseFormatSet()) {

	    sensorThingsResponse.setDataArrayFormatResult(new DataArrayFormatResult(responseString));
	}

	//
	// AssociationLinkResult
	//

	else if (request.isAddressAssociationLinkSet()) {

	    sensorThingsResponse.setAssociationLinkResult(new AssociationLinkResult(responseString));
	}

	//
	// PropertyResult or PropertyValueResult
	//

	else if (request.getEntityProperty().isPresent()) {

	    if (request.getEntityProperty().get().isGetValueSet()) {

		sensorThingsResponse.setPropertyValueResult(new PropertyValueResult(responseString));

	    } else {

		sensorThingsResponse.setPropertyResult(new PropertyResult(responseString));
	    }
	} else if (!request.getAddressableEntityList().isEmpty()) {

	    //
	    // AddressableEntityResult
	    //

	    List<AddressableEntity> addEntitiesList = request.getAddressableEntityList();

	    EntityRef targetEntity = addEntitiesList.get(addEntitiesList.size() - 1).getEntityRef();

	    try {
		if (targetEntity == EntityRef.DATASTREAMS) {
		    sensorThingsResponse
			    .setAddressableEntityResult(new AddressableEntityResult<Datastream>(responseString, Datastream.class));
		} else if (targetEntity == EntityRef.FEATURES_OF_INTEREST) {
		    sensorThingsResponse.setAddressableEntityResult(
			    new AddressableEntityResult<FeatureOfInterest>(responseString, FeatureOfInterest.class));

		} else if (targetEntity == EntityRef.LOCATIONS) {
		    sensorThingsResponse.setAddressableEntityResult(new AddressableEntityResult<Location>(responseString, Location.class));

		} else if (targetEntity == EntityRef.OBSERVATIONS) {
		    sensorThingsResponse
			    .setAddressableEntityResult(new AddressableEntityResult<Observation>(responseString, Observation.class));

		} else if (targetEntity == EntityRef.OBSERVED_PROPERTIES) {
		    sensorThingsResponse.setAddressableEntityResult(
			    new AddressableEntityResult<ObservedProperty>(responseString, ObservedProperty.class));

		} else if (targetEntity == EntityRef.SENSORS) {
		    sensorThingsResponse.setAddressableEntityResult(new AddressableEntityResult<Sensor>(responseString, Sensor.class));

		} else if (targetEntity == EntityRef.THINGS) {
		    sensorThingsResponse.setAddressableEntityResult(new AddressableEntityResult<Thing>(responseString, Thing.class));

		} else if (targetEntity == EntityRef.HISTORICAL_LOCATIONS) {
		    sensorThingsResponse.setAddressableEntityResult(
			    new AddressableEntityResult<HistoricalLocation>(responseString, HistoricalLocation.class));

		} else {

		    sensorThingsResponse.setAddressableEntityResult(new AddressableEntityResult<Entity>(responseString, Entity.class));
		}

	    } catch (JSONException ex) {

		throw GSException.createException(//
			getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			"SensorThings1.1_Client_JSONExceptionError", //
			ex);

	    }
	} else {

	    sensorThingsResponse.setCapabilitiesResult(new ServiceRootResult(new JSONObject(responseString)));
	}

	return sensorThingsResponse;
    }

    /**
     * @return the serviceRootUrl
     */
    public URL getServiceRootUrl() {

	return serviceRootUrl;
    }

    /**
     * @param responseStream
     * @return
     * @throws GSException
     */
    private String getResponseString(InputStream responseStream) throws GSException {

	try {
	    return IOStreamUtils.asUTF8String(responseStream);

	} catch (IOException e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SensorThings1.1_Client_StreamToStringError", //
		    e);
	}
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    private HttpResponseWrapper downloadReponse(SensorThingsRequest request) {

	HttpResponseWrapper wrapper = new HttpResponseWrapper();
	
	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(request.compose());

	    if (response.body() == null) {

		wrapper.setException(GSException.createException(//
			getClass(), //
			"Occurred during request execution, no response stream found", //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			"SensorThings1.1_Client_MissingResponseStreamError" //
		));
	    }

	    wrapper.setResponse(response);

	} catch (Exception e) {

	    wrapper.setException(GSException.createException(//
		    getClass(), //
		    e.getClass().getSimpleName() + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SensorThings1.1_Client_ExceptionDownloadResponseError", //
		    e));
	    
	    return wrapper;
	}

	Integer code = wrapper.getResponse().statusCode();

	if (code != null && code != 200) {

	    wrapper.setException(GSException.createException(//
		    getClass(), //
		    "Error [" + code + "] occurred during request execution", //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SensorThings1.1_Client_ErrorCodeDownloadResponseError"));
	}

	return wrapper;
    }

    /**
     * @author Fabrizio
     */
    private class HttpResponseWrapper {

	private HttpResponse<InputStream> response;
	private GSException exception;

	/**
	 * 
	 */
	private HttpResponseWrapper() {

	}

	/**
	 * @param response
	 */
	public void setResponse(HttpResponse<InputStream> response) {
	    this.response = response;
	}

	/**
	 * @return
	 */
	public HttpResponse<InputStream> getResponse() {
	    return response;
	}

	/**
	 * @return the exception
	 */
	public Optional<GSException> getException() {

	    return Optional.ofNullable(exception);
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(GSException exception) {

	    this.exception = exception;
	}

    }
}
