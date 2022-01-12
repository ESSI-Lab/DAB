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
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionType", propOrder = {
    "insertsAndUpdatesAndDeletes"
})
@XmlRootElement(name = "Transaction")
public class Transaction
    extends RequestBaseType
{

    @XmlElements({
        @XmlElement(name = "Insert", type = InsertType.class),
        @XmlElement(name = "Update", type = UpdateType.class),
        @XmlElement(name = "Delete", type = DeleteType.class)
    })
    protected List<Object> insertsAndUpdatesAndDeletes;
    @XmlAttribute(name = "verboseResponse")
    protected Boolean verboseResponse;
    @XmlAttribute(name = "requestId")
    @XmlSchemaType(name = "anyURI")
    protected String requestId;

    /**
     * Gets the value of the insertsAndUpdatesAndDeletes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the insertsAndUpdatesAndDeletes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInsertsAndUpdatesAndDeletes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InsertType }
     * {@link UpdateType }
     * {@link DeleteType }
     * 
     * 
     */
    public List<Object> getInsertsAndUpdatesAndDeletes() {
        if (insertsAndUpdatesAndDeletes == null) {
            insertsAndUpdatesAndDeletes = new ArrayList<Object>();
        }
        return this.insertsAndUpdatesAndDeletes;
    }

    /**
     * Recupera il valore della proprietà verboseResponse.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVerboseResponse() {
        if (verboseResponse == null) {
            return false;
        } else {
            return verboseResponse;
        }
    }

    /**
     * Imposta il valore della proprietà verboseResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVerboseResponse(Boolean value) {
        this.verboseResponse = value;
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

}
