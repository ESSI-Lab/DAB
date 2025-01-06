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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.GeogLocationType;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo.DefaultTimeZone;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.essi.JAXBWML;

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
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;

public class WML11_To_WML20_Processor extends DataProcessor {

    public WML11_To_WML20_Processor() {
	JAXBWML2.getInstance();
    }

    @Override
    public DataObject process(DataObject dataObject, TargetHandler handler) throws Exception {

	File timeSeriesFile = dataObject.getFile();

	FileInputStream timeSeriesStream = new FileInputStream(timeSeriesFile);

	TimeSeriesResponseType trt;

	trt = JAXBWML.getInstance().parseTimeSeries(timeSeriesStream);

	timeSeriesStream.close();

	List<TimeSeriesType> series = trt.getTimeSeries();

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

	/*
	 * METADATA
	 */

	// description
	StringOrRefType descriptionString = new StringOrRefType();
	descriptionString.setValue("Time series translated from WML 1.1");
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

	/*
	 * DATA
	 */

	for (int i = 0; i < series.size(); i++) {
	    Date dateBegin = null;
	    Date dateEnd = null;
	    String stationName = "";

	    Double lat = null;
	    Double lon = null;
	    TimeSeriesType serie = series.get(i);

	    VariableInfoType variable = serie.getVariable();

	    OMObservationPropertyType observation = new OMObservationPropertyType();
	    OMObservationType obs = new OMObservationType();
	    obs.setId("OBS." + i);
	    TimeObjectPropertyType phenomenonTimeProperty = new TimeObjectPropertyType();

	    ObjectFactory gmlFactory = new ObjectFactory();

	    TimePeriodType timePeriod = new TimePeriodType();
	    timePeriod.setId("TP." + i);
	    TimePositionType timeBegin = new TimePositionType();

	    timePeriod.setBeginPosition(timeBegin);
	    TimePositionType timeEnd = new TimePositionType();

	    timePeriod.setEndPosition(timeEnd);
	    JAXBElement<TimePeriodType> timeObject = gmlFactory.createTimePeriod(timePeriod);
	    phenomenonTimeProperty.setAbstractTimeObject(timeObject);
	    obs.setPhenomenonTime(phenomenonTimeProperty);
	    observation.setOMObservation(obs);

	    collection.getObservationMember().add(observation);

	    TimeZone timeZone = TimeZone.getTimeZone("GMT");
	    SourceInfoType sourceInfo = serie.getSourceInfo();
	    if (sourceInfo != null) {
		if (sourceInfo instanceof SiteInfoType) {
		    SiteInfoType siteInfo = (SiteInfoType) sourceInfo;

		    String siteName = siteInfo.getSiteName();
		    List<SiteCode> siteCodes = siteInfo.getSiteCode();

		    // FOI

		    FeaturePropertyType featureProperty = new FeaturePropertyType();
		    MonitoringPointType monitoringPoint = new MonitoringPointType();
		    monitoringPoint.setId("STA." + i);
		    CodeWithAuthorityType stationIdentifier = new CodeWithAuthorityType();
		    if (!siteCodes.isEmpty()) {
			SiteCode siteCode = siteCodes.iterator().next();
			stationIdentifier.setCodeSpace(siteCode.getNetwork());
			stationIdentifier.setValue(siteCode.getValue());
		    }
		    monitoringPoint.setIdentifier(stationIdentifier);
		    featureProperty.setAbstractFeature(JAXBWML2.getInstance().getFactory().createMonitoringPoint(monitoringPoint));
		    obs.setFeatureOfInterest(featureProperty);

		    List<FeaturePropertyType> features = new ArrayList<>();

		    // net.opengis.samplingspatial.v_2_0.ObjectFactory spatialFactory = new
		    // net.opengis.samplingspatial.v_2_0.ObjectFactory();
		    // SFSpatialSamplingFeatureType sfSamplingProperty = new SFSpatialSamplingFeatureType();
		    // sfSamplingProperty.set

		    for (SiteCode siteCode : siteCodes) {

			FeaturePropertyType sampledFeatureProperty = new FeaturePropertyType();

			sampledFeatureProperty.setHref(siteCode.getValue());
			sampledFeatureProperty.setTitle(siteName);

			features.add(sampledFeatureProperty);
		    }

		    monitoringPoint.getSampledFeature().addAll(features);

		    ShapeType shape = new ShapeType();

		    PointType gmlPoint = new PointType();
		    DirectPositionType dpt = new DirectPositionType();
		    GeoLocation geoLocation = siteInfo.getGeoLocation();
		    if (geoLocation != null) {
			GeogLocationType location = geoLocation.getGeogLocation();
			if (location != null) {
			    String srs = location.getSrs();
			    if (srs == null || srs.equals("")) {
				srs = "EPSG:4326";
			    }
			    dpt.setSrsName(srs);
			    if (location instanceof LatLonPointType) {
				LatLonPointType llpt = (LatLonPointType) location;
				dpt.getValue().add(llpt.getLatitude());
				dpt.getValue().add(llpt.getLongitude());
			    }
			    gmlPoint.setPos(dpt);
			}
		    }

		    JAXBElement<PointType> abstractGeometry = gmlFactory.createPoint(gmlPoint);
		    shape.setAbstractGeometry(abstractGeometry);
		    monitoringPoint.setShape(shape);

		    TimeZoneInfo timeZoneInfo = siteInfo.getTimeZoneInfo();
		    if (timeZoneInfo != null) {
			DefaultTimeZone defaultTimeZone = timeZoneInfo.getDefaultTimeZone();
			if (defaultTimeZone != null) {
			    String timeZoneCode = defaultTimeZone.getZoneAbbreviation();
			    if (timeZoneCode != null) {
				timeZone = TimeZone.getTimeZone(timeZoneCode);
			    }
			}
		    }

		    ReferenceType reference = new ReferenceType();
		    reference.setHref(variable.getSampleMedium());
		    reference.setTitle(variable.getSampleMedium());
		    monitoringPoint.setType(reference);

		}
	    }

	    // PROCEDURE

	    OMProcessPropertyType procedure = new OMProcessPropertyType();
	    ObservationProcessType opt = new ObservationProcessType();
	    opt.setId("PROC." + i);

	    ReferenceType referenceType = new ReferenceType();

	    referenceType.setHref(variable.getValueType());
	    referenceType.setTitle(variable.getValueType());
	    opt.setProcessType(referenceType);

	    procedure.setAny(JAXBWML2.getInstance().getFactory().createObservationProcess(opt));

	    obs.setProcedure(procedure);

	    // OBSERVED PROPERTY

	    ReferenceType propertyReference = new ReferenceType();
	    propertyReference.setHref(variable.getVariableCode().get(0).getValue()); // variable id
	    propertyReference.setTitle(variable.getVariableName()); // variable name
	    obs.setObservedProperty(propertyReference);

	    // MEASUREMENT

	    UnitsType unit = variable.getUnit();
	    if (unit != null) {
		UnitReference unitReference = new UnitReference();
		unitReference.setCode(unit.getUnitCode());
		unitReference.setTitle(unit.getUnitName());
		measurementMetadata.setUom(unitReference);
	    }

	    List<TsValuesSingleVariableType> values = serie.getValues();
	    for (TsValuesSingleVariableType value : values) {
		List<ValueSingleVariable> innerValues = value.getValue();
		Iterator<ValueSingleVariable> iterator = innerValues.iterator();
		while (iterator.hasNext()) {
		    ValueSingleVariable innerValue = (ValueSingleVariable) iterator.next();
		    XMLGregorianCalendar utcTime = innerValue.getDateTimeUTC();
		    XMLGregorianCalendar defaultTime = innerValue.getDateTime();
		    Date date = null;
		    if (utcTime != null) {
			date = utcTime.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
		    } else {
			date = defaultTime.toGregorianCalendar(timeZone, null, null).getTime();
		    }
		    if (date != null) {

			if (dateBegin == null) {
			    dateBegin = date;
			}
			if (dateEnd == null) {
			    dateEnd = date;
			}
			if (date.before(dateBegin)) {
			    dateBegin = date;
			}
			if (date.after(dateEnd)) {
			    dateEnd = date;
			}

			// stationName = station.getName();
			// lat = station.getLatitude();
			// lon = station.getLongitude();

			Point point = new Point();
			// MeasureTVPType measurement = new MeasureTVPType();

			TimePositionType time = new TimePositionType();
			String timeString = ISO8601DateTimeUtils.getISO8601DateTime(date);
			time.getValue().add(timeString);
			MeasureTVPType measurement = new MeasureTVPType();
			measurement.setTime(time);
			eu.essi_lab.jaxb.wml._2_0.MeasureType measureType = new eu.essi_lab.jaxb.wml._2_0.MeasureType();

			measureType.setValue(innerValue.getValue().doubleValue());

			JAXBElement<eu.essi_lab.jaxb.wml._2_0.MeasureType> measure = JAXBWML2.getInstance().getFactory()
				.createValue(measureType);

			measurement.setValue(measure);
			point.setMeasurementTVP(measurement);

			measurementTimeseriesType.getPoint().add(point);
		    }
		}
	    }

	    timeBegin.getValue().add(ISO8601DateTimeUtils.getISO8601DateTime(dateBegin));
	    timeEnd.getValue().add(ISO8601DateTimeUtils.getISO8601DateTime(dateEnd));

	    // result time

	    TimeInstantPropertyType timeInstant = new TimeInstantPropertyType();
	    TimeInstantType ti = new TimeInstantType();
	    ti.setId("TI.1");
	    ti.setTimePosition(timeEnd);
	    timeInstant.setTimeInstant(ti);
	    obs.setResultTime(timeInstant);

	    ResultWrapper resultWrapper = new ResultWrapper();
	    resultWrapper.setMeasurementTimeseriesType(measurementTimeseriesType);
	    obs.setResult(resultWrapper);

	}

	DataObject ret = new DataObject();

	File file = File.createTempFile(getClass().getSimpleName(), ".wml");
	file.deleteOnExit();

	JAXBWML2.getInstance().marshal(jaxbElement, file);
	ret.setFile(file);
	return ret;

    }
}
