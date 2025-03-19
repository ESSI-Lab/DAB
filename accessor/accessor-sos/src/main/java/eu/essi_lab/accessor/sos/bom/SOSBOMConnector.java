package eu.essi_lab.accessor.sos.bom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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

import java.util.AbstractMap.SimpleEntry;

import javax.xml.bind.JAXBElement;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.accessor.sos.SOSConnector;
import eu.essi_lab.accessor.sos.SOSProperties;
import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.sos._2_0.GetFeatureOfInterestResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType.ObservationData;
import eu.essi_lab.jaxb.sos._2_0.gda.DataAvailabilityMemberType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractContentsType.Offering;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasureType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.om__2.TimeObjectPropertyType;
import eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class SOSBOMConnector extends SOSConnector {

    /**
     * 
     */
    public static final String TYPE = "SOS BOM Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    public static boolean isToBeSkipped(String procedure) {
	String[] endsWith = new String[] { "_1ST", "_P_1", "_PB_1", "_PC_1", "_P_C_1", "_V_1", "_C_Std_QaQc_1", "_C_Std_Qcf_1",
		"_C_Std_DMmanQcf_1", "_C_Std_DMmanQcf_1"//
		, "R_PF_1", "R_PF_2", "R_PF_3", "R_PF_4", "Pat3_C_PR01manQcf_1", "Pat3_PF_1", "Pat3_PF_1", "Pat4_D_F_1", "Pat9_P_Std_1",
		"Pat9_V_Std_1"//
		, "Pat4_D_1", "Pat4_D_F_1", "Pat4_C_Std_PR01ManQcf_1", "Pat4_C_Std_PR01ManQcf_HR", "Pat4_C_B_1_Hourly_PR01",
		"Pat4_C_Std_PR01ManQcf_09hr", "Pat4_C_B_1_Daily09_PR01", //
		"Pat6_PR02_ManQc_1", "Pat6_D_1", "Pat7_PR02_ManQc_1"

	};
	for (String end : endsWith) {
	    if (procedure.endsWith(end)) {
		return true;
	    }
	}

	return false;
    }

    public String getDownloadProtocol() {
	return NetProtocols.SOS_2_0_0_BOM.getCommonURN();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	getSOSCache();
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	try {
	    SOSProperties capMetadata = retrieveCapabilitiesMetadata();
	    capMetadata.setProperty(SOSProperty.DOWNLOAD_PROTOCOL, getDownloadProtocol());

	    String token = request.getResumptionToken();

	    if (token == null) { // the token is the number of procedure
		token = "0";
	    }
	    int offeringIndex = Integer.parseInt(token);

	    Optional<Integer> max = getSetting().getMaxRecords();

	    if (max.isPresent()) {
		int m = max.get();
		if (m > 0 && offeringIndex >= m) {
		    ret.setResumptionToken(null);
		    GSLoggerFactory.getLogger(SOSConnector.class).debug("Reached max records limit");
		    return ret;
		}
	    }

	    List<Offering> offerings = getSOSCache().getCapabilities().getContents().getContents().getOffering();
	    int offeringSize = offerings.size();

	    GSLoggerFactory.getLogger(getClass()).info("Serving offering " + offeringIndex + "/" + offeringSize);

	    Offering offering = offerings.get(offeringIndex);

	    AbstractOfferingType abstractOffering = offering.getAbstractOffering().getValue();

	    String procedure = abstractOffering.getProcedure();

	    String nextResumptionToken = null;

	    int nextOffering;
	    if (offeringIndex + 1 < offeringSize) {
		nextOffering = offeringIndex + 1;
		nextResumptionToken = "" + nextOffering;
	    } else {
		nextResumptionToken = null;
	    }
	    ret.setResumptionToken(nextResumptionToken);

	    if (isToBeSkipped(procedure)) {
		GSLoggerFactory.getLogger(getClass()).info("Skipping procedure {}: {}", token, procedure);
		return ret;
	    }

	    UnitReference uom = null;
	    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType interpolation = null;

	    List<String> procedureDescriptionFormats = getProcedureDescriptionFormats(abstractOffering);

	    GetFeatureOfInterestResponseType featureResponse;

	    featureResponse = getSOSCache().getFeaturesCache().get(procedure);

	    if (featureResponse == null) {
		featureResponse = retrieveFeatures(procedure);
		getSOSCache().getFeaturesCache().clear(); // only last retrieved is stored, otherwise it will become too
							  // big
		getSOSCache().getFeaturesCache().put(procedure, featureResponse);
	    }

	    List<FeaturePropertyType> features = featureResponse.getFeatureMember();

	    Map<String, AbstractFeatureType> featureMap = new HashMap<String, AbstractFeatureType>();
	    for (FeaturePropertyType feature : features) {
		AbstractFeatureType abstractFeature = feature.getAbstractFeature().getValue();
		String featureIdentifier = abstractFeature.getIdentifier().getValue();
		featureMap.put(featureIdentifier, abstractFeature);

	    }

	    // DESCRIBE SENSOR
	    HashMap<String, DescribeSensorResponseType> procedureDescriptions = retrieveProcedureDescriptions(procedure,
		    procedureDescriptionFormats);

	    String procedureTitle = procedure;
	    String procedureHref = procedure;

	    GetDataAvailabilityResponseType availabilityResponse = getSOSCache().getAvailabilityCache().get(procedure);

	    if (availabilityResponse == null) {
		availabilityResponse = retrieveDataAvailability(procedure);
		getSOSCache().getAvailabilityCache().clear(); // only last retrieved is stored, otherwise it will become
							      // too big
		getSOSCache().getAvailabilityCache().put(procedure, availabilityResponse);
	    }

	    List<DataAvailabilityMemberType> availabilityMembers = availabilityResponse.getDataAvailabilityMember();

	    for (DataAvailabilityMemberType availabilityMember : availabilityMembers) {
		String observedPropertyHref = availabilityMember.getObservedProperty().getHref();
		String observedPropertyTitle = availabilityMember.getObservedProperty().getTitle();

		ReferenceType proc = availabilityMember.getProcedure();
		if (proc != null) {
		    procedureTitle = proc.getTitle();
		    procedureHref = proc.getHref();
		}

		ReferenceType feature = availabilityMember.getFeatureOfInterest();
		String featureIdentifier = null;
		String featureName = null;
		if (feature != null) {
		    featureIdentifier = feature.getHref();
		    featureName = feature.getTitle();
		}

		SimpleEntry<String, String> temporal = getTemporal(availabilityMember);
		if (temporal != null) {

		    String begin = temporal.getKey();
		    Date beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(begin).get();
		    String end = temporal.getValue();
		    Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();
		    long fullPeriod = endDate.getTime() - beginDate.getTime();
		    Date recentStart = new Date(endDate.getTime() - fullPeriod / 10);

		    SOSProperties metadata = capMetadata.clone();
		    metadata.setProperty(SOSProperty.FOI_COUNTRY, Country.AUSTRALIA.getShortName());
		    metadata.setProperty(SOSProperty.SERVICE_PROVIDER_NAME, "Australian Government Bureau of Meteorology");
		    if (uom == null || interpolation == null) {
			InputStream stream = retrieveDataStream(procedure, featureIdentifier, observedPropertyHref, recentStart, endDate);
			Object object = JAXBSOS.getInstance().unmarshal(stream);
			if (object instanceof JAXBElement<?>) {
			    JAXBElement<?> jaxb = (JAXBElement<?>) object;
			    object = jaxb.getValue();
			}
			ObservationData sosObservation = null;
			if (object instanceof GetObservationResponseType) {
			    GetObservationResponseType dataResponse = (GetObservationResponseType) object;
			    List<ObservationData> observationDatas = dataResponse.getObservationData();
			    for (ObservationData observationData : observationDatas) {
				sosObservation = observationData;
				break;
			    }
			} else {
			    throw new RuntimeException("Error parsing SOS data");
			}
			eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory of = new ObjectFactory();
			JAXBElement<OMObservationType> omObservation = of.createOMObservation(sosObservation.getOMObservation());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBSOS.getInstance().marshal(omObservation, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType observation = JAXBWML2.getInstance().unmarshalObservation(bais);
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
					uom = tvMeta.getUom();
					interpolation = tvMeta.getInterpolationType();
				    }
				}
			    }
			}
		    }

		    if (uom != null) {
			String uomHref = uom.getHref();
			String uomCode = uom.getCode();
			String uomTitle = uom.getTitle();
			if (uomHref != null) {
			    metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_UOM_HREF, uomHref);
			}
			if (uomCode != null) {
			    metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_UOM_CODE, uomCode);
			}
			if (uomTitle != null) {
			    metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_UOM_TITLE, uomTitle);
			}
		    }

		    if (interpolation != null) {
			String interpolationHref = interpolation.getHref();
			String interpolationTitle = interpolation.getTitle();

			if (interpolationHref != null) {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, interpolationHref);
			} else {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, interpolationTitle);
			}
		    }

		    if (interpolation == null || interpolation.getHref().toLowerCase().contains("const")) {
			if (procedureHref.contains("Total")) {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, "Tot");
			}
			if (procedureHref.contains("Max")) {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, "Max");
			}
			if (procedureHref.contains("Mean")) {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, "Mean");
			}
			if (procedureHref.contains("Min")) {
			    metadata.setProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION, "Min");
			}
		    }
		    if (procedureHref.contains("Daily")) {
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_AGGREGATION, "P1D");
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_RESOLUTION, "P1D");
		    }
		    if (procedureHref.contains("Month")) {
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_AGGREGATION, "P1M");
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_RESOLUTION, "P1M");
		    }
		    if (procedureHref.contains("Hourly")) {
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_AGGREGATION, "PT1H");
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_RESOLUTION, "PT1H");
		    }
		    if (procedureHref.contains("Yearly")) {
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_AGGREGATION, "P1Y");
			metadata.setProperty(SOSProperty.PROCEDURE_TIME_RESOLUTION, "P1Y");
		    }

		    metadata.setProperty(SOSProperty.PROCEDURE_IDENTIFIER, procedure);
		    metadata.setProperty(SOSProperty.PROCEDURE_TITLE, procedureTitle);
		    metadata.setProperty(SOSProperty.PROCEDURE_HREF, procedureHref);

		    metadata.setProperty(SOSProperty.TEMP_EXTENT_BEGIN, temporal.getKey());
		    metadata.setProperty(SOSProperty.TEMP_EXTENT_END, temporal.getValue());

		    augmentWithProcedureDescriptions(metadata, procedureDescriptions);

		    AbstractFeatureType abstractFeature = featureMap.get(featureIdentifier);
		    augmentWithFeature(metadata, abstractFeature);
		    metadata.setProperty(SOSProperty.FOI_ID, featureIdentifier);
		    metadata.setProperty(SOSProperty.FOI_NAME, featureName);

		    metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_ID, observedPropertyHref);
		    metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_NAME, observedPropertyTitle);

		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setMetadata(metadata.asString());
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.SOS_2_0);
		    ret.addRecord(metadataRecord);

		    if (max.isPresent()) {
			int m = max.get();
			if (m > 0 && ret.getRecordsAsList().size() >= m) {
			    ret.setResumptionToken(null);
			    GSLoggerFactory.getLogger(SOSConnector.class).debug("Reached max records limit");
			    return ret;
			}
		    }

		}

	    }

	} catch (

	Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOC_CONNECTOR_LIST_RECORDS_ERROR, //
		    e);
	}

	return ret;
    }

}
