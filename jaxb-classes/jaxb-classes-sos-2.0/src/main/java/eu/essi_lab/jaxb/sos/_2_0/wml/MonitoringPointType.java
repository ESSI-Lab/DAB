//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.14 at 04:45:11 PM CEST 
//


package eu.essi_lab.jaxb.sos._2_0.wml;

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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.VerticalDatumPropertyType;
import eu.essi_lab.jaxb.sos._2_0.iso2005.gmd.CIResponsiblePartyPropertyType;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.SFSpatialSamplingFeatureType;


/**
 * <p>Java class for MonitoringPointType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MonitoringPointType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/samplingSpatial/2.0}SF_SpatialSamplingFeatureType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="relatedParty" type="{http://www.isotc211.org/2005/gmd}CI_ResponsibleParty_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="monitoringType" type="{http://www.opengis.net/gml/3.2}ReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="descriptionReference" type="{http://www.opengis.net/gml/3.2}ReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="verticalDatum" type="{http://www.opengis.net/gml/3.2}VerticalDatumPropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="timeZone" type="{http://www.opengis.net/waterml/2.0}TimeZonePropertyType" minOccurs="0"/&gt;
 *         &lt;element name="daylightSavingTimeZone" type="{http://www.opengis.net/waterml/2.0}TimeZonePropertyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonitoringPointType", propOrder = {
    "rest"
})
public class MonitoringPointType
    extends SFSpatialSamplingFeatureType
{

    @XmlElementRefs({
        @XmlElementRef(name = "relatedParty", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "monitoringType", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "descriptionReference", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "verticalDatum", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "timeZone", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "daylightSavingTimeZone", namespace = "http://www.opengis.net/waterml/2.0", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> rest;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "DescriptionReference" is used by two different parts of a schema. See: 
     * line 51 of file:/home/boldrini/git/DAB/jaxb-classes/jaxb-classes-sos-2.0/src/main/resources/schemas/monitoringPoint.xsd
     * line 41 of http://schemas.opengis.net/gml/3.2.1/gmlBase.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CIResponsiblePartyPropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link VerticalDatumPropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeZonePropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeZonePropertyType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        return this.rest;
    }

}