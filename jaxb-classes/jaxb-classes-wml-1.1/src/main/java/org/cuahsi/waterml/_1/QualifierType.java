//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.12.10 at 03:04:33 PM CET 
//


package org.cuahsi.waterml._1;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for QualifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualifierType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="qualifierCode" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="qualifierDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="qualifierID" type="{http://www.cuahsi.org/waterML/1.1/}positiveInt" /&gt;
 *       &lt;attribute ref="{http://www.cuahsi.org/waterML/1.1/}network"/&gt;
 *       &lt;attribute ref="{http://www.cuahsi.org/waterML/1.1/}vocabulary"/&gt;
 *       &lt;attribute ref="{http://www.cuahsi.org/waterML/1.1/}default"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualifierType", propOrder = {
    "qualifierCode",
    "qualifierDescription"
})
public class QualifierType {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String qualifierCode;
    protected String qualifierDescription;
    @XmlAttribute(name = "qualifierID")
    protected Integer qualifierID;
    @XmlAttribute(name = "network", namespace = "http://www.cuahsi.org/waterML/1.1/")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String network;
    @XmlAttribute(name = "vocabulary", namespace = "http://www.cuahsi.org/waterML/1.1/")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String vocabulary;
    @XmlAttribute(name = "default", namespace = "http://www.cuahsi.org/waterML/1.1/")
    protected Boolean _default;

    /**
     * Gets the value of the qualifierCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifierCode() {
        return qualifierCode;
    }

    /**
     * Sets the value of the qualifierCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifierCode(String value) {
        this.qualifierCode = value;
    }

    /**
     * Gets the value of the qualifierDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifierDescription() {
        return qualifierDescription;
    }

    /**
     * Sets the value of the qualifierDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifierDescription(String value) {
        this.qualifierDescription = value;
    }

    /**
     * Gets the value of the qualifierID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getQualifierID() {
        return qualifierID;
    }

    /**
     * Sets the value of the qualifierID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setQualifierID(Integer value) {
        this.qualifierID = value;
    }

    /**
     * Gets the value of the network property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Sets the value of the network property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetwork(String value) {
        this.network = value;
    }

    /**
     * Gets the value of the vocabulary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVocabulary() {
        return vocabulary;
    }

    /**
     * Sets the value of the vocabulary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVocabulary(String value) {
        this.vocabulary = value;
    }

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

}