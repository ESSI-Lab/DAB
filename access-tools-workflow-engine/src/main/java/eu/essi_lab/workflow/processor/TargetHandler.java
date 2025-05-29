package eu.essi_lab.workflow.processor;

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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
import eu.essi_lab.workflow.processor.DataProcessor.DataOperation;

/**
 * Provides the information required to a {@link DataProcessor} in order to do its job
 *
 * @author Fabrizio
 */
public class TargetHandler {

    private DataDescriptor current;
    private DataDescriptor target;
    private ProcessorCapabilities capabilities;

    /**
     * @param current
     * @param target
     * @param capabilities
     */
    public TargetHandler(DataDescriptor current, DataDescriptor target, ProcessorCapabilities capabilities) {
	this.current = current;
	this.target = target;
	this.capabilities = capabilities;
    }

    /**
     * Provides the {@link ProcessorCapabilities} required to go further with the {@link Workflow}
     *
     * @param output the output capabilities of the current process
     * @param nextInput the input capabilities of the next process
     * @param currentCap the current capabilities, initially created from the data descriptor
     * @param targetCap the target capabilities, created from the data descriptor and the target descriptor
     * @return
     * @throws IllegalStateException if the {@link DataProcessor} is not suitable to executed in a {@link Workflow}
     * @see Workflow#check(DataDescriptor, DataDescriptor)
     */
    public static ProcessorCapabilities getNextCapabilities(//

	    ProcessorCapabilities output, //
	    ProcessorCapabilities nextInput, //
	    ProcessorCapabilities currentCap, //
	    ProcessorCapabilities targetCap) {

	ProcessorCapabilities out = new ProcessorCapabilities();

	// -------------------------
	//
	// Data Type
	//
	//
	{
	    TargetCapabilityFinder<DataType> finder = new TargetCapabilityFinder<>();

	    DataType nextDataType = finder.getTargetCapability(//
		    CapabilitiesElement.DATA_TYPE, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);//

	    out.setDataTypeCapability(CapabilityElement.anyFromDataType(nextDataType));
	}

	// -------------------------
	//
	// Data Format
	//
	//
	{
	    TargetCapabilityFinder<DataFormat> finder = new TargetCapabilityFinder<>();

	    DataFormat nextFormat = finder.getTargetCapability(//
		    CapabilitiesElement.DATA_FORMAT, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    out.setDataFormatCapability(CapabilityElement.anyFromDataFormat(nextFormat));
	}

	// -------------------------
	//
	// CRS
	//
	//
	{
	    TargetCapabilityFinder<CRS> finder = new TargetCapabilityFinder<>();

	    CRS nextCRS = finder.getTargetCapability(//
		    CapabilitiesElement.CRS, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    out.setCRSCapability(CapabilityElement.anyFromCRS(nextCRS));
	}

	// -------------------------
	//
	// Subsetting Capability
	//
	//
	{

	    TargetCapabilityFinder<Boolean> finder = new TargetCapabilityFinder<>();

	    Boolean nextSpatialSub = finder.getTargetCapability(//
		    CapabilitiesElement.SPATIAL_SUBSETTING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    Boolean nextTemporalSub = finder.getTargetCapability(//
		    CapabilitiesElement.TEMPORAL_SUBSETTING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    Boolean nextOtherSub = finder.getTargetCapability(//
		    CapabilitiesElement.OTHER_SUBSETTING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    SubsettingCapability subsettingCapability = new SubsettingCapability(//
		    BooleanCapabilityElement.anyFromBoolean(nextSpatialSub), //
		    BooleanCapabilityElement.anyFromBoolean(nextTemporalSub), //
		    BooleanCapabilityElement.anyFromBoolean(nextOtherSub));

	    out.setSubsettingCapability(subsettingCapability);

	}

	// -------------------------
	//
	// Resampling Capability
	//
	//
	{

	    TargetCapabilityFinder<Boolean> finder = new TargetCapabilityFinder<>();

	    Boolean nextSpatialRes = finder.getTargetCapability(//
		    CapabilitiesElement.SPATIAL_RESAMPLING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    Boolean nextTemporalRes = finder.getTargetCapability(//
		    CapabilitiesElement.TEMPORAL_RESAMPLING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    Boolean nextOtherRes = finder.getTargetCapability(//
		    CapabilitiesElement.OTHER_RESAMPLING, //
		    output, //
		    nextInput, //
		    currentCap, //
		    targetCap);

	    ResamplingCapability resamplingCapability = new ResamplingCapability(//
		    BooleanCapabilityElement.anyFromBoolean(nextSpatialRes), //
		    BooleanCapabilityElement.anyFromBoolean(nextTemporalRes), //
		    BooleanCapabilityElement.anyFromBoolean(nextOtherRes));

	    out.setResamplingCapability(resamplingCapability);

	}

	return out;
    }

    /**
     * @return
     */
    public DataType getCurrentDataType() {
	if (current == null) {
	    return null;
	}
	return current.getDataType();
    }

    /**
     * @return
     */
    public DataType getTargetDataType() {
	if (capabilities == null) {
	    return null;
	}
	return capabilities.getDataTypeCapability().getFirstValue();
    }

    /**
     * @return
     */
    public DataFormat getCurrentDataFormat() {
	if (current == null) {
	    return null;
	}
	return current.getDataFormat();
    }

    /**
     * @return
     */
    public DataFormat getTargetDataFormat() {
	if (capabilities == null) {
	    return null;
	}
	return capabilities.getDataFormatCapability().getFirstValue();
    }

    /**
     * @return
     */
    public CRS getCurrentCRS() {
	if (current == null) {
	    return null;
	}
	return current.getCRS();
    }

    /**
     * @return
     */
    public CRS getTargetCRS() {
	if (capabilities == null) {
	    return null;
	}
	return capabilities.getCRSCapability().getFirstValue();
    }

    /**
     * @return
     */
    public List<DataDimension> getCurrentSpatialDimensions() {
	if (current == null) {
	    return null;
	}
	return current.getSpatialDimensions();
    }

    /**
     * @return
     */
    public List<DataDimension> getTargetSpatialDimensions() {

	if (target == null) {
	    return null;
	}
	return target.getSpatialDimensions();
    }

    /**
     * @return
     */
    public List<DataDimension> getCurrentOtherDimensions() {
	if (current == null) {
	    return null;
	}
	return current.getOtherDimensions();
    }

    /**
     * @return
     */
    public List<DataDimension> getTargetOtherDimensions() {
	if (target == null) {
	    return null;
	}
	return target.getOtherDimensions();
    }

    /**
     * @return
     */
    public DataDimension getCurrentTemporalDimension() {
	if (current == null) {
	    return null;
	}
	return current.getTemporalDimension();
    }

    /**
     * @return
     */
    public DataDimension getTargetTemporalDimension() {
	if (target == null) {
	    return null;
	}
	return target.getTemporalDimension();
    }

    /**
     * Returns <code>true</code> if the given <code>operation</code> is required, <code>false</code> otherwise.<br>
     * This method should be
     * used by {@link DataProcessor}es which can execute more than one {@link DataOperation} in order to know which of
     * the supported
     * operations must be executed
     *
     * @param operation
     */
    public boolean toExecute(DataOperation operation) {

	switch (operation) {
	case DATA_TYPE_TRANSFORMATION:

	    DataType dataType = current.getDataType();
	    DataType targetType = capabilities.getDataTypeCapability().getFirstValue();

	    return !Objects.equals(dataType, targetType);

	case DATA_FORMAT_TRANSFORMATION:

	    DataFormat dataFormat = current.getDataFormat();
	    DataFormat targetFormat = capabilities.getDataFormatCapability().getFirstValue();

	    return !Objects.equals(dataFormat, targetFormat);

	case CRS_TRANSFORMATION:

	    CRS crs = current.getCRS();
	    CRS targetCRS = capabilities.getCRSCapability().getFirstValue();

	    return !Objects.equals(crs, targetCRS);

	case SPATIAL_SUBSETTING:

	    return capabilities.getSubsettingCapability().getSpatialSubsetting().getFirstValue();

	case TEMPORAL_SUBSETTING:

	    return capabilities.getSubsettingCapability().getTemporalSubsetting().getFirstValue();

	case OTHER_SUBSETTING:

	    return capabilities.getSubsettingCapability().getOtherSubsetting().getFirstValue();

	case SPATIAL_RESAMPLING:

	    return capabilities.getResamplingCapability().getSpatialResampling().getFirstValue();

	case TEMPORAL_RESAMPLING:

	    return capabilities.getResamplingCapability().getTemporalResampling().getFirstValue();

	case OTHER_RESAMPLING:

	    return capabilities.getResamplingCapability().getOtherResampling().getFirstValue();
	}

	return false;
    }

    /**
     * @author Fabrizio
     */
    private enum CapabilitiesElement {

	DATA_TYPE, //
	DATA_FORMAT, //
	CRS, //
	SPATIAL_SUBSETTING, //
	TEMPORAL_SUBSETTING, //
	OTHER_SUBSETTING, //
	SPATIAL_RESAMPLING, //
	TEMPORAL_RESAMPLING, //
	OTHER_RESAMPLING;
    }

    /**
     * @param <T>
     * @author Fabrizio
     */
    private static class TargetCapabilityFinder<T> {

	/**
	 * Finds the proper target capability of type <code>T</code> according to the supplied <code>element</code>
	 *
	 * @param element the {@link CapabilityElement} to find
	 * @param output the output capabilities of the current {@link DataProcessor}
	 * @param nextInput the input capabilities of the next {@link DataProcessor}
	 * @param currentCap the current capabilities, resulting from the previous calls of the {@link
	 *        TargetHandler#getNextCapabilities(ProcessorCapabilities, ProcessorCapabilities, ProcessorCapabilities, ProcessorCapabilities)}
	 *        method of from the initial {@link DataDescriptor}
	 * @param targetCap the target capabilities, resulting from the target {@link DataDescriptor}
	 * @return
	 * @throws IllegalStateException if the {@link DataProcessor} with the supplied <code>output</code> is not able
	 *         to satisfy the
	 *         requirements of the next {@link DataProcessor} in the {@link DataProcessor} which are provided by
	 *         <code>nextInput</code>
	 */
	@SuppressWarnings("unchecked")
	T getTargetCapability(//
		CapabilitiesElement element, //
		ProcessorCapabilities output, //
		ProcessorCapabilities nextInput, //
		ProcessorCapabilities currentCap, //
		ProcessorCapabilities targetCap) {

	    // ------------------------------------------------------------
	    //
	    // the values T to compare and/or return, in order to update the
	    // define the target capabilities, can be DataType, CRS, or Format or Boolean
	    //

	    // output capabilities supported by this process
	    Set<T> suppOutputs = getCapabilitiesValues(output, element);

	    // input capabilities of next process. empty list if this is the last process
	    Set<T> nextInputs = nextInput != null ? getCapabilitiesValues(nextInput, element) : Sets.newHashSet();

	    boolean isLastProcess = nextInput == null;

	    // this is a test case
	    if (suppOutputs.isEmpty() && isLastProcess) {
		return null;
	    }

	    // the current capability
	    T current = getCapability(element, currentCap);

	    // the target capability
	    T target = getCapability(element, targetCap);

	    // the presence type of the process capabilities
	    PresenceType presence = getPresence(element, output);

	    // true if the target capability is reached
	    boolean targetReached = isCompatible(current, target);

	    // ---------------------------------------------------------------------
	    //
	    // if this process has same-as on capability, the chosen next capability
	    // MUST be the current one (no transformation is possible)
	    //
	    if (presence == PresenceType.SAME_AS) {

		// this is the last process
		if (isLastProcess) {

		    // and the target is not reached, it means that this workflow is not valid
		    if (!targetReached) {

			throw new IllegalStateException("Same AS, last process, target not reached");

		    } else {

			// last process and target reached
			return current;
		    }
		}

		// this is not the last process
		else {

		    // but the next process do not supports
		    // the current capability, it means that this workflow is not valid
		    if (!isCompatible(current, nextInputs)) {

			throw new IllegalStateException("Same AS, next do not support current value");

		    } else {

			// here there is no reason to check if the target is reached, since
			// in any case no transformation is possible.
			// the next process supports the current capability
			return current;
		    }
		}
	    }

	    // ----------------------------------------------
	    //
	    // target capability not yet reached:
	    // we try to transform to the target capability
	    //
	    if (!targetReached) {

		// this process can transform the current capability to the target capability
		if (isCompatible(suppOutputs, target)) {

		    // if this is the last process of the chain, or the next process
		    // accepts the target capability, this process performs the transformation
		    // to the target capability
		    if (isLastProcess || isCompatible(target, nextInputs)) {

			return target;

		    } // 3) this is not the last process and the next process do not support the target

		} else {// this process can NOT transform the current value to the target value

		    // this is the last process, the target is NOT yet reached and this process
		    // cannot transform to the target: this workflow is not valid
		    if (isLastProcess) {

			throw new IllegalStateException("Last process, target not reached, unable to transform to target");
		    }

		    // 2) this process is not the last, and cannot transform to the target
		}

	    } else {

		// ----------------------------------------------------------------------
		//
		// target capability already reached (so it is equals to the current one)
		//

		// this is the last process, the target is reached:
		// there is nothing else to do
		if (isLastProcess) {

		    return current;

		} else {
		    // not the last process

		    // the target is already reached
		    // and accepted by the next process: the current is returned
		    if (isCompatible(current, nextInputs)) {

			return current;

		    } else {

			// 1) this is not the last process,
			// the target is already reached but not supported by the next process
		    }
		}
	    }

	    // -------------------------------------------
	    //
	    // we get here because this is NOT the last process and:
	    // 1) the target capability is already reached but not supported by the next process (*) OR
	    // 2) this process cannot transform to the target capability AND/OR
	    // 3) this process can transform to the target but the next process do not support the target
	    // (*) some other process in the chain
	    // will transform again the capability to arrive to the target

	    // if the next process do NOT SUPPORT the current capability
	    if (!isCompatible(current, nextInputs)) {

		// maybe the next process supports at least one capability that can be provided
		// by this process, so we choose a random one
		List<Object> nextInputsClone = Lists.newArrayList(Arrays.asList(nextInputs.toArray()));
		nextInputsClone.retainAll(suppOutputs);

		// this process cannot provide a capability suitable for the
		// next process, so this process is not valid
		if (nextInputsClone.isEmpty()) {

		    throw new IllegalStateException("Unable to satisfy next process capabilities");
		}

		// we choose one of the capability supported by the next process
		return (T) nextInputsClone.get(0);

	    } else {
		// the next process supports the current capability, so
		// this process makes no change to the current capability
		// and returns it
		return current;
	    }
	}

	private boolean isCompatible(Set<T> currents, T target) {
	    for (T current : currents) {
		boolean compatible = isCompatible(current, target);
		if (compatible) {
		    return true;
		}
	    }
	    return false;
	}

	private boolean isCompatible(T current, Set<T> targets) {
	    for (T target : targets) {
		boolean compatible = isCompatible(current, target);
		if (compatible) {
		    return true;
		}
	    }
	    return false;
	}

	private boolean isCompatible(T current, T target) {
	    boolean compatible = Objects.equals(current, target);
	    if (compatible) {
		return true;
	    } else if (current instanceof DataFormat) {
		DataFormat currentDataFormat = (DataFormat) current;
		DataFormat targetDataFormat = (DataFormat) target;
		if (currentDataFormat.isSubTypeOf(targetDataFormat)) {
		    return true;
		}
	    }
	    return false;

	}

	private PresenceType getPresence(CapabilitiesElement element, ProcessorCapabilities cap) {

	    switch (element) {
	    case DATA_TYPE:

		return cap.getDataTypeCapability().getPresence();
	    case DATA_FORMAT:

		return cap.getDataFormatCapability().getPresence();
	    case CRS:

		return cap.getCRSCapability().getPresence();

	    case SPATIAL_SUBSETTING:

		return cap.getSubsettingCapability().getSpatialSubsetting().getPresence();

	    case SPATIAL_RESAMPLING:

		return cap.getResamplingCapability().getSpatialResampling().getPresence();

	    case TEMPORAL_SUBSETTING:

		return cap.getSubsettingCapability().getTemporalSubsetting().getPresence();

	    case TEMPORAL_RESAMPLING:

		return cap.getResamplingCapability().getTemporalResampling().getPresence();

	    case OTHER_SUBSETTING:

		return cap.getSubsettingCapability().getOtherSubsetting().getPresence();

	    case OTHER_RESAMPLING:

		return cap.getResamplingCapability().getOtherResampling().getPresence();
	    }

	    return null;
	}

	@SuppressWarnings("unchecked")
	private T getCapability(CapabilitiesElement element, ProcessorCapabilities cap) {

	    switch (element) {
	    case DATA_TYPE:

		return (T) cap.getDataTypeCapability().getFirstValue();

	    case DATA_FORMAT:

		return (T) cap.getDataFormatCapability().getFirstValue();
	    case CRS:

		return (T) cap.getCRSCapability().getFirstValue();

	    case SPATIAL_SUBSETTING:

		return (T) cap.getSubsettingCapability().getSpatialSubsetting().getFirstValue();

	    case SPATIAL_RESAMPLING:

		return (T) cap.getResamplingCapability().getSpatialResampling().getFirstValue();

	    case OTHER_SUBSETTING:

		return (T) cap.getSubsettingCapability().getOtherSubsetting().getFirstValue();

	    case OTHER_RESAMPLING:

		return (T) cap.getResamplingCapability().getOtherResampling().getFirstValue();

	    case TEMPORAL_SUBSETTING:

		return (T) cap.getSubsettingCapability().getTemporalSubsetting().getFirstValue();

	    case TEMPORAL_RESAMPLING:

		return (T) cap.getResamplingCapability().getTemporalResampling().getFirstValue();
	    }

	    return null;
	}

	@SuppressWarnings("unchecked")
	private Set<T> getCapabilitiesValues(ProcessorCapabilities cap, CapabilitiesElement element) {
	    switch (element) {
	    case DATA_TYPE:

		return cap.getDataTypeCapability().getValues().stream().map(o -> (T) o).collect(Collectors.toSet());

	    case DATA_FORMAT:

		return cap.getDataFormatCapability().getValues().stream().map(o -> (T) o).collect(Collectors.toSet());

	    case CRS:

		return cap.getCRSCapability().getValues().stream().map(o -> (T) o).collect(Collectors.toSet());

	    case SPATIAL_SUBSETTING:

		return cap.getSubsettingCapability().getSpatialSubsetting().getValues().stream().map(o -> (T) o)
			.collect(Collectors.toSet());

	    case SPATIAL_RESAMPLING:

		return cap.getResamplingCapability().getSpatialResampling().getValues().stream().map(o -> (T) o)
			.collect(Collectors.toSet());

	    case TEMPORAL_SUBSETTING:

		return cap.getSubsettingCapability().getTemporalSubsetting().getValues().stream().map(o -> (T) o)
			.collect(Collectors.toSet());

	    case TEMPORAL_RESAMPLING:

		return cap.getResamplingCapability().getTemporalResampling().getValues().stream().map(o -> (T) o)
			.collect(Collectors.toSet());

	    case OTHER_SUBSETTING:

		return cap.getSubsettingCapability().getOtherSubsetting().getValues().stream().map(o -> (T) o).collect(Collectors.toSet());

	    case OTHER_RESAMPLING:
	    default:
		return cap.getResamplingCapability().getOtherResampling().getValues().stream().map(o -> (T) o).collect(Collectors.toSet());
	    }

	}
    }

}
