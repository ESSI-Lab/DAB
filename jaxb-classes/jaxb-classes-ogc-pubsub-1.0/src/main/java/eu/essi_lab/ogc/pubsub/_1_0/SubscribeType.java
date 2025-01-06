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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import net.opengis.gml.v_3_2_1.TimeInstantType;


/**
 * <p>Classe Java per SubscribeType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SubscribeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}PublicationIdentifier"/&gt;
 *         &lt;element name="Expiration" type="{http://www.opengis.net/gml/3.2}TimeInstantType" minOccurs="0"/&gt;
 *         &lt;element name="FilterLanguageId" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="Filter" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="DeliveryLocation" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="DeliveryMethod" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="DeliveryParameter" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}MessageBatchingCriteria" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}HeartbeatCriteria" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}Extension" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubscribeType", propOrder = {
    "publicationIdentifier",
    "expiration",
    "filterLanguageId",
    "filter",
    "deliveryLocation",
    "deliveryMethod",
    "deliveryParameter",
    "messageBatchingCriteria",
    "heartbeatCriteria",
    "extension"
})
public class SubscribeType {

    @XmlElement(name = "PublicationIdentifier", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String publicationIdentifier;
    @XmlElement(name = "Expiration")
    protected TimeInstantType expiration;
    @XmlElement(name = "FilterLanguageId")
    @XmlSchemaType(name = "anyURI")
    protected String filterLanguageId;
    @XmlElement(name = "Filter")
    @XmlSchemaType(name = "anyURI")
    protected String filter;
    @XmlElement(name = "DeliveryLocation")
    protected Object deliveryLocation;
    @XmlElement(name = "DeliveryMethod")
    @XmlSchemaType(name = "anyURI")
    protected String deliveryMethod;
    @XmlElement(name = "DeliveryParameter")
    protected List<Object> deliveryParameter;
    @XmlElement(name = "MessageBatchingCriteria")
    protected MessageBatchingCriteriaType messageBatchingCriteria;
    @XmlElement(name = "HeartbeatCriteria")
    protected HeartbeatCriteriaType heartbeatCriteria;
    @XmlElement(name = "Extension")
    protected List<Object> extension;

    /**
     * Recupera il valore della proprietà publicationIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicationIdentifier() {
        return publicationIdentifier;
    }

    /**
     * Imposta il valore della proprietà publicationIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicationIdentifier(String value) {
        this.publicationIdentifier = value;
    }

    public boolean isSetPublicationIdentifier() {
        return (this.publicationIdentifier!= null);
    }

    /**
     * Recupera il valore della proprietà expiration.
     * 
     * @return
     *     possible object is
     *     {@link TimeInstantType }
     *     
     */
    public TimeInstantType getExpiration() {
        return expiration;
    }

    /**
     * Imposta il valore della proprietà expiration.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeInstantType }
     *     
     */
    public void setExpiration(TimeInstantType value) {
        this.expiration = value;
    }

    public boolean isSetExpiration() {
        return (this.expiration!= null);
    }

    /**
     * Recupera il valore della proprietà filterLanguageId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilterLanguageId() {
        return filterLanguageId;
    }

    /**
     * Imposta il valore della proprietà filterLanguageId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilterLanguageId(String value) {
        this.filterLanguageId = value;
    }

    public boolean isSetFilterLanguageId() {
        return (this.filterLanguageId!= null);
    }

    /**
     * Recupera il valore della proprietà filter.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Imposta il valore della proprietà filter.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    public boolean isSetFilter() {
        return (this.filter!= null);
    }

    /**
     * Recupera il valore della proprietà deliveryLocation.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDeliveryLocation() {
        return deliveryLocation;
    }

    /**
     * Imposta il valore della proprietà deliveryLocation.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDeliveryLocation(Object value) {
        this.deliveryLocation = value;
    }

    public boolean isSetDeliveryLocation() {
        return (this.deliveryLocation!= null);
    }

    /**
     * Recupera il valore della proprietà deliveryMethod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Imposta il valore della proprietà deliveryMethod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeliveryMethod(String value) {
        this.deliveryMethod = value;
    }

    public boolean isSetDeliveryMethod() {
        return (this.deliveryMethod!= null);
    }

    /**
     * Gets the value of the deliveryParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deliveryParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeliveryParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getDeliveryParameter() {
        if (deliveryParameter == null) {
            deliveryParameter = new ArrayList<Object>();
        }
        return this.deliveryParameter;
    }

    public boolean isSetDeliveryParameter() {
        return ((this.deliveryParameter!= null)&&(!this.deliveryParameter.isEmpty()));
    }

    public void unsetDeliveryParameter() {
        this.deliveryParameter = null;
    }

    /**
     * Recupera il valore della proprietà messageBatchingCriteria.
     * 
     * @return
     *     possible object is
     *     {@link MessageBatchingCriteriaType }
     *     
     */
    public MessageBatchingCriteriaType getMessageBatchingCriteria() {
        return messageBatchingCriteria;
    }

    /**
     * Imposta il valore della proprietà messageBatchingCriteria.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageBatchingCriteriaType }
     *     
     */
    public void setMessageBatchingCriteria(MessageBatchingCriteriaType value) {
        this.messageBatchingCriteria = value;
    }

    public boolean isSetMessageBatchingCriteria() {
        return (this.messageBatchingCriteria!= null);
    }

    /**
     * Recupera il valore della proprietà heartbeatCriteria.
     * 
     * @return
     *     possible object is
     *     {@link HeartbeatCriteriaType }
     *     
     */
    public HeartbeatCriteriaType getHeartbeatCriteria() {
        return heartbeatCriteria;
    }

    /**
     * Imposta il valore della proprietà heartbeatCriteria.
     * 
     * @param value
     *     allowed object is
     *     {@link HeartbeatCriteriaType }
     *     
     */
    public void setHeartbeatCriteria(HeartbeatCriteriaType value) {
        this.heartbeatCriteria = value;
    }

    public boolean isSetHeartbeatCriteria() {
        return (this.heartbeatCriteria!= null);
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
