package eu.essi_lab.iso.datamodel.classes;

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
