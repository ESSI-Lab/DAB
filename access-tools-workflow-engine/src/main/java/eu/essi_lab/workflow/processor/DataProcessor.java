package eu.essi_lab.workflow.processor;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.builder.Workflow;

/**
 * A data processor elaborates a supplied {@link DataObject} according to its {@link DataObject#getDataDescriptor()} in
 * input a
 * {@link TargetHandler} which provides the information about the operation to execute and the target value to provide
 * 
 * @see Workflow
 * @author Fabrizio
 */
public abstract class DataProcessor {

    /**
     * Enumeration of available operations on data
     * 
     * @author Fabrizio
     */
    public enum DataOperation {

	DATA_TYPE_TRANSFORMATION, //
	DATA_FORMAT_TRANSFORMATION, //
	CRS_TRANSFORMATION, //
	SPATIAL_SUBSETTING, //
	TEMPORAL_SUBSETTING, //
	OTHER_SUBSETTING, //
	SPATIAL_RESAMPLING, //
	TEMPORAL_RESAMPLING, //
	OTHER_RESAMPLING;
    }

    private ProcessorCapabilities outputCap;
    private ProcessorCapabilities inputCap;

    public DataProcessor() {
    }

    /**
     * Processes the supplied <code>dataObject</code> according to the given <code>handler</code>
     * 
     * @param dataObject
     * @param handler
     * @return
     * @throws Exception
     */
    public abstract DataObject process(DataObject dataObject, TargetHandler handler) throws Exception;

    /**
     * @param outputCap
     */
    public void setOutputCapabilities(ProcessorCapabilities outputCap) {

	this.outputCap = outputCap;
    }

    /**
     * @return
     */
    public ProcessorCapabilities getOutputCapabilities() {

	return outputCap;
    }

    /**
     * @param inputCap
     */
    public void setInputCapabilities(ProcessorCapabilities inputCap) {

	this.inputCap = inputCap;
    }

    /**
     * @return
     */
    public ProcessorCapabilities getInputCapabilities() {

	return inputCap;
    }
}
