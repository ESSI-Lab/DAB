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

import net.opengis.iso19139.gmd.v_20060504.LIProcessStepType;


/**
 * Description: Information about an event or transformation in the life of the dataset including details of the algorithm and software used for processing - FGDC:  - shortName: DetailProcStep
 * 
 * <p>Java class for LE_ProcessStep_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LE_ProcessStep_Type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.isotc211.org/2005/gmd}LI_ProcessStep_Type"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="output" type="{http://www.isotc211.org/2005/gmi}LE_Source_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="report" type="{http://www.isotc211.org/2005/gmi}LE_ProcessStepReport_PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="processingInformation" type="{http://www.isotc211.org/2005/gmi}LE_Processing_PropertyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LE_ProcessStep_Type", propOrder = {
    "output",
    "report",
    "processingInformation"
})
public class LEProcessStepType
    extends LIProcessStepType
    implements Cloneable, CopyTo2, Equals2, HashCode2, MergeFrom2, ToString2
{

    protected List<LESourcePropertyType> output;
    protected List<LEProcessStepReportPropertyType> report;
    protected LEProcessingPropertyType processingInformation;

    /**
     * Gets the value of the output property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the output property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutput().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LESourcePropertyType }
     * 
     * 
     */
    public List<LESourcePropertyType> getOutput() {
        if (output == null) {
            output = new ArrayList<LESourcePropertyType>();
        }
        return this.output;
    }

    public boolean isSetOutput() {
        return ((this.output!= null)&&(!this.output.isEmpty()));
    }

    public void unsetOutput() {
        this.output = null;
    }

    /**
     * Gets the value of the report property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the report property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReport().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LEProcessStepReportPropertyType }
     * 
     * 
     */
    public List<LEProcessStepReportPropertyType> getReport() {
        if (report == null) {
            report = new ArrayList<LEProcessStepReportPropertyType>();
        }
        return this.report;
    }

    public boolean isSetReport() {
        return ((this.report!= null)&&(!this.report.isEmpty()));
    }

    public void unsetReport() {
        this.report = null;
    }

    /**
     * Gets the value of the processingInformation property.
     * 
     * @return
     *     possible object is
     *     {@link LEProcessingPropertyType }
     *     
     */
    public LEProcessingPropertyType getProcessingInformation() {
        return processingInformation;
    }

    /**
     * Sets the value of the processingInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LEProcessingPropertyType }
     *     
     */
    public void setProcessingInformation(LEProcessingPropertyType value) {
        this.processingInformation = value;
    }

    public boolean isSetProcessingInformation() {
        return (this.processingInformation!= null);
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
            List<LESourcePropertyType> theOutput;
            theOutput = (this.isSetOutput()?this.getOutput():null);
            strategy.appendField(locator, this, "output", buffer, theOutput, this.isSetOutput());
        }
        {
            List<LEProcessStepReportPropertyType> theReport;
            theReport = (this.isSetReport()?this.getReport():null);
            strategy.appendField(locator, this, "report", buffer, theReport, this.isSetReport());
        }
        {
            LEProcessingPropertyType theProcessingInformation;
            theProcessingInformation = this.getProcessingInformation();
            strategy.appendField(locator, this, "processingInformation", buffer, theProcessingInformation, this.isSetProcessingInformation());
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
        final LEProcessStepType that = ((LEProcessStepType) object);
        {
            List<LESourcePropertyType> lhsOutput;
            lhsOutput = (this.isSetOutput()?this.getOutput():null);
            List<LESourcePropertyType> rhsOutput;
            rhsOutput = (that.isSetOutput()?that.getOutput():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "output", lhsOutput), LocatorUtils.property(thatLocator, "output", rhsOutput), lhsOutput, rhsOutput, this.isSetOutput(), that.isSetOutput())) {
                return false;
            }
        }
        {
            List<LEProcessStepReportPropertyType> lhsReport;
            lhsReport = (this.isSetReport()?this.getReport():null);
            List<LEProcessStepReportPropertyType> rhsReport;
            rhsReport = (that.isSetReport()?that.getReport():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "report", lhsReport), LocatorUtils.property(thatLocator, "report", rhsReport), lhsReport, rhsReport, this.isSetReport(), that.isSetReport())) {
                return false;
            }
        }
        {
            LEProcessingPropertyType lhsProcessingInformation;
            lhsProcessingInformation = this.getProcessingInformation();
            LEProcessingPropertyType rhsProcessingInformation;
            rhsProcessingInformation = that.getProcessingInformation();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "processingInformation", lhsProcessingInformation), LocatorUtils.property(thatLocator, "processingInformation", rhsProcessingInformation), lhsProcessingInformation, rhsProcessingInformation, this.isSetProcessingInformation(), that.isSetProcessingInformation())) {
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
            List<LESourcePropertyType> theOutput;
            theOutput = (this.isSetOutput()?this.getOutput():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "output", theOutput), currentHashCode, theOutput, this.isSetOutput());
        }
        {
            List<LEProcessStepReportPropertyType> theReport;
            theReport = (this.isSetReport()?this.getReport():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "report", theReport), currentHashCode, theReport, this.isSetReport());
        }
        {
            LEProcessingPropertyType theProcessingInformation;
            theProcessingInformation = this.getProcessingInformation();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "processingInformation", theProcessingInformation), currentHashCode, theProcessingInformation, this.isSetProcessingInformation());
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
        if (draftCopy instanceof LEProcessStepType) {
            final LEProcessStepType copy = ((LEProcessStepType) draftCopy);
            {
                Boolean outputShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetOutput());
                if (outputShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<LESourcePropertyType> sourceOutput;
                    sourceOutput = (this.isSetOutput()?this.getOutput():null);
                    @SuppressWarnings("unchecked")
                    List<LESourcePropertyType> copyOutput = ((List<LESourcePropertyType> ) strategy.copy(LocatorUtils.property(locator, "output", sourceOutput), sourceOutput, this.isSetOutput()));
                    copy.unsetOutput();
                    if (copyOutput!= null) {
                        List<LESourcePropertyType> uniqueOutputl = copy.getOutput();
                        uniqueOutputl.addAll(copyOutput);
                    }
                } else {
                    if (outputShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetOutput();
                    }
                }
            }
            {
                Boolean reportShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetReport());
                if (reportShouldBeCopiedAndSet == Boolean.TRUE) {
                    List<LEProcessStepReportPropertyType> sourceReport;
                    sourceReport = (this.isSetReport()?this.getReport():null);
                    @SuppressWarnings("unchecked")
                    List<LEProcessStepReportPropertyType> copyReport = ((List<LEProcessStepReportPropertyType> ) strategy.copy(LocatorUtils.property(locator, "report", sourceReport), sourceReport, this.isSetReport()));
                    copy.unsetReport();
                    if (copyReport!= null) {
                        List<LEProcessStepReportPropertyType> uniqueReportl = copy.getReport();
                        uniqueReportl.addAll(copyReport);
                    }
                } else {
                    if (reportShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.unsetReport();
                    }
                }
            }
            {
                Boolean processingInformationShouldBeCopiedAndSet = strategy.shouldBeCopiedAndSet(locator, this.isSetProcessingInformation());
                if (processingInformationShouldBeCopiedAndSet == Boolean.TRUE) {
                    LEProcessingPropertyType sourceProcessingInformation;
                    sourceProcessingInformation = this.getProcessingInformation();
                    LEProcessingPropertyType copyProcessingInformation = ((LEProcessingPropertyType) strategy.copy(LocatorUtils.property(locator, "processingInformation", sourceProcessingInformation), sourceProcessingInformation, this.isSetProcessingInformation()));
                    copy.setProcessingInformation(copyProcessingInformation);
                } else {
                    if (processingInformationShouldBeCopiedAndSet == Boolean.FALSE) {
                        copy.processingInformation = null;
                    }
                }
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new LEProcessStepType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy2 strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy2 strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof LEProcessStepType) {
            final LEProcessStepType target = this;
            final LEProcessStepType leftObject = ((LEProcessStepType) left);
            final LEProcessStepType rightObject = ((LEProcessStepType) right);
            {
                Boolean outputShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetOutput(), rightObject.isSetOutput());
                if (outputShouldBeMergedAndSet == Boolean.TRUE) {
                    List<LESourcePropertyType> lhsOutput;
                    lhsOutput = (leftObject.isSetOutput()?leftObject.getOutput():null);
                    List<LESourcePropertyType> rhsOutput;
                    rhsOutput = (rightObject.isSetOutput()?rightObject.getOutput():null);
                    List<LESourcePropertyType> mergedOutput = ((List<LESourcePropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "output", lhsOutput), LocatorUtils.property(rightLocator, "output", rhsOutput), lhsOutput, rhsOutput, leftObject.isSetOutput(), rightObject.isSetOutput()));
                    target.unsetOutput();
                    if (mergedOutput!= null) {
                        List<LESourcePropertyType> uniqueOutputl = target.getOutput();
                        uniqueOutputl.addAll(mergedOutput);
                    }
                } else {
                    if (outputShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetOutput();
                    }
                }
            }
            {
                Boolean reportShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetReport(), rightObject.isSetReport());
                if (reportShouldBeMergedAndSet == Boolean.TRUE) {
                    List<LEProcessStepReportPropertyType> lhsReport;
                    lhsReport = (leftObject.isSetReport()?leftObject.getReport():null);
                    List<LEProcessStepReportPropertyType> rhsReport;
                    rhsReport = (rightObject.isSetReport()?rightObject.getReport():null);
                    List<LEProcessStepReportPropertyType> mergedReport = ((List<LEProcessStepReportPropertyType> ) strategy.merge(LocatorUtils.property(leftLocator, "report", lhsReport), LocatorUtils.property(rightLocator, "report", rhsReport), lhsReport, rhsReport, leftObject.isSetReport(), rightObject.isSetReport()));
                    target.unsetReport();
                    if (mergedReport!= null) {
                        List<LEProcessStepReportPropertyType> uniqueReportl = target.getReport();
                        uniqueReportl.addAll(mergedReport);
                    }
                } else {
                    if (reportShouldBeMergedAndSet == Boolean.FALSE) {
                        target.unsetReport();
                    }
                }
            }
            {
                Boolean processingInformationShouldBeMergedAndSet = strategy.shouldBeMergedAndSet(leftLocator, rightLocator, leftObject.isSetProcessingInformation(), rightObject.isSetProcessingInformation());
                if (processingInformationShouldBeMergedAndSet == Boolean.TRUE) {
                    LEProcessingPropertyType lhsProcessingInformation;
                    lhsProcessingInformation = leftObject.getProcessingInformation();
                    LEProcessingPropertyType rhsProcessingInformation;
                    rhsProcessingInformation = rightObject.getProcessingInformation();
                    LEProcessingPropertyType mergedProcessingInformation = ((LEProcessingPropertyType) strategy.merge(LocatorUtils.property(leftLocator, "processingInformation", lhsProcessingInformation), LocatorUtils.property(rightLocator, "processingInformation", rhsProcessingInformation), lhsProcessingInformation, rhsProcessingInformation, leftObject.isSetProcessingInformation(), rightObject.isSetProcessingInformation()));
                    target.setProcessingInformation(mergedProcessingInformation);
                } else {
                    if (processingInformationShouldBeMergedAndSet == Boolean.FALSE) {
                        target.processingInformation = null;
                    }
                }
            }
        }
    }

    public void setOutput(List<LESourcePropertyType> value) {
        this.output = null;
        if (value!= null) {
            List<LESourcePropertyType> draftl = this.getOutput();
            draftl.addAll(value);
        }
    }

    public void setReport(List<LEProcessStepReportPropertyType> value) {
        this.report = null;
        if (value!= null) {
            List<LEProcessStepReportPropertyType> draftl = this.getReport();
            draftl.addAll(value);
        }
    }

    public LEProcessStepType withOutput(LESourcePropertyType... values) {
        if (values!= null) {
            for (LESourcePropertyType value: values) {
                getOutput().add(value);
            }
        }
        return this;
    }

    public LEProcessStepType withOutput(Collection<LESourcePropertyType> values) {
        if (values!= null) {
            getOutput().addAll(values);
        }
        return this;
    }

    public LEProcessStepType withReport(LEProcessStepReportPropertyType... values) {
        if (values!= null) {
            for (LEProcessStepReportPropertyType value: values) {
                getReport().add(value);
            }
        }
        return this;
    }

    public LEProcessStepType withReport(Collection<LEProcessStepReportPropertyType> values) {
        if (values!= null) {
            getReport().addAll(values);
        }
        return this;
    }

    public LEProcessStepType withProcessingInformation(LEProcessingPropertyType value) {
        setProcessingInformation(value);
        return this;
    }

    public LEProcessStepType withOutput(List<LESourcePropertyType> value) {
        setOutput(value);
        return this;
    }

    public LEProcessStepType withReport(List<LEProcessStepReportPropertyType> value) {
        setReport(value);
        return this;
    }

}