//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.09.12 at 03:34:17 PM CEST 
//


package eu.floraresearch.wigos._1_0.main;

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

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import eu.floraresearch.wigos._1_0.gml._3_2_1.MeasureType;
import eu.floraresearch.wigos._1_0.gml._3_2_1.ReferenceType;


/**
 * <p>Java class for SamplingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SamplingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="samplingStrategy" type="{http://www.opengis.net/gml/3.2}ReferenceType" minOccurs="0"/&gt;
 *         &lt;element name="samplingProcedure" type="{http://www.opengis.net/gml/3.2}ReferenceType" minOccurs="0"/&gt;
 *         &lt;element name="samplingProcedureDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="sampleTreatment" type="{http://www.opengis.net/gml/3.2}ReferenceType" minOccurs="0"/&gt;
 *         &lt;element name="temporalSamplingInterval" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/&gt;
 *         &lt;element name="samplingTimePeriod" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/&gt;
 *         &lt;element name="spatialSamplingResolution" type="{http://www.opengis.net/gml/3.2}MeasureType" minOccurs="0"/&gt;
 *         &lt;element name="spatialSamplingResolutionDetails" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="samplesPerTimePeriod" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SamplingType", propOrder = {
    "samplingStrategy",
    "samplingProcedure",
    "samplingProcedureDescription",
    "sampleTreatment",
    "temporalSamplingInterval",
    "samplingTimePeriod",
    "spatialSamplingResolution",
    "spatialSamplingResolutionDetails",
    "samplesPerTimePeriod"
})
public class SamplingType {

    protected ReferenceType samplingStrategy;
    protected ReferenceType samplingProcedure;
    protected String samplingProcedureDescription;
    protected ReferenceType sampleTreatment;
    protected Duration temporalSamplingInterval;
    protected Duration samplingTimePeriod;
    protected MeasureType spatialSamplingResolution;
    protected String spatialSamplingResolutionDetails;
    protected BigInteger samplesPerTimePeriod;

    /**
     * Gets the value of the samplingStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getSamplingStrategy() {
        return samplingStrategy;
    }

    /**
     * Sets the value of the samplingStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setSamplingStrategy(ReferenceType value) {
        this.samplingStrategy = value;
    }

    /**
     * Gets the value of the samplingProcedure property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getSamplingProcedure() {
        return samplingProcedure;
    }

    /**
     * Sets the value of the samplingProcedure property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setSamplingProcedure(ReferenceType value) {
        this.samplingProcedure = value;
    }

    /**
     * Gets the value of the samplingProcedureDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSamplingProcedureDescription() {
        return samplingProcedureDescription;
    }

    /**
     * Sets the value of the samplingProcedureDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSamplingProcedureDescription(String value) {
        this.samplingProcedureDescription = value;
    }

    /**
     * Gets the value of the sampleTreatment property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getSampleTreatment() {
        return sampleTreatment;
    }

    /**
     * Sets the value of the sampleTreatment property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setSampleTreatment(ReferenceType value) {
        this.sampleTreatment = value;
    }

    /**
     * Gets the value of the temporalSamplingInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getTemporalSamplingInterval() {
        return temporalSamplingInterval;
    }

    /**
     * Sets the value of the temporalSamplingInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setTemporalSamplingInterval(Duration value) {
        this.temporalSamplingInterval = value;
    }

    /**
     * Gets the value of the samplingTimePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getSamplingTimePeriod() {
        return samplingTimePeriod;
    }

    /**
     * Sets the value of the samplingTimePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setSamplingTimePeriod(Duration value) {
        this.samplingTimePeriod = value;
    }

    /**
     * Gets the value of the spatialSamplingResolution property.
     * 
     * @return
     *     possible object is
     *     {@link MeasureType }
     *     
     */
    public MeasureType getSpatialSamplingResolution() {
        return spatialSamplingResolution;
    }

    /**
     * Sets the value of the spatialSamplingResolution property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasureType }
     *     
     */
    public void setSpatialSamplingResolution(MeasureType value) {
        this.spatialSamplingResolution = value;
    }

    /**
     * Gets the value of the spatialSamplingResolutionDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpatialSamplingResolutionDetails() {
        return spatialSamplingResolutionDetails;
    }

    /**
     * Sets the value of the spatialSamplingResolutionDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpatialSamplingResolutionDetails(String value) {
        this.spatialSamplingResolutionDetails = value;
    }

    /**
     * Gets the value of the samplesPerTimePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSamplesPerTimePeriod() {
        return samplesPerTimePeriod;
    }

    /**
     * Sets the value of the samplesPerTimePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSamplesPerTimePeriod(BigInteger value) {
        this.samplesPerTimePeriod = value;
    }

}