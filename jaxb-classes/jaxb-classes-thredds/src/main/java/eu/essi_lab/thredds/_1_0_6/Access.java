//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.03 at 12:55:12 PM CET 
//


package eu.essi_lab.thredds._1_0_6;

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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element ref="{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}dataSize" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="urlPath" use="required" type="{http://www.w3.org/2001/XMLSchema}token" /&gt;
 *       &lt;attribute name="serviceName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="dataFormat" type="{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}dataFormatTypes" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataSize"
})
@XmlRootElement(name = "access")
public class Access {

    protected DataSize dataSize;
    @XmlAttribute(name = "urlPath", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String urlPath;
    @XmlAttribute(name = "serviceName")
    protected String serviceName;
    @XmlAttribute(name = "dataFormat")
    protected String dataFormat;

    /**
     * Gets the value of the dataSize property.
     * 
     * @return
     *     possible object is
     *     {@link DataSize }
     *     
     */
    public DataSize getDataSize() {
        return dataSize;
    }

    /**
     * Sets the value of the dataSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSize }
     *     
     */
    public void setDataSize(DataSize value) {
        this.dataSize = value;
    }

    /**
     * Gets the value of the urlPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * Sets the value of the urlPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlPath(String value) {
        this.urlPath = value;
    }

    /**
     * Gets the value of the serviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the value of the serviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceName(String value) {
        this.serviceName = value;
    }

    /**
     * Gets the value of the dataFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataFormat() {
        return dataFormat;
    }

    /**
     * Sets the value of the dataFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataFormat(String value) {
        this.dataFormat = value;
    }

}
