package eu.essi_lab.profiler.sos.observation;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType.ObservationData;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeWithAuthorityType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMProcessPropertyType;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.jaxb.wml._2_0.ObjectFactory;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
// import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
// import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
// import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.profiler.sos.SOSUtils;
import eu.essi_lab.profiler.sos.SOSRequest.Parameter;
import eu.essi_lab.wml._2.JAXBWML2;

public class GetObservationResultSetMapper extends DefaultAccessResultSetMapper {

    @Override
    public DataObject map(AccessMessage message, DataObject resource) throws GSException {

	try {

	    GetObservationRequest getValuesRequest = new GetObservationRequest(message.getWebRequest());

	    String observedProperty = getValuesRequest.getParameterValue(Parameter.OBSERVED_PROPERTY);
	    String procedure = getValuesRequest.getParameterValue(Parameter.PROCEDURE);
	    String variable = procedure;
	    if (variable != null) {
		variable = variable.replace(SOSUtils.PROCEDURE_PREFIX, "");
	    }

	    String uniqueSiteCode = getValuesRequest.getParameterValue(Parameter.FEATURE_OF_INTEREST);

	    // TimeSeriesResponseType timeSeries = getBaseResponse(message, resource);

	    Object obj = JAXBSOS.getInstance().unmarshal(resource.getFile());
	    if (obj instanceof JAXBElement<?>) {
		JAXBElement<?> jaxb = (JAXBElement<?>) obj;
		obj = jaxb.getValue();
	    }
	    OMObservationType observation;
	    if (obj instanceof OMObservationType) {
		observation = (OMObservationType) obj;
	    } else {
		String msg = "Unexpected element: " + obj.getClass().getName();
		GSLoggerFactory.getLogger(getClass()).error(msg);
		throw new IllegalArgumentException(msg);
	    }

	    AbstractTimeObjectType abstractTime = observation.getPhenomenonTime().getAbstractTimeObject().getValue();

	    if (abstractTime != null) {
		if (abstractTime instanceof TimePeriodType) {
		    TimePeriodType tpt = (TimePeriodType) abstractTime;
		    TimePositionType bp = tpt.getBeginPosition();
		    List<String> begins = bp.getValue();
		    TimePositionType ep = tpt.getEndPosition();
		    List<String> ends = ep.getValue();
		    if (begins.isEmpty() || ends.isEmpty()) {
			String instantPosition = getValuesRequest.getParameterValue(Parameter.TIME_POSITION);
			String startDate = getValuesRequest.getParameterValue(Parameter.BEGIN_POSITION);
			String endDate = getValuesRequest.getParameterValue(Parameter.END_POSITION);
			if (instantPosition != null) {
			    startDate = instantPosition;
			    endDate = instantPosition;
			}
			if (startDate != null) {
			    bp.getValue().add(startDate);
			}
			if (endDate != null) {
			    ep.getValue().add(endDate);
			}
		    }
		}
	    }

	    eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory factory = new eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory();

	    OMProcessPropertyType procedureType = new OMProcessPropertyType();
	    procedureType.setHref(procedure);
	    observation.setProcedure(procedureType); // SET procedure

	    ReferenceType referenceType = new ReferenceType();
	    referenceType.setHref("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
	    observation.setType(referenceType); // SET observation type

	    ReferenceType observedReference = new ReferenceType();
	    observedReference.setHref(observedProperty);
	    observation.setObservedProperty(observedReference); // SET observed property

	    FeaturePropertyType featureProperty = new FeaturePropertyType();

	    eu.essi_lab.jaxb.sos._2_0.sf._2_0.ObjectFactory sffactory = new eu.essi_lab.jaxb.sos._2_0.sf._2_0.ObjectFactory();
	    SFSpatialSamplingFeatureType sfft = new SFSpatialSamplingFeatureType();
	    CodeType siteNameCode = new CodeType();
	    siteNameCode.setValue("Site name");
	    sfft.getName().add(siteNameCode);
	    CodeWithAuthorityType identifier = new CodeWithAuthorityType();
	    identifier.setValue(uniqueSiteCode);
	    sfft.setIdentifier(identifier);
	    FeaturePropertyType sampledFeature = new FeaturePropertyType();
	    sampledFeature.setHref(uniqueSiteCode);
	    sfft.getSampledFeature().add(sampledFeature);

	    eu.essi_lab.jaxb.sos._2_0.sams._2_0.ObjectFactory samsFactory = new eu.essi_lab.jaxb.sos._2_0.sams._2_0.ObjectFactory();
	    JAXBElement<? extends AbstractFeatureType> abstractFeature = samsFactory.createSFSpatialSamplingFeature(sfft);

	    featureProperty.setAbstractFeature(abstractFeature);
	    observation.setFeatureOfInterest(featureProperty);

	    ObjectFactory wmlFactory = JAXBWML2.getInstance().getFactory();
	    // MeasurementTimeseriesType mtst = new MeasurementTimeseriesType();

	    // JAXBElement<MeasurementTimeseriesType> jaxbElement = wmlFactory.createMeasurementTimeseries(mtst);

	    TVPDefaultMetadataPropertyType point = new TVPDefaultMetadataPropertyType();
	    TVPMeasurementMetadataType measurementMetadata = new TVPMeasurementMetadataType();
	    eu.essi_lab.jaxb.wml._2_0.swe._2.QualityPropertyType quality = new eu.essi_lab.jaxb.wml._2_0.swe._2.QualityPropertyType();
	    // QualityPropertyType quality = new QualityPropertyType();
	    quality.setHref("");
	    quality.setTitle("");
	    measurementMetadata.getQualifier().add(quality);
	    eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference unitReference = new eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference();
	    // UnitReference unitReference = new UnitReference();
	    unitReference.setCode("");
	    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType interpolationReference = new eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType();
	    // net.opengis.gml.v_3_2_1.ReferenceType interpolationReference = new
	    // net.opengis.gml.v_3_2_1.ReferenceType();
	    interpolationReference.setHref("");
	    interpolationReference.setTitle("");
	    measurementMetadata.setInterpolationType(interpolationReference);
	    measurementMetadata.setUom(unitReference);

	    // measurement.setUom("");
	    JAXBElement<? extends TVPMetadataType> pointElement = wmlFactory.createDefaultTVPMeasurementMetadata(measurementMetadata);
	    point.setDefaultTVPMetadata(pointElement);
	    // mtst.getDefaultPointMetadata().add(point);
	    //
	    // try {
	    // List<TsValuesSingleVariableType> values = timeSeries.getTimeSeries().get(0).getValues();
	    // for (TsValuesSingleVariableType value : values) {
	    // List<ValueSingleVariable> vs = value.getValue();
	    // for (ValueSingleVariable v1 : vs) {
	    // XMLGregorianCalendar utc = v1.getDateTimeUTC();
	    // Date date = utc.toGregorianCalendar().getTime();
	    // String timeStr = ISO8601DateTimeUtils.getISO8601DateTime(date);
	    // Point p = new Point();
	    // MeasureTVPType mtvp = new MeasureTVPType();
	    // TimePositionType tpt = new TimePositionType();
	    // tpt.getValue().add(timeStr);
	    // mtvp.setTime(tpt);
	    // MeasureType mt = new MeasureType();
	    // mt.setValue(v1.getValue().doubleValue());
	    // JAXBElement<MeasureType> v = wmlFactory.createMeasureTVPTypeValue(mt);
	    // mtvp.setValue(v);
	    // p.setMeasurementTVP(mtvp);
	    // mtst.getPoint().add(p);
	    // }
	    // }
	    // } catch (Exception e) {
	    // // TODO: handle exception
	    // }

	    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    // JAXBWML2.getInstance().marshal(jaxbElement, baos);
	    // ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	    // XMLDocumentReader reader = new XMLDocumentReader(bais);

	    // XMLDocumentReader reader2 = new XMLDocumentReader("<om:result
	    // xmlns:om=\"http://www.opengis.net/om/2.0\"></om:result>");
	    // XMLDocumentWriter writer2 = new XMLDocumentWriter(reader2);
	    // writer2.addNode("/*[1]", reader.getDocument().getDocumentElement());

	    // observation.setResult(reader2.getDocument().getDocumentElement());

	    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

	    GetObservationResponseType gort = new GetObservationResponseType();
	    ObservationData observationData = new ObservationData();
	    observationData.setOMObservation(observation);
	    gort.getObservationData().add(observationData);

	    JAXBElement<GetObservationResponseType> jaxbResponse = JAXBSOS.getInstance().getFactory().createGetObservationResponse(gort);

	    JAXBSOS.getInstance().marshal(jaxbResponse, baos2);
	    ByteArrayInputStream bais2 = new ByteArrayInputStream(baos2.toByteArray());

	    DataObject ret = new DataObject();
	    ret.setDataDescriptor(message.getTargetDataDescriptor());
	    ret.setFileFromStream(bais2, getClass().getSimpleName() + ".xml");

	    return ret;

	} catch (Exception e) {
	    // throw exception
	}
	return null;

    }

    // protected TimeSeriesResponseType getBaseResponse(AccessMessage message, DataObject resource) throws Exception {
    // WebRequest webRequest = message.getWebRequest();
    //
    // /*
    // * SETTING QUERY INFO
    // */
    // GetObservationRequest getValuesRequest = new GetObservationRequest(webRequest);
    //
    // String procedure = getValuesRequest.getParameterValue(Parameter.PROCEDURE);
    // String variable = procedure;
    // if (variable != null) {
    // variable = variable.replace(SOSUtils.PROCEDURE_PREFIX, "");
    // }
    //
    // String uniqueSiteCode = getValuesRequest.getParameterValue(Parameter.FEATURE_OF_INTEREST);
    //
    // // String startDate = getValuesRequest.getStartDate();
    // // String endDate = getValuesRequest.getEndDate();
    // // String location = getValuesRequest.getParameterValue(parameter)
    //
    // InputStream stream = new FileInputStream(resource.getFile());
    // TimeSeriesResponseType timeSeries = JAXBWML.getInstance().parseTimeSeries(stream);
    // stream.close();
    //
    // QueryInfoType queryInfo = timeSeries.getQueryInfo();
    // if (queryInfo == null) {
    // queryInfo = new QueryInfoType();
    // timeSeries.setQueryInfo(queryInfo);
    // }
    //
    // GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    // calendar.setTime(new Date());
    // XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    // queryInfo.setCreationTime(xmlCalendar);
    // Criteria criteria = new Criteria();
    // criteria.setMethodCalled(getMethodName());
    // // criteria.setLocationParam(location);
    // criteria.setVariableParam(variable);
    // TimeParam timeParam = new TimeParam();
    // // timeParam.setBeginDateTime(startDate);
    // // timeParam.setEndDateTime(endDate);
    // criteria.setTimeParam(timeParam);
    // queryInfo.setCriteria(criteria);
    //
    // /*
    // * SETTING NEEDED VALUES FOR HYDRODESKTOP
    // */
    //
    // List<TimeSeriesType> series = timeSeries.getTimeSeries();
    //
    // if (!series.isEmpty()) {
    // TimeSeriesType serie = series.get(0);
    // VariableInfoType var = serie.getVariable();
    // if (var == null) {
    // var = new VariableInfoType();
    // serie.setVariable(var);
    // }
    // if (var != null) {
    // var.getVariableCode().clear();
    // VariableCode variableCode = new VariableCode();
    // variableCode.setValue(variable);
    // var.getVariableCode().add(variableCode);
    // String variableName = var.getVariableName();
    // if (variableName != null) {
    // // because USGS doesn't correctly escape
    // variableName = variableName.replace("&#179;", "³");
    // }
    // var.setVariableName(variableName);
    //
    // UnitsType units = var.getUnit();
    // if (units == null) {
    // units = new UnitsType();
    // units.setUnitName("");
    // units.setUnitDescription("");
    // units.setUnitAbbreviation("");
    // var.setUnit(units);
    // }
    //
    // }
    // SourceInfoType sourceInfo = serie.getSourceInfo();
    // if (sourceInfo == null) {
    // sourceInfo = new SiteInfoType();
    // serie.setSourceInfo(sourceInfo);
    // }
    // SiteInfoType siteInfo = null;
    // if (sourceInfo instanceof SiteInfoType) {
    // siteInfo = (SiteInfoType) sourceInfo;
    // siteInfo.getSiteCode().clear();
    // SiteCode siteCode = new SiteCode();
    // // siteCode.setValue(location);
    // siteInfo.getSiteCode().add(siteCode);
    // String siteName = siteInfo.getSiteName();
    // if (siteName == null || siteName.equals("")) {
    // // siteInfo.setSiteName(location);
    // }
    // }
    // List<TsValuesSingleVariableType> values = serie.getValues();
    // boolean setGMTTimeZone = true;
    // for (TsValuesSingleVariableType value : values) {
    // List<ValueSingleVariable> innerValues = value.getValue();
    // // order must be ascendent in time for HydroDesktop
    // XMLGregorianCalendar tmpTime = null;
    // boolean reorderNeeded = false;
    // for (ValueSingleVariable innerValue : innerValues) {
    // XMLGregorianCalendar time = innerValue.getDateTime();
    // if (time == null) {
    // time = innerValue.getDateTimeUTC();
    // innerValue.setDateTime(time); // date time must be present because of the CUAHSI 1.1 schema!
    // innerValue.setTimeOffset(null);
    // } else {
    // setGMTTimeZone = false;
    // }
    // if (tmpTime == null) {
    // if (time == null) {
    // reorderNeeded = true;
    // break;
    // }
    // tmpTime = time;
    // } else {
    // int compare = tmpTime.compare(time);
    // switch (compare) {
    // case DatatypeConstants.GREATER:
    // reorderNeeded = true;
    // break;
    // default:
    // case DatatypeConstants.LESSER:
    // case DatatypeConstants.EQUAL:
    // case DatatypeConstants.INDETERMINATE:
    // break;
    // }
    // }
    // }
    // if (setGMTTimeZone) {
    // TimeZoneInfo timeZoneInfo = new TimeZoneInfo();
    // DefaultTimeZone defaultTimeZone = new DefaultTimeZone();
    // defaultTimeZone.setZoneOffset("00:00");
    // timeZoneInfo.setDefaultTimeZone(defaultTimeZone);
    // // no date time found, only date time UTC
    // // so it is safe to set time zone UTC
    // // this is useful for the WaterML R package
    // siteInfo.setTimeZoneInfo(timeZoneInfo);
    // }
    // if (reorderNeeded) {
    // innerValues.sort(new Comparator<ValueSingleVariable>() {
    //
    // @Override
    // public int compare(ValueSingleVariable o1, ValueSingleVariable o2) {
    // XMLGregorianCalendar t1 = o1.getDateTimeUTC();
    // if (t1 == null) {
    // t1 = o1.getDateTime();
    // }
    // XMLGregorianCalendar t2 = o2.getDateTimeUTC();
    // if (t2 == null) {
    // t2 = o2.getDateTime();
    // }
    // if (t1 != null && t2 != null) {
    // return t1.compare(t2);
    // } else {
    // return 0;
    // }
    // }
    // });
    //
    // }
    // }
    // }
    //
    // ObjectFactory factory = new ObjectFactory();
    // return timeSeries;
    // }

    public String getMethodName() {
	return "GetValues";
    }

}
