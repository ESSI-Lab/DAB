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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasureType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.CodeType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.StringOrRefType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.iso2005.gco.CharacterStringPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.NamedValuePropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.NamedValueType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.om__2.TimeObjectPropertyType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.NetCDFAttribute;
import eu.essi_lab.netcdf.timeseries.H4SingleTimeSeriesWriter;
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.timeseries.SimpleStation;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;
import ucar.ma2.DataType;

public class WML20_To_NetCDF_Processor extends DataProcessor {

    public WML20_To_NetCDF_Processor() {
    }

    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MEANINGS = "flag_meanings";
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";
    public static final String FLAG_LONG_DESCRIPTIONS = "flag_long_descriptions";
    public static final String FLAG_LINKS = "flag_links";
    private static final String WML_20_TO_NETCDF_PROCESSOR_ERROR = "WML_20_TO_NETCDF_PROCESSOR_ERROR";

    @Override
    public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {

	InputStream stream = new FileInputStream(dataObject.getFile());
	InputStream result = convert(stream);
	stream.close();
	DataObject ret = new DataObject();
	ret.setFileFromStream(result, "WML11_To_NetCDF3_Processor");
	if (result != null)
	    result.close();
	return ret;
    }

    public InputStream convert(InputStream wml20Stream) throws GSException {

	CollectionType collection;
	try {
	    collection = JAXBWML2.getInstance().unmarshalCollection(wml20Stream);
	    wml20Stream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw getGSException("Error unmarshalling WML 2.0");
	}
	try {

	    SimpleStation station = new SimpleStation();

	    List<OMObservationPropertyType> observationMembers = collection.getObservationMember();

	    TreeMap<Date, Double> map = new TreeMap<>();

	    String varName = "data";

	    String stationId = "";
	    String stationName = "";
	    String stationDescription = "";
	    Double lat = null;
	    Double lon = null;

	    String propertyId = "";
	    String propertyName = "";

	    String unitsId = "";
	    String unitsName = "";
	    String unitsAbbreviation = "";

	    // here is assumed that observations are homogeneous, hence all put in the same netcdf variable
	    if (!observationMembers.isEmpty()) {
		OMObservationPropertyType observationMember = observationMembers.get(0);
		ReferenceType observedProperty = observationMember.getOMObservation().getObservedProperty();
		if (observedProperty != null) {
		    propertyId = observedProperty.getHref();
		    propertyName = observedProperty.getTitle();
		    if (propertyName == null || propertyName.isEmpty()) {
			propertyName = propertyId;
		    }
		}
		FeaturePropertyType foi = observationMember.getOMObservation().getFeatureOfInterest();

		if (foi != null) {
		    JAXBElement<? extends AbstractFeatureType> jaxb = foi.getAbstractFeature();
		    if (jaxb != null) {
			AbstractFeatureType af = jaxb.getValue();
			if (af instanceof SFSpatialSamplingFeatureType) {
			    SFSpatialSamplingFeatureType monitor = (SFSpatialSamplingFeatureType) af;
			    List<CodeType> names = monitor.getName();
			    if (names != null && !names.isEmpty()) {
				stationName = names.get(0).getValue();
			    }
			    List<FeaturePropertyType> sampledFeatures = monitor.getSampledFeature();
			    if (sampledFeatures != null && !sampledFeatures.isEmpty()) {
				FeaturePropertyType sampledFeature = sampledFeatures.get(0);
				if (stationName == null || stationName.isEmpty()) {
				    stationName = sampledFeature.getTitle();
				}
				stationId = sampledFeature.getHref();
			    }
			    if (stationId == null || stationId.isEmpty()) {
				stationId = monitor.getId();
			    }
			    StringOrRefType description = monitor.getDescription();
			    if (description != null) {
				stationDescription = description.getValue();
			    }
			    JAXBElement<? extends AbstractGeometryType> ag = monitor.getShape().getAbstractGeometry();
			    AbstractGeometryType agv = ag.getValue();
			    if (agv instanceof PointType) {
				PointType pt = (PointType) agv;
				List<Double> pos = pt.getPos().getValue();
				if (!pos.isEmpty()) {
				    lat = pos.get(0);
				    lon = pos.get(1);
				}
			    }
			    List<NamedValuePropertyType> parameters = monitor.getParameter();
			    for (NamedValuePropertyType parameter : parameters) {
				NamedValueType namedValue = parameter.getNamedValue();
				if (namedValue != null) {
				    ReferenceType referenceName = namedValue.getName();
				    if (referenceName != null) {
					String title = referenceName.getTitle();
					if (title == null) {
					    title = referenceName.getHref();
					}
					if (title != null && !title.isEmpty()) {
					    Object value = namedValue.getValue();
					    if (value != null) {
						String v = value.toString();
						if (value instanceof CharacterStringPropertyType) {
						    CharacterStringPropertyType cs = (CharacterStringPropertyType) value;
						    v = cs.getCharacterString().getValue().toString();
						}
						station.addProperty(title, v);
					    }
					}
				    }

				}
			    }
			}
		    }
		}
	    }
	    for (OMObservationPropertyType observationMember : observationMembers) {

		OMObservationType observation = observationMember.getOMObservation();

		Result anyResult = observation.getResult();

		ResultWrapper wrapper = new ResultWrapper(anyResult);

		MeasurementTimeseriesType measurementTimeSeries = null;
		try {
		    measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType measure = null;
		if (measurementTimeSeries == null) {
		    measure = wrapper.getMeasureType();
		}
		if (measurementTimeSeries != null) {

		    List<TVPDefaultMetadataPropertyType> dpm = measurementTimeSeries.getDefaultPointMetadata();
		    if (!dpm.isEmpty()) {
			JAXBElement<? extends TVPMetadataType> jaxb = dpm.get(0).getDefaultTVPMetadata();
			if (jaxb != null) {
			    TVPMetadataType tv = jaxb.getValue();
			    if (tv instanceof TVPMeasurementMetadataType) {
				TVPMeasurementMetadataType tvMeta = (TVPMeasurementMetadataType) tv;
				UnitReference uom = tvMeta.getUom();
				if (uom != null) {
				    unitsName = uom.getTitle();
				    unitsId = uom.getHref();
				    unitsAbbreviation = uom.getCode();
				}
			    }

			}
		    }

		    List<Point> points = measurementTimeSeries.getPoint();
		    for (Point point : points) {

			MeasureTVPType mtvp = point.getMeasurementTVP();
			if (mtvp != null) {

			    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time = mtvp.getTime();
			    if (time != null) {
				List<String> values = time.getValue();
				if (values != null && !values.isEmpty()) {
				    String value = values.get(0);
				    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(value);
				    if (optionalDate.isPresent()) {
					Date date = optionalDate.get();
					if (date != null) {
					    JAXBElement<MeasureType> jaxb = mtvp.getValue();
					    if (jaxb != null) {
						MeasureType myMeasure = jaxb.getValue();
						if (myMeasure != null) {
						    map.put(date, myMeasure.getValue());
						} else {
						    map.put(date, null);
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		} else if (measure != null) {

		    Long milliseconds = null;
		    TimeObjectPropertyType time = observation.getPhenomenonTime();
		    if (time != null) {
			JAXBElement<? extends AbstractTimeObjectType> abstractTime = time.getAbstractTimeObject();
			if (abstractTime != null) {
			    AbstractTimeObjectType timeValue = abstractTime.getValue();
			    if (timeValue instanceof TimeInstantType) {
				TimeInstantType instant = (TimeInstantType) timeValue;
				String timeString = instant.getTimePosition().getValue().get(0);
				Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
				if (optionalMilliseconds.isPresent()) {
				    milliseconds = optionalMilliseconds.get().getTime();
				}
			    } else if (timeValue instanceof TimePeriodType) {
				TimePeriodType tpt = (TimePeriodType) timeValue;
				TimePositionType end = tpt.getEndPosition();
				String timeString = end.getValue().get(0);
				Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
				if (optionalMilliseconds.isPresent()) {
				    milliseconds = optionalMilliseconds.get().getTime();
				}

			    }
			}
		    }

		    String uom = measure.getUom();
		    double value = measure.getValue();
		    map.put(new Date(milliseconds), value);
		    unitsName = uom;
		    unitsId = uom;
		    unitsAbbreviation = uom;
		} else {
		    throw GSException.createException(getClass(), "Measure value not found", ErrorInfo.ERRORTYPE_INTERNAL,
			    ErrorInfo.SEVERITY_ERROR, WML_20_TO_NETCDF_PROCESSOR_ERROR);
		}
	    }

	    File out = File.createTempFile("WML-20-to-NC", ".nc");
	    H4SingleTimeSeriesWriter writer = new H4SingleTimeSeriesWriter(out.getAbsolutePath());

	    ////////////////////////////////////////////////////////////////////

	    List<Long> timeValues = new ArrayList<>();
	    List<Double> acquisitions = new ArrayList<>();

	    double missingValue = Double.NaN;
	    for (Date date : map.keySet()) {
		timeValues.add(date.getTime());
		Double v = map.get(date);
		if (v == null) {
		    v = missingValue;
		}
		acquisitions.add(v);
	    }

	    // List<String> methodCode = new ArrayList<>();
	    // List<String> qualityControlLevelCode = new ArrayList<>();
	    // List<String> sourceCode = new ArrayList<>();
	    // List<String> censorCode = new ArrayList<>();

	    NetCDFVariable<Long> timeVariable = new NetCDFVariable<Long>("time", timeValues, "milliseconds since 1970-01-01 00:00:00",
		    DataType.LONG);
	    // VariableInfoType serieVariable = serie.getVariable();
	    // JAXBElement<TimeScale> tScale = serieVariable.getTimeScale();
	    // if (tScale != null) {
	    // TimeScale timeScale = tScale.getValue();
	    // if (timeScale != null) {
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_IS_REGULAR.getNetCDFName(), "" +
	    // timeScale.isIsRegular());
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_NAME.getNetCDFName(),
	    // "" + timeScale.getUnit().getUnitName());
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_TYPE.getNetCDFName(),
	    // "" + timeScale.getUnit().getUnitType());
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_ABBREVIATION.getNetCDFName(),
	    // "" + timeScale.getUnit().getUnitAbbreviation());
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_UNIT_CODE.getNetCDFName(),
	    // "" + timeScale.getUnit().getUnitCode());
	    // timeVariable.addAttribute(NetCDFAttribute.WML_TIME_SCALE_TIME_SUPPORT.getNetCDFName(), "" +
	    // timeScale.getTimeSupport());
	    // }
	    // }
	    if (propertyName != null && !propertyName.isEmpty()) {
		varName = propertyName;
	    }
	    String netCDFVariableName = getNetCDFName(varName);
	    // String netCDFStandardName = getNetCDFStandardName(serieVariable.getVariableName());

	    // TimeZone timeZone = TimeZone.getTimeZone("GMT");
	    // SourceInfoType sourceInfo = serie.getSourceInfo();
	    // if (sourceInfo != null) {
	    // if (sourceInfo instanceof SiteInfoType) {
	    // SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
	    // TimeZoneInfo timeZoneInfo = siteInfo.getTimeZoneInfo();
	    // if (timeZoneInfo != null) {
	    // DefaultTimeZone defaultTimeZone = timeZoneInfo.getDefaultTimeZone();
	    // if (defaultTimeZone != null) {
	    // String timeZoneCode = defaultTimeZone.getZoneAbbreviation();
	    // if (timeZoneCode != null) {
	    // timeZone = TimeZone.getTimeZone(timeZoneCode);
	    // }
	    // }
	    // }
	    //
	    // }
	    // }

	    //// List<TsValuesSingleVariableType> values = serie.getValues();
	    // List<CensorCodeType> censorCodes = new ArrayList<>();
	    // List<QualityControlLevelType> qualityControlLevels = new ArrayList<>();
	    // List<MethodType> methods = new ArrayList<>();
	    // List<SourceType> sources = new ArrayList<>();
	    // for (TsValuesSingleVariableType value : values) {
	    // censorCodes.addAll(value.getCensorCode());
	    // qualityControlLevels.addAll(value.getQualityControlLevel());
	    // methods.addAll(value.getMethod());
	    // sources.addAll(value.getSource());
	    //
	    // List<ValueSingleVariable> innerValues = value.getValue();
	    // for (ValueSingleVariable innerValue : innerValues) {
	    // double dValue = innerValue.getValue().doubleValue();
	    //
	    // Date date = null;
	    // XMLGregorianCalendar utcTime = innerValue.getDateTimeUTC();
	    // XMLGregorianCalendar defaultTime = innerValue.getDateTime();
	    // if (utcTime != null) {
	    // date = utcTime.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
	    // } else {
	    // date = defaultTime.toGregorianCalendar(timeZone, null, null).getTime();
	    // }
	    //
	    // if (date != null) {
	    // // the number of milliseconds since January 1, 1970, 00:00:00 GMT
	    // Long milliseconds = date.getTime();
	    //
	    // timeValues.add(milliseconds);
	    // acquisitions.add(dValue);
	    // if (innerValue.getCensorCode() != null)
	    // censorCode.add(innerValue.getCensorCode());
	    // if (innerValue.getMethodCode() != null)
	    // methodCode.add(innerValue.getMethodCode());
	    // if (innerValue.getQualityControlLevelCode() != null)
	    // qualityControlLevelCode.add(innerValue.getQualityControlLevelCode());
	    // if (innerValue.getSourceCode() != null)
	    // sourceCode.add(innerValue.getSourceCode());
	    //
	    // }
	    // }
	    // }

	    String netCDFUnits = null;
	    if (unitsName != null && !unitsName.equals("")) {
		netCDFUnits = getNetCDFUnits(unitsName);
	    }

	    NetCDFVariable<Double> mainVariable = new NetCDFVariable<Double>(netCDFVariableName, acquisitions, netCDFUnits,
		    DataType.DOUBLE);

	    mainVariable.addAttribute("missing_value", missingValue);

	    mainVariable.addAttribute("long_name", propertyName);

	    mainVariable.addAttribute(NetCDFAttribute.WML_VARIABLE_CODE.getNetCDFName(), propertyId);

	    // mainVariable.addAttribute(NetCDFAttribute.WML_VALUE_TYPE.getNetCDFName(), serieVariable.getValueType());
	    // mainVariable.addAttribute(NetCDFAttribute.WML_DATA_TYPE.getNetCDFName(), serieVariable.getDataType());
	    // mainVariable.addAttribute(NetCDFAttribute.WML_GENERAL_CATEGORY.getNetCDFName(),
	    // serieVariable.getGeneralCategory());
	    // mainVariable.addAttribute(NetCDFAttribute.WML_SAMPLE_MEDIUM.getNetCDFName(),
	    // serieVariable.getSampleMedium());
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_NAME.getNetCDFName(), unitsName);
	    // mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_TYPE.getNetCDFName(), wmlUnitType);
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_ABBREVIATION.getNetCDFName(), unitsAbbreviation);
	    mainVariable.addAttribute(NetCDFAttribute.WML_UNIT_CODE.getNetCDFName(), unitsId);
	    // mainVariable.addAttribute(NetCDFAttribute.WML_SPECIATION.getNetCDFName(), serieVariable.getSpeciation());
	    // mainVariable.setMissingValue(serieVariable.getNoDataValue());

	    // List<NetCDFVariable<String>> ancillaryVariables = new ArrayList<>();
	    // {
	    // NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_censor_code",
	    // censorCode, null,
	    // DataType.STRING);
	    // variable.addAttribute("long_name", "Censor code");
	    // String flagValues = "";
	    // String flagMeanings = "";
	    // String flagDescriptions = "";
	    // for (CensorCodeType code : censorCodes) {
	    // flagValues += code.getCensorCode() + ", ";
	    // flagMeanings += getNetCDFName(code.getCensorCodeDescription()) + ", ";
	    // flagDescriptions += code.getCensorCodeDescription() + "; ";
	    // }
	    // if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
	    // flagValues = flagValues.substring(0, flagValues.length() - 2);
	    // flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
	    // flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
	    // variable.addAttribute(FLAG_VALUES, flagValues.trim());
	    // variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
	    // variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
	    // }
	    // ancillaryVariables.add(variable);
	    // }
	    // {
	    // NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_method_code",
	    // methodCode, null,
	    // DataType.STRING);
	    // variable.addAttribute("long_name", "Method code");
	    // String flagValues = "";
	    // String flagMeanings = "";
	    // String flagDescriptions = "";
	    // String flagLinks = "";
	    // for (MethodType method : methods) {
	    // flagValues += method.getMethodID() + ", ";
	    // flagMeanings += getNetCDFName(method.getMethodDescription()) + ", ";
	    // flagDescriptions += method.getMethodDescription() + "; ";
	    // flagLinks += method.getMethodLink() + "; ";
	    // }
	    // if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
	    // flagValues = flagValues.substring(0, flagValues.length() - 2);
	    // flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
	    // flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
	    // flagLinks = flagLinks.substring(0, flagLinks.length() - 2);
	    // variable.addAttribute(FLAG_VALUES, flagValues.trim());
	    // variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
	    // variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
	    // variable.addAttribute(FLAG_LINKS, flagLinks.trim());
	    // }
	    // ancillaryVariables.add(variable);
	    // }
	    // {
	    // NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName +
	    // "_quality_control_level_code",
	    // qualityControlLevelCode, null, DataType.STRING);
	    // variable.addAttribute("long_name", "Quality control level code");
	    // String flagValues = "";
	    // String flagMeanings = "";
	    // String flagDescriptions = "";
	    // String flagLongDescriptions = "";
	    // for (QualityControlLevelType qualityControlLevel : qualityControlLevels) {
	    // flagValues += qualityControlLevel.getQualityControlLevelCode() + ", ";
	    // flagMeanings += getNetCDFName(qualityControlLevel.getDefinition()) + ", ";
	    // flagDescriptions += qualityControlLevel.getDefinition() + "; ";
	    // flagLongDescriptions += qualityControlLevel.getDefinition() + "; ";
	    // }
	    // if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
	    // flagValues = flagValues.substring(0, flagValues.length() - 2);
	    // flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
	    // flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
	    // flagLongDescriptions = flagLongDescriptions.substring(0, flagLongDescriptions.length() - 2);
	    // variable.addAttribute(FLAG_VALUES, flagValues.trim());
	    // variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
	    // variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
	    // variable.addAttribute(FLAG_LONG_DESCRIPTIONS, flagLongDescriptions.trim());
	    // }
	    // ancillaryVariables.add(variable);
	    // }
	    // {
	    // NetCDFVariable<String> variable = new NetCDFVariable<String>(netCDFVariableName + "_source_code",
	    // sourceCode, null,
	    // DataType.STRING);
	    // variable.addAttribute("long_name", "Source code");
	    // String flagValues = "";
	    // String flagMeanings = "";
	    // String flagDescriptions = "";
	    // String flagLongDescriptions = "";
	    // String flagLinks = "";
	    // for (SourceType source : sources) {
	    // flagValues += source.getSourceCode() + ", ";
	    // flagMeanings += getNetCDFName(source.getOrganization()) + ", ";
	    // flagDescriptions += source.getOrganization() + "; ";
	    // flagLongDescriptions += source.getSourceDescription() + "; ";
	    // flagLinks += source.getSourceLink() + "; ";
	    // }
	    // if (!flagValues.isEmpty() && !flagDescriptions.isEmpty()) {
	    // flagValues = flagValues.substring(0, flagValues.length() - 2);
	    // flagMeanings = flagMeanings.substring(0, flagMeanings.length() - 2);
	    // flagDescriptions = flagDescriptions.substring(0, flagDescriptions.length() - 2);
	    // flagLongDescriptions = flagLongDescriptions.substring(0, flagLongDescriptions.length() - 2);
	    // variable.addAttribute(FLAG_VALUES, flagValues.trim());
	    // variable.addAttribute(FLAG_MEANINGS, flagMeanings.trim());
	    // variable.addAttribute(FLAG_DESCRIPTIONS, flagDescriptions.trim());
	    // variable.addAttribute(FLAG_LONG_DESCRIPTIONS, flagLongDescriptions.trim());
	    // variable.addAttribute(FLAG_LINKS, flagLinks.trim());
	    // }
	    // ancillaryVariables.add(variable);
	    // }
	    // String ancillaryString = "";
	    // for (NetCDFVariable<String> ancillaryVariable : ancillaryVariables) {
	    // ancillaryString += ancillaryVariable.getName() + " ";
	    // }
	    // if (!ancillaryString.isEmpty()) {
	    // mainVariable.addAttribute("ancillary_variables", ancillaryString.trim());
	    // }
	    // if (netCDFStandardName != null) {
	    // mainVariable.setStandardName(netCDFStandardName);
	    // }
	    if (netCDFUnits != null) {
		mainVariable.setUnits(netCDFUnits);
	    }
	    // mainVariable.setLongName(serieVariable.getVariableName());

	    if (stationDescription != null && !stationDescription.isEmpty()) {
		station.addProperty(NetCDFAttribute.WML_SITE_COMMENTS.getNetCDFName(), stationDescription);
	    }
	    // if (sourceInfo instanceof SourceInfoType) {
	    // SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
	    // GeoLocation geoLocation1 = siteInfo.getGeoLocation();
	    // if (geoLocation1 != null) {
	    // GeogLocationType geoLocation = geoLocation1.getGeogLocation();
	    // if (geoLocation instanceof LatLonPointType) {
	    // LatLonPointType point = (LatLonPointType) geoLocation;
	    if (lat != null && lon != null) {
		station.setLatitude(lat); // 3) lat-lon
		station.setLongitude(lon);
	    }
	    // } else {
	    // throw new RuntimeException("Found unexpected geometry");
	    // }
	    // }
	    // station.setAltitude(siteInfo.getElevationM()); // 4) elevation
	    // station.setVerticalDatum(siteInfo.getVerticalDatum()); // 5) vertical datum
	    station.setName(stationName); // 1) site name
	    // List<SiteCode> siteCodes = siteInfo.getSiteCode();
	    // SiteCode siteCode = null;
	    // if (siteCodes != null && !siteCodes.isEmpty()) {
	    // siteCode = siteCodes.get(0);
	    // }
	    // if (siteCode != null) {
	    // station.setIdentifier(siteCode.getNetwork() + ":" + siteCode.getValue()); // 2) site network and
	    // // code
	    // }
	    //
	    // List<PropertyType> properties = siteInfo.getSiteProperty();
	    // for (PropertyType property : properties) {
	    // station.addProperty(property.getName(), property.getValue());
	    // }
	    // }

	    List<NetCDFVariable<?>> variables = new ArrayList<>();
	    variables.add(mainVariable);
	    // variables.addAll(ancillaryVariables);
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
		WML_20_TO_NETCDF_PROCESSOR_ERROR);

    }

}
