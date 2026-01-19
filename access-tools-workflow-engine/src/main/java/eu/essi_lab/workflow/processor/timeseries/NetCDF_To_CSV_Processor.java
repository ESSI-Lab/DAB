package eu.essi_lab.workflow.processor.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.units.SimpleUnit;
import ucar.unidata.geoloc.Station;

public class NetCDF_To_CSV_Processor extends DataProcessor {

    private static final String NETCDF_TO_CSV_PROCESSOR_ERROR = "NETCDF_TO_CSV_PROCESSOR_ERROR";

    @Override
    public DataObject process(GSResource resource, DataObject dataObject, TargetHandler handler) throws Exception {

	FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, dataObject.getFile().getAbsolutePath(), null, null);

	try {
	    FeatureDatasetPoint fdp = (FeatureDatasetPoint) dataset;
	    StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);

	    List<VariableSimpleIF> dataVariables = dataset.getDataVariables();
	    List<VariableSimpleIF> mainVariables = new ArrayList<>();
	    for (VariableSimpleIF dataVariable : dataVariables) {
		Attribute ancillaryVariablesAttribute = dataVariable.findAttributeIgnoreCase("ancillary_variables");
		if (ancillaryVariablesAttribute != null) {
		    mainVariables.add(dataVariable);
		}
	    }

	    ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (CalendarDateRange) null);

	    VariableSimpleIF mainVariable = mainVariables.isEmpty() ? dataVariables.get(0) : mainVariables.get(0);

	    String variableLongName = null;
	    Attribute longNameAttribute = mainVariable.findAttributeIgnoreCase("long_name");
	    if (longNameAttribute != null) {
		variableLongName = longNameAttribute.getStringValue();
	    }
	    if (variableLongName == null) {
		variableLongName = mainVariable.getShortName();
	    }

	    // Extract ancillary variables
	    List<VariableSimpleIF> ancillaryVariableList = new ArrayList<>();
	    Attribute ancillaryVariablesAttribute = mainVariable.findAttributeIgnoreCase("ancillary_variables");
	    if (ancillaryVariablesAttribute != null) {
		String ancillaryVariablesString = ancillaryVariablesAttribute.getStringValue();
		if (ancillaryVariablesString != null && !ancillaryVariablesString.trim().isEmpty()) {
		    // Ancillary variables are space-separated
		    String[] ancillaryVarNames = ancillaryVariablesString.trim().split("\\s+");
		    for (String varName : ancillaryVarNames) {
			if (varName != null && !varName.trim().isEmpty()) {
			    // Find the variable in the dataset
			    for (VariableSimpleIF var : dataVariables) {
				if (var.getShortName().equals(varName.trim())) {
				    ancillaryVariableList.add(var);
				    break;
				}
			    }
			}
		    }
		}
	    }

	    String stationName = "";
	    Station station = null;

	    // Get station info from first feature if available
	    if (pfc.hasNext()) {
		PointFeature pf = pfc.next();
		StationPointFeature spf = (StationPointFeature) pf;
		StationFeature stationFeature = spf.getStation();
		stationName = stationFeature.getName();
		station = stationFeature;
		// Reset iterator by creating a new one
		pfc = fc.flatten(null, (CalendarDateRange) null);
	    } else {
		// If no data, get station from collection
		if (!fc.getStationFeatures().isEmpty()) {
		    station = fc.getStationFeatures().get(0);
		    stationName = station.getName();
		}
	    }

	    // Create CSV file
	    File csvFile = File.createTempFile(getClass().getSimpleName(), ".csv");
	    csvFile.deleteOnExit();

	    try (FileWriter writer = new FileWriter(csvFile)) {
		// Write CSV header
		StringBuilder header = new StringBuilder("time,value,station_name,parameter");
		for (VariableSimpleIF ancVar : ancillaryVariableList) {
		    String ancVarName = ancVar.getShortName();
		    Attribute ancLongNameAttr = ancVar.findAttributeIgnoreCase("long_name");
		    if (ancLongNameAttr != null) {
			ancVarName = ancLongNameAttr.getStringValue();
		    }
		    header.append(",").append(escapeCsvValue(ancVarName));
		}
		header.append("\n");
		writer.append(header.toString());

		// Write data rows
		while (pfc.hasNext()) {
		    PointFeature pf = pfc.next();
		    StationPointFeature spf = (StationPointFeature) pf;

		    if (station == null) {
			StationFeature stationFeature = spf.getStation();
			station = stationFeature;
			stationName = station.getName();
		    }

		    StructureData featureData = pf.getFeatureData();

		    Date date = new Date(pf.getObservationTimeAsCalendarDate().getMillis());
		    String timeString = ISO8601DateTimeUtils.getISO8601DateTime(date);

		    Double value = featureData.getScalarDouble(mainVariable.getShortName());
		    String valueStr = "";
		    if (value == null || value.isNaN() || value.isInfinite()) {
			valueStr = "";
		    } else {
			valueStr = value.toString();
		    }

		    List<StructureMembers.Member> members = featureData.getMembers();

		    // Escape CSV values (handle commas and quotes)
		    String escapedTime = escapeCsvValue(timeString);
		    String escapedValue = escapeCsvValue(valueStr);

		    String escapedStation = escapeCsvValue(stationName);
		    String escapedParameter = escapeCsvValue(variableLongName);

		    writer.append(escapedTime).append(",");
		    writer.append(escapedValue).append(",");
		    writer.append(escapedStation).append(",");
		    writer.append(escapedParameter);

		    // Extract and write ancillary variable values
		    for (VariableSimpleIF ancVar : ancillaryVariableList) {
			String ancVarName = ancVar.getShortName();
			String ancValueStr = "";
			
			// Try to get the value from featureData
			try {
			    // First, find the member to check its data type
			    StructureMembers.Member member = null;
			    for (StructureMembers.Member m : members) {
				if (m.getName().equals(ancVarName)) {
				    member = m;
				    break;
				}
			    }
			    
			    if (member != null && member.getDataType().isNumeric()) {
				// For numeric types, try to get as double
				try {
				    Double ancValue = featureData.getScalarDouble(ancVarName);
				    if (ancValue != null && !ancValue.isNaN() && !ancValue.isInfinite()) {
					ancValueStr = ancValue.toString();
				    }
				} catch (Exception e) {
				    // If getScalarDouble fails, try as string
				    String ancValueString = featureData.getScalarString(ancVarName);
				    if (ancValueString != null) {
					ancValueStr = ancValueString;
				    }
				}
			    } else {
				// For non-numeric types, get as string
				String ancValueString = featureData.getScalarString(ancVarName);
				if (ancValueString != null) {
				    ancValueStr = ancValueString;
				}
			    }
			} catch (Exception e) {
			    // If extraction fails, leave as empty string
			    ancValueStr = "";
			}
			
			writer.append(",").append(escapeCsvValue(ancValueStr));
		    }

		    writer.append("\n");
		}
	    }

	    DataObject ret = new DataObject();
	    ret.setFile(csvFile);
	    return ret;

	} finally {
	    if (dataset != null) {
		dataset.close();
	    }
	}
    }

    /**
     * Escapes a CSV value by wrapping it in quotes if it contains commas, quotes, or newlines
     * 
     * @param value
     *            the value to escape
     * @return the escaped value
     */
    private String escapeCsvValue(String value) {
	if (value == null) {
	    return "";
	}
	// If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
	if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
	    return "\"" + value.replace("\"", "\"\"") + "\"";
	}
	return value;
    }

    private GSException getGSException(String message) {
	return GSException.createException(//
		getClass(),//
		message,//
		ErrorInfo.ERRORTYPE_INTERNAL,//
		ErrorInfo.SEVERITY_ERROR,//
		NETCDF_TO_CSV_PROCESSOR_ERROR);

    }
}

