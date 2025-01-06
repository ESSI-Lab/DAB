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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;
import eu.essi_lab.jaxb.ows._1_0_0.BoundingBoxType;


/**
 * 
 *             This type defines a summary representation of the common record
 *             format.  It extends AbstractRecordType to include the core
 *             properties.
 *          
 * 
 * <p>Classe Java per SummaryRecordType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SummaryRecordType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecordType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}identifier" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}title" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}type" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}subject" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}format" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}relation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/terms/}modified" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/terms/}abstract" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://purl.org/dc/terms/}spatial" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows}BoundingBox" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummaryRecordType", propOrder = {
    "identifiers",
    "titles",
    "type",
    "subjects",
    "formats",
    "relations",
    "modifieds",
    "abstracts",
    "spatials",
    "boundingBoxes"
})
@XmlRootElement(name="SummaryRecord")
public class SummaryRecordType
    extends AbstractRecordType
{

    @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected List<JAXBElement<SimpleLiteral>> identifiers;
    @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    protected List<JAXBElement<SimpleLiteral>> titles;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    protected SimpleLiteral type;
    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    protected List<SimpleLiteral> subjects;
    @XmlElementRef(name = "format", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class, required = false)
    protected List<JAXBElement<SimpleLiteral>> formats;
    @XmlElementRef(name = "relation", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class, required = false)
    protected List<JAXBElement<SimpleLiteral>> relations;
    @XmlElement(name = "modified", namespace = "http://purl.org/dc/terms/")
    protected List<SimpleLiteral> modifieds;
    @XmlElement(name = "abstract", namespace = "http://purl.org/dc/terms/")
    protected List<SimpleLiteral> abstracts;
    @XmlElement(name = "spatial", namespace = "http://purl.org/dc/terms/")
    protected List<SimpleLiteral> spatials;
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
     * Gets the value of the subjects property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subjects property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubjects().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleLiteral }
     * 
     * 
     */
    public List<SimpleLiteral> getSubjects() {
        if (subjects == null) {
            subjects = new ArrayList<SimpleLiteral>();
        }
        return this.subjects;
    }

    /**
     * Gets the value of the formats property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formats property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormats().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * 
     * 
     */
    public List<JAXBElement<SimpleLiteral>> getFormats() {
        if (formats == null) {
            formats = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return this.formats;
    }

    /**
     * Gets the value of the relations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * {@link JAXBElement }{@code <}{@link SimpleLiteral }{@code >}
     * 
     * 
     */
    public List<JAXBElement<SimpleLiteral>> getRelations() {
        if (relations == null) {
            relations = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return this.relations;
    }

    /**
     * Gets the value of the modifieds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifieds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifieds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleLiteral }
     * 
     * 
     */
    public List<SimpleLiteral> getModifieds() {
        if (modifieds == null) {
            modifieds = new ArrayList<SimpleLiteral>();
        }
        return this.modifieds;
    }

    /**
     * Gets the value of the abstracts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstracts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstracts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleLiteral }
     * 
     * 
     */
    public List<SimpleLiteral> getAbstracts() {
        if (abstracts == null) {
            abstracts = new ArrayList<SimpleLiteral>();
        }
        return this.abstracts;
    }

    /**
     * Gets the value of the spatials property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spatials property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpatials().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleLiteral }
     * 
     * 
     */
    public List<SimpleLiteral> getSpatials() {
        if (spatials == null) {
            spatials = new ArrayList<SimpleLiteral>();
        }
        return this.spatials;
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
