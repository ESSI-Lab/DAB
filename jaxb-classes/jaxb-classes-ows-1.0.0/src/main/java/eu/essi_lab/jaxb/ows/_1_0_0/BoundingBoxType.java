//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:24:35 PM CEST 
//


package eu.essi_lab.jaxb.ows._1_0_0;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundingBoxType", propOrder = {
    "lowerCorner",
    "upperCorner"
})
@XmlSeeAlso({
    WGS84BoundingBoxType.class
})
@XmlRootElement
public class BoundingBoxType {

    @XmlList
    @XmlElement(name = "LowerCorner", type = Double.class)
    @XmlSchemaType(name = "anySimpleType")
    protected List<String> lowerCorner;
    @XmlList
    @XmlElement(name = "UpperCorner", type = Double.class)
    @XmlSchemaType(name = "anySimpleType")
    protected List<String> upperCorner;
    @XmlAttribute(name = "crs")
    @XmlSchemaType(name = "anyURI")
    protected String crs;
    @XmlAttribute(name = "dimensions")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger dimensions;

    /**
     * Gets the value of the lowerCorner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lowerCorner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLowerCorner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<String> getLowerCorner() {
        if (lowerCorner == null) {
            lowerCorner = new ArrayList<String>();
        }
        return this.lowerCorner;
    }

    /**
     * Gets the value of the upperCorner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the upperCorner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUpperCorner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<String> getUpperCorner() {
        if (upperCorner == null) {
            upperCorner = new ArrayList<String>();
        }
        return this.upperCorner;
    }

    /**
     * Recupera il valore della proprietà crs.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Imposta il valore della proprietà crs.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrs(String value) {
        this.crs = value;
    }

    /**
     * Recupera il valore della proprietà dimensions.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDimensions() {
        return dimensions;
    }

    /**
     * Imposta il valore della proprietà dimensions.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDimensions(BigInteger value) {
        this.dimensions = value;
    }

}
