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
import net.opengis.iso19139.gmd.v_20060504.MDFormatPropertyType;


/**
 * <p>Java class for MX_DataFile_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MX_DataFile_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gco}AbstractObject_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fileFormat" type="{http://www.isotc211.org/2005/gmd}MD_Format_PropertyType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MX_DataFile_Type", propOrder = {
    "fileFormat"
})
public class MXDataFileType
    extends AbstractObjectType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(required = true)
    protected MDFormatPropertyType fileFormat;

    /**
     * Gets the value of the fileFormat property.
     * 
     * @return
     *     possible object is
     *     {@link MDFormatPropertyType }
     *     
     */
    public MDFormatPropertyType getFileFormat() {
        return fileFormat;
    }

    /**
     * Sets the value of the fileFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link MDFormatPropertyType }
     *     
     */
    public void setFileFormat(MDFormatPropertyType value) {
        this.fileFormat = value;
    }

    public boolean isSetFileFormat() {
        return (this.fileFormat!= null);
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
            MDFormatPropertyType theFileFormat;
            theFileFormat = this.getFileFormat();
            strategy.appendField(locator, this, "fileFormat", buffer, theFileFormat, this.isSetFileFormat());
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
        final MXDataFileType that = ((MXDataFileType) object);
        {
            MDFormatPropertyType lhsFileFormat;
            lhsFileFormat = this.getFileFormat();
            MDFormatPropertyType rhsFileFormat;
            rhsFileFormat = that.getFileFormat();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fileFormat", lhsFileFormat), LocatorUtils.property(thatLocator, "fileFormat", rhsFileFormat), lhsFileFormat, rhsFileFormat, this.isSetFileFormat(), that.isSetFileFormat())) {
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
            MDFormatPropertyType theFileFormat;
            theFileFormat = this.getFileFormat();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "fileFormat", theFileFormat), currentHashCode, theFileFormat, this.isSetFileFormat());
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
        if (draftCopy instanceof MXDataFileType) {
            final MXDataFileType copy = ((MXDataFileType) draftCopy);
            {
                Boolean fileFormatShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetFileFormat());
                if (fileFormatShouldBeCopiedAndSet == Boolean.TRUE) {
                    MDFormatPropertyType sourceFileFormat;
                    sourceFileFormat = this.getFileFormat();
                    MDFormatPropertyType copyFileFormat = ((MDFormatPropertyType) strategy.copy(LocatorUtils.property(locator, "fileFormat", sourceFileFormat), sourceFileFormat, this.isSetFileFormat()));
                    copy.setFileFormat(copyFileFormat);
                } else {
                    if (fileFormatShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.fileFormat = null;
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MXDataFileType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MXDataFileType) {
            final MXDataFileType target = this;
            final MXDataFileType leftObject = ((MXDataFileType) left);
            final MXDataFileType rightObject = ((MXDataFileType) right);
            {
                Boolean fileFormatShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetFileFormat(), rightObject.isSetFileFormat());
                if (fileFormatShouldBeMergedAndSet == Boolean.TRUE) {
                    MDFormatPropertyType lhsFileFormat;
                    lhsFileFormat = leftObject.getFileFormat();
                    MDFormatPropertyType rhsFileFormat;
                    rhsFileFormat = rightObject.getFileFormat();
                    MDFormatPropertyType mergedFileFormat = ((MDFormatPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "fileFormat", lhsFileFormat), LocatorUtils.property(rightLocator, "fileFormat", rhsFileFormat), lhsFileFormat, rhsFileFormat, leftObject.isSetFileFormat(), rightObject.isSetFileFormat()));
                    target.setFileFormat(mergedFileFormat);
                } else {
                    if (fileFormatShouldBeMergedAndSet == Boolean.FALSE) {
                        target.fileFormat = null;
                    }
                }
            }
        }
    }

    public MXDataFileType withFileFormat(MDFormatPropertyType value) {
        setFileFormat(value);
        return this;
    }

}