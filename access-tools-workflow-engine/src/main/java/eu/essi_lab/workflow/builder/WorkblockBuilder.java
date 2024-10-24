package eu.essi_lab.workflow.builder;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.processor.BooleanCapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

/**
 * @author Fabrizio
 */
public abstract class WorkblockBuilder {

    private CapabilityElement<DataType> inputType;
    private CapabilityElement<DataType> outputType;

    private CapabilityElement<CRS> inputCRS;
    private CapabilityElement<DataFormat> inputFormat;

    private SubsettingCapability inputSubsetting;
    private SubsettingCapability outputSubsetting;

    private ResamplingCapability inputResampling;
    private ResamplingCapability outputResampling;

    private CapabilityElement<CRS> outputCRS;
    private CapabilityElement<DataFormat> outputFormat;

    protected WorkblockBuilder() {
    }

    /**
     * Builds a {@link Workblock}s family all sharing the same {@link DataProcessor} and a preference of
     * {@link Workblock#MINIMUM_PREFERENCE}
     * 
     * @see Workblock#getPreference()
     */
    public Workblock build() {

	return build(Workblock.MINIMUM_PREFERENCE);
    }

    /**
     * Builds a {@link Workblock}s family all sharing the same {@link DataProcessor} and a preference of
     * {@link Workblock#MINIMUM_PREFERENCE}
     * 
     * @param preference
     */
    public Workblock build(int preference) {

	init();

	if (outputCRS == null) {
	    throw new IllegalArgumentException("Output CRS not set");
	}

	if (outputFormat == null) {
	    throw new IllegalArgumentException("Output Format null");
	}

	if (inputSubsetting == null) {
	    throw new IllegalArgumentException("Input subsetting can't be null");
	}

	if (inputResampling == null) {
	    throw new IllegalArgumentException("Input resampling can't be null");
	}

	if (outputSubsetting == null) {
	    throw new IllegalArgumentException("Output subsetting can't be null");
	}

	if (outputResampling == null) {
	    throw new IllegalArgumentException("Output resampling can't be null");
	}

	ProcessorCapabilities input = ProcessorCapabilities.create(//
		inputType, //
		inputCRS, //
		inputFormat, //
		new SubsettingCapability(//
			new BooleanCapabilityElement(inputSubsetting.getSpatialSubsetting()), //
			new BooleanCapabilityElement(inputSubsetting.getTemporalSubsetting()), //
			new BooleanCapabilityElement(inputSubsetting.getOtherSubsetting())), //

		new ResamplingCapability(//
			new BooleanCapabilityElement(inputResampling.getSpatialResampling()),
			new BooleanCapabilityElement(inputResampling.getTemporalResampling()),
			new BooleanCapabilityElement(inputResampling.getOtherResampling())) //
	);

	ProcessorCapabilities output = ProcessorCapabilities.create(//
		outputType, //
		outputCRS, //
		outputFormat, //
		new SubsettingCapability(//
			new BooleanCapabilityElement(outputSubsetting.getSpatialSubsetting()), //
			new BooleanCapabilityElement(outputSubsetting.getTemporalSubsetting()), //
			new BooleanCapabilityElement(outputSubsetting.getOtherSubsetting())), //

		new ResamplingCapability(//
			new BooleanCapabilityElement(outputResampling.getSpatialResampling()),
			new BooleanCapabilityElement(outputResampling.getTemporalResampling()),
			new BooleanCapabilityElement(outputResampling.getOtherResampling())) //
	);

	DataProcessor processor = createProcessor();
	processor.setInputCapabilities(input);
	processor.setOutputCapabilities(output);

	Workblock block = new Workblock(processor, this);
	block.setPreference(preference);

	return block;
    }

    /**
     * Initialize a {@link Workblock} family all sharing the same {@link DataProcessor} instance
     */
    protected abstract void init();

    /**
     * @return
     */
    protected abstract DataProcessor createProcessor();

    protected void setInputType(CapabilityElement<DataType> type) {

	this.inputType = type;
    }

    protected void setOutputType(CapabilityElement<DataType> outputType) {

	this.outputType = outputType;
    }

    protected void setCRS(CapabilityElement<CRS> input, CapabilityElement<CRS> output) {

	checkPresence(input.getPresence(), output.getPresence());

	inputCRS = input;
	outputCRS = output;
    }

    protected void setFormat(CapabilityElement<DataFormat> input, CapabilityElement<DataFormat> output) {

	checkPresence(input.getPresence(), output.getPresence());

	inputFormat = input;
	outputFormat = output;
    }

    protected void setSubsetting(SubsettingCapability inputSubsetting, SubsettingCapability outputSubsetting) {

	checkPresence(inputSubsetting.getSpatialSubsetting().getPresence(), outputSubsetting.getSpatialSubsetting().getPresence());
	checkPresence(inputSubsetting.getTemporalSubsetting().getPresence(), outputSubsetting.getTemporalSubsetting().getPresence());
	checkPresence(inputSubsetting.getOtherSubsetting().getPresence(), outputSubsetting.getOtherSubsetting().getPresence());

	this.inputSubsetting = inputSubsetting;
	this.outputSubsetting = outputSubsetting;
    }

    protected void setResampling(ResamplingCapability inputResampling, ResamplingCapability outputResampling) {

	checkPresence(inputResampling.getSpatialResampling().getPresence(), outputResampling.getSpatialResampling().getPresence());
	checkPresence(inputResampling.getTemporalResampling().getPresence(), outputResampling.getTemporalResampling().getPresence());
	checkPresence(inputResampling.getOtherResampling().getPresence(), outputResampling.getOtherResampling().getPresence());

	this.inputResampling = inputResampling;
	this.outputResampling = outputResampling;
    }

    protected void checkPresence(PresenceType presence, PresenceType outputPresence) {
	switch (presence) {
	case ANY:
	    switch (outputPresence) {
	    case ANY:
		break;
	    case SAME_AS:
		throw new IllegalArgumentException("Unsupported combination");
	    }
	    break;
	case SAME_AS:
	    switch (outputPresence) {
	    // O.K. same as is supported only with same as
	    case SAME_AS:
		break;
	    case ANY:
		throw new IllegalArgumentException("Unsupported combination");
	    }
	    break;

	default:
	    throw new IllegalArgumentException("Unsupported combination");
	}
    }
}
