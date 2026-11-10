package eu.essi_lab.accessor.adhi;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class ADHIMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String ADHI_SCHEMA = "https://doi.org/10.23708/LXGXQ9/schema";

    static final String CC_BY_4 = "Creative Commons Attribution 4.0 International (CC BY 4.0) - https://creativecommons.org/licenses/by/4.0/";

    static final String CITATION = "Tramblay, Y., Rouché, N., Paturel, J.-E., Mahé, G., Boyer, J.-F., Amoussou, E., Bodian, A.,"
	    + " Dacosta, H., Dakhlaoui, H., Dezetter, A., Hughes, D., Hanich, L., Peugeot, C., Tshimanga, R., and Lachassagne, P. (2021):"
	    + " ADHI: the African Database of Hydrometric Indices (1950-2018), Earth Syst. Sci. Data, 13, 1547-1560,"
	    + " https://doi.org/10.5194/essd-13-1547-2021";

    static final String ORGANIZATION = "Institut de Recherche pour le Développement (IRD) - UMR HydroSciences Montpellier";

    static final String CONTACT_NAME = "Yves Tramblay";

    static final String CONTACT_EMAIL = "yves.tramblay@ird.fr";

    static final String UNITS = "cubic metres per second";

    static final String UNITS_ABBREVIATION = "m3/s";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	ADHIEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(ADHIEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationId = entity.getStationId();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationId));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(ADHIClient.BEGIN, null);
	String end = object.optString(ADHIClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setIndividualName(CONTACT_NAME);
	party.setRoleCode("pointOfContact");
	Contact contact = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress(CONTACT_EMAIL);
	contact.setAddress(address);
	party.setContactInfo(contact);
	dataId.addPointOfContact(party);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(getLicense(entity) + ". Please cite: " + CITATION);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract(createAbstract(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(ADHIClient.LAT) && object.has(ADHIClient.LON)) {
	    try {
		BigDecimal lat = new BigDecimal(object.get(ADHIClient.LAT).toString());
		BigDecimal lon = new BigDecimal(object.get(ADHIClient.LON).toString());
		coreMetadata.addBoundingBox(lat, lon, lat, lon);
	    } catch (NumberFormatException e) {
		// invalid coordinates
	    }
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.ADHI.getCommonURN(), //
		"download");

	// historical dataset (1950-2018): the end position is never indeterminate

	return dataset;
    }

    /**
     * @param entity
     * @return
     */
    static String createAbstract(ADHIEntity entity) {

	StringBuilder builder = new StringBuilder();

	builder.append("Time series of ").append(getPropertyDescription(entity));
	builder.append(" at station ").append(entity.getStationName()).append(getStationDescription(entity));
	builder.append(", from ADHI, the African Database of Hydrometric Indices (1950-2018)");

	String version = entity.getString(ADHIClient.DATASET_VERSION);
	builder.append(", dataset ").append(ADHIClient.DATASET_DOI_URL);
	if (!version.isEmpty()) {
	    builder.append(" version ").append(version);
	}
	builder.append(". ");

	builder.append("ADHI was compiled by IRD (UMR HydroSciences Montpellier) and partners from daily discharge records of");
	builder.append(" the GRDC and SIEREM databases and of national agencies.");

	entity.getVariable().ifPresent(variable -> {
	    if (variable.isMonthly()) {
		builder.append(" Monthly values are reported at the first day of the month; months without values are omitted,");
		builder.append(" while months with some missing days are included (the number of missing days is given in the");
		builder.append(" ADHI files).");
	    } else {
		builder.append(" Annual values are reported at the first day of the year; years without values are omitted.");
		if (variable == ADHIVariable.ANNUAL_MAX || variable == ADHIVariable.ANNUAL_7DAY_MIN) {
		    builder.append(" The date of occurrence of each value is given in the ADHI files.");
		}
	    }
	});

	String quality = getQualityDescription(entity);
	if (!quality.isEmpty()) {
	    builder.append(" Data quality: ").append(quality).append(".");
	}

	builder.append(" Citation: ").append(CITATION).append(".");

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. "Monthly mean discharge"
     */
    static String getPropertyName(ADHIEntity entity) {

	return entity.getVariable().map(ADHIVariable::getLabel).orElse("Discharge");
    }

    /**
     * @param entity
     * @return e.g. "monthly mean discharge (mean of the daily discharges of the month, m3/s)"
     */
    static String getPropertyDescription(ADHIEntity entity) {

	return getPropertyName(entity).toLowerCase() + " ("
		+ entity.getVariable().map(v -> v.getDescription() + ", ").orElse("") + UNITS_ABBREVIATION + ")";
    }

    /**
     * @param entity
     * @return e.g. " (ADHI_1, code CMQ0001, Cameroon, catchment area 1566.6 km2, data source SIEREM)"
     */
    static String getStationDescription(ADHIEntity entity) {

	StringBuilder builder = new StringBuilder(" (").append(entity.getStationId());

	String code = entity.getStationCode();
	if (!code.isEmpty()) {
	    builder.append(", code ").append(code);
	}

	String country = entity.getString(ADHIClient.COUNTRY);
	if (!country.isEmpty()) {
	    builder.append(", ").append(country);
	}

	String area = entity.getString(ADHIClient.CATCHMENT_AREA);
	if (!area.isEmpty()) {
	    try {
		builder.append(", catchment area ").append(new BigDecimal(area).setScale(1, RoundingMode.HALF_UP).toPlainString())
			.append(" km2");
	    } catch (NumberFormatException e) {
		builder.append(", catchment area ").append(area).append(" km2");
	    }
	}

	String dams = entity.getString(ADHIClient.NUMBER_OF_DAMS);
	if (!dams.isEmpty() && !dams.equals("0")) {
	    builder.append(", ").append(dams).append(dams.equals("1") ? " dam" : " dams").append(" in the catchment");
	}

	String source = entity.getString(ADHIClient.DATA_SOURCE);
	if (!source.isEmpty()) {
	    builder.append(", data source ").append(source);
	}

	return builder.append(")").toString();
    }

    /**
     * @param entity
     * @return the description of the data quality flag of the station, empty if no issue was identified
     */
    static String getQualityDescription(ADHIEntity entity) {

	String flag = entity.getString(ADHIClient.FLAG);
	String comment = entity.getString(ADHIClient.COMMENT);

	String description;
	switch (flag) {
	case "1":
	    description = "some gap filling detected";
	    break;
	case "2":
	    description = "suspicious data";
	    break;
	case "3":
	    description = "obvious regime break";
	    break;
	default:
	    description = "";
	}

	if (!comment.isEmpty() && !comment.equalsIgnoreCase(description)) {
	    description = description.isEmpty() ? comment : description + " (" + comment + ")";
	}

	return description;
    }

    /**
     * @param entity
     * @return
     */
    static String getLicense(ADHIEntity entity) {

	String license = entity.getString(ADHIClient.LICENSE);
	return license.isEmpty() ? CC_BY_4 : license;
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(ADHIEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, "Discharge");
	addKeyword(keywords, entity.getString(ADHIClient.COUNTRY));
	addKeyword(keywords, entity.getString(ADHIClient.DATA_SOURCE));

	addKeyword(keywords, "Africa");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Hydrometric indices");
	addKeyword(keywords, "ADHI");
    }

    /**
     * @param keywords
     * @param value
     */
    private void addKeyword(Keywords keywords, String value) {

	if (value != null && !value.isEmpty()) {
	    keywords.addKeyword(value);
	}
    }

    /**
     * @param entity
     * @param coreMetadata
     */
    private void addPlatform(ADHIEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationName + getStationDescription(entity));
	platform.setMDIdentifierCode(NetProtocolWrapper.ADHI.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(ADHIEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.ADHI.getCommonURN() + ":" + entity.getVariable().map(ADHIVariable::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(ADHIEntity entity, ExtensionHandler handler) {

	handler.setAttributeUnits(UNITS);
	handler.setAttributeUnitsAbbreviation(UNITS_ABBREVIATION);

	String country = entity.getString(ADHIClient.COUNTRY);
	if (!country.isEmpty()) {
	    handler.setCountry(country);
	}

	String quality = getQualityDescription(entity);
	if (!quality.isEmpty()) {
	    handler.setDataDisclaimer("ADHI data quality flag: " + quality);
	}

	entity.getVariable().ifPresent(variable -> {
	    String duration = variable.isMonthly() ? "P1M" : "P1Y";
	    handler.setTimeResolutionDuration8601(duration);
	    handler.setTimeAggregationDuration8601(duration);
	    handler.setTimeInterpolation(variable.getInterpolation());
	});
    }

    /**
     * @param originalMD
     * @return
     */
    private ADHIEntity getEntity(OriginalMetadata originalMD) {

	return new ADHIEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ADHI_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	ADHIEntity entity = getEntity(resource.getOriginalMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), entity.getSeriesId());
    }

    /**
     * @param endpoint
     * @param entityId
     * @return
     */
    private String decorateIdentifier(String endpoint, String entityId) {

	try {
	    return StringUtils.hashSHA1messageDigest(endpoint + entityId);
	} catch (Exception e) {
	    String id = endpoint + entityId;
	    id = id.replace("https:", "");
	    id = id.replace("http:", "");
	    id = id.replace("//", "");
	    id = id.replace("/", "");
	    return StringUtils.encodeUTF8(id);
	}
    }
}
