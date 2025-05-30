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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.AbstractMemberType;


/**
 * <p>Java class for LogType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LogType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="logEntry" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMemberType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://def.wmo.int/wmdr/2017}LogEntry"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/extension&gt;
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
@XmlType(name = "LogType", propOrder = {
    "logEntry"
})
@XmlSeeAlso({
    FacilityLogType.class,
    EquipmentLogType.class
})
public abstract class LogType
    extends AbstractFeatureType
{

    protected List<LogType.LogEntry> logEntry;

    /**
     * Gets the value of the logEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the logEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLogEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LogType.LogEntry }
     * 
     * 
     */
    public List<LogType.LogEntry> getLogEntry() {
        if (logEntry == null) {
            logEntry = new ArrayList<LogType.LogEntry>();
        }
        return this.logEntry;
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
     *         &lt;element ref="{http://def.wmo.int/wmdr/2017}LogEntry"/&gt;
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
        "logEntry"
    })
    public static class LogEntry
        extends AbstractMemberType
    {

        @XmlElementRef(name = "LogEntry", namespace = "http://def.wmo.int/wmdr/2017", type = JAXBElement.class)
        protected JAXBElement<? extends LogEntryType> logEntry;

        /**
         * Gets the value of the logEntry property.
         * 
         * @return
         *     possible object is
         *     {@link JAXBElement }{@code <}{@link ControlCheckReportType }{@code >}
         *     {@link JAXBElement }{@code <}{@link EventReportType }{@code >}
         *     {@link JAXBElement }{@code <}{@link LogEntryType }{@code >}
         *     {@link JAXBElement }{@code <}{@link MaintenanceReportType }{@code >}
         *     
         */
        public JAXBElement<? extends LogEntryType> getLogEntry() {
            return logEntry;
        }

        /**
         * Sets the value of the logEntry property.
         * 
         * @param value
         *     allowed object is
         *     {@link JAXBElement }{@code <}{@link ControlCheckReportType }{@code >}
         *     {@link JAXBElement }{@code <}{@link EventReportType }{@code >}
         *     {@link JAXBElement }{@code <}{@link LogEntryType }{@code >}
         *     {@link JAXBElement }{@code <}{@link MaintenanceReportType }{@code >}
         *     
         */
        public void setLogEntry(JAXBElement<? extends LogEntryType> value) {
            this.logEntry = value;
        }

    }

}
