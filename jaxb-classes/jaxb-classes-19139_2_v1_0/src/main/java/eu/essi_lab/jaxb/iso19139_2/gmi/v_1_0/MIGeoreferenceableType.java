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

import net.opengis.iso19139.gmd.v_20060504.MDGeoreferenceableType;


/**
 * Description: Description of information provided in metadata that allows the geographic or map location raster points to be located - FGDC: Georeferencing_Description - shortName: IGeoref
 * 
 * <p>Java class for MI_Georeferenceable_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_Georeferenceable_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gmd}MD_Georeferenceable_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geolocationInformation" type="{http://www.isotc211.org/2005/gmi}MI_GeolocationInformation_PropertyType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="platformParameters" type="{http://www.isotc211.org/2005/gmi}MI_Platform_PropertyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_Georeferenceable_Type", propOrder = {
    "geolocationInformation",
    "platformParameters"
})
public class MIGeoreferenceableType
    extends MDGeoreferenceableType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(required = true)
    protected List<MIGeolocationInformationPropertyType> geolocationInformation;
    protected MIPlatformPropertyType platformParameters;

    /**
     * Gets the value of the geolocationInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geolocationInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeolocationInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIGeolocationInformationPropertyType }
     * 
     * 
     */
    public List<MIGeolocationInformationPropertyType> getGeolocationInformation() {
        if (geolocationInformation == null) {
            geolocationInformation = new ArrayList<MIGeolocationInformationPropertyType>();
        }
        return this.geolocationInformation;
    }

    public boolean isSetGeolocationInformation() {
        return ((this.geolocationInformation!= null)&&(!this.geolocationInformation.isEmpty()));
    }

    public void unsetGeolocationInformation() {
        this.geolocationInformation = null;
    }

    /**
     * Gets the value of the platformParameters property.
     * 
     * @return
     *     possible object is
     *     {@link MIPlatformPropertyType }
     *     
     */
    public MIPlatformPropertyType getPlatformParameters() {
        return platformParameters;
    }

    /**
     * Sets the value of the platformParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link MIPlatformPropertyType }
     *     
     */
    public void setPlatformParameters(MIPlatformPropertyType value) {
        this.platformParameters = value;
    }

    public boolean isSetPlatformParameters() {
        return (this.platformParameters!= null);
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
            List<MIGeolocationInformationPropertyType> theGeolocationInformation;
            theGeolocationInformation = (this.isSetGeolocationInformation()?this.getGeolocationInformation():null);
            strategy.appendField(locator, this, "geolocationInformation", buffer, theGeolocationInformation, this.isSetGeolocationInformation());
        }
        {
            MIPlatformPropertyType thePlatformParameters;
            thePlatformParameters = this.getPlatformParameters();
            strategy.appendField(locator, this, "platformParameters", buffer, thePlatformParameters, this.isSetPlatformParameters());
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
        final MIGeoreferenceableType that = ((MIGeoreferenceableType) object);
        {
            List<MIGeolocationInformationPropertyType> lhsGeolocationInformation;
            lhsGeolocationInformation = (this.isSetGeolocationInformation()?this.getGeolocationInformation():null);
            List<MIGeolocationInformationPropertyType> rhsGeolocationInformation;
            rhsGeolocationInformation = (that.isSetGeolocationInformation()?that.getGeolocationInformation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "geolocationInformation", lhsGeolocationInformation), LocatorUtils.property(thatLocator, "geolocationInformation", rhsGeolocationInformation), lhsGeolocationInformation, rhsGeolocationInformation, this.isSetGeolocationInformation(), that.isSetGeolocationInformation())) {
                return false;
            }
        }
        {
            MIPlatformPropertyType lhsPlatformParameters;
            lhsPlatformParameters = this.getPlatformParameters();
            MIPlatformPropertyType rhsPlatformParameters;
            rhsPlatformParameters = that.getPlatformParameters();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "platformParameters", lhsPlatformParameters), LocatorUtils.property(thatLocator, "platformParameters", rhsPlatformParameters), lhsPlatformParameters, rhsPlatformParameters, this.isSetPlatformParameters(), that.isSetPlatformParameters())) {
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
            List<MIGeolocationInformationPropertyType> theGeolocationInformation;
            theGeolocationInformation = (this.isSetGeolocationInformation()?this.getGeolocationInformation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "geolocationInformation", theGeolocationInformation), currentHashCode, theGeolocationInformation, this.isSetGeolocationInformation());
        }
        {
            MIPlatformPropertyType thePlatformParameters;
            thePlatformParameters = this.getPlatformParameters();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "platformParameters", thePlatformParameters), currentHashCode, thePlatformParameters, this.isSetPlatformParameters());
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
        if (draftCopy instanceof MIGeoreferenceableType) {
            final MIGeoreferenceableType copy = ((MIGeoreferenceableType) draftCopy);
            {
                Boolean geolocationInformationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetGeolocationInformation());
                if (geolocationInformationShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<MIGeolocationInformationPropertyType> sourceGeolocationInformation;
                    sourceGeolocationInformation = (this.isSetGeolocationInformation()?this.getGeolocationInformation():null);
                    @SuppressWarnings("unchecked")
                    List<MIGeolocationInformationPropertyType> copyGeolocationInformation = ((List<MIGeolocationInformationPropertyType> ) strategy.copy(LocatorUtils.property(locator, "geolocationInformation", sourceGeolocationInformation), sourceGeolocationInformation, this.isSetGeolocationInformation()));
                    copy.unsetGeolocationInformation();
                    if (copyGeolocationInformation!= null) {
                        List<MIGeolocationInformationPropertyType> uniqueGeolocationInformationl = copy.getGeolocationInformation();
                        uniqueGeolocationInformationl.addAll(copyGeolocationInformation);
                    }
                } else {
                    if (geolocationInformationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetGeolocationInformation();
                    }
                }
            }
            {
                Boolean platformParametersShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetPlatformParameters());
                if (platformParametersShouldBeCopiedAndSet == Boolean.TRUE) {
                    MIPlatformPropertyType sourcePlatformParameters;
                    sourcePlatformParameters = this.getPlatformParameters();
                    MIPlatformPropertyType copyPlatformParameters = ((MIPlatformPropertyType) strategy.copy(LocatorUtils.property(locator, "platformParameters", sourcePlatformParameters), sourcePlatformParameters, this.isSetPlatformParameters()));
                    copy.setPlatformParameters(copyPlatformParameters);
                } else {
                    if (platformParametersShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.platformParameters = null;
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIGeoreferenceableType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MIGeoreferenceableType) {
            final MIGeoreferenceableType target = this;
            final MIGeoreferenceableType leftObject = ((MIGeoreferenceableType) left);
            final MIGeoreferenceableType rightObject = ((MIGeoreferenceableType) right);
            {
                Boolean geolocationInformationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetGeolocationInformation(), rightObject.isSetGeolocationInformation());
                if (geolocationInformationShouldBeMergedAndSet == Boolean.TRUE) {
                    List<MIGeolocationInformationPropertyType> lhsGeolocationInformation;
                    lhsGeolocationInformation = (leftObject.isSetGeolocationInformation()?leftObject.getGeolocationInformation():null);
                    List<MIGeolocationInformationPropertyType> rhsGeolocationInformation;
                    rhsGeolocationInformation = (rightObject.isSetGeolocationInformation()?rightObject.getGeolocationInformation():null);
                    List<MIGeolocationInformationPropertyType> mergedGeolocationInformation = ((List<MIGeolocationInformationPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "geolocationInformation", lhsGeolocationInformation), LocatorUtils.property(rightLocator, "geolocationInformation", rhsGeolocationInformation), lhsGeolocationInformation, rhsGeolocationInformation, leftObject.isSetGeolocationInformation(), rightObject.isSetGeolocationInformation()));
                    target.unsetGeolocationInformation();
                    if (mergedGeolocationInformation!= null) {
                        List<MIGeolocationInformationPropertyType> uniqueGeolocationInformationl = target.getGeolocationInformation();
                        uniqueGeolocationInformationl.addAll(mergedGeolocationInformation);
                    }
                } else {
                    if (geolocationInformationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetGeolocationInformation();
                    }
                }
            }
            {
                Boolean platformParametersShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetPlatformParameters(), rightObject.isSetPlatformParameters());
                if (platformParametersShouldBeMergedAndSet == Boolean.TRUE) {
                    MIPlatformPropertyType lhsPlatformParameters;
                    lhsPlatformParameters = leftObject.getPlatformParameters();
                    MIPlatformPropertyType rhsPlatformParameters;
                    rhsPlatformParameters = rightObject.getPlatformParameters();
                    MIPlatformPropertyType mergedPlatformParameters = ((MIPlatformPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "platformParameters", lhsPlatformParameters), LocatorUtils.property(rightLocator, "platformParameters", rhsPlatformParameters), lhsPlatformParameters, rhsPlatformParameters, leftObject.isSetPlatformParameters(), rightObject.isSetPlatformParameters()));
                    target.setPlatformParameters(mergedPlatformParameters);
                } else {
                    if (platformParametersShouldBeMergedAndSet == Boolean.FALSE) {
                        target.platformParameters = null;
                    }
                }
            }
        }
    }

    public void setGeolocationInformation(List<MIGeolocationInformationPropertyType> value) {
        this.geolocationInformation = null;
        if (value!= null) {
            List<MIGeolocationInformationPropertyType> draftl = this.getGeolocationInformation();
            draftl.addAll(value);
        }
    }

    public MIGeoreferenceableType withGeolocationInformation(MIGeolocationInformationPropertyType... values) {
        if (values!= null) {
            for (MIGeolocationInformationPropertyType value: values) {
                getGeolocationInformation().add(value);
            }
        }
        return this;
    }

    public MIGeoreferenceableType withGeolocationInformation(Collection<MIGeolocationInformationPropertyType> values) {
        if (values!= null) {
            getGeolocationInformation().addAll(values);
        }
        return this;
    }

    public MIGeoreferenceableType withPlatformParameters(MIPlatformPropertyType value) {
        setPlatformParameters(value);
        return this;
    }

    public MIGeoreferenceableType withGeolocationInformation(List<MIGeolocationInformationPropertyType> value) {
        setGeolocationInformation(value);
        return this;
    }

}