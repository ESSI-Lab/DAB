package eu.essi_lab.accessor.csw.mapper;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mapper from EEA SDI to set IN situ data
 *
 * @author roncella
 */
public class AGAMEMapper extends FileIdentifierMapper {

    private static final String AGAME_MAPPER_ERROR = "AGAME_MAPPER_ERROR";
    private String HREF_ATTRIBUTE = "@*:href";
    private String WMS_URL = "https://spatialnode.elter.cerit-sc.cz/geoserver/ows?";
    private String WMS_PROTOCOL = "urn:ogc:serviceType:WebMapService:1.3.0:HTTP";

    public AGAMEMapper() {
	// empty constructor needed for service loader
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.AGAME_NS_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    // extracting information URL from the keywords
	    // PARAMETER IDENTIFIERS (map of identifier;label couples)
	    Set<ComparableEntry<String, String>> parameters = new HashSet<>();
	    Node[] keywords = reader.evaluateNodes(
		    "//*:identificationInfo/*:MD_DataIdentification/*:descriptiveKeywords/*:MD_Keywords/*:keyword/*:CharacterString");
	    List<String> listUrl = new ArrayList<String>();
	    for (Node keyNode : keywords) {
		String keyword = keyNode.getTextContent();
		if (keyword != null && keyword.startsWith("http")) {
		    listUrl.add(keyword);
		}

	    }

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    MDMetadata metadata = new MDMetadata(reader.asStream());

	    MIMetadata miMetadata = new MIMetadata(metadata.getElementType());

	    Distribution distribution = miMetadata.getDistribution();

	    if (distribution == null) {

		distribution = new Distribution();
		miMetadata.setDistribution(distribution);
	    }

	    Iterator<Online> onlines = distribution.getDistributionOnlines();
	   
	    while (onlines.hasNext()) {
		Online o = onlines.next();
		o.setLinkage(WMS_URL);
		o.setProtocol(WMS_PROTOCOL);
	    }

	    for (String s : listUrl) {
		Online online = new Online();
		online.setLinkage(s);
		online.setProtocol(NetProtocolWrapper.HTTP.getCommonURN());
		online.setFunctionCode("information");
		online.setDescription("DATASET LINK");

		distribution.addDistributionOnline(online);
	    }

	    coreMetadata.setMIMetadata(miMetadata);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    AGAME_MAPPER_ERROR, //
		    e);
	}
    }

}
