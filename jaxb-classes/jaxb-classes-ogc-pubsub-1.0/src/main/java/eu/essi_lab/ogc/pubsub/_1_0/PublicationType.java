//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.09.10 alle 11:53:22 AM CEST 
//


package eu.essi_lab.ogc.pubsub._1_0;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.ows._1_0_0.BoundingBoxType;
import eu.essi_lab.jaxb.ows._1_0_0.DescriptionType;
import eu.essi_lab.jaxb.ows._1_0_0.MetadataType;


/**
 * <p>Classe Java per PublicationType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="PublicationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Identifier" type="{http://www.opengis.net/pubsub/1.0}PublicationIdentifierType"/&gt;
 *         &lt;element name="ContentType" type="{http://www.opengis.net/ows/1.1}MimeType"/&gt;
 *         &lt;element name="SupportedFilterLanguage" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="SupportedDeliveryMethod" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}BoundingBox" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="FormalContentDefinitionLanguage" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="FormalContentDefinition" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}Extension" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationType", propOrder = {
    "identifier",
    "contentType",
    "supportedFilterLanguage",
    "supportedDeliveryMethod",
    "boundingBox",
    "formalContentDefinitionLanguage",
    "formalContentDefinition",
    "metadata",
    "extension"
})
@XmlSeeAlso({
    DerivedPublicationType.class
})
public class PublicationType
    extends DescriptionType
{

    @XmlElement(name = "Identifier", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String identifier;
    @XmlElement(name = "ContentType", required = true)
    protected String contentType;
    @XmlElement(name = "SupportedFilterLanguage")
    @XmlSchemaType(name = "anyURI")
    protected List<String> supportedFilterLanguage;
    @XmlElement(name = "SupportedDeliveryMethod")
    @XmlSchemaType(name = "anyURI")
    protected List<String> supportedDeliveryMethod;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class, required = false)
    protected List<JAXBElement<BoundingBoxType>> boundingBox;
    @XmlElement(name = "FormalContentDefinitionLanguage")
    @XmlSchemaType(name = "anyURI")
    protected String formalContentDefinitionLanguage;
    @XmlElement(name = "FormalContentDefinition")
    protected Object formalContentDefinition;
    @XmlElement(name = "Metadata", namespace = "http://www.opengis.net/ows/1.1")
    protected List<MetadataType> metadata;
    @XmlElement(name = "Extension")
    protected List<Object> extension;

    /**
     * Recupera il valore della proprietà identifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Imposta il valore della proprietà identifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    public boolean isSetIdentifier() {
        return (this.identifier!= null);
    }

    /**
     * Recupera il valore della proprietà contentType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Imposta il valore della proprietà contentType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    public boolean isSetContentType() {
        return (this.contentType!= null);
    }

    /**
     * Gets the value of the supportedFilterLanguage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supportedFilterLanguage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupportedFilterLanguage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSupportedFilterLanguage() {
        if (supportedFilterLanguage == null) {
            supportedFilterLanguage = new ArrayList<String>();
        }
        return this.supportedFilterLanguage;
    }

    public boolean isSetSupportedFilterLanguage() {
        return ((this.supportedFilterLanguage!= null)&&(!this.supportedFilterLanguage.isEmpty()));
    }

    public void unsetSupportedFilterLanguage() {
        this.supportedFilterLanguage = null;
    }

    /**
     * Gets the value of the supportedDeliveryMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supportedDeliveryMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupportedDeliveryMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSupportedDeliveryMethod() {
        if (supportedDeliveryMethod == null) {
            supportedDeliveryMethod = new ArrayList<String>();
        }
        return this.supportedDeliveryMethod;
    }

    public boolean isSetSupportedDeliveryMethod() {
        return ((this.supportedDeliveryMethod!= null)&&(!this.supportedDeliveryMethod.isEmpty()));
    }

    public void unsetSupportedDeliveryMethod() {
        this.supportedDeliveryMethod = null;
    }

    /**
     * Gets the value of the boundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BoundingBoxType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<BoundingBoxType>> getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new ArrayList<JAXBElement<BoundingBoxType>>();
        }
        return this.boundingBox;
    }

    public boolean isSetBoundingBox() {
        return ((this.boundingBox!= null)&&(!this.boundingBox.isEmpty()));
    }

    public void unsetBoundingBox() {
        this.boundingBox = null;
    }

    /**
     * Recupera il valore della proprietà formalContentDefinitionLanguage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormalContentDefinitionLanguage() {
        return formalContentDefinitionLanguage;
    }

    /**
     * Imposta il valore della proprietà formalContentDefinitionLanguage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormalContentDefinitionLanguage(String value) {
        this.formalContentDefinitionLanguage = value;
    }

    public boolean isSetFormalContentDefinitionLanguage() {
        return (this.formalContentDefinitionLanguage!= null);
    }

    /**
     * Recupera il valore della proprietà formalContentDefinition.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getFormalContentDefinition() {
        return formalContentDefinition;
    }

    /**
     * Imposta il valore della proprietà formalContentDefinition.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setFormalContentDefinition(Object value) {
        this.formalContentDefinition = value;
    }

    public boolean isSetFormalContentDefinition() {
        return (this.formalContentDefinition!= null);
    }

    /**
     * Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataType }
     * 
     * 
     */
    public List<MetadataType> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<MetadataType>();
        }
        return this.metadata;
    }

    public boolean isSetMetadata() {
        return ((this.metadata!= null)&&(!this.metadata.isEmpty()));
    }

    public void unsetMetadata() {
        this.metadata = null;
    }

    /**
     * Gets the value of the extension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getExtension() {
        if (extension == null) {
            extension = new ArrayList<Object>();
        }
        return this.extension;
    }

    public boolean isSetExtension() {
        return ((this.extension!= null)&&(!this.extension.isEmpty()));
    }

    public void unsetExtension() {
        this.extension = null;
    }

}
