//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.03 at 12:55:12 PM CET 
//


package eu.essi_lab.thredds._1_0_6;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FilterSelectorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterSelectorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="regExp" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wildcard" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="atomic" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="collection" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterSelectorType")
public class FilterSelectorType {

    @XmlAttribute(name = "regExp")
    protected String regExp;
    @XmlAttribute(name = "wildcard")
    protected String wildcard;
    @XmlAttribute(name = "atomic")
    protected Boolean atomic;
    @XmlAttribute(name = "collection")
    protected Boolean collection;

    /**
     * Gets the value of the regExp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegExp() {
        return regExp;
    }

    /**
     * Sets the value of the regExp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegExp(String value) {
        this.regExp = value;
    }

    /**
     * Gets the value of the wildcard property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWildcard() {
        return wildcard;
    }

    /**
     * Sets the value of the wildcard property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWildcard(String value) {
        this.wildcard = value;
    }

    /**
     * Gets the value of the atomic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAtomic() {
        return atomic;
    }

    /**
     * Sets the value of the atomic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAtomic(Boolean value) {
        this.atomic = value;
    }

    /**
     * Gets the value of the collection property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCollection() {
        return collection;
    }

    /**
     * Sets the value of the collection property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCollection(Boolean value) {
        this.collection = value;
    }

}
