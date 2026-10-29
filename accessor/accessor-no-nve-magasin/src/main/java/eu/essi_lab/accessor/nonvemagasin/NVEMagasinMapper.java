package eu.essi_lab.accessor.nonvemagasin;

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


import java.util.Optional;

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
public class NVEMagasinMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String NVE_MAGASIN_SCHEMA = "https://biapi.nve.no/magasinstatistikk/schema";

    static final String LICENSE = "NLOD (Norwegian Licence for Open Government Data, the general open data licence of NVE;"
	    + " not explicitly stated for this API) - https://data.norge.no/nlod/en/2.0 - Source: Norwegian Water Resources and"
	    + " Energy Directorate (NVE), reservoir statistics (Magasinstatistikk), https://biapi.nve.no/magasinstatistikk/";

    static final String ORGANIZATION = "Norwegian Water Resources and Energy Directorate (NVE)";

    /**
     * Resolution of the values (one value per ISO week)
     */
    static final String RESOLUTION = "P7D";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	NVEMagasinEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(NVEMagasinEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), entity.getAreaCode()));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String areaName = entity.getAreaName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(NVEMagasinClient.BEGIN, null);
	String end = object.optString(NVEMagasinClient.END, null);

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

	coreMetadata.setTitle(areaName + " - " + propertyName + " (" + entity.getString(NVEMagasinClient.PROPERTY) + ")");

	coreMetadata.setAbstract(getAbstract(entity, propertyName));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	entity.getArea().ifPresent(area -> {
	    double[] box = area.getBoundingBox();
	    // north, west, south, east
	    coreMetadata.addBoundingBox(box[2], box[1], box[0], box[3]);
	});

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.NVE_MAGASIN.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @param propertyName
     * @return
     */
    static String getAbstract(NVEMagasinEntity entity, String propertyName) {

	StringBuilder builder = new StringBuilder();

	builder.append("Weekly time series of ").append(propertyName.toLowerCase()).append(" (").//
		append(entity.getString(NVEMagasinClient.PROPERTY)).append(") of the hydropower reservoirs of ").//
		append(entity.getAreaName());

	String name = entity.getString(NVEMagasinClient.AREA_LONG_NAME);
	String description = entity.getString(NVEMagasinClient.AREA_DESCRIPTION);
	if (!name.isEmpty() || !description.isEmpty()) {
	    builder.append(" (").append(name.isEmpty() ? description : description.isEmpty() ? name : name + ": " + description)
		    .append(")");
	}

	builder.append(", from the reservoir statistics (Magasinstatistikk) of the ").append(ORGANIZATION).append(". ");
	builder.append("Every week NVE collects the water levels of the most important reservoirs of Norway (about 490) and computes"
		+ " the filling of the reservoirs aggregated by area; each value refers to an ISO week and is dated at the date of the"
		+ " week published by NVE (the Sunday ending the week).");

	if (entity.getProperty().filter(p -> p == NVEMagasinProperty.CAPACITY).isPresent()) {
	    builder.append(" The capacity is published as the present capacity of the reservoirs of the area for the whole period.");
	}

	if (entity.getArea().isPresent()) {
	    builder.append(" The bounding box is an approximation of the area.");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. "Reservoir filling ratio"
     */
    static String getPropertyName(NVEMagasinEntity entity) {

	return entity.getProperty().map(NVEMagasinProperty::getLabel).orElse(entity.getString(NVEMagasinClient.PROPERTY));
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(NVEMagasinEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getAreaName());
	addKeyword(keywords, entity.getString(NVEMagasinClient.AREA_NAME));
	addKeyword(keywords, entity.getString(NVEMagasinClient.AREA_LONG_NAME));
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getString(NVEMagasinClient.PROPERTY));

	addKeyword(keywords, "Norway");
	addKeyword(keywords, "Reservoir");
	addKeyword(keywords, "Hydropower");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Magasinstatistikk");
	addKeyword(keywords, "NVE");
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
    private void addPlatform(NVEMagasinEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String areaName = entity.getAreaName();

	Citation citation = new Citation();
	citation.setTitle(areaName);
	platform.setCitation(citation);

	String description = entity.getString(NVEMagasinClient.AREA_DESCRIPTION);
	platform.setDescription("Reservoirs of " + areaName + (description.isEmpty() ? "" : " (" + description + ")"));
	platform.setMDIdentifierCode(NetProtocolWrapper.NVE_MAGASIN.getCommonURN() + "/" + entity.getAreaType() + "/"
		+ entity.getAreaNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(NVEMagasinEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(propertyName + " (" + entity.getString(NVEMagasinClient.PROPERTY) + ")"
		+ entity.getProperty().map(p -> ", " + p.getUnitName()).orElse(""));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.NVE_MAGASIN.getCommonURN() + ":" + entity.getString(NVEMagasinClient.PROPERTY));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(NVEMagasinEntity entity, ExtensionHandler handler) {

	Optional<NVEMagasinProperty> property = entity.getProperty();

	property.ifPresent(p -> {
	    handler.setAttributeUnits(p.getUnitName());
	    handler.setAttributeUnitsAbbreviation(p.getUnitAbbreviation());
	});

	handler.setCountry("Norway");
	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param originalMD
     * @return
     */
    private NVEMagasinEntity getEntity(OriginalMetadata originalMD) {

	return new NVEMagasinEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return NVE_MAGASIN_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	NVEMagasinEntity entity = getEntity(resource.getOriginalMetadata());
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
