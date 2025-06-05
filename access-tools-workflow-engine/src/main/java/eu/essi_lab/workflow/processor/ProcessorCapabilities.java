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

import java.util.Objects;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

/**
 * Describes the input and output capabilities of a {@link DataProcessor}
 *
 * @author Fabrizio
 * @see DataProcessor#getInputCapabilities()
 * @see DataProcessor#getOutputCapabilities()
 */
public class ProcessorCapabilities {

    private CapabilityElement<DataType> type; // grid, time series, etc.
    private CapabilityElement<CRS> crs; // CRS identifier, such as EPSG:4326 etc.
    private CapabilityElement<DataFormat> format; // format, such as NetCDF, WaterML, etc.
    private SubsettingCapability subsettingCapability;
    private ResamplingCapability resamplingCapability;

    /**
     * @param dataType
     * @param outputCrs
     * @param outputFormat
     * @param sub
     * @param res
     * @return
     */
    public static ProcessorCapabilities create(//
	    CapabilityElement<DataType> dataType, //
	    CapabilityElement<CRS> outputCrs, //
	    CapabilityElement<DataFormat> outputFormat, //
	    SubsettingCapability sub, //
	    ResamplingCapability res) {

	ProcessorCapabilities object = new ProcessorCapabilities();
	object.setDataTypeCapability(dataType);

	if (Objects.nonNull(outputCrs)) {
	    object.setCRSCapability(outputCrs);
	}

	if (Objects.nonNull(outputFormat)) {
	    object.setDataFormatCapability(outputFormat);
	}

	object.setSubsettingCapability(sub == null ? SubsettingCapability.NO_SUBSETTING() : sub);
	object.setResamplingCapability(res == null ? ResamplingCapability.NO_RESAMPLING() : res);

	return object;
    }

    public CapabilityElement<DataType> getDataTypeCapability() {

	return type;
    }

    public void setDataTypeCapability(CapabilityElement<DataType> type) {

	this.type = type;
    }

    public SubsettingCapability getSubsettingCapability() {

	return subsettingCapability;
    }

    public void setSubsettingCapability(SubsettingCapability subsettingCapability) {

	this.subsettingCapability = subsettingCapability;
    }

    public ResamplingCapability getResamplingCapability() {

	return resamplingCapability;
    }

    public void setResamplingCapability(ResamplingCapability resamplingCapability) {

	this.resamplingCapability = resamplingCapability;
    }

    public void setCRSCapability(CapabilityElement<CRS> crs) {
	this.crs = crs;

    }

    public void setDataFormatCapability(CapabilityElement<DataFormat> format) {
	this.format = format;

    }

    public CapabilityElement<DataFormat> getDataFormatCapability() {

	return format;
    }

    public CapabilityElement<CRS> getCRSCapability() {

	return crs;
    }

    @Override
    public String toString() {

	return "(frm: " + format + ")," + //
		"(crs: " + crs + ")," + //
		"(sub: " + subsettingCapability.toString() + ")," + //
		"(res: " + resamplingCapability.toString() + ")";
    }

    @Override
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof ProcessorCapabilities))
	    return false;

	ProcessorCapabilities cap = (ProcessorCapabilities) object;

	boolean dataTypeMatch = Objects.equals(getDataTypeCapability(), cap.getDataTypeCapability());
	boolean crsMatch = Objects.equals(getCRSCapability(), cap.getCRSCapability());
	boolean formatMatch = Objects.equals(getDataFormatCapability(), cap.getDataFormatCapability());

	boolean subsettingMatch = Objects.equals(getSubsettingCapability(), cap.getSubsettingCapability());
	boolean resamplingMatch = Objects.equals(getResamplingCapability(), cap.getResamplingCapability());

	return dataTypeMatch && crsMatch && formatMatch && subsettingMatch && resamplingMatch;
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }

    public boolean accept(ProcessorCapabilities output) {

	boolean dataTypeMatch = getDataTypeCapability().accept(output.getDataTypeCapability());
	boolean crsMatch = getCRSCapability().accept(output.getCRSCapability());
	boolean formatMatch = getDataFormatCapability().accept(output.getDataFormatCapability());

	boolean subsettingMatch = getSubsettingCapability().accept(output.getSubsettingCapability());
	boolean resamplingMatch = getResamplingCapability().accept(output.getResamplingCapability());

	return dataTypeMatch && crsMatch && formatMatch && subsettingMatch && resamplingMatch;
    }

}
