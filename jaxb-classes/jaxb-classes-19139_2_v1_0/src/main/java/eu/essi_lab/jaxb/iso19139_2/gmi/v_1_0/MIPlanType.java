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

import net.opengis.iso19139.gco.v_20060504.AbstractObjectType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDProgressCodePropertyType;


/**
 * Description: Designations for the planning information related to meeting requirements - shortName: PlanId
 * 
 * <p>Java class for MI_Plan_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MI_Plan_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gco}AbstractObject_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="type" type="{http://www.isotc211.org/2005/gmi}MI_GeometryTypeCode_PropertyType" minOccurs="0"/&gt;
 *         &lt;element name="status" type="{http://www.isotc211.org/2005/gmd}MD_ProgressCode_PropertyType"/&gt;
 *         &lt;element name="citation" type="{http://www.isotc211.org/2005/gmd}CI_Citation_PropertyType"/&gt;
 *         &lt;element name="satisfiedRequirement" type="{http://www.isotc211.org/2005/gmi}MI_Requirement_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="operation" type="{http://www.isotc211.org/2005/gmi}MI_Operation_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MI_Plan_Type", propOrder = {
    "type",
    "status",
    "citation",
    "satisfiedRequirement",
    "operation"
})
public class MIPlanType
    extends AbstractObjectType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    protected MIGeometryTypeCodePropertyType type;
    @XmlElement(required = true)
    protected MDProgressCodePropertyType status;
    @XmlElement(required = true)
    protected CICitationPropertyType citation;
    protected List<MIRequirementPropertyType> satisfiedRequirement;
    protected List<MIOperationPropertyType> operation;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link MIGeometryTypeCodePropertyType }
     *     
     */
    public MIGeometryTypeCodePropertyType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link MIGeometryTypeCodePropertyType }
     *     
     */
    public void setType(MIGeometryTypeCodePropertyType value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link MDProgressCodePropertyType }
     *     
     */
    public MDProgressCodePropertyType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link MDProgressCodePropertyType }
     *     
     */
    public void setStatus(MDProgressCodePropertyType value) {
        this.status = value;
    }

    public boolean isSetStatus() {
        return (this.status!= null);
    }

    /**
     * Gets the value of the citation property.
     * 
     * @return
     *     possible object is
     *     {@link CICitationPropertyType }
     *     
     */
    public CICitationPropertyType getCitation() {
        return citation;
    }

    /**
     * Sets the value of the citation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CICitationPropertyType }
     *     
     */
    public void setCitation(CICitationPropertyType value) {
        this.citation = value;
    }

    public boolean isSetCitation() {
        return (this.citation!= null);
    }

    /**
     * Gets the value of the satisfiedRequirement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the satisfiedRequirement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSatisfiedRequirement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIRequirementPropertyType }
     * 
     * 
     */
    public List<MIRequirementPropertyType> getSatisfiedRequirement() {
        if (satisfiedRequirement == null) {
            satisfiedRequirement = new ArrayList<MIRequirementPropertyType>();
        }
        return this.satisfiedRequirement;
    }

    public boolean isSetSatisfiedRequirement() {
        return ((this.satisfiedRequirement!= null)&&(!this.satisfiedRequirement.isEmpty()));
    }

    public void unsetSatisfiedRequirement() {
        this.satisfiedRequirement = null;
    }

    /**
     * Gets the value of the operation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MIOperationPropertyType }
     * 
     * 
     */
    public List<MIOperationPropertyType> getOperation() {
        if (operation == null) {
            operation = new ArrayList<MIOperationPropertyType>();
        }
        return this.operation;
    }

    public boolean isSetOperation() {
        return ((this.operation!= null)&&(!this.operation.isEmpty()));
    }

    public void unsetOperation() {
        this.operation = null;
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
            MIGeometryTypeCodePropertyType theType;
            theType = this.getType();
            strategy.appendField(locator, this, "type", buffer, theType, this.isSetType());
        }
        {
            MDProgressCodePropertyType theStatus;
            theStatus = this.getStatus();
            strategy.appendField(locator, this, "status", buffer, theStatus, this.isSetStatus());
        }
        {
            CICitationPropertyType theCitation;
            theCitation = this.getCitation();
            strategy.appendField(locator, this, "citation", buffer, theCitation, this.isSetCitation());
        }
        {
            List<MIRequirementPropertyType> theSatisfiedRequirement;
            theSatisfiedRequirement = (this.isSetSatisfiedRequirement()?this.getSatisfiedRequirement():null);
            strategy.appendField(locator, this, "satisfiedRequirement", buffer, theSatisfiedRequirement, this.isSetSatisfiedRequirement());
        }
        {
            List<MIOperationPropertyType> theOperation;
            theOperation = (this.isSetOperation()?this.getOperation():null);
            strategy.appendField(locator, this, "operation", buffer, theOperation, this.isSetOperation());
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
        final MIPlanType that = ((MIPlanType) object);
        {
            MIGeometryTypeCodePropertyType lhsType;
            lhsType = this.getType();
            MIGeometryTypeCodePropertyType rhsType;
            rhsType = that.getType();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsType), LocatorUtils.property(thatLocator, "type", rhsType), lhsType, rhsType, this.isSetType(), that.isSetType())) {
                return false;
            }
        }
        {
            MDProgressCodePropertyType lhsStatus;
            lhsStatus = this.getStatus();
            MDProgressCodePropertyType rhsStatus;
            rhsStatus = that.getStatus();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "status", lhsStatus), LocatorUtils.property(thatLocator, "status", rhsStatus), lhsStatus, rhsStatus, this.isSetStatus(), that.isSetStatus())) {
                return false;
            }
        }
        {
            CICitationPropertyType lhsCitation;
            lhsCitation = this.getCitation();
            CICitationPropertyType rhsCitation;
            rhsCitation = that.getCitation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "citation", lhsCitation), LocatorUtils.property(thatLocator, "citation", rhsCitation), lhsCitation, rhsCitation, this.isSetCitation(), that.isSetCitation())) {
                return false;
            }
        }
        {
            List<MIRequirementPropertyType> lhsSatisfiedRequirement;
            lhsSatisfiedRequirement = (this.isSetSatisfiedRequirement()?this.getSatisfiedRequirement():null);
            List<MIRequirementPropertyType> rhsSatisfiedRequirement;
            rhsSatisfiedRequirement = (that.isSetSatisfiedRequirement()?that.getSatisfiedRequirement():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "satisfiedRequirement", lhsSatisfiedRequirement), LocatorUtils.property(thatLocator, "satisfiedRequirement", rhsSatisfiedRequirement), lhsSatisfiedRequirement, rhsSatisfiedRequirement, this.isSetSatisfiedRequirement(), that.isSetSatisfiedRequirement())) {
                return false;
            }
        }
        {
            List<MIOperationPropertyType> lhsOperation;
            lhsOperation = (this.isSetOperation()?this.getOperation():null);
            List<MIOperationPropertyType> rhsOperation;
            rhsOperation = (that.isSetOperation()?that.getOperation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "operation", lhsOperation), LocatorUtils.property(thatLocator, "operation", rhsOperation), lhsOperation, rhsOperation, this.isSetOperation(), that.isSetOperation())) {
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
            MIGeometryTypeCodePropertyType theType;
            theType = this.getType();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "type", theType), currentHashCode, theType, this.isSetType());
        }
        {
            MDProgressCodePropertyType theStatus;
            theStatus = this.getStatus();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "status", theStatus), currentHashCode, theStatus, this.isSetStatus());
        }
        {
            CICitationPropertyType theCitation;
            theCitation = this.getCitation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "citation", theCitation), currentHashCode, theCitation, this.isSetCitation());
        }
        {
            List<MIRequirementPropertyType> theSatisfiedRequirement;
            theSatisfiedRequirement = (this.isSetSatisfiedRequirement()?this.getSatisfiedRequirement():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "satisfiedRequirement", theSatisfiedRequirement), currentHashCode, theSatisfiedRequirement, this.isSetSatisfiedRequirement());
        }
        {
            List<MIOperationPropertyType> theOperation;
            theOperation = (this.isSetOperation()?this.getOperation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "operation", theOperation), currentHashCode, theOperation, this.isSetOperation());
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
        if (draftCopy instanceof MIPlanType) {
            final MIPlanType copy = ((MIPlanType) draftCopy);
            {
                Boolean typeShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetType());
                if (typeShouldBeCopiedAndSet == Boolean.TRUE) {
                    MIGeometryTypeCodePropertyType sourceType;
                    sourceType = this.getType();
                    MIGeometryTypeCodePropertyType copyType = ((MIGeometryTypeCodePropertyType) strategy.copy(LocatorUtils.property(locator, "type", sourceType), sourceType, this.isSetType()));
                    copy.setType(copyType);
                } else {
                    if (typeShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.type = null;
                    }
                }
            }
            {
                Boolean statusShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetStatus());
                if (statusShouldBeCopiedAndSet == Boolean.TRUE) {
                    MDProgressCodePropertyType sourceStatus;
                    sourceStatus = this.getStatus();
                    MDProgressCodePropertyType copyStatus = ((MDProgressCodePropertyType) strategy.copy(LocatorUtils.property(locator, "status", sourceStatus), sourceStatus, this.isSetStatus()));
                    copy.setStatus(copyStatus);
                } else {
                    if (statusShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.status = null;
                    }
                }
            }
            {
                Boolean citationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetCitation());
                if (citationShouldBeCopiedAndSet == Boolean.TRUE) {
                    CICitationPropertyType sourceCitation;
                    sourceCitation = this.getCitation();
                    CICitationPropertyType copyCitation = ((CICitationPropertyType) strategy.copy(LocatorUtils.property(locator, "citation", sourceCitation), sourceCitation, this.isSetCitation()));
                    copy.setCitation(copyCitation);
                } else {
                    if (citationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.citation = null;
                    }
                }
            }
            {
                Boolean satisfiedRequirementShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetSatisfiedRequirement());
                if (satisfiedRequirementShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<MIRequirementPropertyType> sourceSatisfiedRequirement;
                    sourceSatisfiedRequirement = (this.isSetSatisfiedRequirement()?this.getSatisfiedRequirement():null);
                    @SuppressWarnings("unchecked")
                    List<MIRequirementPropertyType> copySatisfiedRequirement = ((List<MIRequirementPropertyType> ) strategy.copy(LocatorUtils.property(locator, "satisfiedRequirement", sourceSatisfiedRequirement), sourceSatisfiedRequirement, this.isSetSatisfiedRequirement()));
                    copy.unsetSatisfiedRequirement();
                    if (copySatisfiedRequirement!= null) {
                        List<MIRequirementPropertyType> uniqueSatisfiedRequirementl = copy.getSatisfiedRequirement();
                        uniqueSatisfiedRequirementl.addAll(copySatisfiedRequirement);
                    }
                } else {
                    if (satisfiedRequirementShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetSatisfiedRequirement();
                    }
                }
            }
            {
                Boolean operationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetOperation());
                if (operationShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<MIOperationPropertyType> sourceOperation;
                    sourceOperation = (this.isSetOperation()?this.getOperation():null);
                    @SuppressWarnings("unchecked")
                    List<MIOperationPropertyType> copyOperation = ((List<MIOperationPropertyType> ) strategy.copy(LocatorUtils.property(locator, "operation", sourceOperation), sourceOperation, this.isSetOperation()));
                    copy.unsetOperation();
                    if (copyOperation!= null) {
                        List<MIOperationPropertyType> uniqueOperationl = copy.getOperation();
                        uniqueOperationl.addAll(copyOperation);
                    }
                } else {
                    if (operationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetOperation();
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new MIPlanType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof MIPlanType) {
            final MIPlanType target = this;
            final MIPlanType leftObject = ((MIPlanType) left);
            final MIPlanType rightObject = ((MIPlanType) right);
            {
                Boolean typeShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetType(), rightObject.isSetType());
                if (typeShouldBeMergedAndSet == Boolean.TRUE) {
                    MIGeometryTypeCodePropertyType lhsType;
                    lhsType = leftObject.getType();
                    MIGeometryTypeCodePropertyType rhsType;
                    rhsType = rightObject.getType();
                    MIGeometryTypeCodePropertyType mergedType = ((MIGeometryTypeCodePropertyType) strategy.merge(LocatorUtils.property(leftLocator, "type", lhsType), LocatorUtils.property(rightLocator, "type", rhsType), lhsType, rhsType, leftObject.isSetType(), rightObject.isSetType()));
                    target.setType(mergedType);
                } else {
                    if (typeShouldBeMergedAndSet == Boolean.FALSE) {
                        target.type = null;
                    }
                }
            }
            {
                Boolean statusShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetStatus(), rightObject.isSetStatus());
                if (statusShouldBeMergedAndSet == Boolean.TRUE) {
                    MDProgressCodePropertyType lhsStatus;
                    lhsStatus = leftObject.getStatus();
                    MDProgressCodePropertyType rhsStatus;
                    rhsStatus = rightObject.getStatus();
                    MDProgressCodePropertyType mergedStatus = ((MDProgressCodePropertyType) strategy.merge(LocatorUtils.property(leftLocator, "status", lhsStatus), LocatorUtils.property(rightLocator, "status", rhsStatus), lhsStatus, rhsStatus, leftObject.isSetStatus(), rightObject.isSetStatus()));
                    target.setStatus(mergedStatus);
                } else {
                    if (statusShouldBeMergedAndSet == Boolean.FALSE) {
                        target.status = null;
                    }
                }
            }
            {
                Boolean citationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetCitation(), rightObject.isSetCitation());
                if (citationShouldBeMergedAndSet == Boolean.TRUE) {
                    CICitationPropertyType lhsCitation;
                    lhsCitation = leftObject.getCitation();
                    CICitationPropertyType rhsCitation;
                    rhsCitation = rightObject.getCitation();
                    CICitationPropertyType mergedCitation = ((CICitationPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "citation", lhsCitation), LocatorUtils.property(rightLocator, "citation", rhsCitation), lhsCitation, rhsCitation, leftObject.isSetCitation(), rightObject.isSetCitation()));
                    target.setCitation(mergedCitation);
                } else {
                    if (citationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.citation = null;
                    }
                }
            }
            {
                Boolean satisfiedRequirementShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetSatisfiedRequirement(), rightObject.isSetSatisfiedRequirement());
                if (satisfiedRequirementShouldBeMergedAndSet == Boolean.TRUE) {
                    List<MIRequirementPropertyType> lhsSatisfiedRequirement;
                    lhsSatisfiedRequirement = (leftObject.isSetSatisfiedRequirement()?leftObject.getSatisfiedRequirement():null);
                    List<MIRequirementPropertyType> rhsSatisfiedRequirement;
                    rhsSatisfiedRequirement = (rightObject.isSetSatisfiedRequirement()?rightObject.getSatisfiedRequirement():null);
                    List<MIRequirementPropertyType> mergedSatisfiedRequirement = ((List<MIRequirementPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "satisfiedRequirement", lhsSatisfiedRequirement), LocatorUtils.property(rightLocator, "satisfiedRequirement", rhsSatisfiedRequirement), lhsSatisfiedRequirement, rhsSatisfiedRequirement, leftObject.isSetSatisfiedRequirement(), rightObject.isSetSatisfiedRequirement()));
                    target.unsetSatisfiedRequirement();
                    if (mergedSatisfiedRequirement!= null) {
                        List<MIRequirementPropertyType> uniqueSatisfiedRequirementl = target.getSatisfiedRequirement();
                        uniqueSatisfiedRequirementl.addAll(mergedSatisfiedRequirement);
                    }
                } else {
                    if (satisfiedRequirementShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetSatisfiedRequirement();
                    }
                }
            }
            {
                Boolean operationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetOperation(), rightObject.isSetOperation());
                if (operationShouldBeMergedAndSet == Boolean.TRUE) {
                    List<MIOperationPropertyType> lhsOperation;
                    lhsOperation = (leftObject.isSetOperation()?leftObject.getOperation():null);
                    List<MIOperationPropertyType> rhsOperation;
                    rhsOperation = (rightObject.isSetOperation()?rightObject.getOperation():null);
                    List<MIOperationPropertyType> mergedOperation = ((List<MIOperationPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "operation", lhsOperation), LocatorUtils.property(rightLocator, "operation", rhsOperation), lhsOperation, rhsOperation, leftObject.isSetOperation(), rightObject.isSetOperation()));
                    target.unsetOperation();
                    if (mergedOperation!= null) {
                        List<MIOperationPropertyType> uniqueOperationl = target.getOperation();
                        uniqueOperationl.addAll(mergedOperation);
                    }
                } else {
                    if (operationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetOperation();
                    }
                }
            }
        }
    }

    public void setSatisfiedRequirement(List<MIRequirementPropertyType> value) {
        this.satisfiedRequirement = null;
        if (value!= null) {
            List<MIRequirementPropertyType> draftl = this.getSatisfiedRequirement();
            draftl.addAll(value);
        }
    }

    public void setOperation(List<MIOperationPropertyType> value) {
        this.operation = null;
        if (value!= null) {
            List<MIOperationPropertyType> draftl = this.getOperation();
            draftl.addAll(value);
        }
    }

    public MIPlanType withType(MIGeometryTypeCodePropertyType value) {
        setType(value);
        return this;
    }

    public MIPlanType withStatus(MDProgressCodePropertyType value) {
        setStatus(value);
        return this;
    }

    public MIPlanType withCitation(CICitationPropertyType value) {
        setCitation(value);
        return this;
    }

    public MIPlanType withSatisfiedRequirement(MIRequirementPropertyType... values) {
        if (values!= null) {
            for (MIRequirementPropertyType value: values) {
                getSatisfiedRequirement().add(value);
            }
        }
        return this;
    }

    public MIPlanType withSatisfiedRequirement(Collection<MIRequirementPropertyType> values) {
        if (values!= null) {
            getSatisfiedRequirement().addAll(values);
        }
        return this;
    }

    public MIPlanType withOperation(MIOperationPropertyType... values) {
        if (values!= null) {
            for (MIOperationPropertyType value: values) {
                getOperation().add(value);
            }
        }
        return this;
    }

    public MIPlanType withOperation(Collection<MIOperationPropertyType> values) {
        if (values!= null) {
            getOperation().addAll(values);
        }
        return this;
    }

    public MIPlanType withSatisfiedRequirement(List<MIRequirementPropertyType> value) {
        setSatisfiedRequirement(value);
        return this;
    }

    public MIPlanType withOperation(List<MIOperationPropertyType> value) {
        setOperation(value);
        return this;
    }

}