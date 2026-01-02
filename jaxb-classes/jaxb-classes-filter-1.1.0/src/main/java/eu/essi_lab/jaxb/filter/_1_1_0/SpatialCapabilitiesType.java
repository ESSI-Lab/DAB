//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:33:20 PM CEST 
//


package eu.essi_lab.jaxb.filter._1_1_0;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per Spatial_CapabilitiesType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="Spatial_CapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GeometryOperands" type="{http://www.opengis.net/ogc}GeometryOperandsType"/&gt;
 *         &lt;element name="SpatialOperators" type="{http://www.opengis.net/ogc}SpatialOperatorsType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Spatial_CapabilitiesType", propOrder = {
    "geometryOperands",
    "spatialOperators"
})
public class SpatialCapabilitiesType {

    @XmlElement(name = "GeometryOperands", required = true)
    protected GeometryOperandsType geometryOperands;
    @XmlElement(name = "SpatialOperators", required = true)
    protected SpatialOperatorsType spatialOperators;

    /**
     * Recupera il valore della proprietà geometryOperands.
     * 
     * @return
     *     possible object is
     *     {@link GeometryOperandsType }
     *     
     */
    public GeometryOperandsType getGeometryOperands() {
        return geometryOperands;
    }

    /**
     * Imposta il valore della proprietà geometryOperands.
     * 
     * @param value
     *     allowed object is
     *     {@link GeometryOperandsType }
     *     
     */
    public void setGeometryOperands(GeometryOperandsType value) {
        this.geometryOperands = value;
    }

    public boolean isSetGeometryOperands() {
        return (this.geometryOperands!= null);
    }

    /**
     * Recupera il valore della proprietà spatialOperators.
     * 
     * @return
     *     possible object is
     *     {@link SpatialOperatorsType }
     *     
     */
    public SpatialOperatorsType getSpatialOperators() {
        return spatialOperators;
    }

    /**
     * Imposta il valore della proprietà spatialOperators.
     * 
     * @param value
     *     allowed object is
     *     {@link SpatialOperatorsType }
     *     
     */
    public void setSpatialOperators(SpatialOperatorsType value) {
        this.spatialOperators = value;
    }

    public boolean isSetSpatialOperators() {
        return (this.spatialOperators!= null);
    }

}
