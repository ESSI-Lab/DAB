package eu.essi_lab.accessor.esaca;

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
public class ACAMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String ACA_SCHEMA = "https://aplicacions.aca.gencat.cat/sdim2/apirest/schema";

    static final String LICENSE = "Llicència oberta d'ús d'informació - Catalunya (Open Information Use Licence - Catalonia) -"
	    + " https://administraciodigital.gencat.cat/ca/dades/dades-obertes/informacio-practica/llicencies/ - The ACA datasets are"
	    + " published as open data under this licence on the open data portal of the Generalitat de Catalunya"
	    + " (https://analisi.transparenciacatalunya.cat). Source: Agència Catalana de l'Aigua (ACA).";

    static final String ORGANIZATION = "Agència Catalana de l'Aigua (ACA)";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	ACAEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(ACAEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String component = entity.getComponent();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), component));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String componentName = entity.getComponentName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(ACAClient.BEGIN, null);
	String end = object.optString(ACAClient.END, null);

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

	coreMetadata.setTitle(componentName + " - " + getPropertyTitle(entity));

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " measured by sensor " + entity.getSensor()
		+ " at " + getComponentTypeName(entity) + " " + componentName + " (" + component + ")" + getLocationDescription(entity)
		+ ", from the Sentilo REST API of the " + ORGANIZATION + ". The service keeps only the observations of about the last"
		+ " 3 months (rolling window).");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(ACAClient.LAT) && object.has(ACAClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(ACAClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(ACAClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.ACA.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the English name of the property, e.g. "River discharge", or the ACA description if not known
     */
    static String getPropertyName(ACAEntity entity) {

	return entity.getProperty().map(ACAProperty::getLabel).orElse(entity.getDescription());
    }

    /**
     * @param entity
     * @return the English name of the property followed by the ACA description, e.g. "River discharge (Cabal riu)"
     */
    static String getPropertyTitle(ACAEntity entity) {

	String name = getPropertyName(entity);
	String description = entity.getDescription();

	return description.isEmpty() || description.equalsIgnoreCase(name) ? name : name + " (" + description + ")";
    }

    /**
     * @param entity
     * @return e.g. "river discharge (Cabal riu, m³/s)"
     */
    static String getPropertyDescription(ACAEntity entity) {

	String name = getPropertyName(entity);
	String description = entity.getDescription();
	String unit = entity.getUnit();

	StringBuilder builder = new StringBuilder(name.toLowerCase());
	if (!description.isEmpty() && !description.equalsIgnoreCase(name)) {
	    builder.append(" (").append(description);
	    builder.append(unit.isEmpty() ? ")" : ", " + unit + ")");
	} else if (!unit.isEmpty()) {
	    builder.append(" (").append(unit).append(")");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. "river gauging station"
     */
    static String getComponentTypeName(ACAEntity entity) {

	switch (entity.getComponentType()) {
	case "aforament":
	    return "river gauging station";
	case "embassament":
	    return "reservoir";
	case "piezometre":
	    return "piezometer";
	case "pluviometre":
	    return "rain gauge";
	default:
	    return "station";
	}
    }

    /**
     * @param entity
     * @return
     */
    private static String getLocationDescription(ACAEntity entity) {

	StringBuilder builder = new StringBuilder();

	append(builder, "river", entity.getComponentInfo(ACAClient.RIVER));
	append(builder, "aquifer", entity.getComponentInfo(ACAClient.AQUIFER));
	append(builder, "basin", entity.getComponentInfo(ACAClient.BASIN));
	append(builder, "municipality", entity.getComponentInfo(ACAClient.MUNICIPALITY));
	append(builder, "county", entity.getComponentInfo(ACAClient.COUNTY));

	return builder.toString();
    }

    /**
     * @param builder
     * @param label
     * @param value
     */
    private static void append(StringBuilder builder, String label, String value) {

	if (!value.isEmpty()) {
	    builder.append(", ").append(label).append(" ").append(value);
	}
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(ACAEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getComponentName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getDescription());
	addKeyword(keywords, entity.getComponentInfo(ACAClient.RIVER));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.AQUIFER));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.WATER_BODY));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.BASIN));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.SUB_BASIN));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.MUNICIPALITY));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.COUNTY));
	addKeyword(keywords, entity.getComponentInfo(ACAClient.PROVINCE));

	addKeyword(keywords, "Catalonia");
	addKeyword(keywords, "Spain");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "ACA");
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
    private void addPlatform(ACAEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String componentName = entity.getComponentName();

	Citation citation = new Citation();
	citation.setTitle(componentName);
	platform.setCitation(citation);

	String typeName = getComponentTypeName(entity);

	platform.setDescription(typeName.substring(0, 1).toUpperCase() + typeName.substring(1) + " " + componentName + " ("
		+ entity.getComponent() + ")" + getLocationDescription(entity) + " (Catalonia, Spain)");
	platform.setMDIdentifierCode(NetProtocolWrapper.ACA.getCommonURN() + "/" + entity.getComponent());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(ACAEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.ACA.getCommonURN() + ":"
		+ entity.getProperty().map(ACAProperty::name).orElse(entity.getString(ACAClient.TYPE)));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(ACAEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Spain");

	if (entity.getProperty().filter(p -> p != ACAProperty.PRECIPITATION_INTENSITY).isPresent()) {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param unit the unit used by ACA
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m³/s":
	    return "cubic metres per second";
	case "l/s":
	    return "litres per second";
	case "msnm":
	    return "metres above sea level";
	case "hm³":
	    return "cubic hectometres";
	case "%":
	    return "percent";
	case "ºC":
	case "°C":
	    return "degrees Celsius";
	case "µS/cm":
	    return "microsiemens per centimetre";
	case "mm/h":
	    return "millimetres per hour";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by ACA
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "m³/s":
	    return "m3/s";
	case "hm³":
	    return "hm3";
	case "msnm":
	    return "m";
	case "ºC":
	    return "°C";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private ACAEntity getEntity(OriginalMetadata originalMD) {

	return new ACAEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ACA_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	ACAEntity entity = getEntity(resource.getOriginalMetadata());
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
