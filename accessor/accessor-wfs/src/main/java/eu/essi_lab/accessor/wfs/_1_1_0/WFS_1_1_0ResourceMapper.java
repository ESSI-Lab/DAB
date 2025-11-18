package eu.essi_lab.accessor.wfs._1_1_0;

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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.ows.v_1_0_0.AddressType;
import net.opengis.ows.v_1_0_0.ContactType;
import net.opengis.ows.v_1_0_0.DCP;
import net.opengis.ows.v_1_0_0.HTTP;
import net.opengis.ows.v_1_0_0.KeywordsType;
import net.opengis.ows.v_1_0_0.OnlineResourceType;
import net.opengis.ows.v_1_0_0.Operation;
import net.opengis.ows.v_1_0_0.OperationsMetadata;
import net.opengis.ows.v_1_0_0.RequestMethodType;
import net.opengis.ows.v_1_0_0.ResponsiblePartySubsetType;
import net.opengis.ows.v_1_0_0.ServiceProvider;
import net.opengis.ows.v_1_0_0.TelephoneType;
import net.opengis.ows.v_1_0_0.WGS84BoundingBoxType;
import net.opengis.wfs.v_1_1_0.FeatureTypeType;
import net.opengis.wfs.v_1_1_0.WFSCapabilitiesType;

/**
 * @author boldrini
 */
public class WFS_1_1_0ResourceMapper extends OriginalIdentifierMapper {

    private static final String WMS_MAPPER_MAP_ERROR = "WMS_MAPPER_MAP_ERROR";
    @XmlTransient
    Downloader downloader = new Downloader();

    public WFS_1_1_0ResourceMapper() {
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {

	    JAXBElement<?> jaxbElement = (JAXBElement<?>) WFS_1_1_0Connector.unmarshaller.unmarshal(new ByteArrayInputStream(//
		    resource.getOriginalMetadata().//
			    getMetadata().getBytes(StandardCharsets.UTF_8)));

	    FeatureTypeType feature = ((WFSCapabilitiesType) jaxbElement.getValue()).getFeatureTypeList().getFeatureType().getFirst();

	    return getFeatureName(feature);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	try {

	    JAXBElement<?> jaxbElement = (JAXBElement<?>) WFS_1_1_0Connector.unmarshaller.unmarshal(
		    new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    String endpoint = source.getEndpoint();

	    GSResource resource = mapResource((WFSCapabilitiesType) jaxbElement.getValue(), endpoint);
	    resource.setSource(source);

	    return resource;

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WMS_MAPPER_MAP_ERROR, //
		    e);
	}

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.WFS_1_1_0_NS_URI;
    }

    public void setDownloader(Object object) {

	this.downloader = null;
    }

    /**
     * @param feature
     * @return
     */
    private String getFeatureName(FeatureTypeType feature) {

	QName qName = feature.getName();

	String namespaceURI = qName.getNamespaceURI();

	return namespaceURI == null ? qName.getLocalPart() : qName.getPrefix() + ":" + qName.getLocalPart();
    }

    private GSResource mapResource(WFSCapabilitiesType capabilities, String sourceEndpoint) throws Exception {

	MIMetadata metadata = new MIMetadata();

	GSResource ret = new Dataset();

	FeatureTypeType feature = capabilities.getFeatureTypeList().getFeatureType().getFirst();

	String featureName = getFeatureName(feature);

	HarmonizedMetadata harmonizedMetadata = ret.getHarmonizedMetadata();
	CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();

	coreMetadata.setMIMetadata(metadata);

	ServiceProvider serviceProvider = capabilities.getServiceProvider();
	if (serviceProvider != null) {
	    ResponsiblePartySubsetType contactInformation = serviceProvider.getServiceContact();

	    ExtensionHandler extendedMetadataHandler = ret.getExtensionHandler();

	    ResponsibleParty contact = new ResponsibleParty();
	    if (contactInformation != null) {
		contact.setIndividualName(contactInformation.getIndividualName());
		contact.setPositionName(contactInformation.getPositionName());
		contact.setOrganisationName(serviceProvider.getProviderName());

		if (serviceProvider.getProviderName() != null) {
		    // extendedMetadataHandler.addOriginatorOrganisationIdentifier(originatorOrganisation.getKey());
		    extendedMetadataHandler.addOriginatorOrganisationDescription(serviceProvider.getProviderName());
		}

		Address iaddress = new Address();
		ContactType contactInfo = contactInformation.getContactInfo();
		Contact ci = new Contact();
		contact.setContactInfo(ci);
		ci.setAddress(iaddress);
		if (contactInfo != null) {

		    AddressType address = contactInfo.getAddress();
		    if (address != null) {

			List<String> deliveryPoints = address.getDeliveryPoint();
			if (deliveryPoints != null)
			    for (String deliveryPoint : deliveryPoints) {
				iaddress.addDeliveryPoint(deliveryPoint);
			    }

			iaddress.setAdministrativeArea(address.getAdministrativeArea());
			iaddress.setCity(address.getCity());
			iaddress.setPostalCode(address.getPostalCode());
			iaddress.setCountry(address.getCountry());
			List<String> emails = address.getElectronicMailAddress();
			if (emails != null)
			    for (String email : emails) {
				iaddress.addElectronicMailAddress(email);
			    }

		    }

		    TelephoneType phone = contactInfo.getPhone();

		    if (phone != null) {
			List<String> voices = phone.getVoice();
			if (voices != null)
			    for (String voice : voices) {
				ci.addPhoneVoice(voice);
			    }
			List<String> faxes = phone.getFacsimile();
			if (faxes != null)
			    for (String fax : faxes) {
				ci.addPhoneFax(fax);
			    }
		    }

		    OnlineResourceType site = serviceProvider.getProviderSite();
		    if (site != null) {
			Online online = new Online();
			online.setLinkage(site.getHref());
			online.setDescription(site.getTitle());
			ci.setOnline(online);
		    }

		}

	    }
	    metadata.addContact(contact);
	}

	List<String> crses = new ArrayList<>();
	if (feature.getDefaultSRS() != null)
	    crses.add(feature.getDefaultSRS());
	if (feature.getOtherSRS() != null)
	    for (String crs : feature.getOtherSRS()) {
		crses.add(crs);
	    }

	if (crses != null && !crses.isEmpty()) {
	    for (String crs : crses) {
		ReferenceSystem ref = new ReferenceSystem();
		ref.setCode(crs);
		metadata.addReferenceSystemInfo(ref);
	    }
	} else {
	    if (featureName != null) {
		System.err.println("No CRS specified for this layer: " + feature.getName().toString());
	    }
	}

	DataIdentification identification = new DataIdentification();

	String id;
	if (featureName == null || featureName.isEmpty()) {
	    id = UUID.randomUUID().toString();
	} else {
	    id = featureName;
	}

	identification.setResourceIdentifier(id);

	metadata.addDataIdentification(identification);

	identification.setCitationTitle(feature.getTitle());

	// ABSTRACT
	String abs = feature.getAbstract();
	if (abs != null) {
	    identification.setAbstract(abs);
	}

	// KEYWORD
	HashSet<String> finalKeywords = new HashSet<>();
	List<KeywordsType> keywords = feature.getKeywords();
	if (keywords != null && !keywords.isEmpty()) {
	    for (KeywordsType keywordsType : keywords) {
		finalKeywords.addAll(keywordsType.getKeyword());
	    }
	} else {
	    keywords = capabilities.getServiceIdentification().getKeywords();
	    if (keywords != null && !keywords.isEmpty()) {
		for (KeywordsType keywordsType : keywords) {
		    finalKeywords.addAll(keywordsType.getKeyword());
		}
	    }
	}
	for (String keyword : finalKeywords) {
	    identification.addKeyword(keyword);
	}

	// CONSTRAINTS
	String fees = capabilities.getServiceIdentification().getFees();
	if (fees != null) {
	    identification.setSupplementalInformation("Fees: " + fees);
	}
	List<String> constraints = capabilities.getServiceIdentification().getAccessConstraints();
	if (constraints != null) {
	    for (String constraint : constraints) {
		identification.addAccessConstraint("otherRestrictions");
		LegalConstraints legalConstraints = new LegalConstraints();
		legalConstraints.addOtherConstraints("Access constraints: " + constraint);
		identification.addLegalConstraints(legalConstraints);
	    }
	}

	// BOUNDING BOX
	List<WGS84BoundingBoxType> envelope84s = feature.getWGS84BoundingBox();
	if (envelope84s != null) {
	    for (WGS84BoundingBoxType envelope84 : envelope84s) {
		double west = envelope84.getLowerCorner().get(0);
		double south = envelope84.getLowerCorner().get(1);
		double east = envelope84.getUpperCorner().get(0);
		double north = envelope84.getUpperCorner().get(1);
		identification.addGeographicBoundingBox(north, west, south, east);
	    }
	}

	// ONLINE RESOURCE
	String uuid = UUID.randomUUID().toString();
	Online online = new Online();
	OperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();
	String href = null;
	if (operationsMetadata != null) {
	    List<Operation> operations = operationsMetadata.getOperation();
	    if (operations != null) {
		for (Operation operation : operations) {
		    if (operation.getName() != null && operation.getName().equalsIgnoreCase("getfeature")) {
			List<DCP> dcps = operation.getDCP();
			if (dcps != null) {
			    DCP dcp = dcps.getFirst();
			    HTTP http = dcp.getHTTP();
			    List<JAXBElement<RequestMethodType>> gps = http.getGetOrPost();
			    if (gps != null) {
				for (JAXBElement<RequestMethodType> gp : gps) {
				    href = gp.getValue().getHref();
				}
			    }
			}
		    }
		}
	    }
	}

	boolean useSourceEndpoint = false;
	if (href == null || href.contains("localhost")) {
	    useSourceEndpoint = true;
	}
	try {
	    new URL(href);
	} catch (Exception e) {
	    useSourceEndpoint = true;
	}

	if (!useSourceEndpoint) {

	    Optional<Integer> integer = HttpConnectionUtils.getOptionalResponseCode(href);
	    if (integer.isPresent()) {
		String status = "" + integer;
		// in case of client error, such as 404 not found
		// we try to recover using the source endpoint
		if (status.startsWith("4")) {
		    useSourceEndpoint = true;
		}
	    } else {
		// or even if no response provided
		useSourceEndpoint = true;
	    }

	}
	if (useSourceEndpoint) {
	    href = sourceEndpoint;
	}

	if (href.endsWith("?") || href.endsWith("&")) {

	} else {
	    if (!href.contains("?")) {
		href = href + "?";
	    } else {
		href = href + "&";
	    }
	}

	online.setProtocol(NetProtocolWrapper.WFS_1_1_0.getCommonURN());
	online.setLinkage(href);
	online.setName(featureName);
	online.setFunctionCode("download");
	online.setIdentifier(uuid);

	Distribution distribution = new Distribution();
	distribution.addDistributionOnline(online);
	metadata.setDistribution(distribution);

	// TODO: MetadataURL support missing

	return ret;

    }

}
