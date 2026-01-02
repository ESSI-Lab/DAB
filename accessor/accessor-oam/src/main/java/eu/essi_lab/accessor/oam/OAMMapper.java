/**
 * 
 */
package eu.essi_lab.accessor.oam;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.model.AccessType;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class OAMMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String OAM_SCHEMA_URI = "https://api.openaerialmap.org/schema";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String metadata = resource.getOriginalMetadata().getMetadata();
	JSONObject object = new JSONObject(metadata);

	return object.getString("_id");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String metadata = originalMD.getMetadata();
	JSONObject object = new JSONObject(metadata);

	// ID
	String id = object.getString("_id");

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	MIMetadata md = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	DataIdentification dataId = md.getDataIdentification();

	// TITLE
	try {
	    String title = object.getString("title");

	    md.setFileIdentifier(id);
	    dataId.setCitationTitle(title);
	} catch (Throwable t) {
	}

	// BBOX
	try {
	    JSONArray bboxAr = object.getJSONArray("bbox");

	    Double west = (Double) bboxAr.get(0);
	    Double south = (Double) bboxAr.get(1);
	    Double east = (Double) bboxAr.get(2);
	    Double north = (Double) bboxAr.get(3);

	    dataId.addGeographicBoundingBox(north, west, south, east);
	} catch (Throwable t) {
	}

	// TIME
	try {
	    String timeStart = object.getString("acquisition_start");
	    String timeEnd = object.getString("acquisition_end");

	    dataId.addTemporalExtent(timeStart, timeEnd);
	} catch (Throwable t) {
	}

	// TRANSFER SIZE
	Double transferSize = null;
	try {
	    String size = object.get("file_size").toString();

	    transferSize = Double.valueOf(size);
	    transferSize = (transferSize / 1024.0) / 1024.0;

	} catch (Throwable ex) {
	}

	JSONObject properties = object.getJSONObject("properties");
	String oamSensor = null;

	if (properties != null) {

	    // BROWSE GRAPHIC
	    try {
		String thumb = properties.getString("thumbnail");

		if (thumb != null && !"".equalsIgnoreCase(thumb)) {

		    BrowseGraphic browseGraphic = new BrowseGraphic();
		    browseGraphic.setFileName(thumb);

		    browseGraphic.setFileDescription("Pictorial preview of the dataset");
		    dataId.addGraphicOverview(browseGraphic);
		}
	    } catch (Throwable thr) {
	    }

	    // KEYWORDS
	    try {
		String oamplatform = object.getString("platform");

		if (oamplatform != null && !"".equalsIgnoreCase(oamplatform)) {

		    dataId.addKeyword(oamplatform);

		    String tmpSensor = properties.getString("sensor");

		    if (tmpSensor != null && !tmpSensor.equals("") && !tmpSensor.toLowerCase().contains("unknow")) {
			dataId.addKeyword(tmpSensor);
			oamSensor = tmpSensor;
		    }
		}
	    } catch (Throwable thr) {
	    }

	    // ONLINE
	    try {
		String uuid = object.getString("uuid");
		if (uuid != null && !uuid.equals("")) {

		    Online online = new Online();

		    online.setLinkage(uuid);
		    online.setName("Download");
		    online.setFunctionCode("download");
		    online.setProtocol("HTTP");

		    online.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

		    md.getDistribution().addDistributionOnline(online, transferSize);
		}
	    } catch (Throwable thr) {
	    }
	}

	// PROVIDER
	try {
	    String provider = object.getString("provider");
	    if (provider != null && !"".equalsIgnoreCase(provider)) {

		ResponsibleParty contact = new ResponsibleParty();
		contact.setOrganisationName(provider);

		String mail = object.getString("contact");
		Contact contactinfo = new Contact();
		Address address = new Address();
		address.addElectronicMailAddress(mail);
		contactinfo.setAddress(address);
		contact.setContactInfo(contactinfo);

		dataId.addPointOfContact(contact);
	    }
	} catch (Throwable thr) {
	}

	// PLATFORM
	try {
	    String oamPlatform = object.getString("platform");
	    if (oamPlatform != null && !"".equalsIgnoreCase(oamPlatform) && !oamPlatform.toLowerCase().contains("uav")) {
		if (oamSensor != null && !"".equalsIgnoreCase(oamSensor)) {

		    String platformName = oamSensor;
		    MIPlatform platform = new MIPlatform();

		    if (platformName != null && !platformName.equalsIgnoreCase("")) {

			platform.setDescription(platformName);
		    }

		    md.addMIPlatform(platform);
		}
	    }
	} catch (Throwable thr) {
	}

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return OAM_SCHEMA_URI;
    }

    /**
     * @param clone
     * @return
     * @throws Exception
     */
    static ArrayList<String> splitPageResults(ClonableInputStream clone) throws Exception {

	ArrayList<String> out = Lists.newArrayList();

	JSONObject jResponse = JSONUtils.fromStream(clone.clone());
	JSONArray array = jResponse.getJSONArray("results");
	for (int i = 0; i < array.length(); i++) {

	    JSONObject object = array.getJSONObject(i);
	    out.add(object.toString());
	}

	return out;
    }

}
