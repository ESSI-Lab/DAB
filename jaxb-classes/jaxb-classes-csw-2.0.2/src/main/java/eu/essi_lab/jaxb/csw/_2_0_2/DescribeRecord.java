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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * This request allows a user to discover elements of the
 * information model supported by the catalogue. If no TypeName
 * elements are included, then all of the schemas for the
 * information model must be returned.
 * schemaLanguage - preferred schema language
 * (W3C XML Schema by default)
 * outputFormat - preferred output format (application/xml by default)
 * <p>
 * Classe Java per DescribeRecordType complex type.
 * <p>
 * Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="DescribeRecordType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TypeName" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="outputFormat" type="{http://www.w3.org/2001/XMLSchema}string" default="application/xml" /&gt;
 *       &lt;attribute name="schemaLanguage" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://www.w3.org/XML/Schema" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeRecordType", propOrder = { "typeNames" })
@XmlRootElement(name = "DescribeRecord")
public class DescribeRecord extends RequestBaseType {

    @XmlElement(name = "TypeName")
    protected List<QName> typeNames;
    @XmlAttribute(name = "outputFormat")
    protected String outputFormat;
    @XmlAttribute(name = "schemaLanguage")
    @XmlSchemaType(name = "anyURI")
    protected String schemaLanguage;

    /**
     * Gets the value of the typeNames property.
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the typeNames property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getTypeNames().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     */
    public List<QName> getTypeNames() {
	if (typeNames == null) {
	    typeNames = new ArrayList<QName>();
	}
	return this.typeNames;
    }

    /**
     * Recupera il valore della proprietà outputFormat.
     * 
     * @return
     * 	possible object is
     *         {@link String }
     */
    public String getOutputFormat() {

	return outputFormat;
    }

    /**
     * Imposta il valore della proprietà outputFormat.
     * 
     * @param value
     *        allowed object is
     *        {@link String }
     */
    public void setOutputFormat(String value) {
	this.outputFormat = value;
    }

    /**
     * Recupera il valore della proprietà schemaLanguage.
     * 
     * @return
     * 	possible object is
     *         {@link String }
     */
    public String getSchemaLanguage() {

	return schemaLanguage;
    }

    /**
     * Imposta il valore della proprietà schemaLanguage.
     * 
     * @param value
     *        allowed object is
     *        {@link String }
     */
    public void setSchemaLanguage(String value) {
	this.schemaLanguage = value;
    }

}
