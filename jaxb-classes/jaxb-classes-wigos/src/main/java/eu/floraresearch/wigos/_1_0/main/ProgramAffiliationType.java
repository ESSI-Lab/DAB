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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import eu.floraresearch.wigos._1_0.gml._3_2_1.AbstractMemberType;
import eu.floraresearch.wigos._1_0.gml._3_2_1.ReferenceType;


/**
 * <p>Java class for ProgramAffiliationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProgramAffiliationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="programAffiliation" type="{http://www.opengis.net/gml/3.2}ReferenceType"/&gt;
 *         &lt;element name="programSpecificFacilityId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="reportingStatus" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMemberType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://def.wmo.int/wmdr/1.0}ReportingStatus"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProgramAffiliationType", propOrder = {
    "programAffiliation",
    "programSpecificFacilityId",
    "reportingStatus"
})
public class ProgramAffiliationType {

    @XmlElement(required = true)
    protected ReferenceType programAffiliation;
    protected String programSpecificFacilityId;
    protected List<ProgramAffiliationType.ReportingStatus> reportingStatus;

    /**
     * Gets the value of the programAffiliation property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getProgramAffiliation() {
        return programAffiliation;
    }

    /**
     * Sets the value of the programAffiliation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setProgramAffiliation(ReferenceType value) {
        this.programAffiliation = value;
    }

    /**
     * Gets the value of the programSpecificFacilityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgramSpecificFacilityId() {
        return programSpecificFacilityId;
    }

    /**
     * Sets the value of the programSpecificFacilityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgramSpecificFacilityId(String value) {
        this.programSpecificFacilityId = value;
    }

    /**
     * Gets the value of the reportingStatus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportingStatus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportingStatus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProgramAffiliationType.ReportingStatus }
     * 
     * 
     */
    public List<ProgramAffiliationType.ReportingStatus> getReportingStatus() {
        if (reportingStatus == null) {
            reportingStatus = new ArrayList<ProgramAffiliationType.ReportingStatus>();
        }
        return this.reportingStatus;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMemberType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://def.wmo.int/wmdr/1.0}ReportingStatus"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "reportingStatus"
    })
    public static class ReportingStatus
        extends AbstractMemberType
    {

        @XmlElement(name = "ReportingStatus", required = true)
        protected ReportingStatusType reportingStatus;

        /**
         * Gets the value of the reportingStatus property.
         * 
         * @return
         *     possible object is
         *     {@link ReportingStatusType }
         *     
         */
        public ReportingStatusType getReportingStatus() {
            return reportingStatus;
        }

        /**
         * Sets the value of the reportingStatus property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReportingStatusType }
         *     
         */
        public void setReportingStatus(ReportingStatusType value) {
            this.reportingStatus = value;
        }

    }

}