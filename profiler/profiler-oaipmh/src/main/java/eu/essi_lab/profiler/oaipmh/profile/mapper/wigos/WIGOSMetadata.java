package eu.essi_lab.profiler.oaipmh.profile.mapper.wigos;

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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.wigos._1_0.gco.CharacterStringPropertyType;
import eu.essi_lab.wigos._1_0.gco.CodeListValueType;
import eu.essi_lab.wigos._1_0.gco.ObjectFactory;
import eu.essi_lab.wigos._1_0.gmd.CIAddressPropertyType;
import eu.essi_lab.wigos._1_0.gmd.CIAddressType;
import eu.essi_lab.wigos._1_0.gmd.CIContactPropertyType;
import eu.essi_lab.wigos._1_0.gmd.CIContactType;
import eu.essi_lab.wigos._1_0.gmd.CIOnlineResourcePropertyType;
import eu.essi_lab.wigos._1_0.gmd.CIOnlineResourceType;
import eu.essi_lab.wigos._1_0.gmd.CIResponsiblePartyType;
import eu.essi_lab.wigos._1_0.gmd.CIRoleCodePropertyType;
import eu.essi_lab.wigos._1_0.gmd.CITelephonePropertyType;
import eu.essi_lab.wigos._1_0.gmd.CITelephoneType;
import eu.essi_lab.wigos._1_0.gmd.MDDigitalTransferOptionsPropertyType;
import eu.essi_lab.wigos._1_0.gmd.MDDigitalTransferOptionsType;
import eu.essi_lab.wigos._1_0.gmd.MDDistributionType;
import eu.essi_lab.wigos._1_0.gmd.MDMetadataPropertyType;
import eu.essi_lab.wigos._1_0.gmd.MDMetadataType;
import eu.essi_lab.wigos._1_0.gmd.URLPropertyType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.CodeType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.CodeWithAuthorityType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.GeometryPropertyType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.MeasureType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.PointType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.StringOrRefType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.TimeInstantPropertyType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.TimePeriodPropertyType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.wigos._1_0.gml._3_2_1.VerticalDatumType;
import eu.essi_lab.wigos._1_0.main.AbstractEnvironmentalMonitoringFacilityType.Description;
import eu.essi_lab.wigos._1_0.main.AbstractEnvironmentalMonitoringFacilityType.GeospatialLocation;
import eu.essi_lab.wigos._1_0.main.AbstractEnvironmentalMonitoringFacilityType.ResponsibleParty;
import eu.essi_lab.wigos._1_0.main.ClimateZoneType;
import eu.essi_lab.wigos._1_0.main.ControlCheckReportType;
import eu.essi_lab.wigos._1_0.main.DataGenerationPropertyType;
import eu.essi_lab.wigos._1_0.main.DataGenerationType;
import eu.essi_lab.wigos._1_0.main.DataPolicyType;
import eu.essi_lab.wigos._1_0.main.DeploymentPropertyType;
import eu.essi_lab.wigos._1_0.main.DeploymentType;
import eu.essi_lab.wigos._1_0.main.DeploymentType.InstrumentOperatingStatus;
import eu.essi_lab.wigos._1_0.main.DescriptionPropertyType;
import eu.essi_lab.wigos._1_0.main.DescriptionType;
import eu.essi_lab.wigos._1_0.main.EquipmentLogPropertyType;
import eu.essi_lab.wigos._1_0.main.EquipmentLogType;
import eu.essi_lab.wigos._1_0.main.EquipmentPropertyType;
import eu.essi_lab.wigos._1_0.main.EquipmentType;
import eu.essi_lab.wigos._1_0.main.FacilityLogPropertyType;
import eu.essi_lab.wigos._1_0.main.FacilityLogType;
import eu.essi_lab.wigos._1_0.main.GeospatialLocationType;
import eu.essi_lab.wigos._1_0.main.HeaderType;
import eu.essi_lab.wigos._1_0.main.HeaderType.RecordOwner;
import eu.essi_lab.wigos._1_0.main.InstrumentOperatingStatusType;
import eu.essi_lab.wigos._1_0.main.LogType.LogEntry;
import eu.essi_lab.wigos._1_0.main.MaintenanceReportType;
import eu.essi_lab.wigos._1_0.main.ObservingCapabilityPropertyType;
import eu.essi_lab.wigos._1_0.main.ObservingCapabilityType;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.ClimateZone;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.ProgramAffiliation;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.SurfaceCover;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.SurfaceRoughness;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.Territory;
import eu.essi_lab.wigos._1_0.main.ObservingFacilityType.TopographyBathymetry;
import eu.essi_lab.wigos._1_0.main.ProcessType;
import eu.essi_lab.wigos._1_0.main.ProcessingPropertyType;
import eu.essi_lab.wigos._1_0.main.ProcessingType;
import eu.essi_lab.wigos._1_0.main.ProgramAffiliationType;
import eu.essi_lab.wigos._1_0.main.ProgramAffiliationType.ReportingStatus;
import eu.essi_lab.wigos._1_0.main.ReportingPropertyType;
import eu.essi_lab.wigos._1_0.main.ReportingStatusType;
import eu.essi_lab.wigos._1_0.main.ReportingType;
import eu.essi_lab.wigos._1_0.main.ReportingType.DataPolicy;
import eu.essi_lab.wigos._1_0.main.ReportingType.ReferenceDatum;
import eu.essi_lab.wigos._1_0.main.ResponsiblePartyType;
import eu.essi_lab.wigos._1_0.main.ResultSetPropertyType;
import eu.essi_lab.wigos._1_0.main.ResultSetType;
import eu.essi_lab.wigos._1_0.main.ResultSetType.DistributionInfo;
import eu.essi_lab.wigos._1_0.main.SamplingPropertyType;
import eu.essi_lab.wigos._1_0.main.SamplingType;
import eu.essi_lab.wigos._1_0.main.SchedulePropertyType;
import eu.essi_lab.wigos._1_0.main.ScheduleType;
import eu.essi_lab.wigos._1_0.main.SurfaceCoverType;
import eu.essi_lab.wigos._1_0.main.SurfaceRoughnessType;
import eu.essi_lab.wigos._1_0.main.TerritoryType;
import eu.essi_lab.wigos._1_0.main.TopographyBathymetryType;
import eu.essi_lab.wigos._1_0.main.WIGOSMetadataRecordType;
import eu.essi_lab.wigos._1_0.main.WIGOSMetadataRecordType.Facility;
import eu.essi_lab.wigos._1_0.main.WIGOSMetadataRecordType.HeaderInformation;
import eu.essi_lab.wigos._1_0.main.WIGOSMetadataRecordType.Observation;
import eu.essi_lab.wigos._1_0.om.OMObservationPropertyType;
import eu.essi_lab.wigos._1_0.om.OMObservationType;
import eu.essi_lab.wigos._1_0.om.OMProcessPropertyType;
import eu.essi_lab.wigos._1_0.om.TimeObjectPropertyType;

public class WIGOSMetadata implements IWIGOSMetadata {

    private WIGOSMetadataRecordType record;

    public WIGOSMetadataRecordType getRecord() {
	return record;
    }

    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;
    private static DocumentBuilder builder;

    public static DocumentBuilder getBuilder() {
	return builder;
    }

    public static Unmarshaller getUnmarshaller() {
	return unmarshaller;
    }

    public static Marshaller getMarshaller() {
	return marshaller;
    }

    /**
     * The lock object is needed, as the underlying library (Saxon-HE) is not multithread-safe
     */
    final protected static Object LOCK = new Object();

    static {
	try {
	    synchronized (LOCK) {
		builder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();
	    }

	    JAXBContext context = JAXBContext.newInstance(WIGOSMetadataRecordType.class);
	    unmarshaller = context.createUnmarshaller();
	    marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
		    "http://def.wmo.int/wmdr/2017 http://schemas.wmo.int/wmdr/1.0RC9/wmdr.xsd");
	    NamespacePrefixMapper npm = new NamespacePrefixMapper() {

		@Override
		public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix) {
		    if (namespaceURI == null) {
			return suggestion;
		    }
		    switch (namespaceURI) {
		    case "http://www.opengis.net/gml/3.2":
			return "gml";
		    case "http://www.w3.org/1999/xlink":
			return "xlink";
		    case "http://def.wmo.int/wmdr/2017":
			return "wmdr";
		    case "http://www.isotc211.org/2005/gco":
			return "gco";
		    case "http://www.isotc211.org/2005/gmd":
			return "gmd";
		    case "http://def.wmo.int/opm/2013":
			return "opm";
		    case "http://def.wmo.int/metce/2013":
			return "metce";
		    case "http://www.opengis.net/om/2.0":
			return "om";
		    case "http://www.isotc211.org/2005/gts":
			return "gts";
		    case "http://www.opengis.net/sampling/2.0":
			return "sam";
		    case "http://www.opengis.net/samplingSpatial/2.0":
			return "sams";
		    default:
			return suggestion;
		    }

		}
	    };
	    marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, npm);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public WIGOSMetadata() {
	this.record = new WIGOSMetadataRecordType();

    }

    // HEADER
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setHeaderInformation(java.util.Date, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setHeaderInformation(Date fileDate, String contactSurname, String contactName, String contactTitle,
	    String organizationShort, String contactEmail, String organizationURL) {
	HeaderType header = new HeaderType();

	// [FILE_DATE]
	GregorianCalendar gregory = new GregorianCalendar();
	gregory.setTime(fileDate);

	try {
	    XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
	    header.setFileDateTime(calendar);
	} catch (DatatypeConfigurationException e) {
	    e.printStackTrace();
	}

	// [SURNAME], [NAME], [TITLE]
	RecordOwner owner = new RecordOwner();
	CIResponsiblePartyType party = new CIResponsiblePartyType();
	if (contactName != null && contactSurname != null && contactTitle != null) {
	    String individualName = contactSurname + ", " + contactName + ", " + contactTitle;
	    individualName = individualName.trim();
	    if (individualName.endsWith(",")) {
		individualName = individualName.substring(0, individualName.length() - 1);
	    }
	    party.setIndividualName(getCharacterStringProperty(individualName));
	}

	// [ORGANIZATION_SHORT]
	party.setOrganisationName(getCharacterStringProperty(organizationShort));

	// [CONTACT_EMAIL]
	CIContactPropertyType contactProperty = new CIContactPropertyType();
	CIContactType ciContact = new CIContactType();
	CIAddressPropertyType address = new CIAddressPropertyType();
	CIAddressType ciAddress = new CIAddressType();
	ciAddress.getElectronicMailAddress().add(getCharacterStringProperty(contactEmail));
	address.setCIAddress(ciAddress);
	ciContact.setAddress(address);

	// [ORGANIZATION_URL]
	CIOnlineResourcePropertyType online = new CIOnlineResourcePropertyType();
	CIOnlineResourceType ciOnline = new CIOnlineResourceType();
	URLPropertyType url = new URLPropertyType();
	url.setURL(organizationURL);
	ciOnline.setLinkage(url);
	online.setCIOnlineResource(ciOnline);
	ciContact.setOnlineResource(online);
	contactProperty.setCIContact(ciContact);
	party.setContactInfo(contactProperty);

	CIRoleCodePropertyType role = new CIRoleCodePropertyType();
	CodeListValueType codeList = new CodeListValueType();
	codeList.setCodeList("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists#CI_RoleCode");
	codeList.setCodeListValue("custodian");
	role.setCIRoleCode(codeList);
	party.setRole(role);

	owner.setCIResponsibleParty(party);
	header.setRecordOwner(owner);

	HeaderInformation he = new HeaderInformation();
	he.setHeader(header);
	record.setHeaderInformation(he);

	// record.getHeaderInformation().setHeader(header);
    }

    // CATEGORY 1: OBSERVED VARIABLE
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setObservedVariable(java.lang.String)
     */
    @Override
    public void setObservedVariable(String name, String code) {
	OMObservationType observation = getInnerObservation();
	observation.getName().clear();
	CodeType ct = new CodeType();
	ct.setValue(name);
	observation.getName().add(ct);
	ReferenceType referenceType = new ReferenceType();
	referenceType.setHref(code);
	observation.setObservedProperty(referenceType);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setFeatureOfInterest(java.lang.String)
     */
    @Override
    public void setFeatureOfInterest(String foiCode) {
	OMObservationType observation = getInnerObservation();
	FeaturePropertyType fpt = new FeaturePropertyType();
	fpt.setHref("http://codes.wmo.int/wmdr/FeatureOfInterest/" + foiCode);
	observation.setFeatureOfInterest(fpt);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setMeasurementUnit(java.lang.String)
     */
    @Override
    public void setMeasurementUnit(String uomCode) {
	ReportingType reporting = getInnerReporting();
	ReferenceType referenceType = new ReferenceType();
	if (uomCode.contains("codes.wmo.int/common/unit/")) {
	    referenceType.setHref(uomCode);
	} else {
	    referenceType.setHref("http://codes.wmo.int/common/unit/" + uomCode);
	}
	reporting.setUom(referenceType);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSpatialExtent(java.lang.String)
     */
    @Override
    public void setSpatialExtent(String text) {
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setRepresentativeness(java.lang.String)
     */
    @Override
    public void setRepresentativeness(String representativenessCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/Representativeness/" + representativenessCode);
	getDeployment().setRepresentativeness(ref);
    }

    // CATEGORY 2: PURPOSE OF OBSERVATION
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setApplicationArea(java.lang.String)
     */
    @Override
    public void setApplicationArea(String applicationAreaCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/ApplicationArea/" + applicationAreaCode);
	if (applicationAreaCode == null) {
	    ReferenceType newRef = new ReferenceType();
	    getDeployment().getApplicationArea().add(newRef);
	} else {
	    getDeployment().getApplicationArea().add(ref);
	}
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addProgramme(java.lang.String)
     */
    @Override
    public void addProgramme(String programCode) {
	ProgramAffiliationType pat = new ProgramAffiliationType();
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/ProgramAffiliation/" + programCode);
	pat.setProgramAffiliation(ref);
	ProgramAffiliation pat1 = new ProgramAffiliation();
	pat1.setProgramAffiliation(pat);
	getObservingFacility().getProgramAffiliation().add(pat1);
	getObservingCapability().getProgramAffiliation().add(pat.getProgramAffiliation());
    }

    // CATEGORY 3: STATION/PLATFORM
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setRegionOfOrigin(java.lang.String)
     */
    @Override
    public void setRegionOfOrigin(String regionCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/WMORegion/" + regionCode);
	getObservingFacility().setWmoRegion(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setTerritoryOfOrigin(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setTerritoryOfOrigin(String territoryCode, String validPeriodBegin, String validPeriodEnd) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/TerritoryName/" + territoryCode);
	getObservingFacility().getTerritory().clear();
	Territory t = new Territory();
	TerritoryType tt = new TerritoryType();
	tt.setTerritoryName(ref);
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(validPeriodBegin, validPeriodEnd, null));
	tt.setValidPeriod(tp);
	t.setTerritory(tt);

	getObservingFacility().getTerritory().add(t);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformName(java.lang.String)
     */
    @Override
    public void setStationOrPlatformName(String name) {
	CodeType code = new CodeType();
	code.setValue(name);
	getFacility().getObservingFacility().getName().add(code);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformDescription(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void setStationOrPlatformDescription(String description) {
	StringOrRefType str = new StringOrRefType();
	str.setValue(description);
	getObservingFacility().setDescription(str);

    }

    public void setStationOrPlatformDescription(String description, String beginPosition, String endPosition) {
	this.setStationOrPlatformDescription(description);

	DescriptionType dType = new DescriptionType();
	DescriptionPropertyType pType = new DescriptionPropertyType();
	dType.setDescription(description);
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	dType.setValidPeriod(tp);
	pType.setDescription(dType);
	Description desc = new Description();
	desc.setDescription(dType);
	getObservingFacility().getFacilityDescription().add(desc);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformType(java.lang.String)
     */
    @Override
    public void setStationOrPlatformType(String facilityCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/FacilityType/" + facilityCode);
	getFacility().getObservingFacility().setFacilityType(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformModel()
     */
    @Override
    public void setStationOrPlatformModel() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformIdentifier(java.lang.String)
     * WIGOS IDENTIFIER:
     * A2 scenario: 0 - ISO country code - Number allocated to NHS - Local ID i.e for Botswana 0-72-800-7024 (Boro River
     * at Bokwi station for Botswana)
     * where the number 800 was allocated to NHS by the OSCAR National Focal Point from Botswana Meteorological
     * Authority
     * B2 (allocated by WMO secretariat): 0 - 21016 - RegionCountryBasin - Local ID. Region: 1-6, country: ISO country
     * code, Basin: Local, international or basin code otherwise default 0)
     * i.e 0 – 21016- 172000- 7024 (Boro River from Botswana) where: For 21016 : 1-WMO Region code for Africa (RA I), 72
     * – ISO country code for Botswana, 000-Basin code not known (default)
     * 7024 is the local ID.
     */
    @Override
    public void setStationOrPlatformIdentifier(String id) {

	// facility
	// String initialPart = "0-21016-17100-";
	// try {
	// // oscar identifiers must be : 0-21016-4203-[16 alphanumeric chars]
	// String alphaNumericId = null;
	// if (id.length() > 16) {
	// alphaNumericId = id.substring(0, 16);
	// }
	//
	// if (alphaNumericId == null) {
	//
	// alphaNumericId = StringUtils.hashSHA1messageDigest(id).substring(0, 16);
	//
	// }
	//
	// String newId = initialPart + id;

	// getFacility().getObservingFacility().setId(newId);
	getFacility().getObservingFacility().getIdentifier().setCodeSpace(id);
	getFacility().getObservingFacility().getIdentifier().setValue(id);

	// observation
	getFacilityReference().setHref("http://codes.wmo.int/" + id);
	CodeWithAuthorityType code = new CodeWithAuthorityType();
	code.setCodeSpace("codes.wmo.int");
	code.setValue("http://codes.wmo.int/" + id);
	getObservingCapability().setIdentifier(code);

	// } catch (NoSuchAlgorithmException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOrPlatformDateEstablished(javax.xml.datatype.
     * XMLGregorianCalendar)
     */
    @Override
    public void setStationOrPlatformDateEstablished(XMLGregorianCalendar date) {

	getFacility().getObservingFacility().setDateEstablished(date);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setDataCommunicationMethod(java.lang.String)
     */
    @Override
    public void setDataCommunicationMethod(String communicationMethodCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref(communicationMethodCode);
	getDeployment().setCommunicationMethod(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStationOperatingStatus()
     */
    @Override
    public void setStationOperatingStatus() {
	// ReportingStatus status = new ReportingStatus();
	// ReportingStatusType value = new ReportingStatusType();
	// ReferenceType reference = new ReferenceType();
	// reference.setHref("http://codes.wmo.int/wmdr/ReportingStatus/" + "operational");
	// value.setReportingStatus(reference );
	// status.setReportingStatus(value);
	// ProgramAffiliation pa = getObservingFacility().getProgramAffiliation().get(0);
	// ProgramAffiliationType paType = new ProgramAffiliationType();
	// paType.getReportingStatus().add(status);
	// pa.setProgramAffiliation(paType);
	// getObservingFacility().getProgramAffiliation().add(pa);
	// getObservingCapability().getProgramAffiliation().add(paType.getProgramAffiliation());

    }

    // CATEGORY 4: ENVIRONMENT

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSurfaceCover(java.lang.String, java.lang.String)
     */
    @Override
    public void setSurfaceCover(String codespace, String surfaceCoverCode, String surfaceCoverClassificationCode) {

	ReferenceType ref = new ReferenceType();
	if (surfaceCoverCode != null && surfaceCoverCode.equals("not applicable")) {
	    ref.getNilReason().add(surfaceCoverCode);
	} else {
	    ref.setHref(codespace + "/" + surfaceCoverCode);
	}
	getObservingFacility().getSurfaceCover().clear();
	SurfaceCover sc = new SurfaceCover();

	ReferenceType ref2 = new ReferenceType();
	if (surfaceCoverClassificationCode != null && surfaceCoverClassificationCode.equals("not applicable")) {
	    ref2.getNilReason().add(surfaceCoverClassificationCode);
	} else {
	    ref2.setHref("http://codes.wmo.int/wmdr/SurfaceCoverClassification/" + surfaceCoverClassificationCode);
	}
	SurfaceCoverType sct = new SurfaceCoverType();
	sct.setSurfaceCoverClassification(ref2);
	sct.setSurfaceCover(ref);
	sc.setSurfaceCover(sct);
	getObservingFacility().getSurfaceCover().add(sc);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setTopographyOrBathymetry(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setTopographyOrBathymetry(String beginPosition, String endPosition, String localTopographyCode,
	    String relativeElevationCode, String topographicContextCode, String altitudeOrDepthCode) {

	TopographyBathymetryType tbt = new TopographyBathymetryType();

	if (localTopographyCode != null) {
	    ReferenceType ref1 = new ReferenceType();
	    if (localTopographyCode != null && localTopographyCode.equals("not applicable")) {
		ref1.getNilReason().add(localTopographyCode);
	    } else {
		ref1.setHref("http://codes.wmo.int/wmdr/LocalTopographyType/" + localTopographyCode);
	    }
	    tbt.setLocalTopography(ref1);
	}
	if (relativeElevationCode != null) {
	    ReferenceType ref2 = new ReferenceType();
	    if (relativeElevationCode != null && relativeElevationCode.equals("not applicable")) {
		ref2.getNilReason().add(relativeElevationCode);
	    } else {
		ref2.setHref("http://codes.wmo.int/wmdr/RelativeElevationType/" + relativeElevationCode);
	    }
	    tbt.setRelativeElevation(ref2);
	}
	if (topographicContextCode != null) {
	    ReferenceType ref3 = new ReferenceType();
	    if (topographicContextCode != null && topographicContextCode.equals("not applicable")) {
		ref3.getNilReason().add(topographicContextCode);
	    } else {
		ref3.setHref("http://codes.wmo.int/wmdr/TopographicContextType/" + topographicContextCode);
	    }
	    tbt.setTopographicContext(ref3);
	}
	if (altitudeOrDepthCode != null) {
	    ReferenceType ref4 = new ReferenceType();
	    if (altitudeOrDepthCode != null && altitudeOrDepthCode.equals("not applicable")) {
		ref4.getNilReason().add(altitudeOrDepthCode);
	    } else {
		ref4.setHref("http://codes.wmo.int/wmdr/AltitudeOrDepthType/" + altitudeOrDepthCode);
	    }
	    tbt.setAltitudeOrDepth(ref4);
	}
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));

	tbt.setValidPeriod(tp);

	TopographyBathymetry topoBathy = new TopographyBathymetry();

	topoBathy.setTopographyBathymetry(tbt);
	getObservingFacility().getTopographyBathymetry().clear();
	getObservingFacility().getTopographyBathymetry().add(topoBathy);
    }

    /*
     * (non-Javadoc)
     * @see
     * eu.essi_lab.oai_pmh.IWIGOSMetadata#addEventsAtObservingFacility(eu.essi_lab.wigos._1_0.main.
     * LogType.
     * LogEntry)
     */
    @Override
    public void addEventsAtObservingFacility(LogEntry logEntry) {
	FacilityLogType flt = getFacilityLog();
	flt.getLogEntry().add(logEntry);
    }

    private FacilityLogType getFacilityLog() {
	FacilityLogPropertyType fl = getObservingFacility().getFacilityLog();
	FacilityLogType flt;
	if (fl == null) {
	    fl = new FacilityLogPropertyType();
	    flt = new FacilityLogType();
	    fl.setFacilityLog(flt);
	    getObservingFacility().setFacilityLog(fl);
	} else {
	    flt = fl.getFacilityLog();
	}

	return flt;
    }

    private EquipmentLogType getEquipmentLog() {
	EquipmentLogPropertyType fl = getEquipment().getEquipmentLog();
	EquipmentLogType flt;
	if (fl == null) {
	    fl = new EquipmentLogPropertyType();
	    flt = new EquipmentLogType();
	    fl.setEquipmentLog(flt);
	    getEquipment().setEquipmentLog(fl);
	} else {
	    flt = fl.getEquipmentLog();
	}

	return flt;
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSiteInformation()
     */
    @Override
    public void setSiteInformation() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSurfaceRoughness(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setSurfaceRoughness(String surfaceRoughnessCode, String beginPosition, String endPosition) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/SurfaceRoughnessDavenport/" + surfaceRoughnessCode);
	getObservingFacility().getSurfaceRoughness().clear();
	SurfaceRoughness sr = new SurfaceRoughness();
	SurfaceRoughnessType srt = new SurfaceRoughnessType();
	srt.setSurfaceRoughness(ref);
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	srt.setValidPeriod(tp);
	sr.setSurfaceRoughness(srt);
	getObservingFacility().getSurfaceRoughness().add(sr);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setClimateZone(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setClimateZone(String climateZoneCode, String beginPosition, String endPosition) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/ClimateZone/" + climateZoneCode);
	getObservingFacility().getClimateZone().clear();
	ClimateZone cz = new ClimateZone();
	ClimateZoneType czt = new ClimateZoneType();
	czt.setClimateZone(ref);
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	czt.setValidPeriod(tp);
	cz.setClimateZone(czt);
	getObservingFacility().getClimateZone().add(cz);

    }

    // CATEGORY 5: INSTRUMENTS AND METHODS OF OBSERVATION
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSourceOfObservation(java.lang.String)
     */
    @Override
    public void setSourceOfObservation(String sourceOfObservationCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/SourceOfObservation/" + sourceOfObservationCode);
	getDeployment().setSourceOfObservation(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setMeasurementMethod(java.lang.String)
     */
    @Override
    public void setMeasurementMethod(String measurementMethodCode) {
	EquipmentType equipment = getEquipment();
	ReferenceType ref = new ReferenceType();// http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/213,
						// https://codes.wmo.int/wmdr/ObservingMethodAtmosphere/170,
						// https://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171
	ref.setHref("http://codes.wmo.int/wmdr/ObservingMethodTerrestrial/" + measurementMethodCode);
	equipment.setObservingMethod(ref);

    }

    private EquipmentType getEquipment() {
	EquipmentPropertyType deployedEquipment = getDeployment().getDeployedEquipment();
	if (deployedEquipment == null) {
	    deployedEquipment = new EquipmentPropertyType();
	    EquipmentType equipment = new EquipmentType();
	    deployedEquipment.setEquipment(equipment);
	    getDeployment().setDeployedEquipment(deployedEquipment);
	}
	return deployedEquipment.getEquipment();
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setInstrumentSpecifications(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setInstrumentSpecifications(String observableRange, String specifiedAbsoluteUncertainty,
	    String specifiedRelativeUncertainty, String driftPerUnitTime, String specificationLink) {
	getEquipment().setObservableRange(observableRange);
	getEquipment().setSpecifiedAbsoluteUncertainty(specifiedAbsoluteUncertainty);
	getEquipment().setSpecifiedRelativeUncertainty(specifiedRelativeUncertainty);
	getEquipment().setDriftPerUnitTime(driftPerUnitTime);
	getEquipment().setSpecificationLink(specificationLink);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addInstrumentOperatingStatus(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addInstrumentOperatingStatus(String operatingStatusCode, String beginPosition, String endPosition) {
	InstrumentOperatingStatus ios = new InstrumentOperatingStatus();
	InstrumentOperatingStatusType iost = new InstrumentOperatingStatusType();
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/InstrumentOperatingStatus/" + operatingStatusCode);
	iost.setInstrumentOperatingStatus(ref);
	ios.setInstrumentOperatingStatus(iost);
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	iost.setValidPeriod(tp);
	getDeployment().getInstrumentOperatingStatus().add(ios);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setVerticalDistanceOfSensor(java.lang.String, double,
     * java.lang.String)
     */
    @Override
    public void setVerticalDistanceOfSensor(String uom, double value, String localReferenceSurfaceCode) {
	MeasureType measure = new MeasureType();
	measure.setUom(uom);
	measure.setValue(value);
	getDeployment().setHeightAboveLocalReferenceSurface(measure);
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/TypeOfReferenceSurface/" + localReferenceSurfaceCode);
	getDeployment().setLocalReferenceSurface(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setConfigurationOfInstrumentation(java.lang.String)
     */
    @Override
    public void setConfigurationOfInstrumentation(String configuration) {
	getDeployment().setConfiguration(configuration);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setInstrumentControlSchedule(java.lang.String)
     */
    @Override
    public void setInstrumentControlSchedule(String controlSchedule) {
	getDeployment().setControlSchedule(controlSchedule);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addInstrumentControlResult(eu.essi_lab.wigos._1_0.main.
     * ControlCheckReportType)
     */
    @Override
    public void addInstrumentControlResult(ControlCheckReportType controlCheck) {
	LogEntry logEntry = new LogEntry();
	eu.essi_lab.wigos._1_0.main.ObjectFactory fact = new eu.essi_lab.wigos._1_0.main.ObjectFactory();
	logEntry.setLogEntry(fact.createControlCheckReport(controlCheck));
	getEquipmentLog().getLogEntry().add(logEntry);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setInstrumentModelSerialNumber(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setInstrumentModelSerialNumber(String manufacturer, String modelNumber, String serialNumber, String firmwareVersion) {
	getEquipment().setManufacturer(manufacturer);
	getEquipment().setModel(modelNumber);
	getEquipment().setSerialNumber(serialNumber);
	getEquipment().setFirmwareVersion(firmwareVersion);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setInstrumentRoutineMaintenance(java.lang.String)
     */
    @Override
    public void setInstrumentRoutineMaintenance(String maintenanceSchedule) {
	getDeployment().setMaintenanceSchedule(maintenanceSchedule);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addMaintenanceReport(eu.essi_lab.wigos._1_0.main.
     * MaintenanceReportType)
     */
    @Override
    public void addMaintenanceReport(MaintenanceReportType maintenance) {
	LogEntry logEntry = new LogEntry();
	eu.essi_lab.wigos._1_0.main.ObjectFactory fact = new eu.essi_lab.wigos._1_0.main.ObjectFactory();
	logEntry.setLogEntry(fact.createMaintenanceReport(maintenance));
	getEquipmentLog().getLogEntry().add(logEntry);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addStationGeospatialLocation(java.lang.Double, java.lang.Double,
     * java.lang.Double, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void addStationGeospatialLocation(Double latitude, Double longitude, Double elevation, String crs, String geopositioningMethod,
	    String validTimeBegin, String validTimeEnd) {
	GeospatialLocation geospatialLocation = new GeospatialLocation();
	GeospatialLocationType location = new GeospatialLocationType();
	if (geopositioningMethod != null) {
	    ReferenceType ref = new ReferenceType();
	    ref.setHref("http://codes.wmo.int/wmdr/GeopositioningMethod/" + geopositioningMethod);
	    location.setGeopositioningMethod(ref);
	}
	TimePeriodPropertyType tppt = new TimePeriodPropertyType();
	TimePeriodType timePeriod = createTimePeriodType(validTimeBegin, validTimeEnd, null);
	tppt.setTimePeriod(timePeriod);
	location.setValidPeriod(tppt);

	GeometryPropertyType gpt = new GeometryPropertyType();
	eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory factory = new eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory();
	PointType pt = new PointType();
	DirectPositionType dpt = new DirectPositionType();
	dpt.getValue().add(latitude);
	dpt.getValue().add(longitude);
	pt.setSrsName("EPSG:4326");
	if (elevation != null) {
	    dpt.getValue().add(elevation);
	    pt.setSrsName("EPSG:4979");
	}
	if (crs != null && !crs.equals("")) {
	    pt.setSrsName(crs);
	}
	pt.setPos(dpt);
	JAXBElement<? extends AbstractGeometryType> value = factory.createPoint(pt);

	gpt.setAbstractGeometry(value);
	location.setGeoLocation(gpt);

	ReferenceType ref = new ReferenceType();
	ref.setHref(crs);
	location.setGeopositioningMethod(ref);

	geospatialLocation.setGeospatialLocation(location);
	getObservingFacility().getGeospatialLocation().add(geospatialLocation);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#addInstrumentGeospatialLocation(java.lang.Double,
     * java.lang.Double, java.lang.Double, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void addInstrumentGeospatialLocation(Double latitude, Double longitude, Double elevation, String crs,
	    String geopositioningMethod, String validTimeBegin, String validTimeEnd) {
	addStationGeospatialLocation(latitude, longitude, elevation, crs, geopositioningMethod, validTimeBegin, validTimeEnd);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setStatusOfObservation(java.lang.Boolean)
     */
    @Override
    public void setStatusOfObservation(Boolean officialStatus) {
	getReporting().getReporting().setOfficialStatus(officialStatus);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setExposureOfInstruments(java.lang.String)
     */
    @Override
    public void setExposureOfInstruments(String exposureTypeCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/Exposure/" + exposureTypeCode);
	getDeployment().setExposure(ref);
    }

    // CATEGORY 6: SAMPLING
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSamplingProcedures(java.lang.String, java.lang.String)
     */
    @Override
    public void setSamplingProcedures(String samplingProcedureCode, String samplingProcedureDescription) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/SamplingProcedure/" + samplingProcedureCode);
	getSampling().setSamplingProcedure(ref);
	getSampling().setSamplingProcedureDescription(samplingProcedureDescription);

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSampleTreatment(java.lang.String)
     */
    @Override
    public void setSampleTreatment(String sampleTreatmentCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/SamplingTreatment/" + sampleTreatmentCode);
	getSampling().setSampleTreatment(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSamplingStrategy(java.lang.String)
     */
    @Override
    public void setSamplingStrategy(String strategyCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/common/wmdr/SamplingStrategy/" + strategyCode);
	getSampling().setSamplingStrategy(ref);
    }

    private SamplingType getSampling() {
	DataGenerationType dg = getInnerDataGeneration();
	SamplingPropertyType spt = dg.getSampling();
	SamplingType s;
	if (spt == null) {
	    spt = new SamplingPropertyType();
	    s = new SamplingType();
	    spt.setSampling(s);
	    dg.setSampling(spt);
	} else {
	    s = spt.getSampling();
	}

	return s;
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSamplingTimePeriod(javax.xml.datatype.Duration)
     */
    @Override
    public void setSamplingTimePeriod(Duration duration) {
	getSampling().setSamplingTimePeriod(duration);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSpatialSamplingResolution(java.lang.String, double,
     * java.lang.String)
     */
    @Override
    public void setSpatialSamplingResolution(String uom, double v, String details) {
	MeasureType value = new MeasureType();
	value.setUom(uom);
	value.setValue(v);
	getSampling().setSpatialSamplingResolution(value);
	getSampling().setSpatialSamplingResolutionDetails(details);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setTemporalSamplingResolution(javax.xml.datatype.Duration)
     */
    @Override
    public void setTemporalSamplingResolution(Duration duration) {
	getSampling().setTemporalSamplingInterval(duration);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setDiurnalBaseTime(javax.xml.datatype.XMLGregorianCalendar)
     */
    @Override
    public void setDiurnalBaseTime(XMLGregorianCalendar diurnalBaseTime) {
	ScheduleType s = getSchedule();
	s.setDiurnalBaseTime(diurnalBaseTime);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setScheduleOfObservation(int, int, int, int, int, int, int, int)
     */
    @Override
    public void setScheduleOfObservation(int startMonth, int endMonth, int startWeekDay, int endWeekDay, int startHour, int endHour,
	    int startMinute, int endMinute) {
	ScheduleType s = getSchedule();
	s.setStartMonth(startMonth);
	s.setEndMonth(endMonth);
	s.setStartWeekday(startWeekDay);
	s.setEndWeekday(endWeekDay);
	s.setStartHour(startHour);
	s.setEndHour(endHour);
	s.setStartMinute(startMinute);
	s.setEndMinute(endMinute);
    }

    private ScheduleType getSchedule() {
	DataGenerationType dg = getInnerDataGeneration();
	SchedulePropertyType spt = dg.getSchedule();
	ScheduleType s;
	if (spt == null) {
	    spt = new SchedulePropertyType();
	    s = new ScheduleType();
	    spt.setSchedule(s);
	    dg.setSchedule(spt);
	} else {
	    s = spt.getSchedule();
	}
	return s;

    }

    // CATEGORY 7: DATA PROCESSING AND REPORTING
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setDataProcessingMethodsAndAlgorithms(java.lang.String)
     */
    @Override
    public void setDataProcessingMethodsAndAlgorithms(String dataProcessing) {
	getProcessing().setDataProcessing(dataProcessing);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setProcessingAnalysisCentre(java.lang.String)
     */
    @Override
    public void setProcessingAnalysisCentre(String processingCentre) {
	ProcessingType p = getProcessing();
	p.setProcessingCentre(processingCentre);

    }

    public ProcessingType getProcessing() {
	DataGenerationType dataGeneration = getInnerDataGeneration();
	ProcessingPropertyType processing = dataGeneration.getProcessing();
	if (processing == null) {
	    processing = new ProcessingPropertyType();
	    processing.setProcessing(new ProcessingType());
	    dataGeneration.setProcessing(processing);
	}
	return dataGeneration.getProcessing().getProcessing();

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setTemporalReportingPeriod(javax.xml.datatype.Duration,
     * java.lang.String)
     */
    @Override
    public void setTemporalReportingPeriod(Duration duration, String timeStampMeaningCode) {
	getInnerReporting().setTemporalReportingInterval(duration);
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/TimeStampMeaning/" + timeStampMeaningCode);
	getInnerReporting().setTimeStampMeaning(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSpatialReportingInterval(java.lang.String, double)
     */
    @Override
    public void setSpatialReportingInterval(String uom, double v) {
	MeasureType value = new MeasureType();
	value.setUom(uom);
	value.setValue(v);
	getInnerReporting().setSpatialReportingInterval(value);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSoftwareProcessorAndVersion(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setSoftwareProcessorAndVersion(String softwareDetails, String softwareURL) {
	getProcessing().setSoftwareDetails(softwareDetails);
	getProcessing().setSoftwareURL(softwareURL);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setLevelOfData(java.lang.String)
     */
    @Override
    public void setLevelOfData(String levelOfDataCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/LevelOfData/" + levelOfDataCode);
	getInnerReporting().setLevelOfData(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setDataFormat(java.lang.String)
     */
    @Override
    public void setDataFormat(String dataFormatCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/DataFormat/" + dataFormatCode);
	getInnerReporting().setDataFormat(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setVersionOfDataFormat(java.lang.String)
     */
    @Override
    public void setVersionOfDataFormat(String dataFormatVersion) {
	getInnerReporting().setDataFormatVersion(dataFormatVersion);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setAggregationPeriod(javax.xml.datatype.Duration)
     */
    @Override
    public void setAggregationPeriod(Duration duration) {
	getProcessing().setAggregationPeriod(duration);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setReferenceTime(java.lang.String)
     */
    @Override
    public void setReferenceTime(String referenceTimeSourceCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/ReferenceTime/" + referenceTimeSourceCode);
	getInnerReporting().setReferenceTimeSource(ref);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setReferenceDatum(eu.essi_lab.wigos._1_0.gml._3_2_1.
     * VerticalDatumType)
     */
    @Override
    public void setReferenceDatum(VerticalDatumType verticalDatum) {
	ReferenceDatum referenceDatum = new ReferenceDatum();
	referenceDatum.setVerticalDatum(verticalDatum);
	getInnerReporting().setReferenceDatum(referenceDatum);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setNumericalResolution(java.lang.String, double)
     */
    @Override
    public void setNumericalResolution(String uom, double value) {

	MeasureType measureType = new MeasureType();
	measureType.setUom(uom);
	measureType.setValue(value);
	getInnerReporting().setNumericalResolution(measureType);
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setLatency(javax.xml.datatype.Duration)
     */
    @Override
    public void setTimeliness(Duration duration) {
	getInnerReporting().setTimeliness(duration);
    }

    // CATEGORY 8: DATA QUALITY
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setUncertaintyOfMeasurement()
     */
    @Override
    public void setUncertaintyOfMeasurement() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setProcedureUsedToEstimateUncertainty()
     */
    @Override
    public void setProcedureUsedToEstimateUncertainty() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setQualityFlag()
     */
    @Override
    public void setQualityFlag() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setQualityFlaggingSystem()
     */
    @Override
    public void setQualityFlaggingSystem() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setTraceability()
     */
    @Override
    public void setTraceability() {

    }

    // CATEGORY 9: OWNERSHIP AND DATA POLICY
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setSupervisingOrganization()
     */
    @Override
    public void setSupervisingOrganization() {

    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setDataPolicyUseConstrainst(java.lang.String)
     */
    @Override
    public void setDataPolicyUseConstrainst(String dataPolicyCode) {
	ReferenceType ref = new ReferenceType();
	ref.setHref("http://codes.wmo.int/wmdr/DataPolicy/" + dataPolicyCode);
	DataPolicy policy = new DataPolicy();
	DataPolicyType p = new DataPolicyType();
	p.setDataPolicy(ref);
	policy.setDataPolicy(p);
	getInnerReporting().setDataPolicy(policy);
    }

    // CATEGORY 10: CONTACT
    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setFacilityContact(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setFacilityContact(String individualName, String organizationShort, String phone, String streetAddress, String city,
	    String state, String zip, String isoCountry, String email, String start, String end) {

	// [SURNAME], [NAME], [TITLE]
	CIResponsiblePartyType party = new CIResponsiblePartyType();
	// String individualName = surname + ", " + name + ", " + title;
	if (individualName != null) {
	    party.setIndividualName(getCharacterStringProperty(individualName));
	}

	// [ORGANIZATION_SHORT]
	party.setOrganisationName(getCharacterStringProperty(organizationShort));

	CIContactPropertyType contactProperty = new CIContactPropertyType();
	CIContactType ciContact = new CIContactType();
	// [CONTACT_EMAIL]
	if (phone != null) {

	    CITelephonePropertyType ciTelephone = new CITelephonePropertyType();
	    CITelephoneType ciphone = new CITelephoneType();

	    ciphone.getVoice().add(createCharacterStringProperty(phone));
	    ciTelephone.setCITelephone(ciphone);
	    ciContact.setPhone(ciTelephone);

	}

	CIAddressPropertyType address = new CIAddressPropertyType();
	CIAddressType ciAddress = new CIAddressType();
	if (streetAddress != null) {
	    ciAddress.getDeliveryPoint().add(createCharacterStringProperty(streetAddress));
	}
	if (city != null) {
	    ciAddress.setCity(createCharacterStringProperty(city));
	}
	if (state != null) {
	    ciAddress.setAdministrativeArea(createCharacterStringProperty(state));
	}
	if (zip != null) {
	    ciAddress.setPostalCode(createCharacterStringProperty(zip));
	}
	if (isoCountry != null) {
	    ciAddress.setCountry(createCharacterStringProperty(isoCountry));
	}
	if (email != null) {
	    ciAddress.getElectronicMailAddress().add(getCharacterStringProperty(email));
	}
	address.setCIAddress(ciAddress);
	ciContact.setAddress(address);

	// [ORGANIZATION_URL]
	// CIOnlineResourcePropertyType online = new CIOnlineResourcePropertyType();
	// CIOnlineResourceType ciOnline = new CIOnlineResourceType();
	// online.setCIOnlineResource(ciOnline);
	// ciContact.setOnlineResource(online);
	// contactProperty.setCIContact(ciContact);
	// party.setContactInfo(contactProperty);

	CIRoleCodePropertyType role = new CIRoleCodePropertyType();
	CodeListValueType codeList = new CodeListValueType();
	codeList.setCodeList("https://standards.iso.org/iso/19115/resources/Codelists/gml/CI_RoleCode.xml/owner");
	codeList.setCodeListValue("owner");
	role.setCIRoleCode(codeList);
	party.setRole(role);

	ResponsibleParty myParty = new ResponsibleParty();
	ResponsiblePartyType responsibleParty = new ResponsiblePartyType();
	eu.essi_lab.wigos._1_0.main.ResponsiblePartyType.ResponsibleParty rp = new eu.essi_lab.wigos._1_0.main.ResponsiblePartyType.ResponsibleParty();
	rp.setCIResponsibleParty(party);
	responsibleParty.setResponsibleParty(rp);
	myParty.setResponsibleParty(responsibleParty);
	if (start != null) {
	    TimePeriodPropertyType tppt = new TimePeriodPropertyType();
	    TimePeriodType timePeriod = createTimePeriodType(start, null, null);
	    tppt.setTimePeriod(timePeriod);
	    responsibleParty.setValidPeriod(tppt);
	}
	getObservingFacility().getResponsibleParty().add(myParty);

    }

    private CharacterStringPropertyType createCharacterStringProperty(String str) {
	CharacterStringPropertyType csp = new CharacterStringPropertyType();
	ObjectFactory fac = new ObjectFactory();
	csp.setCharacterString(fac.createCharacterString(str));
	return csp;
    }

    private ReportingType getInnerReporting() {
	ReportingPropertyType reporting = getReporting();
	ReportingType repo = reporting.getReporting();
	if (repo == null) {
	    repo = new ReportingType();
	}
	reporting.setReporting(repo);
	return repo;
    }

    private ReportingPropertyType getReporting() {
	DataGenerationType dg = getInnerDataGeneration();
	ReportingPropertyType reporting = dg.getReporting();
	if (reporting == null) {
	    reporting = new ReportingPropertyType();
	    ReportingType rt = new ReportingType();
	    reporting.setReporting(rt);
	}
	dg.setReporting(reporting);
	return reporting;
    }

    private DataGenerationType getInnerDataGeneration() {
	DataGenerationPropertyType dataGeneration = getDataGeneration();
	DataGenerationType dgt = dataGeneration.getDataGeneration();
	if (dgt == null) {
	    dgt = new DataGenerationType();
	    dgt.setId("_" + UUID.randomUUID().toString());
	}
	dataGeneration.setDataGeneration(dgt);
	return dataGeneration.getDataGeneration();

    }

    private DataGenerationPropertyType getDataGeneration() {
	List<DataGenerationPropertyType> dataGeneration = getDeployment().getDataGeneration();
	if (dataGeneration.isEmpty()) {
	    dataGeneration.add(new DataGenerationPropertyType());
	}
	return dataGeneration.get(0);
    }

    private DeploymentType getDeployment() {
	eu.essi_lab.wigos._1_0.main.ObjectFactory factory = new eu.essi_lab.wigos._1_0.main.ObjectFactory();
	OMProcessPropertyType procedure = getProcedure();
	DeploymentType deployment;
	if (procedure.getAny() == null) {
	    deployment = new DeploymentType();
	    deployment.setId("_" + UUID.randomUUID().toString());
	    ProcessType processType = new ProcessType();
	    JAXBElement<ProcessType> jaxb = factory.createProcess(processType);
	    DeploymentPropertyType dpt = new DeploymentPropertyType();
	    dpt.setDeployment(deployment);
	    processType.setDeployment(dpt);
	    procedure.setAny(jaxb);
	}

	return ((JAXBElement<ProcessType>) procedure.getAny()).getValue().getDeployment().getDeployment();
    }

    private OMProcessPropertyType getProcedure() {
	OMProcessPropertyType procedure = getInnerObservation().getProcedure();
	if (procedure == null) {
	    procedure = new OMProcessPropertyType();
	}
	getInnerObservation().setProcedure(procedure);
	return procedure;
    }

    private OMObservationType getInnerObservation() {
	ObservingCapabilityType observingCapability = getObservingCapability();
	List<OMObservationPropertyType> observations = observingCapability.getObservation();
	if (observations.isEmpty()) {
	    OMObservationPropertyType obs = new OMObservationPropertyType();
	    obs.setOMObservation(new OMObservationType());
	    observations.add(obs);
	}
	return observations.get(0).getOMObservation();
    }

    private ObservingCapabilityType getObservingCapability() {
	Observation observation = getObservation();
	if (observation.getObservingCapability() == null) {
	    observation.setObservingCapability(new ObservingCapabilityType());
	}
	return observation.getObservingCapability();
    }

    private Observation getObservation() {
	if (record.getObservation().isEmpty()) {
	    record.getObservation().add(new Observation());
	}
	Observation ret = record.getObservation().get(0);
	return ret;
    }

    private ObservingFacilityType getObservingFacility() {
	return getFacility().getObservingFacility();
    }

    private Facility getFacility() {
	if (record.getFacility().isEmpty()) {
	    Facility facility = new Facility();
	    ObservingFacilityType of = new ObservingFacilityType();
	    CodeWithAuthorityType value = new CodeWithAuthorityType();
	    value.setCodeSpace("http://codes.wmo.int");
	    of.setIdentifier(value);
	    facility.setObservingFacility(of);
	    record.getFacility().add(facility);
	}
	return record.getFacility().get(0);
    }

    private ReferenceType getFacilityReference() {
	ReferenceType facility = getObservingCapability().getFacility();
	if (facility == null) {
	    facility = new ReferenceType();
	    getObservingCapability().setFacility(facility);
	}
	return facility;
    }

    private CharacterStringPropertyType getCharacterStringProperty(String str) {
	ObjectFactory gcoFactory = new ObjectFactory();
	CharacterStringPropertyType ret = gcoFactory.createCharacterStringPropertyType();
	ret.setCharacterString(gcoFactory.createCharacterString(str));
	return ret;
    }

    public void print() throws JAXBException {
	eu.essi_lab.wigos._1_0.main.ObjectFactory factory = new eu.essi_lab.wigos._1_0.main.ObjectFactory();
	marshaller.marshal(factory.createWIGOSMetadataRecord(record), System.out);
    }

    // public Element asElement() throws Exception {
    // eu.essi_lab.wigos._1_0.main.ObjectFactory factory = new eu.essi_lab.wigos._1_0.main.ObjectFactory();
    // Document doc = builder.newDocument();
    // marshaller.marshal(factory.createWIGOSMetadataRecord(record), doc);
    // Element ret = doc.getDocumentElement();
    // return ret;
    // }

    // private void test() {
    // WIGOSMetadataRecordType record = new WIGOSMetadataRecordType();
    // StringOrRefType description = getString(iso.getDataIdentification().getAbstract());
    // record.setDescription(description);
    // record.setId(resource.getId());
    // CodeWithAuthorityType identifier = getCodeWithAuthority(resource.getProviderId(), iso.getFileIdentifierValue());
    // record.setIdentifier(identifier);
    // CodeType nameType = getCodeType(iso.getDataIdentification().getCitationTitle());
    // record.getName().add(nameType);
    //
    // List<Node> extensions = resource.getExtensions("siteInfo");
    // if (!extensions.isEmpty()) {
    // Node extension = extensions.get(0).getFirstChild();
    // SiteInfoDocument siteInfo = new SiteInfoDocument(extension);
    // if (siteInfo != null) {
    // Facility facility = new Facility();
    // ObservingFacilityType of = new ObservingFacilityType();
    // try {
    // of.getName().add(getCodeType(siteInfo.getSiteName()));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // try {
    // of.setDescription(getString(siteInfo.getSiteName()));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // try {
    // of.setIdentifier(getCodeWithAuthority(siteInfo.getSiteNetwork(), siteInfo.getSiteCode()));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // GeospatialLocation location = new GeospatialLocation();
    // TimestampedLocationType tl = new TimestampedLocationType();
    // TimePeriodPropertyType timePeriod = new TimePeriodPropertyType();
    // TimePeriodType tp = new TimePeriodType();
    // TimePositionType begin = new TimePositionType();
    // begin.getValue().add(iso.getDataIdentification().getTemporalExtent().getBeginPosition());
    // tp.setBeginPosition(begin);
    // TimePositionType end = new TimePositionType();
    // end.getValue().add(iso.getDataIdentification().getTemporalExtent().getEndPosition());
    // tp.setEndPosition(end);
    // timePeriod.setTimePeriod(tp);
    // tl.setValidTimePeriod(timePeriod);
    // location.setTimestampedLocation(tl);
    // GeometryPropertyType geometry = new GeometryPropertyType();
    // eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory gmlFactory = new
    // eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory();
    //
    // eu.essi_lab.wigos._1_0.gml._3_2_1.PointType point = new eu.essi_lab.wigos._1_0.gml._3_2_1.PointType();
    //
    // IGeographicBoundingBox bbox = iso.getDataIdentification().getGeographicBoundingBox();
    // if (bbox != null && bbox.getNorth() != null) {
    // Double north = bbox.getNorth();
    // Double south = bbox.getSouth();
    // Double east = bbox.getEast();
    // Double west = bbox.getWest();
    //
    // point.setSrsName("EPSG:4326");
    // DirectPositionType dpt = new DirectPositionType();
    // dpt.getValue().add(south);
    // dpt.getValue().add(west);
    // JAXBElement<? extends AbstractGeometryType> geometryType = gmlFactory.createPoint(point);
    // geometry.setAbstractGeometry(geometryType);
    // tl.setLocation(geometry);
    // }
    // of.getGeospatialLocation().add(location);
    //
    // IResponsibleParty sourceResponsible = iso.getDataIdentification().getResponsibleParty();
    // ResponsibleParty responsibleParty = new ResponsibleParty();
    // CIResponsiblePartyType cirpt = convertParty(sourceResponsible);
    // responsibleParty.setCIResponsibleParty(cirpt);
    // of.getResponsibleParty().add(responsibleParty);
    //
    // try {
    // List<Series> series = siteInfo.getSeries();
    // for (Series serie : series) {
    //
    // Observation obs = new Observation();
    //// OMObservationType obs = new OMObservationType();
    // obs.setIdentifier(identifier);
    // MDMetadataPropertyType mdm = new MDMetadataPropertyType();
    // mdm.setMDMetadata(convertMetadata(resource.getMD_Metadata()));
    // obs.setMetadata(mdm);
    // TimeObjectPropertyType timeObject = new TimeObjectPropertyType();
    // eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory gmlOF = new
    // eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory();
    // timeObject.setAbstractTimeObject(gmlOF.createTimePeriod(tp));
    //
    // obs.setPhenomenonTime(timeObject);
    //
    // String methodCode = serie.getMethodCode();
    // String methodDescription = serie.getMethodDescription();
    // String variableCode = serie.getVariableCode();
    // String variableName = serie.getVariableName();
    // String variableUnit = serie.getVariableUnitName();
    // ReferenceType reference = new ReferenceType();
    // reference.setHref(variableCode);
    // reference.setTitle(variableName);
    // obs.setObservedProperty(reference);
    // FeaturePropertyType foip = new FeaturePropertyType();
    // // gmlOF.createsf
    // eu.essi_lab.wigos._1_0.sampling.ObjectFactory samplingOF = new
    // eu.essi_lab.wigos._1_0.sampling.ObjectFactory();
    // SFSamplingFeatureType sfsft = new SFSamplingFeatureType();
    // foip.setAbstractFeature(samplingOF.createSFSamplingFeature(sfsft));
    // obs.setFeatureOfInterest(foip);
    //
    // record.getObservation().add(observation );
    //
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // facility.setObservingFacility(of);
    // record.getFacility().add(facility);
    //
    // }
    // }
    //

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setPhenomenontTemporalExtent(java.lang.String, java.lang.String)
     */
    @Override
    public void setPhenomenontTemporalExtent(String beginPosition, String endPosition) {
	TimePeriodType timePeriod = createTimePeriodType(beginPosition, endPosition, null);
	eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory factory = new eu.essi_lab.wigos._1_0.gml._3_2_1.ObjectFactory();
	JAXBElement<TimePeriodType> timePeriodElement = factory.createTimePeriod(timePeriod);
	TimeObjectPropertyType timeObject = new TimeObjectPropertyType();
	timeObject.setAbstractTimeObject(timePeriodElement);
	getInnerObservation().setPhenomenonTime(timeObject);
    }

    private TimePeriodType createTimePeriodType(String beginPosition, String endPosition, String id) {

	if (beginPosition == null && endPosition == null) {
	    return null;
	}

	TimePeriodType timePeriod = new TimePeriodType();
	TimePositionType t1 = new TimePositionType();
	if (beginPosition != null) {
	    t1.getValue().add(beginPosition);
	}
	timePeriod.setBeginPosition(t1);
	TimePositionType t2 = new TimePositionType();
	if (endPosition != null) {
	    t2.getValue().add(endPosition);
	}
	timePeriod.setEndPosition(t2);

	String gmlId = (id == null) ? "_" + UUID.randomUUID().toString() : id;
	timePeriod.setId(gmlId);

	return timePeriod;
    }

    /*
     * (non-Javadoc)
     * @see eu.essi_lab.oai_pmh.IWIGOSMetadata#setObservationDataUrlArchive(java.lang.String)
     */
    @Override
    public void setObservationDataUrlArchive(String url) {
	DistributionInfo distribution = new DistributionInfo();
	MDDistributionType mdd = new MDDistributionType();
	MDDigitalTransferOptionsPropertyType transfer = new MDDigitalTransferOptionsPropertyType();
	MDDigitalTransferOptionsType mdo = new MDDigitalTransferOptionsType();
	CIOnlineResourcePropertyType online = new CIOnlineResourcePropertyType();
	CIOnlineResourceType cionline = new CIOnlineResourceType();
	cionline.setId("_" + UUID.randomUUID().toString());
	URLPropertyType urlPt = new URLPropertyType();
	urlPt.setURL("<![CDATA[" + url + "]]>");
	cionline.setLinkage(urlPt);
	online.setCIOnlineResource(cionline);
	mdo.getOnLine().add(online);
	transfer.setMDDigitalTransferOptions(mdo);
	mdd.getTransferOptions().add(transfer);
	distribution.setMDDistribution(mdd);
	getResult().getDistributionInfo().add(distribution);

    }

    private ResultSetType getResult() {
	Object obj = getInnerObservation().getResult();
	if (obj == null) {
	    ResultSetPropertyType ret = new ResultSetPropertyType();
	    ResultSetType set = new ResultSetType();
	    ret.setResultSet(set);
	    getInnerObservation().setResult(ret);
	    return set;
	} else {
	    return ((ResultSetPropertyType) obj).getResultSet();
	}
    }

    public void setMDMetadata(MDMetadataType metadata) {
	MDMetadataPropertyType mp = new MDMetadataPropertyType();
	mp.setMDMetadata(metadata);
	getInnerObservation().setMetadata(mp);
    }

    public void setResultTime(TimeInstantPropertyType value) {
	OMObservationType observation = getInnerObservation();
	if (value == null) {
	    value = new TimeInstantPropertyType();
	}
	observation.setResultTime(value);
    }

    public void setDatGenarationValidPeriod(String beginPosition, String endPosition) {
	DataGenerationType dg = getInnerDataGeneration();
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	dg.setValidPeriod(tp);

    }

    public void setDeploymentValidPeriod(String beginPosition, String endPosition) {
	TimePeriodPropertyType tp = new TimePeriodPropertyType();
	tp.setTimePeriod(createTimePeriodType(beginPosition, endPosition, null));
	getDeployment().setValidPeriod(tp);
    }

    public void setObservationGeometryType(String type) {
	OMObservationType observation = getInnerObservation();
	ReferenceType ref = new ReferenceType();
	if (type != null) {
	    ref.setHref("http://codes.wmo.int/wmdr/Geometry/" + type);
	}
	observation.setType(ref);
    }

    public void setObservationInFacility() {
	List<Observation> observation = record.getObservation();
	Facility facility = record.getFacility().get(0);

	for (Observation o : observation) {
	    ObservingCapabilityPropertyType obscap = new ObservingCapabilityPropertyType();
	    ObservingCapabilityType val = o.getObservingCapability();
	    obscap.setObservingCapability(val);
	    OMObservationType observationType = getInnerObservation();
	    String href = observationType.getObservedProperty().getHref();
	    obscap.setHref(href);
	    // record.getRecord().getFacility().get(0).getObservingFacility().getObservation().add(obscap);
	    facility.getObservingFacility().getObservation().add(obscap);

	}

	record.getObservation().clear();

    }

    @Override
    public void setFacilityContact(String individualName, String organizationShort, String phone, String streetAddress, String city,
	    String state, String zip, String isoCountry, String email) {

	setFacilityContact(individualName, organizationShort, phone, streetAddress, city, state, zip, isoCountry, email, null, null);

    }

}
