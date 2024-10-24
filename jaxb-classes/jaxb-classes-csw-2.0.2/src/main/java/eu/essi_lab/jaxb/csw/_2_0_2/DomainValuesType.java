//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.06.08 alle 02:33:24 PM CEST 
//


package eu.essi_lab.jaxb.csw._2_0_2;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.namespace.QName;


/**
 * <p>Classe Java per DomainValuesType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="DomainValuesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *           &lt;element name="ParameterName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="ListOfValues" type="{http://www.opengis.net/cat/csw/2.0.2}ListOfValuesType"/&gt;
 *           &lt;element name="ConceptualScheme" type="{http://www.opengis.net/cat/csw/2.0.2}ConceptualSchemeType"/&gt;
 *           &lt;element name="RangeOfValues" type="{http://www.opengis.net/cat/csw/2.0.2}RangeOfValuesType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" /&gt;
 *       &lt;attribute name="uom" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DomainValuesType", propOrder = {
    "parameterName",
    "propertyName",
    "rangeOfValues",
    "conceptualScheme",
    "listOfValues"
})
public class DomainValuesType {

    @XmlElement(name = "ParameterName")
    @XmlSchemaType(name = "anyURI")
    protected String parameterName;
    @XmlElement(name = "PropertyName")
    @XmlSchemaType(name = "anyURI")
    protected String propertyName;
    @XmlElement(name = "RangeOfValues")
    protected RangeOfValuesType rangeOfValues;
    @XmlElement(name = "ConceptualScheme")
    protected ConceptualSchemeType conceptualScheme;
    @XmlElement(name = "ListOfValues")
    protected ListOfValuesType listOfValues;
    @XmlAttribute(name = "type", required = true)
    protected QName type;
    @XmlAttribute(name = "uom")
    @XmlSchemaType(name = "anyURI")
    protected String uom;

    /**
     * Recupera il valore della proprietà parameterName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Imposta il valore della proprietà parameterName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterName(String value) {
        this.parameterName = value;
    }

    /**
     * Recupera il valore della proprietà propertyName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Imposta il valore della proprietà propertyName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

    /**
     * Recupera il valore della proprietà rangeOfValues.
     * 
     * @return
     *     possible object is
     *     {@link RangeOfValuesType }
     *     
     */
    public RangeOfValuesType getRangeOfValues() {
        return rangeOfValues;
    }

    /**
     * Imposta il valore della proprietà rangeOfValues.
     * 
     * @param value
     *     allowed object is
     *     {@link RangeOfValuesType }
     *     
     */
    public void setRangeOfValues(RangeOfValuesType value) {
        this.rangeOfValues = value;
    }

    /**
     * Recupera il valore della proprietà conceptualScheme.
     * 
     * @return
     *     possible object is
     *     {@link ConceptualSchemeType }
     *     
     */
    public ConceptualSchemeType getConceptualScheme() {
        return conceptualScheme;
    }

    /**
     * Imposta il valore della proprietà conceptualScheme.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptualSchemeType }
     *     
     */
    public void setConceptualScheme(ConceptualSchemeType value) {
        this.conceptualScheme = value;
    }

    /**
     * Recupera il valore della proprietà listOfValues.
     * 
     * @return
     *     possible object is
     *     {@link ListOfValuesType }
     *     
     */
    public ListOfValuesType getListOfValues() {
        return listOfValues;
    }

    /**
     * Imposta il valore della proprietà listOfValues.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfValuesType }
     *     
     */
    public void setListOfValues(ListOfValuesType value) {
        this.listOfValues = value;
    }

    /**
     * Recupera il valore della proprietà type.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getType() {
        return type;
    }

    /**
     * Imposta il valore della proprietà type.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setType(QName value) {
        this.type = value;
    }

    /**
     * Recupera il valore della proprietà uom.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUom() {
        return uom;
    }

    /**
     * Imposta il valore della proprietà uom.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUom(String value) {
        this.uom = value;
    }

}
