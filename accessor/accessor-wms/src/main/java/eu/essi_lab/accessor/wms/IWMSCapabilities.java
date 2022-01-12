package eu.essi_lab.accessor.wms;

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

import java.util.List;

public interface IWMSCapabilities {

    // GET
    
    public abstract List<IWMSLayer> getAllAccessibleLayers();
    
    public abstract List<String> getLayerNames();

    public abstract boolean hasLayer(String name);

    public abstract IWMSLayer getLayer(String name);

    public abstract IWMSLayer getRootLayer();

    public abstract IWMSContact getContactInformation();

    public abstract String getServiceOnlineResource();

    public abstract String getGetCapabilitiesOnlineResource();

    public abstract String getGetMapOnlineResource();

    public abstract List<String> getFormats();
    
    public abstract String getFees();
    
    public abstract String getAccessConstraints();

    // SET

    public abstract IWMSLayer addLayer(String name, String parent);

    public abstract void setServiceOnlineResource(String endpoint);

    public abstract void setGetCapabilitiesOnlineResource(String endpoint);

    public abstract void setGetMapOnlineResource(String endpoint);

    public abstract IWMSLayer addFirstLevelLayer(String layerName);


    

}
