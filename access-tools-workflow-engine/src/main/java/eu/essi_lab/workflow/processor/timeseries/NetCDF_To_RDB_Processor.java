package eu.essi_lab.workflow.processor.timeseries;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.ma2.StructureData;
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

public class NetCDF_To_RDB_Processor extends DataProcessor {

    public NetCDF_To_RDB_Processor() {

    }

    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MEANINGS = "flag_meanings";
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";
    public static final String FLAG_LONG_DESCRIPTIONS = "flag_long_descriptions";
    public static final String FLAG_LINKS = "flag_links";
    private static final String NETCDF_TO_RDB_ERROR = "NETCDF_TO_RDB_ERROR";

    @Override
    public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {

	File file = File.createTempFile(getClass().getSimpleName(), ".rdb");
	file.deleteOnExit();

	FileOutputStream fos = new FileOutputStream(file);
	OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
	BufferedWriter writer = new BufferedWriter(osw);

	FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, dataObject.getFile().getAbsolutePath(), null, null);

	List<Variable> variables = dataset.getNetcdfFile().getVariables();
	Variable timeVariable = null;
	for (Variable variable : variables) {
	    Attribute units = variable.findAttribute("units");
	    if (units != null) {
		if (SimpleUnit.isDateUnit(units.getStringValue())) {
		    timeVariable = variable;
		}
	    }
	}
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

	List<Variable> extraVariables = fc.getExtraVariables();
	ucar.nc2.ft.PointFeatureCollection pfc = fc.flatten(null, (CalendarDateRange) null); // LOOK

	VariableSimpleIF mainVariable = mainVariables.isEmpty() ? dataVariables.get(0) : mainVariables.get(0);

	String variableLongName = null;
	Attribute longNameAttribute = mainVariable.findAttributeIgnoreCase("long_name");
	if (longNameAttribute != null) {
	    variableLongName = longNameAttribute.getStringValue();
	}
	if (variableLongName == null) {
	    variableLongName = mainVariable.getShortName();
	}

	/*
	 * DATA
	 */
	Date t1 = null;
	Date t2 = null;
	String stationName = null;

	Double lat = null;
	Double lon = null;
	while (pfc.hasNext()) {
	    PointFeature pf = pfc.next();
	    StationPointFeature spf = (StationPointFeature) pf;

	    StationFeature station = spf.getStation();
	    if (stationName == null) {
		stationName = station.getName();
		writer.write("# ---------------------------------- WARNING ----------------------------------------\n");
		writer.write("# Data has been obtained from 3rd parties by the CNR-IIA Discovery and Access broker\n");
		writer.write("# \n");
		writer.write("# Data for the following 1 site(s) are contained in this file\n");
		writer.write("#    " + stationName + "\n");
		writer.write("# -----------------------------------------------------------------------------------\n");
		writer.write("# \n");
		writer.write("agency_cd\tsite_no\tdatetime\ttz_cd\tPARAMETER_CODE \n");
	    }
	    StructureData featureData = pf.getFeatureData();

	    lat = station.getLatitude();
	    lon = station.getLongitude();

	    Date date = new Date(pf.getObservationTimeAsCalendarDate().getMillis());
	    if (t1 == null) {
		t1 = date;
	    }
	    if (t2 == null) {
		t2 = date;
	    }
	    if (date.before(t1)) {
		t1 = date;
	    }
	    if (date.after(t2)) {
		t2 = date;
	    }
	    String timeString = ISO8601DateTimeUtils.getISO8601DateTime(date).replace("T", " ").replace("Z", "");
	    timeString = timeString.substring(0, timeString.lastIndexOf(':')); // to remove the seconds
	    Double value = featureData.getScalarDouble(mainVariable.getShortName());

	    if (value == null || value.isNaN() || value.isInfinite()) {
		// skip
		writer.write("AGENCY\tSITE_CODE\t" + timeString + "\tGMT\t" +  " \n");

	    }else {
		writer.write("AGENCY\tSITE_CODE\t" + timeString + "\tGMT\t" + value + " \n");
	    }

	    
	}

	DataObject ret = new DataObject();

	if (dataset != null)
	    dataset.close();

	writer.close();
	osw.close();
	fos.close();

	ret.setFile(file);
	return ret;

    }

    private GSException getGSException(String message) {
	
	return GSException.createException(//
		getClass(),//
		message,//
		ErrorInfo.ERRORTYPE_INTERNAL,//
		ErrorInfo.SEVERITY_ERROR,//
		NETCDF_TO_RDB_ERROR);
 
    }
}
