/**
 * 
 */
package eu.essi_lab.accessor.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.jaxb.filter._1_1_0.SortByType;
import eu.essi_lab.jaxb.filter._1_1_0.SortOrderType;
import eu.essi_lab.jaxb.filter._1_1_0.SortPropertyType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourcePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourceType;
import net.opengis.iso19139.gmd.v_20060504.MDDigitalTransferOptionsPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDistributionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDistributionType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;
import net.opengis.iso19139.gmd.v_20060504.URLPropertyType;

/**
 * @author Fabrizio
 */
public class CSWEEASDIConnector extends CSWConnector {

    private static final String CSWEEASDICONNECTOR_EXTRACTION_ERROR = "CSWEEASDICONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW EEA SDI Connector";
    private Downloader downloader = new Downloader();

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * @param elementSetName
     * @return
     */
    protected QueryType createQueryType(ElementSetName elementSetName) {

	QueryType queryType = super.createQueryType(elementSetName);

	 SortByType sortByType = new SortByType();
	
	 SortPropertyType sortPropertyType = new SortPropertyType();
	 PropertyNameType propertyNameType = new PropertyNameType();
	 propertyNameType.getContent().add("gmd:title");
	 sortPropertyType.setPropertyName(propertyNameType);
	 sortPropertyType.setSortOrder(SortOrderType.ASC);
	 sortByType.getSortProperty().add(sortPropertyType);
	
	 queryType.setSortBy(sortByType);

	return queryType;
    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {

	return new CSWHttpGetRecordsRequestCreator(getGetRecordsBinding(), this, getRecords) {

	    public String getGetRecordsUrl() {

		//
		// instead of http://sdi.eea.europa.eu/catalogue/srv/eng/csw-geoss
		//
		return "https://sdi.eea.europa.eu/catalogue/geoss/eng/csw";
	    }
	};
    }

    @Override
    public void fixOnlineResources(ListRecordsResponse<OriginalMetadata> ret) {

	Iterator<OriginalMetadata> iterator = ret.getRecords();
	List<OriginalMetadata> metadatas = new ArrayList<>();
	int countRecord = 0;
	while (iterator.hasNext()) {
	    countRecord++;
	    OriginalMetadata originalMetadata = (OriginalMetadata) iterator.next();
	    String metadata = originalMetadata.getMetadata();
	    try {
		@SuppressWarnings("rawtypes")
		JAXBElement je = CommonContext.unmarshal(metadata, JAXBElement.class);
		Object obj = je.getValue();
		if (obj instanceof MDMetadataType) {
		    MDMetadataType md = (MDMetadataType) obj;
		    List<MDDigitalTransferOptionsPropertyType> transferOptions = null;
		    MDDistributionPropertyType distrInfo = md.getDistributionInfo();
		    if (distrInfo != null) {
			MDDistributionType mdDistrInfo = distrInfo.getMDDistribution();
			if (mdDistrInfo != null) {
			    transferOptions = mdDistrInfo.getTransferOptions();
			}
		    }

		    if (transferOptions != null) {

			for (MDDigitalTransferOptionsPropertyType transfer : transferOptions) {

			    List<CIOnlineResourcePropertyType> onlinesList = new ArrayList<CIOnlineResourcePropertyType>();

			    List<CIOnlineResourcePropertyType> onlines = transfer.getMDDigitalTransferOptions().getOnLine();

			    for (CIOnlineResourcePropertyType o : onlines) {

				CIOnlineResourceType onlineResource = o.getCIOnlineResource();

				if (onlineResource != null) {

				    CharacterStringPropertyType protocol = onlineResource.getProtocol();
				    String prot = ISOMetadata.getStringFromCharacterString(protocol);
				    // check if WMS
				    if (prot.contains("WMS-1.1.0") || prot.contains("WMS-1.1.1") || prot.contains("OGC:WMS")
					    || prot.contains(NetProtocols.WMS_1_1_1.getCommonURN()) || prot.contains("WMS-1.3.0")
					    || prot.contains(NetProtocols.WMS_1_3_0.getCommonURN())) {

					String name = ISOMetadata.getStringFromCharacterString(onlineResource.getName());
					String link = onlineResource.getLinkage().getURL();

					if (link.toLowerCase().contains("getcapabilities")) {

					    String fixed = fixOnlineURL(link);
					    URLPropertyType fixedURL = new URLPropertyType();
					    fixedURL.setURL(fixed);

					    if (name == null || name.isEmpty()) {
						// getcapabilities to retrieve layers and then fix online url
						HttpResponse<InputStream> response;

						try {
						    response = downloader.downloadResponse(link);
						} catch (Exception e) {
						    GSLoggerFactory.getLogger(getClass()).error(
							    "Error ({}) while downloading capabilities Reference URL: {}", e.getMessage(),
							    link);
						    onlinesList.add(o);
						    continue;
						}

						Integer status = response.statusCode();

						if (status != 200) {
						    GSLoggerFactory.getLogger(getClass()).error(
							    "Error (HTTP code {}) while downloading capabilities. Reference URL: {}",
							    status, link);
						    onlinesList.add(o);
						    continue;
						}
						// Optional<InputStream> getCapResponse =
						// downloader.downloadStream(link);
						// downloader.getResponseCode(metadata)
						// if (getCapResponse.isPresent()) {
						XMLDocumentReader xdoc = new XMLDocumentReader(response.body());
						Node[] layersNodes = xdoc.evaluateNodes("//*:Layer/*:Name");
						List<String> lnames = new ArrayList<String>();
						for (Node n : layersNodes) {
						    String layerName = n.getTextContent();
						    lnames.add(layerName);
						}

						for (int j = 0; j < lnames.size(); j++) {

						    CIOnlineResourceType cloneOnline = (CIOnlineResourceType) onlineResource.clone();
						    cloneOnline.setName(MIMetadata.createCharacterStringPropertyType(lnames.get(j)));
						    cloneOnline.setLinkage(fixedURL);
						    CIOnlineResourcePropertyType toAdd = new CIOnlineResourcePropertyType();
						    toAdd.setCIOnlineResource(cloneOnline);
						    onlinesList.add(toAdd);

						}
					    } else {
						onlineResource.setLinkage(fixedURL);
						onlinesList.add(o);
					    }

					} else {
					    onlinesList.add(o);
					}

				    } else {
					onlinesList.add(o);
				    }
				}
			    }
			    transfer.getMDDigitalTransferOptions().unsetOnLine();
			    transfer.getMDDigitalTransferOptions().setOnLine(onlinesList);
			}
		    }

		    MDMetadata mdMetadata = new MDMetadata(md);
		    OriginalMetadata omd = new OriginalMetadata();
		    omd.setMetadata(mdMetadata.asString(true));
		    omd.setSchemeURI(CommonNameSpaceContext.EEA_NS_URI);
		    metadatas.add(omd);

		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	ret.clearRecords();
	for (OriginalMetadata originalMetadata : metadatas) {
	    ret.addRecord(originalMetadata);
	}

    }

    private String fixOnlineURL(String link) {

	return link.contains("?") ? link.split("\\?")[0] + "?" : link;

    }

    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    // str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmi\"",
	    // "\"http://www.isotc211.org/2005/gmi\"");
	    //
	    // str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmie\"",
	    // "\"http://www.isotc211.org/2005/gmi\"");
	    //
	    // str = str.replace("MIE_", "MI_");

	    str = str.replace("\"http://www.opengis.net/gml/3.2\"", "\"http://www.opengis.net/gml\"");
	    // xmlns:gml="http://www.opengis.net/gml/3.2"

	    File ret = File.createTempFile(getClass().getSimpleName(), ".xml");

	    ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

	    FileOutputStream fos = new FileOutputStream(ret);

	    IOUtils.copy(stream, fos);

	    stream.close();
	    fos.close();

	    return ret;
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error fixing GetRecords response", e);

	    throw GSException.createException( //
		    getClass(), //
		    "Error fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWEEASDICONNECTOR_EXTRACTION_ERROR, e);
	}

    }

    // List<OriginalMetadata> originalMetadataList = ret.getRecordsAsList();for(
    // OriginalMetadata om:originalMetadataList)
    // {
    // XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());
    // Node[] onlineResources = xdoc.evaluateNodes("//*:distributionInfo//*:onLine");
    // Set<String> urls = new HashSet<String>();
    // for (Node n : onlineResources) {
    // // XMLDocumentReader onlines = new XMLDocumentReader(n.getOwnerDocument());
    // String protocol = xdoc.evaluateNode(n, "//*:protocol").getTextContent();
    //
    // if (protocol.contains("threddscatalog")) {
    // String url = xdoc.evaluateNode(n, "*:CI_OnlineResource/*:linkage/*:URL").getTextContent();
    // if (url.endsWith(".html")) {
    // url = url.replace(".html", ".xml");
    // } else if (url.endsWith(".htm")) {
    // url = url.replace(".htm", ".xml");
    // }
    // if (url.endsWith(".xml")) {
    // urls.add(url);
    // }
    // }
    // }
    // for (String s : urls) {
    // List<OriginalMetadata> omThredds = getOriginalMetadataFromTHREDDS(s, null);
    // for (OriginalMetadata originalThredds : omThredds) {
    // originalThredds.setSchemeURI(CommonNameSpaceContext.THREDDS_NS_URI);
    // ret.addRecord(originalThredds);
    // }
    // }
    //
    // }


    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("sdi.eea.europa.eu")) {
	    return super.supports(source);
	} else {
	    return false;
	}
    }
}
