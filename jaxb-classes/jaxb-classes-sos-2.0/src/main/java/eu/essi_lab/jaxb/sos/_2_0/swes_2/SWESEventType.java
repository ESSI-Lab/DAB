//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.14 at 04:45:11 PM CEST 
//


package eu.essi_lab.jaxb.sos._2_0.swes_2;

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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import eu.essi_lab.jaxb.sos._2_0.addressing._2005.EndpointReferenceType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.LanguageStringType;


/**
 * <p>Java class for SWESEventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SWESEventType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/swes/2.0}AbstractSWESType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="eventTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="code" type="{http://www.opengis.net/swes/2.0}EventCodeType"/&gt;
 *         &lt;element name="message" type="{http://www.opengis.net/ows/1.1}LanguageStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="service"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.w3.org/2005/08/addressing}EndpointReference"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SWESEventType", propOrder = {
    "eventTime",
    "code",
    "message",
    "service"
})
@XmlSeeAlso({
    OfferingChangedType.class,
    SensorChangedType.class
})
public class SWESEventType
    extends AbstractSWESType
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar eventTime;
    @XmlElement(required = true)
    protected String code;
    protected List<LanguageStringType> message;
    @XmlElement(required = true)
    protected SWESEventType.Service service;

    /**
     * Gets the value of the eventTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEventTime() {
        return eventTime;
    }

    /**
     * Sets the value of the eventTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEventTime(XMLGregorianCalendar value) {
        this.eventTime = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the message property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LanguageStringType }
     * 
     * 
     */
    public List<LanguageStringType> getMessage() {
        if (message == null) {
            message = new ArrayList<LanguageStringType>();
        }
        return this.message;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link SWESEventType.Service }
     *     
     */
    public SWESEventType.Service getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link SWESEventType.Service }
     *     
     */
    public void setService(SWESEventType.Service value) {
        this.service = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.w3.org/2005/08/addressing}EndpointReference"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "endpointReference"
    })
    public static class Service {

        @XmlElement(name = "EndpointReference", namespace = "http://www.w3.org/2005/08/addressing", required = true)
        protected EndpointReferenceType endpointReference;

        /**
         * Gets the value of the endpointReference property.
         * 
         * @return
         *     possible object is
         *     {@link EndpointReferenceType }
         *     
         */
        public EndpointReferenceType getEndpointReference() {
            return endpointReference;
        }

        /**
         * Sets the value of the endpointReference property.
         * 
         * @param value
         *     allowed object is
         *     {@link EndpointReferenceType }
         *     
         */
        public void setEndpointReference(EndpointReferenceType value) {
            this.endpointReference = value;
        }

    }

}