package eu.essi_lab.profiler.pubsub;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * @author Fabrizio
 */
public class PubSubRequestParser {

    public enum PubSubRequestParam {
	/**
	 * Subscription parameter
	 * MANDATORY
	 * String: identifies the client type: must be "js" or "generic"
	 */
	CLIENT("client"),
	/**
	 * Subscription parameter
	 * MANDATORY
	 * Boolean: if true the the whole result set is notified just after the subscription
	 */
	INIT("init"),
	/**
	 * Subscription parameter
	 * MANDATORY
	 * String: the subscription identifier
	 */
	SUBSCRIPTION_ID("subscriptionID"),
	/**
	 * Subscription and unsubscription parameter
	 * MANDATORY
	 * String: the client identifier. It is required to group the subscriptions of the same client
	 */
	CLIENT_ID("clientID"),
	/**
	 * Subscription parameter
	 * MANDATORY
	 * String: the subscription label
	 */
	LABEL("label"),
	/**
	 * Subscription parameter
	 * MANDATORY
	 * Long: subscription creation
	 */
	CREATION("creation"),
	/**
	 * Subscription parameter
	 * MANDATORY
	 * Long: subscription expiration
	 */
	EXPIRATION("expiration"),
	/**
	 * Subscription parameter defined by subscription constraints
	 * OPTIONAL
	 */
	BBOX("bbox"),
	/**
	 * Subscription parameter defined by subscription constraints
	 * OPTIONAL
	 */
	TIME_START("ts"),
	/**
	 * Subscription parameter defined by subscription constraints
	 * OPTIONAL
	 */
	TIME_END("te"), //
	/**
	 * Subscription parameter defined by subscription constraints
	 * Also used as extension keywords
	 * OPTIONAL
	 */
	SEARCH_TERMS("st"),
	/**
	 * Subscription parameter defined by subscription constraints
	 * OPTIONAL
	 */
	PARENTS("parents"),

	/**
	 * Subscription parameter defined by subscription constraints (part of the kvp params)
	 * OPTIONAL
	 */
	SOURCES("sources"),

	/**
	 * Subscription parameter defined by subscription constraints
	 * OPTIONAL
	 */
	REQUEST_ID("reqID"),

	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	START_INDEX("si"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	COUNT("ct"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	SEARCH_FIELDS("searchFields"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	SPATIAL_RELATION("rel"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	TERM_FREQUENCY("tf"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	EXTENSION_RELATION("rela"),
	/**
	 * Subscription parameter defined by subscription options
	 * OPTIONAL
	 */
	EXTENSION_CONCEPTS("subj"),

	/**
	 *
	 */
	OUTPUT_FORMAT("outputFormat");

	private String param;

	private PubSubRequestParam(String param) {
	    this.param = param;
	}

	public String toString() {

	    return param;
	}
    }

    private StringTokenizer tokenizer;
    private String request;

    public PubSubRequestParser(String request) {
	this.request = request;
    }

    public String getParamValue(PubSubRequestParam param) {

	tokenizer = new StringTokenizer(request, "&");

	while (tokenizer.hasMoreTokens()) {
	    String token = tokenizer.nextToken();
	    if (token.startsWith(param.toString())) {
		String[] split = token.split("=");
		if (split.length > 1) {
		    try {
			return URLDecoder.decode(split[1], "UTF-8");
		    } catch (UnsupportedEncodingException e) {
		    }
		}

		return "";
	    }
	}

	return "";
    }
}
