package eu.essi_lab.accessor.siarso;

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
import java.util.ArrayList;
import java.util.List;

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
public class ARSOMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String ARSO_SCHEMA = "https://www.arso.gov.si/xml/vode/schema";

    static final String LICENSE = "Source: Agencija Republike Slovenije za okolje (ARSO). The licence of the data is not explicitly"
	    + " stated (© Agencija Republike Slovenije za okolje, https://www.arso.gov.si/omejitev_odgovornosti.html). The data of the"
	    + " automatic hydrological stations are provisional and informative only and can be significantly corrected before the"
	    + " official publication (https://www.arso.gov.si/vode/podatki/opombe/OpombaHidro.html).";

    static final String ORGANIZATION = "Agencija Republike Slovenije za okolje (ARSO) - Slovenian Environment Agency";

    static final String ROLLING_WINDOW_DESCRIPTION = "Only the observations of the last 30 days are available (rolling window):"
	    + " 10 minutes values for the last day, 30 minutes values for the last 7 days and hourly values for the last 30 days."
	    + " Times are in CET (UTC+1).";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	ARSOEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(ARSOEntity entity, GSSource source) {

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
	String begin = object.optString(ARSOClient.BEGIN, null);
	String end = object.optString(ARSOClient.END, null);

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

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at the automatic hydrological station "
		+ stationName + " (" + stationId + ") of the " + ORGANIZATION + ". " + getThresholdsDescription(entity)
		+ ROLLING_WINDOW_DESCRIPTION);

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(ARSOClient.LAT) && object.has(ARSOClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(ARSOClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(ARSOClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.ARSO.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the English name, e.g. "Water level" (or the original name if not known)
     */
    static String getPropertyName(ARSOEntity entity) {

	return entity.getParameter().map(ARSOParameter::getLabel).orElse(entity.getParameterName());
    }

    /**
     * @param entity
     * @return the property name, with the original name and unit and the gauge zero of the water level
     */
    static String getPropertyDescription(ARSOEntity entity) {

	String ret = getPropertyName(entity).toLowerCase();

	String unit = entity.getUnit();
	ret += " (original name: " + entity.getParameterName() + (unit.isEmpty() ? "" : " [" + unit + "]") + ")";

	String gaugeZero = entity.getString(ARSOClient.GAUGE_ZERO);
	if (entity.getParameter().filter(p -> p == ARSOParameter.WATER_LEVEL || p == ARSOParameter.SEA_LEVEL).isPresent()
		&& !gaugeZero.isEmpty()) {
	    ret += " referred to the gauge zero at " + gaugeZero + " m a.s.l.";
	}

	return ret;
    }

    /**
     * @param entity
     * @return the description of the flood thresholds of the series, if any
     */
    static String getThresholdsDescription(ARSOEntity entity) {

	String suffix = null;
	String unit = null;

	ARSOParameter parameter = entity.getParameter().orElse(null);
	if (parameter == ARSOParameter.DISCHARGE) {
	    suffix = "_pretok";
	    unit = "m³/s";
	} else if (parameter == ARSOParameter.WATER_LEVEL || parameter == ARSOParameter.SEA_LEVEL) {
	    suffix = "_vodostaj";
	    unit = "cm";
	}

	if (suffix == null) {
	    return "";
	}

	String[] labels = new String[] { "first (local overflow)", "second (local floods)", "third (extensive floods)" };
	List<String> thresholds = new ArrayList<>();

	int i = 0;
	for (String tag : ARSOClient.THRESHOLD_TAGS) {
	    if (tag.endsWith(suffix)) {
		String value = entity.getString(tag);
		if (!value.isEmpty()) {
		    thresholds.add(labels[i] + " " + value + " " + unit);
		}
		i++;
	    }
	}

	if (thresholds.isEmpty()) {
	    return "";
	}

	return "High water thresholds: " + String.join(", ", thresholds) + ". ";
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(ARSOEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	if (!propertyName.equals(entity.getParameterName())) {
	    addKeyword(keywords, entity.getParameterName());
	}
	addKeyword(keywords, entity.getString(ARSOClient.RIVER));

	addKeyword(keywords, "Slovenia");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "ARSO");
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
    private void addPlatform(ARSOEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String gaugeZero = entity.getString(ARSOClient.GAUGE_ZERO);

	platform.setDescription("Automatic hydrological station " + stationName + " (" + entity.getStationId() + ")"
		+ (gaugeZero.isEmpty() ? "" : ", gauge zero " + gaugeZero + " m a.s.l.") + " (Slovenia)");
	platform.setMDIdentifierCode(NetProtocolWrapper.ARSO.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(ARSOEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.ARSO.getCommonURN() + ":"
		+ entity.getParameter().map(ARSOParameter::name).orElse(entity.getParameterName()));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(ARSOEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Slovenia");
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param unit the unit used by ARSO
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m":
	    return "metres";
	case "m³/s":
	    return "cubic metres per second";
	case "°C":
	    return "degrees Celsius";
	case "s":
	    return "seconds";
	case "cm/s":
	    return "centimetres per second";
	case "uS":
	    return "microsiemens";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by ARSO
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	return unit.equals("m³/s") ? "m3/s" : unit;
    }

    /**
     * @param originalMD
     * @return
     */
    private ARSOEntity getEntity(OriginalMetadata originalMD) {

	return new ARSOEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ARSO_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	ARSOEntity entity = getEntity(resource.getOriginalMetadata());
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
