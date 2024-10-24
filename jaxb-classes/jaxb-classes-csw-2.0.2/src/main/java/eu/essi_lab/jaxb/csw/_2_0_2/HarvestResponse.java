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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per HarvestResponseType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="HarvestResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}Acknowledgement"/&gt;
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}TransactionResponse"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HarvestResponseType", propOrder = {
    "transactionResponse",
    "acknowledgement"
})
@XmlRootElement(name = "HarvestResponse")
public class HarvestResponse {

    @XmlElement(name = "TransactionResponse")
    protected TransactionResponse transactionResponse;
    @XmlElement(name = "Acknowledgement")
    protected Acknowledgement acknowledgement;

    /**
     * Recupera il valore della proprietà transactionResponse.
     * 
     * @return
     *     possible object is
     *     {@link TransactionResponse }
     *     
     */
    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }

    /**
     * Imposta il valore della proprietà transactionResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionResponse }
     *     
     */
    public void setTransactionResponse(TransactionResponse value) {
        this.transactionResponse = value;
    }

    /**
     * Recupera il valore della proprietà acknowledgement.
     * 
     * @return
     *     possible object is
     *     {@link Acknowledgement }
     *     
     */
    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }

    /**
     * Imposta il valore della proprietà acknowledgement.
     * 
     * @param value
     *     allowed object is
     *     {@link Acknowledgement }
     *     
     */
    public void setAcknowledgement(Acknowledgement value) {
        this.acknowledgement = value;
    }

}
