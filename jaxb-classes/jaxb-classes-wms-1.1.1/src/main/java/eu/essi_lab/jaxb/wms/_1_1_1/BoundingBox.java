//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.08 at 11:47:38 AM CEST 
//


package eu.essi_lab.jaxb.wms._1_1_1;

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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "BoundingBox")
public class BoundingBox {

    @XmlAttribute(name = "SRS", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String srs;
    @XmlAttribute(name = "minx", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String minx;
    @XmlAttribute(name = "miny", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String miny;
    @XmlAttribute(name = "maxx", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String maxx;
    @XmlAttribute(name = "maxy", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String maxy;
    @XmlAttribute(name = "resx")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String resx;
    @XmlAttribute(name = "resy")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String resy;

    /**
     * Gets the value of the srs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSRS() {
        return srs;
    }

    /**
     * Sets the value of the srs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSRS(String value) {
        this.srs = value;
    }

    /**
     * Gets the value of the minx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinx() {
        return minx;
    }

    /**
     * Sets the value of the minx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinx(String value) {
        this.minx = value;
    }

    /**
     * Gets the value of the miny property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiny() {
        return miny;
    }

    /**
     * Sets the value of the miny property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiny(String value) {
        this.miny = value;
    }

    /**
     * Gets the value of the maxx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxx() {
        return maxx;
    }

    /**
     * Sets the value of the maxx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxx(String value) {
        this.maxx = value;
    }

    /**
     * Gets the value of the maxy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxy() {
        return maxy;
    }

    /**
     * Sets the value of the maxy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxy(String value) {
        this.maxy = value;
    }

    /**
     * Gets the value of the resx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResx() {
        return resx;
    }

    /**
     * Sets the value of the resx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResx(String value) {
        this.resx = value;
    }

    /**
     * Gets the value of the resy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResy() {
        return resy;
    }

    /**
     * Sets the value of the resy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResy(String value) {
        this.resy = value;
    }

}
