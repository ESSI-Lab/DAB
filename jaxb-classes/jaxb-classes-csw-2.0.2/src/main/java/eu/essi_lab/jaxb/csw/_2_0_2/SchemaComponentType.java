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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

/**
 * A schema component includes a schema fragment (type
 * definition) or an entire schema from some target namespace;
 * the schema language is identified by URI. If the component
 * is a schema fragment its parent MUST be referenced (parentSchema).
 * <p>
 * Classe Java per SchemaComponentType complex type.
 * <p>
 * Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SchemaComponentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any processContents='lax'/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="targetNamespace" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="parentSchema" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="schemaLanguage" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemaComponentType", propOrder = { "content" })
@XmlRootElement(name = "SchemaComponent")
public class SchemaComponentType {

    @XmlMixed
    @XmlAnyElement(lax = true)
    protected List<Object> content;
    @XmlAttribute(name = "targetNamespace", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String targetNamespace;
    @XmlAttribute(name = "parentSchema")
    @XmlSchemaType(name = "anyURI")
    protected String parentSchema;
    @XmlAttribute(name = "schemaLanguage", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String schemaLanguage;

    /**
     * A schema component includes a schema fragment (type
     * definition) or an entire schema from some target namespace;
     * the schema language is identified by URI. If the component
     * is a schema fragment its parent MUST be referenced (parentSchema).Gets the value of the content property.
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * {@link String }
     */
    public List<Object> getContent() {
	if (content == null) {
	    content = new ArrayList<Object>();
	}
	return this.content;
    }

    /**
     * Recupera il valore della proprietà targetNamespace.
     * 
     * @return
     * 	possible object is
     *         {@link String }
     */
    public String getTargetNamespace() {
	return targetNamespace;
    }

    /**
     * Imposta il valore della proprietà targetNamespace.
     * 
     * @param value
     *        allowed object is
     *        {@link String }
     */
    public void setTargetNamespace(String value) {
	this.targetNamespace = value;
    }

    /**
     * Recupera il valore della proprietà parentSchema.
     * 
     * @return
     * 	possible object is
     *         {@link String }
     */
    public String getParentSchema() {
	return parentSchema;
    }

    /**
     * Imposta il valore della proprietà parentSchema.
     * 
     * @param value
     *        allowed object is
     *        {@link String }
     */
    public void setParentSchema(String value) {
	this.parentSchema = value;
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

    @Override
    public boolean equals(Object object) {

	SchemaComponentType type = (SchemaComponentType) object;
	return type.getTargetNamespace().equals(getTargetNamespace());
    }

    @Override
    public int hashCode() {

	return getTargetNamespace().hashCode();
    }

}
