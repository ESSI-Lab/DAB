package eu.essi_lab.access.compliance;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.processor.BooleanCapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
import eu.essi_lab.workflow.processor.DescriptorUtils;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

/**
 * Different levels of data compliance
 * 
 * @author Fabrizio
 */
public enum DataComplianceLevel {
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: spatial, required. Temporal and other not strictly required</li>
     * <li>Resampling: spatial, required. Temporal and other not strictly required</li>
     * </ul>
     */
    GRID_BASIC_DATA_COMPLIANCE(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.GRID), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    new SubsettingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(PresenceType.ANY)), //

	    new ResamplingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(PresenceType.ANY))),
	    "GRID-B"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: spatial and temporal required. Other not strictly required</li>
     * <li>Resampling: spatial required. Temporal and other not strictly required</li>
     * </ul>
     */
    GRID_DATA_COMPLIANCE_1(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.GRID), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    new SubsettingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY)), //

	    new ResamplingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(PresenceType.ANY))),
	    "GRID-1"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: all</li>
     * <li>Resampling: spatial required. Temporal and other not strictly required</li>
     * </ul>
     */
    GRID_DATA_COMPLIANCE_2(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.GRID), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    SubsettingCapability.SPATIAL_TEMPORAL_OTHER_SUBSETTING(), //

	    new ResamplingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(PresenceType.ANY))),
	    "GRID-2"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: spatial and temporal required. Other not strictly required</li>
     * <li>Resampling: spatial and temporal required. Other not strictly required</li>
     * </ul>
     */
    GRID_DATA_COMPLIANCE_3(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.GRID), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    new SubsettingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY)), //

	    new ResamplingCapability(//
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY))),
	    "GRID-3"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: all required</li>
     * <li>Resampling: all required</li>
     * </ul>
     */
    GRID_DATA_COMPLIANCE_4(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.GRID), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    SubsettingCapability.SPATIAL_TEMPORAL_OTHER_SUBSETTING(), //
	    ResamplingCapability.SPATIAL_TEMPORAL_OTHER_RESAMPLING()), "GRID-4"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: temporal required. Spatial and other not strictly required</li>
     * <li>Resampling: not strictly required</li>
     * </ul>
     */
    TIME_SERIES_BASIC_DATA_COMPLIANCE(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.TIME_SERIES), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    new SubsettingCapability(//
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY)), //

	    ResamplingCapability.ANY_RESAMPLING()), "TS-B"),
    /**
     * Data compliance level properties and operations:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: temporal required. Spatial and other not strictly required</li>
     * <li>Resampling: temporal required. Spatial and other not strictly required</li>
     * </ul>
     */
    TIME_SERIES_DATA_COMPLIANCE_1(ProcessorCapabilities.create(//
	    CapabilityElement.anyFromDataType(DataType.TIME_SERIES), //
	    CapabilityElement.anyFromCRS(CRS.EPSG_4326()), //
	    CapabilityElement.anyFromDataFormat(DataFormat.NETCDF()), //
	    new SubsettingCapability(//
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY)), //

	    new ResamplingCapability(//
		    new BooleanCapabilityElement(PresenceType.ANY), //
		    new BooleanCapabilityElement(true), //
		    new BooleanCapabilityElement(PresenceType.ANY))),
	    "TS-1");

    private ProcessorCapabilities capabilities;
    private String name;

    private DataComplianceLevel(ProcessorCapabilities cap, String name) {

	this.capabilities = cap;
	this.name = name;
    }

    /**
     * @param type
     * @return
     * @throws IllegalArgumentException
     */
    public static List<DataComplianceLevel> getLevels(DataType type) throws IllegalArgumentException {

	switch (type) {
	case GRID:

	    return Arrays.asList(//
		    DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE, //
		    DataComplianceLevel.GRID_DATA_COMPLIANCE_1, //
		    DataComplianceLevel.GRID_DATA_COMPLIANCE_2, //
		    DataComplianceLevel.GRID_DATA_COMPLIANCE_3, //
		    DataComplianceLevel.GRID_DATA_COMPLIANCE_4); //

	case TIME_SERIES:

	    return Arrays.asList(//
		    DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE, //
		    DataComplianceLevel.TIME_SERIES_DATA_COMPLIANCE_1);

	default:
	    throw new IllegalArgumentException("Data type " + type + " not supported yet");
	}
    }

    /**
     * Returns the {@link OutputDescriptor} which describes this compliance level
     */
    public DataDescriptor getTargetDescriptor(DataDescriptor descriptor) {

	return DescriptorUtils.simulateDescriptor(descriptor, capabilities);
    }

    /**
     * @param complianceLevel
     * @return
     */
    public static DataComplianceLevel fromLabel(String complianceLevel) {

	switch (complianceLevel) {
	case "GRID-B":
	    return DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE;
	case "GRID-1":
	    return DataComplianceLevel.GRID_DATA_COMPLIANCE_1;
	case "GRID-2":
	    return DataComplianceLevel.GRID_DATA_COMPLIANCE_2;
	case "GRID-3":
	    return DataComplianceLevel.GRID_DATA_COMPLIANCE_3;
	case "GRID-4":
	    return DataComplianceLevel.GRID_DATA_COMPLIANCE_4;
	case "TS-B":
	    return DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE;
	case "TS-1":
	    return DataComplianceLevel.TIME_SERIES_DATA_COMPLIANCE_1;
	}

	throw new IllegalArgumentException("Invalid level: " + complianceLevel);
    }

    /**
     * @return the capabilities
     */
    public ProcessorCapabilities getCapabilities() {

	return capabilities;
    }

    /**
     * 
     */
    public String getLabel() {

	return name;
    }

}
