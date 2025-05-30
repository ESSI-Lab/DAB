//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.14 at 04:45:11 PM CEST 
//


package eu.essi_lab.jaxb.sos._2_0;

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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.AbstractDataComponentType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.AbstractEncodingType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.AbstractSimpleComponentType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.BinaryEncodingType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.BooleanType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.CategoryRangeType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.CategoryType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.CountRangeType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.CountType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.DataArrayType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.DataChoiceType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.DataRecordType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.MatrixType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.QuantityRangeType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.QuantityType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.TextEncodingType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.TextType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.TimeRangeType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.TimeType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.VectorType;
import eu.essi_lab.jaxb.sos._2_0.swe._2.XMLEncodingType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractSWESType;


/**
 * <p>Java class for ResultTemplateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResultTemplateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/swes/2.0}AbstractSWESType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="offering" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="observationTemplate"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/om/2.0}OM_Observation"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="resultStructure"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/swe/2.0}AbstractDataComponent"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="resultEncoding"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/swe/2.0}AbstractEncoding"/&gt;
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
@XmlType(name = "ResultTemplateType", propOrder = {
    "offering",
    "observationTemplate",
    "resultStructure",
    "resultEncoding"
})
public class ResultTemplateType
    extends AbstractSWESType
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String offering;
    @XmlElement(required = true)
    protected ResultTemplateType.ObservationTemplate observationTemplate;
    @XmlElement(required = true)
    protected ResultTemplateType.ResultStructure resultStructure;
    @XmlElement(required = true)
    protected ResultTemplateType.ResultEncoding resultEncoding;

    /**
     * Gets the value of the offering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffering() {
        return offering;
    }

    /**
     * Sets the value of the offering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffering(String value) {
        this.offering = value;
    }

    /**
     * Gets the value of the observationTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link ResultTemplateType.ObservationTemplate }
     *     
     */
    public ResultTemplateType.ObservationTemplate getObservationTemplate() {
        return observationTemplate;
    }

    /**
     * Sets the value of the observationTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultTemplateType.ObservationTemplate }
     *     
     */
    public void setObservationTemplate(ResultTemplateType.ObservationTemplate value) {
        this.observationTemplate = value;
    }

    /**
     * Gets the value of the resultStructure property.
     * 
     * @return
     *     possible object is
     *     {@link ResultTemplateType.ResultStructure }
     *     
     */
    public ResultTemplateType.ResultStructure getResultStructure() {
        return resultStructure;
    }

    /**
     * Sets the value of the resultStructure property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultTemplateType.ResultStructure }
     *     
     */
    public void setResultStructure(ResultTemplateType.ResultStructure value) {
        this.resultStructure = value;
    }

    /**
     * Gets the value of the resultEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link ResultTemplateType.ResultEncoding }
     *     
     */
    public ResultTemplateType.ResultEncoding getResultEncoding() {
        return resultEncoding;
    }

    /**
     * Sets the value of the resultEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultTemplateType.ResultEncoding }
     *     
     */
    public void setResultEncoding(ResultTemplateType.ResultEncoding value) {
        this.resultEncoding = value;
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
     *         &lt;element ref="{http://www.opengis.net/om/2.0}OM_Observation"/&gt;
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
        "omObservation"
    })
    public static class ObservationTemplate {

        @XmlElement(name = "OM_Observation", namespace = "http://www.opengis.net/om/2.0", required = true)
        protected OMObservationType omObservation;

        /**
         * Gets the value of the omObservation property.
         * 
         * @return
         *     possible object is
         *     {@link OMObservationType }
         *     
         */
        public OMObservationType getOMObservation() {
            return omObservation;
        }

        /**
         * Sets the value of the omObservation property.
         * 
         * @param value
         *     allowed object is
         *     {@link OMObservationType }
         *     
         */
        public void setOMObservation(OMObservationType value) {
            this.omObservation = value;
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
     *         &lt;element ref="{http://www.opengis.net/swe/2.0}AbstractEncoding"/&gt;
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
        "abstractEncoding"
    })
    public static class ResultEncoding {

        @XmlElementRef(name = "AbstractEncoding", namespace = "http://www.opengis.net/swe/2.0", type = JAXBElement.class)
        protected JAXBElement<? extends AbstractEncodingType> abstractEncoding;

        /**
         * Gets the value of the abstractEncoding property.
         * 
         * @return
         *     possible object is
         *     {@link JAXBElement }{@code <}{@link BinaryEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link XMLEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TextEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractEncodingType }{@code >}
         *     
         */
        public JAXBElement<? extends AbstractEncodingType> getAbstractEncoding() {
            return abstractEncoding;
        }

        /**
         * Sets the value of the abstractEncoding property.
         * 
         * @param value
         *     allowed object is
         *     {@link JAXBElement }{@code <}{@link BinaryEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link XMLEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TextEncodingType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractEncodingType }{@code >}
         *     
         */
        public void setAbstractEncoding(JAXBElement<? extends AbstractEncodingType> value) {
            this.abstractEncoding = value;
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
     *         &lt;element ref="{http://www.opengis.net/swe/2.0}AbstractDataComponent"/&gt;
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
        "abstractDataComponent"
    })
    public static class ResultStructure {

        @XmlElementRef(name = "AbstractDataComponent", namespace = "http://www.opengis.net/swe/2.0", type = JAXBElement.class)
        protected JAXBElement<? extends AbstractDataComponentType> abstractDataComponent;

        /**
         * Gets the value of the abstractDataComponent property.
         * 
         * @return
         *     possible object is
         *     {@link JAXBElement }{@code <}{@link CategoryRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CategoryType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TimeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link QuantityType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TimeRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CountType }{@code >}
         *     {@link JAXBElement }{@code <}{@link QuantityRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TextType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CountRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractSimpleComponentType }{@code >}
         *     {@link JAXBElement }{@code <}{@link VectorType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataRecordType }{@code >}
         *     {@link JAXBElement }{@code <}{@link MatrixType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataArrayType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataChoiceType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractDataComponentType }{@code >}
         *     
         */
        public JAXBElement<? extends AbstractDataComponentType> getAbstractDataComponent() {
            return abstractDataComponent;
        }

        /**
         * Sets the value of the abstractDataComponent property.
         * 
         * @param value
         *     allowed object is
         *     {@link JAXBElement }{@code <}{@link CategoryRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CategoryType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TimeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link QuantityType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TimeRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CountType }{@code >}
         *     {@link JAXBElement }{@code <}{@link QuantityRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link TextType }{@code >}
         *     {@link JAXBElement }{@code <}{@link CountRangeType }{@code >}
         *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractSimpleComponentType }{@code >}
         *     {@link JAXBElement }{@code <}{@link VectorType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataRecordType }{@code >}
         *     {@link JAXBElement }{@code <}{@link MatrixType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataArrayType }{@code >}
         *     {@link JAXBElement }{@code <}{@link DataChoiceType }{@code >}
         *     {@link JAXBElement }{@code <}{@link AbstractDataComponentType }{@code >}
         *     
         */
        public void setAbstractDataComponent(JAXBElement<? extends AbstractDataComponentType> value) {
            this.abstractDataComponent = value;
        }

    }

}
