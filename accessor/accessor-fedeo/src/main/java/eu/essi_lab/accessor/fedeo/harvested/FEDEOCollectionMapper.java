package eu.essi_lab.accessor.fedeo.harvested;

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

import java.net.URLEncoder;
import java.util.Optional;

import eu.essi_lab.accessor.fedeo.distributed.FEDEOGranulesConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import eu.essi_lab.ommdk.GMDResourceMapper;

public class FEDEOCollectionMapper extends FileIdentifierMapper {

    protected static final String DEFAULT_CLIENT_ID = "gs-service";
    public static final String SCHEMA_URI = "http://essi-lab.eu/fedeo/collections";

    public final static String FEDEO_REQUEST = "httpAccept=application/atom%2Bxml&";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GMDResourceMapper gmdResourceMapper = new GMDResourceMapper();

	FEDEOOriginalMDWrapper wrapper = new FEDEOOriginalMDWrapper();

	GSResource gmiMapped = gmdResourceMapper.map(wrapper.getOriginalMetadata(originalMD), source);

	try {

	    enrichWithSecondLevelUrl(gmiMapped, wrapper, originalMD);

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return gmiMapped;

    }

    public void enrichWithSecondLevelUrl(GSResource gmiMapped, FEDEOOriginalMDWrapper wrapper, OriginalMetadata originalMD)
	    throws GSException {

	try {

	    XMLDocumentReader xdocReader = new XMLDocumentReader(originalMD.getMetadata());

	    String osdd = xdocReader.evaluateString("//*:link[@type='application/opensearchdescription+xml']/@href");

	    boolean isGranulesAvailable = false;

	    if (osdd != null && !osdd.isEmpty()) {

		String baseURL = gmiMapped.getSource().getEndpoint().replace("series", "datasets");
		baseURL = baseURL.endsWith("?") ? baseURL : baseURL + "?";
		baseURL = baseURL + FEDEOGranulesConnector.FEDEO_REQUEST + "parentIdentifier="
			+ URLEncoder.encode(gmiMapped.getOriginalId().get().trim(), "UTF-8");

		Downloader down = new Downloader();

		Optional<String> result = down.downloadOptionalString(baseURL);
		Optional<Integer> responseCode = HttpConnectionUtils.getOptionalResponseCode(baseURL);
		if (responseCode.isPresent() && responseCode.get() == 200) {
		    isGranulesAvailable = true;
		    // System.out.println("OSDD: " + osdd);
		    gmiMapped.getExtensionHandler().setFEDEOSecondLevelInfo(osdd);
		    gmiMapped.getExtensionHandler().setAvailableGranules(String.valueOf(isGranulesAvailable));
		} else {
		    gmiMapped.getExtensionHandler().setAvailableGranules(String.valueOf(isGranulesAvailable));
		}
		// it seems that it is not anymore sufficient to have an ossd
		//

		if (result.isPresent()) {
		    isGranulesAvailable = true;
		    // System.out.println("OSDD: " + osdd);
		    gmiMapped.getExtensionHandler().setFEDEOSecondLevelInfo(osdd);
		    gmiMapped.getExtensionHandler().setAvailableGranules(String.valueOf(isGranulesAvailable));
		} else {
		    gmiMapped.getExtensionHandler().setAvailableGranules(String.valueOf(isGranulesAvailable));
		}

		// String collectionUrl = osdd.replace("description.xml", "request");
		//
		// collectionUrl = collectionUrl.endsWith("&") ? collectionUrl + FEDEO_REQUEST + "clientId=" +
		// DEFAULT_CLIENT_ID
		// : collectionUrl + "&" + FEDEO_REQUEST + "clientId=" + DEFAULT_CLIENT_ID;
		//
		// Optional<String> openCollection = down.downloadString(collectionUrl);
		// if (openCollection.isPresent()) {
		// if (openCollection.get().contains("ExceptionReport")) {
		// System.out.println("OSDD: " + osdd);
		// }
		// }

		// osdd reader
		// XMLDocumentReader reader = new XMLDocumentReader(result.get());
		//
		// String secondLevelSearchTemplate =
		// reader.evaluateString("//*:Url[@type='application/atom+xml']/@template");
		//
		// int totRes = reader.evaluateNumber("//*:totalResults").intValue();
		//
		// if (totRes > 0 || reader.evaluateBoolean("//*:entry")) {

		// gmiMapped.getExtensionHandler().setFEDEOSecondLevelInfo(gmiMapped.getOriginalId().get().trim());
		// }
	    } else {
		gmiMapped.getExtensionHandler().setAvailableGranules(String.valueOf(isGranulesAvailable));
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SCHEMA_URI;
    }

}
