package eu.essi_lab.profiler.pubsub.handler.csw;

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
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.ogc.pubsub._1_0.SubscribeResponseType;
import eu.essi_lab.ogc.pubsub._1_0.SubscribeType;
import eu.essi_lab.ogc.pubsub._1_0.SubscriptionDeliveryMethodType;
import eu.essi_lab.ogc.pubsub._1_0.SubscriptionType;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;
import eu.essi_lab.profiler.pubsub.handler.SubscribeHandler;
import net.opengis.gml.v_3_2_1.TimeInstantType;
import net.opengis.gml.v_3_2_1.TimePositionType;
public class CSWSubscribeHandler extends SubscribeHandler {

    private static final String ROOT_PUBLICATION = "ROOT";
    private static final String EARTHQUAKE_PUBLICATION = "EARTHQUAKE";
    private static final String RANDOM_PUBLICATION = "RANDOM";
    private static final String CSW_SUBSCRIBE_ERROR = "CSW_SUBSCRIBE_ERROR";
    private static final String CSW_SUBSCRIBE_VALIDATION_ERROR = "CSW_SUBSCRIBE_VALIDATION_ERROR";
    private static final long DEFAULT_DURATION = 1000L * 60 * 60 * 24; // 24 hours

    /**
     * @author Fabrizio
     */
    public enum PublicationType {
	/**
	 *
	 */
	ROOT(""),
	/**
	 *
	 */
	EARTHQUAKE("usgs"),
	/**
	 *
	 */
	RANDOM("random");

	private String sourceId;

	private PublicationType(String sourceId) {

	    this.sourceId = sourceId;
	}

	public static PublicationType decode(String sourceId) {
	    switch (sourceId) {
	    case "":
		return ROOT;
	    case "usgs":
		return EARTHQUAKE;
	    case "random":
		return RANDOM;
	    default:
	    }
	    return null;
	}

	public String getSourceId() {

	    return sourceId;
	}}

    @Override
    public ValidationMessage validate(WebRequest webRequest) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	InputStream inputStream = webRequest.getBodyStream().clone();

	try {

	    JAXBElement<?> unmarsh = (JAXBElement<?>) CommonContext.createUnmarshaller().unmarshal(inputStream);
	    SubscribeType subscribeReq = (SubscribeType) unmarsh.getValue();

	    String publicationId = subscribeReq.getPublicationIdentifier();
	    switch (publicationId) {
	    case ROOT_PUBLICATION:
	    case EARTHQUAKE_PUBLICATION:
	    case RANDOM_PUBLICATION:
		break;
	    default:
		message.setError("Unsupported publication");
		message.setLocator("publication");
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }

	    // delivery method
	    String deliveryMethod = subscribeReq.getDeliveryMethod();
	    if (deliveryMethod != null && !deliveryMethod.equals(CSWSubscriptionsHandler.DELIVERY_METHOD)) {

		message.setError("Unsupported delivery method: " + deliveryMethod);
		message.setLocator("deliveryMethod");
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }

	    // filter language
	    String filterLanguageId = subscribeReq.getFilterLanguageId();
	    if (filterLanguageId != null && !filterLanguageId.equals(CSWSubscriptionsHandler.FILTER_LANGUAGE_ID)) {

		message.setError("Unsupported filter language: " + filterLanguageId);
		message.setLocator("filterLanguageId");
		message.setResult(ValidationResult.VALIDATION_FAILED);
		return message;
	    }

	    // filter
	    String filter = subscribeReq.getFilter();
	    if (filter != null) {
		PublicationType publicationType = PublicationType.valueOf(publicationId);

		switch (publicationType) {
		case ROOT:
		    // all filters allowed
		    break;
		case EARTHQUAKE:
		    if (!isEmptyFilter(filter) && !isBboxFilter(filter)) {
			// empty filter allowed
			// bbox filter allowed

			message.setError(
				"Unsupported filter for publication 'EARTHQUAKE': only 'empty_filter' and 'spatial_filter' supported");
			message.setLocator("filter");
			message.setResult(ValidationResult.VALIDATION_FAILED);
			return message;
		    }
		    break;
		case RANDOM:
		    if (!isEmptyFilter(filter)) {

			message.setError("Unsupported filter for publication 'RANDOM': only 'empty_filter' supported");
			message.setLocator("filter");
			message.setResult(ValidationResult.VALIDATION_FAILED);
			return message;
		    }
		    break;
		}
	    }
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    ex.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_SUBSCRIBE_VALIDATION_ERROR);
	}

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	InputStream inputStream = webRequest.getBodyStream().clone();

	String out = null;
	try {

	    JAXBElement<?> unmarsh = (JAXBElement<?>) CommonContext.createUnmarshaller().unmarshal(inputStream);
	    SubscribeType subscribeType = (SubscribeType) unmarsh.getValue();

	    // ---------------------------------
	    //
	    // creates the CSW response
	    //
	    SubscriptionType subscriptionType = ObjectFactories.PUB_SUB().createSubscriptionType();

	    // publication identifier is mandatory in the request
	    String publicationId = subscribeType.getPublicationIdentifier();
	    subscriptionType.setPublicationIdentifier(publicationId);

	    // subscription identifier
	    String subId = UUID.randomUUID().toString().substring(0, 4);
	    subscriptionType.setSubscriptionIdentifier(subId);

	    // delivery method
	    SubscriptionDeliveryMethodType deliveryMethodType = new SubscriptionDeliveryMethodType();
	    String deliveryMethod = subscribeType.getDeliveryMethod();
	    if (deliveryMethod == null) {
		deliveryMethod = CSWSubscriptionsHandler.DELIVERY_METHOD;
	    }
	    deliveryMethodType.setName(deliveryMethod);
	    subscriptionType.setDeliveryMethod(deliveryMethodType);

	    // filter language
	    String filterLanguageId = subscribeType.getFilterLanguageId();
	    if (filterLanguageId == null) {
		filterLanguageId = CSWSubscriptionsHandler.FILTER_LANGUAGE_ID;
	    }
	    subscriptionType.setFilterLanguageId(filterLanguageId);

	    // filter
	    String filter = subscribeType.getFilter();
	    if (filter != null) {

		PublicationType publicationType = PublicationType.valueOf(publicationId);
		// ROOT: no source
		// EARTHQUAKE: usgs source
		// RANDOM: random source
		if (publicationType != PublicationType.ROOT) {
		    String sourceId = publicationType.getSourceId();
		    filter = filter.replace("?", "?sources=" + sourceId + "&");
		}

		subscriptionType.setFilter(filter);
	    } else {
		throw GSException.createException(//
			getClass(), //
			"Filter can't be null", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CSW_SUBSCRIBE_ERROR);
	    }

	    // expiration
	    TimeInstantType expiration = subscribeType.getExpiration();
	    long creationMillis = System.currentTimeMillis();
	    long expirationMillis = creationMillis + DEFAULT_DURATION;

	    if (expiration == null || !expiration.isSetTimePosition()) {

		expiration = new TimeInstantType();
		expiration.setId(UUID.randomUUID().toString().substring(0, 6));

		String expirationISO = ISO8601DateTimeUtils.getISO8601DateTime(new Date(expirationMillis));

		TimePositionType tpt = new TimePositionType();
		tpt.setValue(Arrays.asList(expirationISO));

		expiration.setTimePosition(tpt);
	    }
	    subscriptionType.setExpiration(expiration);

	    // delivery location
	    String delLocation = webRequest.getUriInfo().getAbsolutePath().toString();

	    if (delLocation.endsWith("/")) {
		delLocation = delLocation.substring(0, delLocation.length() - 1);
	    }
	    delLocation = delLocation.replace("csw/pubsub/subscription", "pubsub/subscribe");
	    delLocation += "?";
	    delLocation += "request=subscribe";
	    delLocation += "&label=CSWSubscription_" + subId;
	    delLocation += "&clientID=CSWPublisher";
	    delLocation += "&subscriptionID=" + subId;
	    delLocation += "&init=true";
	    delLocation += "&creation=" + creationMillis;
	    delLocation += "&expiration=" + expirationMillis;

	    filter = filter.substring(subscribeType.getFilter().indexOf('?') + 1, filter.length());
	    delLocation += "&" + filter;

	    subscriptionType.setDeliveryLocation(delLocation);

	    SubscribeResponseType responseType = ObjectFactories.PUB_SUB().createSubscribeResponseType();
	    responseType.setSubscription(subscriptionType);

	    JAXBElement<SubscribeResponseType> response = ObjectFactories.PUB_SUB().createSubscribeResponse(responseType);

	    out = CommonContext.asString(response, true);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unexpected exception during subscribe", ex);

	    throw GSException.createException(//
		    getClass(), //
		    "Subscribe exception", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_SUBSCRIBE_ERROR);
	}

	return out;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }

    private boolean isEmptyFilter(String filter) {

	PubSubRequestParser parser = new PubSubRequestParser(filter);
	String bbox = parser.getParamValue(PubSubRequestParam.BBOX);
	String st = parser.getParamValue(PubSubRequestParam.SEARCH_TERMS);
	String ts = parser.getParamValue(PubSubRequestParam.TIME_START);
	String te = parser.getParamValue(PubSubRequestParam.TIME_END);

	return bbox.equals("") && st.equals("") && ts.equals("") && te.equals("");
    }

    private boolean isBboxFilter(String filter) {

	PubSubRequestParser parser = new PubSubRequestParser(filter);
	String bbox = parser.getParamValue(PubSubRequestParam.BBOX);
	String st = parser.getParamValue(PubSubRequestParam.SEARCH_TERMS);
	String ts = parser.getParamValue(PubSubRequestParam.TIME_START);
	String te = parser.getParamValue(PubSubRequestParam.TIME_END);

	return !bbox.equals("") && st.equals("") && ts.equals("") && te.equals("");
    }
}
