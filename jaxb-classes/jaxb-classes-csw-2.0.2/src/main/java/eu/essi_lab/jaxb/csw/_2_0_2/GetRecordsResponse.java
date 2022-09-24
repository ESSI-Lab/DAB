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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             The response message for a GetRecords request. Some or all of the 
 *             matching records may be included as children of the SearchResults 
 *             element. The RequestId is only included if the client specified it.
 *          
 * 
 * <p>Classe Java per GetRecordsResponseType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="GetRecordsResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RequestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/&gt;
 *         &lt;element name="SearchStatus" type="{http://www.opengis.net/cat/csw/2.0.2}RequestStatusType"/&gt;
 *         &lt;element name="SearchResults" type="{http://www.opengis.net/cat/csw/2.0.2}SearchResultsType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRecordsResponseType", propOrder = {
    "requestId",
    "searchStatus",
    "searchResults"
})
@XmlRootElement(name = "GetRecordsResponse")
public class GetRecordsResponse {

    @XmlElement(name = "RequestId")
    @XmlSchemaType(name = "anyURI")
    protected String requestId;
    @XmlElement(name = "SearchStatus", required = true)
    protected RequestStatusType searchStatus;
    @XmlElement(name = "SearchResults", required = true)
    protected SearchResultsType searchResults;
    @XmlAttribute(name = "version")
    protected String version;

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
     * Recupera il valore della proprietà searchStatus.
     * 
     * @return
     *     possible object is
     *     {@link RequestStatusType }
     *     
     */
    public RequestStatusType getSearchStatus() {
        return searchStatus;
    }

    /**
     * Imposta il valore della proprietà searchStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestStatusType }
     *     
     */
    public void setSearchStatus(RequestStatusType value) {
        this.searchStatus = value;
    }

    /**
     * Recupera il valore della proprietà searchResults.
     * 
     * @return
     *     possible object is
     *     {@link SearchResultsType }
     *     
     */
    public SearchResultsType getSearchResults() {
        return searchResults;
    }

    /**
     * Imposta il valore della proprietà searchResults.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchResultsType }
     *     
     */
    public void setSearchResults(SearchResultsType value) {
        this.searchResults = value;
    }

    /**
     * Recupera il valore della proprietà version.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Imposta il valore della proprietà version.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
