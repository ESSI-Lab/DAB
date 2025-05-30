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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.IntegerPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDReferenceSystemPropertyType;


/**
 * <p>Java class for MI_GCPCollection_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_GCPCollection_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gmi}AbstractMI_GeolocationInformation_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="collectionIdentification" type="{http://www.isotc211.org/2005/gco}Integer_PropertyType"/&gt;
 *         &lt;element name="collectionName" type="{http://www.isotc211.org/2005/gco}CharacterString_PropertyType"/&gt;
 *         &lt;element name="coordinateReferenceSystem" type="{http://www.isotc211.org/2005/gmd}MD_ReferenceSystem_PropertyType"/&gt;
 *         &lt;element name="gcp" type="{http://www.isotc211.org/2005/gmi}MI_GCP_PropertyType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_GCPCollection_Type", propOrder = {
    "collectionIdentification",
    "collectionName",
    "coordinateReferenceSystem",
    "gcp"
})
public class MIGCPCollectionType
    extends AbstractMIGeolocationInformationType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(required = true)
    protected IntegerPropertyType collectionIdentification;
    @XmlElement(required = true)
    protected CharacterStringPropertyType collectionName;
    @XmlElement(required = true)
    protected MDReferenceSystemPropertyType coordinateReferenceSystem;
    @XmlElement(required = true)
    protected List<MIGCPPropertyType> gcp;

    /**
     * Gets the value of the collectionIdentification property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerPropertyType }
     *     
     */
    public IntegerPropertyType getCollectionIdentification() {
        return collectionIdentification;
    }

    /**
     * Sets the value of the collectionIdentification property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerPropertyType }
     *     
     */
    public void setCollectionIdentification(IntegerPropertyType value) {
        this.collectionIdentification = value;
    }

    public boolean isSetCollectionIdentification() {
        return (this.collectionIdentification!= null);
    }

    /**
     * Gets the value of the collectionName property.
     * 
     * @return
     *     possible object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public CharacterStringPropertyType getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the value of the collectionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public void setCollectionName(CharacterStringPropertyType value) {
        this.collectionName = value;
    }

    public boolean isSetCollectionName() {
        return (this.collectionName!= null);
    }

    /**
     * Gets the value of the coordinateReferenceSystem property.
     * 
     * @return
     *     possible object is
     *     {@link MDReferenceSystemPropertyType }
     *     
     */
    public MDReferenceSystemPropertyType getCoordinateReferenceSystem() {
        return coordinateReferenceSystem;
    }

    /**
     * Sets the value of the coordinateReferenceSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link MDReferenceSystemPropertyType }
     *     
     */
    public void setCoordinateReferenceSystem(MDReferenceSystemPropertyType value) {
        this.coordinateReferenceSystem = value;
    }

    public boolean isSetCoordinateReferenceSystem() {
        return (this.coordinateReferenceSystem!= null);
    }

    /**
     * Gets the value of the gcp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gcp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGcp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIGCPPropertyType }
     * 
     * 
     */
    public List<MIGCPPropertyType> getGcp() {
        if (gcp == null) {
            gcp = new ArrayList<MIGCPPropertyType>();
        }
        return this.gcp;
    }

    public boolean isSetGcp() {
        return ((this.gcp!= null)&&(!this.gcp.isEmpty()));
    }

    public void unsetGcp() {
        this.gcp = null;
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
            IntegerPropertyType theCollectionIdentification;
            theCollectionIdentification = this.getCollectionIdentification();
            strategy.appendField(locator, this, "collectionIdentification", buffer, theCollectionIdentification, this.isSetCollectionIdentification());
        }
        {
            CharacterStringPropertyType theCollectionName;
            theCollectionName = this.getCollectionName();
            strategy.appendField(locator, this, "collectionName", buffer, theCollectionName, this.isSetCollectionName());
        }
        {
            MDReferenceSystemPropertyType theCoordinateReferenceSystem;
            theCoordinateReferenceSystem = this.getCoordinateReferenceSystem();
            strategy.appendField(locator, this, "coordinateReferenceSystem", buffer, theCoordinateReferenceSystem, this.isSetCoordinateReferenceSystem());
        }
        {
            List<MIGCPPropertyType> theGcp;
            theGcp = (this.isSetGcp()?this.getGcp():null);
            strategy.appendField(locator, this, "gcp", buffer, theGcp, this.isSetGcp());
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
        final MIGCPCollectionType that = ((MIGCPCollectionType) object);
        {
            IntegerPropertyType lhsCollectionIdentification;
            lhsCollectionIdentification = this.getCollectionIdentification();
            IntegerPropertyType rhsCollectionIdentification;
            rhsCollectionIdentification = that.getCollectionIdentification();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "collectionIdentification", lhsCollectionIdentification), LocatorUtils.property(thatLocator, "collectionIdentification", rhsCollectionIdentification), lhsCollectionIdentification, rhsCollectionIdentification, this.isSetCollectionIdentification(), that.isSetCollectionIdentification())) {
                return false;
            }
        }
        {
            CharacterStringPropertyType lhsCollectionName;
            lhsCollectionName = this.getCollectionName();
            CharacterStringPropertyType rhsCollectionName;
            rhsCollectionName = that.getCollectionName();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "collectionName", lhsCollectionName), LocatorUtils.property(thatLocator, "collectionName", rhsCollectionName), lhsCollectionName, rhsCollectionName, this.isSetCollectionName(), that.isSetCollectionName())) {
                return false;
            }
        }
        {
            MDReferenceSystemPropertyType lhsCoordinateReferenceSystem;
            lhsCoordinateReferenceSystem = this.getCoordinateReferenceSystem();
            MDReferenceSystemPropertyType rhsCoordinateReferenceSystem;
            rhsCoordinateReferenceSystem = that.getCoordinateReferenceSystem();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "coordinateReferenceSystem", lhsCoordinateReferenceSystem), LocatorUtils.property(thatLocator, "coordinateReferenceSystem", rhsCoordinateReferenceSystem), lhsCoordinateReferenceSystem, rhsCoordinateReferenceSystem, this.isSetCoordinateReferenceSystem(), that.isSetCoordinateReferenceSystem())) {
                return false;
            }
        }
        {
            List<MIGCPPropertyType> lhsGcp;
            lhsGcp = (this.isSetGcp()?this.getGcp():null);
            List<MIGCPPropertyType> rhsGcp;
            rhsGcp = (that.isSetGcp()?that.getGcp():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "gcp", lhsGcp), LocatorUtils.property(thatLocator, "gcp", rhsGcp), lhsGcp, rhsGcp, this.isSetGcp(), that.isSetGcp())) {
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
            IntegerPropertyType theCollectionIdentification;
            theCollectionIdentification = this.getCollectionIdentification();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "collectionIdentification", theCollectionIdentification), currentHashCode, theCollectionIdentification, this.isSetCollectionIdentification());
        }
        {
            CharacterStringPropertyType theCollectionName;
            theCollectionName = this.getCollectionName();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "collectionName", theCollectionName), currentHashCode, theCollectionName, this.isSetCollectionName());
        }
        {
            MDReferenceSystemPropertyType theCoordinateReferenceSystem;
            theCoordinateReferenceSystem = this.getCoordinateReferenceSystem();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "coordinateReferenceSystem", theCoordinateReferenceSystem), currentHashCode, theCoordinateReferenceSystem, this.isSetCoordinateReferenceSystem());
        }
        {
            List<MIGCPPropertyType> theGcp;
            theGcp = (this.isSetGcp()?this.getGcp():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "gcp", theGcp), currentHashCode, theGcp, this.isSetGcp());
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
        if (draftCopy instanceof MIGCPCollectionType) {
            final MIGCPCollectionType copy = ((MIGCPCollectionType) draftCopy);
            {
                Boolean collectionIdentificationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetCollectionIdentification());
                if (collectionIdentificationShouldBeCopiedAndSet == Boolean.TRUE) {
                    IntegerPropertyType sourceCollectionIdentification;
                    sourceCollectionIdentification = this.getCollectionIdentification();
                    IntegerPropertyType copyCollectionIdentification = ((IntegerPropertyType) strategy.copy(LocatorUtils.property(locator, "collectionIdentification", sourceCollectionIdentification), sourceCollectionIdentification, this.isSetCollectionIdentification()));
                    copy.setCollectionIdentification(copyCollectionIdentification);
                } else {
                    if (collectionIdentificationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.collectionIdentification = null;
                    }
                }
            }
            {
                Boolean collectionNameShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetCollectionName());
                if (collectionNameShouldBeCopiedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType sourceCollectionName;
                    sourceCollectionName = this.getCollectionName();
                    CharacterStringPropertyType copyCollectionName = ((CharacterStringPropertyType) strategy.copy(LocatorUtils.property(locator, "collectionName", sourceCollectionName), sourceCollectionName, this.isSetCollectionName()));
                    copy.setCollectionName(copyCollectionName);
                } else {
                    if (collectionNameShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.collectionName = null;
                    }
                }
            }
            {
                Boolean coordinateReferenceSystemShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetCoordinateReferenceSystem());
                if (coordinateReferenceSystemShouldBeCopiedAndSet == Boolean.TRUE) {
                    MDReferenceSystemPropertyType sourceCoordinateReferenceSystem;
                    sourceCoordinateReferenceSystem = this.getCoordinateReferenceSystem();
                    MDReferenceSystemPropertyType copyCoordinateReferenceSystem = ((MDReferenceSystemPropertyType) strategy.copy(LocatorUtils.property(locator, "coordinateReferenceSystem", sourceCoordinateReferenceSystem), sourceCoordinateReferenceSystem, this.isSetCoordinateReferenceSystem()));
                    copy.setCoordinateReferenceSystem(copyCoordinateReferenceSystem);
                } else {
                    if (coordinateReferenceSystemShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.coordinateReferenceSystem = null;
                    }
                }
            }
            {
                Boolean gcpShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetGcp());
                if (gcpShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<MIGCPPropertyType> sourceGcp;
                    sourceGcp = (this.isSetGcp()?this.getGcp():null);
                    @SuppressWarnings("unchecked")
                    List<MIGCPPropertyType> copyGcp = ((List<MIGCPPropertyType> ) strategy.copy(LocatorUtils.property(locator, "gcp", sourceGcp), sourceGcp, this.isSetGcp()));
                    copy.unsetGcp();
                    if (copyGcp!= null) {
                        List<MIGCPPropertyType> uniqueGcpl = copy.getGcp();
                        uniqueGcpl.addAll(copyGcp);
                    }
                } else {
                    if (gcpShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetGcp();
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIGCPCollectionType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MIGCPCollectionType) {
            final MIGCPCollectionType target = this;
            final MIGCPCollectionType leftObject = ((MIGCPCollectionType) left);
            final MIGCPCollectionType rightObject = ((MIGCPCollectionType) right);
            {
                Boolean collectionIdentificationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetCollectionIdentification(), rightObject.isSetCollectionIdentification());
                if (collectionIdentificationShouldBeMergedAndSet == Boolean.TRUE) {
                    IntegerPropertyType lhsCollectionIdentification;
                    lhsCollectionIdentification = leftObject.getCollectionIdentification();
                    IntegerPropertyType rhsCollectionIdentification;
                    rhsCollectionIdentification = rightObject.getCollectionIdentification();
                    IntegerPropertyType mergedCollectionIdentification = ((IntegerPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "collectionIdentification", lhsCollectionIdentification), LocatorUtils.property(rightLocator, "collectionIdentification", rhsCollectionIdentification), lhsCollectionIdentification, rhsCollectionIdentification, leftObject.isSetCollectionIdentification(), rightObject.isSetCollectionIdentification()));
                    target.setCollectionIdentification(mergedCollectionIdentification);
                } else {
                    if (collectionIdentificationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.collectionIdentification = null;
                    }
                }
            }
            {
                Boolean collectionNameShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetCollectionName(), rightObject.isSetCollectionName());
                if (collectionNameShouldBeMergedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType lhsCollectionName;
                    lhsCollectionName = leftObject.getCollectionName();
                    CharacterStringPropertyType rhsCollectionName;
                    rhsCollectionName = rightObject.getCollectionName();
                    CharacterStringPropertyType mergedCollectionName = ((CharacterStringPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "collectionName", lhsCollectionName), LocatorUtils.property(rightLocator, "collectionName", rhsCollectionName), lhsCollectionName, rhsCollectionName, leftObject.isSetCollectionName(), rightObject.isSetCollectionName()));
                    target.setCollectionName(mergedCollectionName);
                } else {
                    if (collectionNameShouldBeMergedAndSet == Boolean.FALSE) {
                        target.collectionName = null;
                    }
                }
            }
            {
                Boolean coordinateReferenceSystemShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetCoordinateReferenceSystem(), rightObject.isSetCoordinateReferenceSystem());
                if (coordinateReferenceSystemShouldBeMergedAndSet == Boolean.TRUE) {
                    MDReferenceSystemPropertyType lhsCoordinateReferenceSystem;
                    lhsCoordinateReferenceSystem = leftObject.getCoordinateReferenceSystem();
                    MDReferenceSystemPropertyType rhsCoordinateReferenceSystem;
                    rhsCoordinateReferenceSystem = rightObject.getCoordinateReferenceSystem();
                    MDReferenceSystemPropertyType mergedCoordinateReferenceSystem = ((MDReferenceSystemPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "coordinateReferenceSystem", lhsCoordinateReferenceSystem), LocatorUtils.property(rightLocator, "coordinateReferenceSystem", rhsCoordinateReferenceSystem), lhsCoordinateReferenceSystem, rhsCoordinateReferenceSystem, leftObject.isSetCoordinateReferenceSystem(), rightObject.isSetCoordinateReferenceSystem()));
                    target.setCoordinateReferenceSystem(mergedCoordinateReferenceSystem);
                } else {
                    if (coordinateReferenceSystemShouldBeMergedAndSet == Boolean.FALSE) {
                        target.coordinateReferenceSystem = null;
                    }
                }
            }
            {
                Boolean gcpShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetGcp(), rightObject.isSetGcp());
                if (gcpShouldBeMergedAndSet == Boolean.TRUE) {
                    List<MIGCPPropertyType> lhsGcp;
                    lhsGcp = (leftObject.isSetGcp()?leftObject.getGcp():null);
                    List<MIGCPPropertyType> rhsGcp;
                    rhsGcp = (rightObject.isSetGcp()?rightObject.getGcp():null);
                    List<MIGCPPropertyType> mergedGcp = ((List<MIGCPPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "gcp", lhsGcp), LocatorUtils.property(rightLocator, "gcp", rhsGcp), lhsGcp, rhsGcp, leftObject.isSetGcp(), rightObject.isSetGcp()));
                    target.unsetGcp();
                    if (mergedGcp!= null) {
                        List<MIGCPPropertyType> uniqueGcpl = target.getGcp();
                        uniqueGcpl.addAll(mergedGcp);
                    }
                } else {
                    if (gcpShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetGcp();
                    }
                }
            }
        }
    }

    public void setGcp(List<MIGCPPropertyType> value) {
        this.gcp = null;
        if (value!= null) {
            List<MIGCPPropertyType> draftl = this.getGcp();
            draftl.addAll(value);
        }
    }

    public MIGCPCollectionType withCollectionIdentification(IntegerPropertyType value) {
        setCollectionIdentification(value);
        return this;
    }

    public MIGCPCollectionType withCollectionName(CharacterStringPropertyType value) {
        setCollectionName(value);
        return this;
    }

    public MIGCPCollectionType withCoordinateReferenceSystem(MDReferenceSystemPropertyType value) {
        setCoordinateReferenceSystem(value);
        return this;
    }

    public MIGCPCollectionType withGcp(MIGCPPropertyType... values) {
        if (values!= null) {
            for (MIGCPPropertyType value: values) {
                getGcp().add(value);
            }
        }
        return this;
    }

    public MIGCPCollectionType withGcp(Collection<MIGCPPropertyType> values) {
        if (values!= null) {
            getGcp().addAll(values);
        }
        return this;
    }

    public MIGCPCollectionType withGcp(List<MIGCPPropertyType> value) {
        setGcp(value);
        return this;
    }

    @Override
    public MIGCPCollectionType withQualityInfo(DQDataQualityPropertyType... values) {
        if (values!= null) {
            for (DQDataQualityPropertyType value: values) {
                getQualityInfo().add(value);
            }
        }
        return this;
    }

    @Override
    public MIGCPCollectionType withQualityInfo(Collection<DQDataQualityPropertyType> values) {
        if (values!= null) {
            getQualityInfo().addAll(values);
        }
        return this;
    }

    @Override
    public MIGCPCollectionType withQualityInfo(List<DQDataQualityPropertyType> value) {
        setQualityInfo(value);
        return this;
    }

}
