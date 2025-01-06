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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.DocumentMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.DocumentMetadataType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.MonitoringPointType;
import eu.essi_lab.jaxb.wml._2_0.ObservationProcessType;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.CodeWithAuthorityType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ObjectFactory;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.StringOrRefType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantPropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMProcessPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.TimeObjectPropertyType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.ShapeType;
import eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.NetCDFAttribute;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;
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

public class NetCDF_To_WML20_Processor extends DataProcessor {

    public NetCDF_To_WML20_Processor() {
	JAXBWML2.getInstance();
    }

    public static final String FLAG_VALUES = "flag_values";
    public static final String FLAG_MEANINGS = "flag_meanings";
    public static final String FLAG_DESCRIPTIONS = "flag_descriptions";
    public static final String FLAG_LONG_DESCRIPTIONS = "flag_long_descriptions";
    public static final String FLAG_LINKS = "flag_links";
    private static final String NETCDF_TO_WML_PROCESSOR_ERROR = "NETCDF_TO_WML_PROCESSOR_ERROR";

    @Override
    public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {

	FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, dataObject.getFile().getAbsolutePath(), null, null);

	MeasurementTimeseriesType measurementTimeseriesType = new MeasurementTimeseriesType();
	List<TVPDefaultMetadataPropertyType> metadatas = new ArrayList<>();
	TVPDefaultMetadataPropertyType metadata = new TVPDefaultMetadataPropertyType();
	TVPMeasurementMetadataType measurementMetadata = new TVPMeasurementMetadataType();

	JAXBElement<? extends TVPMetadataType> defaultMetadata = JAXBWML2.getInstance().getFactory()
		.createDefaultTVPMeasurementMetadata(measurementMetadata);
	metadata.setDefaultTVPMetadata(defaultMetadata);

	metadatas.add(metadata);
	measurementTimeseriesType.getDefaultPointMetadata().addAll(metadatas);

	CollectionType collection = new CollectionType();
	collection.setId("COL.1");
	JAXBElement<CollectionType> jaxbElement = JAXBWML2.getInstance().getFactory().createCollection(collection);

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

	int count = 0;

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

	// description
	StringOrRefType descriptionString = new StringOrRefType();
	descriptionString.setValue("Time series for parameter: " + variableLongName);
	collection.setDescription(descriptionString);

	// document metadata
	DocumentMetadataPropertyType documentMetadata = new DocumentMetadataPropertyType();
	DocumentMetadataType dmt = new DocumentMetadataType();
	dmt.setId("DMD.1");
	Date now = new Date();
	dmt.setGenerationDate(ISO8601DateTimeUtils.getXMLGregorianCalendar(now));
	dmt.setGenerationSystem("GI-suite");
	documentMetadata.setDocumentMetadata(dmt);
	collection.setMetadata(documentMetadata);

	// phenomenon time

	List<OMObservationPropertyType> observations = new ArrayList<>();
	OMObservationPropertyType observation = new OMObservationPropertyType();
	OMObservationType obs = new OMObservationType();
	obs.setId("OBS.1");
	TimeObjectPropertyType phenomenonTimeProperty = new TimeObjectPropertyType();

	ObjectFactory gmlFactory = new ObjectFactory();

	TimePeriodType timePeriod = new TimePeriodType();
	timePeriod.setId("TP.1");
	TimePositionType time1 = new TimePositionType();

	timePeriod.setBeginPosition(time1);
	TimePositionType time2 = new TimePositionType();

	timePeriod.setEndPosition(time2);
	JAXBElement<TimePeriodType> timeObject = gmlFactory.createTimePeriod(timePeriod);
	phenomenonTimeProperty.setAbstractTimeObject(timeObject);
	obs.setPhenomenonTime(phenomenonTimeProperty);
	observation.setOMObservation(obs);
	observations.add(observation);
	collection.getObservationMember().addAll(observations);

	// result time

	TimeInstantPropertyType timeInstant = new TimeInstantPropertyType();
	TimeInstantType ti = new TimeInstantType();
	ti.setId("TI.1");
	ti.setTimePosition(time2);
	timeInstant.setTimeInstant(ti);
	obs.setResultTime(timeInstant);

	// PROCEDURE

	OMProcessPropertyType procedure = new OMProcessPropertyType();
	ObservationProcessType opt = new ObservationProcessType();
	opt.setId("PROC.1");

	Attribute valueTypeAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_VALUE_TYPE.getNetCDFName());
	if (valueTypeAttribute != null) {
	    String valueType = valueTypeAttribute.getStringValue();
	    ReferenceType referenceType = new ReferenceType();

	    referenceType.setHref(valueType);
	    referenceType.setTitle(valueType);
	    opt.setProcessType(referenceType);
	}

	procedure.setAny(JAXBWML2.getInstance().getFactory().createObservationProcess(opt));

	obs.setProcedure(procedure);

	// OBSERVED PROPERTY

	ReferenceType propertyReference = new ReferenceType();
	propertyReference.setHref(mainVariable.getShortName()); // variable id
	propertyReference.setTitle(variableLongName); // variable name
	obs.setObservedProperty(propertyReference);

	// FOI

	FeaturePropertyType featureProperty = new FeaturePropertyType();
	MonitoringPointType monitoringPoint = new MonitoringPointType();
	monitoringPoint.setId("STA.1");
	CodeWithAuthorityType stationIdentifier = new CodeWithAuthorityType();

	monitoringPoint.setIdentifier(stationIdentifier);
	featureProperty.setAbstractFeature(JAXBWML2.getInstance().getFactory().createMonitoringPoint(monitoringPoint));
	obs.setFeatureOfInterest(featureProperty);

	List<FeaturePropertyType> features = new ArrayList<>();
	FeaturePropertyType sampledFeatureProperty = new FeaturePropertyType();
	// net.opengis.samplingspatial.v_2_0.ObjectFactory spatialFactory = new
	// net.opengis.samplingspatial.v_2_0.ObjectFactory();
	// SFSpatialSamplingFeatureType sfSamplingProperty = new SFSpatialSamplingFeatureType();
	// sfSamplingProperty.set

	features.add(sampledFeatureProperty);
	monitoringPoint.getSampledFeature().addAll(features);

	ShapeType shape = new ShapeType();

	PointType gmlPoint = new PointType();
	JAXBElement<PointType> abstractGeometry = gmlFactory.createPoint(gmlPoint);
	shape.setAbstractGeometry(abstractGeometry);
	monitoringPoint.setShape(shape);

	Attribute mediumAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_SAMPLE_MEDIUM.getNetCDFName());
	if (mediumAttribute != null) {
	    String medium = mediumAttribute.getStringValue();
	    if (medium != null && !medium.equals("")) {
		ReferenceType reference = new ReferenceType();
		reference.setHref(medium);
		reference.setTitle(medium);
		monitoringPoint.setType(reference);
	    }
	}

	// MEASUREMENT

	UnitReference unitReference = new UnitReference();
	Attribute unitAbbreviationAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_ABBREVIATION.getNetCDFName());
	if (unitAbbreviationAttribute != null) {
	    String unitAbbreviation = unitAbbreviationAttribute.getStringValue();

	}
	Attribute unitCodeAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_CODE.getNetCDFName());
	if (unitCodeAttribute != null) {
	    String unitCode = unitCodeAttribute.getStringValue();
	    unitReference.setCode(unitCode);
	}
	Attribute unitNameAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_NAME.getNetCDFName());
	if (unitNameAttribute != null) {
	    String unitName = unitNameAttribute.getStringValue();
	    unitReference.setTitle(unitName);
	}
	Attribute unitTypeAttribute = mainVariable.findAttributeIgnoreCase(NetCDFAttribute.WML_UNIT_TYPE.getNetCDFName());
	if (unitTypeAttribute != null) {
	    String unitType = unitTypeAttribute.getStringValue();
	}
	measurementMetadata.setUom(unitReference);
	

	/*
	 * DATA
	 */
	Date t1 = null;
	Date t2 = null;
	String stationName = "";

	Double lat = null;
	Double lon = null;
	while (pfc.hasNext()) {
	    PointFeature pf = pfc.next();
	    StationPointFeature spf = (StationPointFeature) pf;

	    StationFeature station = spf.getStation();
	    stationName = station.getName();
	    StructureData featureData = pf.getFeatureData();

	    lat = station.getLatitude();
	    lon = station.getLongitude();

	    Point point = new Point();
	    // MeasureTVPType measurement = new MeasureTVPType();

	    TimePositionType time = new TimePositionType();
	    Date date = pf.getObservationTimeAsDate();
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
	    String timeString = ISO8601DateTimeUtils.getISO8601DateTime(date);
	    time.getValue().add(timeString);
	    MeasureTVPType measurement = new MeasureTVPType();
	    measurement.setTime(time);
	    eu.essi_lab.jaxb.wml._2_0.MeasureType measureType = new eu.essi_lab.jaxb.wml._2_0.MeasureType();

	    Double value = featureData.getScalarDouble(mainVariable.getShortName());
	    JAXBElement<eu.essi_lab.jaxb.wml._2_0.MeasureType> measure = JAXBWML2.getInstance().getFactory().createValue(measureType);

	    if (value == null || value.isNaN() || value.isInfinite()) {
		measure.setNil(true);
		measureType.setValue(-9999);
	    } else {
		measureType.setValue(value);
	    }
	    
	    measurement.setValue(measure);
	    point.setMeasurementTVP(measurement);

	    measurementTimeseriesType.getPoint().add(point);
	    count++;
	}

	gmlPoint.setId("LOC.1");
	DirectPositionType dpt = new DirectPositionType();
	dpt.setSrsName("EPSG:4326");
	if (lat == null || lon == null || stationName == null || stationName.equals("")) { // it could be null in case
											   // of no values
	    Station station = fc.getStations().get(0);
	    stationName = station.getName();
	    lat = station.getLatitude();
	    lon = station.getLongitude();
	}
	if (lat != 0 && lon != 0) {
	    dpt.getValue().add(lat);
	    dpt.getValue().add(lon);
	}
	gmlPoint.setPos(dpt);

	if (t1 != null) {
	    time1.getValue().add(ISO8601DateTimeUtils.getISO8601DateTime(t1));
	}
	if (t2 != null) {
	    time2.getValue().add(ISO8601DateTimeUtils.getISO8601DateTime(t2));
	}

	stationIdentifier.setCodeSpace("");
	stationIdentifier.setValue(stationName);

	sampledFeatureProperty.setHref(stationName);
	sampledFeatureProperty.setTitle(stationName);

	DataObject ret = new DataObject();

	File file = File.createTempFile(getClass().getSimpleName(), ".wml");
	file.deleteOnExit();

	if (dataset != null)
	    dataset.close();

	ResultWrapper resultWrapper = new ResultWrapper();
	resultWrapper.setMeasurementTimeseriesType(measurementTimeseriesType);
	obs.setResult(resultWrapper);

	JAXBWML2.getInstance().marshal(jaxbElement, file);
	ret.setFile(file);
	return ret;

    }

    private GSException getGSException(String message) {
	return GSException.createException(//
		getClass(),//
		message,//
		ErrorInfo.ERRORTYPE_INTERNAL,//
		ErrorInfo.SEVERITY_ERROR,//
		NETCDF_TO_WML_PROCESSOR_ERROR);

    }
}
