package eu.essi_lab.lib.sensorthings._1_1.client.response;

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

import java.util.Optional;

import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResult;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;

/**
 * @author Fabrizio
 */
public class SensorThingsResponseImpl implements SensorThingsResponse {

    private AddressableEntityResult<?> addressableEntityResult;
    private PropertyResult propertyResult;
    private PropertyValueResult propertyValueResult;
    private AssociationLinkResult associationLinkResult;
    private ServiceRootResult serviceRootResult;
    private DataArrayFormatResult dataArrayResult;

    /**
     * @param entityClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> Optional<AddressableEntityResult<E>> getAddressableEntityResult(Class<E> entityClass) {

	return Optional.ofNullable((AddressableEntityResult<E>) addressableEntityResult);
    }

    /**
     * @return
     */
    public Optional<PropertyResult> getPropertyResult() {

	return Optional.ofNullable(propertyResult);
    }

    /**
     * @return
     */
    public Optional<PropertyValueResult> getPropertyValueResult() {

	return Optional.ofNullable(propertyValueResult);
    }

    /**
     * @return
     */
    public Optional<ServiceRootResult> getServiceRootResult() {

	return Optional.ofNullable(serviceRootResult);
    }

    /**
     * @return
     */
    public Optional<AssociationLinkResult> getAssociationLinkResult() {

	return Optional.ofNullable(associationLinkResult);
    }

    /**
     * @return
     */
    public Optional<DataArrayFormatResult> getDataArrayFormatResult() {

	return Optional.ofNullable(dataArrayResult);
    }

    /**
     * @param serviceRootResult
     */
    public void setCapabilitiesResult(ServiceRootResult serviceRootResult) {

	this.serviceRootResult = serviceRootResult;
    }

    /**
     * @param addressableEntityResult
     */
    public void setAddressableEntityResult(AddressableEntityResult<?> entityResult) {

	this.addressableEntityResult = entityResult;
    }

    /**
     * @param propertyResult
     */
    public void setPropertyResult(PropertyResult propertyResult) {
	this.propertyResult = propertyResult;
    }

    /**
     * @param propertyValueResult
     */
    public void setPropertyValueResult(PropertyValueResult propertyValueResult) {

	this.propertyValueResult = propertyValueResult;
    }

    /**
     * @param associationLinkResult
     */
    public void setAssociationLinkResult(AssociationLinkResult associationLinkResult) {

	this.associationLinkResult = associationLinkResult;
    }

    /**
     * @param dataArrayFormatResult
     */
    public void setDataArrayFormatResult(DataArrayFormatResult dataArrayFormatResult) {

	this.dataArrayResult = dataArrayFormatResult;
    }

}
