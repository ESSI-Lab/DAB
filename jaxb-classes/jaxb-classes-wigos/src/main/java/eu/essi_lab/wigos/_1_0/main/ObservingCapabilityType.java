//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.11.27 at 01:47:52 PM CET 
//


package eu.essi_lab.wigos._1_0.main;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.wigos._1_0.om.OMObservationPropertyType;


/**
 * <p>Java class for ObservingCapabilityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObservingCapabilityType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="facility" type="{http://www.opengis.net/gml/3.2}ReferenceType"/&gt;
 *         &lt;element name="programAffiliation" type="{http://www.opengis.net/gml/3.2}ReferenceType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="observation" type="{http://www.opengis.net/om/2.0}OM_ObservationPropertyType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservingCapabilityType", propOrder = {
    "facility",
    "programAffiliation",
    "observation"
})
public class ObservingCapabilityType
    extends AbstractFeatureType
{

    @XmlElement(required = true)
    protected ReferenceType facility;
    @XmlElement(required = true)
    protected List<ReferenceType> programAffiliation;
    @XmlElement(required = true)
    protected List<OMObservationPropertyType> observation;

    /**
     * Gets the value of the facility property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceType getFacility() {
        return facility;
    }

    /**
     * Sets the value of the facility property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setFacility(ReferenceType value) {
        this.facility = value;
    }

    /**
     * Gets the value of the programAffiliation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the programAffiliation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProgramAffiliation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceType }
     * 
     * 
     */
    public List<ReferenceType> getProgramAffiliation() {
        if (programAffiliation == null) {
            programAffiliation = new ArrayList<ReferenceType>();
        }
        return this.programAffiliation;
    }

    /**
     * Gets the value of the observation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OMObservationPropertyType }
     * 
     * 
     */
    public List<OMObservationPropertyType> getObservation() {
        if (observation == null) {
            observation = new ArrayList<OMObservationPropertyType>();
        }
        return this.observation;
    }

}