package eu.essi_lab.accessor.sos;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.sos._2_0.GetFeatureOfInterestResponseType;
import eu.essi_lab.jaxb.sos._2_0.gda.DataAvailabilityMemberType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractContentsType.Offering;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class SOSConnector extends AbstractSOSConnector {

	/**
	 * 
	 */
	public static final String TYPE = "SOS Connector";

	@Override
	public String getType() {

		return TYPE;
	}

	/**
	 * 
	 */
	public SOSConnector() {

	}

	@Override
	public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
		getSOSCache();
		ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
		try {
			SOSProperties capMetadata = retrieveCapabilitiesMetadata();
			capMetadata.setProperty(SOSProperty.DOWNLOAD_PROTOCOL, getDownloadProtocol());

			String token = request.getResumptionToken();

			SOSTokenMangler sosToken = new SOSTokenMangler();

			if (token == null) {

				sosToken.setOfferingIndex("0");
				sosToken.setFeatureIndex("0");

			} else {
				sosToken.setMangling(token);
			}

			String offeringIndexStr = sosToken.getOfferingIndex();

			int offeringIndex = Integer.parseInt(offeringIndexStr);

			String featureIndexStr = sosToken.getFeatureIndex();

			int featureIndex = Integer.parseInt(featureIndexStr);

			Optional<Integer> max = getSetting().getMaxRecords();

			if (max.isPresent()) {
				int m = max.get();
				if (m > 0 && (featureIndex >= m || offeringIndex >= m)) {
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
			// features = features.subList(0, 1);
			int featuresSize = features.size();

			SOSTokenMangler nextMangler = new SOSTokenMangler();
			String nextResumptionToken = null;

			int nextOffering;
			int nextFeature;
			if (featureIndex + 1 < featuresSize) {
				nextOffering = offeringIndex;
				nextFeature = featureIndex + 1;
				nextMangler.setOfferingIndex("" + nextOffering);
				nextMangler.setFeatureIndex("" + nextFeature);
				nextResumptionToken = nextMangler.getMangling();
			} else {
				if (offeringIndex + 1 < offeringSize) {
					nextOffering = offeringIndex + 1;
					nextFeature = 0;
					nextMangler.setOfferingIndex("" + nextOffering);
					nextMangler.setFeatureIndex("" + nextFeature);
					nextResumptionToken = nextMangler.getMangling();
				} else {
					nextResumptionToken = null;
				}
			}

			if (featuresSize > 0) {
				GSLoggerFactory.getLogger(getClass()).info("Serving offering " + offeringIndex + "/" + offeringSize
						+ " (feature " + featureIndex + "/" + featuresSize + ")");

				FeaturePropertyType feature = features.get(featureIndex);

				AbstractFeatureType abstractFeature = feature.getAbstractFeature().getValue();

				String featureIdentifier = abstractFeature.getIdentifier().getValue();
				String featureName = abstractFeature.getName().get(0).getValue();

				List<String> offeringObservableProperties = abstractOffering.getObservableProperty();
				Set<String> observableProperties = new HashSet<String>(offeringObservableProperties);

				if (offeringObservableProperties.size() != observableProperties.size()) {
					GSLoggerFactory.getLogger(getClass()).warn("Duplicate observable properties detected");
				}
				// List<String> observableProperties = abstractOffering.getObservableProperty();

				if (observableProperties.isEmpty()) {
					// in case observable properties list is empty, those are inherited from
					// contents
					observableProperties = new HashSet<String>(
							getSOSCache().getCapabilities().getContents().getContents().getObservableProperty());
				}

				HashMap<String, DescribeSensorResponseType> procedureDescriptions = retrieveProcedureDescriptions(
						procedure, procedureDescriptionFormats);

				String procedureTitle = procedure;
				String procedureHref = procedure;

				for (String observableProperty : observableProperties) {

					List<SimpleEntry<String, String>> temporals = new ArrayList<>();

					String observedPropertyHref = observableProperty;
					String observedPropertyTitle = observableProperty;

					if (hasGetDataAvailabilityOperation()) {

						GetDataAvailabilityResponseType availabilityResponse = retrieveDataAvailability(procedure,
								featureIdentifier, observableProperty);

						List<DataAvailabilityMemberType> availabilityMembers = availabilityResponse
								.getDataAvailabilityMember();

						for (DataAvailabilityMemberType availabilityMember : availabilityMembers) {
							observedPropertyHref = availabilityMember.getObservedProperty().getHref();
							observedPropertyTitle = availabilityMember.getObservedProperty().getTitle();

							ReferenceType proc = availabilityMember.getProcedure();
							if (proc != null) {
								procedureTitle = proc.getTitle();
								procedureHref = proc.getHref();
							}

							temporals.add(getTemporal(availabilityMember));

						}
					} else {

						temporals.add(getTemporal(abstractOffering));

					}

					for (SimpleEntry<String, String> temporal : temporals) {

						if (temporal == null) {
							continue;
						}
						SOSProperties metadata = capMetadata.clone();

						metadata.setProperty(SOSProperty.PROCEDURE_IDENTIFIER, procedure);
						metadata.setProperty(SOSProperty.PROCEDURE_TITLE, procedureTitle);
						metadata.setProperty(SOSProperty.PROCEDURE_HREF, procedureHref);

						metadata.setProperty(SOSProperty.TEMP_EXTENT_BEGIN, temporal.getKey());
						metadata.setProperty(SOSProperty.TEMP_EXTENT_END, temporal.getValue());

						augmentWithProcedureDescriptions(metadata, procedureDescriptions);

						augmentWithFeature(metadata, abstractFeature);
						metadata.setProperty(SOSProperty.FOI_ID, featureIdentifier);
						metadata.setProperty(SOSProperty.FOI_NAME, featureName);

						metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_ID, observedPropertyHref);
						metadata.setProperty(SOSProperty.OBSERVED_PROPERTY_NAME, observedPropertyTitle);

						OriginalMetadata metadataRecord = new OriginalMetadata();
						metadataRecord.setMetadata(metadata.asString());
						metadataRecord.setSchemeURI(CommonNameSpaceContext.SOS_2_0);
						ret.addRecord(metadataRecord);

					}
				}

			}

			ret.setResumptionToken(nextResumptionToken);

		} catch (Exception e) {
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

	public static void main(String[] args) throws Exception {
		SOSConnector connector = new SOSConnector();
		connector.setSourceURL("http://savahis.org/his/waterml?");
		ListRecordsRequest request = new ListRecordsRequest();
		ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
		OriginalMetadata record = response.getRecords().next();
		String metadata = record.getMetadata();
		System.out.println(metadata);
	}

}
