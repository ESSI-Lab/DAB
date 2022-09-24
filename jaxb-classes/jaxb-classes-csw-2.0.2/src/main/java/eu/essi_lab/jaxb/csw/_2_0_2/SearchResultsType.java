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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Includes representations of result set members if maxRecords > 0.
 *          The items must conform to one of the csw:Record views or a 
 *          profile-specific representation. 
 *          
 *          resultSetId  - id of the result set (a URI).
 *          elementSet  - The element set that has been returned
 *                        (i.e., "brief", "summary", "full")
 *          recordSchema  - schema reference for included records(URI)
 *          numberOfRecordsMatched  - number of records matched by the query
 *          numberOfRecordsReturned - number of records returned to client
 *          nextRecord - position of next record in the result set
 *                       (0 if no records remain).
 *          expires - the time instant when the result set expires and 
 *                    is discarded (ISO 8601 format)
 * 
 * <p>Classe Java per SearchResultsType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SearchResultsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecord" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;any namespace='##other' maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="resultSetId" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="elementSet" type="{http://www.opengis.net/cat/csw/2.0.2}ElementSetType" /&gt;
 *       &lt;attribute name="recordSchema" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="numberOfRecordsMatched" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="numberOfRecordsReturned" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="nextRecord" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="expires" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchResultsType", propOrder = {
    "anies",
    "abstractRecords"
})
public class SearchResultsType {

    @XmlAnyElement(lax = true)
    protected List<Object> anies;
    @XmlElementRef(name = "AbstractRecord", namespace = "http://www.opengis.net/cat/csw/2.0.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractRecordType>> abstractRecords;
    @XmlAttribute(name = "resultSetId")
    @XmlSchemaType(name = "anyURI")
    protected String resultSetId;
    @XmlAttribute(name = "elementSet")
    protected ElementSetType elementSet;
    @XmlAttribute(name = "recordSchema")
    @XmlSchemaType(name = "anyURI")
    protected String recordSchema;
    @XmlAttribute(name = "numberOfRecordsMatched", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger numberOfRecordsMatched;
    @XmlAttribute(name = "numberOfRecordsReturned", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger numberOfRecordsReturned;
    @XmlAttribute(name = "nextRecord")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger nextRecord;
    @XmlAttribute(name = "expires")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expires;

    /**
     * Gets the value of the anies property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anies property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAnies() {
        if (anies == null) {
            anies = new ArrayList<Object>();
        }
        return this.anies;
    }

    /**
     * Gets the value of the abstractRecords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractRecords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractRecords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RecordType }{@code >}
     * {@link JAXBElement }{@code <}{@link DCMIRecordType }{@code >}
     * {@link JAXBElement }{@code <}{@link SummaryRecordType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractRecordType }{@code >}
     * {@link JAXBElement }{@code <}{@link BriefRecordType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractRecordType>> getAbstractRecords() {
        if (abstractRecords == null) {
            abstractRecords = new ArrayList<JAXBElement<? extends AbstractRecordType>>();
        }
        return this.abstractRecords;
    }

    /**
     * Recupera il valore della proprietà resultSetId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultSetId() {
        return resultSetId;
    }

    /**
     * Imposta il valore della proprietà resultSetId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultSetId(String value) {
        this.resultSetId = value;
    }

    /**
     * Recupera il valore della proprietà elementSet.
     * 
     * @return
     *     possible object is
     *     {@link ElementSetType }
     *     
     */
    public ElementSetType getElementSet() {
        return elementSet;
    }

    /**
     * Imposta il valore della proprietà elementSet.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementSetType }
     *     
     */
    public void setElementSet(ElementSetType value) {
        this.elementSet = value;
    }

    /**
     * Recupera il valore della proprietà recordSchema.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSchema() {
        return recordSchema;
    }

    /**
     * Imposta il valore della proprietà recordSchema.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSchema(String value) {
        this.recordSchema = value;
    }

    /**
     * Recupera il valore della proprietà numberOfRecordsMatched.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfRecordsMatched() {
        return numberOfRecordsMatched;
    }

    /**
     * Imposta il valore della proprietà numberOfRecordsMatched.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfRecordsMatched(BigInteger value) {
        this.numberOfRecordsMatched = value;
    }

    /**
     * Recupera il valore della proprietà numberOfRecordsReturned.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfRecordsReturned() {
        return numberOfRecordsReturned;
    }

    /**
     * Imposta il valore della proprietà numberOfRecordsReturned.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfRecordsReturned(BigInteger value) {
        this.numberOfRecordsReturned = value;
    }

    /**
     * Recupera il valore della proprietà nextRecord.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNextRecord() {
        return nextRecord;
    }

    /**
     * Imposta il valore della proprietà nextRecord.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNextRecord(BigInteger value) {
        this.nextRecord = value;
    }

    /**
     * Recupera il valore della proprietà expires.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpires() {
        return expires;
    }

    /**
     * Imposta il valore della proprietà expires.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpires(XMLGregorianCalendar value) {
        this.expires = value;
    }

}
