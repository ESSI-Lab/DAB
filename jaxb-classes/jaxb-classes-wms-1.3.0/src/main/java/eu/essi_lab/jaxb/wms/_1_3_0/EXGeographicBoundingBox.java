//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.11.16 at 10:55:31 AM CET 
//


package eu.essi_lab.jaxb.wms._1_3_0;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="westBoundLongitude" type="{http://www.opengis.net/wms}longitudeType"/&gt;
 *         &lt;element name="eastBoundLongitude" type="{http://www.opengis.net/wms}longitudeType"/&gt;
 *         &lt;element name="southBoundLatitude" type="{http://www.opengis.net/wms}latitudeType"/&gt;
 *         &lt;element name="northBoundLatitude" type="{http://www.opengis.net/wms}latitudeType"/&gt;
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
    "westBoundLongitude",
    "eastBoundLongitude",
    "southBoundLatitude",
    "northBoundLatitude"
})
@XmlRootElement(name = "EX_GeographicBoundingBox")
public class EXGeographicBoundingBox {

    protected double westBoundLongitude;
    protected double eastBoundLongitude;
    protected double southBoundLatitude;
    protected double northBoundLatitude;

    /**
     * Gets the value of the westBoundLongitude property.
     * 
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    /**
     * Sets the value of the westBoundLongitude property.
     * 
     */
    public void setWestBoundLongitude(double value) {
        this.westBoundLongitude = value;
    }

    /**
     * Gets the value of the eastBoundLongitude property.
     * 
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Sets the value of the eastBoundLongitude property.
     * 
     */
    public void setEastBoundLongitude(double value) {
        this.eastBoundLongitude = value;
    }

    /**
     * Gets the value of the southBoundLatitude property.
     * 
     */
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * Sets the value of the southBoundLatitude property.
     * 
     */
    public void setSouthBoundLatitude(double value) {
        this.southBoundLatitude = value;
    }

    /**
     * Gets the value of the northBoundLatitude property.
     * 
     */
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * Sets the value of the northBoundLatitude property.
     * 
     */
    public void setNorthBoundLatitude(double value) {
        this.northBoundLatitude = value;
    }

}
