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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;
import eu.essi_lab.jaxb.ows._1_0_0.BoundingBoxType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BriefRecordType", propOrder = {
    "identifiers",
    "titles",
    "type",
    "boundingBoxes"
})
@XmlRootElement(name="BriefRecord")
public class BriefRecordType
    extends AbstractRecordType
{

    @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected List<JAXBElement<SimpleLiteral>> identifiers;
    @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected List<JAXBElement<SimpleLiteral>> titles;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    protected SimpleLiteral type;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class, required = false)
    protected List<JAXBElement<BoundingBoxType>> boundingBoxes;

    /**
     * Gets the value of the identifiers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifiers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifiers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * 
     * 
     */
    public List<JAXBElement<SimpleLiteral>> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return this.identifiers;
    }

    /**
     * Gets the value of the titles property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the titles property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTitles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * 
     * 
     */
    public List<JAXBElement<SimpleLiteral>> getTitles() {
        if (titles == null) {
            titles = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return this.titles;
    }

    /**
     * Recupera il valore della proprietà type.
     * 
     * @return
     *     possible object is
     *     {@link SimpleLiteral }
     *     
     */
    public SimpleLiteral getType() {
        return type;
    }

    /**
     * Imposta il valore della proprietà type.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleLiteral }
     *     
     */
    public void setType(SimpleLiteral value) {
        this.type = value;
    }

    /**
     * Gets the value of the boundingBoxes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundingBoxes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundingBoxes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BoundingBoxType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<BoundingBoxType>> getBoundingBoxes() {
        if (boundingBoxes == null) {
            boundingBoxes = new ArrayList<JAXBElement<BoundingBoxType>>();
        }
        return this.boundingBoxes;
    }

}
