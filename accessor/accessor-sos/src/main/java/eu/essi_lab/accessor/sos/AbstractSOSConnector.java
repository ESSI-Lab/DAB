package eu.essi_lab.accessor.sos;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.*;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import dev.failsafe.FailsafeException;
import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.cdk.harvest.wrapper.WrappedConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.sos._2_0.CapabilitiesType;
import eu.essi_lab.jaxb.sos._2_0.GetFeatureOfInterestResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType.ObservationData;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType.PhenomenonTime;
import eu.essi_lab.jaxb.sos._2_0.gda.DataAvailabilityMemberType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;
import eu.essi_lab.jaxb.sos._2_0.gda.TimeObjectPropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.AddressType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.CodeType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ContactType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.OnlineResourceType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.Operation;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ResponsiblePartySubsetType;
import eu.essi_lab.jaxb.sos._2_0.ows_1.ServiceProvider;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractContentsType.Offering;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType.Description;
import eu.essi_lab.jaxb.sos._2_0.swes_2.SensorDescriptionType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.SensorDescriptionType.Data;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public abstract class AbstractSOSConnector extends WrappedConnector {

    protected static final String SOS_CONNECTOR_DOWNLOAD_ERROR = "SOS_CONNECTOR_DOWNLOAD_ERROR";
    protected static final String SOC_CONNECTOR_LIST_RECORDS_ERROR = "SOC_CONNECTOR_LIST_RECORDS_ERROR";

    public String getDownloadProtocol() {
	return NetProtocolWrapper.SOS_2_0_0.getCommonURN();
    }

    public SOSCache getSOSCache() {
	return SOSCacheManager.getInstance().getSOSCache(getSourceURL());

    }

    protected SimpleEntry<String, String> getTemporal(AbstractOfferingType abstractOffering) {
	// else data availability is inferred from capabilities
	if (abstractOffering instanceof ObservationOfferingType) {
	    ObservationOfferingType observationOffering = (ObservationOfferingType) abstractOffering;
	    PhenomenonTime phenomenonTime = observationOffering.getPhenomenonTime();
	    if (phenomenonTime != null) {
		TimePeriodType timePeriod = phenomenonTime.getTimePeriod();
		String begin = timePeriod.getBeginPosition().getValue().get(0);
		begin = normalizeDate(begin);
		String end = timePeriod.getEndPosition().getValue().get(0);
		end = normalizeDate(end);
		return new SimpleEntry<String, String>(begin, end);
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("empty phenomenon time");
	    }

	}
	return null;
    }

    protected SimpleEntry<String, String> getTemporal(DataAvailabilityMemberType availabilityMember) {
	TimeObjectPropertyType phenomenonTime = availabilityMember.getPhenomenonTime();
	if (phenomenonTime != null) {
	    JAXBElement<? extends AbstractTimeObjectType> abstractTime = phenomenonTime.getAbstractTimeObject();
	    if (abstractTime != null) {
		AbstractTimeObjectType timeObject = abstractTime.getValue();
		if (timeObject != null) {
		    if (timeObject instanceof TimePeriodType) {
			TimePeriodType timePeriod = (TimePeriodType) timeObject;
			String begin = timePeriod.getBeginPosition().getValue().get(0);
			begin = normalizeDate(begin);
			String end = timePeriod.getEndPosition().getValue().get(0);
			end = normalizeDate(end);
			return new SimpleEntry<String, String>(begin, end);

		    } else {
			GSLoggerFactory.getLogger(getClass()).warn("not a time period");
		    }
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn("empty time object");
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("empty abstract time");
	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).warn("empty phenomenon time");
	}
	return null;
    }

    protected void augmentWithProcedureDescriptions(SOSProperties metadata,
	    HashMap<String, DescribeSensorResponseType> procedureDescriptions) throws Exception {
	for (String format : procedureDescriptions.keySet()) {
	    DescribeSensorResponseType procedureDescription = procedureDescriptions.get(format);
	    if (procedureDescription != null) {
		format = procedureDescription.getProcedureDescriptionFormat();
		List<Description> descriptions = procedureDescription.getDescription();
		if (descriptions != null && !descriptions.isEmpty()) {
		    Description description = descriptions.get(0);
		    if (description != null) {
			SensorDescriptionType sensorDescriptionObject = description.getSensorDescription();
			if (sensorDescriptionObject != null) {
			    Data data = sensorDescriptionObject.getData();
			    if (data != null) {
				Object any = data.getAny();
				if (any != null) {
				    if (any instanceof Node) {
					Node node = (Node) any;
					XMLNodeReader reader = new XMLNodeReader(node);
					if (format != null && format.equals("http://www.opengis.net/sensorml/2.0")) {
					    // SENSOR ML 2.0
					    metadata.setProperty(SOSProperty.SENSOR_UniqueId,
						    reader.evaluateString("/*:PhysicalSystem/*:identifier"));
					    Node[] keywordNodes = reader.evaluateNodes(
						    "/*:PhysicalSystem/*:keywords/*:KeywordList/*:keyword");
					    String sensorKeywords = "";
					    if (keywordNodes != null) {
						for (Node keywordNode : keywordNodes) {
						    String textContent = keywordNode.getTextContent();
						    if (textContent != null && !textContent.isBlank() && !textContent.isEmpty()) {
							sensorKeywords += textContent + ";";
						    }
						}
					    }
					    sensorKeywords += reader.evaluateString("/*:PhysicalSystem/*:identifier");
					    metadata.setProperty(SOSProperty.SENSOR_Keywords, sensorKeywords);
					    if (this.getSourceURL().contains("hn4s.hydronet.com")) {
						Node[] contactNodes = reader.evaluateNodes(
							"/*:PhysicalSystem/*:contacts/*:ContactList/*:contact");
						if (contactNodes != null) {
						    for (Node contact : contactNodes) {
							XMLNodeReader contactReader = new XMLNodeReader(contact);
							metadata.setProperty(SOSProperty.SENSOR_ContactOwnerOrganization,
								contactReader.evaluateString(
									"//*:CI_ResponsibleParty/*:organisationName/*:CharacterString"));

							metadata.setProperty(SOSProperty.SENSOR_ContactOwnerHomepage,
								"https://tahmo.org/twiga/");

							metadata.setProperty(SOSProperty.SENSOR_ContactOwnerAddressEmail,
								contactReader.evaluateString(
									"//*:CI_ResponsibleParty/*:contactInfo/*:CI_Contact/*:address/*:CI_Address/*:electronicMailAddress/*:CharacterString"));
						    }
						}
					    }

					} else {
					    // SENSOR ML 1.0
					    metadata.setProperty(SOSProperty.SENSOR_Name,
						    reader.evaluateString("/*:SensorML/*:member/*/*:name"));
					    metadata.setProperty(SOSProperty.SENSOR_Description,
						    reader.evaluateString("/*:SensorML/*:member/*/*:description"));
					    Node[] keywordNodes = reader.evaluateNodes(
						    "/*:SensorML/*:member/*/*:keywords/*:KeywordList/*:keyword");
					    String sensorKeywords = "";
					    if (keywordNodes != null) {
						for (Node keywordNode : keywordNodes) {
						    sensorKeywords += keywordNode.getTextContent() + ";";
						}
					    }
					    metadata.setProperty(SOSProperty.SENSOR_Keywords, sensorKeywords);
					    metadata.setProperty(SOSProperty.SENSOR_UniqueId, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:identification/*:IdentifierList/*:identifier[@name='uniqueID']/*:Term/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_ManufacturerName, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:identification/*:IdentifierList/*:identifier[@name='Manufacturer Name']/*:Term/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_ShortName, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:identification/*:IdentifierList/*:identifier[@name='shortName']/*:Term/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_LongName, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:identification/*:IdentifierList/*:identifier[@name,'longName']/*:Term/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_Material, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:capabilities/*:DataRecord/*:field[@name='material']/*:Category/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_Status, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:capabilities[@name='Status Capabilities']/*:SimpleDataRecord/*:field[@name='status']/*:Boolean/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_Mobile, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:capabilities[@name='Status Capabilities']/*:SimpleDataRecord/*:field[@name='mobile']/*:Boolean/*:value"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactManufacturerOrganization, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:manufacturer']/*:ResponsibleParty/*:organizationName"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerOrganization, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:organizationName"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerPhone, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:phone/*:voice"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerAddressDeliveryPoint, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:address/*:deliveryPoint"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerAddressCity, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:address/*:city"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerAddressCountry, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:address/*:country"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerAddressEmail, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:address/*:electronicMailAddress"));
					    metadata.setProperty(SOSProperty.SENSOR_ContactOwnerHomepage, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:contact[@*:arcrole='urn:ogc:def:classifiers:OGC:contactType:owner']/*:ResponsibleParty/*:contactInfo/*:onlineResource/@*:href"));
					    String image = reader.evaluateString(
						    "/*:SensorML/*:member/*/*:documentation[@*:arcrole='urn:ogc:def:object:OGC:1.0:image']/*:Document/*:onlineResource/@*:href");
					    image = resolveImage(image);
					    metadata.setProperty(SOSProperty.SENSOR_DocumentationImage, image);
					    metadata.setProperty(SOSProperty.SENSOR_DocumentationImageFormat, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:documentation[@*:arcrole='urn:ogc:def:object:OGC:1.0:image']/*:Document/*:format"));
					    metadata.setProperty(SOSProperty.SENSOR_DocumentationImageDescription, reader.evaluateString(
						    "/*:SensorML/*:member/*/*:documentation[@*:arcrole='urn:ogc:def:object:OGC:1.0:image']/*:Document/*:description"));
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}

    }

    protected void augmentWithFeature(SOSProperties metadata, AbstractFeatureType abstractFeature) {
	if (abstractFeature instanceof SFSpatialSamplingFeatureType) {
	    SFSpatialSamplingFeatureType monitoring = (SFSpatialSamplingFeatureType) abstractFeature;
	    AbstractGeometryType abstractGeometry = monitoring.getShape().getAbstractGeometry().getValue();
	    if (abstractGeometry instanceof PointType) {
		PointType point = (PointType) abstractGeometry;
		List<Double> points = point.getPos().getValue();
		if (points != null && !points.isEmpty()) {
		    if (isLatLon()) {
			metadata.setProperty(SOSProperty.LATITUDE, "" + points.get(0));
			metadata.setProperty(SOSProperty.LONGITUDE, "" + points.get(1));
		    } else {
			metadata.setProperty(SOSProperty.LATITUDE, "" + points.get(1));
			metadata.setProperty(SOSProperty.LONGITUDE, "" + points.get(0));
		    }
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("not a point type");
	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).warn("not a monitoring type feature");
	}

    }

    protected SOSProperties retrieveCapabilitiesMetadata() throws Exception {

	if (getSOSCache().getCapabilities() == null) {
	    CapabilitiesType caps = retrieveCapabilities();
	    getSOSCache().setCapabilities(caps);
	}

	ServiceProvider serviceProvider = getSOSCache().getCapabilities().getServiceProvider();
	String providerName = null;
	String providerSite = null;
	String providerIndividualName = null;
	String providerAddressDeliveryPoint = null;
	String providerAddressCity = null;
	String providerAddressAdministrativeArea = null;
	String providerAddressPostalCode = null;
	String providerAddressCountry = null;
	String providerAddressEmail = null;
	String providerRole = null;

	if (serviceProvider != null) {
	    providerName = serviceProvider.getProviderName();
	    OnlineResourceType site = serviceProvider.getProviderSite();
	    if (site != null) {
		providerSite = site.getHref();
	    }
	    ResponsiblePartySubsetType contact = serviceProvider.getServiceContact();
	    if (contact != null) {
		providerIndividualName = contact.getIndividualName();
		ContactType info = contact.getContactInfo();
		CodeType role = contact.getRole();
		if (role != null) {
		    providerRole = role.getValue();
		}
		AddressType address = info.getAddress();
		if (address != null) {
		    List<String> dps = address.getDeliveryPoint();
		    if (dps != null && !dps.isEmpty()) {
			providerAddressDeliveryPoint = dps.get(0);
		    }
		    providerAddressCity = address.getCity();
		    providerAddressAdministrativeArea = address.getAdministrativeArea();
		    providerAddressPostalCode = address.getPostalCode();
		    providerAddressCountry = address.getCountry();
		    List<String> emails = address.getElectronicMailAddress();
		    if (emails != null && !emails.isEmpty()) {
			providerAddressEmail = emails.get(0);
		    }
		}
	    }
	}
	SOSProperties metadata = new SOSProperties();
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_NAME, providerName);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_SITE, providerSite);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_IndividualName, providerIndividualName);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressDeliveryPoint, providerAddressDeliveryPoint);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressCity, providerAddressCity);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressAdministrativeArea, providerAddressAdministrativeArea);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressPostalCode, providerAddressPostalCode);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressCountry, providerAddressCountry);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_AddressEmailAddress, providerAddressEmail);
	metadata.setProperty(SOSProperty.SERVICE_PROVIDER_ROLE, providerRole);
	return metadata;
    }

    @Override

    public boolean supports(GSSource source) {
	try {
	    CapabilitiesType capabilities = retrieveCapabilities(source.getEndpoint());
	    if (capabilities == null) {
		return false;
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    return false;
	}

    }

    public CapabilitiesType retrieveCapabilities() throws Exception {
	return retrieveCapabilities(getSourceURL());
    }

    public CapabilitiesType retrieveCapabilities(String url) throws Exception {

	SOSRequestBuilder builder = createRequestBuilder();
	String capabilitiesEndpoint = builder.createCapabilitiesRequest();

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	tmpFile.deleteOnExit();
	try {
	    InputStream stream = downloadStreamWithRetry(capabilitiesEndpoint);
	    if (stream == null) {
		throw new RuntimeException("Error downloading SOS capabilities");
	    }
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(stream, fos);
	    GSLoggerFactory.getLogger(getClass()).info("Downloaded capabilities to: " + tmpFile.getAbsolutePath());
	    stream.close();

	} catch (GSException gse) {

	    throw gse;

	} catch (Exception e) {

	    throw new RuntimeException("Error downloading SOS capabilities");
	}

	modifyCapabilitiesResponse(tmpFile);

	try {

	    Object object = unmarshal(tmpFile);

	    if (object instanceof JAXBElement<?>) {
		JAXBElement<?> jaxb = (JAXBElement<?>) object;
		object = jaxb.getValue();
	    }
	    if (object instanceof CapabilitiesType) {
		CapabilitiesType capabilities = (CapabilitiesType) object;
		return capabilities;
	    } else {
		throw new RuntimeException("Error parsing SOS capabilities");
	    }
	} catch (

		Exception e) {
	    throw new RuntimeException("Error downloading SOS capabilities");
	} finally {
	    tmpFile.delete();
	}

    }

    /**
     * Sub classes might modify the downloaded file e.g. to make it XML valid
     *
     * @param tmpFile
     * @throws Exception
     */

    public void modifyCapabilitiesResponse(File tmpFile) throws Exception {

    }

    /**
     * Sub classes might modify the downloaded file e.g. to make it XML valid
     *
     * @param tmpFile
     * @throws Exception
     */

    public InputStream modifyObservationResponse(InputStream stream) throws Exception {
	return stream;
    }

    /**
     * Sub classes might modify the feature response stream e.g. to make it XML valid
     *
     * @param stream
     * @throws Exception
     */

    public InputStream modifyFeatureResponse(InputStream stream) throws Exception {
	return stream;
    }

    public Object unmarshal(File tmpFile) throws Exception {
	return JAXBSOS.getInstance().unmarshal(tmpFile);
    }

    public Object unmarshal(InputStream stream) throws Exception {
	return JAXBSOS.getInstance().unmarshal(stream);
    }

    private String cachedProcedure = null;
    private HashMap<String, DescribeSensorResponseType> cachedProcedureDescription = null;

    /**
     * Returns a map procedure description format to procedure description, describing a procedure with all the available formats
     *
     * @param procedure
     * @param procedureDescriptionFormats
     * @return
     */

    protected HashMap<String, DescribeSensorResponseType> retrieveProcedureDescriptions(String procedure,
	    List<String> procedureDescriptionFormats) {
	if (cachedProcedure != null && cachedProcedure.equals(procedure)) {
	    return cachedProcedureDescription;
	}
	HashMap<String, DescribeSensorResponseType> ret = new HashMap<>();
	if (procedureDescriptionFormats == null || procedureDescriptionFormats.isEmpty()) {
	    procedureDescriptionFormats.add("http://www.opengis.net/sensorML/1.0.1");
	}

	SOSRequestBuilder builder = createRequestBuilder();

	for (String procedureDescriptionFormat : procedureDescriptionFormats) {
	    String describeSensorEndpoint = builder.createProcedureDescriptionRequest(procedure, procedureDescriptionFormat);
	    try {
		InputStream stream = downloadStreamWithRetry(describeSensorEndpoint);
		if (stream == null) {
		    throw new RuntimeException("Error downloading SOS features");
		}
		Object object = unmarshal(stream);
		if (object instanceof JAXBElement<?>) {
		    JAXBElement<?> jaxb = (JAXBElement<?>) object;
		    object = jaxb.getValue();
		}
		if (object instanceof DescribeSensorResponseType) {
		    DescribeSensorResponseType describeResponse = (DescribeSensorResponseType) object;
		    ret.put(procedureDescriptionFormat, describeResponse);
		} else {
		    throw new RuntimeException("Error parsing SOS features");
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Sensor description retrieval failed for format: " + procedureDescriptionFormat);
	    }
	}
	this.cachedProcedure = null;
	this.cachedProcedureDescription = null;
	this.cachedProcedureDescription = ret;
	this.cachedProcedure = procedure;
	return ret;
    }

    public GetFeatureOfInterestResponseType retrieveFeatures(String procedure) throws Exception {

	SOSRequestBuilder builder = createRequestBuilder();

	String featureRequest = builder.createFeaturesRequest(procedure);
	try {
	    InputStream stream = downloadStreamWithRetry(featureRequest);
	    if (stream == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Error downloading features for procedure: " + procedure);
		return new GetFeatureOfInterestResponseType();
	    }
	    stream = modifyFeatureResponse(stream);

	    Object object = unmarshal(stream);
	    if (object instanceof JAXBElement<?>) {
		JAXBElement<?> jaxb = (JAXBElement<?>) object;
		object = jaxb.getValue();
	    }
	    if (object instanceof GetFeatureOfInterestResponseType) {
		GetFeatureOfInterestResponseType foiResponse = (GetFeatureOfInterestResponseType) object;
		return foiResponse;
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Error parsing features for procedure: " + procedure);
		return new GetFeatureOfInterestResponseType();
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Error downloading features for procedure: " + procedure);
	    return new GetFeatureOfInterestResponseType();
	}

    }

    public GetDataAvailabilityResponseType retrieveDataAvailability(String procedure) {
	return retrieveDataAvailability(procedure, null, null);
    }

    public GetDataAvailabilityResponseType retrieveDataAvailability(String procedure, String featureIdentifier) {
	return retrieveDataAvailability(procedure, featureIdentifier, null);
    }

    public GetDataAvailabilityResponseType retrieveDataAvailability(String procedure, String featureIdentifier, String observedProperty) {

	SOSRequestBuilder builder = createRequestBuilder();

	String dataAvEndpoint = builder.createDataAvailabilityRequest(procedure, featureIdentifier, observedProperty);

	try {
	    InputStream stream = downloadStreamWithRetry(dataAvEndpoint);
	    if (stream == null) {
		throw new RuntimeException("Error downloading SOS data availability");
	    }
	    Object object = unmarshal(stream);
	    if (object instanceof JAXBElement<?>) {
		JAXBElement<?> jaxb = (JAXBElement<?>) object;
		object = jaxb.getValue();
	    }
	    if (object instanceof GetDataAvailabilityResponseType) {
		GetDataAvailabilityResponseType foiResponse = (GetDataAvailabilityResponseType) object;
		return foiResponse;
	    } else {
		throw new RuntimeException("Error parsing SOS data availability");
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Error downloading SOS data availability");
	}
    }

    public List<OMObservationType> retrieveData(String procedure, String featureIdentifier, String property, Date begin, Date end) {

	try {
	    InputStream stream = retrieveDataStream(procedure, featureIdentifier, property, begin, end);
	    if (stream == null) {
		throw new RuntimeException("Error downloading SOS data");
	    }

	    stream = modifyObservationResponse(stream);
	    Object object = unmarshal(stream);
	    if (object instanceof JAXBElement<?>) {
		JAXBElement<?> jaxb = (JAXBElement<?>) object;
		object = jaxb.getValue();
	    }
	    if (object instanceof GetObservationResponseType) {
		GetObservationResponseType dataResponse = (GetObservationResponseType) object;
		List<ObservationData> observationDatas = dataResponse.getObservationData();
		List<OMObservationType> ret = new ArrayList<>();
		for (ObservationData observationData : observationDatas) {
		    ret.add(observationData.getOMObservation());
		}
		return ret;
	    } else {
		throw new RuntimeException("Error parsing SOS data");
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw new RuntimeException("Error downloading SOS data");
	}
    }

    public InputStream retrieveDataStream(String procedure, String featureIdentifier, String property, Date begin, Date end) {

	SOSRequestBuilder builder = createRequestBuilder();
	String dataRequest = builder.createDataRequest(procedure, featureIdentifier, property, begin, end);

	try {
	    InputStream stream = downloadStreamWithRetry(dataRequest);
	    if (stream == null) {
		throw new RuntimeException("Error downloading SOS data");
	    }

	    stream = modifyObservationResponse(stream);
	    return stream;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw new RuntimeException("Error downloading SOS data");
	}
    }

    public boolean hasGetDataAvailabilityOperation() throws Exception {
	if (getSOSCache().getCapabilities() == null) {
	    CapabilitiesType caps = retrieveCapabilities();
	    getSOSCache().setCapabilities(caps);
	}
	List<Operation> operations = getSOSCache().getCapabilities().getOperationsMetadata().getOperation();
	for (Operation operation : operations) {
	    String name = operation.getName();
	    if (name != null && name.equals("GetDataAvailability")) {
		return true;
	    }
	}
	return false;
    }

    public List<String> getProcedureDescriptionFormats(AbstractOfferingType abstractOffering) {
	return abstractOffering.getProcedureDescriptionFormat();
    }

    protected String normalizeDate(String date) {
	Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(date);
	if (parsed.isPresent()) {
	    return ISO8601DateTimeUtils.getISO8601DateTime(parsed.get());
	} else {
	    return null;
	}
    }

    public String resolveImage(String image) {
	if (image != null && !image.equals("")) {
	    String lower = image.toLowerCase();
	    if (!lower.endsWith("jpg") && //
		    !lower.endsWith("jpeg") && //
		    !lower.endsWith("png") && //
		    !lower.endsWith("gif") && //
		    !lower.endsWith("tif")) {

		if (image.startsWith("http://")) {
		    try {
			HttpResponse<InputStream> response = downloadResponseWithRetry(image);

			List<String> cTypes = response.headers().allValues("Content-type");
			boolean imageFound = cTypes.stream().anyMatch(v -> v.contains("image"));

			if (!imageFound) {
			    XMLReader tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			    tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			    InputSource input = new InputSource(response.body());
			    SAXSource source = new SAXSource(tagsoupReader, input);
			    DOMResult result = new DOMResult();

			    TransformerFactory transformerFactory = TransformerFactory.newInstance();

			    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

			    Transformer transformer = transformerFactory.newTransformer();
			    
			    transformer.transform(source, result);

			    XMLDocumentReader reader = new XMLDocumentReader((Document) result.getNode());
			    String imgSrc = reader.evaluateString("//*:img[@id='theMainImage']/@src");
			    if (imgSrc == null) {
				imgSrc = reader.evaluateString("//*:img[1]/@src");
			    }
			    if (imgSrc != null) {
				URL url = new URL(image);
				URL imageURL = new URL(url, imgSrc);
				image = imageURL.toString();
			    }
			}
		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).warn("Not possible to resolve image");
		    }
		}
	    }
	}
	return image;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(CommonNameSpaceContext.SOS_2_0);
    }

    /**
     * @param url
     * @return
     * @throws Exception
     */
    public InputStream downloadStreamWithRetry(String url) throws Exception {
	HttpResponse<InputStream> response = downloadResponseWithRetry(url);
	InputStream stream = response.body();
	GSLoggerFactory.getLogger(getClass()).info("Got " + url);
	return stream;
    }

    /**
     * @param url
     * @return
     * @throws GSException
     */
    protected HttpResponse<InputStream> downloadResponseWithRetry(String url) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);
	downloader.setRetryPolicy(5, TimeUnit.SECONDS, 5);

	try {

	    return downloader.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url.trim()));

	} catch (FailsafeException | IOException | InterruptedException | URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_CONNECTOR_DOWNLOAD_ERROR);
	}
    }

    /**
     * @param procedure
     * @return
     */
    public AbstractOfferingType getOffering(String procedure) {
	List<Offering> offerings = getSOSCache().getCapabilities().getContents().getContents().getOffering();
	for (Offering offering : offerings) {
	    AbstractOfferingType abstractOffering = offering.getAbstractOffering().getValue();
	    String myProcedure = abstractOffering.getProcedure();
	    if (myProcedure.equals(procedure)) {
		return abstractOffering;
	    }
	}
	return null;
    }

    protected boolean isLatLon() {
	return true;
    }

    /**
     * @return
     */
    public SOSRequestBuilder createRequestBuilder() {

	return new SOSRequestBuilder(getSourceURL(), "2.0.0");
    }

}
