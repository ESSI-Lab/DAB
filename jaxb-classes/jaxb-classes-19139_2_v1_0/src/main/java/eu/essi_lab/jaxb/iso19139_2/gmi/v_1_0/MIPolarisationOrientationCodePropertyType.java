//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.26 at 04:55:57 PM AST 
//


package eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0;

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
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.lang.CopyStrategy2;
import org.jvnet.jaxb2_commons.lang.CopyTo2;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.HashCode2;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBMergeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.MergeFrom2;
import org.jvnet.jaxb2_commons.lang.MergeStrategy2;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;

import net.opengis.iso19139.gco.v_20060504.CodeListValueType;


/**
 * <p>Java class for MI_PolarisationOrientationCode_PropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_PolarisationOrientationCode_PropertyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.isotc211.org/2005/gmi}MI_PolarisationOrientationCode" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.isotc211.org/2005/gco}nilReason"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_PolarisationOrientationCode_PropertyType", propOrder = {
    "miPolarisationOrientationCode"
})
public class MIPolarisationOrientationCodePropertyType implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(name = "MI_PolarisationOrientationCode")
    protected CodeListValueType miPolarisationOrientationCode;
    @XmlAttribute(name = "nilReason", namespace = "http://www.isotc211.org/2005/gco")
    protected List<String> nilReason;

    /**
     * Gets the value of the miPolarisationOrientationCode property.
     * 
     * @return
     *     possible object is
     *     {@link CodeListValueType }
     *     
     */
    public CodeListValueType getMIPolarisationOrientationCode() {
        return miPolarisationOrientationCode;
    }

    /**
     * Sets the value of the miPolarisationOrientationCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeListValueType }
     *     
     */
    public void setMIPolarisationOrientationCode(CodeListValueType value) {
        this.miPolarisationOrientationCode = value;
    }

    public boolean isSetMIPolarisationOrientationCode() {
        return (this.miPolarisationOrientationCode!= null);
    }

    /**
     * Gets the value of the nilReason property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nilReason property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNilReason().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNilReason() {
        if (nilReason == null) {
            nilReason = new ArrayList<String>();
        }
        return this.nilReason;
    }

    public boolean isSetNilReason() {
        return ((this.nilReason!= null)&&(!this.nilReason.isEmpty()));
    }

    public void unsetNilReason() {
        this.nilReason = null;
    }

    public String toString() {
        final ToStringStrategy2 strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        {
            CodeListValueType theMIPolarisationOrientationCode;
            theMIPolarisationOrientationCode = this.getMIPolarisationOrientationCode();
            strategy.appendField(locator, this, "miPolarisationOrientationCode", buffer, theMIPolarisationOrientationCode, this.isSetMIPolarisationOrientationCode());
        }
        {
            List<String> theNilReason;
            theNilReason = (this.isSetNilReason()?this.getNilReason():null);
            strategy.appendField(locator, this, "nilReason", buffer, theNilReason, this.isSetNilReason());
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final MIPolarisationOrientationCodePropertyType that = ((MIPolarisationOrientationCodePropertyType) object);
        {
            CodeListValueType lhsMIPolarisationOrientationCode;
            lhsMIPolarisationOrientationCode = this.getMIPolarisationOrientationCode();
            CodeListValueType rhsMIPolarisationOrientationCode;
            rhsMIPolarisationOrientationCode = that.getMIPolarisationOrientationCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "miPolarisationOrientationCode", lhsMIPolarisationOrientationCode), LocatorUtils.property(thatLocator, "miPolarisationOrientationCode", rhsMIPolarisationOrientationCode), lhsMIPolarisationOrientationCode, rhsMIPolarisationOrientationCode, this.isSetMIPolarisationOrientationCode(), that.isSetMIPolarisationOrientationCode())) {
                return false;
            }
        }
        {
            List<String> lhsNilReason;
            lhsNilReason = (this.isSetNilReason()?this.getNilReason():null);
            List<String> rhsNilReason;
            rhsNilReason = (that.isSetNilReason()?that.getNilReason():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "nilReason", lhsNilReason), LocatorUtils.property(thatLocator, "nilReason", rhsNilReason), lhsNilReason, rhsNilReason, this.isSetNilReason(), that.isSetNilReason())) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy2 strategy) {
        int currentHashCode = 1;
        {
            CodeListValueType theMIPolarisationOrientationCode;
            theMIPolarisationOrientationCode = this.getMIPolarisationOrientationCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "miPolarisationOrientationCode", theMIPolarisationOrientationCode), currentHashCode, theMIPolarisationOrientationCode, this.isSetMIPolarisationOrientationCode());
        }
        {
            List<String> theNilReason;
            theNilReason = (this.isSetNilReason()?this.getNilReason():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "nilReason", theNilReason), currentHashCode, theNilReason, this.isSetNilReason());
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy2 strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

    public Object clone() {
        return copyTo(createNewInstance());
    }

    public Object copyTo(Object target) {
        final CopyStrategy2 strategy = JAXBCopyStrategy.INSTANCE;
        return copyTo(null, target, strategy);
    }

    public Object copyTo(ObjectLocator locator, Object target, CopyStrategy2 strategy) {
        final Object draftCopy = ((target == null)?createNewInstance():target);
        if (draftCopy instanceof MIPolarisationOrientationCodePropertyType) {
            final MIPolarisationOrientationCodePropertyType copy = ((MIPolarisationOrientationCodePropertyType) draftCopy);
            {
                Boolean miPolarisationOrientationCodeShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetMIPolarisationOrientationCode());
                if (miPolarisationOrientationCodeShouldBeCopiedAndSet == Boolean.TRUE) {
                    CodeListValueType sourceMIPolarisationOrientationCode;
                    sourceMIPolarisationOrientationCode = this.getMIPolarisationOrientationCode();
                    CodeListValueType copyMIPolarisationOrientationCode = ((CodeListValueType) strategy.copy(LocatorUtils.property(locator, "miPolarisationOrientationCode", sourceMIPolarisationOrientationCode), sourceMIPolarisationOrientationCode, this.isSetMIPolarisationOrientationCode()));
                    copy.setMIPolarisationOrientationCode(copyMIPolarisationOrientationCode);
                } else {
                    if (miPolarisationOrientationCodeShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.miPolarisationOrientationCode = null;
                    }
                }
            }
            {
                Boolean nilReasonShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetNilReason());
                if (nilReasonShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<String> sourceNilReason;
                    sourceNilReason = (this.isSetNilReason()?this.getNilReason():null);
                    @SuppressWarnings("unchecked")
                    List<String> copyNilReason = ((List<String> ) strategy.copy(LocatorUtils.property(locator, "nilReason", sourceNilReason), sourceNilReason, this.isSetNilReason()));
                    copy.unsetNilReason();
                    if (copyNilReason!= null) {
                        List<String> uniqueNilReasonl = copy.getNilReason();
                        uniqueNilReasonl.addAll(copyNilReason);
                    }
                } else {
                    if (nilReasonShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetNilReason();
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIPolarisationOrientationCodePropertyType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        if (right instanceof MIPolarisationOrientationCodePropertyType) {
            final MIPolarisationOrientationCodePropertyType target = this;
            final MIPolarisationOrientationCodePropertyType leftObject = ((MIPolarisationOrientationCodePropertyType) left);
            final MIPolarisationOrientationCodePropertyType rightObject = ((MIPolarisationOrientationCodePropertyType) right);
            {
                Boolean miPolarisationOrientationCodeShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetMIPolarisationOrientationCode(), rightObject.isSetMIPolarisationOrientationCode());
                if (miPolarisationOrientationCodeShouldBeMergedAndSet == Boolean.TRUE) {
                    CodeListValueType lhsMIPolarisationOrientationCode;
                    lhsMIPolarisationOrientationCode = leftObject.getMIPolarisationOrientationCode();
                    CodeListValueType rhsMIPolarisationOrientationCode;
                    rhsMIPolarisationOrientationCode = rightObject.getMIPolarisationOrientationCode();
                    CodeListValueType mergedMIPolarisationOrientationCode = ((CodeListValueType) strategy.merge(LocatorUtils.property(leftLocator, "miPolarisationOrientationCode", lhsMIPolarisationOrientationCode), LocatorUtils.property(rightLocator, "miPolarisationOrientationCode", rhsMIPolarisationOrientationCode), lhsMIPolarisationOrientationCode, rhsMIPolarisationOrientationCode, leftObject.isSetMIPolarisationOrientationCode(), rightObject.isSetMIPolarisationOrientationCode()));
                    target.setMIPolarisationOrientationCode(mergedMIPolarisationOrientationCode);
                } else {
                    if (miPolarisationOrientationCodeShouldBeMergedAndSet == Boolean.FALSE) {
                        target.miPolarisationOrientationCode = null;
                    }
                }
            }
            {
                Boolean nilReasonShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetNilReason(), rightObject.isSetNilReason());
                if (nilReasonShouldBeMergedAndSet == Boolean.TRUE) {
                    List<String> lhsNilReason;
                    lhsNilReason = (leftObject.isSetNilReason()?leftObject.getNilReason():null);
                    List<String> rhsNilReason;
                    rhsNilReason = (rightObject.isSetNilReason()?rightObject.getNilReason():null);
                    List<String> mergedNilReason = ((List<String> ) strategy.merge(LocatorUtils.property(leftLocator, "nilReason", lhsNilReason), LocatorUtils.property(rightLocator, "nilReason", rhsNilReason), lhsNilReason, rhsNilReason, leftObject.isSetNilReason(), rightObject.isSetNilReason()));
                    target.unsetNilReason();
                    if (mergedNilReason!= null) {
                        List<String> uniqueNilReasonl = target.getNilReason();
                        uniqueNilReasonl.addAll(mergedNilReason);
                    }
                } else {
                    if (nilReasonShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetNilReason();
                    }
                }
            }
        }
    }

    public void setNilReason(List<String> value) {
        this.nilReason = null;
        if (value!= null) {
            List<String> draftl = this.getNilReason();
            draftl.addAll(value);
        }
    }

    public MIPolarisationOrientationCodePropertyType withMIPolarisationOrientationCode(CodeListValueType value) {
        setMIPolarisationOrientationCode(value);
        return this;
    }

    public MIPolarisationOrientationCodePropertyType withNilReason(String... values) {
        if (values!= null) {
            for (String value: values) {
                getNilReason().add(value);
            }
        }
        return this;
    }

    public MIPolarisationOrientationCodePropertyType withNilReason(Collection<String> values) {
        if (values!= null) {
            getNilReason().addAll(values);
        }
        return this;
    }

    public MIPolarisationOrientationCodePropertyType withNilReason(List<String> value) {
        setNilReason(value);
        return this;
    }

}