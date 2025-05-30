//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.11.27 at 01:47:52 PM CET 
//


package eu.essi_lab.wigos._1_0.main;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.TimePeriodPropertyType;


/**
 * <p>Java class for DataGenerationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataGenerationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="validPeriod" type="{http://www.opengis.net/gml/3.2}TimePeriodPropertyType"/&gt;
 *         &lt;element name="schedule" type="{http://def.wmo.int/wmdr/2017}SchedulePropertyType"/&gt;
 *         &lt;element name="sampling" type="{http://def.wmo.int/wmdr/2017}SamplingPropertyType"/&gt;
 *         &lt;element name="processing" type="{http://def.wmo.int/wmdr/2017}ProcessingPropertyType" minOccurs="0"/&gt;
 *         &lt;element name="reporting" type="{http://def.wmo.int/wmdr/2017}ReportingPropertyType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataGenerationType", propOrder = {
    "validPeriod",
    "schedule",
    "sampling",
    "processing",
    "reporting"
})
public class DataGenerationType
    extends AbstractFeatureType
{

    @XmlElement(required = true)
    protected TimePeriodPropertyType validPeriod;
    @XmlElement(required = true)
    protected SchedulePropertyType schedule;
    @XmlElement(required = true)
    protected SamplingPropertyType sampling;
    protected ProcessingPropertyType processing;
    @XmlElement(required = true)
    protected ReportingPropertyType reporting;

    /**
     * Gets the value of the validPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link TimePeriodPropertyType }
     *     
     */
    public TimePeriodPropertyType getValidPeriod() {
        return validPeriod;
    }

    /**
     * Sets the value of the validPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePeriodPropertyType }
     *     
     */
    public void setValidPeriod(TimePeriodPropertyType value) {
        this.validPeriod = value;
    }

    /**
     * Gets the value of the schedule property.
     * 
     * @return
     *     possible object is
     *     {@link SchedulePropertyType }
     *     
     */
    public SchedulePropertyType getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchedulePropertyType }
     *     
     */
    public void setSchedule(SchedulePropertyType value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the sampling property.
     * 
     * @return
     *     possible object is
     *     {@link SamplingPropertyType }
     *     
     */
    public SamplingPropertyType getSampling() {
        return sampling;
    }

    /**
     * Sets the value of the sampling property.
     * 
     * @param value
     *     allowed object is
     *     {@link SamplingPropertyType }
     *     
     */
    public void setSampling(SamplingPropertyType value) {
        this.sampling = value;
    }

    /**
     * Gets the value of the processing property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessingPropertyType }
     *     
     */
    public ProcessingPropertyType getProcessing() {
        return processing;
    }

    /**
     * Sets the value of the processing property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessingPropertyType }
     *     
     */
    public void setProcessing(ProcessingPropertyType value) {
        this.processing = value;
    }

    /**
     * Gets the value of the reporting property.
     * 
     * @return
     *     possible object is
     *     {@link ReportingPropertyType }
     *     
     */
    public ReportingPropertyType getReporting() {
        return reporting;
    }

    /**
     * Sets the value of the reporting property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportingPropertyType }
     *     
     */
    public void setReporting(ReportingPropertyType value) {
        this.reporting = value;
    }

}
