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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import eu.essi_lab.jaxb.filter._1_1_0.SortByType;


/**
 * Specifies a query to execute against instances of one or
 *          more object types. A set of ElementName elements may be included 
 *          to specify an adhoc view of the csw:Record instances in the result 
 *          set. Otherwise, use ElementSetName to specify a predefined view. 
 *          The Constraint element contains a query filter expressed in a 
 *          supported query language. A sorting criterion that specifies a 
 *          property to sort by may be included.
 * 
 *          typeNames - a list of object types to query.
 * 
 * <p>Classe Java per QueryType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="QueryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractQueryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}ElementSetName"/&gt;
 *           &lt;element name="ElementName" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}Constraint" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ogc}SortBy" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="typeNames" use="required" type="{http://www.opengis.net/cat/csw/2.0.2}TypeNameListType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
    "elementNames",
    "elementSetName",
    "constraint",
    "sortBy"
})
public class QueryType
    extends AbstractQueryType
{

    @XmlElement(name = "ElementName")
    protected List<QName> elementNames;
    @XmlElement(name = "ElementSetName", defaultValue = "summary")
    protected ElementSetName elementSetName;
    @XmlElement(name = "Constraint")
    protected Constraint constraint;
    @XmlElement(name = "SortBy", namespace = "http://www.opengis.net/ogc")
    protected SortByType sortBy;
    @XmlAttribute(name = "typeNames", required = true)
    protected List<QName> typeNames;

    /**
     * Gets the value of the elementNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elementNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElementNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getElementNames() {
        if (elementNames == null) {
            elementNames = new ArrayList<QName>();
        }
        return this.elementNames;
    }

    /**
     * Recupera il valore della proprietà elementSetName.
     * 
     * @return
     *     possible object is
     *     {@link ElementSetName }
     *     
     */
    public ElementSetName getElementSetName() {
        return elementSetName;
    }

    /**
     * Imposta il valore della proprietà elementSetName.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementSetName }
     *     
     */
    public void setElementSetName(ElementSetName value) {
        this.elementSetName = value;
    }

    /**
     * Recupera il valore della proprietà constraint.
     * 
     * @return
     *     possible object is
     *     {@link Constraint }
     *     
     */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * Imposta il valore della proprietà constraint.
     * 
     * @param value
     *     allowed object is
     *     {@link Constraint }
     *     
     */
    public void setConstraint(Constraint value) {
        this.constraint = value;
    }

    /**
     * Recupera il valore della proprietà sortBy.
     * 
     * @return
     *     possible object is
     *     {@link SortByType }
     *     
     */
    public SortByType getSortBy() {
        return sortBy;
    }

    /**
     * Imposta il valore della proprietà sortBy.
     * 
     * @param value
     *     allowed object is
     *     {@link SortByType }
     *     
     */
    public void setSortBy(SortByType value) {
        this.sortBy = value;
    }

    /**
     * Gets the value of the typeNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the typeNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTypeNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getTypeNames() {
        if (typeNames == null) {
            typeNames = new ArrayList<QName>();
        }
        return this.typeNames;
    }

}
