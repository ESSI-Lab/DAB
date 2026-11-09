package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * Maps the records of the {@link HISCentralLombardiaOpenDataConnector}, time series and rating curves, to
 * {@link Dataset}s
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataMapper extends FileIdentifierMapper {

    /**
     * Key added by the connector to the hydrometric zeros features, with the value read from the gauge monograph
     */
    static final String HYDROMETRIC_ZERO = "zeroIdrometrico";

    public static final String MISSING_VALUE = "-9999";

    private static final String ORGANIZATION = "ARPA Lombardia";
    private static final String LICENSE = "Creative Commons 1.0 Universal (Public Domain Dedication)";

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject record = new JSONObject(originalMD.getMetadata());

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	if (HISCentralLombardiaOpenDataConnector.RATING_CURVE.equals(record.optString("type"))) {
	    mapRatingCurve(record, dataset);
	} else {
	    mapTimeSeries(record, dataset);
	}

	return dataset;
    }

    private void mapTimeSeries(JSONObject record, Dataset dataset) {

	JSONObject sensor = record.getJSONObject("sensor");
	LombardiaOpenDataProperty property = LombardiaOpenDataProperty.valueOf(record.getString("property"));

	String sensorId = sensor.getString("idsensore");
	String stationName = sensor.optString("nomestazione");
	String units = sensor.optString("unit_dimisura", null);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	mapStation(dataset, sensor.optString("idstazione"), stationName, sensor.optString("provincia", null), //
		number(sensor.opt("lat")), number(sensor.opt("lng")), number(sensor.opt("quota")));

	String title = stationName + " - " + property.getLabel();
	miMetadata.getDataIdentification().setCitationTitle(title);
	miMetadata.getDataIdentification().setAbstract(title + ". " + property.getInterpolation().getLabel() + " values of sensor "
		+ sensorId + " (" + sensor.optString("tipologia") + ") of the hydro-nivo-meteorological network of " + ORGANIZATION
		+ ", station " + stationName + (sensor.has("provincia") ? " (" + sensor.getString("provincia") + ")" : "")
		+ ". Validated archives and real time data published on the Regione Lombardia open data portal.");

	miMetadata.getDataIdentification().addKeywords(keywords(property.getLabel(), sensor.optString("tipologia"), stationName,
		sensor.optString("provincia", null), "LOMBARDIA", ORGANIZATION));

	switch (property) {
	case LIVELLO_IDROMETRICO:
	case PORTATA:
	case ALTEZZA_NEVE:
	    miMetadata.getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.INLAND_WATERS);
	    break;
	default:
	    miMetadata.getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE);
	    break;
	}

	//
	// temporal extent, from the sensor registry
	//
	TemporalExtent temporalExtent = new TemporalExtent();
	String begin = toUTC(sensor.optString("datastart", null));
	if (begin != null) {
	    temporalExtent.setBeginPosition(begin);
	}
	String end = toUTC(sensor.optString("datastop", null));
	if (end != null && "S".equalsIgnoreCase(sensor.optString("storico"))) {
	    temporalExtent.setEndPosition(end);
	} else {
	    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	}
	miMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	setIndeterminatePosition(dataset);

	//
	// time series properties
	//
	dataset.getExtensionHandler().setTimeInterpolation(property.getInterpolation());
	if (record.has("recentCount")) {
	    String resolution = getResolution(record.getInt("recentCount"), record.getInt("recentDays"));
	    if (resolution != null) {
		dataset.getExtensionHandler().setTimeResolutionDuration8601(resolution);
		dataset.getExtensionHandler().setTimeAggregationDuration8601(resolution);
	    }
	}
	dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
	if (units != null && !units.isBlank()) {
	    dataset.getExtensionHandler().setAttributeUnits(units);
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(units);
	}
	if (record.has("river")) {
	    dataset.getExtensionHandler().setRiver(record.getString("river"));
	}
	mapHydrometricZero(record, dataset);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier("dati.lombardia.it:" + property.name());
	coverageDescription.setAttributeTitle(property.getLabel());
	coverageDescription.setAttributeDescription(property.getLabel() + (units != null ? " (" + units + ")" : ""));
	miMetadata.addCoverageDescription(coverageDescription);

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	Dimension time = new Dimension();
	time.setDimensionNameTypeCode("time");
	grid.addAxisDimension(time);
	miMetadata.addGridSpatialRepresentation(grid);

	HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
	mangler.setSensorIdentifier(sensorId);
	mangler.setProperty(property.name());

	mapIdentifiers(dataset, mangler.getMangling(), dataset.getSource().getEndpoint(), CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI);
    }

    private void mapRatingCurve(JSONObject record, Dataset dataset) {

	String sensorId = record.getString("sensorId");
	JSONArray pieces = record.getJSONArray("ratingCurves");
	JSONObject firstPiece = pieces.getJSONObject(0);
	JSONObject sensor = record.optJSONObject("sensor");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	String stationName;
	if (sensor != null) {
	    stationName = sensor.optString("nomestazione");
	    mapStation(dataset, sensor.optString("idstazione"), stationName, sensor.optString("provincia", null), //
		    number(sensor.opt("lat")), number(sensor.opt("lng")), number(sensor.opt("quota")));
	} else {
	    // gauge not in the open data registry: the station is described by the WFS feature, e.g. "Asola // v. Carducci"
	    stationName = firstPiece.optString("Nome").replace(" // ", " ");
	    mapStation(dataset, sensorId, stationName, null, number(firstPiece.opt("lat")), number(firstPiece.opt("lon")), null);
	}

	String river = record.optString("river", null);
	String title = stationName + " - Scala di deflusso";
	miMetadata.getDataIdentification().setCitationTitle(title);
	miMetadata.getDataIdentification().setAbstract(title + ". Rating curves (stage in m, discharge in m3/s) of the river level sensor "
		+ sensorId + (river != null ? " on the " + river + " river" : "") + ", published by " + ORGANIZATION
		+ " as piece-wise formulas with their validity periods.");

	miMetadata.getDataIdentification().addKeywords(keywords("Scala di deflusso", "Rating curve", stationName, river, "LOMBARDIA", ORGANIZATION));
	miMetadata.getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.INLAND_WATERS);

	//
	// temporal extent, from the validity periods
	//
	LocalDate begin = null;
	LocalDate end = null;
	boolean current = false;
	for (int i = 0; i < pieces.length(); i++) {
	    LocalDate[] period = LombardiaWFSClient.parsePeriod(pieces.getJSONObject(i).optString("periodo"));
	    if (period[0] != null && (begin == null || period[0].isBefore(begin))) {
		begin = period[0];
	    }
	    if (period[1] == null) {
		current = true;
	    } else if (end == null || period[1].isAfter(end)) {
		end = period[1];
	    }
	}
	TemporalExtent temporalExtent = new TemporalExtent();
	if (begin != null) {
	    temporalExtent.setBeginPosition(begin.toString());
	}
	if (current || end == null) {
	    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	} else {
	    temporalExtent.setEndPosition(end.toString());
	}
	miMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	if (river != null) {
	    dataset.getExtensionHandler().setRiver(river);
	}
	mapHydrometricZero(record, dataset);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier("dati.lombardia.it:" + HISCentralLombardiaOpenDataMangler.RATING_CURVE);
	coverageDescription.setAttributeTitle("Scala di deflusso");
	coverageDescription.setAttributeDescription("Scala di deflusso");
	miMetadata.addCoverageDescription(coverageDescription);

	dataset.getPropertyHandler().setIsRatingCurve(true);

	HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
	mangler.setSensorIdentifier(sensorId);
	mangler.setProperty(HISCentralLombardiaOpenDataMangler.RATING_CURVE);

	String linkage = LombardiaWFSClient.getRatingCurvesURL(record.getString("wfsEndpoint"), sensorId);

	mapIdentifiers(dataset, mangler.getMangling(), linkage, CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_SCALE_DEFLUSSO_NS_URI);
    }

    /**
     * Station information shared by time series and rating curves
     */
    private void mapStation(Dataset dataset, String stationId, String stationName, String province, BigDecimal lat, BigDecimal lon,
	    BigDecimal altitude) {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	MIPlatform platform = new MIPlatform();
	platform.setMDIdentifierCode(stationId);
	platform.setDescription(stationName + (province != null ? " (" + province + ")" : ""));
	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);
	miMetadata.addMIPlatform(platform);

	Keywords platformKeywords = new Keywords();
	platformKeywords.setTypeCode("platform");
	platformKeywords.addKeyword(stationName);
	miMetadata.getDataIdentification().addKeywords(platformKeywords);

	if (lat != null && lon != null) {
	    miMetadata.getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);
	}
	if (altitude != null) {
	    miMetadata.getDataIdentification().addVerticalExtent(altitude.doubleValue(), altitude.doubleValue());
	}

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	miMetadata.addReferenceSystemInfo(referenceSystem);

	ResponsibleParty publisher = new ResponsibleParty();
	publisher.setOrganisationName(ORGANIZATION);
	publisher.setRoleCode("publisher");
	miMetadata.getDataIdentification().addPointOfContact(publisher);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addAccessConstraintsCode("other");
	constraints.addUseLimitation(LICENSE);
	miMetadata.getDataIdentification().addLegalConstraints(constraints);

	dataset.getExtensionHandler().setCountry(Country.ITALY.getShortName());

	miMetadata.setLanguage("Italian");
	miMetadata.setCharacterSetCode("utf8");
	miMetadata.addHierarchyLevelScopeCodeListValue("dataset");
    }

    private void mapHydrometricZero(JSONObject record, Dataset dataset) {

	JSONObject zero = record.optJSONObject("hydrometricZero");
	if (zero != null && zero.has(HYDROMETRIC_ZERO)) {
	    dataset.getExtensionHandler().setHydrometricZero(zero.getString(HYDROMETRIC_ZERO));
	}
    }

    private void mapIdentifiers(Dataset dataset, String mangling, String linkage, String protocol) {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	try {
	    String id = StringUtils.hashSHA1messageDigest("HISCentralLombardiaOpenData:" + mangling);
	    coreMetadata.setIdentifier(id);
	    coreMetadata.getMIMetadata().setFileIdentifier(id);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    logger.error("Error generating identifier", e);
	}

	coreMetadata.addDistributionOnlineResource(mangling, linkage, protocol, "download");

	String resourceIdentifier = generateCode(dataset, mangling);
	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
    }

    private static Keywords keywords(String... values) {
	Keywords keywords = new Keywords();
	List<String> list = Arrays.stream(values).filter(Objects::nonNull).filter(v -> !v.isBlank()).distinct().collect(Collectors.toList());
	keywords.addKeywords(list);
	return keywords;
    }

    /**
     * @param count number of observations in the given days
     * @param days number of days
     * @return the ISO 8601 duration of the time step, rounded to the closest usual step
     */
    static String getResolution(int count, int days) {

	if (count <= 0 || days <= 0) {
	    return null;
	}
	double stepMinutes = days * 1440.0 / count;
	int[] steps = { 5, 10, 15, 30, 60, 180, 360, 1440 };
	String[] durations = { "PT5M", "PT10M", "PT15M", "PT30M", "PT1H", "PT3H", "PT6H", "P1D" };
	int best = 0;
	for (int i = 1; i < steps.length; i++) {
	    if (Math.abs(Math.log(stepMinutes / steps[i])) < Math.abs(Math.log(stepMinutes / steps[best]))) {
		best = i;
	    }
	}
	return durations[best];
    }

    /**
     * @param timestamp a registry timestamp in the data time zone, e.g. 2012-04-02T00:00:00.000
     * @return the ISO 8601 UTC time
     */
    static String toUTC(String timestamp) {
	if (timestamp == null || timestamp.isBlank()) {
	    return null;
	}
	try {
	    return LombardiaOpenDataClient.parse(timestamp).atOffset(LombardiaOpenDataClient.DATA_OFFSET).toInstant().toString();
	} catch (Exception e) {
	    return null;
	}
    }

    private static BigDecimal number(Object value) {
	if (value == null || value == JSONObject.NULL) {
	    return null;
	}
	try {
	    return new BigDecimal(value.toString().trim());
	} catch (NumberFormatException e) {
	    return null;
	}
    }
}
