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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.cuahsi.waterml._1.CensorCodeType;
import org.cuahsi.waterml._1.GeogLocationType;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.MethodType;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.PropertyType;
import org.cuahsi.waterml._1.QualityControlLevelType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo.DefaultTimeZone;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.SourceType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.TimeScale;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.joda.time.DateTimeZone;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.NetCDFAttribute;
import eu.essi_lab.netcdf.timeseries.H4SingleTimeSeriesWriter;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.timeseries.SimpleStation;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.ma2.DataType;

public class WML11_To_NetCDF_Processor extends DataProcessor {

    private ObjectFactory factory;

    public WML11_To_NetCDF_Processor() {
	this.factory = new ObjectFactory();
    }

    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MEANINGS = "flag_meanings";
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";
    public static final String FLAG_LONG_DESCRIPTIONS = "flag_long_descriptions";
    public static final String FLAG_LINKS = "flag_links";
    private static final String WML_11_TO_NETCDF_ERROR = "WML_11_TO_NETCDF_ERROR";

    public static Double TOL = Math.pow(10, -8);

    @Override
    public DataObject process(GSResource resource,DataObject dataObject, TargetHandler handler) throws Exception {

	InputStream stream = new FileInputStream(dataObject.getFile());
	InputStream result = convert(stream);
	stream.close();
	DataObject ret = new DataObject();
	ret.setFileFromStream(result, "WML11_To_NetCDF3_Processor");
	if (result != null)
	    result.close();
	return ret;
    }

    public InputStream convert(InputStream timeSeriesStream) throws GSException {

	TimeSeriesResponseType trt;
	try {
	    trt = JAXBWML.getInstance().parseTimeSeries(timeSeriesStream);
	    timeSeriesStream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw getGSException("Error unmarshalling WML 1.1");
	}
	try {

	    List<TimeSeriesType> series = trt.getTimeSeries();
	    if (series.size() == 0) {
		throw getGSException("No time series in WML document");
	    }
	    if (series.size() > 1) {
		throw getGSException("Too many time series in WML document");
	    }

	    File out = File.createTempFile("WML-to-NC", ".nc");
	    H4SingleTimeSeriesWriter writer = new H4SingleTimeSeriesWriter(out.getAbsolutePath());

	    ////////////////////////////////////////////////////////////////////

	    TimeSeriesType serie = series.get(0);

	    List<Long> timeValues = new ArrayList<>();
	    List<Double> acquisitions = new ArrayList<>();
	    List<String> methodCode = new ArrayList<>();
	    List<String> qualityControlLevelCode = new ArrayList<>();
	    List<String> sourceCode = new ArrayList<>();
	    List<String> censorCode = new ArrayList<>();

	    NetCDFVariable<Long> timeVariable = new NetCDFVariable<Long>("time", timeValues, "milliseconds since 1970-01-01 00:00:00",
		    DataType.LONG);
	    VariableInfoType serieVariable = serie.getVariable();
	    JAXBElement<TimeScale> tScale = serieVariable.getTimeScale();
	    if (tScale != null) {
		TimeScale timeScale = tScale.getValue();
		if (timeScale != null) {
		    timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_IS_REGULAR.getNetCDFName(), "" + timeScale.isIsRegular());
		    UnitsType unit = timeScale.getUnit();
		    if (unit != null) {
			timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_NAME.getNetCDFName(),
				"" + timeScale.getUnit().getUnitName());
			timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_TYPE.getNetCDFName(),
				"" + timeScale.getUnit().getUnitType());
			timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_ABBREVIATION.getNetCDFName(),
				"" + timeScale.getUnit().getUnitAbbreviation());
			timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_CODE.getNetCDFName(),
				"" + timeScale.getUnit().getUnitCode());
			timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_TIME_SUPPORT.getNetCDFName(),
				"" + timeScale.getTimeSupport());
		    }
		}
	    }
	    String netCDFVariableName = getNetCDFName(serieVariable.getVariableName());
	    String netCDFStandardName = getNetCDFStandardName(serieVariable.getVariableName());

	    DateTimeZone zone = DateTimeZone.UTC;
	    SourceInfoType sourceInfo = serie.getSourceInfo();
	    Boolean usesDaylightSavingTime = true;
	    if (sourceInfo != null) {
		if (sourceInfo instanceof SiteInfoType) {
		    SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
		    TimeZoneInfo timeZoneInfo = siteInfo.getTimeZoneInfo();
		    if (timeZoneInfo != null) {
			if (timeZoneInfo.isSiteUsesDaylightSavingsTime() != null) {
			    usesDaylightSavingTime = timeZoneInfo.isSiteUsesDaylightSavingsTime();
			}
			DefaultTimeZone defaultTimeZone = timeZoneInfo.getDefaultTimeZone();
			if (defaultTimeZone != null) {
			    String timeZoneCode = defaultTimeZone.getZoneAbbreviation();
			    if (timeZoneCode != null) {
				DateTimeFormatter timezoneFormatter = DateTimeFormatter.ofPattern("z");
				TemporalAccessor temporalAccessor = timezoneFormatter.parse(timeZoneCode);
				ZoneId zoneId = ZoneId.from(temporalAccessor);
				zone = DateTimeZone.forID(zoneId.getId());
			    }
			}
		    }
		}
	    }

	    List<TsValuesSingleVariableType> values = serie.getValues();
	    List<CensorCodeType> censorCodes = new ArrayList<>();
	    List<QualityControlLevelType> qualityControlLevels = new ArrayList<>();
	    List<MethodType> methods = new ArrayList<>();
	    List<SourceType> sources = new ArrayList<>();
	    double missingValue = Double.NaN;
	    if (serieVariable.getNoDataValue() != null) {
		missingValue = serieVariable.getNoDataValue();
	    }
	    for (TsValuesSingleVariableType value : values) {
		censorCodes.addAll(value.getCensorCode());
		qualityControlLevels.addAll(value.getQualityControlLevel());
		methods.addAll(value.getMethod());
		sources.addAll(value.getSource());

		List<ValueSingleVariable> innerValues = value.getValue();
		for (ValueSingleVariable innerValue : innerValues) {
		    BigDecimal decimalValue = innerValue.getValue();
		    double dValue = Double.NaN;
		    if (decimalValue != null) {
			dValue = decimalValue.doubleValue();
		    }
		    if (Double.isFinite(dValue) && Double.isFinite(missingValue)) {
			if (Math.abs(dValue - missingValue) < TOL) {
			    dValue = Double.NaN;
			}
		    }

		    Date date = null;
		    XMLGregorianCalendar utcTime = innerValue.getDateTimeUTC();
		    XMLGregorianCalendar defaultTime = innerValue.getDateTime();
		    if (utcTime != null) {
			date = utcTime.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
		    } else {

			date = defaultTime.toGregorianCalendar(TimeZone.getTimeZone(zone.getID()), null, null).getTime();

			if (!usesDaylightSavingTime) {
			    // in this case a custom offset must be applied
			    int offset = zone.getOffset(date.getTime());
			    int standardOffset = zone.getStandardOffset(date.getTime());
			    date = new Date(date.getTime() + (standardOffset - offset));
			}

		    }

		    if (date != null) {
			// the number of milliseconds since January 1, 1970, 00:00:00 GMT
			Long milliseconds = date.getTime();

			timeValues.add(milliseconds);
			acquisitions.add(dValue);
			if (innerValue.getCensorCode() != null)
			    censorCode.add(innerValue.getCensorCode());
			if (innerValue.getMethodCode() != null)
			    methodCode.add(innerValue.getMethodCode());
			if (innerValue.getQualityControlLevelCode() != null)
			    qualityControlLevelCode.add(innerValue.getQualityControlLevelCode());
			if (innerValue.getSourceCode() != null)
			    sourceCode.add(innerValue.getSourceCode());

		    }
		}
	    }

	    UnitsType seriesUnit = serieVariable.getUnit();
	    String netCDFUnits = null;
	    String wmlUnitName = null;
	    String wmlUnitType = null;
	    String wmlUnitCode = null;
	    String wmlUnitAbbreviation = null;
	    if (seriesUnit != null) {
		wmlUnitName = seriesUnit.getUnitName();
		wmlUnitType = seriesUnit.getUnitType();
		wmlUnitCode = seriesUnit.getUnitCode();
		wmlUnitAbbreviation = seriesUnit.getUnitAbbreviation();
		netCDFUnits = getNetCDFUnits(wmlUnitName);
	    }

	    NetCDFVariable<Double> mainVariable = new NetCDFVariable<Double>(netCDFVariableName, acquisitions, netCDFUnits,
		    DataType.DOUBLE);
	    mainVariable.addAttribute(NetCDFAttribute.WML_VALUE_TYPE.getNetCDFName(), serieVariable.getValueType());
	    mainVariable.addAttribute(NetCDFAttribute.WML_DATA_TYPE.getNetCDFName(), serieVariable.getDataType());
	    mainVariable.addAttribute(NetCDFAttribute.WML_GENERAL_CATEGORY.getNetCDFName(), serieVariable.getGeneralCategory());
	    mainVariable.addAttribute(NetCDFAttribute.WML_SAMPLE_MEDIUM.getNetCDFName(), serieVariable.getSampleMedium());
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_NAME.getNetCDFName(), wmlUnitName);
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_TYPE.getNetCDFName(), wmlUnitType);
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_ABBREVIATION.getNetCDFName(), wmlUnitAbbreviation);
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_CODE.getNetCDFName(), wmlUnitCode);
	    mainVariable.addAttribute(NetCDFAttribute.WML_SPECIATION.getNetCDFName(), serieVariable.getSpeciation());

	    mainVariable.setMissingValue(Double.NaN); // in any case we set missing value to NaN, it is more accurate

	    List<NetCDFVariable<String>> ancillaryVariables = new ArrayList<>();
	    {
		NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_censor_code", censorCode, null,
			DataType.STRING);
		variable.addAttribute("long_name", "Censor code");
		String flagValues = "";
		String flagMeanings = "";
		String flagDescriptions = "";
		for (CensorCodeType code : censorCodes) {
		    flagValues += code.getCensorCode() + ", ";
		    flagMeanings += getNetCDFName(code.getCensorCodeDescription()) + ", ";
		    flagDescriptions += code.getCensorCodeDescription() + "; ";
		}
		if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
		    flagValues = flagValues.substring(0, flagValues.length() - 2);
		    flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
		    flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
		    variable.addAttribute(FLAG_VALUES, flagValues.trim());
		    variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
		    variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
		}
		ancillaryVariables.add(variable);
	    }
	    {
		NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_method_code", methodCode, null,
			DataType.STRING);
		variable.addAttribute("long_name", "Method code");
		String flagValues = "";
		String flagMeanings = "";
		String flagDescriptions = "";
		String flagLinks = "";
		for (MethodType method : methods) {
		    flagValues += method.getMethodID() + ", ";
		    flagMeanings += getNetCDFName(method.getMethodDescription()) + ", ";
		    flagDescriptions += method.getMethodDescription() + "; ";
		    flagLinks += method.getMethodLink() + "; ";
		}
		if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
		    flagValues = flagValues.substring(0, flagValues.length() - 2);
		    flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
		    flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
		    flagLinks = flagLinks.substring(0, flagLinks.length() - 2);
		    variable.addAttribute(FLAG_VALUES, flagValues.trim());
		    variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
		    variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
		    variable.addAttribute(FLAG_LINKS, flagLinks.trim());
		}
		ancillaryVariables.add(variable);
	    }
	    {
		NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_quality_control_level_code",
			qualityControlLevelCode, null, DataType.STRING);
		variable.addAttribute("long_name", "Quality control level code");
		String flagValues = "";
		String flagMeanings = "";
		String flagDescriptions = "";
		String flagLongDescriptions = "";
		for (QualityControlLevelType qualityControlLevel : qualityControlLevels) {
		    flagValues += qualityControlLevel.getQualityControlLevelCode() + ", ";
		    flagMeanings += getNetCDFName(qualityControlLevel.getDefinition()) + ", ";
		    flagDescriptions += qualityControlLevel.getDefinition() + "; ";
		    flagLongDescriptions += qualityControlLevel.getDefinition() + "; ";
		}
		if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
		    flagValues = flagValues.substring(0, flagValues.length() - 2);
		    flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
		    flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
		    flagLongDescriptions = flagLongDescriptions.substring(0, flagLongDescriptions.length() - 2);
		    variable.addAttribute(FLAG_VALUES, flagValues.trim());
		    variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
		    variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
		    variable.addAttribute(FLAG_LONG_DESCRIPTIONS, flagLongDescriptions.trim());
		}
		ancillaryVariables.add(variable);
	    }
	    {
		NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_source_code", sourceCode, null,
			DataType.STRING);
		variable.addAttribute("long_name", "Source code");
		String flagValues = "";
		String flagMeanings = "";
		String flagDescriptions = "";
		String flagLongDescriptions = "";
		String flagLinks = "";
		for (SourceType source : sources) {
		    flagValues += source.getSourceCode() + ", ";
		    flagMeanings += getNetCDFName(source.getOrganization()) + ", ";
		    flagDescriptions += source.getOrganization() + "; ";
		    flagLongDescriptions += source.getSourceDescription() + "; ";
		    flagLinks += source.getSourceLink() + "; ";
		}
		if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
		    flagValues = flagValues.substring(0, flagValues.length() - 2);
		    flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
		    flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
		    flagLongDescriptions = flagLongDescriptions.substring(0, flagLongDescriptions.length() - 2);
		    variable.addAttribute(FLAG_VALUES, flagValues.trim());
		    variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
		    variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
		    variable.addAttribute(FLAG_LONG_DESCRIPTIONS, flagLongDescriptions.trim());
		    variable.addAttribute(FLAG_LINKS, flagLinks.trim());
		}
		ancillaryVariables.add(variable);
	    }
	    String ancillaryString = "";
	    for (NetCDFVariable<String> ancillaryVariable : ancillaryVariables) {
		ancillaryString += ancillaryVariable.getName() + " ";
	    }
	    if (!ancillaryString.isEmpty()) {
		mainVariable.addAttribute("ancillary_variables", ancillaryString.trim());
	    }
	    if (netCDFStandardName != null) {
		mainVariable.setStandardName(netCDFStandardName);
	    }
	    if (netCDFUnits != null) {
		mainVariable.setUnits(netCDFUnits);
	    }
	    mainVariable.setLongName(serieVariable.getVariableName());

	    SimpleStation station = new SimpleStation();

	    if (sourceInfo instanceof SourceInfoType) {
		SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
		GeoLocation geoLocation1 = siteInfo.getGeoLocation();
		if (geoLocation1 != null) {
		    GeogLocationType geoLocation = geoLocation1.getGeogLocation();
		    if (geoLocation != null) {
			if (geoLocation instanceof LatLonPointType) {
			    LatLonPointType point = (LatLonPointType) geoLocation;
			    station.setLatitude(point.getLatitude()); // 3) lat-lon
			    station.setLongitude(point.getLongitude());
			} else {
			    throw new RuntimeException("Found unexpected geometry");
			}
		    }
		}
		station.setAltitude(siteInfo.getElevationM()); // 4) elevation
		station.setVerticalDatum(siteInfo.getVerticalDatum()); // 5) vertical datum
		station.setName(siteInfo.getSiteName()); // 1) site name
		List<SiteCode> siteCodes = siteInfo.getSiteCode();
		SiteCode siteCode = null;
		if (siteCodes != null && !siteCodes.isEmpty()) {
		    siteCode = siteCodes.get(0);
		}
		if (siteCode != null) {
		    station.setIdentifier(siteCode.getNetwork() + ":" + siteCode.getValue()); // 2) site network and
											      // code
		}

		List<PropertyType> properties = siteInfo.getSiteProperty();
		for (PropertyType property : properties) {
		    if (property.getName() != null && property.getValue() != null)
			station.addProperty(property.getName(), property.getValue());
		}
	    }

	    List<NetCDFVariable<?>> variables = new ArrayList<>();
	    variables.add(mainVariable);
	    variables.addAll(ancillaryVariables);
	    writer.write(station, timeVariable, variables.toArray(new NetCDFVariable<?>[] {}));

	    ///
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    FileInputStream fis = new FileInputStream(out);
	    IOUtils.copy(fis, baos);
	    fis.close();
	    baos.close();
	    out.delete();
	    return new ByteArrayInputStream(baos.toByteArray());
	} catch (Exception e) {
	    e.printStackTrace();
	    throw getGSException("Error writing NetCDF");
	}

    }

    /**
     * Normalizes the name according to NetCDF common syntactic rules
     * 
     * @param variableName
     * @return
     */
    public String getNetCDFName(String variableName) {
	if (variableName == null) {
	    return null;
	}
	return variableName.toLowerCase().replace(" ", "_");
    }

    private String getNetCDFStandardName(String variableName) {
	// TODO implement
	return null;
    }

    private String getNetCDFUnits(String unitName) {

	return unitName;
    }

    private GSException getGSException(String message) {

	return GSException.createException(//
		getClass(), //
		message, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		WML_11_TO_NETCDF_ERROR);

    }

}
