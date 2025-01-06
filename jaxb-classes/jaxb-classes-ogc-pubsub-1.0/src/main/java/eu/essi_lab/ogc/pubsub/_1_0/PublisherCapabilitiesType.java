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
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.ows._1_0_0.CapabilitiesBaseType;


/**
 * <p>Classe Java per PublisherCapabilitiesType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="PublisherCapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}CapabilitiesBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}FilterCapabilities"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}DeliveryCapabilities"/&gt;
 *         &lt;element ref="{http://www.opengis.net/pubsub/1.0}Publications"/&gt;
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
@XmlType(name = "PublisherCapabilitiesType", propOrder = {
    "filterCapabilities",
    "deliveryCapabilities",
    "publications",
    "extension"
})
public class PublisherCapabilitiesType
    extends CapabilitiesBaseType
{

    @XmlElement(name = "FilterCapabilities", required = true)
    protected FilterCapabilitiesType filterCapabilities;
    @XmlElement(name = "DeliveryCapabilities", required = true)
    protected DeliveryCapabilitiesType deliveryCapabilities;
    @XmlElement(name = "Publications", required = true)
    protected PublicationsType publications;
    @XmlElement(name = "Extension")
    protected List<Object> extension;

    /**
     * Recupera il valore della proprietà filterCapabilities.
     * 
     * @return
     *     possible object is
     *     {@link FilterCapabilitiesType }
     *     
     */
    public FilterCapabilitiesType getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * Imposta il valore della proprietà filterCapabilities.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterCapabilitiesType }
     *     
     */
    public void setFilterCapabilities(FilterCapabilitiesType value) {
        this.filterCapabilities = value;
    }

    public boolean isSetFilterCapabilities() {
        return (this.filterCapabilities!= null);
    }

    /**
     * Recupera il valore della proprietà deliveryCapabilities.
     * 
     * @return
     *     possible object is
     *     {@link DeliveryCapabilitiesType }
     *     
     */
    public DeliveryCapabilitiesType getDeliveryCapabilities() {
        return deliveryCapabilities;
    }

    /**
     * Imposta il valore della proprietà deliveryCapabilities.
     * 
     * @param value
     *     allowed object is
     *     {@link DeliveryCapabilitiesType }
     *     
     */
    public void setDeliveryCapabilities(DeliveryCapabilitiesType value) {
        this.deliveryCapabilities = value;
    }

    public boolean isSetDeliveryCapabilities() {
        return (this.deliveryCapabilities!= null);
    }

    /**
     * Recupera il valore della proprietà publications.
     * 
     * @return
     *     possible object is
     *     {@link PublicationsType }
     *     
     */
    public PublicationsType getPublications() {
        return publications;
    }

    /**
     * Imposta il valore della proprietà publications.
     * 
     * @param value
     *     allowed object is
     *     {@link PublicationsType }
     *     
     */
    public void setPublications(PublicationsType value) {
        this.publications = value;
    }

    public boolean isSetPublications() {
        return (this.publications!= null);
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
