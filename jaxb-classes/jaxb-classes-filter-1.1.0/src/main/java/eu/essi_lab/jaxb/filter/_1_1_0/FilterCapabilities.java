//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:33:20 PM CEST 
//


package eu.essi_lab.jaxb.filter._1_1_0;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per anonymous complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Spatial_Capabilities" type="{http://www.opengis.net/ogc}Spatial_CapabilitiesType"/&gt;
 *         &lt;element name="Scalar_Capabilities" type="{http://www.opengis.net/ogc}Scalar_CapabilitiesType"/&gt;
 *         &lt;element name="Id_Capabilities" type="{http://www.opengis.net/ogc}Id_CapabilitiesType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "spatialCapabilities",
    "scalarCapabilities",
    "idCapabilities"
})
@XmlRootElement(name = "Filter_Capabilities")
public class FilterCapabilities {

    @XmlElement(name = "Spatial_Capabilities", required = true)
    protected SpatialCapabilitiesType spatialCapabilities;
    @XmlElement(name = "Scalar_Capabilities", required = true)
    protected ScalarCapabilitiesType scalarCapabilities;
    @XmlElement(name = "Id_Capabilities", required = true)
    protected IdCapabilitiesType idCapabilities;

    /**
     * Recupera il valore della proprietà spatialCapabilities.
     * 
     * @return
     *     possible object is
     *     {@link SpatialCapabilitiesType }
     *     
     */
    public SpatialCapabilitiesType getSpatialCapabilities() {
        return spatialCapabilities;
    }

    /**
     * Imposta il valore della proprietà spatialCapabilities.
     * 
     * @param value
     *     allowed object is
     *     {@link SpatialCapabilitiesType }
     *     
     */
    public void setSpatialCapabilities(SpatialCapabilitiesType value) {
        this.spatialCapabilities = value;
    }

    public boolean isSetSpatialCapabilities() {
        return (this.spatialCapabilities!= null);
    }

    /**
     * Recupera il valore della proprietà scalarCapabilities.
     * 
     * @return
     *     possible object is
     *     {@link ScalarCapabilitiesType }
     *     
     */
    public ScalarCapabilitiesType getScalarCapabilities() {
        return scalarCapabilities;
    }

    /**
     * Imposta il valore della proprietà scalarCapabilities.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarCapabilitiesType }
     *     
     */
    public void setScalarCapabilities(ScalarCapabilitiesType value) {
        this.scalarCapabilities = value;
    }

    public boolean isSetScalarCapabilities() {
        return (this.scalarCapabilities!= null);
    }

    /**
     * Recupera il valore della proprietà idCapabilities.
     * 
     * @return
     *     possible object is
     *     {@link IdCapabilitiesType }
     *     
     */
    public IdCapabilitiesType getIdCapabilities() {
        return idCapabilities;
    }

    /**
     * Imposta il valore della proprietà idCapabilities.
     * 
     * @param value
     *     allowed object is
     *     {@link IdCapabilitiesType }
     *     
     */
    public void setIdCapabilities(IdCapabilitiesType value) {
        this.idCapabilities = value;
    }

    public boolean isSetIdCapabilities() {
        return (this.idCapabilities!= null);
    }

}
