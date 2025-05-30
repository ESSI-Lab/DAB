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

import net.opengis.iso19139.gco.v_20060504.AbstractObjectType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;


/**
 * Description: Comprehensive information about the procedure(s), process(es) and algorithm(s) applied in the process step - shortName: Procsg
 * 
 * <p>Java class for LE_Processing_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LE_Processing_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gco}AbstractObject_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="identifier" type="{http://www.isotc211.org/2005/gmd}MD_Identifier_PropertyType"/&gt;
 *         &lt;element name="softwareReference" type="{http://www.isotc211.org/2005/gmd}CI_Citation_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="procedureDescription" type="{http://www.isotc211.org/2005/gco}CharacterString_PropertyType" minOccurs="0"/&gt;
 *         &lt;element name="documentation" type="{http://www.isotc211.org/2005/gmd}CI_Citation_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="runTimeParameters" type="{http://www.isotc211.org/2005/gco}CharacterString_PropertyType" minOccurs="0"/&gt;
 *         &lt;element name="algorithm" type="{http://www.isotc211.org/2005/gmi}LE_Algorithm_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LE_Processing_Type", propOrder = {
    "identifier",
    "softwareReference",
    "procedureDescription",
    "documentation",
    "runTimeParameters",
    "algorithm"
})
public class LEProcessingType
    extends AbstractObjectType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    @XmlElement(required = true)
    protected MDIdentifierPropertyType identifier;
    protected List<CICitationPropertyType> softwareReference;
    protected CharacterStringPropertyType procedureDescription;
    protected List<CICitationPropertyType> documentation;
    protected CharacterStringPropertyType runTimeParameters;
    protected List<LEAlgorithmPropertyType> algorithm;

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link MDIdentifierPropertyType }
     *     
     */
    public MDIdentifierPropertyType getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link MDIdentifierPropertyType }
     *     
     */
    public void setIdentifier(MDIdentifierPropertyType value) {
        this.identifier = value;
    }

    public boolean isSetIdentifier() {
        return (this.identifier!= null);
    }

    /**
     * Gets the value of the softwareReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the softwareReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoftwareReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CICitationPropertyType }
     * 
     * 
     */
    public List<CICitationPropertyType> getSoftwareReference() {
        if (softwareReference == null) {
            softwareReference = new ArrayList<CICitationPropertyType>();
        }
        return this.softwareReference;
    }

    public boolean isSetSoftwareReference() {
        return ((this.softwareReference!= null)&&(!this.softwareReference.isEmpty()));
    }

    public void unsetSoftwareReference() {
        this.softwareReference = null;
    }

    /**
     * Gets the value of the procedureDescription property.
     * 
     * @return
     *     possible object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public CharacterStringPropertyType getProcedureDescription() {
        return procedureDescription;
    }

    /**
     * Sets the value of the procedureDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public void setProcedureDescription(CharacterStringPropertyType value) {
        this.procedureDescription = value;
    }

    public boolean isSetProcedureDescription() {
        return (this.procedureDescription!= null);
    }

    /**
     * Gets the value of the documentation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the documentation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocumentation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CICitationPropertyType }
     * 
     * 
     */
    public List<CICitationPropertyType> getDocumentation() {
        if (documentation == null) {
            documentation = new ArrayList<CICitationPropertyType>();
        }
        return this.documentation;
    }

    public boolean isSetDocumentation() {
        return ((this.documentation!= null)&&(!this.documentation.isEmpty()));
    }

    public void unsetDocumentation() {
        this.documentation = null;
    }

    /**
     * Gets the value of the runTimeParameters property.
     * 
     * @return
     *     possible object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public CharacterStringPropertyType getRunTimeParameters() {
        return runTimeParameters;
    }

    /**
     * Sets the value of the runTimeParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link CharacterStringPropertyType }
     *     
     */
    public void setRunTimeParameters(CharacterStringPropertyType value) {
        this.runTimeParameters = value;
    }

    public boolean isSetRunTimeParameters() {
        return (this.runTimeParameters!= null);
    }

    /**
     * Gets the value of the algorithm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the algorithm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlgorithm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LEAlgorithmPropertyType }
     * 
     * 
     */
    public List<LEAlgorithmPropertyType> getAlgorithm() {
        if (algorithm == null) {
            algorithm = new ArrayList<LEAlgorithmPropertyType>();
        }
        return this.algorithm;
    }

    public boolean isSetAlgorithm() {
        return ((this.algorithm!= null)&&(!this.algorithm.isEmpty()));
    }

    public void unsetAlgorithm() {
        this.algorithm = null;
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
            MDIdentifierPropertyType theIdentifier;
            theIdentifier = this.getIdentifier();
            strategy.appendField(locator, this, "identifier", buffer, theIdentifier, this.isSetIdentifier());
        }
        {
            List<CICitationPropertyType> theSoftwareReference;
            theSoftwareReference = (this.isSetSoftwareReference()?this.getSoftwareReference():null);
            strategy.appendField(locator, this, "softwareReference", buffer, theSoftwareReference, this.isSetSoftwareReference());
        }
        {
            CharacterStringPropertyType theProcedureDescription;
            theProcedureDescription = this.getProcedureDescription();
            strategy.appendField(locator, this, "procedureDescription", buffer, theProcedureDescription, this.isSetProcedureDescription());
        }
        {
            List<CICitationPropertyType> theDocumentation;
            theDocumentation = (this.isSetDocumentation()?this.getDocumentation():null);
            strategy.appendField(locator, this, "documentation", buffer, theDocumentation, this.isSetDocumentation());
        }
        {
            CharacterStringPropertyType theRunTimeParameters;
            theRunTimeParameters = this.getRunTimeParameters();
            strategy.appendField(locator, this, "runTimeParameters", buffer, theRunTimeParameters, this.isSetRunTimeParameters());
        }
        {
            List<LEAlgorithmPropertyType> theAlgorithm;
            theAlgorithm = (this.isSetAlgorithm()?this.getAlgorithm():null);
            strategy.appendField(locator, this, "algorithm", buffer, theAlgorithm, this.isSetAlgorithm());
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
        final LEProcessingType that = ((LEProcessingType) object);
        {
            MDIdentifierPropertyType lhsIdentifier;
            lhsIdentifier = this.getIdentifier();
            MDIdentifierPropertyType rhsIdentifier;
            rhsIdentifier = that.getIdentifier();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "identifier", lhsIdentifier), LocatorUtils.property(thatLocator, "identifier", rhsIdentifier), lhsIdentifier, rhsIdentifier, this.isSetIdentifier(), that.isSetIdentifier())) {
                return false;
            }
        }
        {
            List<CICitationPropertyType> lhsSoftwareReference;
            lhsSoftwareReference = (this.isSetSoftwareReference()?this.getSoftwareReference():null);
            List<CICitationPropertyType> rhsSoftwareReference;
            rhsSoftwareReference = (that.isSetSoftwareReference()?that.getSoftwareReference():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "softwareReference", lhsSoftwareReference), LocatorUtils.property(thatLocator, "softwareReference", rhsSoftwareReference), lhsSoftwareReference, rhsSoftwareReference, this.isSetSoftwareReference(), that.isSetSoftwareReference())) {
                return false;
            }
        }
        {
            CharacterStringPropertyType lhsProcedureDescription;
            lhsProcedureDescription = this.getProcedureDescription();
            CharacterStringPropertyType rhsProcedureDescription;
            rhsProcedureDescription = that.getProcedureDescription();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "procedureDescription", lhsProcedureDescription), LocatorUtils.property(thatLocator, "procedureDescription", rhsProcedureDescription), lhsProcedureDescription, rhsProcedureDescription, this.isSetProcedureDescription(), that.isSetProcedureDescription())) {
                return false;
            }
        }
        {
            List<CICitationPropertyType> lhsDocumentation;
            lhsDocumentation = (this.isSetDocumentation()?this.getDocumentation():null);
            List<CICitationPropertyType> rhsDocumentation;
            rhsDocumentation = (that.isSetDocumentation()?that.getDocumentation():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "documentation", lhsDocumentation), LocatorUtils.property(thatLocator, "documentation", rhsDocumentation), lhsDocumentation, rhsDocumentation, this.isSetDocumentation(), that.isSetDocumentation())) {
                return false;
            }
        }
        {
            CharacterStringPropertyType lhsRunTimeParameters;
            lhsRunTimeParameters = this.getRunTimeParameters();
            CharacterStringPropertyType rhsRunTimeParameters;
            rhsRunTimeParameters = that.getRunTimeParameters();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "runTimeParameters", lhsRunTimeParameters), LocatorUtils.property(thatLocator, "runTimeParameters", rhsRunTimeParameters), lhsRunTimeParameters, rhsRunTimeParameters, this.isSetRunTimeParameters(), that.isSetRunTimeParameters())) {
                return false;
            }
        }
        {
            List<LEAlgorithmPropertyType> lhsAlgorithm;
            lhsAlgorithm = (this.isSetAlgorithm()?this.getAlgorithm():null);
            List<LEAlgorithmPropertyType> rhsAlgorithm;
            rhsAlgorithm = (that.isSetAlgorithm()?that.getAlgorithm():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "algorithm", lhsAlgorithm), LocatorUtils.property(thatLocator, "algorithm", rhsAlgorithm), lhsAlgorithm, rhsAlgorithm, this.isSetAlgorithm(), that.isSetAlgorithm())) {
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
            MDIdentifierPropertyType theIdentifier;
            theIdentifier = this.getIdentifier();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "identifier", theIdentifier), currentHashCode, theIdentifier, this.isSetIdentifier());
        }
        {
            List<CICitationPropertyType> theSoftwareReference;
            theSoftwareReference = (this.isSetSoftwareReference()?this.getSoftwareReference():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "softwareReference", theSoftwareReference), currentHashCode, theSoftwareReference, this.isSetSoftwareReference());
        }
        {
            CharacterStringPropertyType theProcedureDescription;
            theProcedureDescription = this.getProcedureDescription();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "procedureDescription", theProcedureDescription), currentHashCode, theProcedureDescription, this.isSetProcedureDescription());
        }
        {
            List<CICitationPropertyType> theDocumentation;
            theDocumentation = (this.isSetDocumentation()?this.getDocumentation():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "documentation", theDocumentation), currentHashCode, theDocumentation, this.isSetDocumentation());
        }
        {
            CharacterStringPropertyType theRunTimeParameters;
            theRunTimeParameters = this.getRunTimeParameters();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "runTimeParameters", theRunTimeParameters), currentHashCode, theRunTimeParameters, this.isSetRunTimeParameters());
        }
        {
            List<LEAlgorithmPropertyType> theAlgorithm;
            theAlgorithm = (this.isSetAlgorithm()?this.getAlgorithm():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "algorithm", theAlgorithm), currentHashCode, theAlgorithm, this.isSetAlgorithm());
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
        if (draftCopy instanceof LEProcessingType) {
            final LEProcessingType copy = ((LEProcessingType) draftCopy);
            {
                Boolean identifierShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetIdentifier());
                if (identifierShouldBeCopiedAndSet == Boolean.TRUE) {
                    MDIdentifierPropertyType sourceIdentifier;
                    sourceIdentifier = this.getIdentifier();
                    MDIdentifierPropertyType copyIdentifier = ((MDIdentifierPropertyType) strategy.copy(LocatorUtils.property(locator, "identifier", sourceIdentifier), sourceIdentifier, this.isSetIdentifier()));
                    copy.setIdentifier(copyIdentifier);
                } else {
                    if (identifierShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.identifier = null;
                    }
                }
            }
            {
                Boolean softwareReferenceShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetSoftwareReference());
                if (softwareReferenceShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<CICitationPropertyType> sourceSoftwareReference;
                    sourceSoftwareReference = (this.isSetSoftwareReference()?this.getSoftwareReference():null);
                    @SuppressWarnings("unchecked")
                    List<CICitationPropertyType> copySoftwareReference = ((List<CICitationPropertyType> ) strategy.copy(LocatorUtils.property(locator, "softwareReference", sourceSoftwareReference), sourceSoftwareReference, this.isSetSoftwareReference()));
                    copy.unsetSoftwareReference();
                    if (copySoftwareReference!= null) {
                        List<CICitationPropertyType> uniqueSoftwareReferencel = copy.getSoftwareReference();
                        uniqueSoftwareReferencel.addAll(copySoftwareReference);
                    }
                } else {
                    if (softwareReferenceShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetSoftwareReference();
                    }
                }
            }
            {
                Boolean procedureDescriptionShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetProcedureDescription());
                if (procedureDescriptionShouldBeCopiedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType sourceProcedureDescription;
                    sourceProcedureDescription = this.getProcedureDescription();
                    CharacterStringPropertyType copyProcedureDescription = ((CharacterStringPropertyType) strategy.copy(LocatorUtils.property(locator, "procedureDescription", sourceProcedureDescription), sourceProcedureDescription, this.isSetProcedureDescription()));
                    copy.setProcedureDescription(copyProcedureDescription);
                } else {
                    if (procedureDescriptionShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.procedureDescription = null;
                    }
                }
            }
            {
                Boolean documentationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetDocumentation());
                if (documentationShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<CICitationPropertyType> sourceDocumentation;
                    sourceDocumentation = (this.isSetDocumentation()?this.getDocumentation():null);
                    @SuppressWarnings("unchecked")
                    List<CICitationPropertyType> copyDocumentation = ((List<CICitationPropertyType> ) strategy.copy(LocatorUtils.property(locator, "documentation", sourceDocumentation), sourceDocumentation, this.isSetDocumentation()));
                    copy.unsetDocumentation();
                    if (copyDocumentation!= null) {
                        List<CICitationPropertyType> uniqueDocumentationl = copy.getDocumentation();
                        uniqueDocumentationl.addAll(copyDocumentation);
                    }
                } else {
                    if (documentationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetDocumentation();
                    }
                }
            }
            {
                Boolean runTimeParametersShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetRunTimeParameters());
                if (runTimeParametersShouldBeCopiedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType sourceRunTimeParameters;
                    sourceRunTimeParameters = this.getRunTimeParameters();
                    CharacterStringPropertyType copyRunTimeParameters = ((CharacterStringPropertyType) strategy.copy(LocatorUtils.property(locator, "runTimeParameters", sourceRunTimeParameters), sourceRunTimeParameters, this.isSetRunTimeParameters()));
                    copy.setRunTimeParameters(copyRunTimeParameters);
                } else {
                    if (runTimeParametersShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.runTimeParameters = null;
                    }
                }
            }
            {
                Boolean algorithmShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetAlgorithm());
                if (algorithmShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<LEAlgorithmPropertyType> sourceAlgorithm;
                    sourceAlgorithm = (this.isSetAlgorithm()?this.getAlgorithm():null);
                    @SuppressWarnings("unchecked")
                    List<LEAlgorithmPropertyType> copyAlgorithm = ((List<LEAlgorithmPropertyType> ) strategy.copy(LocatorUtils.property(locator, "algorithm", sourceAlgorithm), sourceAlgorithm, this.isSetAlgorithm()));
                    copy.unsetAlgorithm();
                    if (copyAlgorithm!= null) {
                        List<LEAlgorithmPropertyType> uniqueAlgorithml = copy.getAlgorithm();
                        uniqueAlgorithml.addAll(copyAlgorithm);
                    }
                } else {
                    if (algorithmShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetAlgorithm();
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new LEProcessingType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof LEProcessingType) {
            final LEProcessingType target = this;
            final LEProcessingType leftObject = ((LEProcessingType) left);
            final LEProcessingType rightObject = ((LEProcessingType) right);
            {
                Boolean identifierShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetIdentifier(), rightObject.isSetIdentifier());
                if (identifierShouldBeMergedAndSet == Boolean.TRUE) {
                    MDIdentifierPropertyType lhsIdentifier;
                    lhsIdentifier = leftObject.getIdentifier();
                    MDIdentifierPropertyType rhsIdentifier;
                    rhsIdentifier = rightObject.getIdentifier();
                    MDIdentifierPropertyType mergedIdentifier = ((MDIdentifierPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "identifier", lhsIdentifier), LocatorUtils.property(rightLocator, "identifier", rhsIdentifier), lhsIdentifier, rhsIdentifier, leftObject.isSetIdentifier(), rightObject.isSetIdentifier()));
                    target.setIdentifier(mergedIdentifier);
                } else {
                    if (identifierShouldBeMergedAndSet == Boolean.FALSE) {
                        target.identifier = null;
                    }
                }
            }
            {
                Boolean softwareReferenceShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetSoftwareReference(), rightObject.isSetSoftwareReference());
                if (softwareReferenceShouldBeMergedAndSet == Boolean.TRUE) {
                    List<CICitationPropertyType> lhsSoftwareReference;
                    lhsSoftwareReference = (leftObject.isSetSoftwareReference()?leftObject.getSoftwareReference():null);
                    List<CICitationPropertyType> rhsSoftwareReference;
                    rhsSoftwareReference = (rightObject.isSetSoftwareReference()?rightObject.getSoftwareReference():null);
                    List<CICitationPropertyType> mergedSoftwareReference = ((List<CICitationPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "softwareReference", lhsSoftwareReference), LocatorUtils.property(rightLocator, "softwareReference", rhsSoftwareReference), lhsSoftwareReference, rhsSoftwareReference, leftObject.isSetSoftwareReference(), rightObject.isSetSoftwareReference()));
                    target.unsetSoftwareReference();
                    if (mergedSoftwareReference!= null) {
                        List<CICitationPropertyType> uniqueSoftwareReferencel = target.getSoftwareReference();
                        uniqueSoftwareReferencel.addAll(mergedSoftwareReference);
                    }
                } else {
                    if (softwareReferenceShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetSoftwareReference();
                    }
                }
            }
            {
                Boolean procedureDescriptionShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetProcedureDescription(), rightObject.isSetProcedureDescription());
                if (procedureDescriptionShouldBeMergedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType lhsProcedureDescription;
                    lhsProcedureDescription = leftObject.getProcedureDescription();
                    CharacterStringPropertyType rhsProcedureDescription;
                    rhsProcedureDescription = rightObject.getProcedureDescription();
                    CharacterStringPropertyType mergedProcedureDescription = ((CharacterStringPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "procedureDescription", lhsProcedureDescription), LocatorUtils.property(rightLocator, "procedureDescription", rhsProcedureDescription), lhsProcedureDescription, rhsProcedureDescription, leftObject.isSetProcedureDescription(), rightObject.isSetProcedureDescription()));
                    target.setProcedureDescription(mergedProcedureDescription);
                } else {
                    if (procedureDescriptionShouldBeMergedAndSet == Boolean.FALSE) {
                        target.procedureDescription = null;
                    }
                }
            }
            {
                Boolean documentationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetDocumentation(), rightObject.isSetDocumentation());
                if (documentationShouldBeMergedAndSet == Boolean.TRUE) {
                    List<CICitationPropertyType> lhsDocumentation;
                    lhsDocumentation = (leftObject.isSetDocumentation()?leftObject.getDocumentation():null);
                    List<CICitationPropertyType> rhsDocumentation;
                    rhsDocumentation = (rightObject.isSetDocumentation()?rightObject.getDocumentation():null);
                    List<CICitationPropertyType> mergedDocumentation = ((List<CICitationPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "documentation", lhsDocumentation), LocatorUtils.property(rightLocator, "documentation", rhsDocumentation), lhsDocumentation, rhsDocumentation, leftObject.isSetDocumentation(), rightObject.isSetDocumentation()));
                    target.unsetDocumentation();
                    if (mergedDocumentation!= null) {
                        List<CICitationPropertyType> uniqueDocumentationl = target.getDocumentation();
                        uniqueDocumentationl.addAll(mergedDocumentation);
                    }
                } else {
                    if (documentationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetDocumentation();
                    }
                }
            }
            {
                Boolean runTimeParametersShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetRunTimeParameters(), rightObject.isSetRunTimeParameters());
                if (runTimeParametersShouldBeMergedAndSet == Boolean.TRUE) {
                    CharacterStringPropertyType lhsRunTimeParameters;
                    lhsRunTimeParameters = leftObject.getRunTimeParameters();
                    CharacterStringPropertyType rhsRunTimeParameters;
                    rhsRunTimeParameters = rightObject.getRunTimeParameters();
                    CharacterStringPropertyType mergedRunTimeParameters = ((CharacterStringPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "runTimeParameters", lhsRunTimeParameters), LocatorUtils.property(rightLocator, "runTimeParameters", rhsRunTimeParameters), lhsRunTimeParameters, rhsRunTimeParameters, leftObject.isSetRunTimeParameters(), rightObject.isSetRunTimeParameters()));
                    target.setRunTimeParameters(mergedRunTimeParameters);
                } else {
                    if (runTimeParametersShouldBeMergedAndSet == Boolean.FALSE) {
                        target.runTimeParameters = null;
                    }
                }
            }
            {
                Boolean algorithmShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetAlgorithm(), rightObject.isSetAlgorithm());
                if (algorithmShouldBeMergedAndSet == Boolean.TRUE) {
                    List<LEAlgorithmPropertyType> lhsAlgorithm;
                    lhsAlgorithm = (leftObject.isSetAlgorithm()?leftObject.getAlgorithm():null);
                    List<LEAlgorithmPropertyType> rhsAlgorithm;
                    rhsAlgorithm = (rightObject.isSetAlgorithm()?rightObject.getAlgorithm():null);
                    List<LEAlgorithmPropertyType> mergedAlgorithm = ((List<LEAlgorithmPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "algorithm", lhsAlgorithm), LocatorUtils.property(rightLocator, "algorithm", rhsAlgorithm), lhsAlgorithm, rhsAlgorithm, leftObject.isSetAlgorithm(), rightObject.isSetAlgorithm()));
                    target.unsetAlgorithm();
                    if (mergedAlgorithm!= null) {
                        List<LEAlgorithmPropertyType> uniqueAlgorithml = target.getAlgorithm();
                        uniqueAlgorithml.addAll(mergedAlgorithm);
                    }
                } else {
                    if (algorithmShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetAlgorithm();
                    }
                }
            }
        }
    }

    public void setSoftwareReference(List<CICitationPropertyType> value) {
        this.softwareReference = null;
        if (value!= null) {
            List<CICitationPropertyType> draftl = this.getSoftwareReference();
            draftl.addAll(value);
        }
    }

    public void setDocumentation(List<CICitationPropertyType> value) {
        this.documentation = null;
        if (value!= null) {
            List<CICitationPropertyType> draftl = this.getDocumentation();
            draftl.addAll(value);
        }
    }

    public void setAlgorithm(List<LEAlgorithmPropertyType> value) {
        this.algorithm = null;
        if (value!= null) {
            List<LEAlgorithmPropertyType> draftl = this.getAlgorithm();
            draftl.addAll(value);
        }
    }

    public LEProcessingType withIdentifier(MDIdentifierPropertyType value) {
        setIdentifier(value);
        return this;
    }

    public LEProcessingType withSoftwareReference(CICitationPropertyType... values) {
        if (values!= null) {
            for (CICitationPropertyType value: values) {
                getSoftwareReference().add(value);
            }
        }
        return this;
    }

    public LEProcessingType withSoftwareReference(Collection<CICitationPropertyType> values) {
        if (values!= null) {
            getSoftwareReference().addAll(values);
        }
        return this;
    }

    public LEProcessingType withProcedureDescription(CharacterStringPropertyType value) {
        setProcedureDescription(value);
        return this;
    }

    public LEProcessingType withDocumentation(CICitationPropertyType... values) {
        if (values!= null) {
            for (CICitationPropertyType value: values) {
                getDocumentation().add(value);
            }
        }
        return this;
    }

    public LEProcessingType withDocumentation(Collection<CICitationPropertyType> values) {
        if (values!= null) {
            getDocumentation().addAll(values);
        }
        return this;
    }

    public LEProcessingType withRunTimeParameters(CharacterStringPropertyType value) {
        setRunTimeParameters(value);
        return this;
    }

    public LEProcessingType withAlgorithm(LEAlgorithmPropertyType... values) {
        if (values!= null) {
            for (LEAlgorithmPropertyType value: values) {
                getAlgorithm().add(value);
            }
        }
        return this;
    }

    public LEProcessingType withAlgorithm(Collection<LEAlgorithmPropertyType> values) {
        if (values!= null) {
            getAlgorithm().addAll(values);
        }
        return this;
    }

    public LEProcessingType withSoftwareReference(List<CICitationPropertyType> value) {
        setSoftwareReference(value);
        return this;
    }

    public LEProcessingType withDocumentation(List<CICitationPropertyType> value) {
        setDocumentation(value);
        return this;
    }

    public LEProcessingType withAlgorithm(List<LEAlgorithmPropertyType> value) {
        setAlgorithm(value);
        return this;
    }

}
