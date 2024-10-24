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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;


/**
 * 
 *          Requests that the catalogue attempt to harvest a resource from some 
 *          network location identified by the source URL.
 * 
 *          Source          - a URL from which the resource is retrieved
 *          ResourceType    - normally a URI that specifies the type of the resource
 *                            (DCMES v1.1) being harvested if it is known.
 *          ResourceFormat  - a media type indicating the format of the 
 *                            resource being harvested.  The default is 
 *                            "application/xml".
 *          ResponseHandler - a reference to some endpoint to which the 
 *                            response shall be forwarded when the
 *                            harvest operation has been completed
 *          HarvestInterval - an interval expressed using the ISO 8601 syntax; 
 *                            it specifies the interval between harvest 
 *                            attempts (e.g., P6M indicates an interval of 
 *                            six months).
 *          
 * 
 * <p>Classe Java per HarvestType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="HarvestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Source" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="ResourceType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ResourceFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="HarvestInterval" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/&gt;
 *         &lt;element name="ResponseHandler" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HarvestType", propOrder = {
    "source",
    "resourceType",
    "resourceFormat",
    "harvestInterval",
    "responseHandlers"
})
@XmlRootElement(name = "Harvest")
public class Harvest
    extends RequestBaseType
{

    @XmlElement(name = "Source", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String source;
    @XmlElement(name = "ResourceType", required = true)
    protected String resourceType;
    @XmlElement(name = "ResourceFormat", defaultValue = "application/xml")
    protected String resourceFormat;
    @XmlElement(name = "HarvestInterval")
    protected Duration harvestInterval;
    @XmlElement(name = "ResponseHandler")
    @XmlSchemaType(name = "anyURI")
    protected List<String> responseHandlers;

    /**
     * Recupera il valore della proprietà source.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Imposta il valore della proprietà source.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Recupera il valore della proprietà resourceType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Imposta il valore della proprietà resourceType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceType(String value) {
        this.resourceType = value;
    }

    /**
     * Recupera il valore della proprietà resourceFormat.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceFormat() {
        return resourceFormat;
    }

    /**
     * Imposta il valore della proprietà resourceFormat.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceFormat(String value) {
        this.resourceFormat = value;
    }

    /**
     * Recupera il valore della proprietà harvestInterval.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getHarvestInterval() {
        return harvestInterval;
    }

    /**
     * Imposta il valore della proprietà harvestInterval.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setHarvestInterval(Duration value) {
        this.harvestInterval = value;
    }

    /**
     * Gets the value of the responseHandlers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the responseHandlers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponseHandlers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getResponseHandlers() {
        if (responseHandlers == null) {
            responseHandlers = new ArrayList<String>();
        }
        return this.responseHandlers;
    }

}
