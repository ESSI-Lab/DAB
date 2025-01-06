package eu.essi_lab.lib.sensorthings._1_1.client.request;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.net.MalformedURLException;
import java.net.URL;

import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;

/**
 * https://docs.ogc.org/is/18-088/18-088.html#usage-address-entity
 * 
 * @author Fabrizio
 */
public class FluentSensorThingsRequest extends TerminalSensorThingsRequest {

    /**
     * @param serviceRootUrl
     */
    FluentSensorThingsRequest() {

	this(null, false);
    }

    /**
     * @param serviceRootUrl
     */
    FluentSensorThingsRequest(URL serviceRootUrl) {

	this(serviceRootUrl, false);
    }
       
    /**
     * @param quoteIdentifiers
     */
    FluentSensorThingsRequest(boolean quoteIdentifiers) {

	this(null, quoteIdentifiers);
    }

    /**
     * @param serviceRootUrl
     * @param quoteIdentifiers set to quote the entities identifiers between brace symbol “()”.
     *        E.g.: ('1') instead of (1)
     */
    FluentSensorThingsRequest(URL serviceRootUrl, boolean quoteIdentifiers) {

	super(serviceRootUrl, quoteIdentifiers);
    }  

    /**
     * @return
     */
    public static FluentSensorThingsRequest get() {

	return new FluentSensorThingsRequest();
    }

    /**
     * @return
     */
    public static FluentSensorThingsRequest get(URL serviceRootUrl) {

	return new FluentSensorThingsRequest(serviceRootUrl);
    }
    
    /**
     * @return
     * @throws MalformedURLException 
     */
    public static FluentSensorThingsRequest get(String serviceRootUrl) throws MalformedURLException {

	return new FluentSensorThingsRequest(new URL(serviceRootUrl));
    }

    /**
     * @param quoteIdentifiers set to quote the entities identifiers between brace symbol “()”.
     *        E.g.: ('1') instead of (1)
     * @return
     */
    public static FluentSensorThingsRequest get(boolean quoteIdentifiers) {

	return new FluentSensorThingsRequest(quoteIdentifiers);
    }

    /**
     * @param serviceRootUrl
     * @param quoteIdentifiers set to quote the entities identifiers between brace symbol “()”.
     *        E.g.: ('1') instead of (1)
     * @return
     */
    public static FluentSensorThingsRequest get(URL serviceRootUrl, boolean quoteIdentifiers) {

	return new FluentSensorThingsRequest(serviceRootUrl, quoteIdentifiers);
    }
    
    /**
     * @param serviceRootUrl
     * @param quoteIdentifiers set to quote the entities identifiers between brace symbol “()”.
     *        E.g.: ('1') instead of (1)
     * @return
     * @throws MalformedURLException 
     */
    public static FluentSensorThingsRequest get(String serviceRootUrl, boolean quoteIdentifiers) throws MalformedURLException {

	return new FluentSensorThingsRequest(new URL(serviceRootUrl), quoteIdentifiers);
    }

    /**
     * @param serviceRootUrl the serviceRootUrl to set
     * @return
     */
    public FluentSensorThingsRequest setServiceRootUrl(URL serviceRootUrl) {

	super.setServiceRootUrl(serviceRootUrl);
	return this;
    }

    /**
     * @param serviceRootUrl the serviceRootUrl to set
     * @return
     * @throws MalformedURLException
     */
    public FluentSensorThingsRequest setServiceRootUrl(String serviceRootUrl) throws MalformedURLException {

	super.setServiceRootUrl(serviceRootUrl);
	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-entity
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-navigation-property
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path
     * @param entityRef
     */
    public FluentSensorThingsRequest add(EntityRef entityRef) {

	_add(entityRef);
	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-entity
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-navigation-property
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path
     * @param entityRef
     * @param entityIdentifier
     */
    public FluentSensorThingsRequest add(EntityRef entityRef, String entityIdentifier) {

	_add(entityRef, entityIdentifier);
	return this;
    }

    /**
     * Set one of the two possible terminal path segments:
     * <ul>
     * <li>{@link AddressingPathSegment#ENTITY_SET_PROPERTY}</li>
     * <li>{@link AddressingPathSegment#ASSOCIATION_LINK}</li>
     * </ul>
     * Since this path segment is terminal, no other URI segments can follow, so this method returns an instance of
     * {@link TerminalSensorThingsRequest} (instead of {@link FluentSensorThingsRequest}) which do not allows to further
     * add
     * entity
     * set names or system query options
     * 
     * @see AddressingPathSegment
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-property-of-entity
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-value-of-property
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-associationlink
     * @see https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path
     * @param segment
     * @return
     */
    public SensorThingsRequest with(AddressingPathSegment segment) {

	_setAddressingPathSegment(segment);
	return this;
    }

    /**
     * Since these options are the query part of the URI, this method returns an instance of
     * {@link TerminalSensorThingsRequest} (instead of {@link FluentSensorThingsRequest}) which do not allows to further
     * add
     * entity
     * set names or system query options
     * 
     * @param options
     * @return
     */
    public SensorThingsRequest with(SystemQueryOptions options) {

	_setSystemQueryOptions(options);

	return this;
    }

    /**
     * @see https://docs.ogc.org/is/18-088/18-088.html#_request_4
     */
    public FluentSensorThingsRequest setDataArrayResultFormat() {

	_setDataArrayResultFormat();

	return this;
    }

    /**
     * @param
     * @return
     */
    public FluentSensorThingsRequest quoteIdentifiers(boolean quoteIdentifiers) {

	_setQuoteIdentifiers(quoteIdentifiers);
	return this;
    }

    @Override
    public String toString() {

	return compose();
    }
}
