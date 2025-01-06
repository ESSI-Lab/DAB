//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.11 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2017.01.05 alle 09:41:18 AM CET 
//


package eu.essi_lab.jaxb.oaipmh;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per IdentifyType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="IdentifyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="baseURL" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="protocolVersion" type="{http://www.openarchives.org/OAI/2.0/}protocolVersionType"/&gt;
 *         &lt;element name="adminEmail" type="{http://www.openarchives.org/OAI/2.0/}emailType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="earliestDatestamp" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType"/&gt;
 *         &lt;element name="deletedRecord" type="{http://www.openarchives.org/OAI/2.0/}deletedRecordType"/&gt;
 *         &lt;element name="granularity" type="{http://www.openarchives.org/OAI/2.0/}granularityType"/&gt;
 *         &lt;element name="compression" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="description" type="{http://www.openarchives.org/OAI/2.0/}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifyType", propOrder = {
    "repositoryName",
    "baseURL",
    "protocolVersion",
    "adminEmail",
    "earliestDatestamp",
    "deletedRecord",
    "granularity",
    "compression",
    "description"
})
@XmlRootElement
public class IdentifyType {

    @XmlElement(required = true)
    protected String repositoryName;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String baseURL;
    @XmlElement(required = true)
    protected String protocolVersion;
    @XmlElement(required = true)
    protected List<String> adminEmail;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String earliestDatestamp;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DeletedRecordType deletedRecord;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected GranularityType granularity;
    protected List<String> compression;
    protected List<DescriptionType> description;

    /**
     * Recupera il valore della proprietà repositoryName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Imposta il valore della proprietà repositoryName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryName(String value) {
        this.repositoryName = value;
    }

    /**
     * Recupera il valore della proprietà baseURL.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Imposta il valore della proprietà baseURL.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseURL(String value) {
        this.baseURL = value;
    }

    /**
     * Recupera il valore della proprietà protocolVersion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Imposta il valore della proprietà protocolVersion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocolVersion(String value) {
        this.protocolVersion = value;
    }

    /**
     * Gets the value of the adminEmail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the adminEmail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdminEmail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAdminEmail() {
        if (adminEmail == null) {
            adminEmail = new ArrayList<String>();
        }
        return this.adminEmail;
    }

    /**
     * Recupera il valore della proprietà earliestDatestamp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }

    /**
     * Imposta il valore della proprietà earliestDatestamp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEarliestDatestamp(String value) {
        this.earliestDatestamp = value;
    }

    /**
     * Recupera il valore della proprietà deletedRecord.
     * 
     * @return
     *     possible object is
     *     {@link DeletedRecordType }
     *     
     */
    public DeletedRecordType getDeletedRecord() {
        return deletedRecord;
    }

    /**
     * Imposta il valore della proprietà deletedRecord.
     * 
     * @param value
     *     allowed object is
     *     {@link DeletedRecordType }
     *     
     */
    public void setDeletedRecord(DeletedRecordType value) {
        this.deletedRecord = value;
    }

    /**
     * Recupera il valore della proprietà granularity.
     * 
     * @return
     *     possible object is
     *     {@link GranularityType }
     *     
     */
    public GranularityType getGranularity() {
        return granularity;
    }

    /**
     * Imposta il valore della proprietà granularity.
     * 
     * @param value
     *     allowed object is
     *     {@link GranularityType }
     *     
     */
    public void setGranularity(GranularityType value) {
        this.granularity = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the compression property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompression().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCompression() {
        if (compression == null) {
            compression = new ArrayList<String>();
        }
        return this.compression;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DescriptionType }
     * 
     * 
     */
    public List<DescriptionType> getDescription() {
        if (description == null) {
            description = new ArrayList<DescriptionType>();
        }
        return this.description;
    }

}
