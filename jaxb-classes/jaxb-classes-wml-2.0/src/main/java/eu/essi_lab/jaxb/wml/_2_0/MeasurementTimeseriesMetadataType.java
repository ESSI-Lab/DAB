//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.17 at 03:51:45 PM CEST 
//


package eu.essi_lab.jaxb.wml._2_0;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;


/**
 * <p>Java class for MeasurementTimeseriesMetadataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MeasurementTimeseriesMetadataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/waterml/2.0}TimeseriesMetadataType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="startAnchorPoint" type="{http://www.opengis.net/gml/3.2}TimePositionType" minOccurs="0"/&gt;
 *         &lt;element name="endAnchorPoint" type="{http://www.opengis.net/gml/3.2}TimePositionType" minOccurs="0"/&gt;
 *         &lt;element name="cumulative" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="accumulationAnchorTime" type="{http://www.opengis.net/gml/3.2}TimePositionType" minOccurs="0"/&gt;
 *         &lt;element name="accumulationIntervalLength" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/&gt;
 *         &lt;element name="maxGapPeriod" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementTimeseriesMetadataType", propOrder = {
    "startAnchorPoint",
    "endAnchorPoint",
    "cumulative",
    "accumulationAnchorTime",
    "accumulationIntervalLength",
    "maxGapPeriod"
})
public class MeasurementTimeseriesMetadataType
    extends TimeseriesMetadataType
{

    protected TimePositionType startAnchorPoint;
    protected TimePositionType endAnchorPoint;
    protected Boolean cumulative;
    protected TimePositionType accumulationAnchorTime;
    protected Duration accumulationIntervalLength;
    protected Duration maxGapPeriod;

    /**
     * Gets the value of the startAnchorPoint property.
     * 
     * @return
     *     possible object is
     *     {@link TimePositionType }
     *     
     */
    public TimePositionType getStartAnchorPoint() {
        return startAnchorPoint;
    }

    /**
     * Sets the value of the startAnchorPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePositionType }
     *     
     */
    public void setStartAnchorPoint(TimePositionType value) {
        this.startAnchorPoint = value;
    }

    /**
     * Gets the value of the endAnchorPoint property.
     * 
     * @return
     *     possible object is
     *     {@link TimePositionType }
     *     
     */
    public TimePositionType getEndAnchorPoint() {
        return endAnchorPoint;
    }

    /**
     * Sets the value of the endAnchorPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePositionType }
     *     
     */
    public void setEndAnchorPoint(TimePositionType value) {
        this.endAnchorPoint = value;
    }

    /**
     * Gets the value of the cumulative property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCumulative() {
        return cumulative;
    }

    /**
     * Sets the value of the cumulative property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCumulative(Boolean value) {
        this.cumulative = value;
    }

    /**
     * Gets the value of the accumulationAnchorTime property.
     * 
     * @return
     *     possible object is
     *     {@link TimePositionType }
     *     
     */
    public TimePositionType getAccumulationAnchorTime() {
        return accumulationAnchorTime;
    }

    /**
     * Sets the value of the accumulationAnchorTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePositionType }
     *     
     */
    public void setAccumulationAnchorTime(TimePositionType value) {
        this.accumulationAnchorTime = value;
    }

    /**
     * Gets the value of the accumulationIntervalLength property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getAccumulationIntervalLength() {
        return accumulationIntervalLength;
    }

    /**
     * Sets the value of the accumulationIntervalLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setAccumulationIntervalLength(Duration value) {
        this.accumulationIntervalLength = value;
    }

    /**
     * Gets the value of the maxGapPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getMaxGapPeriod() {
        return maxGapPeriod;
    }

    /**
     * Sets the value of the maxGapPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setMaxGapPeriod(Duration value) {
        this.maxGapPeriod = value;
    }

}