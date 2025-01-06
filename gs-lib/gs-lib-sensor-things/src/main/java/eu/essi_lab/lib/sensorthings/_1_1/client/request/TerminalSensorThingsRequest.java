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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;

/**
 * @author Fabrizio
 */
abstract class TerminalSensorThingsRequest implements SensorThingsRequest {

    /**
     * 
     */
    private URL serviceRootUrl;

    /**
     * 
     */
    private AddressingPathSegment segment;

    /**
     * 
     */
    private List<AddressableEntity> addressableEntities;

    /**
     * 
     */
    private SystemQueryOptions options;

    /**
     * 
     */
    private boolean quoteIdentifiers;

    /**
     * 
     */
    private boolean dataArray;

    /**
     * @param serviceRootUrl
     */
    TerminalSensorThingsRequest() {

	this(null, false);
    }

    /**
     * @param serviceRootUrl
     */
    TerminalSensorThingsRequest(URL serviceRootUrl) {

	this(serviceRootUrl, false);
    }

    /**
     * @param quoteIdentifiers
     */
    TerminalSensorThingsRequest(boolean quoteIdentifiers) {

	this(null, quoteIdentifiers);
    }

    /**
     * @param serviceRootUrl
     * @param quoteIdentifiers
     */
    TerminalSensorThingsRequest(URL serviceRootUrl, boolean quoteIdentifiers) {

	this.quoteIdentifiers = quoteIdentifiers;

	this.serviceRootUrl = serviceRootUrl;

	this.addressableEntities = new ArrayList<>();
    }

    /**
     * @param serviceRootUrl
     */
    public TerminalSensorThingsRequest setServiceRootUrl(URL serviceRootUrl) {

	this.serviceRootUrl = serviceRootUrl;
	return this;
    }
    
    /**
     * @param serviceRootUrl
     * @throws MalformedURLException 
     */
    public TerminalSensorThingsRequest setServiceRootUrl(String serviceRootUrl) throws MalformedURLException {

	this.serviceRootUrl = new URL(serviceRootUrl);
	return this;
    }

    /**
     * @return
     */
    public Optional<SystemQueryOptions> getSystemQueryOptions() {

	return Optional.ofNullable(options);
    }

    /**
     * @return
     */
    public boolean isDataArrayResponseFormatSet() {

	return dataArray;
    }

    /**
     * @return
     */
    public Optional<EntityProperty> getEntityProperty() {

	if (segment instanceof EntityProperty) {

	    return Optional.of((EntityProperty) segment);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public boolean isAddressAssociationLinkSet() {

	return segment instanceof AssociationLink;
    }

    /**
     * @return
     */
    public List<AddressableEntity> getAddressableEntityList() {

	return addressableEntities;
    }

    /**
     * @return
     */
    public boolean isQuoteIdentifiersSet() {

	return quoteIdentifiers;
    }

    /**
     * @return the serviceRootUrl
     */
    public URL getServiceRootUrl() {

	return serviceRootUrl;
    }

    @Override
    public String compose() {

	String entities = addressableEntities.stream().map(e -> e.compose()).collect(Collectors.joining("/"));

	boolean entitiesCollection = !addressableEntities.isEmpty()
		&& !addressableEntities.get(addressableEntities.size() - 1).getIdentifier().isPresent();

	if (dataArray && !addressableEntities.isEmpty()) {

	    if ((addressableEntities.get(addressableEntities.size() - 1).getEntityRef() != EntityRef.OBSERVATIONS) || //
		    !entitiesCollection) {

		throw new IllegalArgumentException("'dataArray' response format supported only for Observation entities collection");
	    }
	}

	if (dataArray && segment != null) {

	    throw new IllegalArgumentException(
		    "Addressing a property, a property value or an association link not allowed with 'dataArray' response format");

	}

	if (segment instanceof EntityProperty && entitiesCollection) {

	    throw new IllegalArgumentException(
		    "Addressing a property or a property value not allowed with an entitis collection, entity identifier is required");
	}

	if (segment instanceof AssociationLink && entities.isEmpty()) {

	    throw new IllegalArgumentException("At least one entity set must be provided to address an association link");
	}

	String terminalPathSegment = segment != null ? "/" + segment.compose() : "";

	String url = "";
	if (getServiceRootUrl() != null) {
	    url = getServiceRootUrl().toString().endsWith("/") ? getServiceRootUrl().toString() : getServiceRootUrl().toString() + "/";
	}

	StringBuilder builder = new StringBuilder();

	if (options != null) {

	    if (options.getFilter().isPresent() && !entitiesCollection) {

		throw new IllegalArgumentException("The 'filter' system option is allowed only with an entitis collection");
	    }

	    if (options.getExpandOptions().isPresent() && dataArray) {

		throw new IllegalArgumentException("'expand' system option not allowed with 'dataArray' response format");
	    }

	    builder.append(url);
	    builder.append(entities);
	    builder.append("?");
	    builder.append(options.compose());

	    if (dataArray) {

		builder.append("&$resultFormat=dataArray");
	    }

	} else {

	    builder.append(url);
	    builder.append(entities);
	    builder.append(terminalPathSegment);

	    if (dataArray) {

		builder.append("?");
		builder.append("$resultFormat=dataArray");
	    }
	}

	return builder.toString();
    }

    @Override
    public String toString() {

	return compose();
    }

    /**
     * @param entityRef
     */
    void _add(EntityRef entityRef) {

	addressableEntities.add(new AddressableEntity(entityRef));
    }

    /**
     * @param entityRef
     * @param entityIdentifier
     */
    void _add(EntityRef entityRef, String entityIdentifier) {

	addressableEntities.add(new AddressableEntity(entityRef, entityIdentifier, quoteIdentifiers));
    }

    /**
     * @param segment
     */
    void _setAddressingPathSegment(AddressingPathSegment segment) {

	this.segment = segment;
    }

    /**
     * @param quoteIdentifiers
     * @return
     */
    void _setQuoteIdentifiers(boolean quoteIdentifiers) {

	this.quoteIdentifiers = quoteIdentifiers;
    }

    /**
     * @param option
     * @return
     */
    void _setSystemQueryOptions(SystemQueryOptions options) {

	this.options = options;
    }

    /**
     * @see
     */
    void _setDataArrayResultFormat() {

	this.dataArray = true;
    }
}
