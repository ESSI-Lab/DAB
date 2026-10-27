package eu.essi_lab.accessor.ieopw;

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
public class OPWMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String OPW_SCHEMA = "https://waterlevel.ie/hydro-data/schema";

    static final String LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - https://waterlevel.ie/page/api/ -"
	    + " Contains Irish Public Sector Information licensed under a Creative Commons Attribution 4.0 International (CC BY 4.0)"
	    + " licence (source http://waterlevel.ie - provided by the Office of Public Works). Real time data are provisional.";

    static final String ORGANIZATION = "Office of Public Works (OPW)";

    /**
     * Resolution of the observations of the archives and of the feeds
     */
    static final String RESOLUTION = "PT15M";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	OPWEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(OPWEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationNumber = entity.getStationNumber();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationNumber));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(OPWClient.BEGIN, null);
	String end = object.optString(OPWClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at station " + stationName + " ("
		+ stationNumber + ")" + getWaterbodyDescription(entity) + " from the Hydro-Data service of the " + ORGANIZATION
		+ " of Ireland");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(OPWClient.LAT) && object.has(OPWClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(OPWClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(OPWClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.OPW.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(OPWEntity entity) {

	return entity.getParameter().map(OPWParameter::getLabel).orElse(entity.getString(OPWClient.PARAMETER_LONG_NAME));
    }

    /**
     * @param entity
     * @return the property name, with the datum of the water level
     */
    static String getPropertyDescription(OPWEntity entity) {

	String name = getPropertyName(entity);
	String datum = entity.getString(OPWClient.GAUGE_DATUM_UNIT);

	if (entity.getParameter().filter(p -> p == OPWParameter.S).isPresent() && !datum.isEmpty()) {
	    return name + " (referred to " + datum + ")";
	}

	return name;
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(OPWEntity entity) {

	String waterbody = entity.getString(OPWClient.WATERBODY);
	String catchment = entity.getString(OPWClient.CATCHMENT);

	StringBuilder builder = new StringBuilder();
	if (!waterbody.isEmpty()) {
	    builder.append(", waterbody ").append(waterbody);
	}
	if (!catchment.isEmpty()) {
	    builder.append(", catchment ").append(catchment);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(OPWEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getString(OPWClient.WATERBODY));
	addKeyword(keywords, entity.getString(OPWClient.CATCHMENT));
	addKeyword(keywords, entity.getString(OPWClient.RIVER_BASIN_DISTRICT));

	addKeyword(keywords, "Ireland");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "OPW");
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
    private void addPlatform(OPWEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationName + " (" + entity.getStationNumber() + ")" + getWaterbodyDescription(entity)
		+ " (Ireland)");
	platform.setMDIdentifierCode(NetProtocolWrapper.OPW.getCommonURN() + "/" + entity.getStationNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(OPWEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.OPW.getCommonURN() + ":" + entity.getParameter().map(OPWParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(OPWEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Ireland");
	handler.setTimeResolutionDuration8601(RESOLUTION);

	if (entity.getParameter().filter(p -> p == OPWParameter.P).isPresent()) {
	    // 24 hours moving total
	    handler.setTimeInterpolation(InterpolationType.TOTAL_PREC);
	    handler.setTimeAggregationDuration8601("P1D");
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param unit the unit symbol used by OPW
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "m":
	    return "metres";
	case "cumec":
	    return "cubic metres per second";
	case "°C":
	    return "degrees Celsius";
	case "mm":
	    return "millimetres";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit symbol used by OPW
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	return unit.equals("cumec") ? "m3/s" : unit;
    }

    /**
     * @param originalMD
     * @return
     */
    private OPWEntity getEntity(OriginalMetadata originalMD) {

	return new OPWEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return OPW_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	OPWEntity entity = getEntity(resource.getOriginalMetadata());
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
