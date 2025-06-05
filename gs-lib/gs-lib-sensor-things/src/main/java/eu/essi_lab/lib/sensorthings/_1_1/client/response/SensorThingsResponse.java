package eu.essi_lab.lib.sensorthings._1_1.client.response;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Optional;

import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResult;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;

/**
 * @author Fabrizio
 */
public interface SensorThingsResponse {

    /**
     * @param entityClass
     * @return
     */
    public <E extends Entity> Optional<AddressableEntityResult<E>> getAddressableEntityResult(Class<E> entityClass);

    /**
     * @return
     */
    public Optional<PropertyResult> getPropertyResult();

    /**
     * @return
     */
    public Optional<PropertyValueResult> getPropertyValueResult();

    /**
     * @return
     */
    public Optional<ServiceRootResult> getServiceRootResult();

    /**
     * @return
     */
    public Optional<AssociationLinkResult> getAssociationLinkResult();

    /**
     * @return
     */
    public Optional<DataArrayFormatResult> getDataArrayFormatResult();
}
