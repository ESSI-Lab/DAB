//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.17 at 03:51:45 PM CEST 
//


package eu.essi_lab.jaxb.wml._2_0.swe._2;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BinaryEncodingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinaryEncodingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/swe/2.0}AbstractEncodingType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="member" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;group ref="{http://www.opengis.net/swe/2.0}ComponentOrBlock"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="byteOrder" use="required" type="{http://www.opengis.net/swe/2.0}ByteOrderType" /&gt;
 *       &lt;attribute name="byteEncoding" use="required" type="{http://www.opengis.net/swe/2.0}ByteEncodingType" /&gt;
 *       &lt;attribute name="byteLength" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryEncodingType", propOrder = {
    "member"
})
public class BinaryEncodingType
    extends AbstractEncodingType
{

    @XmlElement(required = true)
    protected List<BinaryEncodingType.Member> member;
    @XmlAttribute(name = "byteOrder", required = true)
    protected ByteOrderType byteOrder;
    @XmlAttribute(name = "byteEncoding", required = true)
    protected ByteEncodingType byteEncoding;
    @XmlAttribute(name = "byteLength")
    protected BigInteger byteLength;

    /**
     * Gets the value of the member property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the member property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BinaryEncodingType.Member }
     * 
     * 
     */
    public List<BinaryEncodingType.Member> getMember() {
        if (member == null) {
            member = new ArrayList<BinaryEncodingType.Member>();
        }
        return this.member;
    }

    /**
     * Gets the value of the byteOrder property.
     * 
     * @return
     *     possible object is
     *     {@link ByteOrderType }
     *     
     */
    public ByteOrderType getByteOrder() {
        return byteOrder;
    }

    /**
     * Sets the value of the byteOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByteOrderType }
     *     
     */
    public void setByteOrder(ByteOrderType value) {
        this.byteOrder = value;
    }

    /**
     * Gets the value of the byteEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link ByteEncodingType }
     *     
     */
    public ByteEncodingType getByteEncoding() {
        return byteEncoding;
    }

    /**
     * Sets the value of the byteEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByteEncodingType }
     *     
     */
    public void setByteEncoding(ByteEncodingType value) {
        this.byteEncoding = value;
    }

    /**
     * Gets the value of the byteLength property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getByteLength() {
        return byteLength;
    }

    /**
     * Sets the value of the byteLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setByteLength(BigInteger value) {
        this.byteLength = value;
    }


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
     *         &lt;group ref="{http://www.opengis.net/swe/2.0}ComponentOrBlock"/&gt;
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
        "component",
        "block"
    })
    public static class Member {

        @XmlElement(name = "Component")
        protected ComponentType component;
        @XmlElement(name = "Block")
        protected BlockType block;

        /**
         * Gets the value of the component property.
         * 
         * @return
         *     possible object is
         *     {@link ComponentType }
         *     
         */
        public ComponentType getComponent() {
            return component;
        }

        /**
         * Sets the value of the component property.
         * 
         * @param value
         *     allowed object is
         *     {@link ComponentType }
         *     
         */
        public void setComponent(ComponentType value) {
            this.component = value;
        }

        /**
         * Gets the value of the block property.
         * 
         * @return
         *     possible object is
         *     {@link BlockType }
         *     
         */
        public BlockType getBlock() {
            return block;
        }

        /**
         * Sets the value of the block property.
         * 
         * @param value
         *     allowed object is
         *     {@link BlockType }
         *     
         */
        public void setBlock(BlockType value) {
            this.block = value;
        }

    }

}
