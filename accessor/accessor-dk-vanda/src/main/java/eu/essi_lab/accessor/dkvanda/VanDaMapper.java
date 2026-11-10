package eu.essi_lab.accessor.dkvanda;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
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
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class VanDaMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String VANDA_SCHEMA = "https://vandah.miljoeportal.dk/api/schema";

    static final String LICENSE = "Terms of use of Danish public data (Vilkår for brug af danske offentlige data) -"
	    + " https://miljoeportal.dk/dataansvar/vilkaar-for-brug/ - Free, worldwide, non-exclusive and unlimited right of use,"
	    + " including commercial use (comparable to CC0 / CC BY). Attribution: \"Indeholder data, som benyttes i henhold til"
	    + " vilkår for brug af danske offentlige data\" (contains data used under the terms of use for Danish public data)."
	    + " Data are provided as they are, without warranty, by Danmarks Miljøportal and the responsible authorities.";

    static final String ORGANIZATION = "Danmarks Miljøportal";

    /**
     * Examination types of manual (field) measurements
     */
    static final Set<Integer> MANUAL_EXAMINATION_TYPES = new HashSet<>(Arrays.asList(21, 22, 24, 30));

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	VanDaEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(VanDaEntity entity, GSSource source) {

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
	String begin = object.optString(VanDaClient.BEGIN, null);
	String end = object.optString(VanDaClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String owner = entity.getString(VanDaClient.STATION_OWNER);
	if (!owner.isEmpty()) {
	    ResponsibleParty ownerParty = new ResponsibleParty();
	    ownerParty.setOrganisationName(owner);
	    ownerParty.setRoleCode("owner");
	    dataId.addPointOfContact(ownerParty);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + getPointLabel(entity) + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at " + getPointDescription(entity)
		+ ", from the VanDa hydrometric database (Hydro API) of " + ORGANIZATION + " (Denmark)" + getOwnerDescription(entity)
		+ getValuesDescription(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(VanDaClient.LAT) && object.has(VanDaClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(VanDaClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(VanDaClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.VANDA.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the English name of the observed property, e.g. "Water level"
     */
    static String getPropertyName(VanDaEntity entity) {

	switch (entity.getExaminationTypeCode()) {
	case 21:
	    return "Gauge datum elevation";
	case 22:
	    return "Water level (staff gauge reading)";
	case 24:
	    return "Discharge (field measurement)";
	case 25:
	    return entity.getParameterCode() == 439 ? "Water level (DVR90)" : "Water level";
	case 27:
	    return "Discharge";
	case 28:
	    return "Dissolved oxygen";
	case 29:
	    return "Water temperature";
	case 30:
	    return "Groundwater level (manual sounding)";
	case 31:
	    return "Groundwater level";
	case 32:
	    return "Electrical conductivity";
	case 33:
	    return "Oxygen saturation";
	case 34:
	    return "Salinity";
	case 42:
	    return "Data logger battery voltage";
	default:
	    String name = entity.getString(VanDaClient.EXAMINATION_TYPE);
	    return name.isEmpty() ? entity.getString(VanDaClient.PARAMETER) : name;
	}
    }

    /**
     * @param entity
     * @return the property name with the original VanDa names, e.g. "Water level (VanDa parameter Vandstand, examination type
     *         Vandstand)"
     */
    static String getPropertyDescription(VanDaEntity entity) {

	return getPropertyName(entity) + " (VanDa parameter " + entity.getString(VanDaClient.PARAMETER) + ", examination type "
		+ entity.getString(VanDaClient.EXAMINATION_TYPE) + ")";
    }

    /**
     * @param entity
     * @return e.g. " (measurement point 2)" if the station has more than one measurement point, otherwise an empty string
     */
    private static String getPointLabel(VanDaEntity entity) {

	if (entity.getObject().optInt(VanDaClient.MEASUREMENT_POINTS_COUNT, 1) > 1) {
	    return " (measurement point " + entity.getMeasurementPointNumber() + ")";
	}

	return "";
    }

    /**
     * @param entity
     * @return
     */
    private static String getPointDescription(VanDaEntity entity) {

	StringBuilder builder = new StringBuilder();

	builder.append("measurement point ").append(entity.getMeasurementPointNumber());

	String name = entity.getString(VanDaClient.MEASUREMENT_POINT_NAME_KEY);
	String type = entity.getString(VanDaClient.MEASUREMENT_POINT_TYPE);
	if (!name.isEmpty() || !type.isEmpty()) {
	    builder.append(" (").append(String.join(", ", Arrays.stream(new String[] { name, type }).//
		    filter(s -> !s.isEmpty()).toArray(String[]::new))).append(")");
	}

	builder.append(" of station ").append(entity.getStationName()).append(" (").append(entity.getStationId()).append(")");

	String locationType = entity.getString(VanDaClient.LOCATION_TYPE);
	if (!locationType.isEmpty()) {
	    builder.append(", location type ").append(locationType);
	}

	String description = entity.getString(VanDaClient.STATION_DESCRIPTION);
	if (!description.isEmpty()) {
	    builder.append(", ").append(description);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return
     */
    private static String getOwnerDescription(VanDaEntity entity) {

	String owner = entity.getString(VanDaClient.STATION_OWNER);
	String operator = entity.getString(VanDaClient.OPERATOR);

	StringBuilder builder = new StringBuilder();
	if (!owner.isEmpty()) {
	    builder.append(". Station owner: ").append(owner);
	}
	if (!operator.isEmpty()) {
	    builder.append(owner.isEmpty() ? ". " : ", ").append("operator: ").append(operator);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return
     */
    private static String getValuesDescription(VanDaEntity entity) {

	StringBuilder builder = new StringBuilder(". Values in ").append(entity.getUnit());

	switch (entity.getExaminationTypeCode()) {
	case 22:
	case 25:
	    if (entity.getParameterCode() != 439) {
		builder.append(", referred to the local gauge zero");
	    }
	    break;
	default:
	}

	if (MANUAL_EXAMINATION_TYPES.contains(entity.getExaminationTypeCode())) {
	    builder.append(". Manual measurements at irregular times");
	}

	return builder.append(".").toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(VanDaEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getString(VanDaClient.PARAMETER));
	addKeyword(keywords, entity.getString(VanDaClient.EXAMINATION_TYPE));
	addKeyword(keywords, entity.getString(VanDaClient.LOCATION_TYPE));
	addKeyword(keywords, entity.getString(VanDaClient.STATION_OWNER));
	addKeyword(keywords, entity.getString(VanDaClient.OPERATOR));

	addKeyword(keywords, "Denmark");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "VanDa");
	addKeyword(keywords, ORGANIZATION);
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
    private void addPlatform(VanDaEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String locationType = entity.getString(VanDaClient.LOCATION_TYPE);
	platform.setDescription("Station " + stationName + " (" + entity.getStationId() + ")"
		+ (locationType.isEmpty() ? "" : ", location type " + locationType) + " (Denmark)");
	platform.setMDIdentifierCode(NetProtocolWrapper.VANDA.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(VanDaEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.VANDA.getCommonURN() + ":" + entity.getExaminationTypeCode() + ":" + entity.getParameterCode());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(VanDaEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Denmark");

	if (MANUAL_EXAMINATION_TYPES.contains(entity.getExaminationTypeCode())) {
	    handler.setTimeInterpolation(InterpolationType.DISCONTINUOUS);
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param unit the unit used by VanDa
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m":
	    return "metres";
	case "l/s":
	    return "litres per second";
	case "grader C":
	    return "degrees Celsius";
	case "mg/l":
	    return "milligrams per litre";
	case "promille":
	    return "per mille";
	case "pct":
	    return "percent";
	case "mS/cm":
	    return "millisiemens per centimetre";
	case "Volt":
	    return "volts";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by VanDa
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "grader C":
	    return "°C";
	case "promille":
	    return "‰";
	case "pct":
	    return "%";
	case "Volt":
	    return "V";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private VanDaEntity getEntity(OriginalMetadata originalMD) {

	return new VanDaEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return VANDA_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	VanDaEntity entity = getEntity(resource.getOriginalMetadata());
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
