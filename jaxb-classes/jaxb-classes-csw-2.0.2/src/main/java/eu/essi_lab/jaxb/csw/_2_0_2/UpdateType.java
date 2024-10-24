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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *             Update statements may replace an entire record or only update part 
 *             of a record:
 *             1) To replace an existing record, include a new instance of the 
 *                record;
 *             2) To update selected properties of an existing record, include
 *                a set of RecordProperty elements. The scope of the update
 *                statement  is determined by the Constraint element.
 *                The 'handle' is a local identifier for the action.
 *          
 * 
 * <p>Classe Java per UpdateType complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="UpdateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;any namespace='##other'/&gt;
 *           &lt;sequence&gt;
 *             &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}RecordProperty" maxOccurs="unbounded"/&gt;
 *             &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}Constraint"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateType", propOrder = {
    "recordProperties",
    "constraint",
    "any"
})
public class UpdateType {

    @XmlElement(name = "RecordProperty")
    protected List<RecordProperty> recordProperties;
    @XmlElement(name = "Constraint")
    protected Constraint constraint;
    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlAttribute(name = "handle")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String handle;

    /**
     * Gets the value of the recordProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recordProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecordProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecordProperty }
     * 
     * 
     */
    public List<RecordProperty> getRecordProperties() {
        if (recordProperties == null) {
            recordProperties = new ArrayList<RecordProperty>();
        }
        return this.recordProperties;
    }

    /**
     * Recupera il valore della proprietà constraint.
     * 
     * @return
     *     possible object is
     *     {@link Constraint }
     *     
     */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * Imposta il valore della proprietà constraint.
     * 
     * @param value
     *     allowed object is
     *     {@link Constraint }
     *     
     */
    public void setConstraint(Constraint value) {
        this.constraint = value;
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
     * Recupera il valore della proprietà handle.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Imposta il valore della proprietà handle.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandle(String value) {
        this.handle = value;
    }

}
