package eu.essi_lab.accessor.wms._1_1_1;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.jaxb.wms._1_1_1.ContactInformation;
import eu.essi_lab.jaxb.wms._1_1_1.Format;
import eu.essi_lab.jaxb.wms._1_1_1.Get;
import eu.essi_lab.jaxb.wms._1_1_1.Layer;
import eu.essi_lab.jaxb.wms._1_1_1.WMTMSCapabilities;

public class WMS_1_1_1Capabilities implements IWMSCapabilities {

    private WMTMSCapabilities capabilities;

    public WMTMSCapabilities getCapabilities() {
	return capabilities;
    }

    public void setCapabilities(WMTMSCapabilities capabilities) {
	this.capabilities = capabilities;
    }

    public WMS_1_1_1Capabilities(WMTMSCapabilities capabilities) {
	this();
	this.capabilities = capabilities;
    }

    public WMS_1_1_1Capabilities() {

    }

    @Override
    public void setServiceOnlineResource(String endpoint) {
	this.capabilities.getService().getOnlineResource().setXlinkHref(endpoint);

    }

    @Override
    public void setGetCapabilitiesOnlineResource(String endpoint) {
	((Get) this.capabilities.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGetOrPost().get(0))
		.getOnlineResource().setXlinkHref(endpoint);

    }

    @Override
    public void setGetMapOnlineResource(String endpoint) {
	((Get) this.capabilities.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGetOrPost().get(0))
		.getOnlineResource().setXlinkHref(endpoint);
    }

    @Override
    public IWMSLayer getLayer(String name) {
	return new WMS_1_1_1Layer(this, name);
    }

    @Override
    public IWMSLayer addFirstLevelLayer(String name) {
	Layer newLayer = new Layer();
	if (name != null) {
	    newLayer.setName(name);
	    newLayer.setQueryable("1");
	    newLayer.setOpaque("0");
	}
	Layer parentLayer = capabilities.getCapability().getLayer();

	parentLayer.getLayer().add(newLayer);

	return new WMS_1_1_1Layer(this, newLayer);
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
	    parentLayer.getLayer().add(newLayer);
	}
	return new WMS_1_1_1Layer(this, newLayer);

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
	return new WMS_1_1_1Layer(this, capabilities.getCapability().getLayer());
    }

    @Override
    public IWMSContact getContactInformation() {
	ContactInformation contact = capabilities.getService().getContactInformation();
	return new WMS_1_1_1Contact(contact);

    }

    @Override
    public String getServiceOnlineResource() {
	return capabilities.getService().getOnlineResource().getXlinkHref();
    }

    @Override
    public String getGetCapabilitiesOnlineResource() {
	try {
	    return ((Get) capabilities.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGetOrPost())
		    .getOnlineResource().getXlinkHref();
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public String getGetMapOnlineResource() {
	try {
	    return ((Get) capabilities.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGetOrPost().get(0))
		    .getOnlineResource().getXlinkHref();
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
	    for (Layer child : parent.getLayer()) {
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
	    for (Layer child : parent.getLayer()) {
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
	List<Layer> children = layer.getLayer();
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
	for (Layer child : layer.getLayer()) {
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
	    ret.add(new WMS_1_1_1Layer(this, layer));
	}
	return ret;
    }

    @Override
    public List<String> getFormats() {
	List<Format> formats = capabilities.getCapability().getRequest().getGetMap().getFormat();
	List<String> ret = new ArrayList<>();
	for (Format format : formats) {
	    ret.add(format.getvalue());
	}
	return ret;
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
