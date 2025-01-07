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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Returns a "brief" view of any newly created catalogue records.
 *             The handle attribute may reference a particular statement in
 *             the corresponding transaction request.
 *          
 * 
 * <p>Classe Java per InsertResultType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="InsertResultType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}BriefRecord" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="handleRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InsertResultType", propOrder = {
    "briefRecords"
})
public class InsertResultType {

    @XmlElement(name = "BriefRecord", required = true)
    protected List<BriefRecordType> briefRecords;
    @XmlAttribute(name = "handleRef")
    @XmlSchemaType(name = "anyURI")
    protected String handleRef;

    /**
     * Gets the value of the briefRecords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the briefRecords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBriefRecords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BriefRecordType }
     * 
     * 
     */
    public List<BriefRecordType> getBriefRecords() {
        if (briefRecords == null) {
            briefRecords = new ArrayList<BriefRecordType>();
        }
        return this.briefRecords;
    }

    /**
     * Recupera il valore della proprietà handleRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandleRef() {
        return handleRef;
    }

    /**
     * Imposta il valore della proprietà handleRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandleRef(String value) {
        this.handleRef = value;
    }

}
