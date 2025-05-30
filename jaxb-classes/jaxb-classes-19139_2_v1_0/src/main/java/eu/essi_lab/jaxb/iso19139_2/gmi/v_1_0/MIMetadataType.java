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

import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;


/**
 * Description: Root entity that defines information about imagery or gridded data - shortName: IMetadata
 * 
 * <p>Java class for MI_Metadata_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_Metadata_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gmd}MD_Metadata_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="acquisitionInformation" type="{http://www.isotc211.org/2005/gmi}MI_AcquisitionInformation_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_Metadata_Type", propOrder = {
    "acquisitionInformation"
})
public class MIMetadataType
    extends MDMetadataType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    protected List<MIAcquisitionInformationPropertyType> acquisitionInformation;

    /**
     * Gets the value of the acquisitionInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the acquisitionInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAcquisitionInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIAcquisitionInformationPropertyType }
     * 
     * 
     */
    public List<MIAcquisitionInformationPropertyType> getAcquisitionInformation() {
        if (acquisitionInformation == null) {
            acquisitionInformation = new ArrayList<MIAcquisitionInformationPropertyType>();
        }
        return this.acquisitionInformation;
    }

    public boolean isSetAcquisitionInformation() {
        return ((this.acquisitionInformation!= null)&&(!this.acquisitionInformation.isEmpty()));
    }

    public void unsetAcquisitionInformation() {
        this.acquisitionInformation = null;
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
            List<MIAcquisitionInformationPropertyType> theAcquisitionInformation;
            theAcquisitionInformation = (this.isSetAcquisitionInformation()?this.getAcquisitionInformation():null);
            strategy.appendField(locator, this, "acquisitionInformation", buffer, theAcquisitionInformation, this.isSetAcquisitionInformation());
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
        final MIMetadataType that = ((MIMetadataType) object);
        {
            List<MIAcquisitionInformationPropertyType> lhsAcquisitionInformation;
            lhsAcquisitionInformation = (this.isSetAcquisitionInformation()?this.getAcquisitionInformation():null);
            List<MIAcquisitionInformationPropertyType> rhsAcquisitionInformation;
            rhsAcquisitionInformation = (that.isSetAcquisitionInformation()?that.getAcquisitionInformation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "acquisitionInformation", lhsAcquisitionInformation), LocatorUtils.property(thatLocator, "acquisitionInformation", rhsAcquisitionInformation), lhsAcquisitionInformation, rhsAcquisitionInformation, this.isSetAcquisitionInformation(), that.isSetAcquisitionInformation())) {
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
            List<MIAcquisitionInformationPropertyType> theAcquisitionInformation;
            theAcquisitionInformation = (this.isSetAcquisitionInformation()?this.getAcquisitionInformation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "acquisitionInformation", theAcquisitionInformation), currentHashCode, theAcquisitionInformation, this.isSetAcquisitionInformation());
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
        if (draftCopy instanceof MIMetadataType) {
            final MIMetadataType copy = ((MIMetadataType) draftCopy);
            {
                Boolean acquisitionInformationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetAcquisitionInformation());
                if (acquisitionInformationShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<MIAcquisitionInformationPropertyType> sourceAcquisitionInformation;
                    sourceAcquisitionInformation = (this.isSetAcquisitionInformation()?this.getAcquisitionInformation():null);
                    @SuppressWarnings("unchecked")
                    List<MIAcquisitionInformationPropertyType> copyAcquisitionInformation = ((List<MIAcquisitionInformationPropertyType> ) strategy.copy(LocatorUtils.property(locator, "acquisitionInformation", sourceAcquisitionInformation), sourceAcquisitionInformation, this.isSetAcquisitionInformation()));
                    copy.unsetAcquisitionInformation();
                    if (copyAcquisitionInformation!= null) {
                        List<MIAcquisitionInformationPropertyType> uniqueAcquisitionInformationl = copy.getAcquisitionInformation();
                        uniqueAcquisitionInformationl.addAll(copyAcquisitionInformation);
                    }
                } else {
                    if (acquisitionInformationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetAcquisitionInformation();
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIMetadataType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MIMetadataType) {
            final MIMetadataType target = this;
            final MIMetadataType leftObject = ((MIMetadataType) left);
            final MIMetadataType rightObject = ((MIMetadataType) right);
            {
                Boolean acquisitionInformationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetAcquisitionInformation(), rightObject.isSetAcquisitionInformation());
                if (acquisitionInformationShouldBeMergedAndSet == Boolean.TRUE) {
                    List<MIAcquisitionInformationPropertyType> lhsAcquisitionInformation;
                    lhsAcquisitionInformation = (leftObject.isSetAcquisitionInformation()?leftObject.getAcquisitionInformation():null);
                    List<MIAcquisitionInformationPropertyType> rhsAcquisitionInformation;
                    rhsAcquisitionInformation = (rightObject.isSetAcquisitionInformation()?rightObject.getAcquisitionInformation():null);
                    List<MIAcquisitionInformationPropertyType> mergedAcquisitionInformation = ((List<MIAcquisitionInformationPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "acquisitionInformation", lhsAcquisitionInformation), LocatorUtils.property(rightLocator, "acquisitionInformation", rhsAcquisitionInformation), lhsAcquisitionInformation, rhsAcquisitionInformation, leftObject.isSetAcquisitionInformation(), rightObject.isSetAcquisitionInformation()));
                    target.unsetAcquisitionInformation();
                    if (mergedAcquisitionInformation!= null) {
                        List<MIAcquisitionInformationPropertyType> uniqueAcquisitionInformationl = target.getAcquisitionInformation();
                        uniqueAcquisitionInformationl.addAll(mergedAcquisitionInformation);
                    }
                } else {
                    if (acquisitionInformationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetAcquisitionInformation();
                    }
                }
            }
        }
    }

    public void setAcquisitionInformation(List<MIAcquisitionInformationPropertyType> value) {
        this.acquisitionInformation = null;
        if (value!= null) {
            List<MIAcquisitionInformationPropertyType> draftl = this.getAcquisitionInformation();
            draftl.addAll(value);
        }
    }

    public MIMetadataType withAcquisitionInformation(MIAcquisitionInformationPropertyType... values) {
        if (values!= null) {
            for (MIAcquisitionInformationPropertyType value: values) {
                getAcquisitionInformation().add(value);
            }
        }
        return this;
    }

    public MIMetadataType withAcquisitionInformation(Collection<MIAcquisitionInformationPropertyType> values) {
        if (values!= null) {
            getAcquisitionInformation().addAll(values);
        }
        return this;
    }

    public MIMetadataType withAcquisitionInformation(List<MIAcquisitionInformationPropertyType> value) {
        setAcquisitionInformation(value);
        return this;
    }

}
