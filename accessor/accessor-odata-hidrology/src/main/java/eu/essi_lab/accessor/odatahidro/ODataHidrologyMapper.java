/**
 * 
 */
package eu.essi_lab.accessor.odatahidro;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.Position2D;
import org.geotools.referencing.CRS;

import eu.essi_lab.accessor.odatahidro.client.ClientResponseWrapper;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient.Variable;
import eu.essi_lab.accessor.odatahidro.client.ODataOriginalMetadata;
import eu.essi_lab.accessor.odatahidro.client.SYKEIdentifierMangler;
import eu.essi_lab.adk.timeseries.TimeSeriesUtils;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;
import net.opengis.iso19139.gmd.v_20060504.DQScopePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopeType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Fabrizio
 */
public class ODataHidrologyMapper extends OriginalIdentifierMapper {

    public static final String ODATA_HIDROLOGY_SCHEME_URI = "odata-hidro-scheme-uri";
    private static final String ODATA_HIDROLOGY_MAPPER_ERROR = "ODATA_HIDROLOGY_MAPPER_ERROR";
    private static final String URN_SIKE = "urn:syke:station:";

    public ODataHidrologyMapper() {
	
	}
    
    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();

	try {

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	    MIMetadata miMetadata = coreMetadata.getMIMetadata();

	    ODataOriginalMetadata odataOriginal = (ODataOriginalMetadata) originalMD;

	    ClientResponseWrapper placeQueryResponseWrapper = odataOriginal.getPlaceQueryResponseWrapper();

	    Optional<ClientResponseWrapper> optHardwareResponseWrapper = odataOriginal.getHardwareResponseWrapper();

	    Optional<ClientResponseWrapper> optStatusResponseWrapper = odataOriginal.getStatusResponseWrapper();

	    Optional<ClientResponseWrapper> optOwnerResponseWrapper = odataOriginal.getOwnerResponseWrapper();

	    ClientResponseWrapper varAscendingQueryResponseWrapper = odataOriginal.getVarAscendingQueryResponseWrapper();

	    ClientResponseWrapper varDescendingQueryResponseWrapper = odataOriginal.getVarDescendingQueryResponseWrapper();

	    Variable variable = odataOriginal.getVariable();

	    Optional<String> optName = placeQueryResponseWrapper.getName(0);

	    Optional<String> optMunicipalityName = placeQueryResponseWrapper.getMunicipalityName(0);

	    Optional<String> optLakeName = placeQueryResponseWrapper.getLakeName(0);

	    Optional<String> optWaterAreaName = placeQueryResponseWrapper.getWaterAreaName(0);

	    Optional<String> optHardware = optHardwareResponseWrapper.isPresent() ? optHardwareResponseWrapper.get().getName(0)
		    : Optional.empty();

	    Optional<String> optStatus = optStatusResponseWrapper.isPresent() ? optStatusResponseWrapper.get().getName(0)
		    : Optional.empty();

	    Optional<String> optVariableId = placeQueryResponseWrapper.getVariableId(0);
	    Optional<String> optPlaceId = placeQueryResponseWrapper.getPlaceId(0);
	    Optional<String> optStationCodeId = placeQueryResponseWrapper.getStationCode(0);
	    Optional<String> optHardwareId = placeQueryResponseWrapper.getHardwareId(0);
	    Optional<String> optOwnerId = placeQueryResponseWrapper.getOwnerId(0);
	    Optional<String> optStatusId = placeQueryResponseWrapper.getStatusId(0);

	    //
	    // language
	    //
	    miMetadata.setLanguage("suomalainen");// finnish

	    //
	    // instrument
	    //
	    if (optHardware.isPresent()) {
		MIInstrument miInstrument = new MIInstrument();
		miInstrument.setSensorType(optHardware.get());
		if (optHardwareId.isPresent()) {
		    miInstrument.setMDIdentifierTypeCode(optHardwareId.get());
		}
		miMetadata.addMIInstrument(miInstrument);
	    }

	    //
	    // coverage description
	    //
	    CoverageDescription coverageDescription = new CoverageDescription();
	    coverageDescription.setAttributeIdentifier("urn:fi:syke:hydrology:variable:" + variable.getVariableId());
	    coverageDescription.setAttributeDescription(variable.getDescription());
	    coverageDescription.setAttributeTitle(variable.getVariableName());

	    miMetadata.addCoverageDescription(coverageDescription);

	    //
	    // platform info
	    //
	    String platformDesc = "";
	    String platformName = "";

	    if (optName.isPresent()) {
		platformDesc += "Paikka nimi: " + optName.get().trim();// place name
		platformName = optName.get().trim();
	    }

	    if (optLakeName.isPresent()) {
		platformDesc += ". Jarvi nimi: " + optLakeName.get().trim();// lake name
	    }

	    if (optWaterAreaName.isPresent()) {
		platformDesc += ". Vesal nimi: " + optWaterAreaName.get().trim();// water are name
	    }

	    MIPlatform miPlatform = new MIPlatform();
	    miPlatform.setDescription(platformDesc);
	    Citation citation2 = new Citation();
	    citation2.setTitle(platformName);
	    miPlatform.setCitation(citation2);
	    if (optStationCodeId.isPresent()) {
		miPlatform.setMDIdentifierCode(URN_SIKE + optStationCodeId.get());
	    } else {
		miPlatform.setMDIdentifierCode(optPlaceId.get());
	    }
	    miMetadata.addMIPlatform(miPlatform);

	    //
	    // keywords
	    //
	    miMetadata.getDataIdentification().addKeyword("Hydrography");
	    miMetadata.getDataIdentification().addKeyword("Hydrologia");
	    miMetadata.getDataIdentification().addKeyword("vesi");
	    miMetadata.getDataIdentification().addKeyword("SYKE");

	    if (optName.isPresent()) {
		miMetadata.getDataIdentification().addKeyword(optName.get().trim());
		Citation citation = new Citation();
		citation.setTitle(optName.get().trim());
		miPlatform.setCitation(citation);
	    }

	    if (optLakeName.isPresent()) {
		miMetadata.getDataIdentification().addKeyword(optLakeName.get().trim());
	    }

	    if (optWaterAreaName.isPresent()) {
		miMetadata.getDataIdentification().addKeyword(optWaterAreaName.get().trim());
	    }

	    if (optMunicipalityName.isPresent()) {
		miMetadata.getDataIdentification().addKeyword(optMunicipalityName.get());
	    }

	    //
	    // owner
	    //
	    Optional<String> optOwner = optOwnerResponseWrapper.isPresent() ? optOwnerResponseWrapper.get().getName(0) : Optional.empty();
	    if (optOwner.isPresent()) {

		String owner = optOwner.get();

		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("pointOfContact");
		responsibleParty.setIndividualName(owner);
		responsibleParty.setOrganisationName("Finnish Environment Insitute (SYKE)");		

		Contact contact = new Contact();
		Address address = new Address();

		address.addElectronicMailAddress("johanna.korhonen@ymparisto.fi");
		address.addElectronicMailAddress("hannu.sirvio@ymparisto.fi");
		contact.setAddress(address);

		responsibleParty.setContactInfo(contact);
		miMetadata.getDataIdentification().addPointOfContact(responsibleParty);
	    }

	    {
		//
		// publisher
		//
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("publisher");
		responsibleParty.setOrganisationName("Suomen ympäristökeskus");

		Contact contact = new Contact();

		Address address = new Address();

		address.addElectronicMailAddress("johanna.korhonen@ymparisto.fi");
		address.addElectronicMailAddress("hannu.sirvio@ymparisto.fi");
		contact.setAddress(address);

		responsibleParty.setContactInfo(contact);

		miMetadata.getDataIdentification().addCitationResponsibleParty(responsibleParty);
		miMetadata.addContact(responsibleParty);
	    }

	    //
	    // topic
	    //
	    miMetadata.getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.INLAND_WATERS);

	    //
	    // data quality
	    //
	    Iterator<DataQuality> dataQualities = miMetadata.getDataQualities();

	    DataQuality dataQuality = null;

	    if (!dataQualities.hasNext()) {

		dataQuality = new DataQuality();
		miMetadata.addDataQuality(dataQuality);

	    } else {

		dataQuality = dataQualities.next();
	    }

	    //
	    // data quality lineage
	    //
	    String statement = "Tuotteeseen liittyvä palvelutarjonta: hydrologisen tietokannan perusdata,";
	    statement += "datan tilastolliset analyysit, vesien käyttöä palvelevat";
	    statement += "laskentaohjelmistot(säännöstelylaskenta ja vaikutusten arviointi).";
	    statement += "Tuotteen tietojen lähde, ympäristöhallinnon hydrologinen havaintotoiminta,";
	    statement += "muiden vesioikeuslupien haltijoiden havaintotoiminta, ostopalvelusopimuksina saatavat";
	    statement += "havainnot (lähinnä Ilmatieteen laitos).";

	    dataQuality.setLineageStatement(statement);

	    //
	    // data quality scope
	    //
	    DQScopePropertyType dqScopePropertyType = new DQScopePropertyType();
	    DQScopeType dqScopeType = new DQScopeType();
	    MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
	    CodeListValueType valueType = ISOMetadata.createCodeListValueType(ISOMetadata.MX_SCOPE_CODE_CODELIST, "dataset",
		    ISOMetadata.ISO_19115_CODESPACE, "dataset");
	    mdScopeCodePropertyType.setMDScopeCode(ObjectFactories.GMD().createMDScopeCode(valueType));
	    dqScopeType.setLevel(mdScopeCodePropertyType);
	    dqScopePropertyType.setDQScope(dqScopeType);

	    DQDataQualityType dqElement = dataQuality.getElement().getValue();
	    dqElement.setScope(dqScopePropertyType);

	    //
	    // title
	    //
	    String title = platformName+": "+variable.getVariableName();

	    coreMetadata.setTitle(title);

	    //
	    // abstract
	    //
	    String abst = "ärjestelmä sisältää tietoja vesivarojen alueellisesta ja ajallisesta";
	    abst += "jakautumisesta Suomessa. Havaintoja tehdään hydrologisen kierron elementeistä (sadanta, haihdunta, virtaama ja valunta),";
	    abst += "veden määrästä (vesistöjen vedenkorkeus) ja muista hydrologisista ilmiöistä (lumen vesiarvo, jäänpaksuus, veden lämpötila, yms.).";
	    abst += "Tietojärjestelmä käsittää hydrologisen tietokannan sekä vesistöjen käyttötoimintaa tukevia tietoja ja ohjelmia. Tietokantaosa";
	    abst += " käsittää n. 10 hydrologista osarekisteriä, joissa on havaittuja tai laskettuja arvoja kaikkiaan noin 5000 mittauspisteeltä tai -alueelta.";
	    abst += "Järjestelmään pääsee Hertta ja Avoin tieto -palveluiden kautta.";

	    abst += "Tietojärjestelmää käytetään Suomen vesivarojen ja ajankohtaisen vesitilanteen kuvaukseen";
	    abst += "sekä arviointiin. Lisäksi järjestelmää käytetään monipuolisesti vesivarojen käytön ja hoidon tukemiseen";
	    abst += "(mm. vesistöennusteet ja vesistöjen käytön päätöksenteon tuki), tulvariskien hallintaan sekä vesiensuojeluun ja";
	    abst += "vesientutkimukseen liittyvissä tehtävissä. Ympäristöhallinnon sisäinen käyttö on hyvin laajaa ja monipuolista,";
	    abst += "mutta tietojärjestelmää käytetään myös paljon oman hallinnon ulkopuolella Avoin tieto –palvelun kautta";
	    abst += "(HTTP://WWW.SYKE.FI/AVOINTIETO). Palvelu on maksuton, mutta vaatii rekisteröitymisen ja käyttöehtojen hyväksymisen.";

	    coreMetadata.setAbstract(abst);

	    //
	    // temporal extent
	    //
	    Optional<String> optMinDate = varAscendingQueryResponseWrapper.getDate(0);
	    Optional<String> optMaxDate = varDescendingQueryResponseWrapper.getDate(0);

	    if (optMinDate.isPresent() && optMaxDate.isPresent()) {

		Date beginDate = ISO8601DateTimeUtils.parseISO8601(optMinDate.get());
		Date endDate = ISO8601DateTimeUtils.parseISO8601(optMaxDate.get());
		coreMetadata.addTemporalExtent(optMinDate.get(), optMaxDate.get());
		setIndeterminatePosition(dataset);

		// Estimate of the data size
		// only an estimate seems to be possible, as this odata service doesn't seem to support the /$count
		// operator

		long expectedSize = TimeSeriesUtils.estimateSize(beginDate, endDate);

		GridSpatialRepresentation grid = new GridSpatialRepresentation();
		grid.setNumberOfDimensions(1);
		grid.setCellGeometryCode("point");
		Dimension time = new Dimension();
		time.setDimensionNameTypeCode("time");
		try {
		    time.setDimensionSize(new BigInteger("" + expectedSize));
		    ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		    extensionHandler.setDataSize(expectedSize);
		    variable.getDescription();
		    extensionHandler.setAttributeUnits(variable.getUnits());
		    extensionHandler.setAttributeUnitsAbbreviation(variable.getUnits());
		   
		} catch (Exception e) {
		}
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    }

	    //
	    // spatial extent
	    //
	    Optional<String> optLat = placeQueryResponseWrapper.getLat(0);
	    Optional<String> optLon = placeQueryResponseWrapper.getLon(0);

	    if (optLat.isPresent() && optLon.isPresent()) {

		double koordErTmPohj = Double.valueOf(optLat.get());
		double koordErTmIta = Double.valueOf(optLon.get());

		SimpleEntry<Double, Double> latLon = getLatLon(koordErTmPohj, koordErTmIta);

		coreMetadata.addBoundingBox(latLon.getKey(), latLon.getValue(), latLon.getKey(), latLon.getValue());
	    }

	    //
	    // security constraints
	    //
	    LegalConstraints legalConstraints = new LegalConstraints();
	    String limitation = "Avoin tieto -palvelusta tietojärjestelmän kautta saatavaa aineistoa koskee";
	    limitation += "Creative Commons Nimeä 4.0 Kansainvälinen -lisenssi";
	    limitation += "CHTTP://WWW.SYKE.FI/FI-FI/AVOIN_TIETO/KAYTTOLUPA_JA_VASTUUT";

	    legalConstraints.addUseLimitation(limitation);
	    legalConstraints.addAccessConstraintsCode("Lisenssi");
	    legalConstraints.addUseConstraintsCode("Lisenssi");
	    // Classification: Unclassified
	    // Copyright: Lähde: SYKE

	    miMetadata.getDataIdentification().addLegalConstraints(legalConstraints);

	    //
	    // distribution info
	    //
	    coreMetadata.addDistributionFormat("Ympäristötietojärjestelmä");
	    coreMetadata.addDistributionOnlineResource("SYKE", "https://www.syke.fi/avointieto", "http", "information");
	    
	    dataset.getExtensionHandler().setCountry(Country.FINLAND.getShortName());

	    SYKEIdentifierMangler mangler = new SYKEIdentifierMangler();
	    mangler.setParameterIdentifier(optVariableId.get());
	    mangler.setPlatformIdentifier(optPlaceId.get());
	    coreMetadata.addDistributionOnlineResource(mangler.getMangling(), ODataHidrologyClient.DEFAULT_URL,
		    NetProtocolWrapper.ODATA_SYKE.getCommonURN(), "download");

	} catch (Exception ex) {
	    ex.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ODATA_HIDROLOGY_MAPPER_ERROR, //
		    ex);
	}

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ODATA_HIDROLOGY_SCHEME_URI;
    }

    /**
     * Returns a pair latitude longitude point from a pair north-east point in the finnish CRS EPSG:5048
     * 
     * @param koordErTmPohj the north value
     * @param koordErTmIta the east value
     * @return
     */
    public static SimpleEntry<Double, Double> getLatLon(double koordErTmPohj, double koordErTmIta) {

	try {
	    MathTransform transform = getMathTransform();
	    Position2D ptSrc = new Position2D(koordErTmPohj, koordErTmIta);
	    Position ptDst = transform.transform(ptSrc, null);
	    SimpleEntry<Double, Double> ret = new SimpleEntry<Double, Double>(ptDst.getOrdinate(0), ptDst.getOrdinate(1));
	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;

    }

    private static MathTransform getMathTransform() {
	if (transform == null) {
	    try {
		CoordinateReferenceSystem finnishCRS = CRS.decode("EPSG:5048");
		CoordinateReferenceSystem latLonCRS = CRS.decode("EPSG:4326");
		transform = CRS.findMathTransform(finnishCRS, latLonCRS, true);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return transform;
    }

    private static MathTransform transform = null;

}
