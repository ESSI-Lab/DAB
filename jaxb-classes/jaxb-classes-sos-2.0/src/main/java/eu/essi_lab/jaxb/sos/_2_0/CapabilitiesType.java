//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.14 at 04:45:11 PM CEST 
//


package eu.essi_lab.jaxb.sos._2_0;

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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.CapabilitiesBaseType;


/**
 * <p>Java class for CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}CapabilitiesBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extension" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="filterCapabilities" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/fes/2.0}Filter_Capabilities"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="contents" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/sos/2.0}Contents"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CapabilitiesType", propOrder = {
    "extension",
    "filterCapabilities",
    "contents"
})
public class CapabilitiesType
    extends CapabilitiesBaseType
{

    protected List<Object> extension;
    protected CapabilitiesType.FilterCapabilities filterCapabilities;
    protected CapabilitiesType.Contents contents;

    /**
     * Gets the value of the extension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getExtension() {
        if (extension == null) {
            extension = new ArrayList<Object>();
        }
        return this.extension;
    }

    /**
     * Gets the value of the filterCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link CapabilitiesType.FilterCapabilities }
     *     
     */
    public CapabilitiesType.FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * Sets the value of the filterCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CapabilitiesType.FilterCapabilities }
     *     
     */
    public void setFilterCapabilities(CapabilitiesType.FilterCapabilities value) {
        this.filterCapabilities = value;
    }

    /**
     * Gets the value of the contents property.
     * 
     * @return
     *     possible object is
     *     {@link CapabilitiesType.Contents }
     *     
     */
    public CapabilitiesType.Contents getContents() {
        return contents;
    }

    /**
     * Sets the value of the contents property.
     * 
     * @param value
     *     allowed object is
     *     {@link CapabilitiesType.Contents }
     *     
     */
    public void setContents(CapabilitiesType.Contents value) {
        this.contents = value;
    }


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
     *         &lt;element ref="{http://www.opengis.net/sos/2.0}Contents"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "contents"
    })
    public static class Contents {

        @XmlElement(name = "Contents", required = true)
        protected ContentsType contents;

        /**
         * Gets the value of the contents property.
         * 
         * @return
         *     possible object is
         *     {@link ContentsType }
         *     
         */
        public ContentsType getContents() {
            return contents;
        }

        /**
         * Sets the value of the contents property.
         * 
         * @param value
         *     allowed object is
         *     {@link ContentsType }
         *     
         */
        public void setContents(ContentsType value) {
            this.contents = value;
        }

    }


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
     *         &lt;element ref="{http://www.opengis.net/fes/2.0}Filter_Capabilities"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "filterCapabilities"
    })
    public static class FilterCapabilities {

        @XmlElement(name = "Filter_Capabilities", namespace = "http://www.opengis.net/fes/2.0", required = true)
        protected eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities filterCapabilities;

        /**
         * Gets the value of the filterCapabilities property.
         * 
         * @return
         *     possible object is
         *     {@link eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities }
         *     
         */
        public eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities getFilterCapabilities() {
            return filterCapabilities;
        }

        /**
         * Sets the value of the filterCapabilities property.
         * 
         * @param value
         *     allowed object is
         *     {@link eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities }
         *     
         */
        public void setFilterCapabilities(eu.essi_lab.jaxb.sos._2_0.fes._2.FilterCapabilities value) {
            this.filterCapabilities = value;
        }

    }

}