package eu.essi_lab.lib.sensorthings._1_1.client.request;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;

/**
 * @author Fabrizio
 */
public interface SensorThingsRequest extends Composable {

    /**
     * @param url
     */
    SensorThingsRequest setServiceRootUrl(URL url);
    
    /**
     * @param url
     * @throws MalformedURLException 
     */
    SensorThingsRequest setServiceRootUrl(String url) throws MalformedURLException;

    /*
     * 
     */
    Optional<SystemQueryOptions> getSystemQueryOptions();

    /**
     * @return
     */
    boolean isDataArrayResponseFormatSet();

    /**
     * @return
     */
    Optional<EntityProperty> getEntityProperty();

    /**
     * @return
     */
    boolean isAddressAssociationLinkSet();

    /**
     * @return
     */
    List<AddressableEntity> getAddressableEntityList();

    /**
     * @return
     */
    boolean isQuoteIdentifiersSet();

    /**
     * @return
     */
    URL getServiceRootUrl();
}