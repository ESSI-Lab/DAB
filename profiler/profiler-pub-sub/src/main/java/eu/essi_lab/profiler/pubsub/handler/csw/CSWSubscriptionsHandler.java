package eu.essi_lab.profiler.pubsub.handler.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.ogc.pubsub._1_0.GetSubscriptionResponseType;
import eu.essi_lab.ogc.pubsub._1_0.SubscriptionDeliveryMethodType;
import eu.essi_lab.ogc.pubsub._1_0.SubscriptionType;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;
import eu.essi_lab.profiler.pubsub.Subscription;
import eu.essi_lab.profiler.pubsub.handler.SubscriptionsHandler;
import eu.essi_lab.profiler.pubsub.handler.csw.CSWSubscribeHandler.PublicationType;
import net.opengis.gml.v_3_2_1.TimeInstantType;
import net.opengis.gml.v_3_2_1.TimePositionType;

/**
 * @author Fabrizio
 */
public class CSWSubscriptionsHandler extends SubscriptionsHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    static final String DELIVERY_METHOD = "http://www.w3.org/TR/eventsource/";
    static final String FILTER_LANGUAGE_ID = "http://www.opengis.net/spec/pubsub/1.0/conf/ows/request-reply-publisher";
    private static final String CSW_GET_SUBSCRIPTION_ERROR = "CSW_GET_SUBSCRIPTION_ERROR";

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String subscriptionId = getSubscriptionId(webRequest);

	JSONArray array = new JSONArray(getResponse(webRequest, subscriptionId));

	GetSubscriptionResponseType responseType = ObjectFactories.PUB_SUB().createGetSubscriptionResponseType();

	for (int i = 0; i < array.length(); i++) {

	    JSONObject jsonObject = array.getJSONObject(i);

	    Subscription subscription = new Subscription(jsonObject);

	    SubscriptionType subType = wrap(webRequest, subscription);
	    responseType.getSubscription().add(subType);
	}

	JAXBElement<GetSubscriptionResponseType> response = ObjectFactories.PUB_SUB().createGetSubscriptionResponse(responseType);

	String out = null;
	try {
	    out = CommonContext.asString(response, false);
	} catch (Exception ex) {

	    ex.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    ex.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_SUBSCRIPTION_ERROR);
	}

	return out;
    }

    private SubscriptionType wrap(WebRequest webRequest, Subscription subscription) {

	SubscriptionType subType = new SubscriptionType();

	// delivery location
	String delLocation = webRequest.getUriInfo().getAbsolutePath().toString();
	if (delLocation.endsWith("/")) {
	    delLocation = delLocation.substring(0, delLocation.length() - 1);
	}
	delLocation += "?";
	delLocation += "request=subscribe";
	delLocation += "&label=" + subscription.getLabel();
	delLocation += "&clientID=CSWPublisher";
	delLocation += "&subscriptionID=" + subscription.getId();
	delLocation += "&init=true";
	delLocation += "&creation=" + subscription.getCreationDate();
	delLocation += "&expiration=" + subscription.getExpirationDate();
	delLocation += "&" + subscription.getOpenSearchQuery();

	subType.setDeliveryLocation(delLocation);

	// delivery method
	SubscriptionDeliveryMethodType methodType = new SubscriptionDeliveryMethodType();
	methodType.setName(DELIVERY_METHOD);
	subType.setDeliveryMethod(methodType);

	// expiration
	TimeInstantType instantType = new TimeInstantType();
	TimePositionType positionType = new TimePositionType();
	String expiration = ISO8601DateTimeUtils.getISO8601DateTime(new Date(subscription.getExpirationDate()));
	positionType.setValue(Arrays.asList(expiration));
	instantType.setTimePosition(positionType);
	subType.setExpiration(instantType);

	// filter
	String filter = webRequest.getUriInfo().getAbsolutePath().toString().replace("csw/pubsub/subscription", "opensearch");
	if (filter.endsWith("/")) {
	    filter = filter.substring(0, filter.length() - 1);
	}
	filter += "?";
	filter += subscription.getOpenSearchQuery();
	subType.setFilter(filter);

	// filter language id
	subType.setFilterLanguageId(FILTER_LANGUAGE_ID);

	// subscription identifier
	subType.setSubscriptionIdentifier(subscription.getId());

	// publication identifier. it can be understood from the sources parameter
	PubSubRequestParser parser = new PubSubRequestParser(subscription.getOpenSearchQuery());
	String sources = parser.getParamValue(PubSubRequestParam.SOURCES);
	PublicationType publicationType = PublicationType.ROOT;
	if (sources != null) {
	    publicationType = PublicationType.decode(sources);
	}
	subType.setPublicationIdentifier(publicationType.name());

	// subType.setPaused(false);
	// subType.setHeartbeatCriteria(new HeartbeatCriteriaType());
	// subType.setMessageBatchingCriteria(new MessageBatchingCriteriaType());

	return subType;
    }

    protected String getSubscriptionId(WebRequest webRequest) {

	int size = webRequest.getUriInfo().getPathSegments().size();
	return webRequest.getUriInfo().getPathSegments().get(size - 1).toString();
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }

}
