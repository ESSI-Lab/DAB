package eu.essi_lab.accessor.wms._1_3_0;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.essi_lab.accessor.wms.IWMSCapabilities;
import eu.essi_lab.accessor.wms.IWMSContact;
import eu.essi_lab.accessor.wms.IWMSLayer;
import eu.essi_lab.jaxb.wms._1_3_0.ContactInformation;
import eu.essi_lab.jaxb.wms._1_3_0.Layer;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;

public class WMS_1_3_0Capabilities implements IWMSCapabilities {

    private WMSCapabilities capabilities;

    public WMSCapabilities getCapabilities() {
	return capabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities) {
	this.capabilities = capabilities;
    }

    public WMS_1_3_0Capabilities(WMSCapabilities capabilities) {
	this();
	this.capabilities = capabilities;
    }

    public WMS_1_3_0Capabilities() {

    }

    @Override
    public void setServiceOnlineResource(String endpoint) {
	this.capabilities.getService().getOnlineResource().setHref(endpoint);

    }

    @Override
    public void setGetCapabilitiesOnlineResource(String endpoint) {
	this.capabilities.getCapability().getRequest().getGetCapabilities().getDCPTypes().get(0).getHTTP().getGet().getOnlineResource()
		.setHref(endpoint);

    }

    @Override
    public void setGetMapOnlineResource(String endpoint) {
	this.capabilities.getCapability().getRequest().getGetMap().getDCPTypes().get(0).getHTTP().getGet().getOnlineResource()
		.setHref(endpoint);
    }

    @Override
    public IWMSLayer getLayer(String name) {
	return new WMS_1_3_0Layer(this, name);
    }

    @Override
    public IWMSLayer addFirstLevelLayer(String name) {
	Layer newLayer = new Layer();
	if (name != null) {
	    newLayer.setName(name);
	    newLayer.setQueryable(true);
	    newLayer.setOpaque(false);
	}
	Layer parentLayer = capabilities.getCapability().getLayer();

	parentLayer.getLayers().add(newLayer);

	return new WMS_1_3_0Layer(this, newLayer);
    }

    @Override
    public IWMSLayer addLayer(String name, String parent) {
	Layer newLayer = new Layer();
	if (name != null) {
	    newLayer.setName(name);
	}
	if (parent == null) {
	    capabilities.getCapability().setLayer(newLayer);
	} else {
	    Layer parentLayer = getLayer(capabilities.getCapability().getLayer(), parent);
	    parentLayer.getLayers().add(newLayer);
	}
	return new WMS_1_3_0Layer(this, newLayer);

    }

    @Override
    public boolean hasLayer(String name) {
	Layer ret = getLayer(capabilities.getCapability().getLayer(), name);
	return ret != null;
    }

    @Override
    public List<String> getLayerNames() {
	List<String> ret = new ArrayList<String>();
	List<Layer> layers = getAllAccessibleLayers(capabilities.getCapability().getLayer());
	for (Layer layer : layers) {
	    ret.add(layer.getName());
	}
	return ret;
    }

    @Override
    public IWMSLayer getRootLayer() {
	return new WMS_1_3_0Layer(this, capabilities.getCapability().getLayer());
    }

    @Override
    public IWMSContact getContactInformation() {
	ContactInformation contact = capabilities.getService().getContactInformation();
	return new WMS_1_3_0Contact(contact);

    }

    @Override
    public String getServiceOnlineResource() {
	return capabilities.getService().getOnlineResource().getHref();
    }

    @Override
    public String getGetCapabilitiesOnlineResource() {
	try {
	    return capabilities.getCapability().getRequest().getGetCapabilities().getDCPTypes().get(0).getHTTP().getGet()
		    .getOnlineResource().getHref();
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public String getGetMapOnlineResource() {
	try {
	    return capabilities.getCapability().getRequest().getGetMap().getDCPTypes().get(0).getHTTP().getGet().getOnlineResource()
		    .getHref();
	} catch (Exception e) {
	    return null;
	}
    }

    protected List<Layer> getHierarchy(Layer parent, Layer layer) {
	List<Layer> ret = new ArrayList<Layer>();
	if (parent != null && Objects.equals(parent.getName(), layer.getName()) && Objects.equals(parent.getTitle(), layer.getTitle())) {
	    ret.add(parent);
	    return ret;
	} else {
	    for (Layer child : parent.getLayers()) {
		List<Layer> hierarchy = getHierarchy(child, layer);
		if (!hierarchy.isEmpty()) {
		    hierarchy.add(0, parent);
		    return hierarchy;
		}
	    }
	}
	return ret;
    }

    protected List<Layer> getHierarchy(Layer parent, String name) {
	List<Layer> ret = new ArrayList<Layer>();
	if (parent.getName() != null && parent.getName().equals(name)) {
	    ret.add(parent);
	    return ret;
	} else {
	    for (Layer child : parent.getLayers()) {
		List<Layer> hierarchy = getHierarchy(child, name);
		if (!hierarchy.isEmpty()) {
		    hierarchy.add(0, parent);
		    return hierarchy;
		}
	    }
	}
	return ret;
    }

    protected List<Layer> getAllAccessibleLayers(Layer layer) {

	List<Layer> ret = new ArrayList<Layer>();
	if (layer.getName() != null && !layer.getName().equals("")) {
	    ret.add(layer);
	}
	List<Layer> children = layer.getLayers();
	for (Layer child : children) {
	    ret.addAll(getAllAccessibleLayers(child));
	}
	return ret;
    }

    protected Layer getLayer(Layer layer, String name) {

	if (name == null) {
	    return layer;
	}

	if (layer.getName() != null && layer.getName().equals(name)) {
	    return layer;
	}
	for (Layer child : layer.getLayers()) {
	    Layer ret = getLayer(child, name);
	    if (ret != null) {
		return ret;
	    }
	}
	return null;
    }

    @Override
    public List<IWMSLayer> getAllAccessibleLayers() {
	List<Layer> layers = getAllAccessibleLayers(capabilities.getCapability().getLayer());
	List<IWMSLayer> ret = new ArrayList<IWMSLayer>();
	for (Layer layer : layers) {
	    ret.add(new WMS_1_3_0Layer(this, layer));
	}
	return ret;
    }

    @Override
    public List<String> getFormats() {
	return capabilities.getCapability().getRequest().getGetMap().getFormats();
    }

    @Override
    public String getFees() {
	return capabilities.getService().getFees();
    }

    @Override
    public String getAccessConstraints() {
	return capabilities.getService().getAccessConstraints();
    }

}
