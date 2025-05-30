//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.26 at 04:55:57 PM AST 
//


package eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0;

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

import net.opengis.iso19139.gco.v_20060504.AbstractObjectType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;


/**
 * <p>Java class for MI_EnvironmentalRecord_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_EnvironmentalRecord_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gco}AbstractObject_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="averageAirTemperature" type="{http://www.isotc211.org/2005/gco}Real_PropertyType"/&gt;
 *         &lt;element name="maxRelativeHumidity" type="{http://www.isotc211.org/2005/gco}Real_PropertyType"/&gt;
 *         &lt;element name="maxAltitude" type="{http://www.isotc211.org/2005/gco}Real_PropertyType"/&gt;
 *         &lt;element name="meterologicalConditions" type="{http://www.isotc211.org/2005/gco}CharacterString_PropertyType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_EnvironmentalRecord_Type", propOrder = {
    "averageAirTemperature",
    "maxRelativeHumidity",
    "maxAltitude",
    "meterologicalConditions"
})
public class MIEnvironmentalRecordType
    extends AbstractObjectType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(required = true)
    protected RealPropertyType averageAirTemperature;
    @XmlElement(required = true)
    protected RealPropertyType maxRelativeHumidity;
    @XmlElement(required = true)
    protected RealPropertyType maxAltitude;
    @XmlElement(required = true)
    protected CharacterStringPropertyType meterologicalConditions;

    /**
     * Gets the value of the averageAirTemperature property.
     * 
     * @return
     *     possible object is
     *     {@link RealPropertyType }
     *     
     */
    public RealPropertyType getAverageAirTemperature() {
        return averageAirTemperature;
    }

    /**
     * Sets the value of the averageAirTemperature property.
     * 
     * @param value
     *     allowed object is
     *     {@link RealPropertyType }
     *     
     */
    public void setAverageAirTemperature(RealPropertyType value) {
        this.averageAirTemperature = value;
    }

    public boolean isSetAverageAirTemperature() {
        return (this.averageAirTemperature!= null);
    }

    /**
     * Gets the value of the maxRelativeHumidity property.
     * 
     * @return
     *     possible object is
     *     {@link RealPropertyType }
     *     
     */
    public RealPropertyType getMaxRelativeHumidity() {
        return maxRelativeHumidity;
    }

    /**
     * Sets the value of the maxRelativeHumidity property.
     * 
     * @param value
     *     allowed object is
     *     {@link RealPropertyType }
     *     
     */
    public void setMaxRelativeHumidity(RealPropertyType value) {
        this.maxRelativeHumidity = value;
    }

    public boolean isSetMaxRelativeHumidity() {
        return (this.maxRelativeHumidity!= null);
    }

    /**
     * Gets the value of the maxAltitude property.
     * 
     * @return
     *     possible object is
     *     {@link RealPropertyType }
     *     
     */
    public RealPropertyType getMaxAltitude() {
        return maxAltitude;
    }

    /**
     * Sets the value of the maxAltitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link RealPropertyType }
     *     
     */
    public void setMaxAltitude(RealPropertyType value) {
        this.maxAltitude = value;
    }

    public boolean isSetMaxAltitude() {
        return (this.maxAltitude!= null);
    }

    /**
     * Gets the value of the meterologicalConditions property.
     * 
     * @return
     *     possible object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public CharacterStringPropertyType getMeterologicalConditions() {
        return meterologicalConditions;
    }

    /**
     * Sets the value of the meterologicalConditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public void setMeterologicalConditions(CharacterStringPropertyType value) {
        this.meterologicalConditions = value;
    }

    public boolean isSetMeterologicalConditions() {
        return (this.meterologicalConditions!= null);
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
        super.appendFields(locator, buffer, strategy);
        {
            RealPropertyType theAverageAirTemperature;
            theAverageAirTemperature = this.getAverageAirTemperature();
            strategy.appendField(locator, this, "averageAirTemperature", buffer, theAverageAirTemperature, this.isSetAverageAirTemperature());
        }
        {
            RealPropertyType theMaxRelativeHumidity;
            theMaxRelativeHumidity = this.getMaxRelativeHumidity();
            strategy.appendField(locator, this, "maxRelativeHumidity", buffer, theMaxRelativeHumidity, this.isSetMaxRelativeHumidity());
        }
        {
            RealPropertyType theMaxAltitude;
            theMaxAltitude = this.getMaxAltitude();
            strategy.appendField(locator, this, "maxAltitude", buffer, theMaxAltitude, this.isSetMaxAltitude());
        }
        {
            CharacterStringPropertyType theMeterologicalConditions;
            theMeterologicalConditions = this.getMeterologicalConditions();
            strategy.appendField(locator, this, "meterologicalConditions", buffer, theMeterologicalConditions, this.isSetMeterologicalConditions());
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
        if (!super.equals(thisLocator, thatLocator, object, strategy)) {
            return false;
        }
        final MIEnvironmentalRecordType that = ((MIEnvironmentalRecordType) object);
        {
            RealPropertyType lhsAverageAirTemperature;
            lhsAverageAirTemperature = this.getAverageAirTemperature();
            RealPropertyType rhsAverageAirTemperature;
            rhsAverageAirTemperature = that.getAverageAirTemperature();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "averageAirTemperature", lhsAverageAirTemperature), LocatorUtils.property(thatLocator, "averageAirTemperature", rhsAverageAirTemperature), lhsAverageAirTemperature, rhsAverageAirTemperature, this.isSetAverageAirTemperature(), that.isSetAverageAirTemperature())) {
                return false;
            }
        }
        {
            RealPropertyType lhsMaxRelativeHumidity;
            lhsMaxRelativeHumidity = this.getMaxRelativeHumidity();
            RealPropertyType rhsMaxRelativeHumidity;
            rhsMaxRelativeHumidity = that.getMaxRelativeHumidity();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "maxRelativeHumidity", lhsMaxRelativeHumidity), LocatorUtils.property(thatLocator, "maxRelativeHumidity", rhsMaxRelativeHumidity), lhsMaxRelativeHumidity, rhsMaxRelativeHumidity, this.isSetMaxRelativeHumidity(), that.isSetMaxRelativeHumidity())) {
                return false;
            }
        }
        {
            RealPropertyType lhsMaxAltitude;
            lhsMaxAltitude = this.getMaxAltitude();
            RealPropertyType rhsMaxAltitude;
            rhsMaxAltitude = that.getMaxAltitude();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "maxAltitude", lhsMaxAltitude), LocatorUtils.property(thatLocator, "maxAltitude", rhsMaxAltitude), lhsMaxAltitude, rhsMaxAltitude, this.isSetMaxAltitude(), that.isSetMaxAltitude())) {
                return false;
            }
        }
        {
            CharacterStringPropertyType lhsMeterologicalConditions;
            lhsMeterologicalConditions = this.getMeterologicalConditions();
            CharacterStringPropertyType rhsMeterologicalConditions;
            rhsMeterologicalConditions = that.getMeterologicalConditions();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "meterologicalConditions", lhsMeterologicalConditions), LocatorUtils.property(thatLocator, "meterologicalConditions", rhsMeterologicalConditions), lhsMeterologicalConditions, rhsMeterologicalConditions, this.isSetMeterologicalConditions(), that.isSetMeterologicalConditions())) {
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
        int currentHashCode = super.hashCode(locator, strategy);
        {
            RealPropertyType theAverageAirTemperature;
            theAverageAirTemperature = this.getAverageAirTemperature();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "averageAirTemperature", theAverageAirTemperature), currentHashCode, theAverageAirTemperature, this.isSetAverageAirTemperature());
        }
        {
            RealPropertyType theMaxRelativeHumidity;
            theMaxRelativeHumidity = this.getMaxRelativeHumidity();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "maxRelativeHumidity", theMaxRelativeHumidity), currentHashCode, theMaxRelativeHumidity, this.isSetMaxRelativeHumidity());
        }
        {
            RealPropertyType theMaxAltitude;
            theMaxAltitude = this.getMaxAltitude();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "maxAltitude", theMaxAltitude), currentHashCode, theMaxAltitude, this.isSetMaxAltitude());
        }
        {
            CharacterStringPropertyType theMeterologicalConditions;
            theMeterologicalConditions = this.getMeterologicalConditions();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "meterologicalConditions", theMeterologicalConditions), currentHashCode, theMeterologicalConditions, this.isSetMeterologicalConditions());
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
        super.copyTo(locator, draftCopy, strategy);
        if (draftCopy instanceof MIEnvironmentalRecordType) {
            final MIEnvironmentalRecordType copy = ((MIEnvironmentalRecordType) draftCopy);
            {
                Boolean averageAirTemperatureShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetAverageAirTemperature());
                if (averageAirTemperatureShouldBeCopiedAndSet == Boolean.TRUE) {
                    RealPropertyType sourceAverageAirTemperature;
                    sourceAverageAirTemperature = this.getAverageAirTemperature();
                    RealPropertyType copyAverageAirTemperature = ((RealPropertyType) strategy.copy(LocatorUtils.property(locator, "averageAirTemperature", sourceAverageAirTemperature), sourceAverageAirTemperature, this.isSetAverageAirTemperature()));
                    copy.setAverageAirTemperature(copyAverageAirTemperature);
                } else {
                    if (averageAirTemperatureShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.averageAirTemperature = null;
                    }
                }
            }
            {
                Boolean maxRelativeHumidityShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetMaxRelativeHumidity());
                if (maxRelativeHumidityShouldBeCopiedAndSet == Boolean.TRUE) {
                    RealPropertyType sourceMaxRelativeHumidity;
                    sourceMaxRelativeHumidity = this.getMaxRelativeHumidity();
                    RealPropertyType copyMaxRelativeHumidity = ((RealPropertyType) strategy.copy(LocatorUtils.property(locator, "maxRelativeHumidity", sourceMaxRelativeHumidity), sourceMaxRelativeHumidity, this.isSetMaxRelativeHumidity()));
                    copy.setMaxRelativeHumidity(copyMaxRelativeHumidity);
                } else {
                    if (maxRelativeHumidityShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.maxRelativeHumidity = null;
                    }
                }
            }
            {
                Boolean maxAltitudeShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetMaxAltitude());
                if (maxAltitudeShouldBeCopiedAndSet == Boolean.TRUE) {
                    RealPropertyType sourceMaxAltitude;
                    sourceMaxAltitude = this.getMaxAltitude();
                    RealPropertyType copyMaxAltitude = ((RealPropertyType) strategy.copy(LocatorUtils.property(locator, "maxAltitude", sourceMaxAltitude), sourceMaxAltitude, this.isSetMaxAltitude()));
                    copy.setMaxAltitude(copyMaxAltitude);
                } else {
                    if (maxAltitudeShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.maxAltitude = null;
                    }
                }
            }
            {
                Boolean meterologicalConditionsShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetMeterologicalConditions());
                if (meterologicalConditionsShouldBeCopiedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType sourceMeterologicalConditions;
                    sourceMeterologicalConditions = this.getMeterologicalConditions();
                    CharacterStringPropertyType copyMeterologicalConditions = ((CharacterStringPropertyType) strategy.copy(LocatorUtils.property(locator, "meterologicalConditions", sourceMeterologicalConditions), sourceMeterologicalConditions, this.isSetMeterologicalConditions()));
                    copy.setMeterologicalConditions(copyMeterologicalConditions);
                } else {
                    if (meterologicalConditionsShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.meterologicalConditions = null;
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIEnvironmentalRecordType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MIEnvironmentalRecordType) {
            final MIEnvironmentalRecordType target = this;
            final MIEnvironmentalRecordType leftObject = ((MIEnvironmentalRecordType) left);
            final MIEnvironmentalRecordType rightObject = ((MIEnvironmentalRecordType) right);
            {
                Boolean averageAirTemperatureShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetAverageAirTemperature(), rightObject.isSetAverageAirTemperature());
                if (averageAirTemperatureShouldBeMergedAndSet == Boolean.TRUE) {
                    RealPropertyType lhsAverageAirTemperature;
                    lhsAverageAirTemperature = leftObject.getAverageAirTemperature();
                    RealPropertyType rhsAverageAirTemperature;
                    rhsAverageAirTemperature = rightObject.getAverageAirTemperature();
                    RealPropertyType mergedAverageAirTemperature = ((RealPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "averageAirTemperature", lhsAverageAirTemperature), LocatorUtils.property(rightLocator, "averageAirTemperature", rhsAverageAirTemperature), lhsAverageAirTemperature, rhsAverageAirTemperature, leftObject.isSetAverageAirTemperature(), rightObject.isSetAverageAirTemperature()));
                    target.setAverageAirTemperature(mergedAverageAirTemperature);
                } else {
                    if (averageAirTemperatureShouldBeMergedAndSet == Boolean.FALSE) {
                        target.averageAirTemperature = null;
                    }
                }
            }
            {
                Boolean maxRelativeHumidityShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetMaxRelativeHumidity(), rightObject.isSetMaxRelativeHumidity());
                if (maxRelativeHumidityShouldBeMergedAndSet == Boolean.TRUE) {
                    RealPropertyType lhsMaxRelativeHumidity;
                    lhsMaxRelativeHumidity = leftObject.getMaxRelativeHumidity();
                    RealPropertyType rhsMaxRelativeHumidity;
                    rhsMaxRelativeHumidity = rightObject.getMaxRelativeHumidity();
                    RealPropertyType mergedMaxRelativeHumidity = ((RealPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "maxRelativeHumidity", lhsMaxRelativeHumidity), LocatorUtils.property(rightLocator, "maxRelativeHumidity", rhsMaxRelativeHumidity), lhsMaxRelativeHumidity, rhsMaxRelativeHumidity, leftObject.isSetMaxRelativeHumidity(), rightObject.isSetMaxRelativeHumidity()));
                    target.setMaxRelativeHumidity(mergedMaxRelativeHumidity);
                } else {
                    if (maxRelativeHumidityShouldBeMergedAndSet == Boolean.FALSE) {
                        target.maxRelativeHumidity = null;
                    }
                }
            }
            {
                Boolean maxAltitudeShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetMaxAltitude(), rightObject.isSetMaxAltitude());
                if (maxAltitudeShouldBeMergedAndSet == Boolean.TRUE) {
                    RealPropertyType lhsMaxAltitude;
                    lhsMaxAltitude = leftObject.getMaxAltitude();
                    RealPropertyType rhsMaxAltitude;
                    rhsMaxAltitude = rightObject.getMaxAltitude();
                    RealPropertyType mergedMaxAltitude = ((RealPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "maxAltitude", lhsMaxAltitude), LocatorUtils.property(rightLocator, "maxAltitude", rhsMaxAltitude), lhsMaxAltitude, rhsMaxAltitude, leftObject.isSetMaxAltitude(), rightObject.isSetMaxAltitude()));
                    target.setMaxAltitude(mergedMaxAltitude);
                } else {
                    if (maxAltitudeShouldBeMergedAndSet == Boolean.FALSE) {
                        target.maxAltitude = null;
                    }
                }
            }
            {
                Boolean meterologicalConditionsShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetMeterologicalConditions(), rightObject.isSetMeterologicalConditions());
                if (meterologicalConditionsShouldBeMergedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType lhsMeterologicalConditions;
                    lhsMeterologicalConditions = leftObject.getMeterologicalConditions();
                    CharacterStringPropertyType rhsMeterologicalConditions;
                    rhsMeterologicalConditions = rightObject.getMeterologicalConditions();
                    CharacterStringPropertyType mergedMeterologicalConditions = ((CharacterStringPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "meterologicalConditions", lhsMeterologicalConditions), LocatorUtils.property(rightLocator, "meterologicalConditions", rhsMeterologicalConditions), lhsMeterologicalConditions, rhsMeterologicalConditions, leftObject.isSetMeterologicalConditions(), rightObject.isSetMeterologicalConditions()));
                    target.setMeterologicalConditions(mergedMeterologicalConditions);
                } else {
                    if (meterologicalConditionsShouldBeMergedAndSet == Boolean.FALSE) {
                        target.meterologicalConditions = null;
                    }
                }
            }
        }
    }

    public MIEnvironmentalRecordType withAverageAirTemperature(RealPropertyType value) {
        setAverageAirTemperature(value);
        return this;
    }

    public MIEnvironmentalRecordType withMaxRelativeHumidity(RealPropertyType value) {
        setMaxRelativeHumidity(value);
        return this;
    }

    public MIEnvironmentalRecordType withMaxAltitude(RealPropertyType value) {
        setMaxAltitude(value);
        return this;
    }

    public MIEnvironmentalRecordType withMeterologicalConditions(CharacterStringPropertyType value) {
        setMeterologicalConditions(value);
        return this;
    }

}
