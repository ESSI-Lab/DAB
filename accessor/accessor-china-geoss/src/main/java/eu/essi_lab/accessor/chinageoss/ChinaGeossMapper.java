/**
 * 
 */
package eu.essi_lab.accessor.chinageoss;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class ChinaGeossMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String CHINA_GEOSS_SCHEME_URI = "china-geoss-scheme-uri";
    private static final String GEOARC_LOGO_URL = "https://dabsatimages.s3.amazonaws.com/GEOARC_logo.png";
    private static final String CASA_LOGO_URL = "https://dabsatimages.s3.amazonaws.com/CASA_LOGO.png";
    private static final String GEODOI_LOGO_URL = "https://dabsatimages.s3.amazonaws.com/GEODOI.png";

    // @Override
    // protected String createOriginalIdentifier(GSResource resource) {
    //
    // JSONObject original = new JSONObject(resource.getOriginalMetadata().getMetadata());
    //
    // return original.getString("dataID");
    // }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);
	dataset.setOriginalMetadata(originalMD);

	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	miMetadata.setHierarchyLevelName("dataset");
	miMetadata.setLanguage("en");

	//
	// set the GDC tag
	//
	dataset.getPropertyHandler().setIsGDC(true);

	JSONObject original = new JSONObject(originalMD.getMetadata());

	//
	// parent identifier/platformId
	//
	String satelliteId = original.getString("satelliteID");

	String parentId = SatelliteUtils.getChinaGEOSSPlatformIndentifier(satelliteId);
	miMetadata.setParentIdentifier(parentId);

	MIPlatform miPlatform = new MIPlatform();
	miPlatform.setMDIdentifierCode(satelliteId);
	miPlatform.setDescription("Satellite " + satelliteId);
	miMetadata.addMIPlatform(miPlatform);

	//
	// sensor id
	//
	String sensorId = original.getString("sensorID");

	MIInstrument miInstrument = new MIInstrument();
	miInstrument.setMDIdentifierTypeCode(sensorId);
	miMetadata.addMIInstrument(miInstrument);

	//
	// bbox
	//
	Double blLat = original.getDouble("bottomLeftLatitude");
	Double blLon = original.getDouble("bottomLeftLongitude");
	Double brLat = original.getDouble("bottomRightLatitude");
	Double brLon = original.getDouble("bottomRightLongitude");

	Double tlLat = original.getDouble("topLeftLatitude");
	Double tlLon = original.getDouble("topLeftLongitude");
	Double trLat = original.getDouble("topRightLatitude");
	Double trLon = original.getDouble("topRightLongitude");

	Double west = tlLon < blLon ? tlLon : blLon;
	Double east = trLon > brLon ? trLon : brLon;

	if (west > east) {
	    Double tmp = west;
	    west = east;
	    east = tmp;
	}

	Double north = tlLat > trLat ? tlLat : trLat;
	Double south = blLat < brLat ? blLat : brLat;

	if (south > north) {
	    Double tmp = north;
	    north = south;
	    south = tmp;
	}

	dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(north, west, south, east);

	// graphic overview
	String previewURL = null;
	if (satelliteId.toLowerCase().equalsIgnoreCase("tansat")) {
	    previewURL = CASA_LOGO_URL;
	} else if (satelliteId.toLowerCase().equalsIgnoreCase("geoarc")) {
	    previewURL = GEOARC_LOGO_URL;
	}
	else if (satelliteId.toLowerCase().equalsIgnoreCase("geodoi")) {
	    previewURL = GEODOI_LOGO_URL;
	}
	if (previewURL != null) {
	    BrowseGraphic graphic = new BrowseGraphic();
	    graphic.setFileDescription("Dataset Logo");
	    graphic.setFileName(previewURL);
	    graphic.setFileType("image/png");
	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGraphicOverview(graphic);
	}

	//
	// time
	//
	String timeStart = original.getString("receiveTime") + "T00:00:00";
	String timeEnd = original.getString("receiveTime") + "T23:59:59";

	dataset.getHarmonizedMetadata().getCoreMetadata().addTemporalExtent(timeStart, timeEnd);

	//
	// organization
	//
	ResponsibleParty responsibleParty = new ResponsibleParty();
	responsibleParty.setRoleCode("originator");
	String dataOwner = original.getString("dataOwner");
	String orgName = (dataOwner != null && !dataOwner.isEmpty()) ? dataOwner : "ChinaGEOSS-EO-BNU";
	responsibleParty.setOrganisationName(orgName);

	miMetadata.getDataIdentification().addPointOfContact(responsibleParty);

	//
	// title
	//
	String fileSpecification = original.getString("fileSpecification");
	String title = "";
	String dataId = original.getString("dataID");
	if (fileSpecification != null && !fileSpecification.isEmpty() && !(satelliteId.toLowerCase().equalsIgnoreCase("geoarc") || satelliteId.toLowerCase().equalsIgnoreCase("tansat") || satelliteId.toLowerCase().equalsIgnoreCase("geodoi") )) {
	    title = satelliteId + " - " + fileSpecification;
	}
	if (title.isEmpty()) {

	    title = satelliteId + " - " + dataId;
	}

	dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);

	// id
	if (dataId != null && !dataId.isEmpty()) {

	    try {
		String identifier = StringUtils.hashSHA1messageDigest(dataId);
		dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(identifier);
		dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setFileIdentifier(identifier);
	    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	//
	// online
	//

	String online = " ftp://geossuser:C2h0i2n0aGEOSS@124.16.184.9";

	String dataURL = original.getString("dataURL");
	String fileStorePath = original.getString("fileStorePath");

	if (dataURL != null) {

	    if (dataURL.startsWith("http")) {
		dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
			fileSpecification, //
			dataURL, //
			NetProtocols.HTTP.getCommonURN(), //
			"download");
	    } else if (dataURL.startsWith("ftp")) {
		dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
			fileSpecification, //
			dataURL, //
			NetProtocols.FTP.getCommonURN(), //
			"download");
	    } else {

		// remote service is changed. dataURL field should contain the the entire path of file
		if (dataURL.contains(fileStorePath)) {
		    online += dataURL;
		    if (!dataURL.contains(fileSpecification)) {
			online += fileSpecification;
		    }
		} else {
		    online += dataURL + fileStorePath + fileSpecification;
		}

		dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
			fileSpecification, //
			online, //
			NetProtocols.FTP.getCommonURN(), //
			"download");
	    }
	}

	String format = original.getString("productFormat");

	dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionFormat(format);

	// ------------------------
	//
	// SatelliteScene extension
	//
	//
	SatelliteScene satelliteScene = new SatelliteScene();
	satelliteScene.setOrigin("china");

	String size = String.valueOf(((original.getDouble("fileSize") / 1024.0) / 1024.0)); // is it in bytes???

	satelliteScene.setSize(size);
	satelliteScene.setFileStorePath(fileStorePath);
	satelliteScene.setFileSpecification(fileSpecification);

	satelliteScene.setProductType(sensorId);

	String queryables = "prodType";

	satelliteScene.setCollectionQueryables(queryables);

	dataset.getExtensionHandler().setSatelliteScene(satelliteScene);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CHINA_GEOSS_SCHEME_URI;
    }

}
