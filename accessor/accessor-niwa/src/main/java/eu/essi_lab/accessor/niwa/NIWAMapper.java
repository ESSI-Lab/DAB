package eu.essi_lab.accessor.niwa;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author Fabrizio
 */
public class NIWAMapper extends FileIdentifierMapper {

    /**
     * 
     */
    private static String NS = "hydrowebportal.niwa.co.nz";

    /**
     * 
     */
    private static final String NIWA_MAPPER_ERROR = "NIWA_MAPPER_ERROR";

    /**
     * 
     */
    private static HashMap<String, String> uomMap = new HashMap<>();

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.NIWA_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset, source);

	return dataset;
    }

    /**
     * @param dataObject
     * @return
     */
    static String readDatasetIDentifier(JSONObject dataObject) {

	return dataObject.getString("DatasetIdentifier");
    }

    /**
     * @param dataObject
     * @return
     */
    static String readStartOfRecord(JSONObject dataObject) {

	return dataObject.getString("StartOfRecord");
    }

    /**
     * @param dataObject
     * @return
     */
    static String readEndOfRecord(JSONObject dataObject) {

	return dataObject.getString("EndOfRecord");
    }

    /**
     * @param originalMD
     * @param dataset
     * @param source
     * @throws GSException
     */
    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset, GSSource source) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {

	    JSONObject object = new JSONObject(originalMetadata);

	    String location = object.getString("Location");

	    String locationId = String.valueOf(object.getInt("LocationId"));

	    String locationIdentifier = object.getString("LocationIdentifier");

	    String locationType = object.getString("LocType");

	    String locationFolder = object.getString("LocationFolder");

	    String datasetIdentifier = object.getString("DatasetIdentifier");

	    String datasetId = String.valueOf(object.getInt("DatasetId"));

	    String recordIdentifier = locationId + "_" + datasetId;

	    String lon = String.valueOf(object.getDouble("LocX"));

	    String lat = String.valueOf(object.getDouble("LocY"));

	    String startDate = object.getString("StartOfRecord");

	    String endDate = object.getString("EndOfRecord");

	    //
	    //
	    //

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    coreMetadata.setIdentifier(recordIdentifier);
	    coreMetadata.getMIMetadata().setFileIdentifier(recordIdentifier);

	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	    //
	    //
	    //

	    String platformIdentifier = NS + ":" + locationIdentifier;

	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(platformIdentifier);
	    platform.setDescription(location);

	    Citation citation = new Citation();
	    citation.setTitle(location);
	    platform.setCitation(citation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    //
	    //
	    //

	    String parameter = datasetIdentifier.split("@")[0];

	    String parameterIdentifier = NS + ":" + parameter;

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier(parameterIdentifier);
	    coverageDescription.setAttributeTitle(parameter);
	    coverageDescription.setAttributeDescription(parameter);

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    //
	    //
	    //

	    String uom = uomMap.get(parameter);

	    if (uom == null) {

		Optional<String> optUom = NIWAClient.retrieveUOM(datasetIdentifier, ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get());

		if (optUom.isPresent()) {

		    uomMap.put(parameter, optUom.get());

		    uom = optUom.get();
		}
	    }

	    if (uom != null) {

		dataset.getExtensionHandler().setAttributeUnits(uom);
		dataset.getExtensionHandler().setAttributeUnitsAbbreviation(uom);
	    }

	    //
	    //
	    //

	    coreMetadata.setTitle(location + " - " + parameter);

	    //
	    //
	    //

	    NIWAIdentifierMangler mangler = new NIWAIdentifierMangler();

	    mangler.setPlatformIdentifier(locationIdentifier);
	    mangler.setParameterIdentifier(parameter);

	    String identifier = mangler.getMangling();

	    coreMetadata.addDistributionOnlineResource(//
		    identifier, //
		    source.getEndpoint(), //
		    NetProtocols.NIWA.getCommonURN(), //
		    "download");

	    String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	    Online downloadOnline = coreMetadata.getOnline();

	    String onlineId = downloadOnline.getIdentifier();
	    if (onlineId == null) {
		downloadOnline.setIdentifier();
	    }

	    downloadOnline.setIdentifier(onlineId);

	    //
	    //
	    //

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(location);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(locationType);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(locationFolder);

	    //
	    //
	    //

	    double w = Double.valueOf(lon);
	    double e = Double.valueOf(lon);
	    double n = Double.valueOf(lat);
	    double s = Double.valueOf(lat);

	    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("4326");
	    referenceSystem.setCodeSpace("EPSG");

	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    //
	    //
	    //

	    TemporalExtent tempExtent = new TemporalExtent();

	    tempExtent.setBeginPosition(startDate);

	    tempExtent.setEndPosition(endDate);

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	    //
	    //
	    //

	    datasetIdentifier = URLEncoder.encode(datasetIdentifier, "UTF-8");
	    startDate = startDate.replace("T", URLEncoder.encode(" ", "UTF-8"));
	    endDate = endDate.replace("T", URLEncoder.encode(" ", "UTF-8"));

	    URL downloadURL = NIWAClient.getDownloadURL(datasetIdentifier, startDate, endDate);

	    Online online = new Online();
	    online.setLinkage(downloadURL.toExternalForm());
	    online.setProtocol("HTTP");
	    online.setFunctionCode("download");
	    online.setDescription("Direct Download");

	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	    Format format = new Format();
	    format.setName("csv");

	    coreMetadata.getMIMetadata().getDistribution().addFormat(format);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NIWA_MAPPER_ERROR, //
		    e);
	}
    }
}
