package eu.essi_lab.accessor.chslf;

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
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class SLFMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String SLF_SCHEMA = "https://measurement-api.slf.ch/schema";

    private static final String SLF_ORGANIZATION = "WSL Institute for Snow and Avalanche Research SLF";

    private static final String SLF_LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - SLF Data Service Terms of Use: https://www.slf.ch/en/services-and-products/slf-data-service";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());

	SLFNetwork network = SLFNetwork.fromId(object.getString(SLFClient.NETWORK));
	SLFParameter parameter = SLFParameter.decode(network, object.getString(SLFClient.PARAMETER));

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationCode = object.getString(SLFClient.STATION_CODE);
	String stationLabel = object.optString(SLFClient.STATION_LABEL, stationCode);
	String timeseriesId = object.getString(SLFClient.TIMESERIES_ID);
	String stationName = stationLabel + " (" + stationCode + ")";

	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationCode));

	coreMetadata.setTitle(parameter.getLabel() + " at " + network.getLabel() + " " + stationName);

	coreMetadata.setAbstract("Timeseries of " + parameter.getDescription().toLowerCase() + " (" + parameter.getUnits() + ") measured at "
		+ network.getLabel() + " " + stationName + ", operated by the " + SLF_ORGANIZATION + ". Published by the SLF Measurement API, which provides the last "
		+ SLFClient.DATA_RETENTION_DAYS + " days of data. Longer records are available from the SLF data service.");

	DataIdentification dataId = coreMetadata.getDataIdentification();

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(SLF_ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(SLF_LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	addKeyword(keywords, stationLabel);
	addKeyword(keywords, parameter.getLabel());
	addKeyword(keywords, network == SLFNetwork.STUDY_PLOT ? "SLF study plot" : "IMIS");
	addKeyword(keywords, "SLF");
	addKeyword(keywords, "Snow");
	addKeyword(keywords, "Cryosphere");
	dataId.addKeywords(keywords);

	String from = object.optString(SLFClient.FROM, null);
	String to = object.optString(SLFClient.TO, null);
	if (from != null && to != null) {
	    coreMetadata.addTemporalExtent(from, to);
	}

	if (object.has(SLFClient.STATION_LAT) && object.has(SLFClient.STATION_LON)) {
	    BigDecimal lat = new BigDecimal(object.get(SLFClient.STATION_LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(SLFClient.STATION_LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (object.has(SLFClient.STATION_ELEVATION) && !object.isNull(SLFClient.STATION_ELEVATION)) {
	    double elevation = object.getDouble(SLFClient.STATION_ELEVATION);
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(elevation);
	    verticalExtent.setMaximumValue(elevation);
	    dataId.addVerticalExtent(verticalExtent);
	}

	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(stationLabel);
	platform.setCitation(citation);
	platform.setDescription(network.getLabel() + " " + stationName);
	platform.setMDIdentifierCode(NetProtocolWrapper.SLF.getCommonURN() + "/" + stationCode);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.SLF.getCommonURN() + ":" + network.getId() + ":" + parameter.getCode());
	coverageDescription.setAttributeTitle(parameter.getLabel());
	coverageDescription.setAttributeDescription(parameter.getDescription() + ", " + parameter.getUnits());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setAttributeUnits(parameter.getUnits());
	handler.setAttributeUnitsAbbreviation(parameter.getUnits());
	handler.setTimeInterpolation(parameter.getInterpolation());
	handler.setTimeResolutionDuration8601(parameter.getResolutionDuration());
	if (parameter.getAggregationDuration() != null) {
	    handler.setTimeAggregationDuration8601(parameter.getAggregationDuration());
	}

	Country country = Country.decode(object.optString(SLFClient.STATION_COUNTRY_CODE, "CH"));
	handler.setCountry(country != null ? country.getShortName() : Country.SWITZERLAND.getShortName());

	coreMetadata.addDistributionOnlineResource(//
		timeseriesId, //
		source.getEndpoint(), //
		NetProtocolWrapper.SLF.getCommonURN(), //
		"download");

	return dataset;
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

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SLF_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject object = new JSONObject(resource.getOriginalMetadata().getMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), object.optString(SLFClient.TIMESERIES_ID, null));
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
