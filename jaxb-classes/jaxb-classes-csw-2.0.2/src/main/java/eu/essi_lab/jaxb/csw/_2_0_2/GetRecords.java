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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          The principal means of searching the catalogue. The matching 
 *          catalogue entries may be included with the response. The client 
 *          may assign a requestId (absolute URI). A distributed search is 
 *          performed if the DistributedSearch element is present and the 
 *          catalogue is a member of a federation. Profiles may allow 
 *          alternative query expressions.
 * 
 * <p>Classe Java per GetRecordsType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="GetRecordsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DistributedSearch" type="{http://www.opengis.net/cat/csw/2.0.2}DistributedSearchType" minOccurs="0"/&gt;
 *         &lt;element name="ResponseHandler" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}AbstractQuery"/&gt;
 *           &lt;any namespace='##other'/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/cat/csw/2.0.2}BasicRetrievalOptions"/&gt;
 *       &lt;attribute name="requestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="resultType" type="{http://www.opengis.net/cat/csw/2.0.2}ResultType" default="hits" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRecordsType", propOrder = {
    "distributedSearch",
    "responseHandlers",
    "any",
    "abstractQuery"
})
@XmlRootElement(name = "GetRecords")
public class GetRecords
    extends RequestBaseType
{

    @XmlElement(name = "DistributedSearch")
    protected DistributedSearchType distributedSearch;
    @XmlElement(name = "ResponseHandler")
    @XmlSchemaType(name = "anyURI")
    protected List<String> responseHandlers;
    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlElementRef(name = "AbstractQuery", namespace = "http://www.opengis.net/cat/csw/2.0.2", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends AbstractQueryType> abstractQuery;
    @XmlAttribute(name = "requestId")
    @XmlSchemaType(name = "anyURI")
    protected String requestId;
    @XmlAttribute(name = "resultType")
    protected ResultType resultType;
    @XmlAttribute(name = "outputFormat")
    protected String outputFormat;
    @XmlAttribute(name = "outputSchema")
    @XmlSchemaType(name = "anyURI")
    protected String outputSchema;
    @XmlAttribute(name = "startPosition")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger startPosition;
    @XmlAttribute(name = "maxRecords")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger maxRecords;

    /**
     * Recupera il valore della proprietà distributedSearch.
     * 
     * @return
     *     possible object is
     *     {@link DistributedSearchType }
     *     
     */
    public DistributedSearchType getDistributedSearch() {
        return distributedSearch;
    }

    /**
     * Imposta il valore della proprietà distributedSearch.
     * 
     * @param value
     *     allowed object is
     *     {@link DistributedSearchType }
     *     
     */
    public void setDistributedSearch(DistributedSearchType value) {
        this.distributedSearch = value;
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

    /**
     * Recupera il valore della proprietà any.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Imposta il valore della proprietà any.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Recupera il valore della proprietà abstractQuery.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AbstractQueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link QueryType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractQueryType> getAbstractQuery() {
        return abstractQuery;
    }

    /**
     * Imposta il valore della proprietà abstractQuery.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AbstractQueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link QueryType }{@code >}
     *     
     */
    public void setAbstractQuery(JAXBElement<? extends AbstractQueryType> value) {
        this.abstractQuery = value;
    }

    /**
     * Recupera il valore della proprietà requestId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Imposta il valore della proprietà requestId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Recupera il valore della proprietà resultType.
     * 
     * @return
     *     possible object is
     *     {@link ResultType }
     *     
     */
    public ResultType getResultType() {
        if (resultType == null) {
            return ResultType.HITS;
        } else {
            return resultType;
        }
    }

    /**
     * Imposta il valore della proprietà resultType.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultType }
     *     
     */
    public void setResultType(ResultType value) {
        this.resultType = value;
    }

    /**
     * Recupera il valore della proprietà outputFormat.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return "application/xml";
        } else {
            return outputFormat;
        }
    }

    /**
     * Imposta il valore della proprietà outputFormat.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputFormat(String value) {
        this.outputFormat = value;
    }

    /**
     * Recupera il valore della proprietà outputSchema.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * Imposta il valore della proprietà outputSchema.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputSchema(String value) {
        this.outputSchema = value;
    }

    /**
     * Recupera il valore della proprietà startPosition.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStartPosition() {
        if (startPosition == null) {
            return new BigInteger("1");
        } else {
            return startPosition;
        }
    }

    /**
     * Imposta il valore della proprietà startPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStartPosition(BigInteger value) {
        this.startPosition = value;
    }

    /**
     * Recupera il valore della proprietà maxRecords.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxRecords() {
        if (maxRecords == null) {
            return new BigInteger("10");
        } else {
            return maxRecords;
        }
    }

    /**
     * Imposta il valore della proprietà maxRecords.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxRecords(BigInteger value) {
        this.maxRecords = value;
    }

}
