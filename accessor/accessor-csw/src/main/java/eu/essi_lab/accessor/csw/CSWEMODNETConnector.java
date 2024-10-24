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
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
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

public class CSWEMODNETConnector extends CSWConnector {

    private static final String CSWEMODNETCONNECTOR__ERROR = "CSWEMODNETCONNECTOR__ERROR";

    private HashMap<String, String> identifierMap = new HashMap<String, String>();
    private HashMap<String, String> parentMap = new HashMap<String, String>();

    private Downloader downloader = new Downloader();

    /**
     * 
     */
    public static final String TYPE = "CSW EMODNET Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * This fix is needed to change the original UUID in order to avoid conflicts with other EMODNET sources already
     * brokered
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    reader = new XMLDocumentReader(str);

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    Node[] metadatas = reader.evaluateNodes("//*:MD_Metadata");
	    for (Node metadata : metadatas) {
		String uuid = UUID.randomUUID().toString();
		Node fileIdentifier = reader.evaluateNode(metadata, "*:fileIdentifier/*:CharacterString");
		String originalFileIdentifier = fileIdentifier.getTextContent();// reader.evaluateString(metadata,
										// "*:fileIdentifier/*:CharacterString");
		if (parentMap.containsKey(originalFileIdentifier)) {
		    uuid = parentMap.get(originalFileIdentifier);
		}
		identifierMap.put(originalFileIdentifier, uuid);

		Node parentIdentifier = reader.evaluateNode(metadata, "*:parentIdentifier/*:CharacterString");
		if (parentIdentifier != null) {
		    String originalParentIdentifier = parentIdentifier.getTextContent(); // reader.evaluateString(metadata,
											 // "*:parentIdentifier/*:CharacterString");
		    if (identifierMap.containsKey(originalParentIdentifier)) {
			// already brokered --> update the parent identifier
			parentIdentifier.setTextContent((identifierMap.get(originalParentIdentifier)));
		    } else {
			String parentUuid = UUID.randomUUID().toString();
			parentMap.put(originalParentIdentifier, parentUuid);
			parentIdentifier.setTextContent(parentUuid);
		    }
		}
		fileIdentifier.setTextContent(uuid);

	    }

	} catch (Exception e1) {
	    GSLoggerFactory.getLogger(getClass()).error("XPathExpressionException fixing GetRecords response", e1);

	    throw GSException.createException( //
		    getClass(), //
		    "XPathExpressionException fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWEMODNETCONNECTOR__ERROR, e1);
	}

	try {

	    File ret = File.createTempFile(getClass().getSimpleName(), ".xml");

	    FileOutputStream fos = new FileOutputStream(ret);

	    ByteArrayInputStream stream = reader.asStream();

	    IOUtils.copy(stream, fos);

	    stream.close();
	    fos.close();

	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Exception fixing GetRecords response", e);

	    throw GSException.createException( //
		    getClass(), //
		    "Exception fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWEMODNETCONNECTOR__ERROR, e);
	}

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
				    if (prot != null) {
					// check if WMS
					if (prot.contains("WMS-1.1.0") || prot.contains("WMS-1.1.1") || prot.contains("OGC:WMS")
						|| prot.contains(NetProtocols.WMS_1_1_1.getCommonURN()) || prot.contains("WMS-1.3.0")
						|| prot.contains(NetProtocols.WMS_1_3_0.getCommonURN())) {

					    String name = ISOMetadata.getStringFromCharacterString(onlineResource.getName());
					    String link = onlineResource.getLinkage().getURL();

					    if (link.toLowerCase().contains("getcapabilities") || link.toLowerCase().contains("?")) {

						String fixed = fixOnlineURL(link);
						URLPropertyType fixedURL = new URLPropertyType();
						fixedURL.setURL(fixed);

						if (name == null || name.isEmpty()) {

						    if (link.contains("DATASET=")) {
							String lname = link.split("DATASET=")[1];
							if (lname.contains("&")) {
							    lname = lname.split("&")[0];
							}
							CIOnlineResourceType cloneOnline = (CIOnlineResourceType) onlineResource.clone();
							cloneOnline.setName(MIMetadata.createCharacterStringPropertyType(lname));
							cloneOnline.setLinkage(fixedURL);
							CIOnlineResourcePropertyType toAdd = new CIOnlineResourcePropertyType();
							toAdd.setCIOnlineResource(cloneOnline);
							onlinesList.add(toAdd);
						    } else {
							// getcapabilities to retrieve layers and then fix online url
							HttpResponse<InputStream> response;
							try {
							    if (!link.toLowerCase().contains("getcapabilities")) {
								link = fixed + "service=WMS&request=GetCapabilities&version=1.3.0";
							    }
							    response = downloader.downloadResponse(link);
							} catch (Exception e) {
							    GSLoggerFactory.getLogger(getClass()).error(
								    "Error ({}) while downloading capabilities Reference URL: {}",
								    e.getMessage(), link);
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

							    CIOnlineResourceType cloneOnline = (CIOnlineResourceType) onlineResource
								    .clone();
							    cloneOnline
								    .setName(MIMetadata.createCharacterStringPropertyType(lnames.get(j)));
							    cloneOnline.setLinkage(fixedURL);
							    CIOnlineResourcePropertyType toAdd = new CIOnlineResourcePropertyType();
							    toAdd.setCIOnlineResource(cloneOnline);
							    onlinesList.add(toAdd);

							}
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
		    omd.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
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

    public static void main(String[] args) throws Exception {
	InputStream is = CSWEMODNETConnector.class.getClassLoader().getResourceAsStream("log.txt");
	String logString = IOStreamUtils.asUTF8String(is);
	String[] splittedString = logString.split("PARENT IDENTIFIER:");
	System.out.println(splittedString.length);

    }
}
