//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.03 at 12:55:12 PM CET 
//


package eu.essi_lab.ncml._2_2;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="readMetadata" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *           &lt;element name="explicit" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="iospParam" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}enumTypedef"/&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}group"/&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}dimension"/&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}variable"/&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}attribute"/&gt;
 *           &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}remove"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2}aggregation" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="location" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="enhance" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="addRecords" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="iosp" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="iospParam" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="bufferSize" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ncoords" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="coordValue" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="section" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "readMetadata",
    "explicit",
    "iospParam",
    "enumTypedefOrGroupOrDimension",
    "aggregation"
})
@XmlRootElement(name = "netcdf")
public class Netcdf {

    protected Object readMetadata;
    protected Object explicit;
    protected Object iospParam;
    @XmlElements({
        @XmlElement(name = "enumTypedef", type = EnumTypedef.class),
        @XmlElement(name = "group", type = Group.class),
        @XmlElement(name = "dimension", type = Dimension.class),
        @XmlElement(name = "variable", type = Variable.class),
        @XmlElement(name = "attribute", type = Attribute.class),
        @XmlElement(name = "remove", type = Remove.class)
    })
    protected List<Object> enumTypedefOrGroupOrDimension;
    protected Aggregation aggregation;
    @XmlAttribute(name = "location")
    @XmlSchemaType(name = "anyURI")
    protected String location;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "enhance")
    protected String enhance;
    @XmlAttribute(name = "addRecords")
    protected Boolean addRecords;
    @XmlAttribute(name = "iosp")
    protected String iosp;
    @XmlAttribute(name = "iospParam")
    protected String iospParamAttribute;
    @XmlAttribute(name = "bufferSize")
    protected Integer bufferSize;
    @XmlAttribute(name = "ncoords")
    protected String ncoords;
    @XmlAttribute(name = "coordValue")
    protected String coordValue;
    @XmlAttribute(name = "section")
    protected String section;

    /**
     * Gets the value of the readMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getReadMetadata() {
        return readMetadata;
    }

    /**
     * Sets the value of the readMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setReadMetadata(Object value) {
        this.readMetadata = value;
    }

    /**
     * Gets the value of the explicit property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getExplicit() {
        return explicit;
    }

    /**
     * Sets the value of the explicit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setExplicit(Object value) {
        this.explicit = value;
    }

    /**
     * Gets the value of the iospParam property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getIospParam() {
        return iospParam;
    }

    /**
     * Sets the value of the iospParam property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setIospParam(Object value) {
        this.iospParam = value;
    }

    /**
     * Gets the value of the enumTypedefOrGroupOrDimension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the enumTypedefOrGroupOrDimension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnumTypedefOrGroupOrDimension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnumTypedef }
     * {@link Group }
     * {@link Dimension }
     * {@link Variable }
     * {@link Attribute }
     * {@link Remove }
     * 
     * 
     */
    public List<Object> getEnumTypedefOrGroupOrDimension() {
        if (enumTypedefOrGroupOrDimension == null) {
            enumTypedefOrGroupOrDimension = new ArrayList<Object>();
        }
        return this.enumTypedefOrGroupOrDimension;
    }

    /**
     * Gets the value of the aggregation property.
     * 
     * @return
     *     possible object is
     *     {@link Aggregation }
     *     
     */
    public Aggregation getAggregation() {
        return aggregation;
    }

    /**
     * Sets the value of the aggregation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Aggregation }
     *     
     */
    public void setAggregation(Aggregation value) {
        this.aggregation = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the enhance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnhance() {
        return enhance;
    }

    /**
     * Sets the value of the enhance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnhance(String value) {
        this.enhance = value;
    }

    /**
     * Gets the value of the addRecords property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAddRecords() {
        return addRecords;
    }

    /**
     * Sets the value of the addRecords property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAddRecords(Boolean value) {
        this.addRecords = value;
    }

    /**
     * Gets the value of the iosp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIosp() {
        return iosp;
    }

    /**
     * Sets the value of the iosp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIosp(String value) {
        this.iosp = value;
    }

    /**
     * Gets the value of the iospParamAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIOSPParamAttribute() {
        return iospParamAttribute;
    }

    /**
     * Sets the value of the iospParamAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIOSPParamAttribute(String value) {
        this.iospParamAttribute = value;
    }

    /**
     * Gets the value of the bufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the value of the bufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBufferSize(Integer value) {
        this.bufferSize = value;
    }

    /**
     * Gets the value of the ncoords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNcoords() {
        return ncoords;
    }

    /**
     * Sets the value of the ncoords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNcoords(String value) {
        this.ncoords = value;
    }

    /**
     * Gets the value of the coordValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoordValue() {
        return coordValue;
    }

    /**
     * Sets the value of the coordValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoordValue(String value) {
        this.coordValue = value;
    }

    /**
     * Gets the value of the section property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSection() {
        return section;
    }

    /**
     * Sets the value of the section property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSection(String value) {
        this.section = value;
    }

}
