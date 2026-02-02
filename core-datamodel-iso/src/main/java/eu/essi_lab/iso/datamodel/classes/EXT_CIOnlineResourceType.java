package eu.essi_lab.iso.datamodel.classes;

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

import net.opengis.iso19139.gmd.v_20060504.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "EXT_CIOnlineResource", namespace = "http://www.isotc211.org/2005/gmd")
public class EXT_CIOnlineResourceType extends CIOnlineResourceType {

    @XmlTransient
    private String queryStringFragment;

    @XmlTransient
    private String layerPk;

    @XmlTransient
    private boolean temporal;

    @XmlTransient
    private String layerStyleName;

    @XmlTransient
    private String layerStyleWorkspace;

    /**
     *
     */
    public EXT_CIOnlineResourceType() {
    }

    @XmlElement
    public String getQueryStringFragment() {

	return queryStringFragment;
    }

    /**
     * @param fragment
     */
    public void setQueryStringFragment(String fragment) {

	this.queryStringFragment = fragment;
    }

    /**
     * @return
     */
    @XmlElement
    public String getLayerPk() {

	return layerPk;
    }

    /**
     * @param layerPk
     */
    public void setLayerPk(String layerPk) {

	this.layerPk = layerPk;
    }

    @XmlElement
    public boolean isTemporal() {

	return temporal;
    }

    /**
     * @param temporal
     */
    public void setTemporal(boolean temporal) {

	this.temporal = temporal;
    }

    /**
     * @return
     */
    @XmlElement
    public String getLayerStyleName() {

	return layerStyleName;
    }

    /**
     * @param layerStyleName
     */
    public void setLayerStyleName(String layerStyleName) {

	this.layerStyleName = layerStyleName;
    }

    /**
     * @return
     */
    @XmlElement
    public String getLayerStyleWorkspace() {

	return layerStyleWorkspace;
    }

    /**
     * @param layerStyleWorkspace
     */
    public void setLayerStyleWorkspace(String layerStyleWorkspace) {

	this.layerStyleWorkspace = layerStyleWorkspace;
    }
}
