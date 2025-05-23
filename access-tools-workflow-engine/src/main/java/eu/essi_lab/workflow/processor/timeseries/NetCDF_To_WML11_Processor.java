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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.NetCDFAttribute;
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
import ucar.unidata.geoloc.Station;

public class NetCDF_To_WML11_Processor extends DataProcessor {

    public NetCDF_To_WML11_Processor() {
	JAXBWML.getInstance();
    }

    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MEANINGS = "flag_meanings";
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";
    public static final String FLAG_LONG_DESCRIPTIONS = "flag_long_descriptions";
    public static final String FLAG_LINKS = "flag_links";
    private static final String NETCDF_TO_WML_11_ERROR = "NETCDF_TO_WML_11_ERROR";

    @Override
    public DataObject process(GSResource resource,DataObject dataObject, TargetHandler handler) throws Exception {

	FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, dataObject.getFile().getAbsolutePath(), null, null);

	TimeSeriesResponseType trt = new TimeSeriesResponseType();

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

	// CFPointWriter.writeFeatureCollection(fdp, outFile.getAbsolutePath(), Version.netcdf3);

	StationTimeSeriesFeatureCollection fc = (StationTimeSeriesFeatureCollection) fdp.getPointFeatureCollectionList().get(0);

	// this.outputFile = File.createTempFile(OUT_PREFIX, SUFFIX);
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
	 * METADATA
	 */

	// PROCEDURE

	// OMProcessPropertyType procedure = new OMProcessPropertyType();
	// ObservationProcessType opt = new ObservationProcessType();
	// opt.setId("PROC.1");
	//
	// Attribute valueTypeAttribute =
	// mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_VALUE_TYPE.getNetCDFName());
	// if (valueTypeAttribute != null) {
	// String valueType = valueTypeAttribute.getStringValue();
	// ReferenceType referenceType = new ReferenceType();
	//
	// referenceType.setHref(valueType);
	// referenceType.setTitle(valueType);
	// opt.setProcessType(referenceType);
	// }
	//
	// procedure.setAny(JAXBWML2.getInstance().getFactory().createObservationProcess(opt));

	// OBSERVED PROPERTY

	TimeSeriesType timeSeries = new TimeSeriesType();
	trt.getTimeSeries().add(timeSeries);

	VariableInfoType variableInfo = new VariableInfoType();
	VariableCode variableCode = new VariableCode();
	variableCode.setValue(mainVariable.getShortName());
	variableInfo.getVariableCode().add(variableCode);
	variableInfo.setVariableName(variableLongName);

	timeSeries.setVariable(variableInfo);

	// FOI

	Attribute mediumAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_SAMPLE_MEDIUM.getNetCDFName());
	if (mediumAttribute != null) {
	    String medium = mediumAttribute.getStringValue();
	    if (medium != null && !medium.equals("")) {

		variableInfo.setSampleMedium(medium);
	    }
	}

	// MEASUREMENT

	UnitsType units = new UnitsType();
	variableInfo.setUnit(units);

	Attribute unitAbbreviationAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_ABBREVIATION.getNetCDFName());
	if (unitAbbreviationAttribute != null) {
	    String unitAbbreviation = unitAbbreviationAttribute.getStringValue();
	    units.setUnitAbbreviation(unitAbbreviation);
	}
	Attribute unitCodeAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_CODE.getNetCDFName());
	if (unitCodeAttribute != null) {
	    String unitCode = unitCodeAttribute.getStringValue();
	    units.setUnitCode(unitCode);

	}
	Attribute unitNameAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_NAME.getNetCDFName());
	if (unitNameAttribute != null) {
	    String unitName = unitNameAttribute.getStringValue();
	    units.setUnitName(unitName);
	}
	Attribute unitTypeAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_TYPE.getNetCDFName());
	if (unitTypeAttribute != null) {
	    String unitType = unitTypeAttribute.getStringValue();
	    units.setUnitType(unitType);
	}

	/*
	 * DATA
	 */

	String stationName = "";

	Double lat = null;
	Double lon = null;

	TsValuesSingleVariableType values = new TsValuesSingleVariableType();

	timeSeries.getValues().add(values);

	Double missingValue = null;
	Attribute missingValueAttribute = mainVariable.findAttributeIgnoreCase("missing_value");
	if (missingValueAttribute != null) {
	    int length = missingValueAttribute.getLength();
	    if (length > 0) {
		Object value = missingValueAttribute.getValue(0);
		if (value instanceof Number) {
		    Number number = (Number) value;
		    double d = number.doubleValue();
		    if (Double.isFinite(d)) {
			missingValue = d;
		    }
		}
	    }
	}
	if (missingValue == null) {
	    missingValueAttribute = mainVariable.findAttributeIgnoreCase("_FillValue");
	    if (missingValueAttribute != null) {
		int length = missingValueAttribute.getLength();
		if (length > 0) {
		    Object value = missingValueAttribute.getValue(0);
		    if (value instanceof Number) {
			Number number = (Number) value;
			double d = number.doubleValue();
			if (Double.isFinite(d)) {
			    missingValue = d;
			}
		    }
		}
	    }
	}
	if (missingValue==null) {
	    missingValue = -9999.;
	}
	variableInfo.setNoDataValue(missingValue);

	while (pfc.hasNext()) {
	    PointFeature pf = pfc.next();
	    StationPointFeature spf = (StationPointFeature) pf;

	    StationFeature station = spf.getStation();
	    stationName = station.getName();
	    StructureData featureData = pf.getFeatureData();

	    lat = station.getLatitude();
	    lon = station.getLongitude();

	    Date date = new Date( pf.getObservationTimeAsCalendarDate().getMillis());

	    Double valueDouble = featureData.getScalarDouble(mainVariable.getShortName());
	    if (valueDouble == null || !Double.isFinite(valueDouble)) {
		valueDouble = missingValue;
	    }

	    ValueSingleVariable value = new ValueSingleVariable();

	    GregorianCalendar c = new GregorianCalendar();
	    c.setTimeZone(TimeZone.getTimeZone("UTC"));
	    c.setTime(date);
	    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

	    value.setDateTimeUTC(date2);
	    if (valueDouble != null && Double.isFinite(valueDouble)) {
		value.setValue(new BigDecimal(valueDouble));
	    }
	    values.getValue().add(value);

	}

	if (lat == null || lon == null || stationName == null || stationName.equals("")) { // it could be null in case
											   // of no values
	    Station station = fc.getStationFeatures().get(0);
	    stationName = station.getName();
	    lat = station.getLatitude();
	    lon = station.getLongitude();
	}
	SiteInfoType siteInfo = new SiteInfoType();
	timeSeries.setSourceInfo(siteInfo);
	siteInfo.setSiteName(stationName);
	if (lat != 0 && lon != 0) {

	    GeoLocation geoLocation = new GeoLocation();
	    LatLonPointType geogLocation = new LatLonPointType();
	    geogLocation.setSrs("EPSG:4326");
	    geogLocation.setLatitude(lat);
	    geogLocation.setLongitude(lon);
	    geoLocation.setGeogLocation(geogLocation);
	    SiteCode siteCode = new SiteCode();
	    siteCode.setValue(stationName);
	    siteInfo.getSiteCode().add(siteCode);
	    siteInfo.setGeoLocation(geoLocation);

	}

	DataObject ret = new DataObject();

	File file = File.createTempFile(getClass().getSimpleName(), ".wml");
	file.deleteOnExit();

	if (dataset != null)
	    dataset.close();

	JAXBWML.getInstance().marshal(trt, file);
	ret.setFile(file);
	return ret;

    }

    private GSException getGSException(String message) {
	 
	return GSException.createException(//
		getClass(),//
		message,//
		ErrorInfo.ERRORTYPE_INTERNAL,//
		ErrorInfo.SEVERITY_ERROR,//
		NETCDF_TO_WML_11_ERROR);

    }
}
